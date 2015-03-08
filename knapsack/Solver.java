import java.io.*;
import java.util.List;
import java.util.ArrayList;

/**
 * The class <code>Solver</code> is an implementation of a greedy algorithm to solve the knapsack problem.
 *
 */
public class Solver {

    private int numItems;
    private int capacity;
    private int[] values;
    private int[] weights;
    private int[] taken;
    private int value;
    
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
        
        // get the temp file name
        for(String arg : args){
            if(arg.startsWith("-file=")){
                fileName = arg.substring(6);
            } 
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
        capacity = Integer.parseInt(firstLine[1]);

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
        value = 0;

        // calculate an optimal knapsack solution
        if ((long)numItems * (long)capacity < 100000000L)
            value = DPSolver();
        else 
            value = BBSolver();

        
        // prepare the solution in the specified output format
        System.out.println(value+" 1");
        for(int i=0; i < numItems; i++){
            System.out.print(taken[i]+" ");
        }
        System.out.println("");        
    }

    private class Node {
        private int value = 0;
        private int weight = 0;
        private int estimate = 0;
        private int level = 0;
        private int[] path;

        private Node () {}
        private Node (int _value, int _weight, int _estimate, int _level, int[] _path) {
            this.value = _value;
            this.weight = _weight;
            this.estimate = _estimate;
            this.level = _level;
            this.path = _path.clone();
        }
        private Node (Node _node) {
            this.value = _node.value;
            this.weight = _node.weight;
            this.estimate = _node.estimate;
            this.level = _node.level;
            this.path = _node.path.clone();
        }
    }

    private void calcEstimate (Node node)
    {
        // can be done faster: only update remaining tail 
        node.estimate = 0;
        for (int i=0; i<numItems; i++)
            // all choosen items with i<=level and ALL items comming
            if (i>node.level || node.path[i] == 1)
                node.estimate += values[i];
    }

    private int BBSolver () {
        return 0;
    }

    private int DPSolver () {
        // generate dynamic table and fill second column //(leave the very first column with no item)
        int[][] table = new int[capacity+1][numItems+1];
        int weight = 0;
        value = 0;

        // fill the table step-by-step
        for(int i=1; i <= numItems; i++){
            for (int j=0; j <= capacity; j++) {
                if (weights[i-1]<=j) {
                    //                    old best value,  sum of current weight and best value with reduced capacity
                    table[j][i] = Math.max(table[j][i-1],  table[j-weights[i-1]][i-1] + values[i-1]);
                }
                // if it doesn't fit, we take the best value so far
                else table[j][i] = table[j][i-1];
            }
        }

        // backtrace through the table
        int currentCap = capacity;
        for (int i=numItems; i > 0; i--){
            if (table[currentCap][i] > table[currentCap][i-1]){
                taken[i-1] = 1;
                value += values[i-1];
                weight += weights[i-1];
                // remove capacity from knapsack
                currentCap -= weights[i-1];
            } 
            // this item hasn't been added to our knapsack
            else taken[i-1] = 0;
        }

        return value;
    }
}
