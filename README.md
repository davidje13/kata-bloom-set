# Bloom Set

Implementation of a kata to create a bloom filter for testing membership of a set.

---

The kata specification is here: http://codekata.com/kata/kata05-bloom-filters/

## Testing

```sh
./gradlew test
```

## Running

```sh
./gradlew installDist && ./build/install/bloom/bin/bloom /usr/share/dict/words
```

Once the program is running, enter words to stdin, then press Ctrl+D to stop.

Any words not in the dictionary will be echoed back.

---

or pipe input directly:

```sh
./gradlew installDist
./build/install/bloom/bin/bloom /usr/share/dict/words <<< 'these are good, buut theeese arenot'
```
