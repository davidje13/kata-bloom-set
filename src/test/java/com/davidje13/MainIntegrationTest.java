package com.davidje13;

import org.junit.Test;

import static com.davidje13.testutil.IntegrationTestUtils.getResource;
import static com.davidje13.testutil.IntegrationTestUtils.getStdOutFrom;
import static com.davidje13.testutil.IntegrationTestUtils.setStdInContent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MainIntegrationTest {
	@Test
	public void main_reportsWordsFromStdInNotFoundInWordListFile() {
		String input = "foo abc baz def";

		setStdInContent(input);
		String out = getStdOutFrom(this::runWithTestWordList);

		assertThat(out, equalTo("abc\ndef\n"));
	}

	@Test
	public void main_isNotCaseSensitive() {
		String input = "FOO";

		setStdInContent(input);
		String out = getStdOutFrom(this::runWithTestWordList);

		assertThat(out, equalTo(""));
	}

	@Test
	public void main_ignoresPunctuation() {
		String input = "foo,woo";

		setStdInContent(input);
		String out = getStdOutFrom(this::runWithTestWordList);

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

	private void runWithTestWordList() {
		try {
			Main.main(new String[]{
					getResource("word-list.txt").getPath()
			});
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
