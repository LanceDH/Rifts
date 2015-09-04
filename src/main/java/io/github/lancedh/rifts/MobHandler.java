/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lancedh.rifts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author LanceDH
 */
public class MobHandler {
    private List<LivingEntity> _spawnedMobs;
    private List<LivingEntity> _chainedMobs;
    private World _world;
    private LivingEntity _boss;
    private boolean _bossHasSpawned = false;

    public MobHandler(World world) {
        _world = world;
        _spawnedMobs = new ArrayList<LivingEntity>();
        _chainedMobs = new ArrayList<LivingEntity>();
    }
    
    public void SpawnMobs(int amount, List<Location> spots){
        
        int minPerSpawn = amount / spots.size();
        
        for (Location l : spots) {
            for (int i = 0; i < minPerSpawn; i++) {
                SpawnMob(l, EntityType.ZOMBIE);
            }
        }
        
        //randomly spawn remaining
        Random rng = new Random();
        for (int i = 0; i < amount % spots.size(); i++) {
            SpawnMob(spots.get(rng.nextInt(spots.size())), EntityType.ZOMBIE);
        }
    }
    
    private void SpawnMob(Location loc, EntityType type){
        LivingEntity mob = (LivingEntity) _world.spawnEntity(loc, type);
        mob.setRemoveWhenFarAway(false);
        // Because telling a creature with half a brain to stay doesn't go well
        mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1000000, 255));
            
        _spawnedMobs.add(mob);
        _chainedMobs.add(mob);
    }
    
    public void SpawnBoss(Location loc){
        LivingEntity mob = (LivingEntity) _world.spawnEntity(loc, EntityType.ZOMBIE);
        mob.setRemoveWhenFarAway(false);
        mob.setMaxHealth(40);
        
        _bossHasSpawned = true;
    }
    
    public void UnchainLoSMobs(Player p){
        LivingEntity mob = null;
        for (int i = _chainedMobs.size()-1; i >= 0; i--) {
            
            mob = _chainedMobs.get(i);
            if(p.hasLineOfSight(mob)){
                mob.removePotionEffect(PotionEffectType.SLOW);
                _chainedMobs.remove(i);
            }
        }
    }
    
    public void DespawnAllMobs(){
        LivingEntity mob = null;
        for (LivingEntity _spawnedMob : _spawnedMobs) {
            _spawnedMob.remove();
        }
        _spawnedMobs.clear();
        _chainedMobs.clear();
        _bossHasSpawned = false;
    }
    
    public int CountAliveMobs(){
        LivingEntity mob = null;
        for (int i = _spawnedMobs.size()-1; i >= 0; i--) {
            
            mob = _spawnedMobs.get(i);
            if(mob.isDead()){
                _spawnedMobs.remove(i);
            }
        }
        
        return _spawnedMobs.size();
    }
    
    public boolean BossIsSpawned(){
        return _bossHasSpawned;
    }
    
    public boolean BossIsDead(){
        if(_boss != null){
            return _boss.isDead();
        }
        return true;
    }
}
