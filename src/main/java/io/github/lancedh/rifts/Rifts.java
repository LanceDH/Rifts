/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lancedh.rifts;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author LanceDH
 */
public class Rifts extends JavaPlugin implements Listener{
    private Generator _gen = null;
    private World _world = null;
    private MobHandler _mh = null;
    private Player _player = null;
    
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Rifts has been enabled.");
        _world = Bukkit.getWorlds().get(0);
        _mh = new MobHandler(_world);
        
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Rifts has been disabled.");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("place")) { 
            if(!(sender instanceof Player)){return false;}
            
            for (int i = 0; i < 300; i++) {
                System.out.println(i + ": " + (char)i); 
            }
            
            return true;
	}
        
        if (cmd.getName().equalsIgnoreCase("draw")) { 
            if(!(sender instanceof Player)){return false;}
            
            if(_gen != null){
                _gen.FillChunkStorage();
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
            
            SetupNewLevel(size, mobCount);
            
            return true;
	}
	return false; 
    }
    
    @EventHandler
    public void OnPlayerMove(PlayerMoveEvent event){
        event.getPlayer().setFoodLevel(20);
        _mh.UnchainLoSMobs(event.getPlayer());
    }
    
    @EventHandler
    public void OnEntityDeath(EntityDeathEvent event){
        LivingEntity entity = event.getEntity();
        if(entity.getType() == EntityType.PLAYER){
            getServer().broadcastMessage("You failed!");
            _mh.DespawnAllMobs();
            return;
        }
        
        int alive = _mh.CountAliveMobs();
        if(_mh.BossIsSpawned()){
            if(_mh.BossIsDead()){
                getServer().broadcastMessage("Level won!");
                SetupNewLevel(7, 50);
                return;
            }
        }else{
            if(alive == 0){
                _mh.SpawnBoss(_gen.GetBossSpawn());
                getServer().broadcastMessage("The boss has spawned at the end of the maze!");
                return;
            }
            else{
                getServer().broadcastMessage(alive + " mobs remain!");
                return;
            }
        }
    }
    
    public void SetupNewLevel(int mazeSize, int mobCount){
        _mh.DespawnAllMobs();
        _gen = new Generator(_player, mazeSize);
        _gen.CreateMaze();
            
        _player.getInventory().clear();
        _player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        _player.teleport(_gen.GetStartPoint());
            
        //Spawn mobs at spawn locations
        _mh.SpawnMobs(mobCount, _gen.GetMobSpawns());
    }
}
