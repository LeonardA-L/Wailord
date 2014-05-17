package filRouge.wailord;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import android.util.Log;

public class Nappe{

	// TAGS pour les logs
	final String POINTS = "POINTS";
	final String INDICES = "INDICES";
	final String COULEURS = "COULEURS";
		
	private float[] nappeVertices;
	private FloatBuffer mVertBuff;
	
    private ArrayList<Buffer> mIndBuff;
    private int nbStripe = 0; 
    private int indicesNumber = 0;
   
 	
	
	// Constructeur
	public Nappe()
	{
        setVerts(10,3f);
        setIndices(10);	
        //setCouleur(10);	
	}
	
	public Nappe(int definition, float hauteur)
	{
		setVerts(definition, hauteur);
		setIndices(definition);
    	//setCouleur(definition);
	}


	private void setVerts(int taille, float hauteur) 
	{	
		// nombre de coordonnées : 3 coord par points + 4 par couleurs pour chaque point.
		int nbCoord = taille*taille*3+taille*taille*4;
		// pas, calculé à partir de la résolution demandée (taille du côté en nbr de points)
		float pas = (float)40/(float)taille;
		// décalage si la taille est paire.
		float offset = 0f;
		int max = (taille-1)/2;
		if(taille%2 == 0)
		{
			max = (taille/2)-1;
			offset = (float)1/((float)taille*(float)2);
		}
		
		// tableau de coordonnées fixes pour une ligne ou une colonne.
		float[] coord1 = new float[taille];
		for(float i = -max; i <= max; i++)
		{
			Log.v(POINTS,"Valeur de i+max : "+(i+max) );
			coord1[(int)i+max] = (float)i*(float)pas-(float)((float)Math.abs(max)/(float)max)*(float)offset;
			Log.v(POINTS,"Coordonnée associée :"+(float)coord1[(int)i+max] );
		}
		
		//--------------------------------------------------//
		Log.v(POINTS,"Début de la génération des points." );
		
		nappeVertices = new float[nbCoord];
		int ligne = 0;
		// incrément de 7 pour avancer à chaque fois au point suivant.
		for(int i = 0; i < nbCoord; i+=7)
		{
			// incrément des lignes.
			if((i/7)%taille == 0 && i !=0)
			{
				ligne++;
				Log.d(POINTS,"_______LIGNE = "+ligne );
			}	
			
			// coordonnées : 
			nappeVertices[i] = (float) coord1[(i/7)%taille];	// X
			nappeVertices[i+1] = (float) -coord1[ligne];		// Y
			//nappeVertices[i+2] = (float) hauteur; //-coord1[(i/3)%taille]*coord1[(i/3)%taille]-coord1[ligne]*coord1[ligne];
			if((i/7)%taille == 0 || (i/7)%taille == taille-1 || ligne == 0 || ligne == taille)
			{
				nappeVertices[i+2] = hauteur;
			}
			else
			{
				nappeVertices[i+2] = hauteur + (float)(Math.random()*10) > 7F ? 2f : (-1f);
			}
			// couleurs :
			if ((float)nappeVertices[i+2] == hauteur)
			{	
				nappeVertices[i+3] = .2f;		// RED  
				nappeVertices[i+4] = .2f;		// GREEN
				nappeVertices[i+5] = .6f;  		// BLUE
				nappeVertices[i+6] = 255f;		// ALPHA
			}
			else
			{
				nappeVertices[i+3] = .8f;		// RED  
				nappeVertices[i+4] = .8f;		// GREEN
				nappeVertices[i+5] = .7f;  		// BLUE
				nappeVertices[i+6] = 255f;		// ALPHA
			}
			
			Log.d(POINTS,"Nouveau point : " );
			Log.d(POINTS,"x = "+(float)nappeVertices[i]+
						 "; y = "+(float)nappeVertices[i+1]+
						 "; z = "+(float)nappeVertices[i+2]+
						 "| R = "+(float)nappeVertices[i+3]+
						 "G = "+(float)nappeVertices[i+4]+
						 "B = "+(float)nappeVertices[i+5]+
						 "Alpha ="+(float)nappeVertices[i+6]);
			Log.d(POINTS,"____________________" );
		}
	
		mVertBuff = ByteBuffer.allocateDirect(nappeVertices.length * 4).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
		mVertBuff.put(nappeVertices);
		mVertBuff.position(0);
	}

	public FloatBuffer getVertex()
	{
		return mVertBuff;
	}
	

	private void setIndices(int taille) 
	{
		Log.v(INDICES, "Début de la génération des indices.");
		
		indicesNumber = (taille-1)*2*3; 	// nombres de valeurs pour une bande de triangles.
		nbStripe = taille - 1; 				// nombre de bandes de triangles
		ArrayList<short[]> stripes = new ArrayList<short[]>();
		
		// remplissage de chaque bande de triangles.
		for(int i = 0; i< nbStripe; i++)
		{
			Log.d(INDICES, "# BANDE N° : "+i);	
			short[] stripe = new short[indicesNumber];
			for(int j = 0; j <= stripe.length-6; j+=6)
			{
				stripe[j+0] = (short) ( (j/6) +i*taille);
				stripe[j+1] = (short) ( (j/6) +i*taille+1);
				stripe[j+2] = (short) ( (j/6) +(i+1)*taille);
				stripe[j+3] = (short) ( (j/6) +i*taille+1);
				stripe[j+4] = (short) ( (j/6) +(i+1)*taille);
				stripe[j+5] = (short) ( (j/6) +(i+1)*taille+1);	
				
				// LOG 
				Log.d(INDICES, "----------------");
				Log.d(INDICES, "stripe["+j+"] = "+stripe[j+0]);
				Log.d(INDICES, "stripe["+(j+1)+"] = "+stripe[j+1]);
				Log.d(INDICES, "stripe["+(j+2)+"] = "+stripe[j+2]);
				Log.d(INDICES, "stripe["+(j+3)+"] = "+stripe[j+3]);
				Log.d(INDICES, "stripe["+(j+4)+"] = "+stripe[j+4]);
				Log.d(INDICES, "stripe["+(j+5)+"] = "+stripe[j+5]);
			}
			stripes.add(stripe);
		}
		
		
		
	/*	mIndBuff = ByteBuffer.allocateDirect(indicesNumber*nbStripe*2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
		for(int i = 0; i < stripes.size(); i++)
		{
			Log.v(INDICES, "Stripe "+i+ " : "+(stripes.get(i))[1]);
			mIndBuff.put(stripes.get(i));
		}	*/
		
		mIndBuff = new ArrayList<Buffer>();
		Log.v(INDICES, "Génération des indices OK.");
		for(int i = 0; i < stripes.size(); i++)
		{
			Log.v(INDICES, "Stripe "+i+ " : "+(stripes.get(i))[1]);
			mIndBuff.add(fillBuffer(stripes.get(i)));
		}	
	}
	
	protected Buffer fillBuffer(short[] array)
    {
        // Each short takes 2 bytes
        ByteBuffer bb = ByteBuffer.allocateDirect(2 * array.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (short s : array)
            bb.putShort(s);
        bb.rewind();
        
        return bb;
        
    }
	
	public ArrayList<Buffer> getInd()
	{
		return mIndBuff;
	}

	public int getNumObjectVertex() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int getNumObjectIndex() {
		return indicesNumber;
	}
}
