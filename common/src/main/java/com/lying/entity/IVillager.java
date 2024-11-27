package com.lying.entity;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.VillagerDataContainer;

public interface IVillager extends VillagerDataContainer
{
	public int getExperience();
	
	public void reinitializeBrain(ServerWorld world);
	
	public boolean isNatural();
	
	public default boolean shouldRestock() { return false; }
	
	public default void restock() { }
}
