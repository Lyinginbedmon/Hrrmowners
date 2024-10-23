package com.lying.utility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.entity.village.PartType;
import com.lying.network.ShowCubesPacket;
import com.lying.reference.Reference;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.world.gen.structure.StructureType;

public class ServerBus
{
	private static final Logger LOGGER = Hrrmowners.LOGGER;
	
	public static void registerEventHandlers()
	{
		LOGGER.info(" # Registered server event handlers");
		PlayerEvent.PLAYER_JOIN.register(player -> 
		{
			Hrrmowners.PLAYERS.add(player);
			
			List<DebugCuboid> comps = Lists.newArrayList();
			player.getServerWorld().getStructureAccessor()
				.getStructureStarts(player.getChunkPos(), s -> s.getType() == StructureType.JIGSAW)
					.forEach(start -> start.getChildren()
						.forEach(piece -> comps.add(DebugCuboid.fromStructurePiece(piece))));
			comps.removeIf(a -> a == null);
			
			LOGGER.info("Found {} structure pieces near {}", comps.size(), player.getChunkPos().toString());
			Map<PartType, Integer> tallies = new HashMap<>();
			comps.forEach(comp -> tallies.put(comp.type(), tallies.getOrDefault(comp.type(), 0) + 1));
			tallies.entrySet().forEach(entry -> LOGGER.info(" # {} - {}", entry.getKey().registryName().getPath(), entry.getValue()));
			ShowCubesPacket.send(player, comps);
		});
		PlayerEvent.PLAYER_QUIT.register(player -> Hrrmowners.PLAYERS.remove(player));
		
		TickEvent.SERVER_LEVEL_POST.register((world) -> 
		{
			if(world.getTime()%Reference.Values.VILLAGE_TICK_RATE == 0)
				Hrrmowners.MANAGER.tickVillages(world.getRegistryKey(), world);
		});
	}
}
