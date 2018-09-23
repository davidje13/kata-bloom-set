package com.davidje13.collections;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

@SuppressWarnings("TypeMayBeWeakened")
public class BloomSetStatisticalTest {
	@Before
	public void generateDeterministicRandomMembership() {
		int memory = 96 * 1024 * 8; // 96kB
		int itemCount = 100000;

		List<String> values = IntStream.range(0, itemCount * 2)
				.mapToObj((v) -> "value-" + v)
				.collect(toList());
		Collections.shuffle(values, new Random(1234));

		members = values.subList(0, itemCount);
		nonmembers = values.subList(itemCount, itemCount * 2);

		bloomSet = BloomSet.withMemoryAndExpectedSize(memory, itemCount);
		bloomSet.addAll(members);
	}

	@Test
	public void contains_returnsTrue_forAddedItems_withCertainty() {
		double errorRate = countFailureRatio(
				bloomSet::contains,
				members
		);

		assertThat(errorRate, equalTo(0.0));
	}

	@Test
	public void contains_returnsFalse_forNonAddedItems_withHighProbability() {
		double errorRate = countFailureRatio(
				(v) -> !bloomSet.contains(v),
				nonmembers
		);

		assertThat(errorRate, lessThan(0.05));
	}

	@Test
	public void expectedFalsePositiveRatio_givesAReasonableEstimate() {
		double errorRate = countFailureRatio(
				(v) -> !bloomSet.contains(v),
				nonmembers
		);

		double predicted = bloomSet.expectedFalsePositiveRatio(members.size());

		assertThat(predicted, greaterThan(errorRate * 0.5));
		assertThat(predicted, lessThan(errorRate * 2.0));
	}

	private <T> double countFailureRatio(Predicate<T> check, List<T> values) {
		long failures = values.stream()
				.filter((value) -> !check.test(value))
				.count();

		return failures / (double) values.size();
	}

	private BloomSet bloomSet;
	private List<String> members;
	private List<String> nonmembers;
}
