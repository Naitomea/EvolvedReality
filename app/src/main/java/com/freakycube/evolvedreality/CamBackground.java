package com.freakycube.evolvedreality;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Laurent on 14/06/2015.
 */
public class CamBackground {
    private static final String TAG = "HUD_InfoTest";

    private FloatBuffer vertexBuffer, colorsBuffer, UVBuffer;
    /*private final float vertices[] = new float[]{
            -1.78f, 1.0f, 0.0f,
            -1.78f, -1.0f, 0.0f,
            1.78f, -1.0f, 0.0f,
            -1.78f, 1.0f, 0.0f,
            1.78f, 1.0f, 0.0f,
            1.78f, -1.0f, 0.0f
    };*/
    private final float vertices[] = new float[]{
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f
    };
    private final float colors[] = new float[]{
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f
            /*0.0f, 0.94f, 1.0f, 1.0f,
            0.0f, 0.94f, 1.0f, 1.0f,
            0.0f, 0.94f, 1.0f, 1.0f,
            0.0f, 0.94f, 1.0f, 1.0f,
            0.0f, 0.94f, 1.0f, 1.0f,
            0.0f, 0.94f, 1.0f, 1.0f*/
    };
    private final float UV[] = new float[]{
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
    };

    private float[] modelMatrix = new float[16], modelViewMatrix = new float[16], MVPMatrix = new float[16];
    private int MVPMatrixParam, positionParam, colorParam, textureParam, UVParam;

    private int texture;

    private int shaderProgram;
    private String vertexShaderCode, fragmentShaderCode;

    private Camera camera;
    private SurfaceTexture camSurface;

    /* ########################### */
    private void startCamera(){
        camSurface = new SurfaceTexture(texture);
        /*camSurface.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                this.cardboardView.requestRender();
            }
        });*/

        camera = Camera.open();

        try{
            camera.setPreviewTexture(camSurface);
            camera.startPreview();
        }
        catch (IOException ioe){
            Log.w(TAG, "CAM LAUNCH FAILED");
        }
    }

    public void updateTexImage(){
        camSurface.updateTexImage();
    }
    /* ########################### */

    public CamBackground(){
        vertexShaderCode = Utils.readTextFile(R.raw.ctt_vshader);
        fragmentShaderCode = Utils.readTextFile(R.raw.ctt_fshader);
        //texture = Utils.loadTexture(R.drawable.hud_info);
        texture = Utils.createTexture();
        startCamera();

        ByteBuffer bbVertices = ByteBuffer.allocateDirect(vertices.length * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        vertexBuffer = bbVertices.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        ByteBuffer bbColors = ByteBuffer.allocateDirect(colors.length * 4);
        bbColors.order(ByteOrder.nativeOrder());
        colorsBuffer = bbColors.asFloatBuffer();
        colorsBuffer.put(colors);
        colorsBuffer.position(0);

        ByteBuffer bbUV = ByteBuffer.allocateDirect(UV.length * 4);
        bbUV.order(ByteOrder.nativeOrder());
        UVBuffer = bbUV.asFloatBuffer();
        UVBuffer.put(UV);
        UVBuffer.position(0);

        int vertexShader = Utils.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = Utils.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShader);
        GLES20.glAttachShader(shaderProgram, fragmentShader);
        GLES20.glLinkProgram(shaderProgram);
        GLES20.glUseProgram(shaderProgram);

        MVPMatrixParam = GLES20.glGetUniformLocation(shaderProgram, "u_MVPMatrix");
        textureParam = GLES20.glGetUniformLocation(shaderProgram, "u_Texture");
        positionParam = GLES20.glGetAttribLocation(shaderProgram, "a_Position");
        colorParam = GLES20.glGetAttribLocation(shaderProgram, "a_Color");
        UVParam = GLES20.glGetAttribLocation(shaderProgram, "a_UV");

        GLES20.glEnableVertexAttribArray(positionParam);
        GLES20.glEnableVertexAttribArray(colorParam);
        GLES20.glEnableVertexAttribArray(UVParam);

        /*Matrix.setIdentityM(modelMatrix, 0);
        // Object first appears directly in front of user.
        Matrix.translateM(modelMatrix, 0, 0, 0, -3.0f);*/
    }

    public void draw(float[] viewMatrix, float[] projectionMatrix){
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);

        GLES20.glUseProgram(shaderProgram);

        // Calcul matrix
        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);


        // Set the ModelViewProjection matrix in the shader.
        GLES20.glUniformMatrix4fv(MVPMatrixParam, 1, false, MVPMatrix, 0);
        // Set the position of the HUD
        GLES20.glVertexAttribPointer(positionParam, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        // Set color of the HUD, again for shading
        GLES20.glVertexAttribPointer(colorParam, 4, GLES20.GL_FLOAT, false, 0, colorsBuffer);


        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(textureParam, 0);
        // Set the UVs of the HUD
        GLES20.glVertexAttribPointer(UVParam, 2, GLES20.GL_FLOAT, false, 0, UVBuffer);


        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.length / 2);

        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
}
