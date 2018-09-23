package com.davidje13.collections;

import org.junit.Test;

import java.util.Set;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

@SuppressWarnings("TypeMayBeWeakened")
public class BloomSetTest {
	private final BloomSet bloomSet =
			BloomSet.withMemoryAndExpectedSize(32 * 8, 10);

	@Test
	public void bloomSet_implementsJavaUtilSet() {
		assertThat(bloomSet, instanceOf(Set.class));
	}

	@Test
	public void add_returnsTrue_ifTheCollectionChanges() {
		assertThat(bloomSet.add("abc"), equalTo(true));
	}

	@Test
	public void add_returnsFalse_ifTheItemHasAlreadyBeenAdded() {
		bloomSet.add("abc");
		assertThat(bloomSet.add("abc"), equalTo(false));
	}

	@Test(expected = NullPointerException.class)
	public void add_rejectsNullItems() {
		bloomSet.add(null);
	}

	@Test
	@SuppressWarnings("SuspiciousMethodCalls")
	public void contains_returnsFalse_forItemsOfTheWrongType() {
		bloomSet.add("abc");
		assertThat(bloomSet.contains(7), equalTo(false));
	}

	@Test
	public void contains_returnsFalse_forNull() {
		bloomSet.add("abc");
		assertThat(bloomSet.contains(null), equalTo(false));
	}

	@Test
	public void contains_probablyReturnsFalse_ifItemIsNotFound() {
		bloomSet.add("abc");
		assertThat(bloomSet.contains("def"), equalTo(false));
	}

	@Test
	public void containsAll_returnsTrue_ifAllItemsAreFound() {
		bloomSet.add("abc");
		bloomSet.add("def");
		bloomSet.add("ghi");

		boolean contains = bloomSet.containsAll(asList("abc", "def"));

		assertThat(contains, equalTo(true));
	}

	@Test
	public void containsAll_probablyReturnsFalse_ifAnyItemIsNotFound() {
		bloomSet.add("abc");
		bloomSet.add("def");
		bloomSet.add("ghi");

		boolean contains = bloomSet.containsAll(asList("abc", "nope"));

		assertThat(contains, equalTo(false));
	}

	@Test
	public void addAll_addsAllGivenItemsToTheSet() {
		bloomSet.addAll(asList("abc", "def", "ghi"));

		assertThat(bloomSet.contains("abc"), equalTo(true));
		assertThat(bloomSet.contains("def"), equalTo(true));
		assertThat(bloomSet.contains("ghi"), equalTo(true));
	}

	@Test
	public void addAll_returnsTrue_ifTheCollectionChanges() {
		bloomSet.add("abc");
		bloomSet.add("ghi");

		boolean returned = bloomSet.addAll(asList("abc", "def", "ghi"));

		assertThat(returned, equalTo(true));
	}

	@Test
	public void addAll_returnsFalse_ifAllItemsHaveAlreadyBeenAdded() {
		bloomSet.add("abc");
		bloomSet.add("def");
		bloomSet.add("ghi");

		boolean returned = bloomSet.addAll(asList("abc", "def", "ghi"));

		assertThat(returned, equalTo(false));
	}

	@Test
	public void addAll_acceptsSimilarBloomSets() {
		bloomSet.add("abc");
		bloomSet.add("def");
		bloomSet.add("ghi");

		BloomSet bloomSet2 = new BloomSet(
				bloomSet.memoryUsageBits(),
				bloomSet.hashes()
		);
		bloomSet2.add("def");
		bloomSet2.add("ghi");
		bloomSet2.add("jkl");

		assertThat(bloomSet.addAll(bloomSet2), equalTo(true));

		assertThat(bloomSet.contains("abc"), equalTo(true));
		assertThat(bloomSet.contains("def"), equalTo(true));
		assertThat(bloomSet.contains("ghi"), equalTo(true));
		assertThat(bloomSet.contains("jkl"), equalTo(true));

		assertThat(bloomSet.addAll(bloomSet2), equalTo(false));
	}

	@Test(expected = IllegalArgumentException.class)
	public void addAll_rejectsDifferentBloomSets() {
		bloomSet.add("abc");
		bloomSet.add("def");
		bloomSet.add("ghi");

		BloomSet bloomSet2 = new BloomSet(1024, 2);
		bloomSet2.add("def");
		bloomSet2.add("ghi");
		bloomSet2.add("jkl");

		bloomSet.addAll(bloomSet2);
	}

	@Test(expected = NullPointerException.class)
	public void addAll_rejectsNullItems() {
		bloomSet.addAll(asList("abc", null, "ghi"));
	}

	@Test
	public void retainAll_keepsOnlySpecifiedItems() {
		bloomSet.add("abc");
		bloomSet.add("def");
		bloomSet.add("ghi");

		bloomSet.retainAll(asList("def", "ghi", "jkl"));

		assertThat(bloomSet.contains("abc"), equalTo(false));
		assertThat(bloomSet.contains("def"), equalTo(true));
		assertThat(bloomSet.contains("ghi"), equalTo(true));
		assertThat(bloomSet.contains("jkl"), equalTo(false));
	}

	@Test
	public void retainAll_acceptsSimilarBloomSets() {
		bloomSet.add("abc");
		bloomSet.add("def");
		bloomSet.add("ghi");

		BloomSet bloomSet2 = new BloomSet(
				bloomSet.memoryUsageBits(),
				bloomSet.hashes()
		);
		bloomSet2.add("def");
		bloomSet2.add("ghi");
		bloomSet2.add("jkl");

		assertThat(bloomSet.retainAll(bloomSet2), equalTo(true));

		assertThat(bloomSet.contains("abc"), equalTo(false));
		assertThat(bloomSet.contains("def"), equalTo(true));
		assertThat(bloomSet.contains("ghi"), equalTo(true));
		assertThat(bloomSet.contains("jkl"), equalTo(false));

		assertThat(bloomSet.retainAll(bloomSet2), equalTo(false));
	}

	@Test(expected = IllegalArgumentException.class)
	public void retainAll_rejectsDifferentBloomSets() {
		bloomSet.add("abc");
		bloomSet.add("def");
		bloomSet.add("ghi");

		BloomSet bloomSet2 = new BloomSet(1024, 2);
		bloomSet2.add("def");
		bloomSet2.add("ghi");
		bloomSet2.add("jkl");

		bloomSet.retainAll(bloomSet2);
	}

	@Test
	public void retainAll_returnsTrue_ifTheCollectionChanges() {
		bloomSet.add("abc");
		bloomSet.add("def");
		bloomSet.add("ghi");

		boolean returned = bloomSet.retainAll(asList("def", "ghi", "jkl"));

		assertThat(returned, equalTo(true));
	}

	@Test
	public void retainAll_returnsFalse_ifNothingChanges() {
		bloomSet.add("def");
		bloomSet.add("ghi");

		boolean returned = bloomSet.retainAll(asList("def", "ghi", "jkl"));

		assertThat(returned, equalTo(false));
	}

	@Test
	public void isEmpty_returnsTrue_ifTheCollectionHasNoItems() {
		assertThat(bloomSet.isEmpty(), equalTo(true));
	}

	@Test
	public void isEmpty_returnsFalse_ifTheCollectionHasAnyItems() {
		bloomSet.add("abc");
		assertThat(bloomSet.isEmpty(), equalTo(false));
	}

	@Test
	public void clear_removesAllItems() {
		bloomSet.add("abc");

		bloomSet.clear();

		assertThat(bloomSet.isEmpty(), equalTo(true));
		assertThat(bloomSet.contains("abc"), equalTo(false));
	}

	@Test
	@SuppressWarnings("EqualsWithItself")
	public void equals_returnsTrue_forSelf() {
		bloomSet.add("abc");
		bloomSet.add("def");

		assertThat(bloomSet.equals(bloomSet), equalTo(true));
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	public void equals_returnsFalse_forNull() {
		assertThat(bloomSet.equals(null), equalTo(false));
	}

	@Test
	public void equals_returnsTrue_forSimilarSets() {
		BloomSet bloomSet1 = new BloomSet(128, 2);
		BloomSet bloomSet2 = new BloomSet(128, 2);
		bloomSet1.add("abc");
		bloomSet1.add("def");
		bloomSet2.add("def");
		bloomSet2.add("abc");

		assertThat(bloomSet1.equals(bloomSet2), equalTo(true));
	}

	@Test
	public void hashCode_isSame_forSimilarSets() {
		BloomSet bloomSet1 = new BloomSet(128, 2);
		BloomSet bloomSet2 = new BloomSet(128, 2);
		bloomSet1.add("abc");
		bloomSet1.add("def");
		bloomSet2.add("def");
		bloomSet2.add("abc");

		int hash1 = bloomSet1.hashCode();
		int hash2 = bloomSet2.hashCode();
		assertThat(hash1, equalTo(hash2));
	}

	@Test
	public void equals_returnsFalse_forDifferentConfiguration() {
		BloomSet bloomSet1 = new BloomSet(128, 2);
		BloomSet bloomSet2 = new BloomSet(128, 3);
		bloomSet1.add("abc");
		bloomSet1.add("def");
		bloomSet2.add("def");
		bloomSet2.add("abc");

		assertThat(bloomSet1.equals(bloomSet2), equalTo(false));
	}

	@Test
	public void hashCode_isProbablyDifferent_forDifferentConfiguration() {
		BloomSet bloomSet1 = new BloomSet(128, 2);
		BloomSet bloomSet2 = new BloomSet(128, 3);
		BloomSet bloomSet3 = new BloomSet(1024, 2);
		bloomSet1.add("abc");
		bloomSet1.add("def");
		bloomSet2.add("def");
		bloomSet2.add("abc");
		bloomSet3.add("def");
		bloomSet3.add("abc");

		int hash1 = bloomSet1.hashCode();
		int hash2 = bloomSet2.hashCode();
		int hash3 = bloomSet3.hashCode();
		assertThat(hash1, not(equalTo(hash2)));
		assertThat(hash2, not(equalTo(hash3)));
		assertThat(hash1, not(equalTo(hash3)));
	}

	@Test
	public void equals_probablyReturnsFalse_forDifferentItems() {
		BloomSet bloomSet1 = new BloomSet(128, 2);
		BloomSet bloomSet2 = new BloomSet(128, 2);
		bloomSet1.add("abc");
		bloomSet1.add("def");
		bloomSet2.add("abc");

		assertThat(bloomSet1.equals(bloomSet2), equalTo(false));
	}

	@Test
	public void hashCode_isProbablyDifferent_forDifferentItems() {
		BloomSet bloomSet1 = new BloomSet(128, 2);
		BloomSet bloomSet2 = new BloomSet(128, 2);
		bloomSet1.add("abc");
		bloomSet1.add("def");
		bloomSet2.add("abc");

		int hash1 = bloomSet1.hashCode();
		int hash2 = bloomSet2.hashCode();
		assertThat(hash1, not(equalTo(hash2)));
	}

	@SuppressWarnings("deprecation")
	@Test(expected = UnsupportedOperationException.class)
	public void size_isNotSupported() {
		bloomSet.size();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void remove_isNotSupported() {
		bloomSet.remove("abc");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void removeAll_isNotSupported() {
		bloomSet.removeAll(asList("abc", "def"));
	}

	@SuppressWarnings("deprecation")
	@Test(expected = UnsupportedOperationException.class)
	public void iterator_isNotSupported() {
		bloomSet.iterator();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void toArray_isNotSupported() {
		bloomSet.toArray();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void toArrayWithParameter_isNotSupported() {
		bloomSet.toArray(new String[0]);
	}
}
