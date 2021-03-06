package com.chomusukestudio.prcandroid2dgameengine.glRenderer

import android.opengl.GLES20
import java.nio.FloatBuffer

class ShapeLayer(z: Float) : Layer(z, intArrayOf(4), 100, 1) {

    companion object {
        private var mProgram = -100
        fun createGLProgram() {
            mProgram = createGLProgram(vertexShaderCode, fragmentShaderCode)
        }
        private const val vertexShaderCode =
        // This matrix member variable provides a hook to manipulate
                // the coordinates of the objects that use this vertex shader
                "uniform mat4 uMVPMatrix;" +

                        "attribute vec4 vPosition;" +
                        "attribute vec4 aColor;" +

                        "varying vec4 vColor;" +

                        "void main() {" +
                        "vColor = aColor;" +
                        // the matrix must be included as a modifier of gl_Position
                        // Note that the uMVPMatrix factor *must be first* in order
                        // for the matrix multiplication product to be correct.
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        "}"

        private const val fragmentShaderCode =
                "precision mediump float;" +
                        "varying vec4 vColor;" +
                        "void main() {" +
                        "  gl_FragColor = vColor;" +
                        "}"
    }

    override fun drawLayer(vertexBuffer: FloatBuffer, fragmentBuffers: Array<FloatBuffer>, vertexCount: Int, mvpMatrix: FloatArray) {
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
        val mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor")
        // Set colors for drawing the triangle
        GLES20.glEnableVertexAttribArray(mColorHandle)
        GLES20.glVertexAttribPointer(mColorHandle, fragmentStrides[0],
                GLES20.GL_FLOAT, false,
                0, fragmentBuffers[0]
        )

        // get handle to shape's transformation matrix
        val mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        // comment for mMVPMatrixHandle when it's still global: Use to access and set the view transformation

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // check for error
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            throw RuntimeException("GL error: $error, $mProgram")
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mColorHandle)
    }
}
