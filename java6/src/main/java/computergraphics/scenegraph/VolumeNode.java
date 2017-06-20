package computergraphics.scenegraph;

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL2;

import computergraphics.datastructures.mesh.TriangleMesh;
import computergraphics.datastructures.mesh.Volume;
import computergraphics.math.Matrix;
import computergraphics.math.Vector;

public class VolumeNode extends InnerNode {
    
    private Volume volume;
    /**
     * List of child nodes.
     */
    private List<INode> childrenX;
    private List<INode> childrenY;
    private List<INode> childrenZ;
    
    public VolumeNode(Volume volume, Vector eye) {
        this.volume = volume;
        childrenX = createLeafNodes("x", eye);
        childrenY = createLeafNodes("y", eye);
        childrenZ = createLeafNodes("z", eye);
    }
    
    private List<INode> createLeafNodes(String axis, Vector eye) {
        ArrayList<INode> nodes = new ArrayList<>();
        List<TriangleMesh> triangleMeshes = volume.getBackToFrontMeshes(axis, eye);
        
        for (TriangleMesh tM : triangleMeshes) {
            nodes.add(new TriangleMeshNode(tM));
        }
        return nodes;
    }
    
    @Override
    public void traverse(GL2 gl, RenderMode mode, Matrix modelMatrix) {
        for (INode child : childrenX) {
            child.traverse(gl, mode, modelMatrix);
        }
        for (INode child : childrenY) {
            child.traverse(gl, mode, modelMatrix);
        }
        for (INode child : childrenZ) {
            child.traverse(gl, mode, modelMatrix);
        }
    }
    
    @Override
    public void timerTick(int counter) {
        for (INode child : childrenX) {
            child.timerTick(counter);
        }
        for (INode child : childrenY) {
            child.timerTick(counter);
        }
        for (INode child : childrenZ) {
            child.timerTick(counter);
        }
    }
    
    /**
     * Add new child node.
     **/
    public void addChild(String axis, INode child) {
        child.setParentNode(this);
        switch (axis) {
        case "x":
            childrenX.add(child);
            break;
        case "y":
            childrenY.add(child);
            break;
        case "z":
            childrenZ.add(child);
            break;
        }
    }
    
    public boolean removeChild(String axis, INode child) {
        switch (axis) {
        case "x":
            return childrenX.remove(child);
        case "y":
            return childrenY.remove(child);
        case "z":
            return childrenZ.remove(child);
        default:
            return false;
        }
    }
    
    public boolean hasChild(String axis, INode child) {
        switch (axis) {
        case "x":
            return childrenX.contains(child);
        case "y":
            return childrenY.contains(child);
        case "z":
            return childrenZ.contains(child);
        default:
            return false;
        }
    }
}
