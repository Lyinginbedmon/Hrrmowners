package com.lying.client;

import com.lying.Hrrmowners;
import com.lying.client.renderer.DebugCuboidRenderer;
import com.lying.client.utility.ClientBus;
import com.lying.network.HideCubesPacket;
import com.lying.network.ShowCubesPacket;

import dev.architectury.networking.NetworkManager;

public class HrrmownersClient
{
	public static final DebugCuboidRenderer VILLAGE_RENDERER = new DebugCuboidRenderer();
	
	public static void clientInit()
	{
		ClientBus.registerEventHandlers();
		registerPacketReceiptOps();
	}
	
	private static void registerPacketReceiptOps()
	{
		Hrrmowners.LOGGER.info(" # Initialised client packet receipt operations");
		NetworkManager.registerReceiver(NetworkManager.s2c(), ShowCubesPacket.PACKET_TYPE, ShowCubesPacket.PACKET_CODEC, (value, context) -> value.highlights().forEach(box -> VILLAGE_RENDERER.add(box)));
		NetworkManager.registerReceiver(NetworkManager.s2c(), HideCubesPacket.PACKET_TYPE, HideCubesPacket.PACKET_CODEC, (value, context) -> value.highlights().forEach(box -> VILLAGE_RENDERER.remove(box)));
	}
}
