package com.lying.block;

import org.jetbrains.annotations.Nullable;

import com.lying.block.entity.NestBlockEntity;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class NestBlock extends Block implements BlockEntityProvider
{
	public static final MapCodec<NestBlock> CODEC = NestBlock.createCodec(NestBlock::new);
	public static final EnumProperty<Part> PART = EnumProperty.of("part", Part.class);
	
	public NestBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(getDefaultState().with(PART, Part.NORTH_WEST));
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return state.get(PART) == Part.NORTH_WEST ? new NestBlockEntity(pos, state) : null; }
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> validateTicker(BlockEntityType<A> given, BlockEntityType<E> expected, BlockEntityTicker<? super E> ticker)
	{
		return expected == given ? (BlockEntityTicker<A>)ticker : null;
	}
	
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return NestBlockEntity.getTicker(world, state, type);
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(PART);
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		BlockPos pos = ctx.getBlockPos();
		World level = ctx.getWorld();
		Part part = getDefaultState().get(PART);
		for(Part p : Part.values())
		{
			Vec3i offset = p == part ? Vec3i.ZERO : p.offset.subtract(part.offset);
			BlockPos neighbour = pos.add((int)Math.signum(offset.getX()), 0, (int)Math.signum(offset.getZ()));
			if(!level.getBlockState(neighbour).canReplace(ctx))
				return null;
		}
		return getDefaultState();
	}
	
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack)
	{
		Part part = state.get(PART);
		for(Part p : Part.values())
			if(p != part)
			{
				Vec3i offset = p.offset.subtract(part.offset);
				world.setBlockState(pos.add((int)Math.signum(offset.getX()), 0, (int)Math.signum(offset.getZ())), state.with(PART, p));
			}
	}
	
	public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player)
	{
		Part part = state.get(PART);
		for(Part p : Part.values())
			if(p != part)
			{
				Vec3i offset = p.offset.subtract(part.offset);
				BlockPos neighbour = pos.add((int)Math.signum(offset.getX()), 0, (int)Math.signum(offset.getZ()));
				BlockState neighbourState = world.getBlockState(neighbour);
				world.setBlockState(neighbour, Blocks.AIR.getDefaultState(), 35);
				world.syncWorldEvent(player, 2001, neighbour, Block.getRawIdFromState(neighbourState));
			}
		return super.onBreak(world, pos, state, player);
	}
	
	protected BlockState rotate(BlockState state, BlockRotation rotate)
	{
		Part start = state.get(PART);
		int turns = 0;
		switch(rotate)
		{
			case CLOCKWISE_90:
				turns = 1;
				break;
			case CLOCKWISE_180:
				turns = 2;
				break;
			case COUNTERCLOCKWISE_90:
				turns = 3;
				break;
			default:
				turns = 0;
		}
		
		return state.with(PART, Part.byClockOrder(start.clockOrder + turns));
	}
	
	protected BlockState mirror(BlockState state, BlockMirror mirror)
	{
		Part start = state.get(PART);
		Axis mirrorAxis = null;
		switch(mirror)
		{
			case FRONT_BACK:
				mirrorAxis = Axis.X;
				break;
			case LEFT_RIGHT:
				mirrorAxis = Axis.Z;
				break;
			case NONE:
			default:
				return state;
		}
		
		return state.with(PART, start.getOpposite(mirrorAxis));
	}
	
	private static enum Part implements StringIdentifiable
	{
		NORTH_EAST(Direction.NORTH, Direction.EAST, 0),
		NORTH_WEST(Direction.NORTH, Direction.WEST, 3),
		SOUTH_EAST(Direction.SOUTH, Direction.EAST, 1),
		SOUTH_WEST(Direction.SOUTH, Direction.WEST, 2);
		
		private static final Part[] CLOCK_ORDER = new Part[] {NORTH_EAST, SOUTH_EAST, SOUTH_WEST, NORTH_WEST};
		private final Direction xAxis, zAxis;
		private final Vec3i offset;
		private final int clockOrder;
		
		private Part(Direction northSouth, Direction eastWest, int order)
		{
			xAxis = eastWest;
			zAxis = northSouth;
			offset = northSouth.getVector().add(eastWest.getVector());
			clockOrder = order;
		}
		
		public String asString() { return name().toLowerCase(); }
		
		public static Part byClockOrder(int index)
		{
			return CLOCK_ORDER[index % CLOCK_ORDER.length];
		}
		
		public Direction getAxisPolarity(Axis axis) { return axis == Axis.X ? xAxis : axis == Axis.Z ? zAxis : Direction.UP; }
		
		public Part getOpposite(Axis axis)
		{
			Direction targetX = axis == Axis.X ? xAxis.getOpposite() : xAxis;
			Direction targetZ = axis == Axis.Z ? zAxis.getOpposite() : zAxis;
			for(Part part : values())
				if(part.getAxisPolarity(Axis.X) == targetX && part.getAxisPolarity(Axis.Z) == targetZ)
					return part;
			return null;
		}
	}
}
