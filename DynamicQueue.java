import java.util.NoSuchElementException;

public class DynamicQueue<T> {
	protected DynamicArray<T> front; // These fields may be renamed
	protected DynamicArray<T> rear; // The methods getFront() and getRear()
									// return them

	// Return the “front” dynamic array of outgoing elements for final testing
	// Target complexity: O(1)
	protected DynamicArray<T> getFront() {
		return front;
	}

	// Return the “rear” dynamic array of incoming elements for final testing
	// Target complexity: O(1)
	protected DynamicArray<T> getRear() {
		return rear;
	}

	// Workhorse constructor. Initialize variables.
	public DynamicQueue() {
		front = new DynamicArray<T>();
		rear = new DynamicArray<T>();
	}

	// Adds x to the rear of the queue
	// Target complexity: O(1)
	public void enqueue(T x) {
		// Add in the Rear of the Queue
		rear.add(x);
	}

	// Removes and returns the element at the front of the queue
	// Throws NoSuchElementException if this queue is empty.
	// Target complexity: O(n)
	public T dequeue() {
		if (isEmpty())
			throw new NoSuchElementException();
		T returnValue = null;
		// Check if front is empty
		if (front != null) {
			if (front.size() == 0) {
				while (rear != null && rear.size() != 0) {
					// Get the Last Element from Rear
					T ele = rear.get(rear.size() - 1);
					// Remove the Last Element
					rear.remove();
					// Push it in front of the queue
					front.add(ele);
				}
			}
			// Return the Value from front
			returnValue = front.get(front.size() - 1);
			// Remove value from front
			front.remove();
		}
		return returnValue;
	}

	// Returns true if the queue is empty
	public boolean isEmpty() {
		return front.size() == 0 && rear.size() == 0;
	}

	// Returns the size of the queue
	public int size() {
		return front.size() + rear.size();
	}

	// Create a pretty representation of the DynamicQueue.
	// Example:
	// [A, B, C, D]
	public String toString() {
		StringBuilder strToRet = new StringBuilder("[");
		// Iterate though front and Print all values
		while (front != null && front.size() != 0) {
			// Get the Last Element from front
			T ele = front.get(front.size() - 1);
			// Remove the Last Element
			front.remove();
			strToRet.append(ele + ", ");
		}
				
		// Get all values from rear and Push them in Front
		while (rear != null && rear.size() != 0) {
			// Get the Last Element from Rear
			T ele = rear.get(rear.size() - 1);
			// Remove the Last Element
			rear.remove();
			// Push it in front of the queue
			front.add(ele);
		}
		// Iterate though front and Print aa values
		while (front != null && front.size() != 0) {
			// Get the Last Element from front
			T ele = front.get(front.size() - 1);
			// Remove the Last Element
			front.remove();
			strToRet.append(ele + ", ");
		}
		// Remove the colon from end
		if (strToRet.toString().endsWith(", "))
			strToRet.replace(strToRet.lastIndexOf(", "), strToRet.lastIndexOf(", ")+1, "");
	
		strToRet.append("]");
			
		return strToRet.toString();
	}

	// Create a pretty representation of the DynamicQueue for debugging.
	// Example:
	// front.toString: [A, B]
	// rear.toString: [C, D]
	protected String toStringForDebugging() {
		StringBuilder strToRet = new StringBuilder();
		// Create Front and Rear Strings
		String frontStr = front.size()==1?front.toString().replace(",", ""):front.toString();
		String rearStr = rear.size()==1?rear.toString().replace(",", ""):rear.toString();
		// Append them in String Builder
		strToRet.append("front.toString: " + frontStr + "\n");
		strToRet.append("rear.toString: " + rearStr + "\n");
		// Return Final String
		return strToRet.toString();
	}
}
