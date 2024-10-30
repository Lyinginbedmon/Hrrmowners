package com.lying.client.renderer.entity;

import com.lying.entity.SeatEntity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class SeatEntityRenderer extends EntityRenderer<SeatEntity>
{
	public SeatEntityRenderer(EntityRendererFactory.Context ctx)
	{
		super(ctx);
	}
	
	public Identifier getTexture(SeatEntity var1) { return null; }
	
	@Override
	public void render(SeatEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) { }
}
