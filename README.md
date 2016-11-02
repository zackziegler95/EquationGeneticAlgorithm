# EquationGeneticAlgorithm

EquationGeneticAlgorithm is a project that aims to use a genetic algorithm to generate an analytical equation for data. This is still very much a work in progress. First, a set of equations, each with many terms, are generated at random. Next, the genetic algorithm runs and the equations that best fit the data are selected for. Through crossover and mutation the top performing equations produce offspring that are a combination of the terms in these equations, with increased genetic diversity. This continues for a number of iterations, resulting in an optimized equation which can be used for extrapolation or interpolation.


Right now data is parsed from MLB games and NFL games, to use while testing. The data comes from http://www.pro-football-reference.com and http://www.baseball-reference.com/. 


### Map


build/ -class files
dist/ -jar files once built for release
nbproject/ -information for netbeans
src/ -source code
src/equationga/BinaryOp.java -abstract class for binary operators
src/equationga/Constant.java -class for constants
src/equationga/Div.java -class for division operator
src/equationga/Equation.java -equation class
src/equationga/EquationGA.java -main class, also contains GA logic
src/equationga/Minus.java -class for subtraction operator
src/equationga/Operator.java -interface that all operator types implement
src/equationga/Plus.java -class for addition operator
src/equationga/Times.java -class for multiplication operator
src/equationga/UnaryOp.java -abstract class for unary operators (really should be 0th order "operators")
src/equationga/Variable.java -class for variables


## Testing

To execute an example run, run the jar file dist/EquationGA.jar from the root directory:
java -jar dist/EquationGA.jar

To play with different setting and parameters you can start in EquationGA.java.


This has been tested with Java 1.8 on Linux Mint 17.3.
