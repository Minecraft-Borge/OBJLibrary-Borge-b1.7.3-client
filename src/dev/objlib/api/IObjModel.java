package dev.objlib.api;

import net.minecraft.src.Tessellator;

import java.util.List;

@SuppressWarnings("unused")
public interface IObjModel {
	IObjModel disableSmoothing();
	void destroy();
	String getFilename();
	boolean allowsMixedFaces();

	void renderAll();
	void tessellateAll(Tessellator tes);
	void tessellateAll(Tessellator tes, float offsetU, float offsetV, float scaleU, float scaleV);

	void render(String... groups);
	void tessellate(Tessellator tes, String... groups);
	void tessellate(Tessellator tes, float offsetU, float offsetV, float scaleU, float scaleV, String... groups);

	void renderGroup(String name);
	void tessellateGroup(Tessellator tes, String name);
	void tessellateGroup(Tessellator tes, float offsetU, float offsetV, float scaleU, float scaleV, String name);

	void renderAllExcept(String... groups);
	void tessellateAllExcept(Tessellator tes, String... groups);
	void tessellateAllExcept(Tessellator tes, float offsetU, float offsetV, float scaleU, float scaleV, String... groups);

	List<String> collectGroupNames();
}
