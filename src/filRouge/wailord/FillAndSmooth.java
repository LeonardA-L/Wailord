package filRouge.wailord;

public class FillAndSmooth {
	static int[][] tab;

	final static int PAS = 10;

	final static int BORDER = -2;
	final static int BLANK = -1;

	static int LARGE;
	static int HAUT;

	static int iTab = 0;
	static int jTab = 0;

	static Stack memory = new Stack(10000); // 0->x 1->y

	static int[][] border;
	static int borderLast = 0;

	static int lvl = 0;
	static int lvlLast = 0;

	/**
	 * Méthode à appeler pour récuper le tableau une fois traité
	 * binarizedAray : Tableau binarisée -1: vide (blanc) -2: traits (noir)
	 * nbIteSmooth : Nombre d'itération de smoothage
	 */
	public static int[][] FillDatAray(int[][] binarizedAray, int nbIteSmooth) {
		tab = binarizedAray;
		LARGE = binarizedAray[0].length;
		HAUT = binarizedAray.length;

		// On suppose que les bordures ne représentent pas plus de 30% du
		// tableau
		border = new int[(int) (0.3 * LARGE * HAUT)][3];

		start(nbIteSmooth);

		return tab;

	}

	public static void start(int nbIte) {

		intro(0);

		// display(tab);

		// initialisation
		memory.push(1, 1);

		// Phase Moteur de remplissage
		while (!memory.empty()) {

			while (!memory.empty()) {
				// supprimer le dernier element
				int x = memory.back()[0];
				int y = memory.back()[1];
				memory.pop();
				fill(x, y);
			}
			lvlLast = lvl;
			findNextBlank();
			// System.out.println("MAIN le level = "+lvl+ " / memory = "+
			// memory.empty());
			// System.out.println("MAIN " + memory.back()[0] + " " +
			// smemory.back()[1]+ " levelLast : "+ lvlLast);
			if (lvl != lvlLast && lvlLast != 0) {
				border(lvlLast);
			}
		}

		fade(nbIte);

		// display(tab);

	}

	// Affichage du tableau passé en paramètre
	/*
	 * public static void display(int[][] tab){ for(int i = 0 ; i < tab.length ;
	 * i++){ for( int j = 0 ; j < tab[0].length ; j++){
	 * System.out.print((int)tab[i][j] + "\t"); } System.out.println(); } }
	 */

	// Lissage du niveau
	public static void fade(int nbIte) {
		int[][] tab2 = new int[HAUT][LARGE];

		// Première moyenne
		for (int ite = 0; ite <= nbIte; ite++) {
			// Moyenne de l'intérieur
			for (int i = 1; i < (tab.length - 1); i++) {
				for (int j = 1; j < (tab[0].length - 1); j++) {
					tab2[i][j] = (int) ((tab[i][j + 1] + tab[i][j - 1]
							+ tab[i + 1][j] + tab[i - 1][j]) / 4);
				}
			}
			// Moyenne Effet de bords
			for (int i = 1; i < (tab.length - 1); i++) {
				tab2[i][0] = (int) ((tab[i + 1][0] + tab[i - 1][0] + tab[i][1]) / 3);
				tab2[i][LARGE - 1] = (int) ((tab[i + 1][LARGE - 1]
						+ tab[i - 1][LARGE - 1] + tab[i][LARGE - 2]) / 3);
			}
			for (int i = 1; i < (tab[0].length - 1); i++) {
				tab2[0][i] = (int) ((tab[1][i] + tab[0][i - 1] + tab[0][i + 1]) / 3);
				tab2[HAUT - 1][i] = (int) ((tab[HAUT - 1][i + 1]
						+ tab[HAUT - 1][i - 1] + tab[HAUT - 2][i]) / 3);
			}

			// Réinitialisation des bordures
			for (int avBorder = 0; avBorder < borderLast; avBorder++) {
				tab2[border[avBorder][0]][border[avBorder][1]] = border[avBorder][2];
			}

			for (int i = 1; i < (tab.length - 1); i++) {
				for (int j = 1; j < (tab[0].length - 1); j++) {
					tab[i][j] = (int) ((tab2[i][j + 1] + tab2[i][j - 1]
							+ tab2[i + 1][j] + tab2[i - 1][j]) / 4);
				}
			}
			// Moyenne Effet de bords
			for (int i = 1; i < (tab.length - 1); i++) {
				tab[i][0] = (int) ((tab2[i + 1][0] + tab2[i - 1][0] + tab2[i][1]) / 3);
				tab[i][LARGE - 1] = (int) ((tab2[i + 1][LARGE - 1]
						+ tab2[i - 1][LARGE - 1] + tab2[i][LARGE - 2]) / 3);
			}
			for (int i = 1; i < (tab[0].length - 1); i++) {
				tab[0][i] = (int) ((tab2[1][i] + tab2[0][i - 1] + tab2[0][i + 1]) / 3);
				tab[HAUT - 1][i] = (int) ((tab2[HAUT - 1][i + 1]
						+ tab2[HAUT - 1][i - 1] + tab2[HAUT - 2][i]) / 3);
			}

			// Réinitialisation des bordures
			for (int avBorder = 0; avBorder < borderLast; avBorder++) {
				tab[border[avBorder][0]][border[avBorder][1]] = border[avBorder][2];
			}

		}
		// return tab2;
	}

	// met une bordure tout autour à bounds pour éviter outOfBounds
	public static void intro(int bounds) {
		for (int i = 0; i < tab.length; i++) {
			tab[i][0] = bounds;
			tab[i][tab[0].length - 1] = bounds;
		}
		for (int i = 0; i < tab[0].length; i++) {
			tab[0][i] = bounds;
			tab[tab.length - 1][i] = bounds;
		}
	}

	// Trouve la prochaine zone à partir de laquelle commencer le remplissage
	public static void findNextBlank() {
		// Boucle permettant de vérifier la présence d'une autre zone fermée
		// de meme altitude
		for (int i = tab.length - 1; i > 1; i--) {
			int ans = 0;
			for (int j = tab[0].length - 1; j > 1; j--) {

				if (tab[i][j] != BLANK && tab[i][j] != BORDER) {
					ans = (int) tab[i][j];
				}

				if (tab[i][j] == BLANK && ans == lvl - PAS) {
					memory.push(i, j);
					return;
				}
			}
		}

		lvl += PAS;
		for (int i = 1; i < tab.length - 1; i++) {
			for (int j = 1; j < tab[0].length - 1; j++) {
				if (tab[i][j] == BLANK) {
					memory.push(i, j);
					return;
				}
			}
		}
	}

	// Da Methode to fill
	public static void fill(int i, int j) {
		iTab = i;
		jTab = j;
		while ((int) (tab[iTab][jTab]) == BLANK) {
			while ((int) (tab[iTab][jTab]) == BLANK) {

				tab[iTab][jTab] = lvl;

				if ((int) (tab[iTab + 1][jTab]) != BLANK
						&& (int) (tab[iTab + 1][jTab + 1]) == BLANK
						&& (int) (tab[iTab][jTab + 1]) == BLANK) {
					// Ajouter dans un stack souvenir
					memory.push(iTab + 1, jTab + 1);
				}
				if ((int) (tab[iTab - 1][jTab]) == BLANK) {
					memory.push(iTab - 1, jTab);
					int x1 = iTab;
					int y1 = jTab;
					while ((int) (tab[x1 - 1][y1]) == BLANK) {
						x1--;
					}
					/*
					 * if(memory.back()[0]<x1){ memory.pop();
					 * System.out.println("Memoire3" + x1 + y1);
					 * memory.push(x1+1, y1+1);
					 * System.out.println(memory.back()[0] +" 000 " +
					 * memory.back()[1] ); }
					 */

					memory.push(x1, y1);
					return;
				}
				jTab++;

			}

			jTab = j;
			iTab++;
			// Pour revenir vers la gauche
			while (tab[iTab][jTab - 1] == BLANK) {
				jTab--;
			}
			j = jTab;

		}

	}

	// Remplacement de la ligne de niveau
	// TODO: changer le paramètre lvl pour que ce soit dynamique
	// pas besoin de changer lvl à chaque fois...
	public static void border(int lvl) {
		for (int i = 1; i < tab.length - 1; i++) {
			for (int j = 1; j < tab[0].length - 1; j++) {
				if ((tab[i][j] == BORDER) && // si on tombe sur une bordure et
												// qu'autour il y a un cas
												// traité
						// TODO : amélioration de l'ordre i+1 / i-1 etc...
						((tab[i + 1][j] == lvl) || (tab[i - 1][j] == lvl)
								|| (tab[i][j + 1] == lvl)
								|| (tab[i][j - 1] == lvl)
								|| (tab[i + 1][j] == lvl - PAS)
								|| (tab[i - 1][j] == lvl - PAS)
								|| (tab[i][j + 1] == lvl - PAS) || (tab[i][j - 1] == lvl
								- PAS))) {
					tab[i][j] = lvl;

					// Ajout dans tableau de bordure
					int[] temp = { i, j, lvl };
					border[borderLast] = temp;
					borderLast++;
				}
			}
		}
	}

}