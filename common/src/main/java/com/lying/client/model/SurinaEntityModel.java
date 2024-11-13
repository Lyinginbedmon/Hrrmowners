package com.lying.client.model;

import com.lying.client.renderer.SurinaAnimations;
import com.lying.entity.SurinaEntity;
import com.lying.entity.SurinaEntity.SurinaAnimation;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose;
import net.minecraft.client.render.entity.model.CrossbowPosing;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public class SurinaEntityModel<T extends SurinaEntity> extends SinglePartEntityModel<T> implements ModelWithArms, ModelWithHead
{
	private static final String ANTENNA_RIGHT = "right_antenna";
	private static final String ANTENNA_LEFT = "left_antenna";
	private final ModelPart root;
	private final ModelPart rightAntenna, leftAntenna;
	private final ModelPart head, body, rightArm, leftArm, rightLeg, leftLeg;
	
	public ArmPose leftArmPose = ArmPose.EMPTY;
	public ArmPose rightArmPose = ArmPose.EMPTY;
	public boolean sneaking;
	public float leaningPitch;
	
	protected final ModelPart[] limbs;
	
	public SurinaEntityModel(ModelPart obj)
	{
		root = obj.getChild(EntityModelPartNames.ROOT);
		head = root.getChild(EntityModelPartNames.HEAD);
		rightAntenna = head.getChild(ANTENNA_RIGHT);
		leftAntenna = head.getChild(ANTENNA_LEFT);
		body = root.getChild(EntityModelPartNames.BODY);
		rightArm = root.getChild(EntityModelPartNames.RIGHT_ARM);
		leftArm = root.getChild(EntityModelPartNames.LEFT_ARM);
		rightLeg = root.getChild(EntityModelPartNames.RIGHT_LEG);
		leftLeg = root.getChild(EntityModelPartNames.LEFT_LEG);
		
		limbs = new ModelPart[] {root, head, rightAntenna, leftAntenna, body, rightArm, leftArm, rightLeg, leftLeg};
	}
	
	public static TexturedModelData createBodyLayer(Dilation dilation)
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData root = modelPartData.addChild(EntityModelPartNames.ROOT, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		
		ModelPartData head = root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -24.75F, -2.75F));
			head.addChild("cube_r1", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -7.0F, -3.0F, 6.0F, 7.0F, 6.0F, dilation), ModelTransform.of(2.0F, 0.4371F, -0.3485F, 0.3054F, 0.0F, 0.0F));
			head.addChild(ANTENNA_LEFT, ModelPartBuilder.create().uv(24, 0).mirrored().cuboid(-1.0F, -9.0F, 0.0F, 3.0F, 9.0F, 0.0F, dilation).mirrored(false), ModelTransform.of(1.25F, -6.5F, -1.25F, -0.9997F, 0.1103F, 0.0706F));
			head.addChild(ANTENNA_RIGHT, ModelPartBuilder.create().uv(24, 0).cuboid(-2.0F, -9.0F, 0.0F, 3.0F, 9.0F, 0.0F, dilation), ModelTransform.of(-1.25F, -6.5F, -1.25F, -0.9997F, -0.1103F, -0.0706F));
		
		root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create()
			.uv(0, 13).cuboid(-4.0F, -12.0F, -2.0F, 8.0F, 6.0F, 4.0F, dilation.add(0.5F))
			.uv(24, 9).cuboid(-3.0F, -6.0F, -1.25F, 6.0F, 6.0F, 3.0F, dilation), ModelTransform.of(0.0F, -13.0F, 2.0F, 0.3054F, 0.0F, 0.0F));
		
		root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create()
			.uv(42, 0).cuboid(-3.0F, -1.5F, -1.5F, 3.0F, 12.0F, 3.0F, dilation), ModelTransform.pivot(-4.5F, -23.5F, -1.25F));
		root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create()
			.uv(42, 0).mirrored().cuboid(0.0F, -1.5F, -1.5F, 3.0F, 12.0F, 3.0F, dilation).mirrored(false), ModelTransform.pivot(4.5F, -23.5F, -1.25F));
		
		ModelPartData right_leg = root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create(), ModelTransform.pivot(-2.5F, -13.0F, 3.0F));
			ModelPartData right_thigh = right_leg.addChild("right_thigh", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 11.0F, -3.0F));
				right_thigh.addChild("cube_r2", ModelPartBuilder.create().uv(15, 18).cuboid(-3.0F, -3.0F, -1.0F, 4.0F, 3.0F, 9.0F, dilation), ModelTransform.of(1.0F, -6.0F, -2.25F, 0.6109F, 0.0F, 0.0F));
			ModelPartData right_ankle = right_leg.addChild("right_ankle", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 11.75F, -3.0F));
				right_ankle.addChild("cube_r3", ModelPartBuilder.create().uv(37, 15).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 5.0F, dilation.add(0.5F)), ModelTransform.of(0.0F, -6.25F, -1.0F, -0.5236F, 0.0F, 0.0F));
			right_leg.addChild("right_foot", ModelPartBuilder.create().uv(44, 15).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 8.0F, dilation), ModelTransform.of(0.0F, 13.0F, -1.0F, 1.2217F, 0.0F, 0.0F));
		
		ModelPartData left_leg = root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create(), ModelTransform.pivot(2.5F, -13.0F, 3.0F));
			ModelPartData left_thigh = left_leg.addChild("left_thigh", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 11.0F, -3.0F));
				left_thigh.addChild("cube_r4", ModelPartBuilder.create().uv(15, 18).mirrored().cuboid(-1.0F, -3.0F, -1.0F, 4.0F, 3.0F, 9.0F, dilation).mirrored(false), ModelTransform.of(-1.0F, -6.0F, -2.25F, 0.6109F, 0.0F, 0.0F));
			ModelPartData left_ankle = left_leg.addChild("left_ankle", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 11.75F, -3.0F));
				left_ankle.addChild("cube_r5", ModelPartBuilder.create().uv(37, 15).mirrored().cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 5.0F, dilation.add(0.5F)).mirrored(false), ModelTransform.of(0.0F, -6.25F, -1.0F, -0.5236F, 0.0F, 0.0F));
			left_leg.addChild("left_foot", ModelPartBuilder.create().uv(44, 15).mirrored().cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 8.0F, dilation).mirrored(false), ModelTransform.of(0.0F, 13.0F, -1.0F, 1.2217F, 0.0F, 0.0F));
			
		return TexturedModelData.of(modelData, 64, 32);
	}
	
	public static TexturedModelData createCloakLayer(Dilation dilation)
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData root2 = modelPartData.addChild(EntityModelPartNames.ROOT, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		
		ModelPartData head2 = root2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -24.75F, -2.75F));
			head2.addChild("cube_r1", ModelPartBuilder.create()
				.uv(24, 0).cuboid(-5.0F, -7.0F, -3.0F, 6.0F, 7.0F, 6.0F, dilation.add(0.5F))
				.uv(0, 0).cuboid(-5.0F, -7.0F, -3.0F, 6.0F, 7.0F, 6.0F, dilation.add(0.25F)), ModelTransform.of(2.0F, 0.4371F, -0.3485F, 0.3054F, 0.0F, 0.0F));
			head2.addChild(ANTENNA_LEFT, ModelPartBuilder.create(), ModelTransform.of(1.25F, -6.5F, -1.25F, -0.9997F, 0.1103F, 0.0706F));
			head2.addChild(ANTENNA_RIGHT, ModelPartBuilder.create(), ModelTransform.of(-1.25F, -6.5F, -1.25F, -0.9997F, -0.1103F, -0.0706F));
		
		root2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 13).cuboid(-4.0F, -12.0F, -2.0F, 8.0F, 15.0F, 4.0F, dilation.add(1.0F)), ModelTransform.of(0.0F, -13.0F, 2.0F, 0.3054F, 0.0F, 0.0F));
		
		root2.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(24, 13).cuboid(-3.0F, -1.5F, -1.5F, 3.0F, 12.0F, 3.0F, dilation.add(0.5F)), ModelTransform.pivot(-4.5F, -23.5F, -1.25F));
		root2.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(24, 13).mirrored().cuboid(0.0F, -1.5F, -1.5F, 3.0F, 12.0F, 3.0F, dilation.add(0.5F)).mirrored(false), ModelTransform.pivot(4.5F, -23.5F, -1.25F));
		
		root2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create(), ModelTransform.pivot(-2.5F, -13.0F, 3.0F));
		root2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create(), ModelTransform.pivot(2.5F, -13.0F, 3.0F));
		
		return TexturedModelData.of(modelData, 64, 32);
	}
	
	public ModelPart getPart() { return root; }
	
	public void animateModel(T arg, float f, float g, float h)
	{
		this.leaningPitch = arg.getLeaningPitch(h);
		super.animateModel(arg, f, g, h);
	}
	
	public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch)
	{
		getPart().traverse().forEach(part -> part.resetTransform());
		if(entity.isPlayingAnimation(SurinaAnimation.IDLE) || !entity.isPlayingAnimation())
			doIdleAnimation(entity, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);
		else
		{
			this.updateAnimation(entity.getAnimation(SurinaAnimation.BUILD_START), SurinaAnimations.build_start, entity.age);
			this.updateAnimation(entity.getAnimation(SurinaAnimation.BUILD_MAIN), SurinaAnimations.build_main, entity.age);
			this.updateAnimation(entity.getAnimation(SurinaAnimation.BUILD_END), SurinaAnimations.build_end, entity.age);
		}
	}
	
	private void doIdleAnimation(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch)
	{
		boolean isFlying = entity.getFallFlyingTicks() > 4;
		boolean isSwimming = entity.isInSwimmingPose();
		this.head.yaw = headYaw * ((float)Math.PI / 180);
		this.head.pitch = isFlying ? -0.7853982f : (this.leaningPitch > 0.0f ? (isSwimming ? this.lerpAngle(this.leaningPitch, this.head.pitch, -0.7853982f) : this.lerpAngle(this.leaningPitch, this.head.pitch, headPitch * ((float)Math.PI / 180))) : headPitch * ((float)Math.PI / 180));
		this.body.yaw = 0.0f;
		float f = 1.0f;
		if(isFlying)
		{
			f = (float)((Entity)((Object)entity)).getVelocity().lengthSquared();
			f /= 0.2f;
			f *= f * f;
		}
		if (f < 1.0f)
			f = 1.0f;
		
		this.rightArm.pitch = MathHelper.cos(limbSwing * 0.6662f + (float)Math.PI) * 2.0f * limbSwingAmount * 0.5f / f;
		this.leftArm.pitch = MathHelper.cos(limbSwing * 0.6662f) * 2.0f * limbSwingAmount * 0.5f / f;
		this.rightArm.roll = 0.0f;
		this.leftArm.roll = 0.0f;
		this.rightLeg.pitch = MathHelper.cos(limbSwing * 0.6662f) * (float)Math.toRadians(30D) * limbSwingAmount / f;
		this.leftLeg.pitch = MathHelper.cos(limbSwing * 0.6662f + (float)Math.PI) * (float)Math.toRadians(30D) * limbSwingAmount / f;
		this.rightLeg.yaw = 0.005f;
		this.leftLeg.yaw = -0.005f;
		this.rightLeg.roll = 0.005f;
		this.leftLeg.roll = -0.005f;
		if(this.riding)
		{
			this.rightArm.pitch += -0.62831855f;
			this.leftArm.pitch += -0.62831855f;
			this.rightLeg.pitch = -1.4137167f;
			this.rightLeg.yaw = 0.31415927f;
			this.rightLeg.roll = 0.07853982f;
			this.leftLeg.pitch = -1.4137167f;
			this.leftLeg.yaw = -0.31415927f;
			this.leftLeg.roll = -0.07853982f;
		}
		this.rightArm.yaw = 0.0f;
		this.leftArm.yaw = 0.0f;
		this.animateArms(entity, ageInTicks);
		
		if (this.rightArmPose != ArmPose.SPYGLASS)
			CrossbowPosing.swingArm(this.rightArm, ageInTicks, 1.0f);
		
		if (this.leftArmPose != ArmPose.SPYGLASS)
			CrossbowPosing.swingArm(this.leftArm, ageInTicks, -1.0f);
		
		if (this.leaningPitch > 0.0f)
		{
			float f5 = limbSwing % 26.0f;
			Arm humanoidarm = this.getPreferredArm(entity);
			float f1 = humanoidarm == Arm.RIGHT && this.handSwingProgress > 0.0f ? 0.0f : this.leaningPitch;
			float f2 = humanoidarm == Arm.LEFT && this.handSwingProgress > 0.0f ? 0.0f : this.leaningPitch;
			if (!entity.isUsingItem())
			{
				if (f5 < 14.0f)
				{
					this.leftArm.pitch = this.lerpAngle(f2, this.leftArm.pitch, 0.0f);
					this.rightArm.pitch = MathHelper.lerp(f1, this.rightArm.pitch, 0.0f);
					this.leftArm.yaw = this.lerpAngle(f2, this.leftArm.yaw, (float)Math.PI);
					this.rightArm.yaw = MathHelper.lerp(f1, this.rightArm.yaw, (float)Math.PI);
					this.leftArm.roll = this.lerpAngle(f2, this.leftArm.roll, (float)Math.PI + 1.8707964f * this.method_2807(f5) / this.method_2807(14.0f));
					this.rightArm.roll = MathHelper.lerp(f1, this.rightArm.roll, (float)Math.PI - 1.8707964f * this.method_2807(f5) / this.method_2807(14.0f));
				}
				else if (f5 >= 14.0f && f5 < 22.0f)
				{
					float f6 = (f5 - 14.0f) / 8.0f;
					this.leftArm.pitch = this.lerpAngle(f2, this.leftArm.pitch, 1.5707964f * f6);
					this.rightArm.pitch = MathHelper.lerp(f1, this.rightArm.pitch, 1.5707964f * f6);
					this.leftArm.yaw = this.lerpAngle(f2, this.leftArm.yaw, (float)Math.PI);
					this.rightArm.yaw = MathHelper.lerp(f1, this.rightArm.yaw, (float)Math.PI);
					this.leftArm.roll = this.lerpAngle(f2, this.leftArm.roll, 5.012389f - 1.8707964f * f6);
					this.rightArm.roll = MathHelper.lerp(f1, this.rightArm.roll, 1.2707963f + 1.8707964f * f6);
				}
				else if (f5 >= 22.0f && f5 < 26.0f)
				{
					float f32 = (f5 - 22.0f) / 4.0f;
					this.leftArm.pitch = this.lerpAngle(f2, this.leftArm.pitch, 1.5707964f - 1.5707964f * f32);
					this.rightArm.pitch = MathHelper.lerp(f1, this.rightArm.pitch, 1.5707964f - 1.5707964f * f32);
					this.leftArm.yaw = this.lerpAngle(f2, this.leftArm.yaw, (float)Math.PI);
					this.rightArm.yaw = MathHelper.lerp(f1, this.rightArm.yaw, (float)Math.PI);
					this.leftArm.roll = this.lerpAngle(f2, this.leftArm.roll, (float)Math.PI);
					this.rightArm.roll = MathHelper.lerp(f1, this.rightArm.roll, (float)Math.PI);
				}
			}
			this.leftLeg.pitch = MathHelper.lerp(this.leaningPitch, this.leftLeg.pitch, 0.3f * MathHelper.cos(limbSwing * 0.33333334f + (float)Math.PI));
			this.rightLeg.pitch = MathHelper.lerp(this.leaningPitch, this.rightLeg.pitch, 0.3f * MathHelper.cos(limbSwing * 0.33333334f));
		}
	}
	
	protected float lerpAngle(float angleOne, float angleTwo, float magnitude)
	{
		float f = (magnitude - angleTwo) % ((float)Math.PI * 2);
		if (f < (float)(-Math.PI))
			f += (float)Math.PI * 2;
		
		if (f >= (float)Math.PI)
			f -= (float)Math.PI * 2;
		
		return angleTwo + angleOne * f;
	}
	
	public void copyPoseTo(SurinaEntityModel<?> child)
	{
		for(int i=0; i<limbs.length; i++)
			child.limbs[i].copyTransform(limbs[i]);
	}
	
	public void setVisible(boolean visible)
	{
		getPart().traverse().forEach(part -> part.visible = visible);
	}
	
	public ModelPart getHead() { return this.head; }
	
	private Arm getPreferredArm(T entity)
	{
		Arm arm = entity.getMainArm();
		return entity.preferredHand == Hand.MAIN_HAND ? arm : arm.getOpposite();
	}
	
	protected ModelPart getArm(Arm arm) { return arm == Arm.LEFT ? this.leftArm : this.rightArm; }
	
	public void setArmAngle(Arm var1, MatrixStack var2) { this.getArm(var1).rotate(var2); }
	
	protected void animateArms(T entity, float animationProgress)
	{
		if(!(this.handSwingProgress <= 0.0f))
		{
			Arm humanoidarm = this.getPreferredArm(entity);
			ModelPart modelpart = this.getArm(humanoidarm);
			float f = this.handSwingProgress;
			this.body.yaw = MathHelper.sin(MathHelper.sqrt(f) * ((float)Math.PI * 2)) * 0.2f;
			if (humanoidarm == Arm.LEFT)
				this.body.yaw *= -1.0f;
			
//			this.rightArm.pivotZ = MathHelper.sin(this.body.yaw) * 5.0f;
//			this.rightArm.pivotX = -MathHelper.cos(this.body.yaw) * 5.0f;
//			this.leftArm.pivotZ = -MathHelper.sin(this.body.yaw) * 5.0f;
//			this.leftArm.pivotX = MathHelper.cos(this.body.yaw) * 5.0f;
			this.rightArm.yaw += this.body.yaw;
			this.leftArm.yaw += this.body.yaw;
			this.leftArm.pitch += this.body.yaw;
			f = 1.0f - this.handSwingProgress;
			f *= f;
			f *= f;
			f = 1.0f - f;
			float f1 = MathHelper.sin(f * (float)Math.PI);
			float f2 = MathHelper.sin(this.handSwingProgress * (float)Math.PI) * -(this.head.pitch - 0.7f) * 0.75f;
			modelpart.pitch -= f1 * 1.2f + f2;
			modelpart.yaw += this.body.yaw * 2.0f;
			modelpart.roll += MathHelper.sin(this.handSwingProgress * (float)Math.PI) * -0.4f;
		}
	}
	
	private float method_2807(float f) { return -65F * f + f * f; }
}
