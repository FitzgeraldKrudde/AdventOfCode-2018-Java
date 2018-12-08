package com.ing;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;

import static java.util.stream.Collectors.toMap;

public class Day_2_1 {

    static String fileName = "input_2.txt";

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        System.out.println("#2 x #3 = " +
                Files.lines(Paths.get(fileName)).flatMap(s ->
                        s.chars()
                                .boxed()
                                .collect(toMap(
                                        k -> (char) k.intValue(),
                                        v -> 1,
                                        Integer::sum))
                                .values()
                                .stream()
                                .distinct()
                                .filter(c -> c == 2 || c == 3)
                ).collect(toMap(
                        k -> (char) k.intValue(),
                        v -> 1,
                        Integer::sum)
                )
                        .values()
                        .stream()
                        .map(i -> BigInteger.valueOf(i))
                        .reduce(BigInteger.ONE, BigInteger::multiply));

        LocalTime finish = LocalTime.now();
        System.out.println("\nduration (ms): " + Duration.between(start, finish).toMillis());
    }
}