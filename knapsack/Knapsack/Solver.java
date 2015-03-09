package knapsack;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import knapsack.Item;
import knapsack.Node;


/**
 * The class <code>Solver</code> is an implementation of a greedy algorithm to solve the knapsack problem.
 *
 */
public class Solver {

    int numItems;
    int kpCapacity;
    int kpValue;
    int[] taken;
	Stack<Node> BBTree;
    Node solution;
    ArrayList<Item> items;

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
        taken = new int[numItems];
        items = new ArrayList<Item>();


        for(int i=1; i < numItems+1; i++){
          String line = lines.get(i);
          String[] parts = line.split("\\s+");
          
          Item item = new Item();
          item.id = i;
          item.value = Integer.parseInt(parts[0]);
          item.weight = Integer.parseInt(parts[1]);
          items.add(item);
        }

        // calculate an optimal knapsack solution with dynamic programming
        if ((long)numItems * (long)kpCapacity < 100000000L && isDP)
            kpValue = DPSolver();
        else {
            BBTree = new Stack<Node>();
            if(BBSolver() != null) {
            	kpValue = solution.accValue;
            	taken = solution.path;
            }
            else 
            	System.out.println("Error finding a solution.");
        }

        
        // prepare the solution in the specified output format
        System.out.println(kpValue+" 1");
        for(int i=0; i < numItems; i++){
            System.out.print(taken[i]+" ");
        }
        System.out.println("");        
    }


    private float calcBound (Node node)
    {
    	int totalWeight;
    	float result;
    	// cut the branch on non feasibility 
    	if (node.accWeight > kpCapacity) {
    		return 0;
    	}
    	// calculate a new bound for this node 
    	else {
    		totalWeight = node.accWeight;
    		result = node.accValue;
    		int j = node.level+1;
    		node.bound = node.accValue;
    		while (j<numItems && totalWeight + items.get(j).weight <= kpCapacity) {
    			totalWeight += items.get(j).weight;
    			result += items.get(j).value;
    			j++;
    		}
    		if (j<numItems)
    			// add a fraction of the last non-fitting item
    			result += (kpCapacity-node.accWeight) * items.get(j).value/(double)items.get(j).weight;
    	}
    	return result;
    }

    private Node BBSolver () {
    	// this is our root
        Node rootNode = new Node(numItems);
        // sort values by density
        items.sort(new valuePerWeightComparator());
        // calculate a best possible bound, when allowing for fractional items
        rootNode.bound = calcBound(rootNode);
        // add root to tree
        BBTree.push(rootNode);

        // traverse the tree
        while (!BBTree.isEmpty()) {
            Node node = BBTree.pop();
            int level = -1;

            // we expand the tree only in case of an good bound 
            if (node.level+1 < numItems && calcBound(node) > kpValue)
                level = node.level+1;
            // we are on the last level, check if it's the best solution so far
            else if(node.accWeight <= kpCapacity && node.accValue > kpValue){
                	kpValue = node.accValue;
                    solution = new Node(node);
                    continue;
            }
            // last level and not feasible or bounded by bound
            else continue;
            
            // check 'left' node (if item is added to knapsack)
            Node left = new Node(node.accValue + items.get(level).value, node.accWeight + items.get(level).weight, node.bound, level, node.path);
            left.path[level] = 1;
            // only add left node if the bound is bigger than the current value
            if (calcBound(left) > kpValue)
                BBTree.push(left);

            // check 'right' node (if items is not added to knapsack)
            Node right = new Node(node.accValue, node.accWeight, node.bound, level, node.path);
            right.path[level] = 0; // redundant, but readable
            // only add right node if the bound is bigger than the current value
            if (calcBound(right) > kpValue)
                BBTree.push(right);

        }
        // can be NULL 
        return solution;
    }

    private int DPSolver () {
        // generate dynamic table and fill second column //(leave the very first column with no item)
        int[][] table = new int[kpCapacity+1][numItems+1];
        kpValue = 0;

        // fill the table step-by-step
        for(int i=1; i <= numItems; i++){
            for (int j=0; j <= kpCapacity; j++) {
            	Item item = items.get(i-1);
                if (item.weight<=j) {
                    //                    old best value,  sum of current accumulatedWeight and best value with reduced capacity
                    table[j][i] = Math.max(table[j][i-1], 
                    					   table[j-item.weight][i-1] + item.value);
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
                kpValue += items.get(i-1).value;
                // remove capacity from knapsack
                currentCap -= items.get(i-1).weight;
            } 
            // this item hasn't been added to our knapsack
            else taken[i-1] = 0;
        }

        return kpValue;
    }
}
