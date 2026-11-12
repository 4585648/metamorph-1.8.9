// src/main/java/com/example/metamorph/network/MorphRequestMessage.java
package com.example.metamorph.network;

import com.example.metamorph.common.MorphApi;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MorphRequestMessage implements IMessage {

    private int morphId;

    public MorphRequestMessage() {
    }

    public MorphRequestMessage(int morphId) {
        this.morphId = morphId;
    }

    public int getMorphId() {
        return morphId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        morphId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(morphId);
    }

    public static class Handler implements IMessageHandler<MorphRequestMessage, IMessage> {

        @Override
        public IMessage onMessage(final MorphRequestMessage message, final MessageContext ctx) {
            if (ctx.side != Side.SERVER) {
                return null;
            }

            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;

            player.worldObj.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    MorphApi.setMorph(player, message.getMorphId(), false);
                }
            });

            return null;
        }
    }
}