package testswailord;

import java.util.ArrayList;

public abstract class LevelLines {
    public static void fillHeightsInMap(int N, int[][] shapeMap, Point[][] pointMap, int height){
        int level = 2;
        int countFalses = 0;
        while(countFalses < 2){
            boolean b = fillTheBlank(N,shapeMap,pointMap,level,height);
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
    
    public static boolean fillTheBlank(int N, int[][] shapeMap, Point[][] pointMap, int level, int height){
        ArrayList<Point> openList = new ArrayList<Point>();
        ArrayList<Point> closeList = new ArrayList<Point>();
        Point startPoint = null;
        
        for(int i=0;i<N;i++){
            for(int j=0;j<N;j++){
                
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
            ArrayList<Point> vlist = surroundings(victim,false, N, pointMap);
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
                ArrayList<Point> vlist = surroundings(victim,true,N,pointMap);
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
    
    public static ArrayList<Point> surroundings(Point p, boolean diag, int N, Point[][] pointMap){
        ArrayList<Point> result = new ArrayList<Point>();
        int x = p.x;
        int y = p.y;
        int bound = N-1;
        
        if(x>0){
            if(y > 0 && diag){
                result.add(pointMap[y-1][x-1]);
            }
            result.add(pointMap[y][x-1]);
            if(y < bound && diag){
                result.add(pointMap[y+1][x-1]);
            }
        }
        if(x<bound){
            if(y > 0 && diag){
                result.add(pointMap[y-1][x+1]);
            }
            result.add(pointMap[y][x+1]);
            if(y < bound && diag){
                result.add(pointMap[y+1][x+1]);
            }
        }
        
        if(y > 0){
            result.add(pointMap[y-1][x]);
        }
        if(y < bound){
            result.add(pointMap[y+1][x]);
        }
        
        return result;
    }
}
