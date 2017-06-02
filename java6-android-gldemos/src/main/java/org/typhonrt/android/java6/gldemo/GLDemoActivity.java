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
package org.typhonrt.android.java6.gldemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import org.typhonrt.commons.java6.opengl.utils.*;

import org.typhonrt.android.java6.gldemo.gles31.invert.*;

import org.typhonrt.android.java6.opengl.utils.AndroidGLESUtil;

/**
 * GLDemoActivity - Provides a ListView to select OpenGL demo. Warns user if the device GL version is lower than
 * the required version from the given GL demo selected.
 */
public class GLDemoActivity extends ComputeInvertActivity implements GestureDetector.OnGestureListener
{
   public static final int    s_UI_OPTIONS = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    | View.SYSTEM_UI_FLAG_FULLSCREEN
    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

   private GLHeaderAdapter    adapter;

   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      final IGLVersion deviceGLVersion = AndroidGLESUtil.getGLVersion(this);

      if( deviceGLVersion.lessThan(XeGLES3.GLES3_1))
         showErrorDlg();
   }

   /**
    * Handles setting immersive sticky when the window is focused. Necessary when an Activity is minimized and
    * brought back to front.
    *
    * @param hasFocus
    */
   @Override
   public void onWindowFocusChanged(boolean hasFocus)
   {
      super.onWindowFocusChanged(hasFocus);

      if (hasFocus)
      {
         getWindow().getDecorView().setSystemUiVisibility(s_UI_OPTIONS);
      }
   }

   private void showErrorDlg()
   {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);

      builder.setIcon(android.R.drawable.ic_dialog_alert);

      builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);

      builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener()
      {
         public void onClick(DialogInterface dialog, int id) {}
      });

      builder.create().show();
   }

   @Override
   public boolean onDown(MotionEvent e)
   {
      return true;
   }

   @Override
   public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
   {
      return true;
   }

   @Override
   public void onLongPress(MotionEvent e)
   {

   }

   @Override
   public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
   {
      return true;
   }

   @Override
   public void onShowPress(MotionEvent e)
   {

   }

   @Override
   public boolean onSingleTapUp(MotionEvent e)
   {
      return true;
   }
}
