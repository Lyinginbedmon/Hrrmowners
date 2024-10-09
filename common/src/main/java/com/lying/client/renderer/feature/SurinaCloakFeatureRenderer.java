package com.lying.client.renderer.feature;

import com.lying.client.init.HOModelLayerParts;
import com.lying.client.model.SurinaEntityModel;
import com.lying.entity.SurinaEntity;
import com.lying.reference.Reference;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;

public class SurinaCloakFeatureRenderer<T extends SurinaEntity> extends FeatureRenderer<T, SurinaEntityModel<T>>
{
	private final SurinaEntityModel<T> model;
	
	public SurinaCloakFeatureRenderer(FeatureRendererContext<T, SurinaEntityModel<T>> context, EntityModelLoader loader)
	{
		super(context);
		model = new SurinaEntityModel<T>(loader.getModelPart(HOModelLayerParts.SURINA_CLOAK));
	}
	
	public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T surinaEntity, float f, float g, float h, float j, float k, float l)
	{
		VillagerData data = surinaEntity.getVillagerData();
		if(data.getProfession() == VillagerProfession.NONE)
			return;
		
		SurinaEntityModel<T> contextModel = getContextModel();
		contextModel.copyStateTo(model);
		contextModel.copyPoseTo(model);
		renderModel(model, getCloakTexture(data.getProfession()), matrixStack, vertexConsumerProvider, i, surinaEntity, -1);
	}
	
	public static Identifier getCloakTexture(VillagerProfession profession)
	{
		return Reference.ModInfo.prefix("textures/entity/surina/"+profession.id().toLowerCase()+".png");
	}
}
