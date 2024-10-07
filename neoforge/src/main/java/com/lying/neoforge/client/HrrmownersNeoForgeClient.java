package com.lying.neoforge.client;

import com.lying.client.HrrmownersClient;
import com.lying.client.init.HOModelLayerParts;
import com.lying.client.renderer.entity.SurinaEntityRenderer;
import com.lying.init.HOEntityTypes;
import com.lying.reference.Reference;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class HrrmownersNeoForgeClient
{
	@SubscribeEvent
	public static void setupClient(final FMLClientSetupEvent event)
	{
		HrrmownersClient.clientInit();
	}
	
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerEntityRenderer(HOEntityTypes.SURINA.get(), SurinaEntityRenderer::new);
	}
	
	public static void registerModelParts(EntityRenderersEvent.RegisterLayerDefinitions event)
	{
		HOModelLayerParts.init((layer, func) -> event.registerLayerDefinition(layer, func));
	}
}
