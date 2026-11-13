package com.example.metamorph.network;

import com.example.metamorph.common.MorphManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.Set;

public class MorphUnlockMessage implements IMessage {

    private Set<Integer> unlocked = new HashSet<Integer>();

    public MorphUnlockMessage() {
    }

    public MorphUnlockMessage(Set<Integer> unlocked) {
        this.unlocked = unlocked;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        unlocked.clear();
        for (int i = 0; i < size; i++) {
            unlocked.add(buf.readInt());
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(unlocked.size());
        for (Integer id : unlocked) {
            buf.writeInt(id);
        }
    }

    public static class Handler implements IMessageHandler<MorphUnlockMessage, IMessage> {
        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(final MorphUnlockMessage message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                // 强制在主游戏线程更新缓存，防止并发集合读取不到更新
                Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                    @Override
                    public void run() {
                        MorphManager.setUnlockedClient(message.unlocked);
                    }
                });
            }
            return null;
        }
    }
}
