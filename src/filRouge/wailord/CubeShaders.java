/*==============================================================================
 Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package filRouge.wailord;

public class CubeShaders
{
    
    public static final String CUBE_MESH_VERTEX_SHADER = " \n" + "\n"
        + "attribute vec4 vertexPosition; \n"
        + "attribute vec4 vertexNormal; \n"
        + "attribute vec2 vertexTexCoord; \n" + "\n"
        + "varying vec2 texCoord; \n" + "varying vec4 normal; \n" + "\n"
        + "uniform mat4 modelViewProjectionMatrix; \n" + "\n"
        + "void main() \n" + "{ \n"
        + "   gl_Position = modelViewProjectionMatrix * vertexPosition; \n"
        + "   normal = vertexNormal; \n" + "   texCoord = vertexTexCoord; \n"
        + "} \n";
    
    public static final String CUBE_MESH_FRAGMENT_SHADER = " \n" + "\n"
        + "precision mediump float; \n" + " \n" + "varying vec2 texCoord; \n"
        + "varying vec4 normal; \n" + " \n"
        + "uniform sampler2D texSampler2D; \n" + " \n" + "void main() \n"
        + "{ \n" + "   gl_FragColor = texture2D(texSampler2D, texCoord); \n"
        + "} \n";
    
    
    public static final String WAILORD_MESH_VERTEX_SHADER = " \n" + "\n"
            + "attribute vec4 a_Position; \n"   // used to be called vertexPosition
            + "\n"
            + "attribute vec4 a_Color; \n"   // color of a point
            + "\n"
            + "varying vec4 v_Color; \n"   // varying color
            + "\n"
            + "uniform mat4 modelViewProjectionMatrix; \n" + "\n"
            + "void main() \n" + "{ \n"
            + "   gl_Position = modelViewProjectionMatrix * a_Position; \n"
            + "   v_Color = a_Color;  \n"			
            + "} \n";
        
        public static final String WAILORD_MESH_FRAGMENT_SHADER = " \n" + "\n"
            + "precision mediump float; \n" + " \n" + "varying vec2 texCoord; \n"
            + "varying vec4 v_Color; \n" + " \n"  // u_color used to be called normal 
            + " \n" + "void main() \n"
            + "{ \n" + "   gl_FragColor = v_Color; \n"
            + "} \n";
    
}
