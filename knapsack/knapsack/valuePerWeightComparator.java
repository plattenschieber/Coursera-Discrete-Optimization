package knapsack;

import java.util.Comparator;


public class valuePerWeightComparator implements Comparator<Item>
{
	@Override
	public int compare(Item o1, Item o2) {
		if (o1.value/(double)o1.weight > o2.value/(double)o2.weight) return -1;
		else if (o1.value/(double)o1.weight < o2.value/(double)o2.weight) return 1;
		else return 0;
	}
}
