package jade;

import java.util.Iterator;

/**
 * just a wrapper for arrays that implements the iterable interace, so we don't have to copy arrays to iterable types
 * @author cody
 *
 */
public class IterableArray implements Iterable {

	Object[] array;

	public IterableArray(Object[] array) {
		this.array = array;
	}

	@Override
	public Iterator iterator() {
		return new ArrayIterator(array);
	}
	
	class ArrayIterator implements Iterator {

		Object[] array;
		int i;
		
		public ArrayIterator(Object[] array) {
			this.array = array;
			i = 0;
		}
		
		@Override
		public boolean hasNext() {
			return i < array.length;
		}

		@Override
		public Object next() {
			return array[i++];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove method not supported for ArrayIterable");
		}
	}
}
