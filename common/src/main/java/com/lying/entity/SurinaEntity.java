package com.lying.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;

public class SurinaEntity extends MerchantEntity
{
	public SurinaEntity(EntityType<? extends MerchantEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	protected void initGoals()
	{
		this.goalSelector.add(7, (Goal)new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
		this.goalSelector.add(8, (Goal)new LookAroundGoal(this));
	}
	
	public static DefaultAttributeContainer.Builder createSurinaAttributes()
	{
		return MobEntity.createMobAttributes();
	}
	
	protected void afterUsing(TradeOffer var1)
	{
		// TODO Auto-generated method stub
		
	}
	
	protected void fillRecipes()
	{
		// TODO Auto-generated method stub
		
	}
	
	public PassiveEntity createChild(ServerWorld var1, PassiveEntity var2) { return null; }
	
	protected SoundEvent getAmbientSound()
	{
		if(isSleeping())
			return null;
		
		return hasCustomer() ? SoundEvents.ENTITY_VILLAGER_TRADE : SoundEvents.ENTITY_VILLAGER_AMBIENT;
	}
	
	protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENTITY_VILLAGER_HURT; }
	
	protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_VILLAGER_DEATH; }
}
