/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lancedh.rifts;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author LanceDH
 */
public class Generator {
    private final int GROUND_Y = 7;
    private final int MAZE_GROUND = 32;
    private int _chunkSize = 0;
    private int _nrSpawnedChunks = 0;

    public Generator(Player p) {
        InitChunkSize(p);
        CopyChunk(p, 0);
    }
    
    private void InitChunkSize(Player p){
        Location loc = new Location(p.getLocation().getWorld(), 0, GROUND_Y, -1);
        int count = 1;
        while (count <= 16 && loc.getBlock().getType() != Material.WOOL) {
            count += 1;
            loc.add(1, 0, 0);
        }
        _chunkSize = count;
    }
    
    public void CopyChunk(Player p, int chunkId){
        Location loc = p.getLocation();
        Block b = null;
        World w = loc.getWorld();
        
        for (int x = 0; x < _chunkSize; x++) {
            for (int y = 0; y < _chunkSize; y++) {
                for (int z = 0; z < _chunkSize; z++) {
                   b =  w.getBlockAt(x, GROUND_Y + y, chunkId * _chunkSize + z);
                   Location nloc = new Location(w, 0, MAZE_GROUND, _nrSpawnedChunks * _chunkSize);
                   nloc.add(x, y, z);
                   nloc.getBlock().setType(b.getType());
                   nloc.getBlock().setData(b.getData());
                }
            }
        }
        
        _nrSpawnedChunks += 1;
    }
}
