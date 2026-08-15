package dev.objlib.parts;

import net.minecraft.src.Tessellator;
import net.minecraft.src.Vec3D;
import net.minecraftborge.loader.math.Vector3f;

public class Face {
	public int[] verticesID;
	public Vector3f[] positions;
	public Vector3f[] normals;
	public Vector3f faceNormal;
	public Vector3f[] uvs;
	private final boolean smoothing;

	public Face(boolean smoothing) {
		this.smoothing = smoothing;
	}

	public void addFaceForRender(Tessellator tes) {
		this.addFaceForRender(tes, 0.0F, 0.0F, 1.0F, 1.0F);
	}

	public void addFaceForRender(Tessellator tes, float offsetU, float offsetV, float scaleU, float scaleV) {
		if (this.faceNormal == null) {
			this.faceNormal = this.calculateFaceNormal();
		}
		if (!this.smoothing) {
			tes.setNormal(this.faceNormal.x, this.faceNormal.y, this.faceNormal.z);
		}

		float averageU = 0.0F, averageV = 0.0F;
		if ((this.uvs != null) && (this.uvs.length > 0)) {
			for (Vector3f uv : this.uvs) {
				averageU += uv.x;
				averageV += uv.y;
			}

			averageU /= this.uvs.length;
			averageV /= this.uvs.length;
		}

		for (int i = 0; i < this.positions.length; i++) {
			if (this.smoothing && this.normals != null && i < this.normals.length) {
				tes.setNormal(this.normals[i].x, this.normals[i].y, this.normals[i].z);
			}
			if (this.uvs != null && this.uvs.length > 0) {
				tes.addVertexWithUV(this.positions[i].x, this.positions[i].y, this.positions[i].z, (this.uvs[i].x + offsetU) * scaleU, (this.uvs[i].y + offsetV) * scaleV);
			} else {
				tes.addVertex(this.positions[i].x, this.positions[i].y, this.positions[i].z);
			}
		}
	}

	public Vector3f calculateFaceNormal() {
		Vec3D v1 = Vec3D.createVector(this.positions[1].x - this.positions[0].x, this.positions[1].y - this.positions[0].y, this.positions[1].z - this.positions[0].z);
		Vec3D v2 = Vec3D.createVector(this.positions[2].x - this.positions[0].x, this.positions[2].y - this.positions[0].y, this.positions[2].z - this.positions[0].z);
		Vec3D normal = v1.crossProduct(v2).normalize();
		return new Vector3f((float) normal.xCoord, (float) normal.yCoord, (float) normal.zCoord);
	}
}
