package com.sarahk.togglyfiers.network;

import com.sarahk.togglyfiers.proxy.ClientProxy;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.util.EnumMap;

public final class TogglyfierNetworkHandler extends FMLIndexedMessageToMessageCodec<TogglyfierPacket>
{
	public static EnumMap<Side, FMLEmbeddedChannel> channels;
	public static TogglyfierNetworkHandler instance = new TogglyfierNetworkHandler();
	
	public TogglyfierNetworkHandler()
	{
		addDiscriminator(0, TogglyfierPacket.TogglyfierInventoryChangedPacket.class);
		addDiscriminator(1, TogglyfierPacket.TogglyfierSwitchModePacket.class);
		addDiscriminator(2, TogglyfierPacket.TogglyfierGuiClosed.class);
	}
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, TogglyfierPacket msg, ByteBuf target) throws Exception
	{
		target.writeBytes(msg.data);
	}
	
	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, TogglyfierPacket msg)
	{
		msg.consumePacket(source);
	}
	
	public static void sendToServer(TogglyfierPacket packet)
	{
		channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
		channels.get(Side.CLIENT).writeOutbound(packet);
	}
	
	public static void sendToPlayer(TogglyfierPacket packet, EntityPlayer player)
	{
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
		channels.get(Side.SERVER).writeOutbound(packet);
	}
	
	public static void sendToAllPlayers(TogglyfierPacket packet)
	{
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
		channels.get(Side.SERVER).writeOutbound(packet);
	}
	
	public static void setupChannel()
	{
		if(channels == null)
		{
			channels = NetworkRegistry.INSTANCE.newChannel("Togglyfiers", TogglyfierNetworkHandler.instance);
			String clientTargetName = channels.get(Side.CLIENT).findChannelHandlerNameForType(TogglyfierNetworkHandler.class);
			String serverTargetName = channels.get(Side.SERVER).findChannelHandlerNameForType(TogglyfierNetworkHandler.class);
			channels.get(Side.CLIENT).pipeline().addAfter(clientTargetName, "TogglyfierPacketHandler", new TogglyfierPacketHandler(Side.CLIENT));
			channels.get(Side.SERVER).pipeline().addAfter(serverTargetName, "TogglyfierPacketHandler", new TogglyfierPacketHandler(Side.SERVER));
		}
	}
	
	private static class TogglyfierPacketHandler extends SimpleChannelInboundHandler<TogglyfierPacket>
	{
		private final Side side;
		private TogglyfierPacketHandler(Side side)
		{
			this.side = side;
		}
		
		@Override
		protected void channelRead0(ChannelHandlerContext ctx, TogglyfierPacket msg) throws Exception
		{
			switch (side)
			{
			case CLIENT:
				if(msg instanceof TogglyfierPacket.PlayerNotificationPacket)
				{
					msg.execute(null);
				}
				else
				{
					ClientProxy.addScheduledTask(() -> msg.execute(ClientProxy.getClientPlayer()));
				}
				break;
			case SERVER:
				INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
				EntityPlayerMP player = ((NetHandlerPlayServer) netHandler).player;
				player.getServerWorld().addScheduledTask(() -> msg.execute(player));
				break;
			}
		}
	}
}
