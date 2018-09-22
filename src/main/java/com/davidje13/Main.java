package com.davidje13;

import com.davidje13.collections.BloomSet;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws IOException {
		int sizeKb = 256;
		BloomSet set = BloomSet.withMemoryAndExpectedSize(
				sizeKb * 1024 * 8,
				250000
		);

		Files.lines(new File(args[0]).toPath(), StandardCharsets.UTF_8)
				.forEach((word) -> set.add(word.toLowerCase()));

		try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name())) {
			scanner
					.useDelimiter("[^a-zA-Z0-9]+")
					.tokens()
					.filter((word) -> !set.contains(word.toLowerCase()))
					.forEach(System.out::println);
		}
	}
}
