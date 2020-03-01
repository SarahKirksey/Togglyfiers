package com.sarahk.togglyfiers.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;

import javax.annotation.Nonnull;

//TODO: Everything
public class Packet250CustomPayload extends SPacketCustomPayload
{
	public Packet250CustomPayload(@Nonnull final String togglyfiers, @Nonnull final byte[] toByteArray)
	{
		super(togglyfiers, new PacketBuffer(Unpooled.buffer().writeBytes(toByteArray)));
	}
}
