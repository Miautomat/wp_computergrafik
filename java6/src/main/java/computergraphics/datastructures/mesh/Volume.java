package computergraphics.datastructures.mesh;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jogamp.opengl.GL2;

import computergraphics.datastructures.bsp.BspTreeNode;
import computergraphics.datastructures.bsp.BspTreeToolsDummy;
import computergraphics.math.Vector;
import computergraphics.rendering.Texture;

public class Volume {
    
    // TriangleMeshes as Planes per Axis
    private Map<String, ITriangleMesh[]> triangleMeshes = new HashMap<>();
    // BSP-Tree for back-to-front-Sorting
    private BspTreeToolsDummy bspTool;
    private Map<String, BspTreeNode> rootNodes = new HashMap<>();
    // Center Points of planes per Axis for back-to-front-sorting
    private Map<String, List<Vector>> centers = new HashMap<>();
    private Map<String, List<Integer>> centerIndices = new HashMap<>();
    private Map<String, List<Integer>> sortedCenterIndices = new HashMap<>();
    // byteArray from raw-File
    private byte[] model;
    // Textures per Axis
    private Map<String, Texture[]> textures = new HashMap<>();
    // color-byte-Arrays per Axis
    private Map<String, byte[][]> colors = new HashMap<>();
    // Resolution per Axis
    private Map<String, Integer> resolution = new HashMap<>();
    
    private Vector eye;
    
    public Volume(String modelString, GL2 gl, Vector eye) throws IOException {
        this.eye = eye;
        model = Files.readAllBytes(setModel(modelString));
        generateTexturePlanes(gl);
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
    
    private void generateTexturePlanes(GL2 gl) {
        // stack Axis: X
        colors.put("x", generateColorByteArrays("x", resolution.get("y"), resolution.get("z"),
            resolution.get("x")));
        triangleMeshes.put("x", generateTriangleMeshPerAxis("x", resolution.get("x"),
            centers.get("x"), centerIndices.get("x")));
        textures.put("x", generateAndBindTexturesPerAxis(gl, resolution.get("x"),
            resolution.get("y"), resolution.get("z"),
            triangleMeshes.get("x"), colors.get("x")));
        rootNodes.put("x", bspTool.createBspTree(null, centers.get("x"), centerIndices.get("x")));
        
        // stack Axis: Y
        colors.put("y", generateColorByteArrays("y", resolution.get("x"), resolution.get("z"),
            resolution.get("y")));
        triangleMeshes.put("y", generateTriangleMeshPerAxis("y", resolution.get("y"),
            centers.get("y"), centerIndices.get("y")));
        textures.put("y", generateAndBindTexturesPerAxis(gl, resolution.get("y"),
            resolution.get("x"), resolution.get("z"),
            triangleMeshes.get("y"), colors.get("y")));
        rootNodes.put("y", bspTool.createBspTree(null, centers.get("y"), centerIndices.get("y")));
        
        // stack Axis: Z
        colors.put("z", generateColorByteArrays("z", resolution.get("x"), resolution.get("y"),
            resolution.get("z")));
        triangleMeshes.put("z", generateTriangleMeshPerAxis("z", resolution.get("z"),
            centers.get("z"), centerIndices.get("z")));
        textures.put("z", generateAndBindTexturesPerAxis(gl, resolution.get("z"),
            resolution.get("x"), resolution.get("y"),
            triangleMeshes.get("z"), colors.get("z")));
        rootNodes.put("z", bspTool.createBspTree(null, centers.get("z"), centerIndices.get("z")));
    }
    
    private Texture[] generateAndBindTexturesPerAxis(GL2 gl, int resAxis, int resA, int resB,
        ITriangleMesh[] triangleMeshes, byte[][] colors) {
        Texture[] t = new Texture[resAxis];
        
        int[] intary = new int[resAxis];
        IntBuffer intbuf = IntBuffer.wrap(intary);
        gl.glGenTextures(resAxis, intbuf);
        
        for (int i = 0; i <= resAxis; i++) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(resA * resB * 4);
            buffer.put(colors[i]);
            buffer.position(0);
            
            gl.glEnable(GL2.GL_TEXTURE_2D);
            gl.glBindTexture(GL2.GL_TEXTURE_2D, intbuf.get(i));
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
            gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, 256, 256, 0, GL2.GL_RGBA,
                GL2.GL_BYTE, buffer);
            t[i] = new Texture(intbuf.get(i));
            triangleMeshes[i].setTexture(t[i]);
        }
        return t;
    }
    
    private byte[][] generateColorByteArrays(String axis, int resA, int resB, int stackAxis) {
        
        int arySize = resA * resB * 4;
        byte[][] bAry = new byte[stackAxis][arySize];
        int accu = 0;
        
        // for each plane
        for (int i = 0; i <= stackAxis; i++) {
            for (int a = 0; a <= resA; a++) {
                for (int b = 0; b <= resB; b++) {
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
        }
        return bAry;
    }
    
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
        return model[z * resolution.get("x") * resolution.get("y") + y + resolution.get("z") + x];
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
    private ITriangleMesh[] generateTriangleMeshPerAxis(String axis, int res,
        List<Vector> centrePoints,
        List<Integer> centreIndices) {
        // draft see assets/notes/assignment6_1.JPG
        ITriangleMesh[] tAry = new TriangleMesh[res];
        for (int i = 0; i <= res; i++) {
            ITriangleMesh tM = new TriangleMesh();
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
        }
        return tAry;
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
    
    public Map<String, ITriangleMesh[]> getTriangles() {
        return triangleMeshes;
    }
    
    public void setTriangles(Map<String, ITriangleMesh[]> triangleMeshes) {
        this.triangleMeshes = triangleMeshes;
    }
    
    public Map<String, List<Integer>> getSortedCentreIndices() {
        return sortedCenterIndices;
    }
    
    public void setSortedCentreIndices(Map<String, List<Integer>> sortedCentreIndices) {
        this.sortedCenterIndices = sortedCentreIndices;
    }
    
    public Map<String, Texture[]> getTextures() {
        return textures;
    }
    
    public void setTextures(Map<String, Texture[]> textures) {
        this.textures = textures;
    }
    
    public Vector getEye() {
        return eye;
    }
    
    public void setEye(Vector eye) {
        this.eye = eye;
    }
    
    /**
     * To String to byteArray to check if it is correct
     * 
     * @param ary
     * @return String of byte[]
     */
    public String byteArrayToString(byte[] ary) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (byte b : ary) {
            sb.append("i = " + i).append(", b = " + b).append("\n");
            i++;
        }
        return sb.toString();
    }
}
