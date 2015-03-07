import java.io.*;
import java.util.List;
import java.util.ArrayList;

/**
 * The class <code>Solver</code> is an implementation of a greedy algorithm to solve the knapsack problem.
 *
 */
public class Solver {
    
    /**
     * The main class
     */
    public static void main(String[] args) {
        try {
            solve(args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Read the instance, solve it, and print the solution in the standard output
     */
    public static void solve(String[] args) throws IOException {
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
        int items = Integer.parseInt(firstLine[0]);
        int capacity = Integer.parseInt(firstLine[1]);

        int[] values = new int[items];
        int[] weights = new int[items];

        for(int i=1; i < items+1; i++){
          String line = lines.get(i);
          String[] parts = line.split("\\s+");

          values[i-1] = Integer.parseInt(parts[0]);
          weights[i-1] = Integer.parseInt(parts[1]);
        }

        // a trivial greedy algorithm for filling the knapsack
        // it takes items in-order until the knapsack is full
        int value = 0;
        int weight = 0;
        int[] taken = new int[items];

        // generate dynamic table and fill second column //(leave the very first column with no item)
        int[][] table = new int[capacity+1][items+1];

        // fill the table step-by-step
        for(int i=1; i <= items; i++){
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
        for (int i=items; i > 0; i--){
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
        
        // prepare the solution in the specified output format
        System.out.println(value+" 1");
        for(int i=0; i < items; i++){
            System.out.print(taken[i]+" ");
        }
        System.out.println("");        
    }
}
