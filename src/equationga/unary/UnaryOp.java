package equationga.unary;

import equationga.MInt;
import equationga.Operator;

public abstract class UnaryOp implements Operator {
    protected Operator a1;
    
    public UnaryOp(Operator a1) {
        this.a1 = a1;
    }
    
    @Override
    public int getLeavesCount(boolean changeVars) {
        return a1.getLeavesCount(changeVars);
    }
    
    @Override
    public int getPowCount() {
        int i = this instanceof Pow ? 1 : 0;
        return a1.getPowCount()+i;
    }
    
    @Override
    public int getHeight() {
        return 1+a1.getHeight();
    }
    
    @Override
    public void setOp(Operator toFind, Operator replacement) { // Warning, no copying
        if (a1 == toFind) {
            a1 = replacement.copy();
        } else {
            a1.setOp(toFind, replacement);
        }
    }
    
    @Override
    public Operator getRandomBranch(int h, int currentH, Operator stop) {
        if (this == stop) {
            System.out.println("stopping b/c found op");
            return null;
        }
        double prob = 1.0*currentH/h;
        //System.out.println(this+", "+prob);
        if (Math.random() < prob) return this;
        Operator o1 = a1.getRandomBranch(h, currentH+1, stop);
        return o1;
    }
    
    @Override
    public int replaceLeaf(int i, Operator o, boolean changeVars) {
        if ((!changeVars && a1.isConstant()) || (changeVars && a1.isVariable())) {
            if (i == 0) {
                a1 = o.copy();
                return -1;
            } else {
                i--;
            }
        } else {
            i = a1.replaceLeaf(i, o, changeVars); // This is unary, the rest is 0th order
        }
        return i;
    }
    
    public Operator geta1() {
        return a1;
    }
    
    @Override
    public Operator getPow(MInt mi) {
        if (a1.isPow()) {
            if (mi.i == 0) {
                return a1;
            } else {
                mi.i--;
            }
        }
        return a1.getPow(mi);
    }
    
    @Override
    public int getNumOps() {
        return 1+a1.getNumOps();
    }
    
    @Override
    public boolean isConstant() {
        return false;
    }
    
    @Override
    public boolean isVariable() {
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
