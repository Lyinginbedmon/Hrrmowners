package com.lying.data;

import com.lying.reference.Reference;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;

public class HOTags
{
	public static final TagKey<PointOfInterestType> SURINA_JOB_SITES	= TagKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, Identifier.of(Reference.ModInfo.MOD_ID, "surina_job_sites"));
}
