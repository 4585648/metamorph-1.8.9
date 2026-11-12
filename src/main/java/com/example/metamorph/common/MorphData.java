// src/main/java/com/example/metamorph/common/MorphData.java
package com.example.metamorph.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class MorphData implements IExtendedEntityProperties {

    public static final String PROP = "MetamorphData";

    private int morphId = 0;

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

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("MorphId", morphId);
        compound.setTag(PROP, tag);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        if (compound.hasKey(PROP, 10)) {
            NBTTagCompound tag = compound.getCompoundTag(PROP);
            morphId = tag.getInteger("MorphId");
        }
    }

    @Override
    public void init(Entity entity, World world) {

    }
}