package com.finalProject;
import static org.lwjgl.opengl.GL11.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class Piece implements Shape{

    ArrayList<Cube> cubes;
    int[][][] cubesArr;
    Color color;
    
    int centerX;
    int centerY;
    int centerZ;
    
    int sideLength; 
    
    public Piece(int x, int y, int z, int s){
       cubes = new ArrayList<Cube>(); 
       cubesArr = new int[3][3][3];
       
       Random rand = new Random();
       color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
       
       sideLength = s;
       
       centerX = x; 
       centerY = y;
       centerZ = z; 
       
       double chanceStep = 0; 
       cubesArr[1][1][1] = 1; 
              
       if(chance(.6 - chanceStep)){
           cubesArr[1][0][1] = 1;
           chanceStep += 0.1; 
       }
       if(chance(.6 - chanceStep)){
           cubesArr[1][2][1] = 1;
           chanceStep += 0.1;
       }
       if(chance(.3 - chanceStep)){
           cubesArr[0][1][1] = 1;
           chanceStep += 0.1;
       }
       if(chance(.3 - chanceStep)){
           cubesArr[2][1][1] = 1;
           chanceStep += 0.1; 
       }
       
       updateCubeSet();
       
    }
    
    public boolean chance(double pTrue){
        return Math.random() <= pTrue;
    }
    
    @Override
    public void translate(int x, int y, int z) {
        centerX += x; 
        centerY += y;
        centerZ += z;
        
        for(Cube c: cubes){
            c.translate(x, y, z);
        }
    }

    @Override
    public void rotate(int x, int y, int z) {
        if(x == 1){
            rotateXCW();
        }else if(x == -1){
            rotateXCCW();
        }
        
        if(y == 1){
            rotateYCW();
        }else if(y == -1){
            rotateYCCW();
        }
        
        if(z == 1){
            rotateZCW();
        }else if(z == -1){
            rotateZCCW();
        }
    }

    @Override
    public void draw3D() {
        glPushMatrix();

        for(Cube c: cubes){
            c.draw3D();
        }

        glPopMatrix();
    }
    
    public void rotateXCCW(){
        int[][][] n = new int[cubesArr.length][cubesArr[0].length][cubesArr[0][0].length];
        
        for(int j = 0; j < cubesArr[0].length; j++){
            int in = cubesArr.length - 1;
            int kn = 0;
            
            for(int i = 0; i < cubesArr.length; i++){
                in = cubesArr.length - 1;
                for(int k = 0; k < cubesArr[i][j].length; k++){
                    n[in][j][kn] = cubesArr[i][j][k];
                    in--;
                }
                kn++;
            }
        }
        
        cubesArr = n; 
        updateCubeSet();
    }
    
    public void rotateXCW(){
        int[][][] n = new int[cubesArr.length][cubesArr[0].length][cubesArr[0][0].length];
        
        for(int j = 0; j < cubesArr[0].length; j++){
            int in = cubesArr.length - 1;
            int kn = 0;
            
            for(int i = cubesArr.length - 1; i >= 0; i--){
                in = cubesArr.length - 1;
                for(int k = cubesArr[i][j].length - 1; k >= 0; k--){
                    n[in][j][kn] = cubesArr[i][j][k];
                    in--;
                }
                kn++;
            }
        }
        
        cubesArr = n; 
        updateCubeSet();
    }
    
    public void rotateYCCW(){
        int[][][] n = new int[cubesArr.length][cubesArr[0].length][cubesArr[0][0].length];
        
        for(int k = 0; k < cubesArr[0][0].length; k++){
            int in = 0;
            int jn = 0;
            
            for(int j = cubesArr[0].length - 1; j >= 0; j--){
                jn = 0;
                for(int i = 0; i < cubesArr.length; i++){
                    n[in][jn][k] = cubesArr[i][j][k];
                    jn++;
                }
                in++;
            }
        }
        
        cubesArr = n; 
        updateCubeSet();
    }
    
    public void rotateYCW(){
        int[][][] n = new int[cubesArr.length][cubesArr[0].length][cubesArr[0][0].length];
        
        for(int k = 0; k < cubesArr[0][0].length; k++){
            int in = 0;
            int jn = 0;
            
            for(int j = 0; j < cubesArr[0].length; j++){
                jn = 0;
                for(int i = cubesArr.length - 1; i >= 0; i--){
                    n[in][jn][k] = cubesArr[i][j][k];
                    jn++;
                }
                in++;
            }
        }
        
        cubesArr = n; 
        updateCubeSet();
    }
    
    public void rotateZCCW(){
        int[][][] n = new int[cubesArr.length][cubesArr[0].length][cubesArr[0][0].length];
        
        for(int i = 0; i < cubesArr.length; i++){
            int jn = 0;
            int kn = 0;
            
            for(int j = cubesArr[0].length - 1; j >= 0; j--){
                jn = 0;
                for(int k = 0; k < cubesArr[i][j].length; k++){
                    n[i][jn][kn] = cubesArr[i][j][k];
                    jn++;
                }
                kn++;
            }
        }
        
        cubesArr = n; 
        updateCubeSet();
    }
    
    public void rotateZCW(){
        int[][][] n = new int[cubesArr.length][cubesArr[0].length][cubesArr[0][0].length];
        
        for(int i = 0; i < cubesArr.length; i++){
            int jn = 0;
            int kn = 0;
            
            for(int j = 0; j < cubesArr[i].length; j++){
                jn = 0;
                for(int k = cubesArr[i][j].length - 1; k >= 0; k--){
                    n[i][jn][kn] = cubesArr[i][j][k];
                    jn++;
                }
                kn++;
            }
        }
        
        cubesArr = n; 
        updateCubeSet();
    }
    
    public void updateCubeSet(){
        cubes = new ArrayList<Cube>();
        
        for(int i = 0; i < cubesArr.length; i++){
            for(int j = 0; j < cubesArr[i].length; j++){
                for(int k = 0; k < cubesArr[i][j].length; k++){
                    if(cubesArr[i][j][k] == 1){
                    int cx = centerX + (j-1)*sideLength;
                    int cz = centerZ + (i-1)*sideLength; 
                    int cy = centerY - (k-1)*sideLength;
                    
                    cubes.add(new Cube(cx, cy, cz, sideLength/2, color));
                    }
                }
            }
        }
    }
    
    public Collection<Cube> getCubes(){
        return cubes;
    }
    
}
