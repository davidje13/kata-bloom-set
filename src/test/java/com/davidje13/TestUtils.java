package com.davidje13;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

public class TestUtils {
	public static long timeTakenMillis(Runnable runnable) {
		long begin = System.currentTimeMillis();
		runnable.run();
		long end = System.currentTimeMillis();

		return end - begin;
	}

	public static double averageTimeTakenMillis(int repetitions, Runnable runnable) {
		long begin = System.currentTimeMillis();
		for (int rep = 0; rep < repetitions; ++ rep) {
			runnable.run();
		}
		long end = System.currentTimeMillis();

		return (end - begin) / (double) repetitions;
	}

	public static long getMemoryUsage(int repetitions, Supplier<?> allocator) {
		Collection<Object> holder = new ArrayList<>(repetitions);
		long begin = getMemoryUsage();
		for (int rep = 0; rep < repetitions; ++ rep) {
			holder.add(allocator.get());
		}
		long end = getMemoryUsage();

		return (end - begin) / holder.size();
	}

	private static long getMemoryUsage() {
		System.gc();
		Runtime runtime = Runtime.getRuntime();
		return runtime.totalMemory() - runtime.freeMemory();
	}
}
