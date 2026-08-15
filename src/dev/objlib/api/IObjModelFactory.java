package dev.objlib.api;

import dev.objlib.OBJLibrary;
import net.minecraftborge.loader.ModList;

public interface IObjModelFactory {
	String FACTORY_CLASS = "dev.objlib.WavefrontFactory";

	IObjModel create(String path);
	IObjModel create(String path, boolean allowMixedFaces);

	static IObjModelFactory create(IObjModelFactory fallback) {
		try {
			if (ModList.get().getLoadedMods().contains(OBJLibrary.MODID)) {
				Class<?> clazz = Class.forName(FACTORY_CLASS);
				if (IObjModelFactory.class.isAssignableFrom(clazz)) return (IObjModelFactory) clazz.newInstance();
				else throw new RuntimeException("WavefrontFactory is not instance of IObjModelFactory ???");
			}
		} catch (Throwable e) {
			System.err.println("Failed to retrieve OBJ model factory: " + e);
		}
		return fallback;
	}
}
