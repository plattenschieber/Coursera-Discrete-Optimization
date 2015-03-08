package Knapsack;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.stream.IntStream;


/**
 * The class <code>Solver</code> is an implementation of a greedy algorithm to solve the knapsack problem.
 *
 */
public class Solver {

    private int numItems;
    private int kpCapacity;
    private int kpValue;
    private int[] values;
    private int[] weights;
    private int[] taken;
	private Stack<Node> BBTree;
    private Node solution;

    
    /**
     * The main class
     */
    public static void main(String[] args) {

        Solver solver = new Solver();
        try {
            solver.solve(args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Read the instance, solve it, and print the solution in the standard output
     */
    private void solve(String[] args) throws IOException {
        String fileName = null;
        boolean isDP = false;
        // get the temp file name
        for(String arg : args){
            if(arg.startsWith("-file=")){
                fileName = arg.substring(6);
            } 
            if (arg.equalsIgnoreCase("-dp"))
            	isDP = true;
        }
        if(fileName == null)
            return;
        
        // read the lines out of the file
        List<String> lines = new ArrayList<String>();

        BufferedReader input =  new BufferedReader(new FileReader(fileName));
        try {
            String line = null;
            while (( line = input.readLine()) != null){
                lines.add(line);
            }
        }
        finally {
            input.close();
        }
        
        
        // parse the data in the file
        String[] firstLine = lines.get(0).split("\\s+");
        numItems = Integer.parseInt(firstLine[0]);
        kpCapacity = Integer.parseInt(firstLine[1]);

        values = new int[numItems];
        weights = new int[numItems];

        for(int i=1; i < numItems+1; i++){
          String line = lines.get(i);
          String[] parts = line.split("\\s+");

          values[i-1] = Integer.parseInt(parts[0]);
          weights[i-1] = Integer.parseInt(parts[1]);
        }

        // a trivial greedy algorithm for filling the knapsack
        // it takes numItems in-order until the knapsack is full
        taken = new int[numItems];

        // calculate an optimal knapsack solution
        if ((long)numItems * (long)kpCapacity < 100000000L && !isDP)
            kpValue = DPSolver();
        else {
            BBTree = new Stack<Node>();
            kpValue = BBSolver();
            taken = solution.path;
        }

        
        // prepare the solution in the specified output format
        System.out.println(kpValue+" 1");
        for(int i=0; i < numItems; i++){
            System.out.print(taken[i]+" ");
        }
        System.out.println("");        
    }

    private class Node {
        private int accValue = 0;
        private int accWeight = 0;
        private int estimate = 0;
        private int level = -1;
        private int[] path;

        private Node () {}
        private Node (int size) {
        	this.accValue = 0;
        	this.accWeight = 0;
        	this.estimate = 0;
        	this.level = -1;
        	this.path = new int[size];
        }
        private Node (int _value, int _weight, int _estimate, int _level, int[] _path) {
            this.accValue = _value;
            this.accWeight = _weight;
            this.estimate = _estimate;
            this.level = _level;
            this.path = _path.clone();
        }
        private Node (Node _node) {
            this.accValue = _node.accValue;
            this.accWeight = _node.accWeight;
            this.estimate = _node.estimate;
            this.level = _node.level;
            this.path = _node.path.clone();
        }
    }

    private void calcEstimate (Node node)
    {
    	if (node.accWeight > kpCapacity) {
    		node.estimate = 0;
    		return;
    	}
        // in case we don't take the current node, remove its value 
        if (node.path[node.level] == 0)
        	node.estimate -= values[node.level];
    }

    private int BBSolver () {
        Node rootNode = new Node(numItems);
        // calculate the most basic estimate (relax the capacity constraint completely)
        rootNode.estimate = IntStream.of(values).sum();

        // add root to tree
        BBTree.push(rootNode);

        while (!BBTree.isEmpty()) {
            Node node = BBTree.pop();
            int level = -1;

            // we expand the tree only in case of an good estimate 
            if (node.level+1 < numItems && node.estimate > kpValue)
                level = node.level+1;
            // we are on the last level, check if it's the best solution so far
            else if(node.accWeight <= kpCapacity && node.accValue > kpValue){
                	kpValue = node.accValue;
                    solution = new Node(node);
                    continue;
            }
            // last level and not feasible or bounded by estimate
            else continue;
            
            // check 'left' node (if item is added to knapsack)
            Node left = new Node(node.accValue + values[level], node.accWeight + weights[level], node.estimate, level, node.path);
            left.path[level] = 1;
            // only add left node if the estimate is bigger than the current value
            calcEstimate(left);
            if (left.estimate > kpValue)
                BBTree.push(left);

            // check 'right' node (if items is not added to knapsack)
            Node right = new Node(node.accValue, node.accWeight, node.estimate, level, node.path);
            right.path[level] = 0; // redundant, but readable
            // only add right node if the estimate is bigger than the current value
            calcEstimate(right);
            if (right.estimate > kpValue)
                BBTree.push(right);

        }
        return solution.accValue;
    }

    private int DPSolver () {
        // generate dynamic table and fill second column //(leave the very first column with no item)
        int[][] table = new int[kpCapacity+1][numItems+1];
        kpValue = 0;

        // fill the table step-by-step
        for(int i=1; i <= numItems; i++){
            for (int j=0; j <= kpCapacity; j++) {
                if (weights[i-1]<=j) {
                    //                    old best value,  sum of current accumulatedWeight and best value with reduced capacity
                    table[j][i] = Math.max(table[j][i-1],  table[j-weights[i-1]][i-1] + values[i-1]);
                }
                // if it doesn't fit, we take the best value so far
                else table[j][i] = table[j][i-1];
            }
        }

        // backtrace through the table
        int currentCap = kpCapacity;
        for (int i=numItems; i > 0; i--){
            if (table[currentCap][i] > table[currentCap][i-1]){
                taken[i-1] = 1;
                kpValue += values[i-1];
                // remove capacity from knapsack
                currentCap -= weights[i-1];
            } 
            // this item hasn't been added to our knapsack
            else taken[i-1] = 0;
        }

        return kpValue;
    }
}
