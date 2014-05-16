package filRouge.wailord;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.util.Log;

public class NappeDeLignes {

	// TAGS pour les logs
	final String POINTS = "POINTS";
	final String INDICES = "INDICES";
	final String COULEURS = "COULEURS";
	
	// constante utilis��e pour les couleurs
	final byte MAXCOLOR = (byte)255;
	
	// buffer definissant les points de la nappe
	private float[] nappeVertices; // coordonn��es
	private FloatBuffer mFNappeVerticesBuffer; // coordonn��es dans le buffer
	
	// buffer definissant les indices
	private byte[] nappeIndices;
	private ByteBuffer mFNappeIndices;
	private int nbPoints;
	
	// buffer pour la couleur
	private ByteBuffer mColorBuffer; 
    	
	// Constructeur
	public NappeDeLignes()
	{
		// points de la nappes.
		initNappe(); 
		// indices des triangles
		initIndices();
		// indices des couleurs...
		initCouleur();
	}
	
	
	// Initialisation des coordonn��es des points de la nappe
	private void initNappe()
	{
		
		Log.v(POINTS,"D��but de la g��n��ration des points." );
		// on suppose que l'on re��oit un tableau qui contient des coordonn��es de la forme
		// tab[x][y] = z dans un rep��re conventionnel opengl :
		// # x vers la droite,
		// # y vers le haut
		// # z vers l'utilisateur
		
		/*
		float[] map = 
		{
			1.0f, 0.7f, 0.8f, 0.5f, 1.0f,
			0.3f, 0.5f, 0.5f, 0.4f, 0.7f,
			0.6f, 0.8f, 0.4f, 0.3f, 0.8f,
			0.4f, 0.9f, 0.7f, 0.5f, 0.9f,
			1.0f, 0.8f, 0.5f, 0.7f, 1.0f
		};
		*/
		
		int res = (int)Math.pow((double)12,(double)2);
		float[] map = new float[res];
		float z = 0.25f;
		for(int i = 0; i < res; i++)
		{
			map[i] = z+=0.03f;
		}
	
		nbPoints = map.length;
		Log.v(POINTS, "nbPoints = "+nbPoints);
		// taille repr��sente le c��t�� de la nappe
		int taille =  (int) Math.sqrt(map.length); 
		Log.v(POINTS, "taille = "+taille);
		
		// A partir de ce tableau, on va faire un tableau de points �� trois coordonn��es
		// on ajoutera un plan de points �� z = 0 pour pouvoir tracer des lignes selon l'axe z
		
		// nombre de coordonn��es dans le tableau : 3 coord par points.
		int nbCoord = nbPoints*3;
		// pas : on veut un recatangle de 1 unit�� de c��t��
		float pas = (float)1/(float)taille;
		
		// Calcul de l'offset et d'un max 
		// utilis��s pour la g��n��ration d'un vecteur d'abscisses/ordonn��es
		float offset = 0f;
		int max = (taille-1)/2;
		if(taille%2 == 0) // si taille paire
		{
			max = (taille/2)-1;
			offset = (float)1/((float)taille*(float)2);
		}
		
		// tableau de coordonn��es fixes pour une ligne ou une colonne.
		float[] coord1 = new float[taille];
		for(float i = -max; i <= max; i++)
		{
			// Log.v(POINTS,"Valeur de i+max : "+(i+max) );
			coord1[(int)i+max] = (float)i*(float)pas-(float)((float)Math.abs(max)/(float)max)*(float)offset;
			// Log.v(POINTS,"Coordonn��e associ��e :"+(float)coord1[(int)i+max] );
		}
		
		// tableau de points : 
		nappeVertices = new float[nbCoord*2]; // x2 pour les points �� 0
		
		int ligne = 0;
		// incr��ment de 3 pour avancer �� chaque fois au point suivant.
		for(int i = 0; i < nbPoints*2*3; i+=6)
		{	
			Log.d(POINTS,"# i ="+i);
			// Incr��ment des lignes.
			if( (i/6)%taille == 0 && i != 0)
			{
				ligne++;
				Log.d(POINTS,"___LIGNE = "+ligne );
			}
			
			// point �� z = 0
			nappeVertices[i] = (float)coord1[(i/3)%taille];
			nappeVertices[i+1] = (float)-coord1[ligne];
			nappeVertices[i+2] = 0f;
			// point �� z = map[i/6]
			nappeVertices[i+3] = (float)coord1[(i/3)%taille];
			nappeVertices[i+4] = (float)-coord1[ligne];
			nappeVertices[i+5] = map[i/6];
			
			// -LOG-
			Log.d(POINTS,"Point : "+ i/6 );
			Log.d(POINTS,"("+(float)nappeVertices[i]+","+(float)nappeVertices[i+1]+","+
					(float)nappeVertices[i+2]+") ; ("+(float)nappeVertices[i+3]+","
					+(float)nappeVertices[i+4]+","+(float)nappeVertices[i+5]+")");
			Log.d(POINTS,"____________________" );
		}
	
		// placer les points dans un buffer pour opengl
		ByteBuffer pyra = ByteBuffer.allocateDirect(nappeVertices.length*4);
		pyra.order(ByteOrder.nativeOrder());
		mFNappeVerticesBuffer = pyra.asFloatBuffer();
		mFNappeVerticesBuffer.put(nappeVertices);
		mFNappeVerticesBuffer.position(0);
		Log.v(POINTS, "Buffer de points ok.");
	}
	
	// Initialisation des indices des bandes de triangle.
	private void initIndices()
	{
		Log.v(INDICES,"D��but de la g��n��ration des indices." );
		nappeIndices = new byte[nbPoints*2];
		Log.v(INDICES,"Avant la boucle" );
		for(int i = 0; i < nbPoints*2; i++)
		{
			nappeIndices[i] = (byte) (i);
			Log.v(INDICES,"nappeIndice["+i+"] = "+nappeIndices[i]);
		}
		mFNappeIndices = ByteBuffer.allocateDirect(nbPoints*2);
		mFNappeIndices.put(nappeIndices);
		mFNappeIndices.position(0);		
	}
	
	// Initialisation des couleurs pour chaque point de la nappe
	public void initCouleur()
	{
		int taille = nbPoints;
		Log.v(COULEURS, "D��but de la g��n��ration des couleurs");
		byte[] couleurs = new byte[taille*taille*4];		
		for(int i = 0; i < (taille*taille*4)-4; i+=4)
		{
			couleurs[i] = MAXCOLOR;			// RED
			couleurs[i+1] = 0;				// BLUE
			couleurs[i+2] = 0;				// GREEN
			couleurs[i+3] = MAXCOLOR;   	// ALPHA
			Log.v(COULEURS," Point :"+i+" => R :"+couleurs[i]+", G :"+couleurs[i+1]+", B :"+couleurs[i+2]+" || Alpha :"+couleurs[i+3]);
		}
		mColorBuffer = ByteBuffer.allocateDirect(couleurs.length);
		mColorBuffer.put(couleurs);
		mColorBuffer.position(0);	
	}
	
	// utilitaire pour la g��n��ration des couleurs.
	public byte couleurAleatoire()
	{
		byte a= (byte) Math.ceil((Math.random()*10));		
		return a > 5 ? MAXCOLOR : (byte) 0;
	}
	
	public void draw(GL10 gl)
	{
		gl.glFrontFace(GL10.GL_CW);
		gl.glVertexPointer(3, GL11.GL_FLOAT, 0, mFNappeVerticesBuffer);
		gl.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 0, mColorBuffer);
		gl.glDrawElements(GL10.GL_LINES, nbPoints*2, GL10.GL_UNSIGNED_BYTE, mFNappeIndices);
		gl.glFrontFace(GL10.GL_CCW);
	}
	
}
