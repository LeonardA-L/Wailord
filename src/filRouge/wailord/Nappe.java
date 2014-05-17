package filRouge.wailord;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
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
    
    private Buffer allIndices2;
    private ShortBuffer mIndBuffer2;
    
    private int nbStripe = 0; 
    private int indicesNumber = 0;
   
 	
    private int[][] processedImage;
    
	
	// Constructeur
	public Nappe()
	{
        setVertsCarre(10,3f);
        setIndicesCarre(10);	
        //setCouleur(10);	
	}
	
	public Nappe(int definition, float hauteur)
	{
		setVertsCarre(definition, hauteur);
		setIndicesCarre(definition);
    	//setCouleur(definition);
	}
	
	public Nappe(int definition, float hauteur, int[][] aProcessedImage)
	{
		setVertsCarre(definition, hauteur);
		setIndicesCarre(definition);
    	//setCouleur(definition);
		processedImage = aProcessedImage;
	}



	private void setVertsCarre(int taille, float hauteur) 
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
				nappeVertices[i+2] = hauteur + (float)(Math.random()/2);
			}
			// couleurs :
			if ((i/7)%2==0)
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
	
	
	
	private void setVertsImage(int[][] image) 
	{	
		// dimension du tableau
		int longueur = image.length;
		int largeur = image[1].length;
		
		// nombre de coordonnées : 3 coord par points + 4 par couleurs pour chaque point.
		int nbCoord = longueur*largeur*(3+4);
		// pas en longueur : 
		float pasLong = (float)40/(float)longueur;
		// pas en largeur :
		float pasLarg = (float)40/(float)largeur;
	
	
		// tableau de coordonnées fixes dans la longueur :
		float[] coordLong = new float[longueur];
		float halfLong = longueur/2;
		for(float i = -halfLong; i <= halfLong; i++)
		{
			Log.v(POINTS,"Valeur de i+max : "+(i+halfLong) );
			coordLong[(int)(i+halfLong)] = (float)i*(float)pasLong;
			Log.v(POINTS,"Coordonnée associée :"+(float)coordLong[(int)(i+halfLong)] );
		}
		// tableau de coordonnées fixes dans la largeur :
		float[] coordLarg = new float[largeur];
		float halfLarg = largeur/2;
		for(float i = -halfLarg; i <= halfLarg; i++)
		{
			Log.v(POINTS,"Valeur de i+max : "+(i+halfLong) );
			coordLarg[(int)(i+halfLarg)] = (float)i*(float)pasLarg;
			Log.v(POINTS,"Coordonnée associée :"+(float)coordLarg[(int)(i+halfLarg)] );
		}
		
		//--------------------------------------------------//
		Log.v(POINTS,"Début de la génération des points." );
		
		nappeVertices = new float[nbCoord];
		int ligne = 0;
		int indexLigneImage = 0;
		int indexColonneImage = 0;
		// ____ BOUCLE PRINCIPALE _____
		//  i va de 7 en 7 pour ajouter 1 point à chaque fois (3 coordonnées plus 4 couleurs)
		for(int i = 0; i < nbCoord; i+=7)
		{
			// incrément des lignes.
			if((i/7)%longueur == 0 && i !=0)
			{
				ligne++;
				Log.d(POINTS,"_______LIGNE = "+ligne );
			}	

			// coordonnées : 
			nappeVertices[i]   = (float) coordLarg[(i/7)%largeur];	// X sera dans la largeur..
			nappeVertices[i+1] = (float) -coordLong[ligne];			// Y sera dans la longeur..
			nappeVertices[i+2] = image[indexLigneImage][indexColonneImage];		// Récup de la hauteur dans l'image.
			if(indexLigneImage%longueur == 0 && indexLigneImage != 0)
			{	indexLigneImage = 0;  }
			else
			{ 	indexLigneImage++; }
			if(indexColonneImage%largeur == 0 && indexColonneImage != 0)
			{ 	indexColonneImage = 0;  }
			else
			{ 	indexColonneImage++;}
			
			// couleurs :
			if (nappeVertices[i+2] == 0f)
			{	
				nappeVertices[i+3] = .2f;		// RED  
				nappeVertices[i+4] = .2f;		// GREEN
				nappeVertices[i+5] = .8f;  		// BLUE
				nappeVertices[i+6] = 255f;		// ALPHA
			}
			else if (nappeVertices[i+2] > 0f && nappeVertices[i+2] < 2f)
			{
				nappeVertices[i+3] = .2f;		// RED  
				nappeVertices[i+4] = .8f;		// GREEN
				nappeVertices[i+5] = .2f;  		// BLUE
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
	

	private void setIndicesCarre(int taille) 
	{
		Log.v(INDICES, "Début de la génération des indices.");
		
		indicesNumber = (taille-1)*3*2; 	// nombres de valeurs pour une bande de triangles.
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
				stripe[j+1] = (short) ( (j/6) +i*(taille) + 1);
				stripe[j+2] = (short) ( (j/6) +(i+1)*(taille));
				stripe[j+3] = (short) ( (j/6) +i*(taille) + 1);
				stripe[j+4] = (short) ( (j/6) +(i+1)*(taille)+1);
				stripe[j+5] = (short) ( (j/6) +(i+1)*(taille));	
				
				// LOG 
				Log.w(INDICES, "----------------");
				Log.w(INDICES, "stripe["+ j   +"] = "+stripe[j+0]);
				Log.w(INDICES, "stripe["+(j+1)+"] = "+stripe[j+1]);
				Log.w(INDICES, "stripe["+(j+2)+"] = "+stripe[j+2]);
				Log.w(INDICES, "stripe["+(j+3)+"] = "+stripe[j+3]);
				Log.w(INDICES, "stripe["+(j+4)+"] = "+stripe[j+4]);
				Log.w(INDICES, "stripe["+(j+5)+"] = "+stripe[j+5]);
			}
			stripes.add(stripe);
		}
		
	}
	private void setIndices(int longueur, int largeur) 
	{
		Log.v(INDICES, "Début de la génération des indices.");
		
		indicesNumber = (longueur-1)*3*2; 	// nombres de valeurs pour une bande de triangles.
		nbStripe = largeur - 1; 				// nombre de bandes de triangles
		ArrayList<short[]> stripes = new ArrayList<short[]>();
		
		// remplissage de chaque bande de triangles.
		for(int i = 0; i< nbStripe; i++)
		{
			Log.d(INDICES, "# BANDE N° : "+i);	
			short[] stripe = new short[indicesNumber];
			for(int j = 0; j <= stripe.length-6; j+=6)
			{
				stripe[j+0] = (short) ( (j/6) +i*longueur);
				stripe[j+1] = (short) ( (j/6) +i*(longueur) + 1);
				stripe[j+2] = (short) ( (j/6) +(i+1)*(longueur));
				stripe[j+3] = (short) ( (j/6) +i*(longueur) + 1);
				stripe[j+4] = (short) ( (j/6) +(i+1)*(longueur)+1);
				stripe[j+5] = (short) ( (j/6) +(i+1)*(longueur));	
				
				// LOG 
				Log.w(INDICES, "----------------");
				Log.w(INDICES, "stripe["+ j   +"] = "+stripe[j+0]);
				Log.w(INDICES, "stripe["+(j+1)+"] = "+stripe[j+1]);
				Log.w(INDICES, "stripe["+(j+2)+"] = "+stripe[j+2]);
				Log.w(INDICES, "stripe["+(j+3)+"] = "+stripe[j+3]);
				Log.w(INDICES, "stripe["+(j+4)+"] = "+stripe[j+4]);
				Log.w(INDICES, "stripe["+(j+5)+"] = "+stripe[j+5]);
			}
			stripes.add(stripe);
		}
		
		
	/*	mIndBuff = ByteBuffer.allocateDirect(indicesNumber*nbStripe*2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
		for(int i = 0; i < stripes.size(); i++)
		{
			Log.v(INDICES, "Stripe "+i+ " : "+(stripes.get(i))[1]);
			mIndBuff.put(stripes.get(i));
		}	*/
		
		// on veut mettre toutes les bandes de triangle de stripes dans un seul buffer...
		
		short[] allIndices = new short[indicesNumber*stripes.size()];
		int index = 0;
		short[] temp;
		for(int i = 0; i < stripes.size(); i++)
		{
			temp = stripes.get(i);
			for(int j = 0; j < temp.length;j++)
			{
				allIndices[index] = temp[j];
				index++;
			}
		}
		
		allIndices2 = fillBuffer(allIndices);
		
		
		/*
		mIndBuffer2.allocate(indicesNumber*stripes.size());
		short[] temp2;
		for(int i = 0; i < stripes.size(); i++)
		{
			Log.w("BLABLA", "buffer en remplissage : "+i+"/"+stripes.size());
			temp2 = stripes.get(i);
			for(short s : temp2)
			{
				mIndBuffer2.put(s);
			}
		}	
		mIndBuffer2.position(0);
		
		
		mIndBuff = new ArrayList<Buffer>();
		Log.v(INDICES, "Génération des indices OK.");
		for(int i = 0; i < stripes.size(); i++)
		{
			Log.v(INDICES, "Stripe "+i+ " : "+(stripes.get(i))[1]);
			mIndBuff.add(fillBuffer(stripes.get(i)));
		}	
		*/
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
	
	public ArrayList<Buffer> getInd1()
	{
		return mIndBuff;
	}

	public Buffer getInd2()
	{
		return allIndices2;
	}

	
	public int getNumObjectVertex() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int getNumObjectIndex() {
		return indicesNumber*nbStripe;
	}
}
