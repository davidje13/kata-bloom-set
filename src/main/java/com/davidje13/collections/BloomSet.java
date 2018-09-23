package com.davidje13.collections;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractCollection;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * A set which uses fixed memory and has constant lookup time, but may report
 * false positives when checking membership (though never false negatives).
 *
 * Generates a number of hashes for each item and stores the results in a bit-
 * set. Allows trading memory usage for accuracy.
 *
 * Supports String membership using an MD5 hash.
 */
public class BloomSet extends AbstractCollection<String> implements Set<String> {
	/**
	 * Calculates the idealised false-positive rate for a given configuration.
	 *
	 * @param items  the number of items expected to be in the set
	 * @param bits   the number of bits allocated to the set
	 * @param hashes the number of hashes used in the set
	 * @return a value from 0 (no false positives) to 1 (always false positives)
	 */
	public static double expectedFalsePositiveRatio(int items, int bits, int hashes) {
		// http://pages.cs.wisc.edu/~cao/papers/summary-cache/node8.html
		return Math.pow(1.0 - Math.pow(1.0 - 1.0 / bits, hashes * (double) items), hashes);
	}

	/**
	 * Calculates the best hash count to use to minimise idealised false-
	 * positives for a given configuration.
	 *
	 * @param items  the number of items expected to be in the set
	 * @param bits   the number of bits allocated to the set
	 * @return the optimum number of hashes to use
	 */
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

	/**
	 * Creates a new BloomSet optimised for the given constraints.
	 *
	 * @param bits         the amount of memory to allocate
	 * @param expectedSize the expected number of items
	 * @return an optimal BloomSet for the given configuration
	 */
	public static BloomSet withMemoryAndExpectedSize(int bits, int expectedSize) {
		return new BloomSet(bits, idealHashCount(expectedSize, bits));
	}

	private BitSet internal;
	private final MessageDigest md5;
	private final int[] bucketsCache;

	/**
	 * Create a BloomSet with specific configuration.
	 *
	 * Actual memory usage may differ slightly, and will typically be in
	 * multiples of 64 bits.
	 *
	 * @param bits      the amount of memory to allocate
	 * @param hashCount the number of hashes to use
	 * @see BloomSet#withMemoryAndExpectedSize(int, int)
	 */
	public BloomSet(int bits, int hashCount) {
		this.internal = new BitSet(bits);
		this.bucketsCache = new int[hashCount];
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Calculates the idealised false-positive rate for a given number of items.
	 *
	 * @param items the number of items expected to be in the set
	 * @return a value from 0 (no false positives) to 1 (always false positives)
	 * @see BloomSet#expectedFalsePositiveRatio(int, int, int)
	 */
	public double expectedFalsePositiveRatio(int items) {
		return expectedFalsePositiveRatio(items, internal.size(), bucketsCache.length);
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * Test membership of the given value.
	 *
	 * Due to the nature of bloom filters, this may report false positives, but
	 * will never report false negatives.
	 *
	 * @param value the value to test for membership
	 * @return {@code true} if the value appears to be in the set
	 * @see BloomSet#expectedFalsePositiveRatio(int)
	 */
	@Override
	public boolean contains(Object value) {
		if (!(value instanceof String)) {
			return false;
		}

		getHashes((String) value, bucketsCache);
		for (int bucket : bucketsCache) {
			if (!internal.get(bucket)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public boolean add(String value) {
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

	/**
	 * Adds all of the elements in the specified BloomSet to this set if
	 * they're not already present.
	 *
	 * @param  values a BloomSet with the same configuration as this one
	 * @return {@code true} if this set changed as a result of the call
	 *
	 * @throws IllegalArgumentException if the two sets do not have similar
	 *                                  configuration
	 * @see #add(Object)
	 */
	public boolean addAll(BloomSet values) {
		checkSimilar(values);
		int oldCount = internal.cardinality();
		internal.or(values.internal);
		return internal.cardinality() != oldCount;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec
	 * This implementation builds a new BloomSet with the given values then
	 * ANDs the resulting bit-set with its own.
	 */
	@Override
	public boolean retainAll(Collection<?> values) {
		BloomSet other = new BloomSet(internal.size(), bucketsCache.length);
		values.stream()
				.filter(String.class::isInstance)
				.forEach((o) -> other.add((String) o));
		return retainAll(other);
	}

	/**
	 * Retains only the elements in this set that are contained in the
	 * specified BloomSet.
	 *
	 * @param  values a BloomSet with the same configuration as this one
	 * @return {@code true} if this set changed as a result of the call
	 *
	 * @throws IllegalArgumentException if the two sets do not have similar
	 *                                  configuration
	 * @see #remove(Object)
	 */
	public boolean retainAll(BloomSet values) {
		checkSimilar(values);
		int oldCount = internal.cardinality();
		internal.and(values.internal);
		return internal.cardinality() != oldCount;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec
	 * This implementation resets the internal bloom bit-set. It is a constant-
	 * time operation.
	 */
	@Override
	public void clear() {
		internal.clear();
	}

	/**
	 * Returns the number of elements in this collection (not supported).
	 *
	 * @throws UnsupportedOperationException this is not supported by BloomSet
	 * @deprecated not supported
	 */
	@Override
	@Deprecated
	public int size() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns an iterator over the elements contained in this collection (not
	 * supported).
	 *
	 * @throws UnsupportedOperationException this is not supported by BloomSet
	 * @deprecated not supported
	 */
	@Override
	@Deprecated
	public Iterator<String> iterator() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof BloomSet)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		BloomSet o = (BloomSet) other;
		return (
				o.internal.equals(internal)
				&& o.bucketsCache.length == bucketsCache.length
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return internal.hashCode() + bucketsCache.length;
	}

	private void checkSimilar(BloomSet other) {
		if (
				other.internal.size() != internal.size()
				|| other.bucketsCache.length != bucketsCache.length
		) {
			throw new IllegalArgumentException("BloomSets are not compatible");
		}
	}
}
