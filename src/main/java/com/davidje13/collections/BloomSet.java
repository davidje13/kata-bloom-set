package com.davidje13.collections;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BloomSet<T> extends AbstractCollection<T> implements Set<T> {
	private final Collection<T> internal = new HashSet<>();

	@Override
	public boolean isEmpty() {
		return internal.isEmpty();
	}

	@Override
	public boolean contains(Object value) {
		return internal.contains(value);
	}

	@Override
	public boolean add(T value) {
		if (value == null) {
			throw new NullPointerException();
		}
		return internal.add(value);
	}

	@Override
	public boolean retainAll(Collection<?> values) {
		return internal.retainAll(values);
	}

	@Override
	public void clear() {
		internal.clear();
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<T> iterator() {
		throw new UnsupportedOperationException();
	}
}
