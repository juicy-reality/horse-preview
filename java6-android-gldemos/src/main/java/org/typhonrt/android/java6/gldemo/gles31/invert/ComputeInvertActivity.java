/**
 * Copyright 2015 Michael Leahy / TyphonRT, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.typhonrt.android.java6.gldemo.gles31.invert;

import android.content.res.Resources;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.SystemClock;


import org.typhonrt.android.java6.gldemo.shared.BaseDemoActivity;

import org.typhonrt.android.java6.opengl.utils.AndroidGLES31Util;
import org.typhonrt.android.java6.opengl.utils.GLBufferUtil;
import org.typhonrt.commons.java6.opengl.utils.XeGLES3;

import org.typhonrt.android.java6.data.option.model.OptionModel;

import org.typhonrt.android.java6.gldemo.R;


import static android.opengl.GLES31.*;

public class ComputeInvertActivity extends BaseDemoActivity
{
   private static final String   s_VERT_SHADER_FILE = "shaders/gles30/common/directTexture.vert";
   private static final String   s_FRAG_SHADER_FILE = "shaders/gles30/common/directTexture.frag";

   private static final String   s_COMP_SHADER_FILE = "shaders/gles31/color/invertTexture.comp";
   private static final String   s_CLEARE_SHADER_FILE = "shaders/gles31/color/clearTexture.comp";

   private static final String   S_COMP_SHADER_HEADER = "#version 310 es\n#define LOCAL_SIZE %d\n";

   private int                   workgroupSize;

   private static final int WIDTH = 640;
   private static final int HEIGHT = 480;

   private int                   directProgramID, computeProgramID, clearProgramID;
   private int                   sourceTextureId, depthTextureId, resultTextureId;

   private OptionModel           invertEnabled = new OptionModel("Invert", true);

   private float[] mViewMatrix = new float[16];
   private float[] mModelMatrix = new float[16];
   private float[] mProjectionMatrix = new float[16];
   private float[] mMVPMatrix = new float[16];

   private int mMVPMatrixHandle;

   public ComputeInvertActivity()
   {
      super(XeGLES3.GLES3_1);  // Require OpenGL ES 3.1 context
   }

   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      drawerControl.setOptionModels(new OptionModel[]{invertEnabled});
   }

   public void onGLDrawFrame()
   {
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

      // Do a complete rotation every 10 seconds.
      long time = SystemClock.uptimeMillis() % 5000L - 2500L;
      float angleInDegrees = (-90.0f / 10000.0f) * ((int) time);

      Matrix.setIdentityM(mModelMatrix, 0);
      Matrix.rotateM(mModelMatrix, 0, 5, 1.0f, 0.0f, 0.0f);
      Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
      Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, 1.0f);

      // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
      // (which currently contains model * view).
      Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

      Matrix.frustumM(mProjectionMatrix, 0, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 40.0f);

      // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
      // (which now contains model * view * projection).
      Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);



      if (invertEnabled.getBooleanValue())
      {
         runClearShader(resultTextureId);
         runComputeFilter(depthTextureId, resultTextureId);
         drawTexture(resultTextureId, sourceTextureId);
      }
      else
      {
         drawTexture(sourceTextureId, sourceTextureId);
      }
   }

   private void drawTexture(int textureID, int textureID2)
   {
      glUseProgram(directProgramID);

      glActiveTexture(GL_TEXTURE0);
      glBindTexture(GL_TEXTURE_2D, textureID);

      glActiveTexture(GL_TEXTURE1);
      glBindTexture(GL_TEXTURE_2D, textureID2);

      glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, 4, 1);
   }

   private void runClearShader(int resultTexId)
   {
      glUseProgram(clearProgramID);

      glBindImageTexture(1, resultTexId, 0, false, 0, GL_READ_WRITE, GL_RGBA8);

      glDispatchCompute(WIDTH / workgroupSize, HEIGHT / workgroupSize, 1);

      glMemoryBarrier(GL_COMPUTE_SHADER_BIT);
   }
   private void runComputeFilter(int sourceTexId, int resultTexId)
   {
      glUseProgram(computeProgramID);

      glBindImageTexture(0, sourceTexId, 0, false, 0, GL_READ_ONLY, GL_RGBA8);
      glBindImageTexture(1, resultTexId, 0, false, 0, GL_READ_WRITE, GL_RGBA8);
      glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

      glDispatchCompute(WIDTH / workgroupSize,  HEIGHT / workgroupSize, 1);

      // GL_COMPUTE_SHADER_BIT is the same as GL_SHADER_IMAGE_ ACCESS_BARRIER_BIT
      glMemoryBarrier(GL_COMPUTE_SHADER_BIT);
   }


   public void onGLSurfaceChanged(int width, int height)
   {
      glViewport(0, 0, width, height);

      // Setup a VBO (Vertex Buffer Object)
      // Note: A VBO is created and binded for the rest of the runtime, so we don't hold onto a GLBuffer reference.
      // Quad (vertex + u/v); height / width provides aspect ratio when width is wider than height
      GLBufferUtil.createQuadVertexUVBuffer((float) height / (float) width).bind();

      glVertexAttribPointer(glGetAttribLocation(directProgramID, "inPosition"), 3, GL_FLOAT, false,
       GLBufferUtil.s_QUAD_BUFFER_STRIDE, 0);

      glVertexAttribPointer(glGetAttribLocation(directProgramID, "aTexCoord"), 2, GL_FLOAT, false,
       GLBufferUtil.s_QUAD_BUFFER_STRIDE, GLBufferUtil.s_QUAD_UV_OFFSET);
   }

   public void onGLSurfaceCreated()
   {
      // Sets the color rendered by glClear
      glClearColor(159.0f/256.0f, 163.0f/256.0f, 164.0f/256.0f, 1.0f);  // Set background to dark purple

      glEnable(GL_BLEND);
      glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

      // init camera view
      // Position the eye behind the origin.
      final float eyeX = 0.0f;
      final float eyeY = 0.0f;
      final float eyeZ = 1.5f;

      // We are looking toward the distance
      final float lookX = 0.0f;
      final float lookY = 0.0f;
      final float lookZ = -3.0f;

      // Set our up vector. This is where our head would be pointing were we holding the camera.
      final float upX = 0.0f;
      final float upY = 1.0f;
      final float upZ = 0.0f;

      // Set the view matrix. This matrix can be said to represent the camera position.
      // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
      // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
      Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
      Resources resources = getResources();

      workgroupSize = AndroidGLES31Util.getMaxComputePowerWorkGroupSize(2);

      // Create shader programs
      directProgramID = AndroidGLES31Util.buildProgramFromAssets(resources, s_VERT_SHADER_FILE, s_FRAG_SHADER_FILE);
      computeProgramID = AndroidGLES31Util.buildProgramFromAssets(resources, s_COMP_SHADER_FILE, GL_COMPUTE_SHADER,
              String.format(S_COMP_SHADER_HEADER, workgroupSize));

      clearProgramID = AndroidGLES31Util.buildProgramFromAssets(resources, s_CLEARE_SHADER_FILE, GL_COMPUTE_SHADER,
              String.format(S_COMP_SHADER_HEADER, workgroupSize));

      // Load texture from R.drawable.flower1024 (flip due to GL coordinates)
      sourceTextureId = AndroidGLES31Util.loadTexture(resources, R.drawable.color_alpha, GL_RGBA8, true);
      depthTextureId = AndroidGLES31Util.loadTexture(resources, R.drawable.depth_rgb, GL_RGBA8, true);
      resultTextureId = AndroidGLES31Util.createTexture(GL_RGBA8, WIDTH, HEIGHT);

      mMVPMatrixHandle = glGetUniformLocation(computeProgramID, "u_MVPMatrix");

      glUseProgram(directProgramID);

      // Bind texture to texture unit 0
      glActiveTexture(GL_TEXTURE0);
      glBindTexture(GL_TEXTURE_2D, sourceTextureId);

      // Set sampler2d in GLSL fragment shader to texture unit 0
      glUniform1i(glGetUniformLocation(directProgramID, "uSourceTex"), 0);
      glUniform1i(glGetUniformLocation(directProgramID, "uSourceTex2"), 1);

      glEnableVertexAttribArray(0);
      glEnableVertexAttribArray(1);
   }
}
