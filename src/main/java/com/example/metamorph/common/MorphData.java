package com.example.metamorph.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

import java.util.HashSet;
import java.util.Set;

public class MorphData implements IExtendedEntityProperties {

    public static final String PROP = "MetamorphData";

    private int morphId = 0;
    private Set<Integer> unlockedMorphs = new HashSet<Integer>();

    public static void register(EntityPlayer player) {
        if (player.getExtendedProperties(PROP) == null) {
            player.registerExtendedProperties(PROP, new MorphData());
        }
    }

    public static MorphData get(EntityPlayer player) {
        return (MorphData) player.getExtendedProperties(PROP);
    }

    public int getMorphId() {
        return morphId;
    }

    public void setMorphId(int morphId) {
        this.morphId = morphId;
    }

    public boolean isUnlocked(int id) {
        return id == 0 || unlockedMorphs.contains(id);
    }

    public void unlock(int id) {
        unlockedMorphs.add(id);
    }

    public Set<Integer> getUnlocked() {
        return unlockedMorphs;
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("MorphId", morphId);
        
        NBTTagList list = new NBTTagList();
        for (Integer id : unlockedMorphs) {
            NBTTagCompound t = new NBTTagCompound();
            t.setInteger("id", id);
            list.appendTag(t);
        }
        tag.setTag("Unlocked", list);
        
        compound.setTag(PROP, tag);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        if (compound.hasKey(PROP, 10)) {
            NBTTagCompound tag = compound.getCompoundTag(PROP);
            morphId = tag.getInteger("MorphId");
            
            unlockedMorphs.clear();
            if (tag.hasKey("Unlocked", 9)) {
                NBTTagList list = tag.getTagList("Unlocked", 10);
                for (int i = 0; i < list.tagCount(); i++) {
                    unlockedMorphs.add(list.getCompoundTagAt(i).getInteger("id"));
                }
            }
        }
    }

    @Override
    public void init(Entity entity, World world) {
    }
}
