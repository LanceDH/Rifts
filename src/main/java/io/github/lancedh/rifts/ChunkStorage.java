
/**
 * Chunk Storage is part of the generator.
 * It saves the different chunks at the creation of the world so they can be removed from the world.
 */

package io.github.lancedh.rifts;

import java.util.ArrayList;
import java.util.Random;
import org.bukkit.Bukkit;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

/**
 *
 * @author LanceDH
 */
public class ChunkStorage {
    
    // First ArrayList is the a list for the different ids (spawn, direction up, ..)
    // Second ArrayList is to store different variants
    // Block array is to store the cunk
    private ArrayList<ArrayList<BlockState[][][]>> chunkList;

    public ChunkStorage() {
        chunkList = new ArrayList<>();
        // 0: spawn
        // 1-15: different directions
        // 16-19: different end points
        for (int i = 0; i <= 19; i++) {
            chunkList.add(i, new ArrayList<BlockState[][][]>());
        }
    }
    
    public void Reset(){
        chunkList = new ArrayList<>();
        for (int i = 0; i <= 19; i++) {
            chunkList.add(i, new ArrayList<BlockState[][][]>());
        }
    }
    
    public void AddChunk(int id, BlockState[][][] chunk){
        ArrayList<BlockState[][][]> variantList = chunkList.get(id);
        //System.out.println(id +" before "+variantList.size());
        variantList.add(chunk);
        //System.out.println(id +" after "+variantList.size());
    }
    
    public BlockState[][][] GetChunkOfId(int id){
        ArrayList<BlockState[][][]> variantList = chunkList.get(id);
        Random r = new Random();
        int number = r.nextInt(variantList.size());
        //System.out.println(id + " has " + variantList.size() + " variants and using " + number);
        return variantList.get(number);
    }
}
