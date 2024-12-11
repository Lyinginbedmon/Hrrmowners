package com.lying.utility;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.lying.entity.village.VillagePart;
import com.lying.init.HOVillageParts;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.pool.FeaturePoolElement;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public record DebugCuboid(
		BlockPos min, 
		BlockPos max,
		VillagePart type,
		String name)
{
	protected static final Codec<DebugCuboid> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockPos.CODEC.fieldOf("Min").forGetter(v -> v.min()),
			BlockPos.CODEC.fieldOf("Max").forGetter(v -> v.max()),
			VillagePart.CODEC.fieldOf("Type").forGetter(v -> v.type()),
			Codec.STRING.fieldOf("Name").forGetter(v -> v.name()))
				.apply(instance, DebugCuboid::new));
	
	public NbtElement toNbt() { return CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow(); }
	
	public static DebugCuboid fromNbt(NbtCompound nbt) { return CODEC.parse(NbtOps.INSTANCE, nbt).getOrThrow(); }
	
	public boolean matches(DebugCuboid cuboid)
	{
		return Objects.equals(toNbt(), cuboid.toNbt());  
	}
	
	public BlockPos core()
	{
		BlockPos offset = max.subtract(min);
		return min.add(offset.getX() / 2, offset.getY() / 2, offset.getZ() / 2);
	}
	
	@Nullable
	public static DebugCuboid fromStructurePiece(StructurePiece comp)
	{
		String name = comp.toString();
		if(comp instanceof PoolStructurePiece)
		{
			PoolStructurePiece pool = (PoolStructurePiece)comp;
			StructurePoolElement element = pool.getPoolElement();
			if(element instanceof FeaturePoolElement)
				return null;
			name = pool.getPoolElement().toString();
		}
		
		VillagePart type = HOVillageParts.fromPartName(name);
		if(type == null)
			return null;
		
		BlockBox bounds = comp.getBoundingBox();
		BlockPos min = new BlockPos(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ());
		BlockPos max = new BlockPos(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ());
		
		return new DebugCuboid(min, max, type, name);
	}
}
