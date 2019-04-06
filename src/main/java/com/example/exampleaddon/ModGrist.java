package com.example.exampleaddon;

import com.mraof.minestuck.util.GristType;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import static com.mraof.minestuck.util.GristHelper.secondaryGristMap;

public class ModGrist {
	
	public static final GristType example = new GristType("example", 0.3F, new ResourceLocation("exampleaddon", "example")).setRegistryName("example");

	@SubscribeEvent
	public void registerGrist(RegistryEvent.Register<GristType> event)
	{
		IForgeRegistry<GristType> registry = event.getRegistry();
		
		registry.register(example);
		
		secondaryGristMap.get(GristType.Amber).add(example);
	}

}
