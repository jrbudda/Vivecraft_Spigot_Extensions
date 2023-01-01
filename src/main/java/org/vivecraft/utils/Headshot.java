package org.vivecraft.utils;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.vivecraft.VSE;

/**
 * Created by Yildri on 7-1-2017.
 */
public class Headshot {
    private static VSE vse;

    public static void init(VSE plugin){
        vse = plugin;
    }

    public static boolean isHeadshot(Entity target, Arrow arrow){
        boolean headshot = false;

        if(target.isInsideVehicle())
            return false;

        if(target instanceof Player){
            Player player = (Player) target;
            if(player.isSneaking()){
                //totalHeight = 1.65;
                //bodyHeight = 1.20;
                //headHeight = 0.45;
                if(arrow.getLocation().getY() >= player.getLocation().getY() + 1.20)
                    headshot = true;

            }else if(!player.isGliding()){
                //This means they must be standing normally (I can't calculate it for gliding players)
                //totalHeight = 1.80;
                //bodyHeight = 1.35;
                //headHeight = 0.45;
                if(arrow.getLocation().getY() >= player.getLocation().getY() + 1.35)
                    headshot = true;
            }

        }else if(!vse.getConfig().getBoolean("bow.headshotmobs")){
            return false;
        //TODO: Mobs
        /*}else if(target instanceof Zombie){
            Zombie zombie = (Zombie) target;
            if(zombie.isBaby()){

            }else{
            //totalHeight = 1.80;
            // bodyHeight = 1.35;
            //headHeight = 0.45;
            if(arrow.getLocation().getY() >= zombie.getLocation().getY() + 1.35)
                headshot = true;
            }*/
        }

        return headshot;
    }
}