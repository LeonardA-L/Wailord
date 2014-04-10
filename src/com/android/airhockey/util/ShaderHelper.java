package com.android.airhockey.util;

import static android.opengl.GLES20.*;

import android.util.Log;

public class ShaderHelper {

	private static final String TAG = "ShaderHelper";
	
	
	public static int compileVertexShader(String shaderCode)
	{
		return compileShader(GL_VERTEX_SHADER, shaderCode);
	}

	public static int compileFragmentShader(String shaderCode)
	{
		return compileShader(GL_FRAGMENT_SHADER, shaderCode);
	}

	private static int compileShader(int type, String shaderCode) 
	{
		final int shaderObjectId = glCreateShader(type);  // new shader object
		if (shaderObjectId == 0)
		{
			if(LoggerConfig.ON)
			{
				Log.w(TAG, "Could not create new shader");
			}
			return 0;
		}
		glShaderSource(shaderObjectId, shaderCode); // upload the source code to the shader
		glCompileShader(shaderObjectId);	// compile shader
		final int[] compileStatus = new int[1];
		glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);  // check wether the compile failed
		if(LoggerConfig.ON)
		{
			Log.v(TAG, "Result of compiling source = " + "\n" + shaderCode+"\n"+glGetShaderInfoLog(shaderObjectId));
		}
		if(compileStatus[0] == 0) // if compile failed
		{
			glDeleteShader(shaderObjectId);
			if(LoggerConfig.ON)
			{
				Log.w(TAG, "Compilation failed");
			}
			return 0;
		}
		return shaderObjectId;
	}
	
	public static int linkProgram(int vertexShaderId, int fragmentShaderId)
	{
		final int programObjectId = glCreateProgram();  // create new program object
		if(programObjectId == 0) // if compile failed
		{
			if(LoggerConfig.ON)
			{
				Log.w(TAG, "Could not create new program.");
			}
			return 0;
		}
		if(LoggerConfig.ON)
		{
			Log.v(TAG, "New Program Created in linkProgram.");
		}
		glAttachShader(programObjectId, vertexShaderId);  // attach shaders to the program
		glAttachShader(programObjectId, fragmentShaderId);
		// join the shaders together
		glLinkProgram(programObjectId);
		final int[] linkStatus = new int[1];
		glGetShaderiv(programObjectId, GL_COMPILE_STATUS, linkStatus, 0);
		if(LoggerConfig.ON)
		{
			Log.w(TAG, "Resultat du link, linkStatus[0] = "+linkStatus[0]);
		}
		/*if(linkStatus[0] == 0) // if compile failed
		{
			glDeleteProgram(programObjectId);
			if(LoggerConfig.ON)
			{
				Log.w(TAG, "Link failed (poor Zelda)");
			}
			return 0;
		}*/
		
		return programObjectId;
	}
	
	
	public static boolean validateProgram(int programObjectId)
	{
		glValidateProgram(programObjectId);
		final int[] validateStatus = new int[1];
		glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0);
		Log.v(TAG, "Validating program : "+validateStatus+"\nLog: "+glGetProgramInfoLog(programObjectId));
		return validateStatus[0] != 0;
	}
	
	
}
