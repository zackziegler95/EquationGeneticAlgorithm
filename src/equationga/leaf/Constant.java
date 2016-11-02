package equationga.leaf;

public class Constant extends LeafOp {
    private double d;
    
    public Constant(double d) {
        super(true);
        this.d = d;
    }
    
    @Override
    public Constant copy() {
        return new Constant(d);
    }
    
    public double getD() {
        return d;
    }
    
    @Override
    public double getRes(double[] vars) {
        return d;
    }
    
    @Override
    public String toString(double[] vars) {
        return ""+d;
    }
}
