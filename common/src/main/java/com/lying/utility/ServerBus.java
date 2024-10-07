package com.lying.utility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.network.ShowCubesPacket;
import com.lying.utility.VillageComponent.Type;

import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.world.gen.structure.StructureType;

public class ServerBus
{
	public static void registerEventHandlers()
	{
		PlayerEvent.PLAYER_JOIN.register(player -> 
		{
			Hrrmowners.PLAYERS.add(player);
			
			List<VillageComponent> comps = Lists.newArrayList();
			player.getServerWorld().getStructureAccessor()
				.getStructureStarts(player.getChunkPos(), s -> s.getType() == StructureType.JIGSAW)
					.forEach(start -> start.getChildren()
						.forEach(piece -> comps.add(VillageComponent.fromStructurePiece(piece))));
			comps.removeIf(a -> a == null);
			
			System.out.println("Found "+comps.size()+" structure pieces");
			Map<Type, Integer> tallies = new HashMap<>();
			comps.forEach(comp -> tallies.put(comp.type(), tallies.getOrDefault(comp.type(), 0) + 1));
			tallies.entrySet().forEach(entry -> System.out.println(" # "+entry.getKey().name()+" - "+entry.getValue()));
			ShowCubesPacket.send(player, comps);
		});
		PlayerEvent.PLAYER_QUIT.register(player -> Hrrmowners.PLAYERS.remove(player));
	}
}
