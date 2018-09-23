package com.davidje13.collections;

import org.junit.Test;

import java.util.Collection;

import static com.davidje13.testutil.TestUtils.averageMemoryUsageBytes;
import static com.davidje13.testutil.TestUtils.averageTimeTakenMillis;
import static com.davidje13.testutil.TestUtils.timeTakenMillis;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;

public class BloomSetPerformanceTest {
	@Test
	public void addingItems_consumesConstantMemory() {
		int itemCount = 1024;

		long bytes = averageMemoryUsageBytes(1000, () -> {
			Collection<String> set = new BloomSet(32 * 8, 1);
			seedData(set, itemCount);
			return set;
		});

		assertThat(bytes, lessThan((long) itemCount / 2));
	}

	@Test
	public void constructingAndAddingLargeNumbersOfItems_runsQuickly() {
		int memory = 1024 * 1024 * 8; // 1MB

		long millis = timeTakenMillis(() -> {
			BloomSet bloomSet = new BloomSet(memory, 5);
			for(int i = 0; i < 1000000; ++i) {
				bloomSet.add("value-" + i);
			}
			assertThat(bloomSet.contains("value-0"), equalTo(true));
		});

		assertThat(millis, lessThan(1000L));
	}

	@Test
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void contains_returnsQuickly_evenWhenSetIsLarge() {
		BloomSet bloomSet = seededBloomSet();

		double millis = averageTimeTakenMillis(100000, () ->
				bloomSet.contains("value-1234567")
		);

		assertThat(millis, lessThan(0.001)); // 1 microsecond
	}

	@Test
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void isEmpty_runsQuickly_evenWhenSetIsLarge() {
		BloomSet bloomSet = seededBloomSet();

		double millis = averageTimeTakenMillis(100000,
				bloomSet::isEmpty
		);

		assertThat(millis, lessThan(0.001)); // 1 microsecond
	}

	private BloomSet seededBloomSet() {
		int itemCount = 100000;
		int memory = 96 * 1024 * 8; // 96kB

		BloomSet set = BloomSet.withMemoryAndExpectedSize(memory, itemCount);
		seedData(set, itemCount);
		return set;
	}

	private static void seedData(Collection<String> set, int count) {
		for(int i = 0; i < count; ++i) {
			set.add("value-" + i);
		}
	}
}
