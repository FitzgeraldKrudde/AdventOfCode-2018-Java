package com.ing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Day_1_1 {

    static String fileName = "input_1.txt";
    static long frequency = 0;

    public static void main(String[] args) throws IOException {
        Files.lines(Paths.get(fileName))
                .forEach(s -> frequency += Long.valueOf(s));
        System.out.println("frequency: " + frequency);
    }
}