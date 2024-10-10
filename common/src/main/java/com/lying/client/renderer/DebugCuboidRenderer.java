package com.lying.client.renderer;

import java.util.List;

import org.joml.Vector3f;

import com.google.common.collect.Lists;
import com.lying.utility.DebugCuboid;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class DebugCuboidRenderer
{
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	private final List<DebugCuboid> renderables = Lists.newArrayList();
	
	public void tick()
	{
		if(mc.world == null || renderables.isEmpty())
			return;
		
		// Do any client-side update stuff
	}
	
	public void render(VertexConsumer consumer, Vec3d cameraPos, VertexConsumerProvider vertexConsumerProvider)
	{
		renderables.forEach(box -> renderComponent(box, consumer, cameraPos, vertexConsumerProvider));
	}
	
	public int size() { return renderables.size(); }
	
	public void add(DebugCuboid... renderable)
	{
		for(DebugCuboid render : renderable)
			renderables.add(render);
	}
	
	public void remove(DebugCuboid... renderable)
	{
		for(DebugCuboid render : renderable)
			renderables.removeIf(cube -> cube.matches(render));
	}
	
	public void clear() { renderables.clear(); }
	
	private static void renderComponent(DebugCuboid comp, VertexConsumer consumer, Vec3d cameraPos, VertexConsumerProvider vertexConsumerProvider)
	{
//		if(!comp.core().isWithinDistance(cameraPos, 32))
//			return;
		
		BlockPos min = comp.min();
		BlockPos max = comp.max();
		
		Vector3f colour = decimalToVector(comp.type().color());
		
		Vec3d minVec = new Vec3d(min.getX(), min.getY(), min.getZ()).subtract(cameraPos);
		Vec3d maxVec = new Vec3d(max.getX() + 1, max.getY() + 1, max.getZ() + 1).subtract(cameraPos);
		WorldRenderer.drawBox(consumer, minVec.x, minVec.y, minVec.z, maxVec.x, maxVec.y, maxVec.z, colour.x, colour.y, colour.z, 1F);
	}
	
	public static Vector3f decimalToVector(int color)
	{
		int r = (color & 0xFF0000) >> 16;
		int g = (color & 0xFF00) >> 8;
		int b = (color & 0XFF) >> 0;
		return new Vector3f((float)r / 255F, (float)g / 255F, (float)b / 255F);
	}
}
