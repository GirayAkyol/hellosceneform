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
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class HelloSceneformActivity extends AppCompatActivity
{
   private static final String TAG = HelloSceneformActivity.class.getSimpleName();

   private ArFragment arFragment;
   private ModelRenderable andyRenderable;
   private Anchor anchor;
   private Node node;
   private AnchorNode anchorNode;
   private boolean placed;

   @Override
   @SuppressWarnings({ "AndroidApiChecker", "FutureReturnValueIgnored" })
   // CompletableFuture requires api level 24
   // FutureReturnValueIgnored is not valid
   protected void onCreate( Bundle savedInstanceState )
   {
      super.onCreate( savedInstanceState );


      setContentView( R.layout.activity_ux );
      arFragment = (ArFragment) getSupportFragmentManager().findFragmentById( R.id.ux_fragment );
      Scene scene = arFragment.getArSceneView().getScene();
      anchorNode = null;
      placed = false;
      node = null;
      anchor = null;
      // When you build a Renderable, Sceneform loads its resources in the
      // background while returning
      // a CompletableFuture. Call thenAccept(), handle(), or check isDone()
      // before calling get().
      ModelRenderable.builder().setSource( this, R.raw.andy ).build().thenAccept( new Consumer<ModelRenderable>()
      {
         @Override
         public void accept( ModelRenderable renderable )
         {
            andyRenderable = renderable;
         }
      } ).exceptionally( new Function<Throwable, Void>()
      {
         @Override
         public Void apply( Throwable throwable )
         {
            Toast toast = Toast.makeText( HelloSceneformActivity.this, "Unable to load andy renderable", Toast.LENGTH_LONG );
            toast.setGravity( Gravity.CENTER, 0, 0 );
            toast.show();
            return null;
         }
      } );

      scene.addOnUpdateListener( new Scene.OnUpdateListener()
      {
         @Override
         public void onUpdate( FrameTime frameTime )
         {
            if ( arFragment.getArSceneView().getArFrame() == null )
            {
               Log.d( TAG, "onUpdate: No frame available" );
               // No frame available
               return;
            }

            if ( arFragment.getArSceneView().getArFrame().getCamera().getTrackingState() != TrackingState.TRACKING )
            {
               Log.d( TAG, "onUpdate: Tracking not " + "started" + " yet" );
               // Tracking not started yet
               return;
            }

            if ( HelloSceneformActivity.this.anchorNode == null && andyRenderable != null && !placed )
            {
               Log.d( TAG, "onUpdate: mAnchorNode is null" );
               Collection<Plane> planeCollection = arFragment.getArSceneView().getSession().getAllTrackables( Plane.class );

               for ( Plane p : planeCollection )
               {
                  if ( p.getType() == Plane.Type.HORIZONTAL_UPWARD_FACING && p.getTrackingState() == TrackingState.TRACKING )
                  {
                     for ( HitResult hitResult : arFragment.getArSceneView().getArFrame().hitTest( findViewById( R.id.ux_fragment ).getWidth() / 2f,
                                                                                                   findViewById( R.id.ux_fragment ).getHeight() / 2f
                                                                                                 ) )
                     {
                        anchor = p.createAnchor( hitResult.getHitPose() );
                        anchorNode = new AnchorNode( anchor );
                        anchorNode.setParent( arFragment.getArSceneView().getScene() );
                        node = new Node();
                        node.setParent( anchorNode );
                        node.setRenderable( andyRenderable );
                        node.setWorldPosition( new Vector3( anchor.getPose().tx(), anchor.getPose().ty(),
                                                            //anchor.getPose().compose( Pose.makeTranslation( 0f, 0.05f, 0f ) ).ty(),
                                                            anchor.getPose().tz()
                        ) );
                        placed = true;
                        break; //TODO
                     }


                     //break;//TODO
                  }
               }

               Vector3 cameraPos = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
               Vector3 cameraForward = arFragment.getArSceneView().getScene().getCamera().getForward();
               Vector3 position = Vector3.add( cameraPos, cameraForward.scaled( 1.0f ) );

               // Create an ARCore Anchor at the
               // position.
               Pose pose = Pose.makeTranslation( position.x, position.y, position.z );
               Anchor anchor = arFragment.getArSceneView().getSession().createAnchor( pose );

               anchorNode = new AnchorNode( anchor );
               anchorNode.setParent( arFragment.getArSceneView().getScene() );


               Node node = new Node();
               node.setRenderable( andyRenderable );
               node.setParent( anchorNode );
               node.setOnTapListener( new Node.OnTapListener()
               {
                  @Override
                  public void onTap( HitTestResult hitTestResult, MotionEvent motionEvent )
                  {
                     Toast.makeText( getApplicationContext(), "TAPTAPTAP", Toast.LENGTH_SHORT ).show();
                     Log.d( TAG, "TAPTAPTAP" );
                  }
               } );
            }

         }
      } );

   }
}
