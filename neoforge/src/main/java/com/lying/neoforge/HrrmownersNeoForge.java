package com.lying.neoforge;

import com.lying.Hrrmowners;
import com.lying.init.HOEntityTypes;
import com.lying.neoforge.data.HODataGeneratorsForge;
import com.lying.reference.Reference;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(Reference.ModInfo.MOD_ID)
public final class HrrmownersNeoForge
{
    public HrrmownersNeoForge(final IEventBus eventBus)
    {
        Hrrmowners.commonInit();
        eventBus.addListener(HrrmownersNeoForge::registerEntityAttributes);
        eventBus.addListener(HODataGeneratorsForge::onGatherDataEvent);
    }
    
    public static void registerEntityAttributes(EntityAttributeCreationEvent event)
    {
    	HOEntityTypes.registerAttributeContainers((type,builder) -> event.put(type, builder.build()));
    }
}
