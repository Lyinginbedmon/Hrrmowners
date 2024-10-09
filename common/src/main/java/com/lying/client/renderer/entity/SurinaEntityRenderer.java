package com.lying.client.renderer.entity;

import com.lying.client.init.HOModelLayerParts;
import com.lying.client.model.SurinaEntityModel;
import com.lying.client.renderer.feature.SurinaCloakFeatureRenderer;
import com.lying.entity.SurinaEntity;
import com.lying.reference.Reference;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.util.Identifier;

public class SurinaEntityRenderer extends MobEntityRenderer<SurinaEntity, SurinaEntityModel<SurinaEntity>>
{
	public static final Identifier TEXTURE = Reference.ModInfo.prefix("textures/entity/surina/surina.png");
	public static final Identifier EYES_TEXTURE = Reference.ModInfo.prefix("textures/entity/surina/surina_eyes.png");
	
	public SurinaEntityRenderer(Context ctx)
	{
		super(ctx, new SurinaEntityModel<SurinaEntity>(ctx.getPart(HOModelLayerParts.SURINA)), 0.5F);
		this.addFeature(new EyesFeatureRenderer<SurinaEntity, SurinaEntityModel<SurinaEntity>>(this) 
		{
			public RenderLayer getEyesTexture() { return RenderLayer.getEyes(SurinaEntityRenderer.EYES_TEXTURE); }
		});
		this.addFeature(new SurinaCloakFeatureRenderer<SurinaEntity>(this, ctx.getModelLoader()));
	}
	
	public Identifier getTexture(SurinaEntity var1)
	{
		// TODO Add Badlands texture
		return TEXTURE;
	}
}
