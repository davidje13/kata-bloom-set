package com.davidje13.collections;

import org.junit.Test;

import java.util.Set;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

public class BloomSetTest {
	private final BloomSet<String> bloomSet = new BloomSet<>();

	@Test
	public void bloomSet_implements_JavaUtilSet() {
		assertThat(bloomSet, instanceOf(Set.class));
	}

	@Test
	public void add_returnsTrueIfTheValueHasNotAlreadyBeenAdded_withHighProbability() {
		assertThat(bloomSet.add("abc"), equalTo(true));
	}

	@Test
	public void add_returnsFalseIfTheValueHasAlreadyBeenAdded() {
		bloomSet.add("abc");
		assertThat(bloomSet.add("abc"), equalTo(false));
	}

	@Test(expected = NullPointerException.class)
	public void add_rejectsNullValues() {
		bloomSet.add(null);
	}

	@Test
	public void contains_returnsTrueForItemsWhichHaveBeenAdded() {
		bloomSet.add("abc");
		assertThat(bloomSet.contains("abc"), equalTo(true));
	}

	@Test
	public void contains_returnsFalseForItemsWhichHaveNotBeenAdded_withHighProbability() {
		bloomSet.add("abc");
		assertThat(bloomSet.contains("def"), equalTo(false));
	}

	@Test
	@SuppressWarnings("SuspiciousMethodCalls")
	public void contains_returnsFalseForItemsOfTheWrongType() {
		bloomSet.add("abc");
		assertThat(bloomSet.contains(7), equalTo(false));
	}

	@Test
	public void contains_returnsFalseForNull() {
		bloomSet.add("abc");
		assertThat(bloomSet.contains(null), equalTo(false));
	}

	@Test
	public void containsAll_returnsTrueIfAllTestedItemsAreInTheSet() {
		bloomSet.add("abc");
		bloomSet.add("def");
		bloomSet.add("ghi");
		assertThat(bloomSet.containsAll(asList("abc", "def")), equalTo(true));
	}

	@Test
	public void containsAll_returnsFalseIfAnyTestedItemsAreNotInTheSet_withHighProbability() {
		bloomSet.add("abc");
		bloomSet.add("def");
		bloomSet.add("ghi");
		assertThat(bloomSet.containsAll(asList("abc", "nope")), equalTo(false));
	}

	@Test
	public void addAll_addsAllGivenItemsToTheSet() {
		bloomSet.addAll(asList("abc", "def", "ghi"));
		assertThat(bloomSet.contains("abc"), equalTo(true));
		assertThat(bloomSet.contains("def"), equalTo(true));
		assertThat(bloomSet.contains("ghi"), equalTo(true));
	}

	@Test
	public void addAll_returnsTrueIfAnyItemHasNotAlreadyBeenAdded_withHighProbability() {
		bloomSet.add("abc");
		bloomSet.add("ghi");
		assertThat(bloomSet.addAll(asList("abc", "def", "ghi")), equalTo(true));
	}

	@Test
	public void addAll_returnsFalseIfAllItemsHaveAlreadyBeenAdded() {
		bloomSet.add("abc");
		bloomSet.add("def");
		bloomSet.add("ghi");
		assertThat(bloomSet.addAll(asList("abc", "def", "ghi")), equalTo(false));
	}

	@Test(expected = NullPointerException.class)
	public void addAll_rejectsNullValues() {
		bloomSet.addAll(asList("abc", null, "ghi"));
	}

	@Test
	public void retainAll_keepsOnlySpecifiedValues() {
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
	public void retainAll_returnsTrueIfTheCollectionChanges() {
		bloomSet.add("abc");
		bloomSet.add("def");
		bloomSet.add("ghi");
		assertThat(bloomSet.retainAll(asList("def", "ghi", "jkl")), equalTo(true));
	}

	@Test
	public void retainAll_returnsFalseIfNothingChanges_withHighProbability() {
		bloomSet.add("def");
		bloomSet.add("ghi");
		assertThat(bloomSet.retainAll(asList("def", "ghi", "jkl")), equalTo(false));
	}

	@Test
	public void isEmpty_returnsTrueIfTheCollectionHasNoValues() {
		assertThat(bloomSet.isEmpty(), equalTo(true));
	}

	@Test
	public void isEmpty_returnsFalseIfTheCollectionHasAnyValues() {
		bloomSet.add("abc");
		assertThat(bloomSet.isEmpty(), equalTo(false));
	}

	@Test
	public void clear_resetsCollection() {
		bloomSet.add("abc");
		bloomSet.clear();
		assertThat(bloomSet.isEmpty(), equalTo(true));
		assertThat(bloomSet.contains("abc"), equalTo(false));
	}

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
