/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lancedh.rifts;

import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author LanceDH
 */
public class Rifts extends JavaPlugin implements Listener{
    private Generator _gen;
    private World _world;
    private Player _player;
    private Server _server;
    
    @Override
    public void onEnable() {
        _server = getServer();
        _server.getPluginManager().registerEvents(this, this);
        getLogger().info("Rifts has been enabled.");
        _world = Bukkit.getWorlds().get(0);
        _gen = new Generator(_world, _server);
        
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Rifts has been disabled.");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("place")) { 
            if(!(sender instanceof Player)){return false;}
            
            ItemStack sword = new ItemStack(Material.IRON_SWORD);
            sword.addEnchantment(Enchantment.DAMAGE_ALL, 5);
            sword.addEnchantment(Enchantment.DAMAGE_UNDEAD, 5);
            ((Player)sender).getInventory().addItem(sword);
            
            return true;
	}
        
        if (cmd.getName().equalsIgnoreCase("draw")) { 
            if(!(sender instanceof Player)){return false;}
            
            if(_gen != null){
                //_gen.FillChunkStorage();
            }
	}
        
         if (cmd.getName().equalsIgnoreCase("gen")) { 
            if(!(sender instanceof Player)){return false;}
            _player = (Player)sender;
            
            int size = 7;
            int mobCount = 0;
            try {
                size = Integer.parseInt(args[0]);
                mobCount = Integer.parseInt(args[1]);
            } catch (Exception e) {
                sender.sendMessage("Wrong arguements.");
                return false;
            }

            if(size < 3){
                sender.sendMessage("Maze too small.");
                return false;
            }
            
            _gen.SetupNewLevel();
            
            return true;
	}
	return false; 
    }
    
    @EventHandler
    public void OnPlayerMove(PlayerMoveEvent event){
        event.getPlayer().setFoodLevel(20);
        _gen.UnchainLoSMobs(event.getPlayer());
        
    }
    
    @EventHandler
    public void OnEntityDeath(EntityDeathEvent event){
        LivingEntity entity = event.getEntity();

        _gen.EntityDied(entity);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        _gen.AddPlayer(event.getPlayer());
        //_gen.ShowScoreboardToPlayer(event.getPlayer());
    }
    
    
}
