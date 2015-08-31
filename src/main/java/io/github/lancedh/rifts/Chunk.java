/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lancedh.rifts;

/**
 *
 * @author LanceDH
 */
public class Chunk {
    private int chunkId = -1;
    private int xPos = 0;
    private int yPos = 0;

    public Chunk(int id, int x, int y) {
        chunkId = id;
        xPos = x;
        yPos = y;
    }

    public int getChunkId() {
        return chunkId;
    }

    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }
    
    
}
