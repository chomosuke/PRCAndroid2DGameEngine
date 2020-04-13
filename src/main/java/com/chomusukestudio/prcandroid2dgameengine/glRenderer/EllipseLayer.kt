package com.chomusukestudio.prcandroid2dgameengine.glRenderer

import android.opengl.GLES30
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
        GLES30.glUseProgram(mProgram)

        // get handle to vertex shader's vPosition member
        val mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition")

        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(mPositionHandle)

        // Prepare the triangle coordinate data
        GLES30.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
            GLES30.GL_FLOAT, false,
            0, vertexBuffer)

        // get handle to fragment shader's vColor member
        val mColorHandle = GLES30.glGetAttribLocation(mProgram, "color")
        // Set colors for drawing the circle
        GLES30.glEnableVertexAttribArray(mColorHandle)
        GLES30.glVertexAttribPointer(mColorHandle, fragmentStrides[0],
            GLES30.GL_FLOAT, false,
            0, fragmentBuffers[0]
        )

        val tCoordsHandle = GLES30.glGetAttribLocation(mProgram, "tCoords")
        GLES30.glEnableVertexAttribArray(tCoordsHandle)
        GLES30.glVertexAttribPointer(tCoordsHandle, fragmentStrides[1],
                GLES30.GL_FLOAT, false,
                0, fragmentBuffers[1]
        )

        val abHandle = GLES30.glGetAttribLocation(mProgram, "ab")
        GLES30.glEnableVertexAttribArray(abHandle)
        GLES30.glVertexAttribPointer(abHandle, fragmentStrides[2],
                GLES30.GL_FLOAT, false,
                0, fragmentBuffers[2]
        )

        GLES30.glUniform2f(GLES30.glGetUniformLocation(mProgram, "pixelSize"),
                pixelSize.x, pixelSize.y)

        // get handle to shape's transformation matrix
        val mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix")
        // comment for mMVPMatrixHandle when it's still global: Use to access and set the view transformation

        // Pass the projection and view transformation to the shader
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)

        // Draw the triangle
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)

        // check for error
        val error = GLES30.glGetError()
        if (error != GLES30.GL_NO_ERROR) {
            throw RuntimeException("GL error: $error, $mProgram")
        }

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandle)
        GLES30.glDisableVertexAttribArray(mColorHandle)
    }
}