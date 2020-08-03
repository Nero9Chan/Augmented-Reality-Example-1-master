package com.applaudostudios.karcore

import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    //lateinit for Augmented Reality Fragment
    private lateinit var arFragment: ArFragment
    //lateinit for the model uri
    private lateinit var selectedObject: Uri

    private var pressTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Init Fragment
        arFragment = supportFragmentManager.findFragmentById(sceneform_fragment_view.id) as ArFragment

        //Default model
        setModelPath("rocket.sfb")

        //Tab listener for the ArFragment
        arFragment.setOnTapArPlaneListener { hitResult, plane, _ ->
            //If surface is not horizontal and upward facing
            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                //return for the callback
                return@setOnTapArPlaneListener
            }
            //create a new anchor
            val anchor = hitResult.createAnchor()
            placeObject(arFragment, anchor, selectedObject)
        }

        //Click listener for lamp and table objects
        smallTable.setOnClickListener {
            setModelPath("model.sfb")
        }
        bigLamp.setOnClickListener {
            setModelPath("LampPost.sfb")
        }
        curtain.setOnClickListener {
            setModelPath("curtain.sfb")
        }
    }

    /***
     * function to handle the renderable object and place object in scene
     */
    private fun placeObject(fragment: ArFragment, anchor: Anchor, modelUri: Uri) {
        val modelRenderable = ModelRenderable.builder()
            .setSource((fragment.requireContext()), modelUri)
            .build()
        //when the model render is build add node to scene
        modelRenderable.thenAccept { renderableObject -> addNodeToScene(fragment, anchor, renderableObject) }
        //handle error
        modelRenderable.exceptionally {
            val toast = Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT)
            toast.show()
            null
        }
    }

    /***
     * Function to a child anchor to a new scene.
     */
    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderableObject: Renderable) {
        val anchorNode = AnchorNode(anchor)
        val transformableNode = TransformableNode(fragment.transformationSystem)
        transformableNode.renderable = renderableObject

        //****************//
        //ALL scales of models should between 0.75 and 1.75
        // >= 0.75 && <= 1.75

        if(selectedObject == Uri.parse("curtain.sfb")){
            transformableNode.localScale = Vector3(0.5f, 0.5f, 0.5f)
            //transformableNode.localRotation = Quaternion.axisAngle(Vector3(0f, 40f, 0f), 40f)
            Log.d("checking", "detected curtain")
        }

        if(selectedObject == Uri.parse("LampPost.sfb")){
            transformableNode.localScale = Vector3(2f, 2f, 2f)
            Log.d("checking", "detected LampPost")
        }

        transformableNode.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        transformableNode.select()

        transformableNode.setOnTouchListener{hitTestResult, motionEvent ->
            when(motionEvent.action){
                MotionEvent.ACTION_DOWN->{
                    pressTime = motionEvent.eventTime
                }

                MotionEvent.ACTION_UP->{
                    val releaseTime = SystemClock.uptimeMillis()
                    if((releaseTime - pressTime) > 2000){
                        Log.d("checking", "Removing child")
                        fragment.arSceneView.scene.removeChild(anchorNode)
                    }
                }

                MotionEvent.ACTION_MOVE->{
                    Log.d("checking", "scale: " + transformableNode.localScale.toString())
                }
            }
            true
        }
    }

    /***
     * function to get the model resource on assets directory for each figure.
     */
    private fun setModelPath(modelFileName: String) {
        selectedObject = Uri.parse(modelFileName)
        val toast = Toast.makeText(applicationContext, modelFileName, Toast.LENGTH_SHORT)
        toast.show()
    }
}
