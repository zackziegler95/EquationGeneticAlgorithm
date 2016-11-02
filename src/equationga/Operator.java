package equationga;

public interface Operator {
    double getRes(double[] vars);
    int getLeavesCount(boolean changeVars); // get's either the number of constants or num of vars
    int getPowCount();
    int getNumOps();
    Operator getPow(MInt i);
    //int getVarsCount(); Don't know why this was here, also replaced by getLeavesCount(true)
    int replaceLeaf(int i, Operator o, boolean changeVars); // replaces either constants or variables
    boolean isConstant();
    boolean isVariable();
    boolean isPow();
    int getHeight();
    Operator getRandomBranch(int h, int currentH, Operator stop);
    Operator copy();
    void setOp(Operator toFind, Operator replacement);
    public String toString(double[] vars);
    @Override
    public String toString();
    public String toSmallString();
}
