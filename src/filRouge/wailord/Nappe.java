package filRouge.wailord;

import java.nio.Buffer;
import java.util.ArrayList;

import android.util.Log;

public class Nappe extends MeshObject {

	// TAGS pour les logs
	final String POINTS = "POINTS";
	final String INDICES = "INDICES";
	final String COULEURS = "COULEURS";
	
	// constante utilisée pour les couleurs
	final byte MAXCOLOR = (byte)255;
		
	
	private Buffer mVertBuff;
    private ArrayList<Buffer> mIndBuff;
    
    private Buffer mColorBuff;
    
    private int indicesNumber = 0;
    private int verticesNumber = 0;
    
 	
	
	// Constructeur
	public Nappe()
	{
		verticesNumber = 5;
        setVerts(5);
        setIndices(5);	
        setCouleur(5);	
	}
	
	public Nappe(int definition)
	{
		verticesNumber = definition;
		setVerts(definition);
		setIndices(definition);
    	setCouleur(definition);
	}


	private void setVerts(int taille) 
	{	
		// nombre de coordonnées : 3 coord par points.
		int nbCoord = taille*taille*3;
		// pas, calculé à partir de la résolution demandée (taille du côté en nbr de points)
		float pas = (float)10/(float)taille;
		//Log.w(POINTS, "Test du pas : "+(float)pas);
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
		
		
		Log.v(POINTS,"Début de la génération des points." );
		
		float[] nappeVertices = new float[nbCoord];
		
		int ligne = 0;
		// incrément de 3 pour avancer à chaque fois au point suivant.
		for(int i = 0; i < nbCoord; i+=3)
		{
			// incrément des lignes.
			if((i/3)%taille == 0 && i !=0)
			{
				ligne++;
				Log.d(POINTS,"_______LIGNE = "+ligne );
			}	
			
			nappeVertices[i] = (float) coord1[(i/3)%taille];
			nappeVertices[i+1] = (float) -coord1[ligne];
			nappeVertices[i+2] = (float) -coord1[(i/3)%taille]*coord1[(i/3)%taille]-coord1[ligne]*coord1[ligne];
			Log.d(POINTS,"Nouveau point : " );
			Log.d(POINTS,"x = "+(float)nappeVertices[i]+"; y = "+(float)nappeVertices[i+1]+"; z = "+
					(float)nappeVertices[i+2]);
			Log.d(POINTS,"____________________" );
		}
		
		mVertBuff = fillBuffer(nappeVertices);	
	}

	public Buffer getVertex()
	{
		return mVertBuff;
	}
	

	private void setIndices(int taille) 
	{
		Log.v(INDICES, "Début de la génération des indices.");
		
		indicesNumber = (taille-1)*2*3;
		int nbStripe = taille - 1;
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
		
		Log.v(INDICES, "Génération des indices OK.");
		Buffer b;
		for(int i = 0; i < stripes.size(); i++)
		{
			b = fillBuffer(stripes.get(i)); 
			mIndBuff.add(b);
		}	
	}
	
	
	public ArrayList<Buffer> getInd()
	{
		return mIndBuff;
	}
	
	// Initialisation des couleurs pour chaque point de la nappe
	public void setCouleur(int taille)
	{
		Log.v(COULEURS, "Début de la génération des couleurs");
		byte[] couleurs = new byte[taille*taille*4];		
		for(int i = 0; i < (taille*taille*4)-4; i+=4)
		{
			if( (i < (taille*taille*4)/2) )
			{
				couleurs[i] = 0;			// RED
				couleurs[i+1] = MAXCOLOR;	// BLUE
				couleurs[i+2] = 0;			// GREEN
				couleurs[i+3] = MAXCOLOR;   // ALPHA
			}
			else
			{
				couleurs[i] = 0;
				couleurs[i+1] = 0;
				couleurs[i+2] = MAXCOLOR;
				couleurs[i+3] = MAXCOLOR;
			}
			Log.v(COULEURS,"Couleur du point "+i+" : ");
			Log.v(COULEURS,"R :"+couleurs[i]+", G :"+couleurs[i+1]+", B :"+couleurs[i+2]+" || Alpha :"+couleurs[i+3]);
		}
		mColorBuff = fillBuffer(couleurs);
	}
	
	public Buffer getCouleur()
	{
		return mColorBuff;
	}
	
	
	@Override
	public int getNumObjectVertex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumObjectIndex() {
		return indicesNumber;
	}

	@Override
	public Buffer getBuffer(BUFFER_TYPE bufferType) {return null;}
	@SuppressWarnings("unused")
	private void setNorms() {}
	@SuppressWarnings("unused")
	private void setTexCoords(){}
}
