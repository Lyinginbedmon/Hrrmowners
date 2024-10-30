package com.lying.block.entity;

import java.util.Optional;
import java.util.UUID;

import com.lying.block.NestBlock;
import com.lying.entity.SeatEntity;
import com.lying.init.HOBlockEntityTypes;
import com.lying.init.HOEntityTypes;
import com.lying.reference.Reference;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class NestBlockEntity extends BlockEntity
{
	private Optional<UUID> seatID = Optional.empty();
	private Entity seatEnt = null;
	
	public NestBlockEntity(BlockPos pos, BlockState state)
	{
		super(HOBlockEntityTypes.NEST.get(), pos, state);
	}
	
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		seatID.ifPresent(id -> nbt.putUuid("Seat", id));
	}
	
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		if(nbt.contains("Seat", NbtElement.INT_ARRAY_TYPE))
			seatID = Optional.of(nbt.getUuid("Seat"));
	}
	
	public static <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return type != HOBlockEntityTypes.NEST.get() ? null : NestBlock.validateTicker(type, HOBlockEntityTypes.NEST.get(), world.isClient() ? NestBlockEntity::tickClient : NestBlockEntity::tickServer);
	}
	
	public static <T extends BlockEntity> void tickClient(World world, BlockPos pos, BlockState state, NestBlockEntity nest) { }
	
	public static <T extends BlockEntity> void tickServer(World world, BlockPos pos, BlockState state, NestBlockEntity nest)
	{
		if(!nest.hasWorld() || world.getTime()%Reference.Values.TICKS_PER_SECOND > 0) return;
		
		if(nest.seatID.isEmpty())
		{
			SeatEntity seat = HOEntityTypes.SEAT.get().create(world);
			seat.setPos(pos.getX() + 1, pos.getY() + 0.75D, pos.getZ() + 1);
			seat.setAttachedBlock(nest.getPos());
			world.spawnEntity(seat);
			nest.setSeat(seat);
		}
		else
		{
			// Try to cache the seat entity
			if(nest.seatEnt == null)
				nest.seatEnt = world.getEntitiesByClass(SeatEntity.class, new Box(nest.getPos()).expand(6D), nest::isSeatEntity).stream().findFirst().orElse(null);
			
			// If the seat entity cannot be found or is no longer valid, reset
			if(nest.seatEnt == null || !isUseableSeat(nest.seatEnt))
				nest.clearSeat();
		}
	}
	
	public boolean isOccupied() { return seatEnt != null && seatEnt.hasPassengers(); }
	
	public void setSeat(Entity seatIn)
	{
		seatID = Optional.of(seatIn.getUuid());
		seatEnt = seatIn;
	}
	
	public void clearSeat()
	{
		seatID = Optional.empty();
		seatEnt = null;
	}
	
	public boolean isSeatEntity(Entity ent) { return ent != null && seatID.isPresent() && ent.getUuid().equals(seatID.get()) && isUseableSeat(ent); }
	
	public static boolean isUseableSeat(Entity ent) { return ent.getType() == HOEntityTypes.SEAT.get() && ent.isAlive() && !ent.isRemoved(); }
}
