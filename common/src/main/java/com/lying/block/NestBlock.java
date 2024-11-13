package com.lying.block;

import org.jetbrains.annotations.Nullable;

import com.lying.block.entity.NestBlockEntity;
import com.lying.init.HOBlockEntityTypes;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class NestBlock extends Block implements BlockEntityProvider
{
	public static final MapCodec<NestBlock> CODEC = NestBlock.createCodec(NestBlock::new);
	public static final EnumProperty<Part> PART = EnumProperty.of("part", Part.class);
	
	protected static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 8, 16);
	protected static final VoxelShape SHAPE_NW = VoxelShapes.union(SHAPE, Block.createCuboidShape(2, 8, 2, 16, 11, 16), Block.createCuboidShape(8, 11, 5, 16, 16, 8), Block.createCuboidShape(5, 11, 5, 8, 16, 16));
	protected static final VoxelShape SHAPE_NE = VoxelShapes.union(SHAPE, Block.createCuboidShape(0, 8, 2, 14, 11, 16), Block.createCuboidShape(0, 11, 5, 8, 16, 8), Block.createCuboidShape(8, 11, 5, 11, 16, 16));
	protected static final VoxelShape SHAPE_SE = VoxelShapes.union(SHAPE, Block.createCuboidShape(0, 8, 0, 14, 11, 14), Block.createCuboidShape(0, 11, 8, 8, 16, 11), Block.createCuboidShape(8, 11, 0, 11, 16, 11));
	protected static final VoxelShape SHAPE_SW = VoxelShapes.union(SHAPE, Block.createCuboidShape(2, 8, 0, 16, 11, 14), Block.createCuboidShape(8, 11, 8, 16, 16, 11), Block.createCuboidShape(5, 11, 0, 8, 16, 11));
	
	public NestBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(getDefaultState().with(PART, Part.NORTH_WEST));
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return state.get(PART) == Part.NORTH_WEST ? new NestBlockEntity(pos, state) : null; }
	
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		switch(state.get(PART))
		{
			case NORTH_EAST:
				return SHAPE_NE;
			default:
			case NORTH_WEST:
				return SHAPE_NW;
			case SOUTH_EAST:
				return SHAPE_SE;
			case SOUTH_WEST:
				return SHAPE_SW;
		}
	}
	
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
				world.setBlockState(pos.add(part.toNeighbourPos(p)), state.with(PART, p));
	}
	
	public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player)
	{
		Part part = state.get(PART);
		for(Part p : Part.values())
			if(p != part)
			{
				BlockPos neighbour = pos.add(part.toNeighbourPos(p));
				BlockState neighbourState = world.getBlockState(neighbour);
				world.setBlockState(neighbour, Blocks.AIR.getDefaultState(), 35);
				world.syncWorldEvent(player, 2001, neighbour, Block.getRawIdFromState(neighbourState));
			}
		return super.onBreak(world, pos, state, player);
	}
	
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit)
	{
		if(world.isClient()) return ActionResult.CONSUME;
		
		BlockPos tilePos = pos.add(state.get(PART).toTilePos());
		if(world.getBlockEntity(tilePos) != null && world.getBlockEntity(tilePos).getType() == HOBlockEntityTypes.NEST.get())
		{
			NestBlockEntity tile = (NestBlockEntity)world.getBlockEntity(tilePos);
			return tile.tryToSeat(player) ? ActionResult.SUCCESS : ActionResult.PASS;
		}
		
		return ActionResult.PASS;
	}
	
	protected boolean canPathfindThrough(BlockState state, NavigationType type) { return false; }
	
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
		private final Vec3i offset, toNW;
		private final int clockOrder;
		
		private Part(Direction northSouth, Direction eastWest, int order)
		{
			xAxis = eastWest;
			zAxis = northSouth;
			offset = northSouth.getVector().add(eastWest.getVector());
			
			Vec3i base = (new Vec3i(-1, 0, -1)).subtract(offset); 
			toNW = new Vec3i((int)Math.signum(base.getX()), 0, (int)Math.signum(base.getZ()));
			clockOrder = order;
		}
		
		public String asString() { return name().toLowerCase(); }
		
		public Vec3i toTilePos() { return toNW; }
		
		public Vec3i toNeighbourPos(Part part)
		{
			if(part == Part.NORTH_WEST) return toNW;
			Vec3i dir = part.offset.subtract(this.offset);
			return new Vec3i((int)Math.signum(dir.getX()), 0, (int)Math.signum(dir.getZ()));
		}
		
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
