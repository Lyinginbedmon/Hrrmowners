package com.lying.init;

import static net.minecraft.server.command.CommandManager.literal;

import com.lying.Hrrmowners;
import com.lying.reference.Reference;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HOCommands
{
	public static void init()
	{
		CommandRegistrationEvent.EVENT.register((dispatcher, access, environment) -> 
		{
			dispatcher.register(literal(Reference.ModInfo.MOD_ID).requires(source -> source.hasPermissionLevel(2))
				.then(literal("create").executes(context -> 
				{
					ServerCommandSource source = context.getSource();
					Vec3d pos = source.getPosition();
					BlockPos blockPos = BlockPos.ofFloored(pos.getX(), pos.getY(), pos.getZ());
					ServerWorld world = source.getWorld();
					RegistryKey<World> dimension = world.getRegistryKey();
					
					if(Hrrmowners.MANAGER.createVillage(blockPos, dimension, world))
						source.sendFeedback(() -> Text.literal("Created new village at "+blockPos.toShortString()), false);
					else
						source.sendFeedback(() -> Text.literal("Failed to create village at "+blockPos.toShortString()), false);
					return 15;
				}))
				.then(literal("kill").executes(context -> 
				{
					Hrrmowners.MANAGER.killAll(context.getSource().getWorld());
					return 15;
				})));
		});
	}
}
