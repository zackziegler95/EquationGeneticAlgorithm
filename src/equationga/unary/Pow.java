package equationga.unary;

import equationga.Operator;

public class Pow extends UnaryOp {
    private int exp;
    
    public Pow(Operator a1, int exp) {
        super(a1);
        
        if (a1 == null) {
            System.out.println("Error, a1 == null for pow, this shouldn't happen");
            System.exit(1);
        }
        this.exp = exp;
    }
    
    @Override
    public Pow copy() {
        return new Pow(a1.copy(), exp);
    }
    
    @Override
    public double getRes(double[] vars) {
        double preRes = a1.getRes(vars);
        double res = preRes;
        for (int i = 1; i < exp; i++) {
            res *= preRes;
        }
        return res;
    }
    
    @Override
    public String toString(double[] vars) {
        return "("+a1.toString(vars)+"^"+exp+")";
    }
    
    @Override
    public String toSmallString() {
        String s1 = a1.getLeavesCount(true) == 0 ? ""+a1.getRes(null) : ""+a1.toSmallString();
        return "("+s1+"^"+exp+")";
    }

    @Override
    public boolean isPow() {
        return true;
    }
}
