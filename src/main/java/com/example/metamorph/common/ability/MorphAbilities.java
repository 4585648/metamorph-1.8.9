package com.example.metamorph.common.ability;

import com.example.metamorph.common.IMorphAbility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;

public class MorphAbilities {

    // 僵尸：原版无特殊被动
    public static class ZombieAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) {}
        public void onMorphEnd(EntityPlayer p) {}
        public void onUpdate(EntityPlayer p) {}
    }

    // 蜘蛛：原版无特殊被动
    public static class SpiderAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) {}
        public void onMorphEnd(EntityPlayer p) {}
        public void onUpdate(EntityPlayer p) {}
    }

    // 史莱姆：跳跃提升（原版特性）
    public static class SlimeAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) { p.addPotionEffect(new PotionEffect(Potion.jump.id, Integer.MAX_VALUE, 2, true, false)); }
        public void onMorphEnd(EntityPlayer p) { p.removePotionEffect(Potion.jump.id); }
        public void onUpdate(EntityPlayer p) {}
    }

    // 鸡：缓降
    public static class ChickenAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) {}
        public void onMorphEnd(EntityPlayer p) {}
        public void onUpdate(EntityPlayer p) {
            if (p.fallDistance > 0 && !p.onGround && p.motionY < -0.08D) {
                p.motionY = -0.08D;
            }
        }
    }

    // 烈焰人：抗火
    public static class BlazeAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) { p.addPotionEffect(new PotionEffect(Potion.fireResistance.id, Integer.MAX_VALUE, 0, true, false)); }
        public void onMorphEnd(EntityPlayer p) { p.removePotionEffect(Potion.fireResistance.id); }
        public void onUpdate(EntityPlayer p) {}
    }

    // 末影人：怕水
    public static class EndermanAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) {}
        public void onMorphEnd(EntityPlayer p) {}
        public void onUpdate(EntityPlayer p) {
            if (p.isWet()) {
                p.attackEntityFrom(DamageSource.drown, 1.0F);
            }
        }
    }

    // 鱿鱼：水下呼吸
    public static class SquidAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) { p.addPotionEffect(new PotionEffect(Potion.waterBreathing.id, Integer.MAX_VALUE, 0, true, false)); }
        public void onMorphEnd(EntityPlayer p) { p.removePotionEffect(Potion.waterBreathing.id); }
        public void onUpdate(EntityPlayer p) {}
    }

    // 以下生物的主动技能通过右键触发，此处留空
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

    public static class GhastAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) {}
        public void onMorphEnd(EntityPlayer p) {}
        public void onUpdate(EntityPlayer p) {}
    }
    
    public static class BatAbility implements IMorphAbility {
        public void onMorphStart(EntityPlayer p) {}
        public void onMorphEnd(EntityPlayer p) {}
        public void onUpdate(EntityPlayer p) {}
    }
}
