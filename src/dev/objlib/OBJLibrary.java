package dev.objlib;

import dev.objlib.api.OBJLoadedEvent;
import dev.objlib.api.OBJLoadingEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.src.TexturePackBase;
import net.minecraftborge.loader.event.EventBus;
import net.minecraftborge.loader.event.EventBusSubscriber;
import net.minecraftborge.loader.event.EventHandler;
import net.minecraftborge.loader.event.IModLifecycleListener;
import net.minecraftborge.loader.event.lifecycle.ModPostInitializationEvent;
import net.minecraftborge.loader.event.register.TerrainStitchEvent;

import java.io.IOException;
import java.io.InputStream;

@EventBusSubscriber(OBJLibrary.MODID)
public class OBJLibrary implements IModLifecycleListener {
	public static final String MODID = "objlib";

	@Override
	public void modPostInit(ModPostInitializationEvent event) {
		EventBus.push(new OBJLoadingEvent());
		Minecraft mc = Minecraft.getTheMinecraft();
		TexturePackBase textures = mc.texturePackList.selectedTexturePack;
		for (WavefrontModel model : WavefrontModel.allModels) {
			String name = model.getFilename();
			try (InputStream in = textures.getResourceAsStream(name)) {
				model.loadModel(in);
			} catch (IOException e) {
				throw new WavefrontException("Failed to load model " + name, e);
			}
		}
		EventBus.push(new OBJLoadedEvent());
	}
}
