package equationga.binary;

import equationga.MInt;
import equationga.Operator;

public abstract class BinaryOp implements Operator {
    protected Operator a1;
    protected Operator a2;
    
    public BinaryOp(Operator a1, Operator a2) {
        this.a1 = a1;
        this.a2 = a2;
    }
    
    @Override
    public int getLeavesCount(boolean changeVars) {
        return a1.getLeavesCount(changeVars)+a2.getLeavesCount(changeVars);
    }
    
    @Override
    public int getPowCount() {
        return a1.getPowCount()+a2.getPowCount();
    }
    
    @Override
    public int getHeight() {
        return 1+Math.max(a1.getHeight(), a2.getHeight());
    }
    
    @Override
    public int getNumOps() {
        return 1+a1.getNumOps()+a2.getNumOps();
    }
    
    @Override
    public Operator getRandomBranch(int h, int currentH, Operator stop) {
        if (this == stop) return null;
        
        double prob = 1.0*currentH/h;
        //System.out.println(this+", "+prob);
        if (Math.random() < prob) return this;
        
        double first = Math.random();
        
        if (first < 0.5) {
            Operator o1 = a1.getRandomBranch(h, currentH+1, stop);
            if (o1 != null) return o1;

            return a2.getRandomBranch(h, currentH+1, stop);
        } else {
            Operator o2 = a2.getRandomBranch(h, currentH+1, stop);
            if (o2 != null) return o2;

            return a1.getRandomBranch(h, currentH+1, stop);
        }
    }
    
    public Operator geta1() {
        return a1;
    }
    
    public Operator geta2() {
        return a2;
    }
    
    @Override
    public void setOp(Operator toFind, Operator replacement) {
        if (a1 == toFind) {
            a1 = replacement.copy();
        } else if (a2 == toFind) {
            a2 = replacement.copy();
        } else {
            a1.setOp(toFind, replacement);
            a2.setOp(toFind, replacement);
        }
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
        Operator res = a1.getPow(mi);
        if (res != null) return res;
        
        if (a2.isPow()) {
            if (mi.i == 0) {
                return a2;
            } else {
                mi.i--;
            }
        } 
        return a2.getPow(mi);
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
            i = a1.replaceLeaf(i, o, changeVars);
            if (i == -1) return -1;
        }
        
        if ((!changeVars && a2.isConstant()) || (changeVars && a2.isVariable())) {
            if (i == 0) {
                a2 = o.copy();
                return -1;
            } else {
                i--;
            }
        } else {
            i = a2.replaceLeaf(i, o, changeVars);
        }
        return i;
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
    public boolean isPow() {
        return false;
    }
    
    @Override
    public String toString() {
        return toString(null);
    }
}
