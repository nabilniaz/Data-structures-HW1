
class DynamicArray<T> {
	protected Object[] arrayOfBlocks;
	protected final int DEFAULTCAPACITY = 4;
	protected int sizeOfArrayOfBlocks; // number of Blocks in arrayOfBlocks
	protected int size; // number of elements in DynamicArray
	protected int numberOfEmptyDataBlocks;
	protected int numberOfNonEmptyDataBlocks;
	protected int numberOfDataBlocks;
	protected int indexOfLastNonEmptyDataBlock;
	protected int indexOfLastDataBlock;
	protected int numberOfSuperBlocks;
	protected SuperBlock lastSuperBlock; // right-most SuperBlock

	// Workhorse constructor. Initialize variables, create the array
	// and the last SuperBlock, which represents SB0.
	DynamicArray() {
		// Create Array of Blocks
		arrayOfBlocks = new Object[DEFAULTCAPACITY];
		// Create the first Empty Block
		arrayOfBlocks[0] = new Block<T>(0, 1);
		
		// Initialize Variables
		sizeOfArrayOfBlocks = 1;
		size = 0;
		numberOfEmptyDataBlocks = 1;
		numberOfNonEmptyDataBlocks = 0;
		numberOfDataBlocks = numberOfEmptyDataBlocks + numberOfNonEmptyDataBlocks;
		indexOfLastNonEmptyDataBlock = -1;
		indexOfLastDataBlock = 0;
		numberOfSuperBlocks = 1;
		lastSuperBlock = new SuperBlock(0, 1, 1, 1);
	}

	// Returns the Location of element i, which is the index of the Block
	// and the position of i within that Block.
	protected Location locate(int index) {
		int p, b, e, maskE, maskB;
		int r = index + 1;
		// Get the Value of K
		int k = (int) Math.floor(log2(r));
		// Calculate the Value of p using k
		if (k % 2 == 0)
			p = (int) ( 2 * (Math.pow(2, Math.floor(k/2)) - 1) );
		else
			p = (int) ( ( 2 * (Math.pow(2, Math.floor(k/2)) - 1) ) + Math.pow(2, Math.floor(k/2)) );
		// Bit masks for b and e
		maskE = maskOfN( (int) Math.ceil( (double)k / (double)2 ) );
		maskB = maskOfN( (int) Math.floor(k/2) );
		// Calculate the Shift amount for calculation of b
		int shift = (int) (log2(r) - k/2);
		// Calculate e and b
		e = r & maskE;
		b = (r >> shift) & maskB;
		// Return the new Location found
		return new Location(p+b, e);
	}

	// Returns the Block at position i in arrayOfBlocks.
	// Target complexity: O(1)
	@SuppressWarnings("unchecked")
	protected Block<T> getBlock(int i) {
		return (Block<T>) arrayOfBlocks[i];
	}

	// Returns the element at position i in the DynamicArray.
	// Throws IllegalArgumentException if index < 0 or
	// index > size -1;
	// Target complexity: O(1)
	public T get(int i) {
		if (i < 0 || i > size - 1)
			throw new IllegalArgumentException("Index is out of range.");
		Location getLoc = locate(i);
		return getBlock(getLoc.blockIndex).getElement(getLoc.elementIndex);
	}

	// Sets the value at position i in DynamicArray to x.
	// Throws IllegalArgumentException if index < 0 or
	// index > size -1;
	// Target complexity: O(1)
	public void set(int index, T x) {
		if (index < 0 || index > size - 1)
			throw new IllegalArgumentException("Index is out of range.");
		Location getLoc = locate(index);
		getBlock(getLoc.blockIndex).setElement(getLoc.elementIndex, x);
	}

	// Allocates one more spaces in the DynamicArray. This may
	// require the creation of a Block and the last SuperBlock may change.
	// Also, expandArray is called if the arrayOfBlocks is full when
	// a Block is created.
	// Called by add.
	// Target complexity: O(1)
	@SuppressWarnings("unchecked")
	protected void grow() {
		// Get Last Non Empty Block
		Block<T> lastNonEmptyBlock = (Block<T>) arrayOfBlocks[indexOfLastDataBlock];
		
		if (lastNonEmptyBlock.size == lastNonEmptyBlock.capacity) {
			// Last Data Block is Full
			// 	a. Check if the Last Super Block is Full
			if (lastSuperBlock.currentNumberOfDataBlocks == lastSuperBlock.maxNumberOfDataBlocks) {
				numberOfSuperBlocks++;
				int maxNumberOfDataBlocks = 0;
				int maxNumberOfElementsPerBlock = 0;
				// Set the new Blocks and Elements Limits
				if (numberOfSuperBlocks % 2 == 1) {
					maxNumberOfDataBlocks = lastSuperBlock.getMaxNumberOfDataBlocks() * 2;
					maxNumberOfElementsPerBlock = lastSuperBlock.getMaxNumberOfElementsPerBlock();
				} else {
					maxNumberOfDataBlocks = lastSuperBlock.getMaxNumberOfDataBlocks();
					maxNumberOfElementsPerBlock = lastSuperBlock.getMaxNumberOfElementsPerBlock() * 2;
				}
				// Create the New Super Block		
				lastSuperBlock = new SuperBlock(numberOfSuperBlocks-1, maxNumberOfDataBlocks, maxNumberOfElementsPerBlock, 0);
			}
			
			//	b. If Array of Blocks is Full, Expand it
			if (sizeOfArrayOfBlocks == arrayOfBlocks.length) {
				// Array of Blocks is Full. Expand it.
				expandArray();
			}
			
			
			Block<T> newBlock = new Block<T>(numberOfDataBlocks, lastSuperBlock.getMaxNumberOfElementsPerBlock());
			// Add Block at the End
			arrayOfBlocks[sizeOfArrayOfBlocks] = newBlock;
			// Increment Current Data Blocks in Super Block
			lastSuperBlock.incrementCurrentNumberOfDataBlocks();
			// Update the Indexes
			indexOfLastNonEmptyDataBlock = sizeOfArrayOfBlocks;
			indexOfLastDataBlock = sizeOfArrayOfBlocks;
			// Increment Size of Array of Blocks
			sizeOfArrayOfBlocks++;
			// Increment the Total Number of Blocks
			numberOfDataBlocks++;
			// Update the Number of Blocks
			numberOfNonEmptyDataBlocks++;
			numberOfEmptyDataBlocks = 0;
			
			// Grow the Newly Created Block
			newBlock.grow();
			
			
		} else {
			// No Need to Create new Block, Just Grow It
			lastNonEmptyBlock.grow();
			// Update the Last Non Empty Data Block
			indexOfLastNonEmptyDataBlock = indexOfLastDataBlock;
			numberOfEmptyDataBlocks = 0;
			// Increment in case of first block only
			if (numberOfNonEmptyDataBlocks == 0)
				numberOfNonEmptyDataBlocks++;
		}		
	}

	// Grows the DynamicArray by one space, increases the size of the
	// DynamicArray, and sets the last element to x.
	// Target complexity: O(1)
	@SuppressWarnings("unchecked")
	public void add(T x) {
		// Grow the Dynamic Array
		grow();
		// Increment the Size
		size++;
		// Sets the Last element to x
		Block<T> lastBlock = (Block<T>) arrayOfBlocks[indexOfLastDataBlock];
		lastBlock.setElement(lastBlock.size() - 1, x);
	}

	// Write a null value to the last element, shrinks the DynamicArray by one
	// space, and decreases the size of the DynamicArray. A Block may be
	// deleted and the last SuperBlock may change.
	// Also, shrinkArray is called if the arrayOfBlocks is less than or equal
	// to a quarter full when a Block is deleted.
	// Throws IllegalStateException if the DynamicArray is empty when remove is
	// called.
	// Target complexity: O(1)
	public void remove() {
		// Throw Exception for Empty Array
		if (size == 0)
			throw new IllegalStateException();
		
		// Get the Last non-Empty Block
		Block<T> lastNonEmptyBlock = getBlock(indexOfLastNonEmptyDataBlock);
		lastNonEmptyBlock.setElement(lastNonEmptyBlock.size() - 1, null);
		lastNonEmptyBlock.shrink();
		// Reduce the Size
		size--;
		
		// Check if this Block is Empty
		if( lastNonEmptyBlock.size() == 0 ) {
			// Increment the Empty Data Blocks
			numberOfEmptyDataBlocks++;
			// Decrement the Non Empty Blocks
			numberOfNonEmptyDataBlocks--;
			// Decrement the index of Last Non Empty Block as well
			indexOfLastNonEmptyDataBlock--;
			
			// Check if any other Block is EMpty then Delete that Block
			if (numberOfEmptyDataBlocks > 1) {
				// Delete one Empty Block
				arrayOfBlocks[indexOfLastDataBlock--] = null;
				// Decrement the Size of Array of Blocks
				sizeOfArrayOfBlocks--;
				// If Array is Quarter Full. Shrink the Array to Half
				if (sizeOfArrayOfBlocks <= (arrayOfBlocks.length / 4))
					shrinkArray();
				// Decrement number of Blocks
				numberOfDataBlocks--;
				// Decrement number of Empty Blocks
				numberOfEmptyDataBlocks--;
				// Decrement Current Number of Data Blocks in Super Blocks
				lastSuperBlock.decrementCurrentNumberOfDataBlocks();
			}
			
			// Check if Super Block is Empty. Change the Reference of SB
			if (lastSuperBlock.getCurrentNumberOfDataBlocks() == 0) {
				// Decrement number of super Blocks
				numberOfSuperBlocks--;
				int maxNumberOfDataBlocks = 0;
				int maxNumberOfElementsPerBlock = 0;
				// Set the new Blocks and Elements Limits
				if (numberOfSuperBlocks % 2 == 0) {
					maxNumberOfDataBlocks = lastSuperBlock.getMaxNumberOfDataBlocks() / 2;
					maxNumberOfElementsPerBlock = lastSuperBlock.getMaxNumberOfElementsPerBlock();
				} else {
					maxNumberOfDataBlocks = lastSuperBlock.getMaxNumberOfDataBlocks();
					maxNumberOfElementsPerBlock = lastSuperBlock.getMaxNumberOfElementsPerBlock() / 2;
				}
				
				// Create the New Super Block That is Full	
				lastSuperBlock = new SuperBlock(numberOfSuperBlocks-1, maxNumberOfDataBlocks, maxNumberOfElementsPerBlock, maxNumberOfDataBlocks);
			}
		}
	}

	// Decreases the length of the arrayOfBlocks by half. Create a new
	// arrayOfBlocks and copy the Blocks from the old one to this new array.
	protected void shrinkArray() {
		// Array with half Length
		Object[] halfLengthArray = new Object[arrayOfBlocks.length / 2];
		for (int i = 0; i < halfLengthArray.length; i++) {
			halfLengthArray[i] = arrayOfBlocks[i];
		}
		// Save the Reference of new array in arrayOfBlocks
		arrayOfBlocks = halfLengthArray;
	}

	// Doubles the length of the arrayOfBlocks. Create a new
	// arrayOfBlocks and copy the Blocks from the old one to this new array.
	protected void expandArray() {
		// Double the length of Array
		Object[] newArrayOfBlocks = new Object[sizeOfArrayOfBlocks * 2];
		// Copy the Blocks from one array to Other
		for (int i = 0; i < sizeOfArrayOfBlocks; i++) {
			newArrayOfBlocks[i] = arrayOfBlocks[i];
		}
		// Save the Reference of new array in arrayOfBlocks
		arrayOfBlocks = newArrayOfBlocks;
	}

	// Returns the size of the DynamicArray which is the number of elements that
	// have been added to it with the add(x) method but not removed. The size
	// does not correspond to the capacity of the array.
	public int size() {
		return size;
	}

	// Returns the log base 2 of n
	protected static double log2(int n) {
		return (Math.log(n) / Math.log(2));
	}

	// Returns a mask of N 1 bits; this code is provided below and can be used
	// as is
	protected int maskOfN(int N) {
		int POW2ToN = 1 << N; // left shift 1 N places; e.g., 1 << 2 = 100 = 4
		int mask = POW2ToN - 1; // subtract 1; e.g., 1002 – 12 = 0112 = 3
		// Integer.toString(mask,2); // a String with the bits of mask
		return mask;
	}

	// Create a pretty representation of the DynamicArray. This method should
	// return string formatted similarly to ArrayList
	// Examples:
	// [] // 0 elements in DynamicArray
	// [X] // one element in DynamicArray
	// [A, B, C, D] // 4 elements in DynamicArray
	//
	// Target Complexity: O(N)
	// N: number of elements in the DynamicArray
	public String toString() {
		StringBuilder strToRet = new StringBuilder("[");
		// Iterate though array of Blocks and append all elements in StringBuilder
		for (int i = 0; i < sizeOfArrayOfBlocks; i++) {
			strToRet.append(getBlock(i).toString().trim().replace(" ", ", "));
			if (sizeOfArrayOfBlocks != 1 && i < sizeOfArrayOfBlocks - 1)
				strToRet.append(", ");		// Append Semicolon
		}
		strToRet.append("]");
		return strToRet.toString();
	}

	// Create a pretty representation of the DynamicArray for debugging
	// Example:
	// DynamicArray: A B
	// numberOfDataBlocks: 2
	// numberOfEmptyDataBlocks: 0
	// numberOfNonEmptyDataBlocks: 2
	// indexOfLastNonEmptyDataBlock: 1
	// indexOfLastDataBlock: 1
	// numberOfSuperBlocks: 2
	// lastSuperBlock: SB1
	// Block0: A
	// - capacity=1 size=1
	// Block1: B
	// - capacity=2 size=1
	// SB1:
	// - maxNumberOfDataBlocks: 1
	// - maxNumberOfElementsPerBlock: 2
	// - currentNumberOfDataBlocks: 1

	protected String toStringForDebugging() {
		StringBuilder strToRet = new StringBuilder();
		strToRet.append("DynamicArray: " + this.toString().replace("[", "").replace("]", "").replace(",", " ") + "\n");
		strToRet.append("numberOfDataBlocks: " + numberOfDataBlocks + "\n");
		strToRet.append("numberOfEmptyDataBlocks: " + numberOfEmptyDataBlocks + "\n");
		strToRet.append("numberOfNonEmptyDataBlocks: " + numberOfNonEmptyDataBlocks + "\n");
		strToRet.append("indexOfLastNonEmptyDataBlock: " + indexOfLastNonEmptyDataBlock + "\n");
		strToRet.append("indexOfLastDataBlock: " + indexOfLastDataBlock + "\n");
		strToRet.append("numberOfSuperBlocks: " + numberOfSuperBlocks + "\n");
		strToRet.append("lastSuperBlock: SB" + lastSuperBlock.getNumber() + "\n");
		for (int i = 0; i < sizeOfArrayOfBlocks; i++) {
			strToRet.append("Block" + getBlock(i).getNumber() + ": " + getBlock(i).toStringForDebugging() + "\n");
		}
		strToRet.append("SB" + lastSuperBlock.getNumber() + ":\n");
		strToRet.append("- maxNumberOfDataBlocks: " + lastSuperBlock.maxNumberOfDataBlocks + "\n");
		strToRet.append("- maxNumberOfElementsPerBlock: " + lastSuperBlock.maxNumberOfElementsPerBlock + "\n");
		strToRet.append("- currentNumberOfDataBlocks: " + lastSuperBlock.currentNumberOfDataBlocks + "\n");
		
		
		return strToRet.toString();
	}
}
