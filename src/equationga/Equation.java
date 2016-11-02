package equationga;

import equationga.unary.Pow;
import equationga.leaf.Constant;
import equationga.leaf.Variable;
import equationga.binary.Times;
import equationga.binary.Div;
import equationga.binary.Minus;
import equationga.binary.Plus;
import java.util.ArrayList;

public class Equation {
    public Operator[] terms; // Fixed number of terms or variable?
    
    /**
     * Initialize an equation with constants
     * @param n Number of terms
     */
    public Equation(int n) {
        terms = new Operator[n];
        for (int i = 0; i < n; i++) {
            terms[i] = new Constant(EquationGA.r.nextInt(EquationGA.maxVal)+1);
        }
        //terms.add(new Constant(Math.random()*10));
    }
    
    /*public Equation(ArrayList<Operator> terms) {
        this.terms = new ArrayList<>();
        for (Operator o : terms) {
            this.terms.add(o.copy());
        }
    }*/
    
    /**
     * Copy an equation
     * @param e The copy whose contents you want to copy
     */
    public Equation(Equation e) {
        terms = new Operator[e.terms.length];
        for (int i = 0; i < e.terms.length; i++) {
            terms[i] = e.terms[i].copy();
        }
    }
    
    public Operator getTerm(int i) { // NOTE: doesn't get a copied version
        return terms[i];
    }
    
    public void setTerm(int i, Operator o) { // NOTE: doesn't get a copied version
        terms[i] = o;
    }
    
    public int getNumTerms() {
        return terms.length;
    }
    
    /**
     * Selects a random branch of the equation
     * @param termNum term index in the equation
     * @param rootPossible If the root branch can be selected
     * @param stop used to prevent inversion from inverting itself
     * @return The node of the selected branch
     */
    public Operator selectBranch(int termNum, boolean rootPossible, Operator stop) {
        // If rootPossible, the root branch can be selected
        // stop is to prevent inversion from inverting itself, stops looking after a certain operator
        if (termNum >= terms.length) {
            System.err.println("Error: Trying to generate a term out of the range of the equation");
            System.exit(1);
        }
        
        int h = terms[termNum].getHeight()-1;
        int mod = rootPossible ? 1 : 0;
        return terms[termNum].getRandomBranch(h+mod, mod, stop);
    }
    
    public void setOp(int termNum, Operator toFind, Operator replacement) {
        if (terms[termNum] == toFind) {
            terms[termNum] = replacement.copy();
        } else {
            terms[termNum].setOp(toFind, replacement);
        }
    }
    
    /**
     * Actually evaluate the value of the equation with given variables
     * @param vars The given variables
     * @return the scalar result
     */
    public double eval(double[] vars) {
        double res = 0;
        for (Operator o : terms) {
            res += o.getRes(vars);
        }
        return res;
    }
    
    /**
     * Change a term into a new random term
     * @param termNum The number to replace
     * @param numOp The number of operators the new term has
     */
    public void generateTerm(int termNum, int numOp) { // n = number of operators
        if (termNum >= terms.length) {
            System.err.println("Error: Trying to generate a term out of the range of the equation");
            System.exit(1);
        }
        
        for (int i = 0; i < numOp; i++) {
            double d = Math.random();
            Constant c1 = new Constant(EquationGA.r.nextInt(EquationGA.maxVal)+1);
            Constant c2 = new Constant(EquationGA.r.nextInt(EquationGA.maxVal)+1);

            Operator newOp;
            if (d < 1.0/6.0 && terms[termNum].getLeavesCount(false) > 2) { // Want to get more likely as the leave count increases?
                newOp = new Variable(EquationGA.r.nextInt(EquationGA.numVars));
            } else if (d < 2.0/6.0) {
                newOp = new Minus(c1, c2);
            } else if (d < 3.0/6.0) {
                newOp = new Plus(c1, c2);
            } else if (d < 4.0/6.0) {
                newOp = new Times(c1, c2);
            } else if (d < 5.0/6.0) { 
                newOp = new Div(c1, c2);
            } else {
                newOp = new Pow(c1, EquationGA.r.nextInt(10)+1);
            }
            
            changeRandomLeaf(termNum, newOp, false);
        }
    }
    
    public int getLeavesCount(int termNum, boolean changeVars) {
        return terms[termNum].getLeavesCount(changeVars);
    }
    
    public int getPowCount(int termNum) {
        return terms[termNum].getPowCount();
    }
    
    public int getNumOps() {
        int res = 0;
        for (Operator term : terms) {
            res += term.getNumOps();
        }
        return res;
    }
    
    /**
     * Get a random power of a term
     * @param termNum The index of the term
     * @return the Pow operator, if one exists
     */
    public Operator getRandomPow(int termNum) {
        int numPows = terms[termNum].getPowCount();
        if (numPows == 0) { // Might want to handle this
            System.err.println("Error: trying to get a random pow but there are no free pows");
            System.exit(1);
        }

        int powNum = (int)(Math.random()*numPows);
        
        if (terms[termNum].isPow()) {
            if (powNum == 0) {
                return terms[termNum];
            } else {
                powNum--;
            }
        }
        
        return terms[termNum].getPow(new MInt(powNum));
    }
    
    /**
     * Changes a constant or a variable to another operator.
     * @param termNum
     * @param o
     * @param changeVars true=change variables, false=only change constants
     */
    public void changeRandomLeaf(int termNum, Operator o, boolean changeVars) {
        if (terms[termNum].isConstant()) {
            terms[termNum] = o;
            return;
        }
        
        int numLeaves = terms[termNum].getLeavesCount(changeVars);
        if (numLeaves == 0) { // Might want to handle this
            System.err.println("Error: trying to add a random operator but there are no free leaves");
            System.exit(1);
        }

        int leafNum = (int)(Math.random()*numLeaves);
        terms[termNum].replaceLeaf(leafNum, o, changeVars);
    }
    
    /*public void addOp(Operator o, boolean replaceVars) {
        if (root.isConstant()) {
            root = o;
            return;
        }
        
        int n = root.getLeavesCount(replaceVars);
        if (n == 0) { // Might want to handle this
            //System.err.println("Error: trying to add a random operator but there are no free leaves");
            //System.exit(1);
        }
        
        int i = (int)(Math.random()*n);
        root.replaceLeaf(i, o, replaceVars);
    }
    
    public void addRandomOp(boolean replaceVars) {
        double d = Math.random();
        Constant c1 = new Constant(Math.random()*100+1);
        Constant c2 = new Constant(Math.random()*100+1);
        
        if (d < 1.0/5.0 && root.getLeavesCount(false) > 2) { // Want to get more likely as the leave count increases?
            addOp(new Variable(0), replaceVars);
        } else if (d < 2.0/5.0) {
            addOp(new Minus(c1, c2), replaceVars);
        } else if (d < 3.0/5.0) {
            addOp(new Plus(c1, c2), replaceVars);
        } else if (d < 4.0/5.0) {
            addOp(new Times(c1, c2), replaceVars);
        } else { 
            addOp(new Div(c1, c2), replaceVars);
        }
    }*/
    
    /**
     * One way to implement a fitness function, basically r^2
     * @param points A list of points, where each point has all the variables 
     * @return r^2, 0 = terrible, 1 = perfect
     */
    public double fitnessTest(ArrayList<double[]> points) {
        double mean = 0;
        for (int i = 0; i < points.size(); i++) {
            mean += points.get(i)[EquationGA.numVars];
        }
        mean /= points.size();
        
        double totSS = 0;
        double resSS = 0;
        
        for (int i = 0; i < points.size(); i++) {
            double predictedVal = eval(points.get(i));
            double y = points.get(i)[EquationGA.numVars];
            totSS += (y-mean)*(y-mean);
            resSS += (y-predictedVal)*(y-predictedVal);
        }
        
        return 1-resSS/totSS;
    }
    
    /**
     * Another fitness function
     * @param points A list of points, where each point has all the variables 
     * @param totSSCache Scales the result
     * @return r^2, 0 = terrible, 1 = perfect
     */
    public double fitnessTest2(ArrayList<double[]> points, double totSSCache) {
        double resSS = 0;
        
        for (int i = 0; i < points.size(); i++) {
            double predictedVal = eval(points.get(i));
            double y = points.get(i)[EquationGA.numVars];
            /*if (Double.isInfinite(predictedVal) || Double.isNaN(predictedVal)) {
                System.err.println("Error, predicted value problem");
                System.exit(1);
            } // We don't actually have to check this, it just works out.*/
            resSS += (y-predictedVal)*(y-predictedVal);
        }
        
        return Math.sqrt(totSSCache/resSS); // Best is very large, usually very small
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < terms.length-1; i++) {
            s.append(terms[i]);
            s.append("+\n");
        }
        s.append(terms[terms.length-1]);
        s.append("\n");
        return s.toString();
    }
    
    public String toSmallString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < terms.length-1; i++) {
            s.append(terms[i].toSmallString());
            s.append("+\n");
        }
        s.append(terms[terms.length-1].toSmallString());
        s.append("\n");
        return s.toString();
    }
}
