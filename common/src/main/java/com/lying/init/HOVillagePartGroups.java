package com.lying.init;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.lying.Hrrmowners;
import com.lying.entity.village.VillagePartGroup;

import net.minecraft.util.Identifier;

public class HOVillagePartGroups
{
	private static final Map<Identifier, Supplier<VillagePartGroup>> VALUES = new HashMap<>();
	
	public static final Supplier<VillagePartGroup> CENTER	= register(prefix("center"), id -> new VillagePartGroup(id, 0x0000FF));
	public static final Supplier<VillagePartGroup> HOUSE	= register(prefix("house"), id -> new VillagePartGroup(id, 0x00FF00, isBuilding()));
	public static final Supplier<VillagePartGroup> WORK		= register(prefix("workstation"), id -> new VillagePartGroup(id, 0xFF0000, isBuilding()));
	public static final Supplier<VillagePartGroup> STREET	= register(prefix("street"), id -> new VillagePartGroup(id, 0xFFFFFF, isStreet()));
	
	private static Supplier<VillagePartGroup> register(Identifier name, Function<Identifier, VillagePartGroup> funcIn)
	{
		Supplier<VillagePartGroup> supplier = () -> funcIn.apply(name);
		VALUES.put(name, supplier);
		return supplier;
	}
	
	private static Predicate<VillagePartGroup> isStreet() { return t -> t.equalsAny(STREET.get()); }
	
	private static Predicate<VillagePartGroup> isBuilding() { return t -> t.equalsAny(HOUSE.get(), WORK.get()); }
	
	public static void init()
	{
		Hrrmowners.LOGGER.info(" # Initialised {} village part groups", VALUES.size());
	}
	
	public static List<VillagePartGroup> values() { return VALUES.values().stream().map(t -> t.get()).toList(); }
	
	@Nullable
	public static VillagePartGroup byID(Identifier idIn) { return VALUES.getOrDefault(idIn, () -> null).get(); }
}
