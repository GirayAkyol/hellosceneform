/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.sceneform.samples.hellosceneform;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

/**
 * This is an example activity that uses the Sceneform UX package to make common
 * AR tasks easier.
 */
public class HelloSceneformActivity extends AppCompatActivity
{
   private static final String TAG =
           HelloSceneformActivity.class.getSimpleName();
   private static final double MIN_OPENGL_VERSION = 3.0;

   private ArFragment arFragment;
   private ModelRenderable andyRenderable;
   private AnchorNode anchorNode;

   @Override
   @SuppressWarnings({ "AndroidApiChecker", "FutureReturnValueIgnored" })
   // CompletableFuture requires api level 24
   // FutureReturnValueIgnored is not valid
   protected void onCreate( Bundle savedInstanceState )
   {
      super.onCreate( savedInstanceState );


      setContentView( R.layout.activity_ux );
      arFragment =
              (ArFragment) getSupportFragmentManager().findFragmentById( R.id.ux_fragment );
      Scene scene = arFragment.getArSceneView().getScene();
      anchorNode = null;
      Node node1 = null;
      // When you build a Renderable, Sceneform loads its resources in the
      // background while returning
      // a CompletableFuture. Call thenAccept(), handle(), or check isDone()
      // before calling get().
      ModelRenderable.builder().setSource( this,
                                           R.raw.andy
                                         ).build().thenAccept( renderable -> andyRenderable = renderable ).exceptionally(
              throwable ->
              {
                 Toast toast = Toast.makeText( this,
                                               "Unable to load andy renderable",
                                               Toast.LENGTH_LONG
                                             );
                 toast.setGravity( Gravity.CENTER, 0, 0 );
                 toast.show();
                 return null;
              } );

      scene.addOnUpdateListener( frameTime ->
                                 {
                                    if ( arFragment.getArSceneView().getArFrame() == null )
                                    {
                                       Log.d( TAG,
                                              "onUpdate: No frame available"
                                            );
                                       // No frame available
                                       return;
                                    }

                                    if ( arFragment.getArSceneView().getArFrame().getCamera().getTrackingState() != TrackingState.TRACKING )
                                    {
                                       Log.d( TAG,
                                              "onUpdate: Tracking not " +
                                                      "started" + " yet"
                                            );
                                       // Tracking not started yet
                                       return;
                                    }

                                    if ( this.anchorNode == null && andyRenderable != null )
                                    {
                                       Log.d( TAG,
                                              "onUpdate: mAnchorNode is null"
                                            );
                                       Session session =
                                               arFragment.getArSceneView().getSession();
                                       Vector3 cameraPos =
                                               arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
                                       Vector3 cameraForward =
                                               arFragment.getArSceneView().getScene().getCamera().getForward();
                                       Vector3 position =
                                               Vector3.add( cameraPos,
                                                                       cameraForward.scaled(
                                                                               1.0f )
                                                                     );

                                       // Create an ARCore Anchor at the
                                       // position.
                                       Pose pose = Pose.makeTranslation(
                                               position.x,
                                               position.y,
                                               position.z
                                                                       );
                                       Anchor anchor =
                                               arFragment.getArSceneView().getSession().createAnchor(
                                               pose );

                                       anchorNode = new AnchorNode( anchor );
                                       anchorNode.setParent( arFragment.getArSceneView().getScene() );


                                       Node node = new Node();
                                       node.setRenderable( andyRenderable );
                                       node.setParent( anchorNode );
                                       node.setOnTapListener( new Node.OnTapListener()
                                       {
                                          @Override
                                          public void onTap( HitTestResult hitTestResult,
                                                             MotionEvent motionEvent )
                                          {
                                             Toast.makeText(
                                                     getApplicationContext(),
                                                     "TAPTAPTAP",
                                                     Toast.LENGTH_SHORT
                                                           ).show();
                                             Log.d( TAG, "TAPTAPTAP" );
                                          }
                                       } );
                                    }

                                 } );

   }
}
