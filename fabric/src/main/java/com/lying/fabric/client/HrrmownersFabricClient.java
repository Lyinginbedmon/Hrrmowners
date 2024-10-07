package com.lying.fabric.client;

import com.lying.client.HrrmownersClient;
import com.lying.client.init.HOModelLayerParts;
import com.lying.client.renderer.entity.SurinaEntityRenderer;
import com.lying.init.HOEntityTypes;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class HrrmownersFabricClient implements ClientModInitializer
{
    public void onInitializeClient()
    {
    	HrrmownersClient.clientInit();
    	
    	HOModelLayerParts.init((layer, func) -> EntityModelLayerRegistry.register(layer, func));
    	
    	EntityRendererRegistry.register(HOEntityTypes.SURINA.get(), SurinaEntityRenderer::new);
    }
}
