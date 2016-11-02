/**
 * The goal of this program is to use a genetic algorithm to generate an
 * analytical equation that fits data better than a simple regression can.
 * 
 * This is very much in a state of testing and research, it is not a finished
 * product.
 */

package equationga;

// Todo: make constant and variable "ops" 0 order instead of unary
// fix mutation so that it works even when there is only 1 or 2 leaves
// add simplifying algorithm so that things like $0/$0 is simplified to 1
// make sure it doesn't remove groups of constants though. Maybe this is not a good idea
// CrossOver and inversion need to not be allowed to make it smaller, they are cutting it up
// Also implement elitist selection: top 2 stay on.
// Remove solutions that lead to x/0 or 0/0

import equationga.binary.BinaryOp;
import equationga.binary.Div;
import equationga.binary.Minus;
import equationga.binary.Plus;
import equationga.binary.Times;
import equationga.leaf.Constant;
import equationga.leaf.Variable;
import equationga.unary.Pow;
import equationga.unary.UnaryOp;
import java.util.ArrayList;
import java.util.Random;


public class EquationGA {
    public static Random r = new Random();
    public static final int maxVal = 100;
    public static final int numVars = 7;
    
    /*private static ArrayList<double[]> points = new ArrayList(Arrays.asList(new double[][]{
        new double[]{1.01, 3}, {2.01, 7}, {3.01, 2}, {4.01, 20}, {5.01, 50}, {6.01, 60}, {7.01, 70}, 
        {8.01, 100}, {9.01, 60}, {10.01, 50}, {11.01, 200}, {12.01, 140}, {13.01, 180}, {14.01, 300},
        {15.01, 238}, {16.01, 300}, {17.01, 400}, {18.01, 300}, {19.01, 294}, {20.01, 500} 
    }));*/
    
    //private static double[] x = new double[]{1.01, 2.01, 3.01, 4.01, 5.01, 6.01, 7.01, 8.01, 9.01, 10.01, 11.01, 12.01, 13.01, 14.01, 15.01, 16.01, 17.01, 18.01, 19.01, 20.01};
    //private static double[] y = new double[]{3, 7, 2, 20, 50, 60, 70, 100, 60, 50, 200, 140, 180, 300, 238, 300, 400, 300, 294, 500};
    private static double totSSCache = 0;
    
    private static int numInversions = 0;
    private static int numInvIssues = 0;
    
    /**
     * Print the code to plot an n-th order polynomial
     * @param n The order of the polynomial
     */
    private static void printExcelPoly(int n) {
        String list = "{";
        for (int i = 1; i < n; i++) {
            list = list+i+",";
        }
        list = list+n+"}";
        
        String y = "";
        for (int i = 1; i <= n; i++) {
            y = y+"$Q$"+i+"*A1^"+(n+1-i)+"+";
        }
        y = y+"$Q$"+(n+1);
        
        System.out.println("1 =INDEX(LINEST($B$1:$B$20,$A$1:$A$20^"+list+"),1,P1) ="+y);
        for (int i = 2; i <= n+1; i++) {
            System.out.println(i+" =INDEX(LINEST($B$1:$B$20,$A$1:$A$20^"+list+"),1,P"+i+")");
        }
    }
    
    /**
     * From two equations, make the children for the next round
     * @param p1 Parent 1
     * @param p2 Parent 2
     * @return A list with all of the children
     */
    public static ArrayList<Equation> makeChildren(Equation p1, Equation p2) {
        Equation c1 = new Equation(p1);
        Equation c2 = new Equation(p2);
        
        // Crossover between the two parents
        crossOver(c1, c2);
        //inversion(c1);
        //inversion(c2);
        //System.out.println("Mutating");
        // More mutations should help increase sampling, but also kills good children sometimes
        for (int i = 0; i < 5; i++) {
            mutation(c1);
            mutation(c2);
        }
        //System.out.println("Done mutating");
        // perform a random genetic operator on each child: inversion, mutation, or neither
    	/*Random rand = new Random();
    	for(int i=0; i<numChildren; i++) {
            if(rand.nextBoolean())
                inversion(children.get(i));
            else if(rand.nextBoolean())
                mutation(children.get(i));
    	}*/
        
        /*Equation c1 = new Equation(4);
        Equation c2 = new Equation(4);
        for (int n = 0; n < 4; n++) { // Make each equation with 6 random operators
            c1.generateTerm(n, 30);
            c2.generateTerm(n, 30);
        }*/
        ArrayList<Equation> children = new ArrayList<>();
        children.add(c1);
        children.add(c2);
        return children;
    }
    
    /**
     * Take a random set of genes from parents 1, and random set from parent 2
     * @param e1 Parent 1
     * @param e2 Parent 2
     */
    public static void crossOver(Equation e1, Equation e2) {
        int termNum1 = r.nextInt(e1.getNumTerms());
        int termNum2 = r.nextInt(e2.getNumTerms());
        Operator termFrom1 = e1.getTerm(termNum1);
        Operator termFrom2 = e2.getTerm(termNum2);
        e1.setTerm(termNum1, termFrom2);
        e2.setTerm(termNum2, termFrom1);
    }
    
    /**
     * Swap two branches
     * @param e The equation in question
     */
    public static void inversion(Equation e) {
        numInversions++;
        // This should be fixed, make it so it can't invert itself
        //System.out.println("inversion"+e+", leaves: "+e.root.getLeavesCount());
        
        int termNum = r.nextInt(e.getNumTerms());
        
        Operator branch1 = e.selectBranch(termNum, false, null);
        //System.out.println(branch1);
        Operator branch2 = e.selectBranch(termNum, false, branch1);
        //System.out.println(branch2);
        
        if (branch2 == null) { // This shouldn't really be a problem, in the future just return
            //System.err.println("Error, didn't find a second branch");
            //System.exit(1);
            numInvIssues++;
            return;
        }
        System.out.println("setting ops");
        e.setOp(termNum, branch1, branch2);
        e.setOp(termNum, branch2, branch1);
        System.out.println("done");
    }
    
    /**
     * Another way to add genetic diversity, with mutations of the equation
     * @param e The equation in question
     */
    public static void mutation(Equation e) {
        // todo:
        // increase how much is changed each time this is called, i.e. more than just one thing
        // add different possibility for changes, like constant value, constant to var, var to const
        
        //System.out.println("mutation"+e+", leaves: "+e.root.getLeavesCount());
        double typeDet = r.nextDouble()*99;
        int termNum = r.nextInt(e.getNumTerms());
        
        if (typeDet < 33) { // Binary mutation: change one sign randomly
            Operator branch = null;
            
            //long t = System.currentTimeMillis();
            while (branch == null || !(branch instanceof BinaryOp)) {
                branch = e.selectBranch(termNum, true, null);
                /*System.out.println(branch);
                if (System.currentTimeMillis()-t > 3000) {
                    System.out.println(branch);
                }
                if (System.currentTimeMillis()-t > 4000) {
                    System.out.println(e);
                    System.err.println("Error, cannot find a binaryOp");
                }*/
            }

            BinaryOp b = (BinaryOp) branch;
            double d = Math.random();
            Operator a1 = b.geta1(); // Doesn't get copies
            Operator a2 = b.geta2();
            BinaryOp newBranch;

            if (d < 1.0/4.0) {
                newBranch = new Div(a1, a2);
            } else if (d < 2.0/4.0) {
                newBranch = new Minus(a1, a2);
            } else if (d < 3.0/4.0) {
                newBranch = new Plus(a1, a2);
            } else {
                newBranch = new Times(a1, a2);
            }
            e.setOp(termNum, branch, newBranch);
        } else if (typeDet < 66) { // Leaf mutations
            /*if (typeDet < 44 && e.getLeavesCount(termNum, true) > 0) { // Change a variable to a constant
                Constant c = new Constant(r.nextInt(maxVal)+1);
                e.changeRandomLeaf(termNum, c, true);
            } else if (typeDet < 55 && e.getLeavesCount(termNum, false) > 0) { // Change a constant's value
                Constant c = new Constant(r.nextInt(maxVal)+1);
                e.changeRandomLeaf(termNum, c, false);
            } else if (e.getLeavesCount(termNum, false) > 0) { // Change a constant to a variable
                Variable v = new Variable(r.nextInt(numVars));
                e.changeRandomLeaf(termNum, v, false);
            }*/
            if (typeDet < 41.25 && e.getLeavesCount(termNum, true) > 0) { // Change a variable to a constant
                Constant c = new Constant(r.nextInt(maxVal)+1);
                e.changeRandomLeaf(termNum, c, true);
            } else if (typeDet < 49.5 && e.getLeavesCount(termNum, false) > 0) { // Change a constant's value
                Constant c = new Constant(r.nextInt(maxVal)+1);
                e.changeRandomLeaf(termNum, c, false);
            } else if (typeDet < 57.75 && e.getLeavesCount(termNum, true) > 0) { // Change a variable's num
                Variable v = new Variable(r.nextInt(numVars));
                e.changeRandomLeaf(termNum, v, true);
            } else if (e.getLeavesCount(termNum, false) > 0) { // Change a constant to a variable
                Variable v = new Variable(r.nextInt(numVars));
                e.changeRandomLeaf(termNum, v, false);
            }
        } else { // Unary mutations
            if (typeDet < 77 && e.getPowCount(termNum) > 0) { // Remove a power
                UnaryOp pow = (UnaryOp)e.getRandomPow(termNum);
                Operator replacement = pow.geta1();
                e.setOp(termNum, pow, replacement);
            } else if (typeDet < 88 && e.getPowCount(termNum) > 0) { // Change a power's power
                UnaryOp pow = (UnaryOp)e.getRandomPow(termNum);
                Operator replacement = new Pow(pow.geta1(), r.nextInt(10)+1);
                e.setOp(termNum, pow, replacement);
            } else { // Add a power
                Operator branch = null;

                while (branch == null || branch instanceof Pow) {
                    branch = e.selectBranch(termNum, true, null); // Might not want the prob to increase
                }
                
                Operator pow = new Pow(branch, r.nextInt(10)+1);
                e.setOp(termNum, branch, pow);
            }
        }
    }
    
    public static void main(String[] args) {
        /*x = new double[20];
        y = new double[20];
        makeData();*/
        
        //printExcelPoly(16);
        
        NFLParser2 p = new NFLParser2(); // Get NFL data as a test
        ArrayList<double[]> allPoints = p.dataPoints;
        ArrayList<double[]> points = new ArrayList<>();
        System.out.println("Total number of data points: "+allPoints.size());
        
        for (int i = 0; i < allPoints.size(); i++) {
            int n = r.nextInt(allPoints.size());
            double[] point = allPoints.remove(n);
            for (int j = 0; j < point.length-1; j++) {
                System.out.print(point[j]+", ");
            }
            System.out.println(point[point.length-1]);
            points.add(point);
        }
        
        if (points.get(0).length != (numVars + 1)) {
            System.err.println("Error, numVars doesn't reflect the true number of variables");
            System.exit(1);
        }
        
        double mean = 0;
        for (int i = 0; i < points.size(); i++) {
            mean += points.get(i)[numVars];
        }
        mean /= points.size();
        
        for (int i = 0; i < points.size(); i++) {
            totSSCache += (points.get(i)[numVars]-mean)*(points.get(i)[numVars]-mean);
        }
        
        //Equation e = new Equation(new Times(new Constant(1.125), new Times(new Variable(0), new Variable(0))));
        //Equation e = new Equation(new Plus(new Times(new Variable(0), new Variable(0)), new Times(new Variable(0), new Variable(0))));
        //System.out.println(e+", "+e.fitnessTest(x, y));
        /*
        int numTerms = 3;
        Equation e1 = new Equation(numTerms);
        Equation e2 = new Equation(numTerms);
        for (int i = 0; i < numTerms; i++) { // Make each equation with 6 random operators
            e1.generateTerm(i, 10);
            e2.generateTerm(i, 10);
        }
        
        System.out.println(e1+", "+e1.eval(new double[]{5, 2, 3}));
        mutation(e1);
        System.out.println(e1);
        //System.out.println(e2+""+e2.getPowCount(0));
        /*e1.changeRandomLeaf(0, new Constant(5), false);
        e2.changeRandomLeaf(0, new Constant(5), true);
        System.out.println(e1+""+e1.getPowCount(0));
        System.out.println(e2+""+e2.getPowCount(0));*/
        
        long tStart = System.currentTimeMillis();
        // Controls the number of children in each generation
        int genSize = 20;
        if (genSize % 2 != 0) {
            System.err.println("Error: genSize must be even!");
            System.exit(1);
        }
        // Number of terms each equation has
        int numTerms = 7;
        //Number of operators that each term starts out with
        int numOps = 40;
        
        Equation[] children = new Equation[genSize];
        double[] rSq = new double[genSize]; //These are used to keep track of the scores for each equation
        double[] prob = new double[genSize]; // The prob of being selected, proportional to the rSq value
        
        for (int n = 0; n < genSize; n++) { // generation size
            Equation e = new Equation(numTerms);
            for (int i = 0; i < numTerms; i++) { // Make each equation with 6 random operators
                e.generateTerm(i, numOps);
            }
            children[n] = e;
        }
        
        int generations = 0;
        double bestNum; // Outside the loop to use in the whlie condition
        
        // Start advancing through generations
        do {
            System.out.println(generations);
            //double sum = 0; // We are keeping the sum because the prob of each = rSq/sum;
            for (int i = 0; i < genSize; i++) { // Take the fitness of each equation
                rSq[i] = children[i].fitnessTest2(points, totSSCache);
                while (Double.isNaN(rSq[i]) || Double.isInfinite(rSq[i]) || rSq[i] == 0) {
                    Equation e = new Equation(numTerms);
                    for (int n = 0; n < numTerms; n++) { // Make each equation with 6 random operators
                        e.generateTerm(n, numOps);
                    }
                    children[i] = e;
                    rSq[i] = e.fitnessTest2(points, totSSCache);
                }
                //sum += rSq[i];
            }
            
            //***NOTE***
            // I'm keeping this in here for now so that I can print out the best one, I'm not using it in selection
            bestNum = -999999; // The best and second best are stored so we can keep the two best children each generation
            int bestI = -1;
            double secBestNum = -999999;
            int secBestI = -1;
            
            for (int i = 0; i < genSize; i++) {
                //prob[i] = rSq[i]/sum; // Set the probability
                if (rSq[i] > bestNum) {
                    secBestNum = bestNum;
                    secBestI = bestI;
                    bestNum = rSq[i];
                    bestI = i;
                } else if (rSq[i] > secBestNum) {
                    secBestNum = rSq[i];
                    secBestI = i;
                }
                //System.out.println(rSq[i]+", "+prob[i]);
            }
            
            /*for (int i = 0; i < genSize; i++) {
                System.out.println(children[i]+""+rSq[i]);
            }COMMENT ME*/
            System.out.println(bestNum+", "+children[bestI].getNumOps());
            
            Equation[] newChildren = new Equation[genSize];
            newChildren[0] = children[bestI];
            newChildren[1] = children[secBestI];
            
            ArrayList<Integer> matingPool = new ArrayList<>();
            for (int n = 0; n < genSize; n++) {
                int mateI = r.nextInt(genSize);
                while (mateI == n) mateI = r.nextInt(genSize);
                
                if (rSq[n] > rSq[mateI]) {
                    matingPool.add(n);
                } else {
                    matingPool.add(mateI);
                }
            }
            
            for (int n = 1; n < genSize/2; n++) {
                int p1I = r.nextInt(matingPool.size());
                Equation p1 = children[matingPool.remove(p1I)];
                int p2I = r.nextInt(matingPool.size());
                Equation p2 = children[matingPool.remove(p2I)];
                ArrayList<Equation> newChildrenAL = makeChildren(p1, p2);
                newChildren[2*n] = newChildrenAL.get(0);
                newChildren[2*n+1] = newChildrenAL.get(1);
            }
            
            /*for (int n = 1; n < genSize/2; n++) {
                double[] newProb = new double[genSize];
                double newSum = sum;
                System.arraycopy(prob, 0, newProb, 0, genSize);
                
                int index1 = -1;
                
                double d = Math.random()*100;
                for (int i = 0; i < genSize; i++) {
                    d -= newProb[i]*100;
                    if (d <= 0) {
                        index1 = i;
                        newSum -= rSq[i];
                        break;
                    }
                }
                
                // Now redo the scores to account for the one taken out
                for (int i = 0; i < genSize; i++) {
                    if (index1 == i) newProb[i] = 0;
                    else newProb[i] = rSq[i]/newSum;
                }
                
                double pSum = 0;
                for (int i = 0; i < genSize; i++) {
                    //System.out.println(newProb[i]);
                    pSum += newProb[i];
                }
                //System.out.println("Sum after: "+pSum);
                
                int index2 = -1;
                
                d = Math.random()*100;
                for (int i = 0; i < genSize; i++) {
                    d -= newProb[i]*100;
                    if (d <= 0) {
                        index2 = i;
                        break;
                    }
                }
                
                if (index1 == index2 || index1 == -1 || index2 == -1) {
                    System.err.println("Error, index1 == index2, this shoulnd't happen. 1: "+index1+", 2: "+index2);
                    
                    System.exit(1);
                }
                ArrayList<Equation> newChildrenAL = makeChildren(children[index1], children[index2]);
                newChildren[2*n] = newChildrenAL.get(0);
                newChildren[2*n+1] = newChildrenAL.get(1);
            }*/
            
            children = newChildren;
            generations++;
        } while (generations < 500);//while (bestNum < 18 && generations < 20); 
        
        double bestRSq = -999999;
        Equation bestE = null;
        
        for (Equation e : children) {
            double d = e.fitnessTest(points);
            if (d > bestRSq) {
                bestRSq = d;
                bestE = e;
            }
            System.out.println(e.toSmallString()+", "+d+", "+e.fitnessTest2(points, totSSCache));
        }
        //System.out.println("Inversions: "+numInversions+", Inversion Issues: "+numInvIssues);
        System.out.println("Time: "+(System.currentTimeMillis()-tStart));
        //System.out.println(bestE+", "+bestRSq);
        
        /*Equation p1 = new Equation();
        Equation p2 = new Equation();
        for (int i = 0; i < 6; i++) { // Make each equation with 5 random operators
            p1.addRandomOp();
            p2.addRandomOp();
        }
        
        //System.out.println(p1+"="+p1.root.getRes(new double[]{5}));
        
        ArrayList<Equation> children = new ArrayList<>();
        int generations = 0;
        double bestRSq = -999999999;
        int bestEqIndex = 0;
        
        while (bestRSq < 0.95) {
            generations++;
            bestRSq = -999999999;
            bestEqIndex = 0;
            
            children = makeChildren(p1, p2);
            // find the most fit child
            for(int i=0; i<children.size(); i++) {
                //System.out.println(children.get(i));
                double rSq = children.get(i).fitnessTest(x, y);
                if(rSq > bestRSq) {
                    bestRSq = rSq;
                    bestEqIndex = i;
                }
            } // end for

            System.out.println(generations+", "+bestRSq);
            //System.out.println(bestRSq);
            
            // most fit child becomes a parent
            p1 = children.get(bestEqIndex);

            // a random child becomes the other parent
            Random rand = new Random();
            int randomChild = rand.nextInt(children.size());
            while(randomChild == bestEqIndex) {
                randomChild = rand.nextInt(children.size());
            }
            p2 = children.get(randomChild);

        }

        System.out.println(generations+" generations later...\n");
        System.out.print("MOST FIT CHILD\n----------------");
        System.out.println(children.get(bestEqIndex)); // print perfect equation
        */
        
        
        /*for (int n = 0; n < 10; n++) { // Make 10 equations to compare
            Equation e = new Equation();
            for (int i = 0; i < 5; i++) { // Make each equation with 5 random operators
                e.addRandomOp();
                //System.out.println(e);
            }
            double fit = e.fitnessTest(x, y);
            //System.out.println(e+", "+fit);
            e.selectBranch();
            if (fit > bestRSq) {
                bestRSq = fit;
                bestEq = e;
            }
        }
        */
        //Plus p = new Plus(new Times(new Constant(15), new Variable(0)), new Constant(5));
        //Equation e = new Equation(p);
        //System.out.println(e+", "+e.fitnessTest(x, y));
        
        //System.out.println(bestEq+", "+bestRSq);
        
        //Plus p = new Plus(new Constant(5), new Times(new Plus(new Constant(10), new Variable(0)), new Constant(2)));
        //double[] vars = new double[]{5};
        //System.out.println(p+"="+p.getRes(vars));
        
        
        /*p.replaceLeaf(3, new Constant(1));
        System.out.println(p+"="+p.getRes());*/
        
        //Equation e = new Equation(p);
        //e.addOp();
        //System.out.println(p+"="+p.getRes());
    }
}
