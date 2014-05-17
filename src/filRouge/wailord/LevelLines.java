package filRouge.wailord;

import java.util.ArrayList;

import android.graphics.Point;

public abstract class LevelLines {
    /**
     * Will detect level lines in the shapeMap (and the corresponding pointMap) and replace them with the corresponding height
     * @param shapeMap an integer map with 0 for non-level line tile and 1 for level lines
     * @param pointMap a representation of the map with Points
     * @param height the height between two level lines
     */
    public static void fillHeightsInMap(int[][] shapeMap, Point[][] pointMap, int height){
        int level = 2;
        int countFalses = 0;
        while(countFalses < 2){
            boolean b = fillTheBlank(shapeMap,pointMap,level,height);
            //System.out.println(b);
            if(!b){
                level++;
                countFalses++;
            }
            else{
                countFalses = 0;
            }
        }
    }


    /**
     * Will highlight the first level line it finds, with the designated level and height
     * @param shapeMap the height map
     * @param pointMap the coordinates map
     * @param level the current line level
     * @param height the height between two level lines
     * @return a boolean wether or not a line was found
     */
    public static boolean fillTheBlank(int[][] shapeMap, Point[][] pointMap, int level, int height){
        ArrayList<Point> openList = new ArrayList<Point>();
        ArrayList<Point> closeList = new ArrayList<Point>();
        Point startPoint = null;
        
        for(int i=0;i<shapeMap.length;i++){
            for(int j=0;j<shapeMap[i].length;j++){
                
                    if(shapeMap[i][j] == 0){
                        startPoint = pointMap[i][j];
                        break;
                    }
                
            }
            if(startPoint != null){
                break;
            }
        }
        
        openList.add(startPoint);
        closeList.add(startPoint);
        startPoint = null;
        while(!openList.isEmpty()){
            //System.out.println(openList);
            Point victim = openList.get(0);
            openList.remove(0);
            ArrayList<Point> vlist = surroundings(victim,false, pointMap);
            for(Point p:vlist){
                if(shapeMap[p.y][p.x] < (level*height)){
                    if(!closeList.contains(p)){
                        closeList.add(p);
                        openList.add(p);
                    }
                }
                if(shapeMap[p.y][p.x] == 1){
                    startPoint = p;
                    break;
                }
            }
            if(startPoint != null){
                break;
            }
        }
        
        if(startPoint != null){
            openList.clear();
            closeList.clear();
            
            openList.add(startPoint);
            closeList.add(startPoint);
            
            while(!openList.isEmpty()){
                //System.out.println(openList);
                Point victim = openList.get(0);
                openList.remove(0);
                ArrayList<Point> vlist = surroundings(victim,true,pointMap);
                for(Point p:vlist){
                    if(shapeMap[p.y][p.x] == 1){
                        if(!closeList.contains(p)){
                            closeList.add(p);
                            openList.add(p);
                        }
                    }
                }
            }
            
            for(Point p:closeList){
                shapeMap[p.y][p.x] = height*level;
            }
            //repaint();
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Returns a list of the surrounding tiles
     * @param p the start tile
     * @param diag a boolean enabling diagonal search
     * @param pointMap  the point map
     * @return a list of neighbouring points
     */
    public static ArrayList<Point> surroundings(Point p, boolean diag, Point[][] pointMap){
        ArrayList<Point> result = new ArrayList<Point>();
        int x = p.x;
        int y = p.y;
        int boundY = pointMap.length-1;
        int boundX = pointMap[0].length-1;
        
        if(x>0){
            if(y > 0 && diag){
                result.add(pointMap[y-1][x-1]);
            }
            result.add(pointMap[y][x-1]);
            if(y < boundY && diag){
                result.add(pointMap[y+1][x-1]);
            }
        }
        if(x<boundX){
            if(y > 0 && diag){
                result.add(pointMap[y-1][x+1]);
            }
            result.add(pointMap[y][x+1]);
            if(y < boundY && diag){
                result.add(pointMap[y+1][x+1]);
            }
        }
        
        if(y > 0){
            result.add(pointMap[y-1][x]);
        }
        if(y < boundY){
            result.add(pointMap[y+1][x]);
        }
        
        return result;
    }
    
    public static int[][] smoothOnce(int[][] tab, int[][] cpy) {
        int[][] tab2 = new int[tab.length][tab[0].length];
        for (int i = 1; i < (tab.length - 1); i++) {
            for (int j = 1; j < (tab[0].length - 1); j++) {
                if(cpy[i][j] == 0){
                    tab2[i][j] = (tab[i + 1][j + 1] + tab[i - 1][j - 1] + tab[i + 1][j - 1] + tab[i - 1][j + 1]) / 4;
                }
                else{
                    tab2[i][j] = cpy[i][j];
                }
            }
        }
        return tab2;
    }
    
    public static int[][] smoothSeveral(int[][] tab, int[][] cpy, int n){
        for(int i=0;i<n;i++){
            tab = smoothOnce(tab, cpy);
        }
        return tab;
    }
}
