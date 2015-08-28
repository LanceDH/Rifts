/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lancedh.rifts;

import java.util.Random;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author LanceDH
 */
public class Rifts extends JavaPlugin implements Listener{
     private final int GROUND_Y = 7;
    private final int MAZE_GROUND = 32;
    private int _chunkSize = 0;
    private int _nrSpawnedChunks = 0;
    
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Rifts has been enabled.");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Rifts has been disabled.");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("place")) { 
            if(!(sender instanceof Player)){return false;}
            _chunkSize = CountChunkSize((Player)sender);
            Random rng = new Random();
            CopyChunk((Player)sender, rng.nextInt(7)+1);
            return true;
	}
	return false; 
    }
    
    public int CountChunkSize(Player p){
        Location loc = new Location(p.getLocation().getWorld(), 0, GROUND_Y, -1);
        int count = 1;
        while (count <= 16 && loc.getBlock().getType() != Material.WOOL) {
            count += 1;
            loc.add(1, 0, 0);
        }
        return count;
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
