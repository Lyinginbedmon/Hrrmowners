package com.lying.entity;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.lying.block.entity.NestBlockEntity;
import com.lying.init.HOBlockEntityTypes;
import com.lying.init.HOBlocks;
import com.lying.reference.Reference;

import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.util.Arm;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SeatEntity extends Entity
{
	private static final TrackedData<Optional<BlockPos>> BLOCK = DataTracker.registerData(SeatEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
	
	public SeatEntity(EntityType<?> type, World world)
	{
		super(type, world);
	}
	
	protected void initDataTracker(Builder builder)
	{
		builder.add(BLOCK, Optional.empty());
	}
	
	protected void readCustomDataFromNbt(NbtCompound nbt)
	{
		if(nbt.contains("Block", NbtElement.INT_ARRAY_TYPE))
			NbtHelper.toBlockPos(nbt, "Block").ifPresent(p -> setAttachedBlock(p));
	}
	
	protected void writeCustomDataToNbt(NbtCompound nbt)
	{
		getDataTracker().get(BLOCK).ifPresent(p -> nbt.put("Block", NbtHelper.fromBlockPos(p)));
	}
	
	public void setAttachedBlock(BlockPos pos)
	{
		getDataTracker().set(BLOCK, Optional.of(pos));
	}
	
	public Optional<BlockPos> attachedBlock() { return getDataTracker().get(BLOCK); }
	
	public Optional<NestBlockEntity> attachedTile()
	{
		if(!isAttached())
			return Optional.empty();
		
		World world = getEntityWorld();
		BlockPos tilePos = attachedBlock().get();
		if(!world.getBlockState(tilePos).isOf(HOBlocks.NEST.get()))
			return Optional.empty();
		if(world.getBlockEntity(tilePos) == null || world.getBlockEntity(tilePos).getType() != HOBlockEntityTypes.NEST.get())
			return Optional.empty();
		
		return Optional.of((NestBlockEntity)world.getBlockEntity(tilePos));
	}
	
	public boolean isAttached() { return attachedBlock().isPresent(); }
	
	public void tick()
	{
		super.tick();
		if(!getEntityWorld().isClient())
		{
			if(this.age%Reference.Values.TICKS_PER_SECOND == 0 && !isAttachedBlock(attachedBlock().get(), getEntityWorld()))
				remove();
			else if(!hasPassengers())
				remove();
		}
	}
	
	public boolean isAttachedBlock(BlockPos pos, World world)
	{
		Optional<NestBlockEntity> tile = attachedTile();
		return tile.isPresent() && ((NestBlockEntity)tile.get()).isSeatEntity(this);
	}
	
	public void remove()
	{
		if(!getEntityWorld().isClient())
			attachedTile().ifPresent(nest -> nest.clearSeat());
		discard();
	}
	
	public Vec3d updatePassengerForDismount(LivingEntity passenger)
	{
		Vec3d dismountRight = AbstractHorseEntity.getPassengerDismountOffset(this.getWidth(), passenger.getWidth(), this.getYaw() + (passenger.getMainArm() == Arm.RIGHT ? 90.0f : -90.0f));
		Vec3d dismountPos = this.locateSafeDismountingPos(dismountRight, passenger);
		if(dismountPos != null)
			return dismountPos;
		
		Vec3d dismountLeft = AbstractHorseEntity.getPassengerDismountOffset(this.getWidth(), passenger.getWidth(), this.getYaw() + (passenger.getMainArm() == Arm.LEFT ? 90.0f : -90.0f));
		dismountPos = this.locateSafeDismountingPos(dismountLeft, passenger);
		if(dismountPos != null)
			return dismountPos;
		
		return this.getPos();
	}
	
	@Nullable
	private Vec3d locateSafeDismountingPos(Vec3d offset, LivingEntity passenger)
	{
		double d = this.getX() + offset.x;
		double e = this.getBoundingBox().minY;
		double f = this.getZ() + offset.z;
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		block0: for(EntityPose entityPose : passenger.getPoses())
		{
			mutable.set(d, e, f);
			double g = getBoundingBox().maxY + 0.75;
			do
			{
				double h = getWorld().getDismountHeight(mutable);
				if((double)mutable.getY() + h > g)
					continue block0;
				
				if(Dismounting.canDismountInBlock(h))
				{
					Box box = passenger.getBoundingBox(entityPose);
					Vec3d vec3d = new Vec3d(d, (double)mutable.getY() + h, f);
					if(Dismounting.canPlaceEntityAt(this.getWorld(), passenger, box.offset(vec3d)))
					{
						passenger.setPose(entityPose);
						return vec3d;
					}
				}
				mutable.move(Direction.UP);
			}
			while ((double)mutable.getY() < g);
		}
		return null;
	}
	
	protected Vec3d getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) { return Vec3d.ZERO; }
	
	public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry)
	{
		return new EntitySpawnS2CPacket(this, entityTrackerEntry);
	}
}
