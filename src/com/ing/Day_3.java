package com.ing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;

public class Day_3 {

    static String fileName = "input_3.txt";
    static boolean overlap = false;
    static String id;

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        ArrayList<ArrayList<Integer>> al2D = new ArrayList<>();
        Files.lines(Paths.get(fileName)).forEach(s -> {
                    String[] elems = s.split(" ");

                    String[] position = elems[2].split(",");
                    int x = Integer.valueOf(position[0]);
                    int y = Integer.valueOf(position[1].split(":")[0]);

                    String[] dimension = elems[3].split("x");
                    int w = Integer.valueOf(dimension[0]);
                    int h = Integer.valueOf(dimension[1]);

                    // extend size if needed
                    for (int i = al2D.size(); i < x + w; i++) {
                        al2D.add(new ArrayList<>());
                    }
                    for (int i = x; i < x + w; i++) {
                        ArrayList<Integer> al = al2D.get(i);

                        // extend size if needed
                        for (int j = al.size(); j < y + h; j++) {
                            al.add(j, 0);
                        }

                        // add fabric usage
                        for (int j = y; j < y + h; j++) {
                            al.set(j, al.get(j) + 1);
                        }
                    }
                }
        );

        long count = al2D.parallelStream()
                .flatMap(al -> al.stream()
                        .filter(i -> i.intValue() > 1)
                )
                .count();

        System.out.println("\n#overlapping squares: " + count);
        LocalTime finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());

        // part 2

        start = LocalTime.now();
        overlap = true;
        Files.lines(Paths.get(fileName))
                .filter(s -> overlap)
                .forEach(s -> {
                    String[] elems = s.split(" ");

                    id = elems[0].split("#")[1];

                    String[] position = elems[2].split(",");
                    int x = Integer.valueOf(position[0]);
                    int y = Integer.valueOf(position[1].split(":")[0]);

                    String[] dimension = elems[3].split("x");
                    int w = Integer.valueOf(dimension[0]);
                    int h = Integer.valueOf(dimension[1]);

                    overlap = false;
                    for (int i = x; !overlap && i < x + w; i++) {
                        ArrayList<Integer> al = al2D.get(i);
                        for (int j = y; !overlap && j < y + h; j++) {
                            // check overlap
                            if (al.get(j) != 1) {
                                overlap = true;
                            }
                        }
                    }
                    if (!overlap) {
                        System.out.println("\nfound non-overlapping id: " + id);
                    }
                });

        finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
    }
}