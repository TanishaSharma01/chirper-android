package com.example.hackathon.sorteddata;

import java.util.Iterator;

/**
 * Maintains a set of data, with no duplicates, in sorted order.
 * @param <T> The type of data to be stored.
 */
public abstract class SortedData<T> {
	/**
	 Attempts to insert a value into the data structure.
	 @param value The value to insert.
	 @return true if successful, false if data structure already contains value.
	 */
	public abstract boolean insert(T value);

	/**
	 Attempts to find a value inside the data structure.
	 @param value The value to insert.
	 @return the element if found, and null otherwise.
	 */
	public abstract T get(T value);

	/**
	 * Gets a particular element, in sorted order.
	 * @param i the index of teh desired item
	 * @return the item at that index
	 */
	public abstract T getAtIndex(int i);

	/**
	 * Generates an Iterator that searches through the current state of the data structure.
	 * @param start the element from which to start looking
	 * @param count the maximum number of elements to search for.
	 * @param backwards in which direction to perform the iteration
	 * @return the Iterator
	 */
	public abstract Iterator<T> getRange(T start, int count, boolean backwards);

	/**
	 * Creates an iterator that goes through each element in the data structure in order.
	 * @return the Iterator
	 */
	public Iterator<T> getAll() {
		return getRange(null, -1, false);
	}

	/**
	 * Uniformly selects a random element from the data structure.
	 * @return null if the structure is empty, otherwise a randomly selected element.
	 */
	public abstract T getRandom();

	/**
	 * Returns an iterable view of all items in the collection.
	 * This is the corrected method, without the 'default' keyword.
	 */
	public Iterable<T> getAllIterable() {
		return this::getAll;
	}
}
