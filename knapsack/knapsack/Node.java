package knapsack;

public class Node {
    int accValue = 0;
    int accWeight = 0;
    float bound = 0;
    int level = -1;
    int[] path;

    // constructors
    public Node () {}
    public Node (int size) {
    	this.accValue = 0;
    	this.accWeight = 0;
    	this.bound = 0;
    	this.level = -1;
    	this.path = new int[size];
    }
    public Node (int _value, int _weight, float _bound, int _level, int[] _path) {
        this.accValue = _value;
        this.accWeight = _weight;
        this.bound = _bound;
        this.level = _level;
        this.path = _path.clone();
    }
    public Node (Node _node) {
        this.accValue = _node.accValue;
        this.accWeight = _node.accWeight;
        this.bound = _node.bound;
        this.level = _node.level;
        this.path = _node.path.clone();
    }
}

