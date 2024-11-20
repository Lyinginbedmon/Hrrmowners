package com.lying.block.entity;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicate;
import com.lying.block.NestBlock;
import com.lying.entity.SeatEntity;
import com.lying.entity.SurinaEntity;
import com.lying.init.HOBlockEntityTypes;
import com.lying.init.HOEntityTypes;
import com.lying.init.HOVillagerProfessions;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class NestBlockEntity extends BlockEntity
{
	private static final Predicate<Entity> IS_AUTHORITY_FIGURE = e -> 
	{
		if(e.getType() == EntityType.PLAYER)
			return ((PlayerEntity)e).isCreative();
		
		if(e.getType() == HOEntityTypes.SURINA.get())
			return ((SurinaEntity)e).getVillagerData().getProfession() == HOVillagerProfessions.QUEEN.get();
		return false;
	};
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
		nest.tryFindSeat();
		if(nest.seatID.isPresent() && (nest.seatEnt == null || !isUseableSeat(nest.seatEnt)))
			nest.clearSeat();
	}
	
	private SeatEntity createNewSeat()
	{
		BlockPos pos = getPos();
		SeatEntity seat = HOEntityTypes.SEAT.get().create(getWorld());
		seat.setPos(pos.getX() + 1, pos.getY() + 0.75D, pos.getZ() + 1);
		seat.setAttachedBlock(getPos());
		setSeat(seat);
		return seat;
	}
	
	public void setSeat(@Nullable Entity seatIn)
	{
		seatID = seatIn == null ? Optional.empty() : Optional.of(seatIn.getUuid());
		seatEnt = seatIn;
	}
	
	public void clearSeat() { setSeat(null); }
	
	/** Returns true if this nest has someone sitting in it */
	public boolean isOccupied()
	{
		tryFindSeat();
		return seatEnt != null && seatEnt.hasPassengers() && seatEnt.getPassengerList().stream().anyMatch(IS_AUTHORITY_FIGURE);
	}
	
	public boolean tryToSeat(Entity sitter)
	{
		if(isOccupied() || seatID.isPresent())
			return false;
		
		if(!getWorld().isClient())
		{
			SeatEntity seat = createNewSeat();
			getWorld().spawnEntity(seat);
			sitter.startRiding(seat);
		}
		
		return true;
	}
	
	public void tryFindSeat()
	{
		if(seatEnt != null || seatID.isEmpty())
			return;
		seatEnt = getWorld().getEntitiesByClass(Entity.class, (new Box(getPos())).expand(6D), this::isSeatEntity).stream().findFirst().orElse(null);
	}
	
	public boolean isSeatEntity(Entity ent)
	{
		return ent != null && isUseableSeat(ent) && ent.getUuid().equals(seatID.orElse(null));
	}
	
	public static boolean isUseableSeat(Entity ent)
	{
		return 
				ent != null && 
				ent.getType() == HOEntityTypes.SEAT.get() && 
				ent.isAlive();
	}
}
