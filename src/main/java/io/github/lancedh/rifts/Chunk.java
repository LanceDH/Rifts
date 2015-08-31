/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lancedh.rifts;

import static org.bukkit.Bukkit.getLogger;

/**
 *
 * @author LanceDH
 */
public class Chunk {
    private int chunkId = -1;
    private int xPos = 0;
    private int yPos = 0;
    private int[] freeSpots = new int[4];

    public Chunk(int id, int x, int y) {
        chunkId = id;
        xPos = x;
        yPos = y;
        for (int i = 0; i < 4; i++) {
            freeSpots[i] = 0;
        }
    }

    public int GetChunkId() {
        return chunkId;
    }


    public int GetxPos() {
        return xPos;
    }

    public int GetyPos() {
        return yPos;
    }
    
    public void AddDirectionToChunkId(int dir){
        chunkId += dir;
    }
    
    public void SetFreeSpots(int[] arr){
        freeSpots[0] = arr[0];
        freeSpots[1] = arr[1];
        freeSpots[2] = arr[2];
        freeSpots[3] = arr[3];
    }
    
    public int[] getFreeSpots() {
        return freeSpots;
    }

    public void PrintIfFreeSpots() {
        if(getFreeSpots()[0] == 0 && getFreeSpots()[1] == 0 && getFreeSpots()[2] == 0 && getFreeSpots()[3] == 0){
            return;
        }
        getLogger().info(xPos + "," + yPos + " : " + getFreeSpots()[0] + " " + getFreeSpots()[1] + " " + getFreeSpots()[2] + " " + getFreeSpots()[3]);
    }
    
    public boolean HasFreeSpots(){
        if(getFreeSpots()[0] == 0 && getFreeSpots()[1] == 0 && getFreeSpots()[2] == 0 && getFreeSpots()[3] == 0){
            return false;
        }
        return true;
    }


}
