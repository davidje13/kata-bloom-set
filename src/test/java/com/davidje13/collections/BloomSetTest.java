package com.davidje13.collections;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static com.davidje13.TestUtils.averageTimeTakenMillis;
import static com.davidje13.TestUtils.getMemoryUsage;
import static com.davidje13.TestUtils.timeTakenMillis;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;

@SuppressWarnings("TypeMayBeWeakened")
public class BloomSetTest {
	private final int TEST_MEMORY_KB = 96;
	private final int TEST_MEMBERSHIP = 100000;

	private final BloomSet bloomSet = BloomSet.withMemoryAndExpectedSize(
			TEST_MEMORY_KB * 1024 * 8,
			TEST_MEMBERSHIP
	);

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
	public void contains_returnsTrueForItemsWhichHaveBeenAdded_withCertainty() {
		Map<Boolean, List<String>> membership = generateDeterministicRandomMembership(TEST_MEMBERSHIP * 2);
		List<String> members = membership.get(true);

		bloomSet.addAll(members);

		double falseNegativeRatio = countFailureRatio(bloomSet::contains, members);
		assertThat(falseNegativeRatio, equalTo(0.0));
	}

	@Test
	public void contains_returnsFalseForItemsWhichHaveNotBeenAdded_withHighProbability() {
		Map<Boolean, List<String>> membership = generateDeterministicRandomMembership(TEST_MEMBERSHIP * 2);
		List<String> members = membership.get(true);
		List<String> nonmembers = membership.get(false);

		bloomSet.addAll(members);

		double falsePositiveRatio = countFailureRatio((v) -> !bloomSet.contains(v), nonmembers);
		assertThat(falsePositiveRatio, lessThan(0.05));
	}

	@Test
	public void expectedFalsePositiveRatio_givesAReasonableEstimate() {
		Map<Boolean, List<String>> membership = generateDeterministicRandomMembership(TEST_MEMBERSHIP * 2);
		List<String> members = membership.get(true);
		List<String> nonmembers = membership.get(false);

		bloomSet.addAll(members);

		double actualFalsePositiveRatio = countFailureRatio((v) -> !bloomSet.contains(v), nonmembers);

		double predicted = bloomSet.expectedFalsePositiveRatio(members.size());

		assertThat(predicted, greaterThan(actualFalsePositiveRatio * 0.5));
		assertThat(predicted, lessThan(actualFalsePositiveRatio * 2.0));
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

	@Test
	public void addAll_acceptsSimilarBloomSets() {
		bloomSet.add("abc");
		bloomSet.add("def");
		bloomSet.add("ghi");

		BloomSet bloomSet2 = BloomSet.withMemoryAndExpectedSize(
				TEST_MEMORY_KB * 1024 * 8,
				TEST_MEMBERSHIP
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
	public void retainAll_acceptsSimilarBloomSets() {
		bloomSet.add("abc");
		bloomSet.add("def");
		bloomSet.add("ghi");

		BloomSet bloomSet2 = BloomSet.withMemoryAndExpectedSize(
				TEST_MEMORY_KB * 1024 * 8,
				TEST_MEMBERSHIP
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

	@Test
	@SuppressWarnings("EqualsWithItself")
	public void equals_returnsTrueForSelf() {
		BloomSet bloomSet1 = new BloomSet(128, 2);
		bloomSet1.add("abc");
		bloomSet1.add("def");

		assertThat(bloomSet1.equals(bloomSet1), equalTo(true));
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	public void equals_returnsFalseForNull() {
		BloomSet bloomSet1 = new BloomSet(128, 2);
		bloomSet1.add("abc");
		bloomSet1.add("def");

		assertThat(bloomSet1.equals(null), equalTo(false));
	}

	@Test
	public void equals_returnsTrueForSimilarSets() {
		BloomSet bloomSet1 = new BloomSet(128, 2);
		BloomSet bloomSet2 = new BloomSet(128, 2);
		bloomSet1.add("abc");
		bloomSet1.add("def");
		bloomSet2.add("def");
		bloomSet2.add("abc");

		assertThat(bloomSet1.equals(bloomSet2), equalTo(true));
	}

	@Test
	public void hashCode_returnsSameValueForSimilarSets() {
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
	public void equals_returnsFalseForSetsWithDifferentConfiguration() {
		BloomSet bloomSet1 = new BloomSet(128, 2);
		BloomSet bloomSet2 = new BloomSet(128, 3);
		bloomSet1.add("abc");
		bloomSet1.add("def");
		bloomSet2.add("def");
		bloomSet2.add("abc");

		assertThat(bloomSet1.equals(bloomSet2), equalTo(false));
	}

	@Test
	public void hashCode_returnsDifferentValuesForSetsWithDifferentConfiguration_withHighProbabiltiy() {
		BloomSet bloomSet1 = new BloomSet(128, 2);
		BloomSet bloomSet2 = new BloomSet(128, 3);
		bloomSet1.add("abc");
		bloomSet1.add("def");
		bloomSet2.add("def");
		bloomSet2.add("abc");

		int hash1 = bloomSet1.hashCode();
		int hash2 = bloomSet2.hashCode();
		assertThat(hash1, not(equalTo(hash2)));
	}

	@Test
	public void equals_returnsFalseForSetsWithDifferentValues_withHighProbabiltiy() {
		BloomSet bloomSet1 = new BloomSet(128, 2);
		BloomSet bloomSet2 = new BloomSet(128, 2);
		bloomSet1.add("abc");
		bloomSet1.add("def");
		bloomSet2.add("abc");

		assertThat(bloomSet1.equals(bloomSet2), equalTo(false));
	}

	@Test
	public void hashCode_returnsDifferentValuesForSetsWithDifferentValues_withHighProbabiltiy() {
		BloomSet bloomSet1 = new BloomSet(128, 2);
		BloomSet bloomSet2 = new BloomSet(128, 2);
		bloomSet1.add("abc");
		bloomSet1.add("def");
		bloomSet2.add("abc");

		int hash1 = bloomSet1.hashCode();
		int hash2 = bloomSet2.hashCode();
		assertThat(hash1, not(equalTo(hash2)));
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

	@Test
	public void constructor_returnsQuickly() {
		double millis = averageTimeTakenMillis(100, () -> new BloomSet(1024 * 1024 * 8, 1));

		assertThat(millis, lessThan(2.0));
	}

	@Test
	public void addingLargeNumbersOfItems_runsQuickly() {
		long millis = timeTakenMillis(() -> {
			for(int i = 0; i < 1000000; ++i) {
				bloomSet.add("value-" + i);
			}
		});

		assertThat(millis, lessThan(1000L));
	}

	@Test
	public void addingLargeNumbersOfItems_consumesLittleMemory() {
		long bytes = getMemoryUsage(1000, () -> {
			BloomSet set = new BloomSet(32 * 8, 1);
			for(int i = 0; i < 1000; ++i) {
				bloomSet.add("value-" + i);
			}
			return set;
		});

		assertThat(bytes, lessThan(512L));
	}

	@Test
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void contains_returnsQuickly_evenWhenSetIsLarge() {
		seedLargeData();

		double millis = averageTimeTakenMillis(100000, () -> bloomSet.contains("value-1234567"));

		assertThat(millis, lessThan(0.001)); // 1 microsecond
	}

	@Test
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void isEmpty_runsQuickly_evenWhenSetIsLarge() {
		seedLargeData();

		double millis = averageTimeTakenMillis(100000, bloomSet::isEmpty);

		assertThat(millis, lessThan(0.001)); // 1 microsecond
	}

	private void seedLargeData() {
		for(int i = 0; i < TEST_MEMBERSHIP; ++i) {
			bloomSet.add("value-" + i);
		}
	}

	private Map<Boolean, List<String>> generateDeterministicRandomMembership(int totalSize) {
		List<String> values = IntStream.range(0, totalSize)
				.mapToObj((v) -> "value-" + v)
				.collect(toList());
		Collections.shuffle(values, new Random(1234));

		Map<Boolean, List<String>> result = new HashMap<>();
		result.put(true, values.subList(0, totalSize / 2));
		result.put(false, values.subList(totalSize / 2, totalSize));
		return result;
	}

	private <T> double countFailureRatio(Predicate<T> check, List<T> values) {
		int failures = 0;
		for(T value : values) {
			if (!check.test(value)) {
				++ failures;
			}
		}

		return failures / (double) values.size();
	}
}
