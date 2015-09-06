/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lancedh.rifts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 *
 * @author LanceDH
 */
public class Generator {

    private InfoBoard _infoBoard;
    private MobHandler _mh;
    private Maze _maze;
    private Server _server;
    private List<Player> _players;
    
    public Generator(World w, Server s) {
        _maze = new Maze(w);
        _infoBoard = new InfoBoard(s);
        _mh = new MobHandler(w);
        _server = s;
        _players = new ArrayList<>();
    }
    
    public void AddPlayer(Player p){
        _players.add(p);
    }
    
    public void UpdateScoreboard(int count){
        _infoBoard.SetMobsLeft(count);
    }
    
    public void ShowScoreboardToPlayer(Player p){
        _infoBoard.ShowToPlayer(p);
    }
    
    
    
    public void UnchainLoSMobs(Player p){
        _mh.UnchainLoSMobs(p);
    }
    
    public void PlayerDies(){
        _mh.DespawnAllMobs();
    }
    
    public void EntityDied(Entity e){
        // Check if player died
        if(e.getType() == EntityType.PLAYER){
            _server.broadcastMessage("You failed!");
            PlayerDies();
            return;
        }
        
        // Otherwise monster died
        int alive = _mh.CountAliveMobs();
        UpdateScoreboard(alive);
        if(_mh.BossIsSpawned()){
            if(_mh.BossIsDead()){
                _server.broadcastMessage("Level won!");
                SetupNewLevel();
                return ;
            }
        }else{
            if(alive == 0){
                _mh.SpawnBoss(_maze.GetBossSpawn());
                _server.broadcastMessage("The boss has spawned at the end of the maze!");
                return ;
            }
        }
    }
    
    private int GetSizeForLevel(int level){
        switch(level){
            case 1:
                return 4;
            case 2:
                return 4;
            case 3:
                return 5;
            case 4:
                return 5;
            case 5:
                return 5;
            case 6:
                return 6;
            case 7:
                return 6;
            case 8:
                return 6;
        }
        
        return 7;
    }
    
    public void SetupNewLevel(){
        _mh.DespawnAllMobs();
        
        int x = _maze.GetLevel() + 1;
        int size = GetSizeForLevel(x);
        
        _maze.CreateMaze(size);
        
        for (Player player : _players) {
            player.teleport(_maze.GetStartPoint());
            ShowScoreboardToPlayer(player);
        
            Random rng = new Random();
            if (rng.nextInt(2)== 1) {
                CombatClass.EquipPlayer(CombatClass.Class.ARCHER, player);
            }else{
                CombatClass.EquipPlayer(CombatClass.Class.WARRIOR, player);
            }
        }
        
        // Calc mobs number based on current level
        
        int mobCount = (x*x) + (x * 4) + 7;
        
        //Spawn mobs at spawn locations
        
        _mh.SpawnMobs(mobCount, _maze.GetMobSpawns());
        
        _infoBoard.SetLevel(_maze.GetLevel());
        _infoBoard.SetMobsLeft(_mh.CountAliveMobs());
    }
}
