package com.lying.client.utility;

import org.joml.Matrix4f;

import com.lying.Hrrmowners;
import com.lying.client.HrrmownersClient;
import com.lying.client.event.RenderEvents;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;

public class ClientBus
{
	public static MinecraftClient mc = MinecraftClient.getInstance();
	
	public static void registerEventHandlers()
	{
		Hrrmowners.LOGGER.info(" # Registered client event handlers");
		ClientTickEvent.CLIENT_POST.register(mc -> 
		{
			HrrmownersClient.VILLAGE_RENDERER.tick();
		});
		
		RenderEvents.BEFORE_WORLD_RENDER_EVENT.register((RenderTickCounter tickCounter, Camera camera, GameRenderer renderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f1, Matrix4f matrix4f2, VertexConsumerProvider vertexConsumerProvider) -> 
		{
			HrrmownersClient.VILLAGE_RENDERER.render(vertexConsumerProvider.getBuffer(RenderLayer.LINES), camera.getPos(), vertexConsumerProvider);
		});
		
		PlayerEvent.CHANGE_DIMENSION.register((player, w1, w2) -> 
		{
			if(player.getUuid() == mc.player.getUuid())
				HrrmownersClient.VILLAGE_RENDERER.clear();
		});
		
		PlayerEvent.PLAYER_QUIT.register(player -> 
		{
			if(player.getUuid().equals(mc.player.getUuid()))
				HrrmownersClient.VILLAGE_RENDERER.clear();
		});
	}
}
