package com.lying.client.renderer;

import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.AnimationHelper;
import net.minecraft.client.render.entity.animation.Keyframe;
import net.minecraft.client.render.entity.animation.Transformation;

public class SurinaAnimations
{
	public static final Animation build_start = Animation.Builder.create(0.5417F)
			.addBoneAnimation("head", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.4583F, AnimationHelper.createRotationalVector(22.5F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("head", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0833F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.2083F, AnimationHelper.createTranslationalVector(0.0F, -3.5F, -4.0F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.4583F, AnimationHelper.createTranslationalVector(0.0F, -11.0F, -8.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_antenna", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.2917F, AnimationHelper.createRotationalVector(31.937F, -3.1803F, -6.8165F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_antenna", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.375F, AnimationHelper.createRotationalVector(31.937F, 3.1803F, 6.8165F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("body", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0833F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.4583F, AnimationHelper.createRotationalVector(65.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("body", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_arm", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 17.5F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.5417F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_arm", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0833F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.2083F, AnimationHelper.createTranslationalVector(0.0F, -3.09F, -4.18F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.4583F, AnimationHelper.createTranslationalVector(0.0F, -9.0F, -7.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_arm", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(0.0F, 0.0F, -17.5F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.5417F, AnimationHelper.createRotationalVector(-41.1509F, 11.7215F, 13.0867F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_arm", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0833F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.2083F, AnimationHelper.createTranslationalVector(0.0F, -3.09F, -4.18F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.4583F, AnimationHelper.createTranslationalVector(0.0F, -9.0F, -7.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_leg", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.375F, AnimationHelper.createRotationalVector(0.0F, 22.5F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_leg", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_leg", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.4583F, AnimationHelper.createRotationalVector(0.0F, -22.5F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_leg", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.build();

		public static final Animation build_main = Animation.Builder.create(0.5F).looping()
			.addBoneAnimation("head", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(22.6746F, -6.9262F, -2.8842F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(22.6746F, 6.9262F, 2.8842F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.5F, AnimationHelper.createRotationalVector(22.6746F, -6.9262F, -2.8842F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("head", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, -11.0F, -8.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("body", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(65.1758F, 1.6189F, 12.3964F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(65.1758F, -1.6189F, -12.3964F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createRotationalVector(65.1758F, 1.6189F, 12.3964F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("body", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_arm", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(115.8751F, -23.8008F, 11.6368F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.125F, AnimationHelper.createRotationalVector(33.3523F, -25.0181F, 10.2186F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(-41.1509F, -11.7215F, -13.0867F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.375F, AnimationHelper.createRotationalVector(33.3523F, -25.0181F, 10.2186F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.5F, AnimationHelper.createRotationalVector(115.8751F, -23.8008F, 11.6368F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_arm", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, -9.0F, -7.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_arm", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(-41.1509F, 11.7215F, 13.0867F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.125F, AnimationHelper.createRotationalVector(33.3523F, 25.0181F, -10.2186F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(115.8751F, 23.8008F, -11.6368F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.375F, AnimationHelper.createRotationalVector(33.3523F, 25.0181F, -10.2186F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.5F, AnimationHelper.createRotationalVector(-41.1509F, 11.7215F, 13.0867F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_arm", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, -9.0F, -7.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_leg", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 22.5F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_leg", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_leg", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, -22.5F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_leg", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_antenna", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(31.937F, -3.1803F, -6.8165F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.125F, AnimationHelper.createRotationalVector(50.3915F, 0.4563F, 1.204F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(33.8459F, 4.0928F, 9.2245F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.375F, AnimationHelper.createRotationalVector(17.8915F, 0.4563F, 1.204F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createRotationalVector(31.937F, -3.1803F, -6.8165F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_antenna", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(33.8459F, -4.0928F, -9.2245F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.125F, AnimationHelper.createRotationalVector(17.8915F, -0.4563F, -1.204F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(31.937F, 3.1803F, 6.8165F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.375F, AnimationHelper.createRotationalVector(50.3915F, -0.4563F, -1.204F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createRotationalVector(33.8459F, -4.0928F, -9.2245F), Transformation.Interpolations.LINEAR)
			))
			.build();

		public static final Animation build_end = Animation.Builder.create(0.5417F)
			.addBoneAnimation("head", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(22.5F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.2917F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("head", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, -11.0F, -8.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.2917F, AnimationHelper.createTranslationalVector(0.0F, -3.5F, -4.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5417F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("body", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(65.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5417F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("body", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5417F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_arm", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(26.74F, -5.49F, 2.69F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.125F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.2917F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 7.5F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.5417F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_arm", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, -9.0F, -7.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.2917F, AnimationHelper.createTranslationalVector(0.0F, -3.09F, -4.18F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5417F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_arm", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(79.64F, 21.01F, -5.93F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.2083F, AnimationHelper.createRotationalVector(31.5092F, 12.9292F, -3.6492F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.5417F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_arm", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, -9.0F, -7.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.2917F, AnimationHelper.createTranslationalVector(0.0F, -3.09F, -4.18F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.5417F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_leg", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 22.5F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5417F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_leg", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5417F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_leg", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, -22.5F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5417F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_leg", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5417F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_antenna", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(31.937F, -3.1803F, -6.8165F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.2917F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_antenna", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(31.937F, 3.1803F, 6.8165F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.build();
}
