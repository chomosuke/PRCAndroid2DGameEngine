package com.chomusukestudio.prcandroid2dgameengine.glRenderer

import android.opengl.GLES20
import java.nio.FloatBuffer

class EllipseLayer(z: Float): Layer(z, intArrayOf(4/*color*/, 2/*reference coords*/, 2/*pixelSize*/), 5, 2) {
    companion object {
        private var mProgram = -100
        fun createGLProgram() {
            mProgram = createGLProgram(vertexShaderCode, fragmentShaderCode)
        }
        private const val vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +

                    "uniform vec2 pixelSize;" +

                    "attribute vec4 vPosition;" +
                    "attribute vec2 tCoords;" +
                    "attribute vec4 color;" +
                    "attribute vec2 ab;" +

                    "varying vec2 tCoordsF;" +
                    "varying vec4 colorF;" +
                    "varying vec2 tPixelSizeF;" +

                    "void main() {" +
                    "  tCoordsF = tCoords;" +
                    "  colorF = color;" +

                    // calculate tPixelSizeF
                    "  tPixelSizeF = pixelSize / ab;" +

                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}"

        private const val fragmentShaderCode =
            "precision mediump float;" +

                    "varying vec2 tPixelSizeF;" +
                    "varying vec4 colorF;" +
                    "varying vec2 tCoordsF;" +

                    "void main() {" +

                    "  float distanceFromCenterSq = tCoordsF.x * tCoordsF.x + tCoordsF.y * tCoordsF.y;" +
                    "  float dDistance = tPixelSizeF.x + tPixelSizeF.y;" +

                    "  if (distanceFromCenterSq < 1.0 - dDistance) " +
                    "    gl_FragColor = colorF;" +
                    "  else if (distanceFromCenterSq > 1.0 + dDistance)" +
                    "    gl_FragColor = vec4(0.0);" +
                    "  else {" +
                    // 9x antialiasing
//                    "    float dx = tPixelSizeF.x / 8.0;" +
//                    "    float dy = tPixelSizeF.y / 8.0;" +
//                    "    float x = tCoordsF.x + (dx*2.0);" +
//                    "    float y = tCoordsF.y + (dy*4.0);" +
//                    "    int count = int(x*x + y*y < 1.0);" +
//                    "    x = tCoordsF.x - dx;       y = tCoordsF.y + (dy*3.0);" +
//                    "    count += int(x*x + y*y < 1.0);" +
//                    "    x = tCoordsF.x - (dx*4.0); y = tCoordsF.y + (dy*2.0);" +
//                    "    count += int(x*x + y*y < 1.0);" +
//                    "    x = tCoordsF.x + (dx*3.0); y = tCoordsF.y + dy;" +
//                    "    count += int(x*x + y*y < 1.0);" +
//                    "    x = tCoordsF.x;            y = tCoordsF.y;" +
//                    "    count += int(x*x + y*y < 1.0);" +
//                    "    x = tCoordsF.x - (dx*3.0); y = tCoordsF.y - dy;" +
//                    "    count += int(x*x + y*y < 1.0);" +
//                    "    x = tCoordsF.x + (dx*4.0); y = tCoordsF.y - (dy*2.0);" +
//                    "    count += int(x*x + y*y < 1.0);" +
//                    "    x = tCoordsF.x + dx;       y = tCoordsF.y - (dy*3.0);" +
//                    "    count += int(x*x + y*y < 1.0);" +
//                    "    x = tCoordsF.x - (dx*2.0); y = tCoordsF.y - (dy*4.0);" +
//                    "    count += int(x*x + y*y < 1.0);" +
                    // 4x antialiasing
                    "    float dx = tPixelSizeF.x / 8.0;" +
                    "    float dy = tPixelSizeF.y / 8.0;" +
                    "    float x = tCoordsF.x + (dx*1.0);" +
                    "    float y = tCoordsF.y + (dy*3.0);" +
                    "    int count = int(x*x + y*y < 1.0);" +
                    "    x = tCoordsF.x - (dx*3.0);       y = tCoordsF.y + (dy*1.0);" +
                    "    count += int(x*x + y*y < 1.0);" +
                    "    x = tCoordsF.x + (dx*3.0); y = tCoordsF.y - (dy*1.0);" +
                    "    count += int(x*x + y*y < 1.0);" +
                    "    x = tCoordsF.x - (dx*1.0); y = tCoordsF.y - (dy*3.0);" +
                    "    count += int(x*x + y*y < 1.0);" +

                    "    gl_FragColor = colorF;" +

                    "    if (gl_FragColor.a > 1.0) gl_FragColor.a = 1.0; " +
                    "    else if (gl_FragColor.a < 0.0) gl_FragColor.a = 0.0;" +

                    "    gl_FragColor.a *= float(count) / 4.0;" +
                    "  }" +
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
        val mColorHandle = GLES20.glGetAttribLocation(mProgram, "color")
        // Set colors for drawing the circle
        GLES20.glEnableVertexAttribArray(mColorHandle)
        GLES20.glVertexAttribPointer(mColorHandle, fragmentStrides[0],
            GLES20.GL_FLOAT, false,
            0, fragmentBuffers[0]
        )

        val tCoordsHandle = GLES20.glGetAttribLocation(mProgram, "tCoords")
        GLES20.glEnableVertexAttribArray(tCoordsHandle)
        GLES20.glVertexAttribPointer(tCoordsHandle, fragmentStrides[1],
                GLES20.GL_FLOAT, false,
                0, fragmentBuffers[1]
        )

        val abHandle = GLES20.glGetAttribLocation(mProgram, "ab")
        GLES20.glEnableVertexAttribArray(abHandle)
        GLES20.glVertexAttribPointer(abHandle, fragmentStrides[2],
                GLES20.GL_FLOAT, false,
                0, fragmentBuffers[2]
        )

        GLES20.glUniform2f(GLES20.glGetUniformLocation(mProgram, "pixelSize"),
                pixelSize.x, pixelSize.y)

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