package computergraphics.datastructures.mesh;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jogamp.opengl.GL2;

import computergraphics.datastructures.bsp.BspTreeNode;
import computergraphics.datastructures.bsp.BspTreeToolsDummy;
import computergraphics.math.Vector;
import computergraphics.rendering.Texture;

public class Volume {
    
    /*
     * -----------------------VARIABLES----------------------------------
     */
    
    // byteArray from raw-File
    private byte[] model;
    // Resolution per Axis
    private Map<String, Integer> resolution = new HashMap<>();
    
    // color-byte-Arrays per Axis
    // TODO wird nicht mit TriangleMesh "verbunden" - wozu hier speichern?
    private Map<String, byte[][]> colors = new HashMap<>();
    // TriangleMeshes as Planes per Axis
    private Map<String, ITriangleMesh[]> triangleMeshes = new HashMap<>();
    
    // BSP-Tree for back-to-front-Sorting
    private BspTreeToolsDummy bspTool = new BspTreeToolsDummy();
    private Map<String, BspTreeNode> rootNodes = new HashMap<>();
    // Center Points of planes per Axis for back-to-front-sorting
    private Map<String, List<Vector>> centers = new HashMap<>();
    private Map<String, List<Integer>> centerIndices = new HashMap<>();
    private Map<String, List<Integer>> sortedCenterIndices = new HashMap<>();
    
    /*
     * -----------------------CONSTRUCTOR---------------------------------------
     */
    
    public Volume(String modelString, GL2 gl) throws IOException {
        model = Files.readAllBytes(setModel(modelString));
        
        // init lists, arrays what so ever to avoid nullpointers
        String[] axes = new String[] {"x", "y", "z"};
        for (int i = 0; i < 3; i++) {
            centers.put(axes[i], new ArrayList<Vector>());
            centerIndices.put(axes[i], new ArrayList<Integer>());
            sortedCenterIndices.put(axes[i], new ArrayList<Integer>());
            rootNodes.put(axes[i], new BspTreeNode());
        }
        
        // stack Axis: X
        setupTextureStack(gl, "x", resolution.get("y"), resolution.get("z"));
        // stack Axis: Y
        setupTextureStack(gl, "y", resolution.get("x"), resolution.get("z"));
        // stack Axis: Z
        setupTextureStack(gl, "z", resolution.get("x"), resolution.get("y"));
        System.out.println("ende");
    }
    
    /*
     * ----------------------MAINMETHODS--------------------------------------
     */
    
    private void setupTextureStack(GL2 gl, String axis, int resA, int resB) {
        colors.put(axis, createColorArray(axis, resolution.get(axis), resA, resB));
        
        ITriangleMesh[] triangleMeshAry = createTriangleMesh(axis, resolution.get(axis),
            centers.get(axis), centerIndices.get(axis));
        triangleMeshes.put(axis, triangleMeshAry);
        
        createAndBindTextures(gl, resolution.get(axis), resA, resB,
            triangleMeshes.get(axis), colors.get(axis));
        
        rootNodes.put(axis, bspTool.createBspTree(null, centers.get(axis),
            centerIndices.get(axis)));
    }
    
    private byte[][] createColorArray(String axis, int stackAxis, int resA, int resB) {
        
        int arySize = resA * resB * 4;
        byte[][] bAry = new byte[stackAxis][arySize];
        int accu = 0;
        
        // for each plane
        for (int i = 0; i < stackAxis; i++) {
            for (int a = 0; a < resA; a++) {
                for (int b = 0; b < resB; b++) {
                    // calculate color for each point within the plane
                    byte[] colors = new byte[4];
                    // ugly :(
                    switch (axis) {
                    case "x":
                        colors = calcColor(i, a, b);
                        break;
                    case "y":
                        colors = calcColor(a, i, b);
                        break;
                    case "z":
                        colors = calcColor(a, b, i);
                        break;
                    }
                    bAry[i][accu] = colors[0];
                    bAry[i][accu + 1] = colors[1];
                    bAry[i][accu + 2] = colors[2];
                    bAry[i][accu + 3] = colors[3];
                    accu += 4;
                }
            }
            accu = 0;
        }
        return bAry;
    }
    
    /**
     * generates a simple triangle Mesh per plane, fills the list of
     * Centre-Points and its indices
     * 
     * @param axis
     * @param centrePoints
     * @param centreIndices
     * @return
     */
    private ITriangleMesh[] createTriangleMesh(String axis, int res,
        List<Vector> centrePoints,
        List<Integer> centreIndices) {
        // draft see assets/notes/assignment6_1.JPG
        ITriangleMesh[] tAry = new TriangleMesh[res];
        for (int i = 0; i < res; i++) {
            ITriangleMesh tM = new TriangleMesh();
            Vector[] vectors = createVectorVertices(axis, res, i);
            
            // Calculate Centre-Point of Plane and add to centrePoint-Array
            // a + ((d - a) * 0.5) --> connection-vector from a to d cut by a
            // half
            Vector centre = vectors[0].add((vectors[3].subtract(vectors[0])).multiply(0.5));
            centrePoints.add(centre);
            centreIndices.add(i);
            
            // Add Triangles and Vertices to TriangleMesh
            // addVertices not tested!
            tM.addVertices(vectors); // a = 0; b = 1; c = 2; d = 3;
            
            Triangle t1 = new Triangle(2, 1, 0); // [c,b,a]
            Triangle t2 = new Triangle(2, 3, 1); // [c,d,b]
            
            // add texture coordinates to triangleMesh
            tM.addTextureCoordinate(new Vector(0, 0, 0));
            tM.addTextureCoordinate(new Vector(1, 0, 0));
            tM.addTextureCoordinate(new Vector(1, 1, 0));
            tM.addTextureCoordinate(new Vector(0, 1, 0));
            
            tM.addTriangle(t1);
            tM.addTriangle(t2);
            
            tAry[i] = tM;
        }
        return tAry;
    }
    
    private void createAndBindTextures(GL2 gl, int resAxis, int resA, int resB,
        ITriangleMesh[] triangleMeshes, byte[][] colors) {
        
        // setup
        int[] intary = new int[resAxis];
        IntBuffer intbuf = IntBuffer.wrap(intary);
        gl.glGenTextures(resAxis, intbuf);
        
        // creation and binding
        for (int i = 0; i < resAxis; i++) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(resA * resB * 4);
            buffer.put(colors[i]);
            buffer.position(0);
            
            gl.glEnable(GL2.GL_TEXTURE_2D);
            gl.glBindTexture(GL2.GL_TEXTURE_2D, intbuf.get(i));
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
            // TODO resA und B hier richtig?
            gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, resA, resB, 0, GL2.GL_RGBA,
                GL2.GL_BYTE, buffer);
            
            triangleMeshes[i].setTexture(new Texture(intbuf.get(i)));
        }
    }
    
    /*
     * ----------------------HELPERMETHODS--------------------------------------
     */
    
    // see assignment 6 recommendation
    private byte[] calcColor(int x, int y, int z) {
        return new byte[] {accessVolumeData(x, y, z), 0, 0, accessVolumeData(x, y, z)};
    }
    
    /**
     * this method may acces volume data
     * 
     * @param x
     *            in 0...rX-1
     * @param y
     *            in 0...rY-1
     * @param z
     *            in 0...rZ-1
     * @return volume data as byte
     */
    public byte accessVolumeData(int x, int y, int z) {
        // see assignment6
        return model[z * resolution.get("x") * resolution.get("y") + y * resolution.get("x") + x];
    }
    
    /**
     * helper-Method for "generateTriangleMeshPerAxis"
     * 
     * @param axis
     * @param res
     * @param i
     * @return
     */
    private Vector[] createVectorVertices(String axis, int res, int i) {
        // see assignment 6 page 2
        int p = -1 + (2 * i) / (res - 1);
        Vector[] vectors = new Vector[4];
        switch (axis) {
        case "x":
            Vector[] vecX = {new Vector(p, 1, 1), new Vector(p, -1, 1), new Vector(p, 1, -1),
                new Vector(p, -1, -1)};
            vectors = vecX;
            break;
        case "y":
            Vector[] vecY = {new Vector(-1, p, 1), new Vector(1, p, 1), new Vector(-1, p, -1),
                new Vector(1, p, -1)};
            vectors = vecY;
            break;
        case "z":
            Vector[] vecZ = {new Vector(-1, -1, p), new Vector(1, -1, p), new Vector(-1, 1, p),
                new Vector(1, 1, p)};
            vectors = vecZ;
            break;
        }
        return vectors;
    }
    
    /**
     * enables this class to proceed back-to-front-sorting for texture-stack
     * planes
     * 
     * @param rootNode
     * @param points
     * @param eye
     * @return
     */
    public List<Integer> sortStackBackToFront(BspTreeNode rootNode, List<Vector> points,
        Vector eye) {
        return bspTool.getBackToFront(rootNode, points, eye);
    }
    
    /**
     * makes setup easier Resolution might need to be adjusted depending on
     * hardware!
     * 
     * @param modelString
     *            ["negship", "monkey", "engine", "foot"]
     * @return path for given model
     */
    private Path setModel(String modelString) {
        String s = "";
        switch (modelString) {
        case "negship":
            resolution.put("x", 64);
            resolution.put("y", 64);
            resolution.put("z", 64);
            s = "assets/volumedata/neghip.vox";
            break;
        case "monkey":
            resolution.put("x", 256);
            resolution.put("y", 256);
            resolution.put("z", 62);
            s = "assets/volumedata/monkey.raw";
            break;
        case "engine":
            resolution.put("x", 256);
            resolution.put("y", 256);
            resolution.put("z", 256);
            s = "assets/volumedata/engine.raw";
            break;
        case "foot":
            resolution.put("x", 256);
            resolution.put("y", 256);
            resolution.put("z", 256);
            s = "assets/volumedata/foot.raw";
            break;
        }
        return Paths.get(s);
    }
    
    public String nestedArrayPartToString(byte[][] ary, int deepness) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < deepness; i++) {
            sb.append(i).append(". ").append(Arrays.toString(ary[i])).append("\n");
        }
        return sb.toString();
    }
}
