package com.davidje13;

import com.davidje13.testutil.IntegrationTestUtils.Output;
import org.junit.Test;

import static com.davidje13.testutil.IntegrationTestUtils.getOutputFrom;
import static com.davidje13.testutil.IntegrationTestUtils.getResource;
import static com.davidje13.testutil.IntegrationTestUtils.setStdInContent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MainIntegrationTest {
	@Test
	public void main_reportsWordsFromStdInNotFoundInWordListFile() {
		String input = "foo abc baz def";

		setStdInContent(input);
		Output output = getOutputFrom(this::runWithTestWordList);

		assertThat(output.out, equalTo("abc\ndef\n"));
		assertThat(output.err, equalTo(""));
	}

	@Test
	public void main_isNotCaseSensitive() {
		String input = "FOO";

		setStdInContent(input);
		Output output = getOutputFrom(this::runWithTestWordList);

		assertThat(output.out, equalTo(""));
	}

	@Test
	public void main_ignoresPunctuation() {
		String input = "foo,woo";

		setStdInContent(input);
		Output output = getOutputFrom(this::runWithTestWordList);

		assertThat(output.out, equalTo("woo\n"));
	}

	@Test
	public void main_reportsAnErrorIfTheWordListIsNotFound() {
		String input = "foo,woo";

		setStdInContent(input);
		Output output = getOutputFrom(() -> Main.main(new String[]{"nope"}));

		assertThat(output.out, equalTo(""));
		assertThat(output.err, equalTo(
				"Failed to load word list from nope\n"
		));
	}

	@Test
	public void main_displaysUsage_ifCalledWithoutArguments() {
		Output output = getOutputFrom(() -> Main.main(new String[]{}));

		assertThat(output.out, equalTo(""));
		assertThat(output.err, equalTo(
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
		Main.main(new String[]{
				getResource("word-list.txt").getPath()
		});
	}
}
