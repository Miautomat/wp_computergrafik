/**
 * Prof. Philipp Jenke
 * Hochschule für Angewandte Wissenschaften (HAW), Hamburg
 * 
 * Base framework for "WP Computergrafik".
 */

package computergraphics.datastructures.bsp;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jogamp.opengl.GL2;

import computergraphics.datastructures.bsp.BspNode;
import computergraphics.datastructures.bsp.BspTreeNode;
import computergraphics.math.Vector;
import computergraphics.misc.Scene;
import computergraphics.rendering.Shader;
import computergraphics.scenegraph.INode.RenderMode;

/**
 * Scene and application for the BSP tree example.
 * 
 * @author Philipp Jenke
 */
public class BspScene extends Scene {

  private static final long serialVersionUID = 6506789797991105075L;

  /**
   * Scene graph BSP node
   */
  private BspNode node;

  public BspScene() {
    super(100, Shader.ShaderMode.PHONG, RenderMode.REGULAR);
  }

  @Override
  public void setupScenegraph(GL2 gl) {
    getRoot().setLightPosition(new Vector(1, 1, 1));
    getRoot().setAnimated(true);
    getRoot().setBackgroundColor(new Vector(0.25, 0.25, 0.25, 1));
    gl.glLineWidth(5);
    gl.glPointSize(5);

		// Create data
		// Simple two point-BSP
		// Vector x = new Vector(1.0, 1.0, 0);
		// Vector y = new Vector(3.0, 3.0, 0);
		//
		//// Vector p = new Vector(2.0, 2.0, 0);
		//// Vector n = new Vector(0.5, 0.5, 0);
		//
		// Vector[] pointsAry = { x, y };
		// Integer[] pointIndicesAry = { 0, 1 };
		// List<Integer> pointIndices = Arrays.asList(pointIndicesAry);
		// List<Vector> points = Arrays.asList(pointsAry);

		// Simple 4-Point BSP
		// Vector a1 = new Vector(1.0, 1.0, 0); // 0
		// Vector b1 = new Vector(2.0, 2.0, 0); // 1
		// Vector c1 = new Vector(4.0, 4.0, 0); // 2
		// Vector d1 = new Vector(5.5, 5.5, 0); // 3
		//
		// Vector[] pointsAry = { a1, b1, c1, d1 };
		// List<Vector> points = Arrays.asList(pointsAry);
		// Integer[] pointIndicesAry = { 0, 1, 2, 3 };
		// List<Integer> pointIndices = Arrays.asList(pointIndicesAry);

		// Vector a2 = new Vector(1.0, 1.0, 0);
		// Vector b2 = new Vector(2.0, 2.0, 0);
		// Vector c2 = new Vector(1.5, 3.0, 0);
		// Vector d2 = new Vector(3.0, 4.5, 0);
		// Vector e2 = new Vector(4.0, 5.5, 0);
		//
		// Vector[] ary2 = { a2, b2, c2, d2, e2 };
		// List<Vector> points2 = Arrays.asList(ary2);
		// Integer[] aryI2 = { 0, 1, 2, 3, 4 };
		// List<Integer> pointsIndices2 = Arrays.asList(aryI2);
    
		 // Generated BSP
		 int numberOfPoints = 10;
		 List<Vector> points = new ArrayList<Vector>();
		 List<Integer> pointIndices = new ArrayList<Integer>();
		 for (int i = 0; i < numberOfPoints; i++) {
		 points.add(new Vector((float) (2 * Math.random() - 1), (float) (2 *
		 Math.random() - 1), 0));
		 pointIndices.add(i);
		 }
    System.out.println("points: " + points);
    System.out.println("pointIndices: " + pointIndices);
    // Create tree
    BspTreeToolsDummy tools = new BspTreeToolsDummy();
    BspTreeNode rootNode = tools.createBspTree(null, points, pointIndices);

    // Add result to scene graph
    if (rootNode != null) {
      Vector observer = new Vector(1, 1, 0);
      List<Integer> back2FrontSorted = tools.getBackToFront(rootNode, points, observer);
      node = new BspNode(rootNode, points, back2FrontSorted, observer);
      getRoot().addChild(node);
    }
  }

  @Override
  public void keyPressed(KeyEvent keyEvent) {
    char keyCode = keyEvent.getKeyChar();
    switch (keyCode) {
    case 'p':
      node.showPoints = !node.showPoints;
      break;
    case 'e':
      node.showElements = !node.showElements;
      break;
    case 'l':
      node.showPlanes = !node.showPlanes;
      break;
    case 'b':
      node.showBackToFront = !node.showBackToFront;
      break;
    }
  }

  @Override
  public void timerTick(int counter) {
  }

  public static void main(String[] args) {
    new BspScene();
  }
}
