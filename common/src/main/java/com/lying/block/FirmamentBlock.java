package com.lying.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class FirmamentBlock extends Block
{
	public FirmamentBlock(Settings settings)
	{
		super(settings);
	}
	
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random rand)
	{
		BlockPos down = pos.down();
		if(rand.nextInt(16) == 0 && FallingBlock.canFallThrough(world.getBlockState(down)))
			ParticleUtil.spawnParticle(world, pos, rand, new BlockStateParticleEffect(ParticleTypes.BLOCK, state));
	}
}
