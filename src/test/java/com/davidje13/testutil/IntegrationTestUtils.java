package com.davidje13.testutil;

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

	public static Output getOutputFrom(Runnable runnable) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		PrintStream stdout = System.out;
		PrintStream stderr = System.err;

		try {
			System.setOut(new PrintStream(out));
			System.setErr(new PrintStream(err));

			runnable.run();

			return new Output(out.toString(), err.toString());
		} finally {
			System.setOut(stdout);
			System.setErr(stderr);
		}
	}

	public static class Output {
		public final String out;
		public final String err;

		private Output(String out, String err) {
			this.out = out;
			this.err = err;
		}
	}
}
