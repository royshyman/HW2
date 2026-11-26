package il.ac.tau.cs.software1.inventory;

/*
 * A simple array-based Inventory (Question 6).
 */
public class NaiveInventory implements Inventory {

	private static final int INITIAL_CAPACITY = 100;

	// TODO: add the fields:
	//       - an array of Collectibles (it may be larger than the number of items actually stored)
	//       - an int that counts how many items are actually stored
	private Collectible[] arr;
	private int curr_items;

	public NaiveInventory() {
		arr = new Collectible[INITIAL_CAPACITY];
		curr_items = 0;
	}

	@Override
	public void collect(Collectible collectible) {
		if (arr.length == curr_items){
			Collectible[] new_arr = new Collectible[arr.length*2];
			for (int i=0;i<curr_items;i++)
				new_arr[i] = arr[i];
			arr = new_arr;
		}
		arr[curr_items] = collectible;
		curr_items++;
	}

	@Override
	public float getTotalWeight() {
		float total = 0.f;
		for (int i=0;i<curr_items;i++)
			total += arr[i].getWeight();
		return total;
	}

	@Override
	public int getCurrentCount() {
		return curr_items;
	}

	// Returns the array of Collectibles that is stored in the field of this class
	public Collectible[] getCollArr() {
		return arr;
	}
}
