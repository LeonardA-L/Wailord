/*==============================================================================
 Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package filRouge.wailord;

import java.nio.Buffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.Vuforia;


// The renderer class for the ImageTargetsBuilder sample. 
public class WailordTargetRenderer implements GLSurfaceView.Renderer
{
    private static final String LOGTAG = "UserDefinedTargetRenderer";
    
    
    MainActivity vuforiaAppSession;  
    public boolean mIsActive = false;    
//  private Vector<Texture> mTextures;  
    
    // shader program
    private int shaderProgramID;  
    // vertex Coordinates + RGB in the end
    private int vertexHandle;  
    private static final String A_POSITION = "a_Position";
 
    // colors ... 
    private  static final String A_COLOR = "a_Color";
    private static final int COLOR_COMPONENT_COUNT = 3;
    private int aColorLocation;
    
    // View matrix for orientation ?
    private int mvpMatrixHandle;
    
//  private int normalHandle;    
//  private int textureCoordHandle;    

//  private int texSampler2DHandle;
    
    // Constants:
    static final float kObjectScale = 3.f;
    
    private Teapot mTeapot;
    private Nappe mNappe;
    
    // Reference to main activity
    private MainActivity mActivity;
    
    
    public WailordTargetRenderer(MainActivity activity,
    		MainActivity session)
    {
        mActivity = activity;
        vuforiaAppSession = session;
    }
    
    
    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");
        
        // Call function to initialize rendering:
        initRendering();
        
        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }
    
    
    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");
        
        // Call function to update rendering when render surface
        // parameters have changed:
        mActivity.updateRendering();
        
        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }
    
    
    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;
        
        // Call our function to render content
        renderFrame();
    }
    
    
    private void renderFrame()
    {
        // Clear color and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        // Get the state from Vuforia and mark the beginning of a rendering
        // section
        State state = Renderer.getInstance().begin();
        
        // Explicitly render the Video Background
        Renderer.getInstance().drawVideoBackground();
        
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);        
        GLES20.glFrontFace(GLES20.GL_CCW); // pour utilisation avec la caméra arrière, sinon CW
            
        // Render the RefFree UI elements depending on the current state
        mActivity.refFreeFrame.render();
        
        // Did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {
            // Get the trackable:
            TrackableResult trackableResult = state.getTrackableResult(tIdx);
            Matrix44F modelViewMatrix_Vuforia = Tool
                .convertPose2GLMatrix(trackableResult.getPose());
            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();
            
            float[] modelViewProjection = new float[16];
            Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f, kObjectScale);
            Matrix.scaleM(modelViewMatrix, 0, kObjectScale, kObjectScale,
                kObjectScale);
            Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);
            
            GLES20.glUseProgram(shaderProgramID);
            
            
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,false, 0, mNappe.getVertex());
            GLES20.glVertexAttribPointer(aColorLocation,3, GLES20.GL_FLOAT,false, 0, mNappe.getCouleur());
            
            
            //GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,false, 0, mTeapot.getNormals());
            //GLES20.glVertexAttribPointer(textureCoordHandle, 2,GLES20.GL_FLOAT, false, 0, mTeapot.getTexCoords());
            
            GLES20.glEnableVertexAttribArray(vertexHandle);  		// utiliser les points
            GLES20.glEnableVertexAttribArray(aColorLocation);       // utiliser les couleurs
            
           // GLES20.glEnableVertexAttribArray(normalHandle);
           // GLES20.glEnableVertexAttribArray(textureCoordHandle);
            
            //GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures.get(0).mTextureID[0]);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                modelViewProjection, 0);
            // GLES20.glUniform1i(texSampler2DHandle, 0);
            
            // actual drawing -> with TRIANGLES
            //GLES20.glDrawElements(GLES20.GL_TRIANGLES,mTeapot.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,mTeapot.getIndices());
            
            ArrayList<Buffer> stripeBuffer = new ArrayList<Buffer>();
            stripeBuffer = mNappe.getInd();
            for(int i = 0; i < stripeBuffer.size() ; i++)
    		{
    			GLES20.glDrawElements(GL11.GL_TRIANGLE_STRIP, mNappe.getNumObjectIndex(), GL11.GL_UNSIGNED_BYTE, stripeBuffer.get(i));
    		}
            
            
            GLES20.glDisableVertexAttribArray(vertexHandle);
            GLES20.glDisableVertexAttribArray(aColorLocation);
            //GLES20.glDisableVertexAttribArray(normalHandle);
            //GLES20.glDisableVertexAttribArray(textureCoordHandle);
            
            SampleUtils.checkGLError("UserDefinedTargets renderFrame");
        }
        
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        
        Renderer.getInstance().end();
    }
    
    
    private void initRendering()
    {
        Log.d(LOGTAG, "initRendering");
        
        //mTeapot = new Teapot();
        
        mNappe = new Nappe();
        
        // Define clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
            : 1.0f);
     
        /*
        // Now generate the OpenGL texture objects and add settings
        for (Texture t : mTextures)
        {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, t.mData);
        }
        */
        
        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
            CubeShaders.WAILORD_MESH_VERTEX_SHADER,
            CubeShaders.WAILORD_MESH_FRAGMENT_SHADER);
        
        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID, A_POSITION);
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "modelViewProjectionMatrix");
        aColorLocation = GLES20.glGetAttribLocation(shaderProgramID, A_COLOR);
        
        
        
       /* normalHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "texSampler2D"); */
    }
    
    /*
    public void setTextures(Vector<Texture> textures)
    {
        mTextures = textures;
        
    }
    */
}
