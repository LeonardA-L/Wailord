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
		
	private float[] nappeVertices;	// nappe de points 
	private FloatBuffer mVertBuff;  // buffer de points 
	
    private ArrayList<Buffer> mIndBuff;
    
    private Buffer allIndices2;
    //private ShortBuffer mIndBuffer2;
    
    private int nbStripe = 0; 
    private int indicesNumber = 0;
   	
    private int[][] processedImage;
    
	
    // ---------------- Constructeurs --------------
    
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
	
	// COnstructeur utilisé pour la génération de nappe à partir d'une image.	
	public Nappe(int[][] aProcessedImage)
	{
		processedImage = new int[][]{
				  { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
				  { 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
				  { 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
				  { 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1 },
				  { 1, 0, 0, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 3, 3, 3, 3, 2, 1, 1, 1, 1, 1, 1 },
				  { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 4, 4, 3, 2, 1, 1, 1, 1, 1, 1 },
				  { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 5, 5, 3, 2, 1, 1, 1, 1, 1, 1 },
				  { 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 2, 3, 3, 3, 3, 2, 1, 1, 1, 1, 1, 1 },
				  { 1, 1, 1, 1, 2, 3, 3, 3, 3, 3, 2, 1, 1, 1, 1, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1 },
				  { 1, 1, 1, 1, 3, 4, 4, 4, 4, 3, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
				};
		/*processedImage = new int[][]{
				  { 2, 2, 3, 2},
				  { 2, 3, 4, 3},
				  { 2, 3, 4, 3},
				  { 2, 2, 3, 2},
				  { 2, 2, 3, 2},
				  { 2, 3, 4, 3},
				};*/
		setVertsImage(processedImage);  // générer les points et leur couleur
		setIndicesImages(processedImage.length, processedImage[1].length); // générer les indices des triangles.
	}

	// -----------------------------------------------------
	
	// NAPPE CARRE
	
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
	
	// NAPPE A PARTIR D'UNE IMAGE 
	
	private void setVertsImage(int[][] image) 
	{	
		// dimension du tableau
		int longueur = image.length;
		int largeur = image[1].length;
		
		
		// nombre de coordonnées : 3 coord par points + 4 par couleurs pour chaque point -> taille totale du tableau de points.
		int nbCoord = longueur*largeur*(3+4);  
		// pas en longueur : 
		float pasLong = (float)20/(float)((longueur-1)); 		// modifier les taille des côtés ici et en dessous  et plus bas dans les boucles
		// pas en largeur :
		float pasLarg = (float)40/(float)((largeur-1));
		
		Log.w(POINTS,"pas : longueur ("+longueur+") -> "+pasLong+" | largeur "+largeur+"-> "+pasLarg);
		
		// tableau de coordonnées fixes dans la longueur :
		float[] coordLong = new float[longueur];
		float halfLong = (longueur/2);

		int index = 0;
		float valeur = -20/2;			// modifier ici et en dessous en cas de changement de taille
		while(valeur <= 20/2)
		{
			Log.w(POINTS,"Valeur à index "+index+" : "+valeur);
			coordLong[index] = valeur;
			index++;
			valeur+=pasLong;
		}
		
		// tableau de coordonnées fixes dans la largeur :
		float[] coordLarg = new float[largeur];
		float halfLarg = (largeur/2);

		int index2 = 0;
		float valeur2 = -40/2;			// modifier ici et en dessous en cas de changement de taille
		while(valeur2 <= 40/2)
		{
			Log.v(POINTS,"Valeur à index "+index2+" : "+valeur2);
			coordLarg[index2] = valeur2;
			index2++;
			valeur2+=pasLarg;
		}
		
		//--------------------------------------------------//
		Log.v(POINTS,"Début de la génération des points." );
		
		nappeVertices = new float[nbCoord]; 	// tableau final
		int ligne = 0; 							// index des lignes du tableau ci dessus. 
		// index pour les valeurs Z du tableau image
		int indexLigneImage = 0;				
		int indexColonneImage = 0;
		
		// ____ BOUCLE PRINCIPALE _____
		//  i va de 7 en 7 pour ajouter 1 point à chaque fois (3 coordonnées plus 4 couleurs)
		for(int i = 0; i < nbCoord; i+=7)
		{
			// incrément des lignes.
			if((i/7)%largeur == 0 && i !=0)
			{
				ligne++;
				Log.d(POINTS,"_______LIGNE = "+ligne );
			}
			
			// coordonnées : 
			Log.v(POINTS,"Taille cordLarg : :"+coordLarg.length+" | TailleCoordLong : "+coordLong.length);
			Log.v(POINTS,"X prochain :"+coordLarg[(i/7)%largeur]+" | Y prochain : "+-coordLong[ligne]);
			nappeVertices[i]   = (float) coordLarg[(i/7)%largeur];	// X sera dans la largeur..
			nappeVertices[i+1] = (float) -coordLong[ligne];			// Y sera dans la longeur..
			//Log.w(POINTS,"index L :"+indexLigneImage+" | index l "+indexColonneImage);
			nappeVertices[i+2] = image[indexLigneImage][indexColonneImage];		// Récup de la hauteur dans l'image.
			if(indexColonneImage%(largeur-1) == 0 && indexColonneImage != 0)
			{	
				indexColonneImage = 0;  
				indexLigneImage++;
			}
			else
			{ 	
				indexColonneImage++; 
			}
			
			// couleurs :
			if (nappeVertices[i+2] == 0f)
			{	
				nappeVertices[i+3] = .2f;		// RED  
				nappeVertices[i+4] = .2f;		// GREEN
				nappeVertices[i+5] = .8f;  		// BLUE
				nappeVertices[i+6] = 255f;		// ALPHA
			}
			else if (nappeVertices[i+2] == 1f)
			{
				nappeVertices[i+3] = .3f;		// RED  
				nappeVertices[i+4] = .8f;		// GREEN
				nappeVertices[i+5] = .3f;  		// BLUE
				nappeVertices[i+6] = 255f;		// ALPHA
			}
			else if (nappeVertices[i+2] == 2f)
			{
				nappeVertices[i+3] = .4f;		// RED  
				nappeVertices[i+4] = .8f;		// GREEN
				nappeVertices[i+5] = .4f;  		// BLUE
				nappeVertices[i+6] = 255f;		// ALPHA
			}
			else if (nappeVertices[i+2] == 3f)
			{
				nappeVertices[i+3] = .5f;		// RED  
				nappeVertices[i+4] = .9f;		// GREEN
				nappeVertices[i+5] = .5f;  		// BLUE
				nappeVertices[i+6] = 255f;		// ALPHA
			}
			else if (nappeVertices[i+2] == 4f)
			{
				nappeVertices[i+3] = .6f;		// RED  
				nappeVertices[i+4] = .6f;		// GREEN
				nappeVertices[i+5] = .6f;  		// BLUE
				nappeVertices[i+6] = 255f;		// ALPHA
			}
			else 
			{
				nappeVertices[i+3] = .1f;		// RED  
				nappeVertices[i+4] = .1f;		// GREEN
				nappeVertices[i+5] = .1f;  		// BLUE
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

	private void setIndicesImages(int longueur, int largeur) 
	{
		Log.v(INDICES, "Début de la génération des indices.");
		
		indicesNumber = (largeur-1)*3*2; 	// nombres de valeurs pour une bande de triangles.
		nbStripe = longueur - 1; 				// nombre de bandes de triangles
		ArrayList<short[]> stripes = new ArrayList<short[]>();
		
		// remplissage de chaque bande de triangles.
		for(int i = 0; i< nbStripe; i++)
		{
			Log.d(INDICES, "# BANDE N° : "+i);	
			short[] stripe = new short[indicesNumber];
			for(int j = 0; j <= stripe.length-6; j+=6)
			{
				stripe[j+0] = (short) ( (j/6) +i*(largeur));
				stripe[j+1] = (short) ( (j/6) +i*(largeur) + 1);
				stripe[j+2] = (short) ( (j/6) +(i+1)*(largeur));
				stripe[j+3] = (short) ( (j/6) +i*(largeur) + 1);
				stripe[j+4] = (short) ( (j/6) +(i+1)*(largeur)+1);
				stripe[j+5] = (short) ( (j/6) +(i+1)*(largeur));	
				
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
	}
	
	// GETTERS 

	public FloatBuffer getVertex()
	{
		return mVertBuff;
	}
	
	public ArrayList<Buffer> getInd1()
	{
		return mIndBuff;
	}

	public Buffer getInd2()
	{
		return allIndices2;
	}
	
	
	// UTIL 
	
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

	
	public int getNumIndex() {
		return indicesNumber*nbStripe;
	}
}
