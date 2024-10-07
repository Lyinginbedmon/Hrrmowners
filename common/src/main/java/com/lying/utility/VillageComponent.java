package com.lying.utility;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.pool.FeaturePoolElement;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public record VillageComponent(
		BlockPos min, 
		BlockPos max,
		Type type,
		String name)
{
	protected static final Codec<VillageComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockPos.CODEC.fieldOf("Min").forGetter(v -> v.min()),
			BlockPos.CODEC.fieldOf("Max").forGetter(v -> v.max()),
			Type.CODEC.fieldOf("Type").forGetter(v -> v.type()),
			Codec.STRING.fieldOf("Name").forGetter(v -> v.name()))
				.apply(instance, VillageComponent::new));
	
	public NbtElement toNbt() { return CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow(); }
	
	public static VillageComponent fromNbt(NbtCompound nbt) { return CODEC.parse(NbtOps.INSTANCE, nbt).getOrThrow(); }
	
	@Nullable
	public static VillageComponent fromStructurePiece(StructurePiece comp)
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
		
		Type type = Type.fromPartName(name);
		if(type == null)
			return null;
		
		BlockBox bounds = comp.getBoundingBox();
		BlockPos min = new BlockPos(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ());
		BlockPos max = new BlockPos(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ());
		
		return new VillageComponent(min, max, type, name);
	}
	
	public static enum Type implements StringIdentifiable
	{
		HOUSE(0x00FF00),
		WORK(0xFF0000),
		STREET(0xFFFFFF);
		
		@SuppressWarnings("deprecation")
		public static final StringIdentifiable.EnumCodec<Type> CODEC = StringIdentifiable.createCodec(Type::values);
		private static final List<String> WORKSTATION_NAMES = List.of(
				"smith", 
				"cartographer", 
				"farm",
				"armorer",
				"butcher",
				"fisher",
				"library",
				"mason",
				"shepherd",
				"tanner");
		
		private final int colour;
		
		private Type(int col)
		{
			colour = col;
		}
		
		public int color() { return colour; }
		
		public String asString() { return name().toLowerCase(); }
		
		@Nullable
		public Type fromString(String name)
		{
			for(Type type : values())
				if(type.asString().equalsIgnoreCase(name))
					return type;
			return null;
		}
		
		@Nullable
		public static Type fromPartName(final String name)
		{
			if(name.contains("accessory"))
				return null;
			
			if(name.contains("street"))
				return Type.STREET;
			else if(WORKSTATION_NAMES.stream().anyMatch(s -> name.contains(s)))
				return Type.WORK;
			else if(name.contains("house"))
				return Type.HOUSE;
			
			return null;
		}
	}
}
