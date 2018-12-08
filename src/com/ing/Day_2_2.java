package com.ing;

import lombok.AllArgsConstructor;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ToString
@AllArgsConstructor
class Tuple {
    String s1;
    String s2;

    public String commonCharacters() {
        return IntStream.range(0, s1.length())
                .filter(i -> s1.charAt(i) == s2.charAt(i))
                .mapToObj(c -> String.valueOf(s1.charAt(c)))
                .collect(Collectors.joining());
    }
}

public class Day_2_2 {

    static String fileName = "input_2.txt";

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        Tuple tuple =
                Files.lines(Paths.get(fileName))
                        .flatMap(s ->
                        {
                            try {
                                return Files.lines(Paths.get(fileName))
                                        .map(s2 -> new Tuple(s, s2));
                            } catch (IOException e) {
                                e.printStackTrace();
                                return null;
                            }
                        })
                        .parallel()
                        .filter(t -> differByOneCharacter(t))
                        .findFirst()
                        .orElseGet(null);

        System.out.println("\nTuple: " + tuple);
        System.out.println("Common characters: " + tuple.commonCharacters());
        LocalTime finish = LocalTime.now();
        System.out.println("\nduration (ms): " + Duration.between(start, finish).toMillis());
    }

    private static Predicate<Tuple> differByOneCharacter() {
        return (t) -> (int) IntStream.range(0, t.s1.length())
                .filter(i -> t.s1.charAt(i) != t.s2.charAt(i))
                .limit(2)
                .count() == 1;
    }

    private static boolean differByOneCharacter(final Tuple t) {
        return IntStream.range(0, t.s1.length())
                .filter(i -> t.s1.charAt(i) != t.s2.charAt(i))
                .limit(2)
                .count() == 1;
    }
}