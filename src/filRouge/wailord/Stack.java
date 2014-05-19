package filRouge.wailord;
public class Stack {

private static int last;

private int[][] memory;

public Stack(int value){
memory = new int[value][2];
last = 0;
}

public int[] back(){
/*int i = 0;
while(memory[i][0]!=0){
i++;
}*/

return memory[last];
}

public void pop(){
memory[last][0]=0;
memory[last][1]=0;
last--;
}

public void push(int x, int y){
last++;
memory[last][0]=x;
memory[last][1]=y;
}

//return true si vide
public boolean empty(){
if(last==0){
return true;
} else {
return false;
}
}

}