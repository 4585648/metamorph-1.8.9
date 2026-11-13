package com.example.metamorph.network;

import com.example.metamorph.common.CommonForgeEventHandler;
import com.example.metamorph.common.MorphApi;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MorphRequestMessage implements IMessage {

    private int morphId;
    private int action; // 0 = 变形, 1 = 释放技能

    public MorphRequestMessage() {
    }

    public MorphRequestMessage(int morphId) {
        this.morphId = morphId;
        this.action = 0;
    }

    public MorphRequestMessage(int morphId, int action) {
        this.morphId = morphId;
        this.action = action;
    }

    public int getMorphId() {
        return morphId;
    }

    public int getAction() {
        return action;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        morphId = buf.readInt();
        action = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(morphId);
        buf.writeInt(action);
    }

    public static class Handler implements IMessageHandler<MorphRequestMessage, IMessage> {

        @Override
        public IMessage onMessage(final MorphRequestMessage message, final MessageContext ctx) {
            if (ctx.side != Side.SERVER) {
                return null;
            }

            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;

            // 1.8.9 的正确异步调度方式：使用 MinecraftServer 的调度器
            player.mcServer.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    if (message.getAction() == 1) {
                        CommonForgeEventHandler.triggerSkill(player);
                    } else {
                        MorphApi.setMorph(player, message.getMorphId(), false);
                    }
                }
            });

            return null;
        }
    }
}
