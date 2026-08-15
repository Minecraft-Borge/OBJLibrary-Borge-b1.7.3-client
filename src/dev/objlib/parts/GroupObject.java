package dev.objlib.parts;

import net.minecraft.src.Tessellator;

import java.util.ArrayList;

public class GroupObject {
	public String name;
	public ArrayList<Face> faces = new ArrayList<>();
	public int glDrawMode;

	public GroupObject() {
		this("");
	}
	public GroupObject(String name) {
		this(name, -1);
	}
	public GroupObject(String name, int glDrawMode) {
		this.name = name;
		this.glDrawMode = glDrawMode;
	}

	public void render() {
		if (!this.faces.isEmpty()) {
			Tessellator tes = Tessellator.instance;
			tes.startDrawing(this.glDrawMode);
			this.tessellate(tes);
			tes.draw();
		}
	}
	public void tessellate(Tessellator tes) {
		if (!this.faces.isEmpty()) {
			for (Face face : this.faces) face.addFaceForRender(tes);
		}
	}public void tessellate(Tessellator tes, float offsetU, float offsetV, float scaleU, float scaleV) {
		if (!this.faces.isEmpty()) {
			for (Face face : this.faces) face.addFaceForRender(tes, offsetU, offsetV, scaleU, scaleV);
		}
	}
}
