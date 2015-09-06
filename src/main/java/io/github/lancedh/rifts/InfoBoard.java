/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lancedh.rifts;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 *
 * @author LanceDH
 */
public class InfoBoard{
    private int _level = 0;
    private int _mobsLeft = 0;
    private Scoreboard _scoreboard;
    private Objective _sidebar;
    private Server _server;

    public InfoBoard(Server s) {
        _server = s;
        _scoreboard = _server.getScoreboardManager().getNewScoreboard();
        _sidebar = _scoreboard.registerNewObjective("- Rift level 0 -", "wut");
        _sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
        UpdateText();
    }
    
    private void UpdateText(){
        int line = 2;
        for (String s : _scoreboard.getEntries()) {
            _scoreboard.resetScores(s);
        }
        _sidebar.setDisplayName("- Rift level " + _level + " - ");
        _sidebar.getScore(" ").setScore(line--);
        _sidebar.getScore("Mobs: " + _mobsLeft).setScore(line--);
    }
    
    public void SetMobsLeft(int amount){
        _mobsLeft = amount;
        UpdateText();
    }
    
    public void SetLeveL(int level){
        _level = level;
        UpdateText();
    }
    
    public void ShowToPlayer(Player p){
        p.setScoreboard(_scoreboard);
    }
    
}
