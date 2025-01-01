package com.lying;

import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.lying.entity.village.VillageManager;
import com.lying.init.HOBlockEntityTypes;
import com.lying.init.HOBlocks;
import com.lying.init.HOCommands;
import com.lying.init.HOEntityTypes;
import com.lying.init.HOItems;
import com.lying.init.HOMemoryModuleTypes;
import com.lying.init.HOPointOfInterestTypes;
import com.lying.init.HOSurinaTrades;
import com.lying.init.HOVillagePartGroups;
import com.lying.init.HOVillageParts;
import com.lying.init.HOVillagerProfessions;
import com.lying.reference.Reference;
import com.lying.utility.ServerBus;
import com.lying.utility.XPlatHandler;

import net.minecraft.server.network.ServerPlayerEntity;

public final class Hrrmowners
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Reference.ModInfo.MOD_ID);
    
    /** List of all server players, used in the absence of a world or server object */
    public static List<ServerPlayerEntity> PLAYERS = Lists.newArrayList();
    
    public static final VillageManager MANAGER = new VillageManager();
    
    public static XPlatHandler HANDLER = new XPlatHandler()
	{
		public void registerPOIs() { }
	};
    
	/*
	 * Goal: A system that autonomously manages villages to fulfill the needs of its occupants
	 * 
	 * Roadmap
	 * * Enable visualisation of village components (this means telling the client where they are, how big, etc. and then displaying that, since it isn't synced) so 
	 *   we can actually *see* the village and later monitor how it's growing
	 * * Break down each component to its classification (primarily if it is a living space, work station, or road) with its connection points (structure jigsaw blocks)
	 * * Create a persistent manager object (the Hrrmowners Association :drum riff: ) that stores those classifications for each village, populated during worldgen, as well 
	 *   as a record of villagers living here (this *should* already exist for spawning golems and cats but we'll see)
	 * * Have the manager issue a "supervision" order to a villager so that the villager goes to the border of a new component and spawns in the structure
	 * * Establish a GOAP-style planner to enable the manager to make architectural decisions to satisfy its objectives:
	 * * * There should always be N open points for further expansion
	 * * * There should always be at least as many beds as villagers
	 * * * There should always be at least as many workstations as adult working (not Nitwit) villagers
	 * * * Workstations should avoid repetition (ie. don't build six cartography buildings)
	 * * * Replace/repair destroyed homes/workstations (tentative, the hard part is identifying damage reliably & efficiently)
	 * * Have the manager periodically recalculate its analytics (how far/close to meeting its objectives it is) and execute plans as needed
	 */
	
    public static void commonInit()
    {
    	ServerBus.registerEventHandlers();
    	
    	HOCommands.init();
    	HOBlocks.init();
    	HOBlockEntityTypes.init();
    	HOEntityTypes.init();
    	HOMemoryModuleTypes.init();
    	HOItems.init();
    	HOPointOfInterestTypes.init();
    	HOVillagerProfessions.init();
    	HOSurinaTrades.init();
    	HOVillagePartGroups.init();
    	HOVillageParts.init();
    }
    
    public static void forAllPlayers(Consumer<ServerPlayerEntity> consumer) { PLAYERS.forEach(player -> consumer.accept(player)); }
}
