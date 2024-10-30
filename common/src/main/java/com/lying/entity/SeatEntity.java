package com.lying.entity;

import java.util.Optional;

import com.lying.block.entity.NestBlockEntity;
import com.lying.init.HOBlockEntityTypes;
import com.lying.init.HOBlocks;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SeatEntity extends Entity
{
	private Optional<BlockPos> attachedBlock = Optional.empty();
	private boolean attached = false;
	
	public SeatEntity(EntityType<?> type, World world)
	{
		super(type, world);
	}
	
	protected void initDataTracker(Builder var1) { }
	
	protected void readCustomDataFromNbt(NbtCompound nbt)
	{
		if(nbt.contains("Block", NbtElement.INT_ARRAY_TYPE))
			NbtHelper.toBlockPos(nbt, "Block").ifPresent(p -> setAttachedBlock(p));
	}
	
	protected void writeCustomDataToNbt(NbtCompound nbt)
	{
		attachedBlock.ifPresent(p -> nbt.put("Block", NbtHelper.fromBlockPos(p)));
	}
	
	public void setAttachedBlock(BlockPos pos)
	{
		attachedBlock = Optional.of(pos);
		attached = true;
	}
	
	public void tick()
	{
		super.tick();
		if(attached)
			if(attachedBlock.isEmpty() || !isAttachedBlock(attachedBlock.get(), getEntityWorld()))
				discard();
	}
	
	public boolean isAttachedBlock(BlockPos pos, World world)
	{
		BlockEntity tile = world.getBlockEntity(pos);
		return world.getBlockState(pos).isOf(HOBlocks.NEST.get()) && tile != null && tile.getType() == HOBlockEntityTypes.NEST.get() && ((NestBlockEntity)tile).isSeatEntity(this);
	}
	
	public boolean canHit() { return isAlive() && !isRemoved(); }
	
	public ActionResult interact(PlayerEntity player, Hand hand)
	{
		ActionResult ret = super.interact(player, hand);
		if(ret.isAccepted())
			return ret;
		
		if(player.shouldCancelInteraction() || hasPassengers())
			return ActionResult.PASS;
		if(!getWorld().isClient())
			return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
		return ActionResult.SUCCESS;
	}
	
	protected Vec3d getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) { return Vec3d.ZERO; }
	
	// FIXME Ensure seat is usable when spawned without needing to reboot
	public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry)
	{
		return new EntitySpawnS2CPacket(this, entityTrackerEntry);
	}
}
