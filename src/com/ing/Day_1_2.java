package com.ing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day_1_2 {

    static String fileName = "input_1.txt";
    static long frequency = 0;
    static HashSet<Long> hs = new HashSet<>();

    public static void main(String[] args) {
        IntStream.iterate(0, i -> i).forEach(i ->
                {
                    processFile();
                }
        );
    }

    private static void processFile() {
        try (
                Stream<String> stream = Files.lines(Paths.get(fileName));
        ) {
            stream.forEach(s -> {
                frequency += Long.valueOf(s);
                if (!hs.add(frequency)) {
                    System.out.println("recurring frequency: " + frequency);
                    throw new RuntimeException("exiting, found recurring frequency");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}