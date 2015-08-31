/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lancedh.rifts;

import java.util.Random;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author LanceDH
 */
public class Rifts extends JavaPlugin implements Listener{
    private Generator gen = null;
    
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
            
            if(gen == null){
                gen = new Generator((Player)sender, 7);
            }
            
            gen.GenNextChunk();
            gen.DrawMaze();

            return true;
	}
        
        if (cmd.getName().equalsIgnoreCase("draw")) { 
            if(!(sender instanceof Player)){return false;}
            
            if(gen != null){
                gen.DrawMaze();
            }
	}
        
         if (cmd.getName().equalsIgnoreCase("gen")) { 
            if(!(sender instanceof Player)){return false;}
            
            if(gen == null){
                gen = new Generator((Player)sender, 7);
            }
            
            gen.WipeMaze();
            
            int size = Integer.parseInt(args[0]);
             for (int i = 0; i < size; i++) {
                gen.GenNextChunk();
                
             }
            
             gen.DrawMaze();
            
            return true;
	}
	return false; 
    }
    
    
    
    
}
