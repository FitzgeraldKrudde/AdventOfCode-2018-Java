package com.ing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Comparator;

public class Day_5 {

    static String fileName = "input_5.txt";
    static int nReacts = 0;

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        String polymer = Files.lines(Paths.get(fileName)).
                findFirst().
                orElse("");
        String result = reactPolymer(polymer);

        System.out.println("\nresult: " + result);
        System.out.println("#result: " + result.length());

        LocalTime finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());

        //part 2

        start = LocalTime.now();

        // determine unique characters and determine smallest polymer when removing a unit (character)
        char c = polymer
                .toUpperCase()
                .chars()
                .sorted()
                .distinct()
                .mapToObj(i -> (char) i)
                .min(Comparator.comparing(e -> reactPolymer(removeUnitFromPolymer(polymer, e)).length()))
                .get();

        // now we got our "optimal" unit
        result = reactPolymer(removeUnitFromPolymer(polymer, c));

        System.out.println("\nproblem unit: " + c);
        System.out.println("#result: " + result.length());
        System.out.println("#reacts: " + nReacts);

        finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
    }

    private static String reactPolymer(final String s) {
        String sOld = s;
        StringBuilder sNew = new StringBuilder(s.length());
        boolean lengthChanged = true;
        int i = 0;
        char c1, c2;

        while (lengthChanged) {
            sNew.setLength(0);
            for (i = 0; i < sOld.length() - 1; i++) {
                c1 = sOld.charAt(i);
                c2 = sOld.charAt(i + 1);
                if (Character.toUpperCase(c1) == Character.toUpperCase(c2) && c1 != c2) {
                    i++;
                } else {
                    sNew.append(c1);
                }
            }

            // copy last element, unless we passed by it (last 2 elements matched)
            if (i == sOld.length() - 1) {
                sNew.append(sOld.charAt(i));
            }

            // check if we did mod our input String
            if (sOld.length() == sNew.length()) {
                lengthChanged = false;
            } else {
                sOld = sNew.toString();
            }
        }

        nReacts++;
        return sNew.toString();
    }

    private static String removeUnitFromPolymer(String polymer, char unit) {
        return polymer.chars()
                .filter(c -> Character.toUpperCase(c) == Character.toUpperCase(unit)).toString();
    }
}