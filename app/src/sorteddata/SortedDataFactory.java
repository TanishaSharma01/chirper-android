package sorteddata;

import java.util.Comparator;

//#WEEK 3 !
/**
 * As of week 3, the only concrete class implementing SortedData is SortedArrayList.
 * Accordingly, every class that needs to store sorted data - such as UserDAO and PostDAO
 * which extend DAO, as well as Post (for its replies) - could invoke the constructor
 * of SortedArrayList manually.
 * However, suppose that another implementation of SortedData existed, as indeed it will
 * next week. If that implementation is superior, we would need to modify each of the
 * existing classes, because they are strongly dependent on the particular implementation.
 * By using this simple instance of the Factory design pattern, we can instead direct
 * each of UserDAO, PostDAO, and Post through this Factory, and centralise any necessary
 * changes to just the single line of code here.
 */
//#WEEK 4
/**
 * As of week 4, we now have an additional implementation of the Sorted Data interface,
 * namely AVLTree. By replacing just this one line of code below, we are able to change
 * the type of sorted data used across all the modelled DAO classes.
 */
//#WEEK END
public class SortedDataFactory {
	public static <T> SortedData<T> makeSortedData(Comparator<T> comparator) {
		//#WEEK 3 !
		return new sorteddata.sortedarraylist.SortedArrayList<>(comparator);
		//#WEEK 4
		//return new sorteddata.avltree.AVLTree<>(comparator);
		//#WEEK END
	}
}
