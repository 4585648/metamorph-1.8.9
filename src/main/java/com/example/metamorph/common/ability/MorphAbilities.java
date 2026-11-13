package com.example.metamorph.common.ability;

import com.example.metamorph.common.IMorphAbility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class MorphAbilities {

    // 僵尸：被动力量
    public static class ZombieAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) { p.addPotionEffect(new PotionEffect(Potion.damageBoost.id, Integer.MAX_VALUE, 0, true, false)); }
        public void onMorphEnd(EntityPlayer p) { p.removePotionEffect(Potion.damageBoost.id); }
        public void onUpdate(EntityPlayer p) {}
    }

    // 蜘蛛：被动夜视
    public static class SpiderAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) { p.addPotionEffect(new PotionEffect(Potion.nightVision.id, Integer.MAX_VALUE, 0, true, false)); }
        public void onMorphEnd(EntityPlayer p) { p.removePotionEffect(Potion.nightVision.id); }
        public void onUpdate(EntityPlayer p) {}
    }

    // 鸡：被动缓降（在空中时限制下落速度）
    public static class ChickenAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) {}
        public void onMorphEnd(EntityPlayer p) {}
        public void onUpdate(EntityPlayer p) {
            if (p.fallDistance > 0 && !p.onGround && p.motionY < -0.1D) {
                p.motionY = -0.1D;
            }
        }
    }

    // 史莱姆：被动跳跃提升
    public static class SlimeAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) { p.addPotionEffect(new PotionEffect(Potion.jump.id, Integer.MAX_VALUE, 2, true, false)); }
        public void onMorphEnd(EntityPlayer p) { p.removePotionEffect(Potion.jump.id); }
        public void onUpdate(EntityPlayer p) {}
    }

    // 烈焰人：被动抗火
    public static class BlazeAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) { p.addPotionEffect(new PotionEffect(Potion.fireResistance.id, Integer.MAX_VALUE, 0, true, false)); }
        public void onMorphEnd(EntityPlayer p) { p.removePotionEffect(Potion.fireResistance.id); }
        public void onUpdate(EntityPlayer p) {}
    }

    // 蝙蝠：被动移动速度提升
    public static class BatAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) { p.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, Integer.MAX_VALUE, 1, true, false)); }
        public void onMorphEnd(EntityPlayer p) { p.removePotionEffect(Potion.moveSpeed.id); }
        public void onUpdate(EntityPlayer p) {}
    }

    // 鱿鱼：被动水下呼吸
    public static class SquidAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) { p.addPotionEffect(new PotionEffect(Potion.waterBreathing.id, Integer.MAX_VALUE, 0, true, false)); }
        public void onMorphEnd(EntityPlayer p) { p.removePotionEffect(Potion.waterBreathing.id); }
        public void onUpdate(EntityPlayer p) {}
    }

    // 主动技能占位（逻辑在 CommonForgeEventHandler 中）
    public static class CreeperAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) {}
        public void onMorphEnd(EntityPlayer p) {}
        public void onUpdate(EntityPlayer p) {}
    }

    public static class SkeletonAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) {}
        public void onMorphEnd(EntityPlayer p) {}
        public void onUpdate(EntityPlayer p) {}
    }

    public static class EndermanAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) {}
        public void onMorphEnd(EntityPlayer p) {}
        public void onUpdate(EntityPlayer p) {}
    }

    public static class GhastAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) {}
        public void onMorphEnd(EntityPlayer p) {}
        public void onUpdate(EntityPlayer p) {}
    }
}
