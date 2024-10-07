package com.lying.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

import com.lying.Hrrmowners;
import com.lying.init.HOEntityTypes;

public final class HrrmownersFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Hrrmowners.commonInit();
        HOEntityTypes.registerAttributeContainers((type,builder) -> FabricDefaultAttributeRegistry.register(type, builder));
    }
}
