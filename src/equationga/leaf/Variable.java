package equationga.leaf;

public class Variable extends LeafOp {
    private int vNum;
    
    public Variable(int vNum) {
        super(false);
        this.vNum = vNum;
    }
    
    @Override
    public Variable copy() {
        return new Variable(vNum);
    }
    
    public int getVNum() {
        return vNum;
    }
    
    @Override
    public double getRes(double[] vars) {
        if (vars == null || vars.length <= vNum) {
            System.err.println("Error, variable list not long enough. Length: "+vars.length+", vNum: "+vNum);
            System.exit(1);
        }
        return vars[vNum];
    }
    
    @Override
    public String toString(double[] vars) {
        if (vars != null) {
            if (vars.length <= vNum) {
                System.err.println("Error, variable list not long enough. Length: "+vars.length+", vNum: "+vNum);
                System.exit(1);
            }
            return ""+vars[vNum];
        } else {
            return "$"+vNum;
        }
    }
}
