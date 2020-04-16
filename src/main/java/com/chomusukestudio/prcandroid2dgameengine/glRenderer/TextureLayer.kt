package com.chomusukestudio.prcandroid2dgameengine.glRenderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import com.chomusukestudio.prcandroid2dgameengine.shape.Vector
import java.nio.FloatBuffer

// this layer only have a single image/texture
class TextureLayer(private val resourceId: Int,
                   vertex1: Vector, vertex2: Vector, vertex3: Vector, vertex4: Vector,
                   z: Float) : Layer(z, intArrayOf(2) /*texture 2d*/, 1, 2) {
    companion object {
        private var mProgram = -100
        fun createGLProgram() {
            mProgram = createGLProgram(vertexShaderCode, fragmentShaderCode)
        }
        private const val vertexShaderCode =
                "uniform mat4 uMVPMatrix;" +
                        "attribute vec4 vPosition;" +
                        "attribute vec2 tCoords;" +
                        "varying vec2 tCoordsF;" +
                        "void main() {" +
                        "  tCoordsF = tCoords;" +
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        "}"

        private const val fragmentShaderCode =
                "precision mediump float;" +

                        "uniform sampler2D texture;" +

                        "uniform vec4 colorBeSwapped;" +
                        "uniform vec4 colorSwappedTo;" +
                        "uniform vec4 colorOffset;" +

                        "varying vec2 tCoordsF;" +

                        "void main() {" +
                        "  vec4 color = texture2D(texture, tCoordsF);" +
                        "  if (color == colorBeSwapped) " +
                        "    color = colorSwappedTo;" +
                        "  gl_FragColor = color + colorOffset;" +
                        "}"
    }

    private var textureHandle = 0

    private val colorBeSwapped = arrayOf(0f, 0f, 0f, 0f)
    private val colorSwappedTo = arrayOf(0f, 0f, 0f, 0f)
    fun setColorSwap(colorBeSwapped: Array<Float>, colorSwappedTo: Array<Float>) {
        if (colorBeSwapped.size != this.colorBeSwapped.size
                || colorSwappedTo.size != this.colorSwappedTo.size)
            throw IllegalArgumentException()
        for (i in 0 .. 3) {
            this.colorBeSwapped[i] = colorBeSwapped[i]
            this.colorSwappedTo[i] = colorSwappedTo[i]
        }
    }

    val colorOffset = arrayOf(0f, 0f, 0f, 0f)

    init {
        triangleCoords[0] = vertex1.x
        triangleCoords[1] = vertex1.y
        triangleCoords[2] = vertex2.x
        triangleCoords[3] = vertex2.y
        triangleCoords[4] = vertex3.x
        triangleCoords[5] = vertex3.y
        triangleCoords[6] = vertex1.x
        triangleCoords[7] = vertex1.y
        triangleCoords[8] = vertex4.x
        triangleCoords[9] = vertex4.y
        triangleCoords[10] = vertex3.x
        triangleCoords[11] = vertex3.y

        // image have y axis pointing downwards
        fragmentDatas[0][0] = 0f
        fragmentDatas[0][1] = 0f
        fragmentDatas[0][2] = 1f
        fragmentDatas[0][3] = 0f
        fragmentDatas[0][4] = 1f
        fragmentDatas[0][5] = 1f
        fragmentDatas[0][6] = 0f
        fragmentDatas[0][7] = 0f
        fragmentDatas[0][8] = 0f
        fragmentDatas[0][9] = 1f
        fragmentDatas[0][10] = 1f
        fragmentDatas[0][11] = 1f
    }

    override fun drawLayer(vertexBuffer: FloatBuffer, fragmentBuffers: Array<FloatBuffer>, vertexCount: Int, mvpMatrix: FloatArray) {
        if (textureHandle == 0)
            // haven't done the lazy initialization yet
            textureHandle = loadTexture(getBitmap(context, resourceId))

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram)

        // get handle to vertex shader's vPosition member
        val mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
            0, vertexBuffer)

        // get handle to fragment shader's vColor member
        val tCoordsHandle = GLES20.glGetAttribLocation(mProgram, "tCoords")
        // Set colors for drawing the triangle
        GLES20.glEnableVertexAttribArray(tCoordsHandle)
        GLES20.glVertexAttribPointer(tCoordsHandle, fragmentStrides[0],
                GLES20.GL_FLOAT, false,
                0, fragmentBuffers[0]
        )

        val textureUniformHandle = GLES20.glGetUniformLocation(mProgram, "texture")

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(textureUniformHandle, 0)

        // get handle to shape's transformation matrix
        val mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        // comment for mMVPMatrixHandle when it's still global: Use to access and set the view transformation

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)

        setUniforms(mProgram)

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // check for error
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            throw RuntimeException("GL error: $error")
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(tCoordsHandle)
    }
    private fun setUniforms(mProgram: Int) {
        GLES20.glUniform4f(GLES20.glGetUniformLocation(mProgram, "colorBeSwapped"),
                colorBeSwapped[0], colorBeSwapped[1], colorBeSwapped[2], colorBeSwapped[3])

        GLES20.glUniform4f(GLES20.glGetUniformLocation(mProgram, "colorSwappedTo"),
                colorSwappedTo[0], colorSwappedTo[1], colorSwappedTo[2], colorSwappedTo[3])

        GLES20.glUniform4f(GLES20.glGetUniformLocation(mProgram, "colorOffset"),
                colorOffset[0], colorOffset[1], colorOffset[2], colorOffset[3])
    }

    protected fun finalize() {
        GLES20.glDeleteTextures(1, intArrayOf(textureHandle), 0)
    }
}

fun getBitmap(context: Context, resourceId: Int): Bitmap {
    val options = BitmapFactory.Options()
    options.inScaled = false // No pre-scaling

    return BitmapFactory.decodeResource(context.resources, resourceId, options)
}

fun loadTexture(bitmap: Bitmap): Int {
    val textureHandle = IntArray(1)
    GLES20.glGenTextures(1, textureHandle, 0)
    if (textureHandle[0] == 0) {
        throw RuntimeException("Error generating texture handle.")
    }

    // Bind to the texture in OpenGL
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

    // Set filtering
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

    // Load the bitmap into the bound texture.
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

    // generate mipmaps for GL_LINEAR_MIPMAP_LINEAR texture min filter
    GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)

    // Recycle the bitmap, since its data has been loaded into OpenGL.
    bitmap.recycle()

    return textureHandle[0]
}