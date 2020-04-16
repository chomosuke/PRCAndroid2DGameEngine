package com.chomusukestudio.prcandroid2dgameengine.glRenderer

import android.content.Context
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import com.chomusukestudio.prcandroid2dgameengine.shape.Vector
import com.chomusukestudio.prcandroid2dgameengine.threadClasses.ProcessWaiter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.locks.ReentrantLock

// if any layer.triangleCoords[i1] contain this value then it's unused
const val UNUSED = -107584485858583778999908789293009999999f
const val COORDS_PER_VERTEX = 2
const val CPT = COORDS_PER_VERTEX * 3 // number of coordinates per vertex in this array

abstract class Layer(val z: Float, protected val fragmentStrides: IntArray, initialNumOfUnit: Int, private val trianglePerUnit: Int) { // depth for the drawing order

    private var size = initialNumOfUnit * trianglePerUnit // number of triangle (including unused) in this layer
    // drawing order from the one with the largest z value to the one with the smallest z value

    var triangleCoords: FloatArray = FloatArray(size * CPT) { UNUSED }// coordinate of triangles
    private var vertexBuffer: FloatBuffer = generateFloatBuffer(triangleCoords.size)

    private var vertexCount: Int = size * 3// number of vertex for each layer
    // number of triangle times 3

    var fragmentDatas: Array<FloatArray> = Array(fragmentStrides.size) {
        FloatArray(size * fragmentStrides[it] * 3) } // data for each triangle vertex to be used in fragment shader
    private var fragmentBuffers: Array<FloatBuffer> = Array(fragmentStrides.size) { generateFloatBuffer(fragmentDatas[it].size) }

    private fun generateFloatBuffer(size: Int): FloatBuffer {
        // initialize vertex byte buffer for shape coordinates
        val bb = ByteBuffer.allocateDirect(
            // (number of coordinate values * 4 bytes per float)
            size * 4)
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder())

        // create a floating point buffer from the ByteBuffer
        return bb.asFloatBuffer()
    }

    fun passArraysToBuffers() {

        drawLock.lock() // not passing arrays to buffers when it's still drawing

        // offset the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords)
        // set the buffer to read the first coordinate
        vertexBuffer.position(0)
        for (i in fragmentBuffers.indices) {
            // offset the coordinates to the FloatBuffer
            fragmentBuffers[i].put(fragmentDatas[i])
            // set the buffer to read the first coordinate
            fragmentBuffers[i].position(0)
        }

        drawLock.unlock()
    }

    private var lastUsedCoordIndex = 0 // should increase performance by ever so slightly, isn't really necessary.
    @Synchronized fun getCoordPointer(): Int {
//        if (!Thread.currentThread().name.equals("nextFrameThread"))
//            Log.d("currentThread", Thread.currentThread().name)
        var i = 0
        while (true) {
            // if lastUsedCoordIndex has reached vertexCount then bring it back to 0
            if (lastUsedCoordIndex == vertexCount * COORDS_PER_VERTEX)
                lastUsedCoordIndex = 0

            if (i >= vertexCount * COORDS_PER_VERTEX / 2) {
                // if half of all before vertexCount does not have unused triangle rightEnd then increment
                lastUsedCoordIndex = incrementVertexCountAndGiveNewCoordsPointer()
                return lastUsedCoordIndex
            }

            if (triangleCoords[lastUsedCoordIndex] == UNUSED) {
                // found an unused coords
                return lastUsedCoordIndex
            }

            lastUsedCoordIndex += CPT * trianglePerUnit
            i += CPT * trianglePerUnit
        }
    }

    fun getFragmentPointer(coordPointer: Int, index: Int): Int {
        return coordPointer / CPT * fragmentStrides[index] * 3
    }

    private fun increaseSize() {
        size = (size * 1.5).toInt() / trianglePerUnit * trianglePerUnit

        // store arrays
        val oldTriangleCoords = triangleCoords
        // create new arrays with new size
        triangleCoords = FloatArray(CPT * size)
        // copy old array to new array
        System.arraycopy(oldTriangleCoords, 0, triangleCoords, 0, oldTriangleCoords.size)
        // initialize the new part of the array
        for (i in oldTriangleCoords.size until triangleCoords.size)
            triangleCoords[i] = UNUSED

        // repeat for fragmentDatas
        for (i in fragmentDatas.indices) {
            val oldFragmentData = fragmentDatas[i]
            fragmentDatas[i] = FloatArray(fragmentStrides[i] * 3 * size)
            System.arraycopy(oldFragmentData, 0, fragmentDatas[i], 0, oldFragmentData.size)
        }

        // size buffers with new arrays' sizes
        vertexBuffer = generateFloatBuffer(triangleCoords.size)
        fragmentBuffers = Array(fragmentStrides.size) { generateFloatBuffer(fragmentDatas[it].size) }
        // pass arrays to the new setup buffer
        passArraysToBuffers()

        // log it
        if (vertexCount % 50 == 0)
            Log.d("number of triangle draw", "" + vertexCount / 3)
        //                else
        //                    Log.v("number of triangle draw", "" + layer.vertexCount / 3);
    }

    private val vertexPerUnit = 3 * trianglePerUnit
    private fun incrementVertexCountAndGiveNewCoordsPointer(): Int {
        val coordsPointerToBeReturned = vertexCount * COORDS_PER_VERTEX
        // vertexCount have to be multiple of 3
        val newVertexCount = (vertexCount * 1.25).toInt() / vertexPerUnit * vertexPerUnit // 25% more triangle
        vertexCount = if (newVertexCount != vertexCount) newVertexCount else vertexCount * 2
        while (vertexCount > size * 3) {
            increaseSize()
        } // check if index out of bound.
        return coordsPointerToBeReturned
    }

    companion object {
        fun createGLProgram(vertexShaderCode: String, fragmentShaderCode: String): Int {
            val program = GLES30.glCreateProgram()
            // can't do in the declaration as will return 0 because not everything is prepared

            val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
            val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)

            // offset the vertex shader to program
            GLES30.glAttachShader(program, vertexShader)

            // offset the fragment shader to program
            GLES30.glAttachShader(program, fragmentShader)

            // creates OpenGL ES program executables
            GLES30.glLinkProgram(program)

            // check for errors in glLinkProgram
            val linkStatus = IntArray(1)
            GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES30.GL_TRUE) {
                throw RuntimeException("gl program not successful linked: " + linkStatus[0] +
                        "\n" + GLES30.glGetProgramInfoLog(program) +
                        "\nvertexShader's Errors:\n" + GLES30.glGetShaderInfoLog(vertexShader) +
                        "\nfragmentShader's Errors:\n" + GLES30.glGetShaderInfoLog(fragmentShader) +
                        "\n" + vertexShaderCode +
                        "\n" + fragmentShaderCode)
            }
            return program
        }
        private fun loadShader(type: Int, shaderCode: String): Int {

            // create a vertex shader type (GLES31.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES31.GL_FRAGMENT_SHADER)
            val shader = GLES30.glCreateShader(type)

            // offset the source code to the shader and compile it
            GLES30.glShaderSource(shader, shaderCode)
            GLES30.glCompileShader(shader)

            return shader
        }
    }

    lateinit var pixelSize: Vector
        internal set
    lateinit var context: Context
        internal set

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private var mvpMatrix = FloatArray(16)
    internal fun setLRBTEnds(LRBTEnds: FloatArray) {
        if (LRBTEnds.size != 4)
            throw IllegalArgumentException("leftRightBottomTopBoundaries' size isn't 4")

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method

        val mProjectionMatrix = FloatArray(16)
        val mViewMatrix = FloatArray(16)

        Matrix.orthoM(
            mProjectionMatrix, 0, LRBTEnds[0], LRBTEnds[1],
            LRBTEnds[2], LRBTEnds[3], -1000f, 1000f)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mvpMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)
    }

    private val drawLock = ReentrantLock() // for buffer not updating during draw
    fun drawLayer() {
        drawLock.lock()
        drawLayer(vertexBuffer, fragmentBuffers, vertexCount, mvpMatrix)
        drawLock.unlock()
    }

    protected abstract fun drawLayer(vertexBuffer: FloatBuffer, fragmentBuffers: Array<FloatBuffer>, vertexCount: Int, mvpMatrix: FloatArray)
}

