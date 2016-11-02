package equationga.leaf;

import equationga.MInt;
import equationga.Operator;

public abstract class LeafOp implements Operator {
    protected final boolean constant;
    
    public LeafOp(boolean constant) {
        this.constant = constant;
    }
    
    @Override
    public int getLeavesCount(boolean changeVars) {
        if (!changeVars && isConstant()) return 1;
        if (changeVars && isVariable()) return 1;
        return 0;
    }
    
    @Override
    public int getPowCount() {
        return 0;
    }
    
    @Override
    public int getHeight() {
        return 1;
    }
    
    @Override
    public int getNumOps() {
        return 1;
    }
    
    @Override
    public void setOp(Operator toFind, Operator replacement) {
        // Do nothing
    }
    
    @Override
    public Operator getRandomBranch(int h, int currentH, Operator stop) {
        if (this == stop) return null;
        double prob = 1.0*currentH/h;
        //System.out.println(this+", "+prob);
        if (Math.random() < prob) return this;
        return null;
    }
    
    @Override
    public int replaceLeaf(int i, Operator o, boolean changeVars) {
        return i;
    }
    
    @Override
    public Operator getPow(MInt mi) {
        return null;
    }
    
    @Override
    public boolean isConstant() {
        return constant;
    }
    
    @Override
    public boolean isVariable() {
        return !constant;
    }
    
    @Override
    public boolean isPow() {
        return false;
    }
    
    @Override
    public String toString() {
        return toString(null);
    }
    
    @Override
    public String toSmallString() {
        return toString(null);
    }
}
