


public class maClasse {

	static int[][] tab;
	
	final static int BORDER = -2;
	final static int BLANK = -1;
	
	final static int LARGE = 1*16;
	final static int HAUT = 1*12;
	static int iTab = 0;
	static int jTab = 0;
	
	static Stack memory = new Stack(10000); //0->x  1->y
	//On suppose que les bordures ne représentent pas plus de 10% du tableau
	static int[][] border = new int[(int)(0.3*LARGE*HAUT)][3];
	static int borderLast = 0;
	
	static int lvl = 0;
	static int lvlLast = 0;
	
	//static int[][] memory = new int[1000][2]; //0->x  1->y
	
	public static void main(String[] params){
		tab = new int[HAUT][LARGE];
		
		for(int i = 0 ; i < tab.length ; i++){
			for( int j = 0 ; j < tab[0].length ; j++){
				tab[i][j] = -1;
			}
		}
		
		intro(0);
		
		
		display(tab);
		//System.out.println("Hello World");
		
		//daMeth(1,1);
		//display(tab);
		
		int[] tabe = new int[2];
		System.out.println("dsqg");
		System.out.println(tabe[0]);
		System.out.println("dsqg");
		if(tabe[1]==0){
			System.out.println("true");
		} else {
			System.out.println("false");
		}
		//display(fade());
		
		//TODO : faire une boucle avec point de départ du remplissage
		//							et niveau altitude
		
		//initialisation
		memory.push(1, 1);
				//Phase Moteur de remplissage
		while(!memory.empty()){
			
			while(!memory.empty()){
				//TODO : supprimer le dernier element
				int x = memory.back()[0];
				int y = memory.back()[1];
				memory.pop();
				//System.out.println("Main : " + x + " "+ y);
				fill(x,y,0);
			}
			lvlLast=lvl;
			findNextBlank();
			//System.out.println("MAIN le level = "+lvl+ " / memory = "+ memory.empty());
			//System.out.println("MAIN " + memory.back()[0] + " " + memory.back()[1]+ " levelLast : "+ lvlLast);
			if(lvl!=lvlLast && lvlLast!=0){
				border(lvlLast);
			}
		}
		
		fade(10);
		
		display(tab);
		
	}
	
	//Affichage du tableau passé en paramètre
	public static void display(int[][] tab){
		for(int i = 0 ; i < tab.length ; i++){
			for( int j = 0 ; j < tab[0].length ; j++){
				System.out.print((int)tab[i][j] + "\t");
			}
			System.out.println();
		}
	}
	
	//Lissage du niveau
	public static void fade(int nbIte){
		int[][] tab2 = new int[HAUT][LARGE];
	
		//Première moyenne
		for(int ite = 0 ; ite<=nbIte ; ite++){
			//Moyenne de l'intérieur
			for(int i = 1 ; i < (tab.length - 1); i++){
				for( int j = 1 ; j < (tab[0].length - 1) ; j++){
					tab2[i][j] = (int)((tab[i][j+1] + tab[i][j-1] + tab[i+1][j] + tab[i-1][j])/4); 
				}
			}
			//Moyenne Effet de bords
			for(int i = 1 ; i< (tab.length-1) ; i++){
				tab2[i][0]= (int)((tab[i+1][0] + tab[i-1][0] + tab[i][1])/3);
				tab2[i][LARGE-1]= (int)((tab[i+1][LARGE-1] + tab[i-1][LARGE-1] + tab[i][LARGE-2])/3);
			}
			for(int i = 1 ; i< (tab[0].length-1) ; i++){
				tab2[0][i]= (int)((tab[1][i] + tab[0][i-1] + tab[0][i+1])/3);
				tab2[HAUT-1][i]= (int)((tab[HAUT-1][i+1] + tab[HAUT-1][i-1] + tab[HAUT-2][i])/3);
			}
		
			//Réinitialisation des bordures
			for( int avBorder = 0 ; avBorder < borderLast ; avBorder++){
				tab2[border[avBorder][0]][border[avBorder][1]] = border[avBorder][2];
			}
			
			
			for(int i = 1 ; i < (tab.length - 1); i++){
				for( int j = 1 ; j < (tab[0].length - 1) ; j++){
					tab[i][j] = (int)((tab2[i][j+1] + tab2[i][j-1] + tab2[i+1][j] + tab2[i-1][j])/4); 
				}
			}
			//Moyenne Effet de bords
			for(int i = 1 ; i< (tab.length-1) ; i++){
				tab[i][0]= (int)((tab2[i+1][0] + tab2[i-1][0] + tab2[i][1])/3);
				tab[i][LARGE-1]= (int)((tab2[i+1][LARGE-1] + tab2[i-1][LARGE-1] + tab2[i][LARGE-2])/3);
			}
			for(int i = 1 ; i< (tab[0].length-1) ; i++){
				tab[0][i]= (int)((tab2[1][i] + tab2[0][i-1] + tab2[0][i+1])/3);
				tab[HAUT-1][i]= (int)((tab2[HAUT-1][i+1] + tab2[HAUT-1][i-1] + tab2[HAUT-2][i])/3);
			}
			
			//Réinitialisation des bordures
			for( int avBorder = 0 ; avBorder < borderLast ; avBorder++){
				tab[border[avBorder][0]][border[avBorder][1]] = border[avBorder][2];
			}
		
		}
		//return tab2;
	}
	
	//met une bordure tout autour à bounds pour éviter outOfBounds
	public static void intro (int bounds){
		for(int i = 0; i < tab.length; i++){
			tab[i][0]=bounds;
			tab[i][tab[0].length-1]=bounds;
		}
		for(int i = 0; i < tab[0].length; i++){
			tab[0][i]=bounds;
			tab[tab.length-1][i]=bounds;
		}
		
		/*tab[5][5]=BORDER;
		tab[6][4]=BORDER;
		tab[4][1]=0;
		tab[4][2]=0;
		tab[4][3]=0;
		tab[4][4]=0;
		tab[4][5]=0;*/
		
		for(int i = 2; i < 6; i++){
			tab[i][2]=BORDER;
			tab[i][5]=BORDER;
		}
		for(int i = 2; i < 6; i++){
			tab[2][i]=BORDER;
			tab[5][i]=BORDER;
		}
		
		for(int i = 8; i < 11; i++){
			tab[i][7]=BORDER;
			tab[i][10]=BORDER;
		}
		for(int i = 8; i < 11; i++){
			tab[7][i]=BORDER;
			tab[10][i]=BORDER;
		}
		
		/*
		for(int i = 80; i < 120; i++){
			tab[i][79]=BORDER;
			tab[i][119]=BORDER;
		}
		for(int i = 80; i < 120; i++){
			tab[79][i]=BORDER;
			tab[119][i]=BORDER;
		}
		
		for(int i = 90; i < 110; i++){
			tab[i][89]=BORDER;
			tab[i][109]=BORDER;
		}
		for(int i = 90; i < 110; i++){
			tab[89][i]=BORDER;
			tab[109][i]=BORDER;
		}
		/*
		tab[tab.length-4][tab.length-4]=BLANK;
		tab[tab.length-5][tab.length-5]=BORDER;
		tab[tab.length-6][tab.length-5]=BORDER;
		tab[tab.length-6][tab.length-4]=BORDER;
		tab[tab.length-6][tab.length-3]=BORDER;
		tab[tab.length-6][tab.length-2]=BORDER;*/

	}
	
	//Trouve la prochaine zone à partir de laquelle commencer le remplissage
	public static void findNextBlank (){
		//Boucle permettant de vérifier la présence d'une autre zone fermée
		//de meme altitude
		for(int i = tab.length-1; i>1; i--){
			int ans = 0;
			for(int j = tab[0].length - 1 ; j>1 ; j--){
				
				if(tab[i][j]!=BLANK && tab[i][j]!=BORDER){
					ans = (int)tab[i][j];
				}
				
				if(tab[i][j]==BLANK && ans == lvl-10){
					memory.push(i,j);
					return;
				}
			}
		}
		
		lvl+=10;
		for(int i = 1; i<tab.length-1; i++){
			for(int j = 1 ; j<tab[0].length - 1 ; j++){
				if(tab[i][j]==BLANK){
					memory.push(i,j);
					return;
				}
			}
		}	
	}
	
	public static void fill(int i, int j, int mem ){
		iTab = i;
		jTab = j;
		while((int)(tab[iTab][jTab])==BLANK){
			while((int)(tab[iTab][jTab])==BLANK){

				tab[iTab][jTab]=lvl;

				if((int)(tab[iTab+1][jTab])!=BLANK && (int)(tab[iTab+1][jTab+1])==BLANK  && (int)(tab[iTab][jTab+1])==BLANK){
					//TODO
					//Ajouter dans un stack souvenir
					memory.push(iTab+1,jTab+1);
				}
				if((int)(tab[iTab-1][jTab])==BLANK){
					memory.push(iTab-1, jTab);
					int x1 = iTab;
					int y1 = jTab;
					while((int)(tab[x1-1][y1])==BLANK){
						x1--;
					}
					/*if(memory.back()[0]<x1){
						memory.pop();
						System.out.println("Memoire3" + x1 + y1);
						memory.push(x1+1, y1+1);
						System.out.println(memory.back()[0] +" 000 " + memory.back()[1] );
					}*/
					
					memory.push(x1, y1);
					return;
				}
				jTab++;
				
			}
			
			jTab = j;
			iTab++;
			//Pour revenir vers la gauche
			while(tab[iTab][jTab-1]==BLANK){
				jTab--;
			}
			j= jTab;
			
		}
		
		
		/*
		if((int)(tab[iTab][jTab])==BLANK){
			
		}*/
	}
	
	//Remplacement de la ligne de niveau
	//TODO: changer le paramètre lvl pour que ce soit dynamique
	//pas besoin de changer lvl à chaque fois...
	public static void border(int lvl){
		for(int i = 1 ; i < tab.length-1 ; i++){
			for(int j = 1 ; j< tab[0].length-1 ; j++){
				if( (tab[i][j] == BORDER) && //si on tombe sur une bordure et qu'autour il y a un cas traité
						//TODO : amélioration de l'ordre i+1 / i-1 etc...
						( (tab[i+1][j] == lvl ) || (tab[i-1][j] == lvl) || (tab[i][j+1] == lvl) || (tab[i][j-1] == lvl) ||
								(tab[i+1][j] == lvl-10 ) || (tab[i-1][j] == lvl-10) || (tab[i][j+1] == lvl-10) || (tab[i][j-1] == lvl-10)) ){
					tab[i][j] = lvl;
					
					//Ajout dans tableau de bordure
					int[] temp = {i,j,lvl};
					border[borderLast]=temp;
					borderLast++;
				}
			}
		}
	}

}
