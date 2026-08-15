package dev.objlib.api;

import net.minecraftborge.loader.ModList;

public interface IObjModelFactory {
	String MODID = "objlib";
	String FACTORY_CLASS = "dev.objlib.WavefrontFactory";

	IObjModel create(String path);
	IObjModel create(String path, boolean allowMixedFaces);

	static IObjModelFactory newFactory(IObjModelFactory fallback) {
		try {
			if (ModList.get().getLoadedMods().contains(MODID)) {
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
