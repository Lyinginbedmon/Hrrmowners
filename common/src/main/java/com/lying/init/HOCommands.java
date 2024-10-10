package com.lying.init;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.Optional;

import com.lying.Hrrmowners;
import com.lying.entity.village.Village;
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
				.then(literal("grow").executes(context -> 
					{
						ServerCommandSource source = context.getSource();
						Vec3d pos = source.getPosition();
						BlockPos blockPos = BlockPos.ofFloored(pos.getX(), pos.getY(), pos.getZ());
						ServerWorld world = source.getWorld();
						RegistryKey<World> dimension = world.getRegistryKey();
						
						Optional<Village> village = Hrrmowners.MANAGER.getVillage(dimension, blockPos);
						village.ifPresentOrElse(v -> 
						{
							if(v.grow(world))
								source.sendFeedback(() -> Text.literal("Added a random part to the village"), false);
							else
								source.sendFeedback(() -> Text.literal("Failed to grow the village"), false);
						}, () -> 
						{
							source.sendFeedback(() -> Text.literal("No village to grow"), false);
						});
						return 15;
					}))
				.then(literal("kill").executes(context -> 
					{
						ServerCommandSource source = context.getSource();
						Vec3d pos = source.getPosition();
						BlockPos blockPos = BlockPos.ofFloored(pos.getX(), pos.getY(), pos.getZ());
						ServerWorld world = source.getWorld();
						RegistryKey<World> dimension = world.getRegistryKey();
						
						Optional<Village> village = Hrrmowners.MANAGER.getVillage(dimension, blockPos);
						village.ifPresentOrElse(v -> 
						{
							Hrrmowners.MANAGER.kill(world, v.id());
							source.sendFeedback(() -> Text.literal("Village removed successfully"), false);
						}, () -> 
						{
							source.sendFeedback(() -> Text.literal("No village to remove"), false);
						});
						return 15;
					})
					.then(literal("all").executes(context -> 
					{
						ServerCommandSource source = context.getSource();
						ServerWorld world = source.getWorld();
						RegistryKey<World> dimension = world.getRegistryKey();
						int count = Hrrmowners.MANAGER.killAll(dimension, world);
						source.sendFeedback(() -> Text.literal("Removed "+count+" villages"), false);
						return 15;
					}))));
		});
	}
}
