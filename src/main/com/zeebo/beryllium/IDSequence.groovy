package com.zeebo.beryllium

import javax.naming.OperationNotSupportedException
import java.util.function.Consumer

/**
 * Insert-Delete Sequence
 *
 * User: eric
 * Date: 11/28/14
 */
class IDSequence implements Iterable<OperationalTransform>, Cloneable {

	List<OperationalTransform> insertions
	List<OperationalTransform> deletes

	IDSequence() {
		insertions = []
		deletes = []
	}

	@Override
	Iterator<OperationalTransform> iterator() {
		return new IDSequenceIterator(sequence: this)
	}

	@Override
	void forEach(Consumer<? super OperationalTransform> action) {
		iterator().forEachRemaining(action)
	}

	@Override
	Spliterator<OperationalTransform> spliterator() {
		throw new OperationNotSupportedException("No Spliterator defined.")
	}

	static class IDSequenceIterator implements Iterator<OperationalTransform> {

		private static def lists = ['insertions', 'deletes']

		def sequence

		int lidx
		int idx

		@Override
		boolean hasNext() {

			def sizes = lists.collect { sequence."$it".size() }

			return idx < sizes[lidx] || sizes.findIndexOf(lidx + 1) { it > 0 } >= 0
		}

		@Override
		OperationalTransform next() {

			def sizes = lists.collect { sequence."$it".size() }

			if (!(idx < sizes[lidx])) {
				lidx = sizes.findIndexOf(lidx + 1) { it > 0 }
				idx = 0
			}

			return sequence."${lists[lidx]}"[idx++]
		}

		@Override
		void remove() {
			sequence."${lists[lidx]}".remove(idx)
		}

		@Override
		void forEachRemaining(Consumer<? super OperationalTransform> action) {
			while(hasNext()) {
				action.accept(next())
			}
		}
	}
}
