package com.davidje13;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;

public class IntegrationTestUtils {
	public static URL getResource(String name) {
		ClassLoader loader = IntegrationTestUtils.class.getClassLoader();
		return loader.getResource(name);
	}

	public static void setStdInContent(String input) {
		System.setIn(new ByteArrayInputStream(input.getBytes()));
	}

	public static String getStdOutFrom(Runnable runnable) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream stdout = System.out;

		try {
			System.setOut(new PrintStream(out));
			runnable.run();
			return out.toString();
		} finally {
			System.setOut(stdout);
		}
	}
}
