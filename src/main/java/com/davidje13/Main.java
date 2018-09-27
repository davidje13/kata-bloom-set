package com.davidje13;

import com.davidje13.collections.BloomSet;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		if (args.length == 0) {
			showUsage();
			return;
		}

		int sizeKb = 256;
		BloomSet set = BloomSet.withMemoryAndExpectedSize(
				sizeKb * 1024 * 8,
				250000
		);

		Charset utf8 = StandardCharsets.UTF_8;
		try {
			Files.lines(new File(args[0]).toPath(), utf8)
					.forEach((word) -> set.add(word.toLowerCase()));
		} catch (IOException e) {
			System.err.println("Failed to load word list from " + args[0]);
			return;
		}

		try (Scanner scanner = new Scanner(System.in, utf8.name())) {
			scanner
					.useDelimiter("[^a-zA-Z0-9]+")
					.tokens()
					.filter((word) -> !set.contains(word.toLowerCase()))
					.forEach(System.out::println);
		}
	}

	private static void showUsage() {
		System.err.println("Performs spell-checking against a given");
		System.err.println("dictionary using a bloom set.");
		System.err.println();
		System.err.println("Usage:");
		System.err.println("  ./program <path_to_word_list>");
		System.err.println("  - provide words to check to stdin");
		System.err.println("  - non-matching words are reported to stdout");
	}
}
