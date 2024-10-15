package com.lying.entity.village.ai;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Connector
{
	public final StructureBlockInfo info;
	
	public final BlockPos pos;
	public final Direction facing;
	
	public final String name;
	
	public Connector(StructureBlockInfo infoIn)
	{
		info = infoIn;
		pos = info.pos();
		facing = JigsawBlock.getFacing(info.state());
		name = info.nbt().getString(JigsawBlockEntity.NAME_KEY);
	}
	
	public BlockPos linkPos() { return pos.offset(facing); }
	
	public boolean linksTo(Connector b)
	{
		return b.facing == facing.getOpposite();
	}
	
	public boolean equals(Connector b)
	{
		return 
				b.pos.isWithinDistance(pos, 0.5D) &&
				b.facing == facing &&
				NbtHelper.matches(b.info.nbt(), info.nbt(), true);
	}
	
	public NbtCompound toNbt()
	{
		NbtCompound data = new NbtCompound();
		data.put("Pos", NbtHelper.fromBlockPos(info.pos()));
		data.put("State", NbtHelper.fromBlockState(info.state()));
		if(!info.nbt().isEmpty())
			data.put("NBT", info.nbt());
		return data;
	}
	
	@Nullable
	public static Connector fromNbt(NbtCompound data)
	{
		Optional<BlockPos> pos = NbtHelper.toBlockPos(data, "Pos");
		if(pos.isEmpty())
			return null;
		BlockState state = NbtHelper.toBlockState(Registries.BLOCK.getReadOnlyWrapper(), data.getCompound("State"));
		if(state.isAir())
			return null;
		NbtCompound stateData = data.contains("NBT", NbtElement.COMPOUND_TYPE) ? data.getCompound("NBT") : null;
		return new Connector(new StructureBlockInfo(pos.get(), state, stateData));
	}
}
