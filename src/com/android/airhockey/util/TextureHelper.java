package com.android.airhockey.util;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLUtils.texImage2D;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexParameteri;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


public class TextureHelper {

	private static final String TAG = "TextureHelper";
	
	public static int loadTexture(Context context, int ressourceId)
	{
		final int[] textureObjectIds = new int[1];
		glGenTextures(1,textureObjectIds, 0);
		if(textureObjectIds[0] == 0)
		{
			if(LoggerConfig.ON)
			{
				Log.w(TAG, "Problème à la génération de la textures");
			}
			return 0;
		}
		
		final BitmapFactory.Options options = new BitmapFactory.Options();
		// on veut l'image originale, pas une version mise à l'échelle
		options.inScaled = false;
		
		final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), ressourceId, options);
		if(bitmap == null)
		{
			if(LoggerConfig.ON)
			{
				Log.w(TAG, "ResourceID "+ressourceId+
						" n'a pas été générée.");
			}
			glDeleteTextures(1, textureObjectIds,0);
			return 0;
		}

		glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		
		texImage2D(GL_TEXTURE_2D,0,bitmap,0);
		
	}
	
}
