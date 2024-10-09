package com.lying.init;

import static com.lying.reference.Reference.ModInfo.prefix;

import com.lying.Hrrmowners;

import net.minecraft.util.Identifier;;

public class HOPacketHandler
{
	public static final Identifier SHOW_CUBE_ID	= prefix("s2c_show_cube");
	public static final Identifier HIDE_CUBE_ID	= prefix("s2c_hide_cube");
	
	public static void initServer()
	{
		Hrrmowners.LOGGER.info(" # Initialised server packet receipt operations");
		// Register packet receipt ops here
	}
}
