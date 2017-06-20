package computergraphics.exercises;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import com.jogamp.opengl.GL2;

import computergraphics.datastructures.curve.BezierCurve;
import computergraphics.datastructures.mesh.ObjReader;
import computergraphics.datastructures.mesh.TriangleMesh;
import computergraphics.math.Vector;
import computergraphics.misc.Scene;
import computergraphics.rendering.Shader;
import computergraphics.rendering.Texture;
import computergraphics.scenegraph.TranslationNode;
import computergraphics.scenegraph.TriangleMeshNode;
import computergraphics.scenegraph.INode.RenderMode;

public class Exercise6 extends Scene {
	private static final long serialVersionUID = 8141036480333465137L;
	
	
	private TranslationNode translation;
	private TranslationNode translation1;
	private TriangleMesh tri = new TriangleMesh();
	private TriangleMesh tri1 = new TriangleMesh();

	public Exercise6() {
		// Timer timeout and shader mode (PHONG, TEXTURE, NO_LIGHTING)
		super(16, Shader.ShaderMode.TEXTURE, RenderMode.REGULAR);
	}

	@Override
	public void setupScenegraph(GL2 gl) {
		// Setup scene after OpenGL is ready
		getRoot().setLightPosition(new Vector(0, 2, 0));

//		Texture t = new Texture(0);
//		t.bind(gl);
		
		byte[] test = new byte[2*2*4];
		test[0] = 50;
		test[1] = 0;
		test[2] = 0;
		test[3] = 50;
		test[4] = 0;
		test[5] = 0;
		test[6] = 0;
		test[7] = 0;
		test[8] = 0;
		test[9] = 0;
		test[10] = 0;
		test[11] = 0;
		test[12] = 50;
		test[13] = 0;
		test[14] = 0;
		test[15] = 50;
		ByteBuffer buffer = ByteBuffer.allocateDirect(2*2*4);
		buffer.put(test);
		buffer.position(0);
		
		byte[] test1 = new byte[2*2*4];
		test1[0] = 0;
		test1[1] = 0;
		test1[2] = 0;
		test1[3] = 0;
		test1[4] = 50;
		test1[5] = 0;
		test1[6] = 0;
		test1[7] = 50;
		test1[8] = 50;
		test1[9] = 0;
		test1[10] = 0;
		test1[11] = 50;
		test1[12] = 0;
		test1[13] = 0;
		test1[14] = 0;
		test1[15] = 0;
		ByteBuffer buffer1 = ByteBuffer.allocateDirect(2*2*4);
		buffer1.put(test1);
		buffer1.position(0);
		
		
		int[] intary = new int[2];
		
		IntBuffer intbuf = IntBuffer.wrap(intary);
		
		gl.glGenTextures(2, intbuf);
		
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, intbuf.get(0));
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
//			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
//			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, 2, 2, 0, GL2.GL_RGBA, GL2.GL_BYTE, buffer);
		
		gl.glBindTexture(GL2.GL_TEXTURE_2D, intbuf.get(1));
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
//			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
//			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, 2, 2, 0, GL2.GL_RGBA, GL2.GL_BYTE, buffer1);
		
		System.out.println(intbuf.get(0));
		
		ObjReader objReader = new ObjReader();
		objReader.read("meshes/square.obj", tri);
		tri.setTexture(new Texture(intbuf.get(0)));
		
		TriangleMeshNode tn = new TriangleMeshNode(tri);
		translation = new TranslationNode(new Vector(0, 0.5, 0));
		translation.addChild(tn);
		getRoot().addChild(translation);
		
		ObjReader objReader1 = new ObjReader();
		objReader1.read("meshes/square.obj", tri1);
		tri1.setTexture(new Texture(intbuf.get(1)));
		
		TriangleMeshNode tn1 = new TriangleMeshNode(tri1);
		translation1 = new TranslationNode(new Vector(0, 0.5, 0.1));
		translation1.addChild(tn1);
		getRoot().addChild(translation1);
	}

	@Override
	public void keyPressed(KeyEvent keyEvent) {

	}

	@Override
	public void timerTick(int counter) {

	}

	/**
	 * Program entry point.
	 */
	public static void main(String[] args) {
		new Exercise6();
	}
}
