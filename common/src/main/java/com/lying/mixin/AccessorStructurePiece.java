package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.structure.StructurePiece;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

@Mixin(StructurePiece.class)
public interface AccessorStructurePiece
{
	@Accessor("facing")
	public Direction ho$facing();
	
	@Accessor("mirror")
	public BlockMirror ho$mirror();
	
	@Accessor("rotation")
	public BlockRotation ho$rotation();
}
