package dev.objlib;

import dev.objlib.api.IObjModel;
import dev.objlib.parts.Face;
import dev.objlib.parts.GroupObject;
import net.minecraft.src.Tessellator;
import net.minecraftborge.loader.math.Vector3f;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WavefrontModel implements IObjModel {
	public static final LinkedHashSet<WavefrontModel> allModels = new LinkedHashSet<>();

	private static final Pattern vertexPattern = Pattern.compile("(v( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *\\n)|(v( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *$)");
	private static final Pattern vertexNormalPattern = Pattern.compile("(vn( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *\\n)|(vn( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *$)");
	private static final Pattern textureCoordinatePattern = Pattern.compile("(vt( (\\-){0,1}\\d+\\.\\d+){2,3} *\\n)|(vt( (\\-){0,1}\\d+(\\.\\d+)?){2,3} *$)");
	private static final Pattern face_V_VT_VN_Pattern = Pattern.compile("(f( \\d+/\\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+/\\d+){3,4} *$)");
	private static final Pattern face_V_VT_Pattern = Pattern.compile("(f( \\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+){3,4} *$)");
	private static final Pattern face_V_VN_Pattern = Pattern.compile("(f( \\d+//\\d+){3,4} *\\n)|(f( \\d+//\\d+){3,4} *$)");
	private static final Pattern face_V_Pattern = Pattern.compile("(f( \\d+){3,4} *\\n)|(f( \\d+){3,4} *$)");
	private static final Pattern groupObjectPattern = Pattern.compile("([go]( [\\w\\d\\.]+) *\\n)|([go]( [\\w\\d\\.]+) *$)");

	private static Matcher vertexMatcher, vertexNormalMatcher, textureCoordinateMatcher;
	private static Matcher face_V_VT_VN_Matcher, face_V_VT_Matcher, face_V_VN_Matcher, face_V_Matcher;
	private static Matcher groupObjectMatcher;

	public ArrayList<Vector3f> positions = new ArrayList<>();
	public ArrayList<Vector3f> normals = new ArrayList<>();
	public ArrayList<Vector3f> uvs = new ArrayList<>();
	public ArrayList<GroupObject> groupObjects = new ArrayList<>();
	private GroupObject currentGroup;
	private final String filename;
	private boolean smoothing = true;
	private boolean disableFormatCheck = false;
	private boolean allowMixedFaces = false;

	public WavefrontModel(String path) {
		this(path, false);
	}
	public WavefrontModel(String path, boolean allowMixedFaces)  {
		if (allowMixedFaces) this.allowMixedFaces = true;

		this.filename = path;

		allModels.add(this);
	}

	public WavefrontModel disableSmoothing() {
		this.smoothing = false;
		return this;
	}
	public WavefrontModel disableFormatCheck() {
		this.disableFormatCheck = true;
		return this;
	}
	public void destroy() {
		this.positions.clear();
		this.normals.clear();
		this.uvs.clear();
		this.groupObjects.clear();
		this.currentGroup = null;
	}

	public String getFilename() {
		return this.filename;
	}
	public boolean allowsMixedFaces() {
		return this.allowMixedFaces;
	}

	public void loadModel(InputStream in) {
		String line;
		int count = 0;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			while ((line = reader.readLine()) != null) {
				count++;
				line = line.replaceAll("\\s+", " ").trim();

				if (line.startsWith("#") || line.isEmpty()) continue;
				else if (line.startsWith("v ")) {
					Vector3f vertex = this.parsePositions(line, count);
					if (vertex != null) this.positions.add(vertex);
				} else if (line.startsWith("vn ")) {
					Vector3f vertex = this.parseNormals(line, count);
					if (vertex != null) this.normals.add(vertex);
				} else if (line.startsWith("vt ")) {
					Vector3f vertex = this.parseUVs(line, count);
					if (vertex != null) this.uvs.add(vertex);
				} else if (line.startsWith("f ")) {
					if (this.currentGroup == null) this.currentGroup = new GroupObject("Default");
					Face face = this.parseFace(line, count);
					if (face != null) this.currentGroup.faces.add(face);
				} else if (line.startsWith("g ") || line.startsWith("o ")) {
					GroupObject group = this.parseGroup(line, count);
					if (group != null) {
						if (this.currentGroup != null) this.groupObjects.add(this.currentGroup);
						this.currentGroup = group;
					} else this.currentGroup = null;
				}
			}
			this.groupObjects.add(this.currentGroup);
		} catch (IOException e) {
			throw new WavefrontException("Exception reading OBJ model", e);
		}
	}

	public void renderAll() {
		if (this.allowMixedFaces) throw new UnsupportedOperationException("Cannot render mixed-faces model " + this.filename);

		Tessellator tes = Tessellator.instance;
		if (this.currentGroup != null) {
			tes.startDrawing(this.currentGroup.glDrawMode);
		} else tes.startDrawing(GL11.GL_TRIANGLES);
		this.tessellateAll(tes);
		tes.draw();
	}
	public void tessellateAll(Tessellator tes) {
		for (GroupObject group : this.groupObjects) {
			group.tessellate(tes);
		}
	}
	public void tessellateAll(Tessellator tes, float offsetU, float offsetV, float scaleU, float scaleV) {
		for (GroupObject group : this.groupObjects) {
			group.tessellate(tes, offsetU, offsetV, scaleU, scaleV);
		}
	}

	public void render(String... groups) {
		if (this.allowMixedFaces) throw new UnsupportedOperationException("Cannot render mixed-faces model " + this.filename);

		for (GroupObject group : this.groupObjects) {
			for (String name : groups) {
				if (name.equalsIgnoreCase(group.name)) group.render();
			}
		}
	}
	public void tessellate(Tessellator tes, String... groups) {
		for (GroupObject group : this.groupObjects) {
			for (String name : groups) {
				if (name.equalsIgnoreCase(group.name)) group.tessellate(tes);
			}
		}
	}
	public void tessellate(Tessellator tes, float offsetU, float offsetV, float scaleU, float scaleV, String... groups) {
		for (GroupObject group : this.groupObjects) {
			for (String name : groups) {
				if (name.equalsIgnoreCase(group.name)) group.tessellate(tes, offsetU, offsetV, scaleU, scaleV);
			}
		}
	}

	public void renderGroup(String name) {
		if (this.allowMixedFaces) throw new UnsupportedOperationException("Cannot render mixed-faces model " + this.filename);

		for (GroupObject group : this.groupObjects) {
			if (name.equalsIgnoreCase(group.name)) group.render();
		}
	}
	public void tessellateGroup(Tessellator tes, String name) {
		for (GroupObject group : this.groupObjects) {
			if (name.equalsIgnoreCase(group.name)) group.tessellate(tes);
		}
	}
	public void tessellateGroup(Tessellator tes, float offsetU, float offsetV, float scaleU, float scaleV, String name) {
		for (GroupObject group : this.groupObjects) {
			if (name.equalsIgnoreCase(group.name)) group.tessellate(tes, offsetU, offsetV, scaleU, scaleV);
		}
	}

	public void renderAllExcept(String... groups) {
		if (this.allowMixedFaces) throw new UnsupportedOperationException("Cannot render mixed-faces model " + this.filename);

		loop:
		for (GroupObject group : this.groupObjects) {
			for(String name : groups) {
				if (name.equalsIgnoreCase(group.name)) continue loop;
			}
			group.render();
		}
	}
	public void tessellateAllExcept(Tessellator tes, String... groups) {
		loop:
		for (GroupObject group : this.groupObjects) {
			for (String name : groups) {
				if (name.equalsIgnoreCase(group.name)) continue loop;
			}
			group.tessellate(tes);
		}
	}
	public void tessellateAllExcept(Tessellator tes, float offsetU, float offsetV, float scaleU, float scaleV, String... groups) {
		loop:
		for (GroupObject group : this.groupObjects) {
			for (String name : groups) {
				if (name.equalsIgnoreCase(group.name)) continue loop;
			}
			group.tessellate(tes, offsetU, offsetV, scaleU, scaleV);
		}
	}

	private Vector3f parsePositions(String line, int count) {
		if (this.disableFormatCheck || isValidVertexLine(line)) {
			line = line.substring(line.indexOf(" ") + 1);
			String[] tokens = line.split(" ");

			try {
				if (tokens.length == 2) {
					return new Vector3f(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), 0.0F);
				}
				if (tokens.length == 3) {
					return new Vector3f(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
				}
			} catch (NumberFormatException e) {
				throw new WavefrontException("Number format exception at line " + count, e);
			}
		} else {
			this.throwEntryParseError(line, count, "incorrect positions format");
		}

		return null;
	}

	private Vector3f parseNormals(String line, int count) {
		if (this.disableFormatCheck || isValidVertexNormalLine(line)) {
			line = line.substring(line.indexOf(" ") + 1);
			String[] tokens = line.split(" ");

			try {
				if (tokens.length == 3) {
					return new Vector3f(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
				}
			} catch (NumberFormatException e) {
				throw new WavefrontException("Number format exception at line " + count, e);
			}
		} else {
			this.throwEntryParseError(line, count, "incorrect normals format");
		}

		return null;
	}

	private Vector3f parseUVs(String line, int count) {
		if (this.disableFormatCheck || isValidTextureCoordinateLine(line)) {
			line = line.substring(line.indexOf(" ") + 1);
			String[] tokens = line.split(" ");

			try {
				if (tokens.length == 2) {
					return new Vector3f(Float.parseFloat(tokens[0]), 1.0F - Float.parseFloat(tokens[1]), 0.0F);
				}
				if (tokens.length == 3) {
					return new Vector3f(Float.parseFloat(tokens[0]), 1.0F - Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
				}
			} catch (NumberFormatException e) {
				throw new WavefrontException("Number format exception at line " + count, e);
			}
		} else {
			this.throwEntryParseError(line, count, "incorrect uvs format");
		}

		return null;
	}

	private Face parseFace(String line, int count) {
		Face face = null;

		if (this.disableFormatCheck || isValidFaceLine(line)) {
			face = new Face(this.smoothing);

			String trimmedLine = line.substring(line.indexOf(" ") + 1);
			String[] tokens = trimmedLine.split(" ");
			String[] subTokens;

			if (!this.allowMixedFaces) {
				if (tokens.length == 3) {
					if (this.currentGroup.glDrawMode == -1) this.currentGroup.glDrawMode = GL11.GL_TRIANGLES;
					else if (this.currentGroup.glDrawMode != GL11.GL_TRIANGLES) {
						this.throwEntryParseError(line, count, "invalid face size (expected 4, got " + tokens.length + ")");
					}
				}
				if (tokens.length == 4) {
					if (this.currentGroup.glDrawMode == -1) this.currentGroup.glDrawMode = GL11.GL_QUADS;
					else if (this.currentGroup.glDrawMode != GL11.GL_QUADS) {
						this.throwEntryParseError(line, count, "invalid face size (expected 3, got " + tokens.length + ")");
					}
				}
			}

			if(this.disableFormatCheck || isValidFace_V_VT_VN_Line(line)) {
				face.positions = new Vector3f[tokens.length];
				face.uvs = new Vector3f[tokens.length];
				face.normals = new Vector3f[tokens.length];

				for(int i = 0; i < tokens.length; ++i) {
					subTokens = tokens[i].split("/");

					face.positions[i] = positions.get(Integer.parseInt(subTokens[0]) - 1);
					face.uvs[i] = uvs.get(Integer.parseInt(subTokens[1]) - 1);
					face.normals[i] = normals.get(Integer.parseInt(subTokens[2]) - 1);
				}

				face.faceNormal = face.calculateFaceNormal();
			} else if(this.disableFormatCheck || isValidFace_V_VT_Line(line)) {
				face.positions = new Vector3f[tokens.length];
				face.uvs = new Vector3f[tokens.length];

				for(int i = 0; i < tokens.length; ++i) {
					subTokens = tokens[i].split("/");

					face.positions[i] = positions.get(Integer.parseInt(subTokens[0]) - 1);
					face.uvs[i] = uvs.get(Integer.parseInt(subTokens[1]) - 1);
				}

				face.faceNormal = face.calculateFaceNormal();
			} else if(this.disableFormatCheck || isValidFace_V_VN_Line(line)) {
				face.positions = new Vector3f[tokens.length];
				face.normals = new Vector3f[tokens.length];

				for(int i = 0; i < tokens.length; ++i) {
					subTokens = tokens[i].split("//");

					face.positions[i] = positions.get(Integer.parseInt(subTokens[0]) - 1);
					face.normals[i] = normals.get(Integer.parseInt(subTokens[1]) - 1);
				}

				face.faceNormal = face.calculateFaceNormal();
			} else if(this.disableFormatCheck || isValidFace_V_Line(line)) {
				face.positions = new Vector3f[tokens.length];

				for(int i = 0; i < tokens.length; ++i) {
					face.positions[i] = positions.get(Integer.parseInt(tokens[i]) - 1);
				}

				face.faceNormal = face.calculateFaceNormal();
			} else {
				this.throwEntryParseError(line, count, "incorrect faces format");
			}
		}

		return face;
	}

	private GroupObject parseGroup(String line, int count) {
		GroupObject group = null;

		if (this.disableFormatCheck || isValidGroupObjectLine(line)) {
			String trimmedLine = line.substring(line.indexOf(" ") + 1);

			if (!trimmedLine.isEmpty()) group = new GroupObject(trimmedLine);
		} else {
			this.throwEntryParseError(line, count, "incorrect groups format");
		}

		return group;
	}

	private void throwEntryParseError(String line, int count, String message) {
		throw new WavefrontException("Failed to parse entry ('" + line + "', line " + count + ") in file '" + this.filename + "': " + message);
	}

	private static boolean isValidVertexLine(String line) {
		if(vertexMatcher != null) {
			vertexMatcher.reset();
		}

		vertexMatcher = vertexPattern.matcher(line);
		return vertexMatcher.matches();
	}

	private static boolean isValidVertexNormalLine(String line) {
		if(vertexNormalMatcher != null) {
			vertexNormalMatcher.reset();
		}

		vertexNormalMatcher = vertexNormalPattern.matcher(line);
		return vertexNormalMatcher.matches();
	}

	private static boolean isValidTextureCoordinateLine(String line) {
		if(textureCoordinateMatcher != null) {
			textureCoordinateMatcher.reset();
		}

		textureCoordinateMatcher = textureCoordinatePattern.matcher(line);
		return textureCoordinateMatcher.matches();
	}

	private static boolean isValidFace_V_VT_VN_Line(String line) {
		if(face_V_VT_VN_Matcher != null) {
			face_V_VT_VN_Matcher.reset();
		}

		face_V_VT_VN_Matcher = face_V_VT_VN_Pattern.matcher(line);
		return face_V_VT_VN_Matcher.matches();
	}

	private static boolean isValidFace_V_VT_Line(String line) {
		if(face_V_VT_Matcher != null) {
			face_V_VT_Matcher.reset();
		}

		face_V_VT_Matcher = face_V_VT_Pattern.matcher(line);
		return face_V_VT_Matcher.matches();
	}

	private static boolean isValidFace_V_VN_Line(String line) {
		if(face_V_VN_Matcher != null) {
			face_V_VN_Matcher.reset();
		}

		face_V_VN_Matcher = face_V_VN_Pattern.matcher(line);
		return face_V_VN_Matcher.matches();
	}

	private static boolean isValidFace_V_Line(String line) {
		if(face_V_Matcher != null) {
			face_V_Matcher.reset();
		}

		face_V_Matcher = face_V_Pattern.matcher(line);
		return face_V_Matcher.matches();
	}

	private static boolean isValidFaceLine(String line) {
		return isValidFace_V_VT_VN_Line(line) || isValidFace_V_VT_Line(line) || isValidFace_V_VN_Line(line) || isValidFace_V_Line(line);
	}

	private static boolean isValidGroupObjectLine(String line) {
		if(groupObjectMatcher != null) {
			groupObjectMatcher.reset();
		}

		groupObjectMatcher = groupObjectPattern.matcher(line);
		return groupObjectMatcher.matches();
	}

	public List<String> collectGroupNames() {
		List<String> names = new ArrayList<>();
		for (GroupObject group : this.groupObjects) {
			if (!names.contains(group.name)) names.add(group.name);
		}
		return names;
	}
}
