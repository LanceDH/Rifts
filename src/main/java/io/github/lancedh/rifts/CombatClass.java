/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lancedh.rifts;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author LanceDH
 */
public class CombatClass {
    public enum Class{
        WARRIOR
        ,ARCHER
    }
    
    public static void EquipPlayer(Class type, Player p){
        p.getInventory().clear();
        
        
        switch(type){
            case WARRIOR:
                EquipWarriorGear(p);
                break;
            case ARCHER:
                EquipArcherGear(p);
                break;
        }

    }
    
    private static void EquipWarriorGear(Player p){
        // Armor
        p.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        p.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        
        // Other items
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        p.getInventory().addItem(sword);

    }
    
    private static void EquipArcherGear(Player p){
        // Armor
        p.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        p.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        
        // Other items
        ItemStack bow = new ItemStack(Material.BOW);
        bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
        p.getInventory().addItem(bow);
        p.getInventory().addItem(new ItemStack(Material.ARROW));
    }
}
