// src/main/java/com/example/metamorph/network/MorphSyncMessage.java
package com.example.metamorph.network;

import com.example.metamorph.common.MorphManager;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class MorphSyncMessage implements IMessage {

    private UUID uuid;
    private int morphId;

    public MorphSyncMessage() {
    }

    public MorphSyncMessage(UUID uuid, int morphId) {
        this.uuid = uuid;
        this.morphId = morphId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getMorphId() {
        return morphId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        uuid = new UUID(buf.readLong(), buf.readLong());
        morphId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
        buf.writeInt(morphId);
    }

    public static class Handler implements IMessageHandler<MorphSyncMessage, IMessage> {

        @Override
        public IMessage onMessage(MorphSyncMessage message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                MorphManager.setClient(message.getUuid(), message.getMorphId());
            }

            return null;
        }
    }
}