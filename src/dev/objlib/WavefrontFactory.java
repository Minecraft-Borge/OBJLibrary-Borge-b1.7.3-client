package dev.objlib;

import dev.objlib.api.IObjModel;
import dev.objlib.api.IObjModelFactory;

public class WavefrontFactory implements IObjModelFactory {
	@Override
	public IObjModel create(String path) {
		return new WavefrontModel(path);
	}

	@Override
	public IObjModel create(String path, boolean allowMixedFaces) {
		return new WavefrontModel(path, allowMixedFaces);
	}
}
