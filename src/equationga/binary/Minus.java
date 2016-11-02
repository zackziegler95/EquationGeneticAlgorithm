package equationga.binary;

import equationga.Operator;

public class Minus extends BinaryOp {

    public Minus(Operator a1, Operator a2) {
        super(a1, a2);
    }
    
    @Override
    public Minus copy() {
        return new Minus(a1.copy(), a2.copy());
    }
    
    @Override
    public double getRes(double[] vars) {
        return a1.getRes(vars)-a2.getRes(vars);
    }
    
    @Override
    public String toString(double[] vars) {
        return "("+a1.toString(vars)+"-"+a2.toString(vars)+")";
    }
    
    @Override
    public String toSmallString() {
        //System.out.println(a1+", "+a1.getLeavesCount(true));
        String s1 = a1.getLeavesCount(true) == 0 ? ""+a1.getRes(null) : ""+a1.toSmallString();
        String s2 = a2.getLeavesCount(true) == 0 ? ""+a2.getRes(null) : ""+a2.toSmallString();
        return "("+s1+"-"+s2+")";
    }
}
