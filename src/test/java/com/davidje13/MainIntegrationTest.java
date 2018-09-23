package com.davidje13;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MainIntegrationTest {
	@Test
	public void main_testsWordsFromStdInAgainstWordListInFile_reportingMismatches() {
		String input = "foo abc baz def";

		setStdInContent(input);
		String out = getStdOutFrom(() -> {
			try {
				Main.main(new String[]{getResource("wordlist.txt").getPath()});
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		});

		assertThat(out, equalTo("abc\ndef\n"));
	}

	@Test
	public void main_isNotCaseSensitive() {
		String input = "FOO";

		setStdInContent(input);
		String out = getStdOutFrom(() -> {
			try {
				Main.main(new String[]{getResource("wordlist.txt").getPath()});
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		});

		assertThat(out, equalTo(""));
	}

	@Test
	public void main_ignoresPunctuation() {
		String input = "foo,woo";

		setStdInContent(input);
		String out = getStdOutFrom(() -> {
			try {
				Main.main(new String[]{getResource("wordlist.txt").getPath()});
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		});

		assertThat(out, equalTo("woo\n"));
	}

	@Test
	public void main_displaysUsage_ifCalledWithoutArguments() {
		String out = getStdOutFrom(() -> {
			try {
				Main.main(new String[]{});
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		});

		assertThat(out, equalTo(
				"Performs spell-checking against a given\n" +
				"dictionary using a bloom set.\n" +
				"\n" +
				"Usage:\n" +
				"  ./program <path_to_word_list>\n" +
				"  - provide words to check to stdin\n" +
				"  - non-matching words are reported to stdout\n"
		));
	}

	private URL getResource(String name) {
		ClassLoader loader = getClass().getClassLoader();
		return loader.getResource(name);
	}

	private void setStdInContent(String input) {
		System.setIn(new ByteArrayInputStream(input.getBytes()));
	}

	private String getStdOutFrom(Runnable runnable) {
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
