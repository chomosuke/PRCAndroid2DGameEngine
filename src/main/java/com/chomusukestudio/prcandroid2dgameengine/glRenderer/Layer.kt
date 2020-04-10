package com.chomusukestudio.prcandroid2dgameengine.glRenderer

import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

// if any layer.triangleCoords[i1] or layer.colors[i2] contain this value then it's unused
const val UNUSED = -107584485858583778999908789293009999999f
const val COORDS_PER_VERTEX = 2
const val CPT = COORDS_PER_VERTEX * 3 // number of coordinates per vertex in this array

abstract class Layer(val z: Float, private val fragmentBlockSize: Int, initialNumOfTriangle: Int) { // depth for the drawing order

    private val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    private var vertexBuffer: FloatBuffer
    private var fragmentBuffer: FloatBuffer

    private var size = initialNumOfTriangle // number of triangle (including unused) in this layer
    // drawing order from the one with the largest z value to the one with the smallest z value

    var triangleCoords: FloatArray // coordinate of triangles

    private var vertexCount: Int = initialNumOfTriangle * 3// number of vertex for each layer
    // number of triangle times 3

    var fragmentData: FloatArray // colors of triangles

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
                // if half of all before vertexCount does not have unused triangle rightEnd
                lastUsedCoordIndex = incrementVertexCountAndGiveNewCoordsPointer()
                return lastUsedCoordIndex
            }

            if (triangleCoords[lastUsedCoordIndex] == UNUSED) {
                // found an unused coords
                return lastUsedCoordIndex
            }

            lastUsedCoordIndex += CPT
            i += CPT
        }
    }

    init {
        // mark ALL colors and triangleCoords as UNUSED
        triangleCoords = FloatArray(size * CPT) { UNUSED }
        fragmentData = FloatArray(size * fragmentBlockSize) { UNUSED }

        // setupBuffers
        val bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.size * 4)
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder())

        // create a floating score buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer()

        // initialize vertex byte buffer for shape coordinates
        val bb2 = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                fragmentData.size * 4)
        // use the device hardware's native byte order
        bb2.order(ByteOrder.nativeOrder())

        // create a floating score buffer from the ByteBuffer
        fragmentBuffer = bb2.asFloatBuffer()
    }

    private fun setupBuffers() {
//        while (changingBuffer || drawing); // don't change buffer while changing buffer or drawing

        changingBuffer = true

        newBufferPassedToArray = false

        // initialize vertex byte buffer for shape coordinates
        val bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.size * 4)
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder())

        // create a floating score buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer()

        // initialize vertex byte buffer for shape coordinates
        val bb2 = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                fragmentData.size * 4)
        // use the device hardware's native byte order
        bb2.order(ByteOrder.nativeOrder())

        // create a floating score buffer from the ByteBuffer
        fragmentBuffer = bb2.asFloatBuffer()

        changingBuffer = false
    }
    @Volatile private var changingBuffer = false
    @Volatile private var newBufferPassedToArray = false
    fun passArraysToBuffers() {
//        while (changingBuffer || drawing); // don't change buffer while changing buffer or drawing

        changingBuffer = true

        // offset the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords)
        // set the buffer to read the first coordinate
        vertexBuffer.position(0)
        // offset the coordinates to the FloatBuffer
        fragmentBuffer.put(fragmentData)
        // set the buffer to read the first coordinate
        fragmentBuffer.position(0)

        newBufferPassedToArray = true

        changingBuffer = false
    }

    fun getFragmentPointer(coordPointer: Int): Int {
        return coordPointer / CPT * fragmentBlockSize
    }

    private fun increaseSize() {
        size = (size * 1.5).toInt()

        // store arrays
        val oldTriangleCoords = triangleCoords
        val oldColor = fragmentData

        // create new arrays with new size
        triangleCoords = FloatArray(CPT * size)
        fragmentData = FloatArray(fragmentBlockSize * size)

        // copy old array to new array
        System.arraycopy(oldTriangleCoords, 0, triangleCoords, 0, oldTriangleCoords.size)
        System.arraycopy(oldColor, 0, fragmentData, 0, oldColor.size)

        // initialize the new part of the array
        for (i in oldTriangleCoords.size until triangleCoords.size)
            triangleCoords[i] = UNUSED
        for (i in oldColor.size until fragmentData.size)
            fragmentData[i] = UNUSED

        // size buffers with new arrays' sizes
        setupBuffers()
        // pass arrays to the new setup buffer
        passArraysToBuffers()

        // log it
        if (vertexCount % 50 == 0)
            Log.d("number of triangle draw", "" + vertexCount / 3)
        //                else
        //                    Log.v("number of triangle draw", "" + layer.vertexCount / 3);
    }

    private fun incrementVertexCountAndGiveNewCoordsPointer(): Int {
        val coordsPointerToBeReturned = vertexCount * COORDS_PER_VERTEX
        // vertexCount have to be multiple of 3
        val newVertexCount = (vertexCount * 1.25).toInt() / 3 * 3 // 25% more triangle
        vertexCount = if (newVertexCount != vertexCount) newVertexCount else vertexCount * 2
        while (vertexCount > size * 3) {
            increaseSize()
        } // check if index out of bound.
        return coordsPointerToBeReturned
    }


    companion object {

        // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
        private val mProjectionMatrix = FloatArray(16)
        private val mViewMatrix = FloatArray(16)
        private val mvpMatrix = FloatArray(16)

        fun refreshMatrix(leftRightBottomTopEnd: Array<Float>) {
            // this projection matrix is applied to object coordinates
            // in the onDrawFrame() method

            val leftEnd = leftRightBottomTopEnd[0]
            val rightEnd = leftRightBottomTopEnd[1]
            val bottomEnd = leftRightBottomTopEnd[2]
            val topEnd = leftRightBottomTopEnd[3]
            // for debugging
//        Matrix.orthoM(mProjectionMatrix, 0, rightEnd * 2, leftEnd * 2,
//                bottomEnd * 2, topEnd * 2, -1000f, 1000f)
            Matrix.orthoM(
                mProjectionMatrix, 0, leftEnd, rightEnd,
                    bottomEnd, topEnd, -1000f, 1000f)
            // this game shall be optimised for any aspect ratio as nowXY all rightEnd, leftEnd, bottomEnd and topEnd are visibility

            // Set the camera position (View matrix)
            Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

            // Calculate the projection and view transformation
            Matrix.multiplyMM(mvpMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)
        }

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
                Log.e("glLinkProgram", "program not successful linked: " + linkStatus[0] +
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

    protected abstract val mProgram: Int
    @Volatile private var drawing = false
    fun drawLayer() {
//        while (changingBuffer || !newBufferPassedToArray);

        drawing = true

        drawLayer(mProgram, vertexBuffer, fragmentBuffer, vertexStride, vertexCount, mvpMatrix)

        drawing = false
    }

    protected abstract fun drawLayer(mProgram: Int, vertexBuffer: FloatBuffer, fragmentBuffer: FloatBuffer, vertexStride: Int, vertexCount: Int, mvpMatrix: FloatArray)
}

