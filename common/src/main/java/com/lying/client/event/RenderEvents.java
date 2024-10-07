package com.lying.client.event;

import org.joml.Matrix4f;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;

public class RenderEvents
{
	public static final Event<WorldRenderEvent> BEFORE_WORLD_RENDER_EVENT = EventFactory.createLoop(WorldRenderEvent.class);
	public static final Event<WorldRenderEvent> AFTER_WORLD_RENDER_EVENT = EventFactory.createLoop(WorldRenderEvent.class);
	
	public interface WorldRenderEvent
	{
		void onRender(RenderTickCounter tickCounter, Camera camera, GameRenderer renderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f1, Matrix4f matrix4f2, VertexConsumerProvider vertexConsumerProvider);
	}
}
