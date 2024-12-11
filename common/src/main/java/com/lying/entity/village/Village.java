package com.lying.entity.village;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.block.entity.NestBlockEntity;
import com.lying.entity.SurinaEntity;
import com.lying.entity.village.ai.Connector;
import com.lying.entity.village.ai.HOA;
import com.lying.entity.village.ai.action.Action;
import com.lying.entity.village.ai.action.ActionIncConnector;
import com.lying.entity.village.ai.action.ActionPlacePart;
import com.lying.entity.village.ai.goal.GoalHaveConnectors;
import com.lying.entity.village.ai.goal.GoalTypeMinimum;
import com.lying.entity.village.ai.goal.GoalWorkstationDiversity;
import com.lying.init.HOBlockEntityTypes;
import com.lying.init.HOVillagePartGroups;
import com.lying.init.HOVillageParts;
import com.lying.init.HOVillagerProfessions;
import com.lying.reference.Reference;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.structure.pool.alias.StructurePoolAliasLookup;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public class Village
{
	private static final Logger LOGGER = Hrrmowners.LOGGER;
	
	private final UUID id;
	private final RegistryKey<World> dimension;
	private final RegistryKey<Biome> biome;
	
	/** A set of VillagePart reflecting the layout of the village */
	private final VillageModel model = new VillageModel();
	
	private Optional<BlockPos> throneCached = Optional.empty();
	
	private final HOA hoa;
	
	/** True if this village exists in the world yet */
	private boolean inWorld = false;
	
	/** Cached array of residents within the boundaries of the village */
	private final List<SurinaEntity> residents = Lists.newArrayList();
	
	public Village(UUID idIn, RegistryKey<World> dimIn, RegistryKey<Biome> biomeIn)
	{
		id = idIn;
		dimension = dimIn;
		biome = biomeIn;
		
		// Prepare HOA
		hoa = new HOA(List.of(), List.of(
				new GoalHaveConnectors(3, t -> t.canLinkTo(HOVillageParts.STREET.get())),
				new GoalHaveConnectors(1, t -> t.canLinkTo(HOVillageParts.HOUSE.get())),
				GoalTypeMinimum.ofType(HOVillageParts.STREET, 1),
				GoalTypeMinimum.ofGroup(HOVillagePartGroups.HOUSE, VillageModel::residentPop),
				GoalTypeMinimum.ofGroup(HOVillagePartGroups.WORK, m -> m.residentsOfType(Resident.WORKER)),
				new GoalWorkstationDiversity()
				));
		
		for(VillagePart type : HOVillageParts.values())
			hoa.addAction(new ActionPlacePart(type, biome));
		
		hoa.addAction(new ActionIncConnector(0.7F));
	}
	
	public UUID id() { return this.id; }
	
	public void setInWorld() { this.inWorld = true; }
	
	@SuppressWarnings("deprecation")
	public boolean isLoaded(World world)
	{
		if(!inWorld || model.getCenter().isEmpty())
			return false;
		
		VillagePartInstance core = model.getCenter().get();
		return world.isChunkLoaded(core.min()) && world.isChunkLoaded(core.max());
	}
	
	public VillageModel model() { return this.model; }
	
	public void tryPlan(ServerWorld world)
	{
		hoa.tryGeneratePlan(model, this, world);
	}
	
	public void tick(ServerWorld world)
	{
		if(model.isEmpty())
			return;
		else if(throneCached.isPresent())
		{
			// Ensure the last-identified active throne is still active
			BlockPos pos = throneCached.get();
			Optional<NestBlockEntity> throne = world.getBlockEntity(pos, HOBlockEntityTypes.NEST.get());
			if(throne.isEmpty() || !throne.get().isOccupied())
			{
				throneCached = Optional.empty();
				return;
			}
		}
		else
		{
			Optional<VillagePartInstance> core = model.getCenter();
			if(core.isEmpty())
				return;
			
			// Try to find an active throne
			VillagePartInstance center = core.get();
			throneCached = center.getTilesOfType(world, HOBlockEntityTypes.NEST.get()).stream()
				.filter(p -> ((NestBlockEntity)world.getBlockEntity(p)).isOccupied()).findFirst();
			
			return;
		}
		
		// Update residential census
		if(world.getTime()%(Reference.Values.VILLAGE_TICK_RATE * 2) == 0)
		{
			residents.removeIf(r -> r == null || !r.isAlive() || r.isRemoved());
			model.getEnclosedResidents(SurinaEntity.class, world, 3D).forEach(r -> 
			{
				if(!r.hasVillage() || r.villageID().equals(id) && !residents.contains(r))
					registerResident(r);
			});
		}
		
		// Periodically evaluate goals and update plan if necessary
		if(hoa.hasPlan())
			hoa.tickPlan(world, this);
	}
	
	public void registerResident(SurinaEntity entity)
	{
		entity.setVillage(id);
		residents.add(entity);
	}
	
	public NbtCompound writeToNbt(NbtCompound nbt, ServerWorld world)
	{
		nbt.putUuid("ID", id);
		Identifier.CODEC.encodeStart(NbtOps.INSTANCE, dimension.getValue()).resultOrPartial(LOGGER::error).ifPresent(e -> nbt.put("Dim", e));
		Identifier.CODEC.encodeStart(NbtOps.INSTANCE, biome.getValue()).resultOrPartial(LOGGER::error).ifPresent(e -> nbt.put("Biome", e));
		nbt.put("Model", model.writeToNbt(new NbtCompound(), world));
		return nbt;
	}
	
	public static Village readFromNbt(NbtCompound nbt, ServerWorld world)
	{
		RegistryKey<World> dimension = World.CODEC.parse(NbtOps.INSTANCE, nbt.get("Dim")).resultOrPartial(LOGGER::error).orElse(World.OVERWORLD);
		Optional<RegistryEntry<Biome>> biomeOpt = Biome.REGISTRY_CODEC.parse(NbtOps.INSTANCE, nbt.get("Biome")).resultOrPartial(LOGGER::error);
		RegistryKey<Biome> biome = biomeOpt.isPresent() ? biomeOpt.get().getKey().get() : BiomeKeys.DESERT;
		Village village = new Village(nbt.getUuid("ID"), dimension, biome);
		village.model.readFromNbt(nbt.getCompound("Model"), world);
		return village;
	}
	
	public int getPopulation(@Nullable Resident type)
	{
		return type == null ? residents.size() : (int)residents.stream().filter(type::test).count();
	}
	
	public List<SurinaEntity> getResidentsMatching(Predicate<SurinaEntity> predicate)
	{
		return residents.stream().filter(predicate).toList();
	}
	
	public void erase(ServerWorld world)
	{
		residents.forEach(r -> r.setVillage(null));
		model.eraseAll(world, dimension);
	}
	
	public RegistryKey<Biome> biome() { return this.biome; }
	
	/** Adds a random new part to the village */
	public boolean grow(ServerWorld world)
	{
		Random rand = world.getRandom();
		VillagePart type = HOVillageParts.values().get(rand.nextInt(HOVillageParts.values().size()));
		return tryAddOfType(world, type, rand);
	}
	
	public boolean tryAddOfType(ServerWorld world, VillagePart type, Random rand)
	{
		if(model.isEmpty() || model.cannotExpand())
		{
			Hrrmowners.LOGGER.error("Villages cannot be founded haphazardly");
			return false;
		}
		
		/**
		 * Generate a random new VillagePart
		 * Find an available connector that can link to the new part
		 * If fail, create new part with different rotation
		 * If success, add that part to the model and generate
		 */
		
		/** The village center defines the starting rotation of future parts */
		BlockRotation baseRotation = model.getCenter().get().rotation;
		BlockRotation[] rotations = BlockRotation.values();
		
		for(int i=0; i<rotations.length; i++)
		{
			BlockRotation rotation = rotations[(baseRotation.ordinal() + i)%rotations.length];
			Optional<VillagePartInstance> partOpt = makeNewPart(BlockPos.ORIGIN, rotation, world, type, type.getStructurePool(biome), rand);
			if(partOpt.isEmpty())
			{
				Hrrmowners.LOGGER.error("Failed to create new part to expand village");
				return false;
			}
			
			VillagePartInstance part = partOpt.get();
			for(Connector connector : model.connectors())
			{
				Optional<BlockPos> connectOffset = part.getOffsetToLinkTo(connector);
				if(connectOffset.isPresent())
				{
					part.translate(connectOffset.get(), world.getStructureTemplateManager());
					
					// Ensure part won't intersect with another part in this position
					if(model.wouldIntersect(part))
						continue;
					
					addPart(part, world, true);
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean addPart(VillagePartInstance part, ServerWorld world, boolean generate)
	{
		boolean result = model.addPart(part, world, inWorld);
		if(inWorld)
		{
			part.notifyObservers(world.getRegistryKey());
			if(generate)
				part.placeInWorld(world);
		}
		return result;
	}
	
	public void notifyObservers()
	{
		if(!inWorld) return;
		model.notifyObservers(dimension);
	}
	
	public boolean intersects(Village village)
	{
		for(VillagePartInstance part : village.model.parts())
			if(model.wouldIntersect(part))
				return true;
		return false;
	}
	
	public boolean contains(BlockPos position) { return model.contains(position); }
	
	public void generateAll(ServerWorld world)
	{
		model.parts().forEach(part -> part.placeInWorld(world));
	}
	
	public boolean acceptPing(BlockPos position, SurinaEntity resident)
	{
		if(hoa.hasPlan())
		{
			Optional<Action> current = hoa.currentAction();
			return current.isPresent() ? current.get().acceptPing(position, resident, model) : false;
		}
		return false;
	}
	
	public static Optional<VillagePartInstance> makeNewPart(final BlockPos position, final BlockRotation rotation, ServerWorld server, VillagePart type, RegistryKey<StructurePool> poolKey, Random rand)
	{
		DynamicRegistryManager registryManager = server.getRegistryManager();
		Registry<StructurePool> registry = registryManager.get(RegistryKeys.TEMPLATE_POOL);
		StructurePoolAliasLookup aliasLookup = StructurePoolAliasLookup.create(List.of(), position, server.getSeed());
		Optional<StructurePool> optPool = Optional.of(poolKey).flatMap(key -> registry.getOrEmpty(aliasLookup.lookup(key)));
		if(optPool.isEmpty())
		{
			Hrrmowners.LOGGER.error("Structure pool {} is empty", poolKey.getValue().toString());
			return Optional.empty();
		}
		for(StructurePoolElement element : optPool.get().getElementIndicesInRandomOrder(rand).stream().filter(e -> e.getType() != StructurePoolElementType.EMPTY_POOL_ELEMENT).toList())
			return makeNewPart(element, type, position, rotation, server.getStructureTemplateManager());
		Hrrmowners.LOGGER.error("No useable elements found in structure pool {}", poolKey.getValue().toString());
		return Optional.empty();
	}
	
	public static Optional<VillagePartInstance> makeNewPart(StructurePoolElement element, VillagePart type, BlockPos position, BlockRotation rotation, StructureTemplateManager manager)
	{
		PoolStructurePiece poolStructurePiece = new PoolStructurePiece(
				manager, 
				element, 
				position, 
				element.getGroundLevelDelta(), 
				rotation, 
				element.getBoundingBox(manager, position, rotation), 
				StructureLiquidSettings.APPLY_WATERLOGGING);
		return Optional.of(new VillagePartInstance(UUID.randomUUID(), type, poolStructurePiece, manager));
	}
	
	public static enum Resident implements StringIdentifiable
	{
		/** Adults capable of having a profession */
		WORKER(s -> !s.isBaby() && s.getVillagerData().getProfession() != VillagerProfession.NITWIT && s.getVillagerData().getProfession() != HOVillagerProfessions.QUEEN.get()),
		/** Adult nitwits */
		NEET(s -> !s.isBaby() && s.getVillagerData().getProfession() == VillagerProfession.NITWIT),
		/** Children */
		CHILD(s -> s.isBaby()),
		/** Queen of a given village */
		QUEEN(s -> !s.isBaby() && s.getVillagerData().getProfession() == HOVillagerProfessions.QUEEN.get());
		
		private final Predicate<SurinaEntity> check;
		
		private Resident(Predicate<SurinaEntity> checkIn)
		{
			check = checkIn;
		}
		
		public boolean test(SurinaEntity ent) { return check.test(ent); }
		
		public String asString() { return name().toLowerCase(); }
		
		@Nullable
		public static Resident fromString(String name)
		{
			for(Resident res : Resident.values())
				if(res.asString().equalsIgnoreCase(name))
					return res;
			return null;
		}
	}
}
