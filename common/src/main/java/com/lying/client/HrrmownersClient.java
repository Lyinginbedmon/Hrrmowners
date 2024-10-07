package com.lying.client;

import com.lying.client.renderer.VillageRenderer;
import com.lying.client.utility.ClientBus;
import com.lying.network.ShowCubesPacket;

import dev.architectury.networking.NetworkManager;

public class HrrmownersClient
{
	public static final VillageRenderer VILLAGE_RENDERER = new VillageRenderer();
	
	public static void clientInit()
	{
		ClientBus.registerEventHandlers();
		registerPacketReceiptOps();
	}
	
	private static void registerPacketReceiptOps()
	{
		NetworkManager.registerReceiver(NetworkManager.s2c(), ShowCubesPacket.PACKET_TYPE, ShowCubesPacket.PACKET_CODEC, (value, context) -> 
    	{
    		value.highlights().forEach(box -> VILLAGE_RENDERER.add(box));
    	});
	}
}
