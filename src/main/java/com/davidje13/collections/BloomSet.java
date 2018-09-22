package com.davidje13.collections;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractCollection;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class BloomSet<T> extends AbstractCollection<T> implements Set<T> {
	public static double expectedFalsePositiveRatio(int items, int bits, int hashes) {
		// http://pages.cs.wisc.edu/~cao/papers/summary-cache/node8.html
		return Math.pow(1.0 - Math.pow(1.0 - 1.0 / bits, hashes * (double) items), hashes);
	}

	public static int idealHashCount(int items, int bits) {
		double ideal = Math.log(2.0) * bits / (double) items;
		if (ideal <= 1) {
			return 1;
		}
		int option1 = (int) Math.floor(ideal);
		int option2 = (int) Math.ceil(ideal);
		double fpRatio1 = expectedFalsePositiveRatio(items, bits, option1);
		double fpRatio2 = expectedFalsePositiveRatio(items, bits, option2);
		if (fpRatio1 <= fpRatio2) {
			return option1;
		} else {
			return option2;
		}
	}

	public static <T> BloomSet<T> withMemoryAndExpectedSize(int bits, int expectedSize) {
		return new BloomSet<>(bits, idealHashCount(expectedSize, bits));
	}

	private BitSet internal;
	private final MessageDigest md5;
	private final int[] bucketsCache;

	public BloomSet(int bits, int hashCount) {
		this.internal = new BitSet(bits);
		this.bucketsCache = new int[hashCount];
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public double expectedFalsePositiveRatio(int items) {
		return expectedFalsePositiveRatio(items, internal.size(), bucketsCache.length);
	}

	@Override
	public boolean isEmpty() {
		return internal.isEmpty();
	}

	private void getHashes(String value, int[] target) {
		md5.reset();
		byte[] digest = md5.digest(value.getBytes(StandardCharsets.UTF_8));

		int hashCount = target.length;
		int bucketCount = internal.size();

		for (int i = 0; i < hashCount; ++ i) {
			target[i] = 0;
		}
		for (int p = 0; p < digest.length; ++ p) {
			int i = p % hashCount;
			target[i] = target[i] * 256 + digest[p];
		}
		for (int i = 0; i < hashCount; ++ i) {
			int shift = i * bucketCount / hashCount;
			target[i] = Math.floorMod(target[i] + shift, bucketCount);
		}
	}

	private void getHashes(Object value, int[] target) {
		if (value instanceof String) {
			getHashes((String) value, target);
			return;
		}

		int bucketCount = internal.size();
		int hash = value.hashCode();

		for (int i = 0; i < target.length; ++ i) {
			target[i] = Math.floorMod(hash, bucketCount);
			hash /= bucketCount;
		}
	}

	@Override
	public boolean contains(Object value) {
		if (value == null) {
			return false;
		}

		getHashes(value, bucketsCache);
		for (int bucket : bucketsCache) {
			if (!internal.get(bucket)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean add(T value) {
		if (value == null) {
			throw new NullPointerException();
		}
		boolean changed = false;
		getHashes(value, bucketsCache);
		for (int bucket : bucketsCache) {
			if (!internal.get(bucket)) {
				internal.set(bucket);
				changed = true;
			}
		}
		return changed;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean retainAll(Collection<?> values) {
		BloomSet<T> other = new BloomSet<>(internal.size(), bucketsCache.length);
		other.addAll((Collection<T>) values);
		other.internal.and(internal);
		if (internal.equals(other.internal)) {
			return false;
		}
		internal = other.internal;
		return true;
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
