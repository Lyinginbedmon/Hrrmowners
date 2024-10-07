package com.lying.client.init;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.lying.client.model.SurinaEntityModel;
import com.lying.reference.Reference;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;

public class HOModelLayerParts
{
	private static final Map<EntityModelLayer, Supplier<TexturedModelData>> LAYERS = new HashMap<>();
	
	public static final EntityModelLayer SURINA		= make("surina", "main", () -> SurinaEntityModel.createBodyLayer(Dilation.NONE));
	public static final EntityModelLayer SURINA_CLOAK	= make("surina", "cloak", () -> SurinaEntityModel.createCloakLayer(Dilation.NONE));
	
	/** Creates a new model layer and registers its supplier for generation */
	private static EntityModelLayer make(String id, String name, Supplier<TexturedModelData> supplier)
	{
		EntityModelLayer layer = new EntityModelLayer(Reference.ModInfo.prefix(id), name);
		LAYERS.put(layer, supplier);
		return layer;
	}
	
	public static void init(BiConsumer<EntityModelLayer, Supplier<TexturedModelData>> consumer)
	{
		LAYERS.entrySet().forEach(entry -> consumer.accept(entry.getKey(), entry.getValue()));
	}
}
