package com.ing;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

import static java.util.AbstractMap.SimpleEntry;
import static java.util.stream.Collectors.toList;

class Construction {
    private final Square startingPoint = new Square(0, 0);
    @Getter
    private Map<Square, Integer> mapSquareDistance = new HashMap<>();
    private int depth = 0;
    private int maxRecursionDepth = 0;

    Construction(String inputLine) {
        constructMapFromInputline(inputLine);
    }

    private void constructMapFromInputline(String inputLine) {
        String line = inputLine.substring(1, inputLine.length() - 1);

        // fill the map with coordinate->distance for the starting point (with distance 0)
        mapSquareDistance.put(startingPoint, 0);

        calculateDistances(startingPoint, line);
        System.out.println("maxRecursionDepth = " + maxRecursionDepth);
    }

    private void calculateDistances(Square startingPoint, String line) {
        if (++depth > maxRecursionDepth) {
            maxRecursionDepth = depth;
        }

        Square currentSquare = startingPoint;
        // first go through normal navigational directions (NSWE)
        while (line.length() > 0 && "NSEW".indexOf(line.charAt(0)) >= 0) {
            int currentDistance = mapSquareDistance.get(currentSquare);
            Square nextSquare = currentSquare.nextSquare(line.charAt(0));
            int currentNextSquareDistance = mapSquareDistance.get(nextSquare) != null ? mapSquareDistance.get(nextSquare) : Integer.MAX_VALUE;
            mapSquareDistance.put(nextSquare, Integer.min(currentDistance + 1, currentNextSquareDistance));
            line = line.substring(1);
            currentSquare = nextSquare;
        }

        if (line.length() == 0) {
            depth--;
            return;
        }

        // the line must start now with a '('
        assert line.charAt(0) == '(';

        // find matching parenthesis
        int posCloseParenthesis = findPositionMatchingParenthesis(line);
        // substring with branches (or detours)
        String branchSubLine = line.substring(1, posCloseParenthesis);
        // substring with remainder
        String remainder = line.substring(posCloseParenthesis + 1);

        // find the branches or detours, a '|' at the end (before ')' ) means detours!
        if (branchSubLine.charAt(branchSubLine.length() - 1) == '|') {
            // detours
            List<String> detours = findBranches(branchSubLine);
            // process the detours
            for (String detour : detours) {
                calculateDistances(currentSquare, detour);
            }
            // add the normal route (without detours)
            calculateDistances(currentSquare, remainder);
        } else {
            // branches
            List<String> branches = findBranches(branchSubLine);
            // process the branches
            for (String branch : branches) {
                calculateDistances(currentSquare, branch + remainder);
            }
        }

        depth--;
    }

    private List<String> findBranches(String branchSubLine) {
        ArrayList<String> branches = new ArrayList<>();
        int startPos = 0;
        int balance = 0;
        for (int i = 0; i < branchSubLine.length(); i++) {
            switch (branchSubLine.charAt(i)) {
                case 'N':
                case 'S':
                case 'E':
                case 'W':
                    break;
                case '|':
                    if (balance == 0) {
                        // we got a branch
                        branches.add(branchSubLine.substring(startPos, i));
                        startPos = i + 1;
                    }
                    break;
                case '(':
                    balance++;
                    break;
                case ')':
                    balance--;
                    break;
                default:
                    throw new IllegalStateException(String.format("unknown char: %c", branchSubLine.charAt(i)));
            }
        }
        // get last branch
        if (startPos != branchSubLine.length()) {
            branches.add(branchSubLine.substring(startPos));
        }
        return branches;
    }

    private int findPositionMatchingParenthesis(String s) {
        int balance = 0;

        for (int j = 1; j < s.length(); j++) {
            switch (s.charAt(j)) {
                case '(':
                    balance++;
                    break;
                case ')':
                    if (balance == 0) {
                        return j;
                    } else {
                        balance--;
                    }
                    break;
            }
        }
        return -1;
    }
}

public class Day_20 {

    private static String FILENAME = "input_20.txt";

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        // read 1 line from input
        String inputLine = readFile(args).get(0);
        System.out.println("inputLine = " + inputLine);

        Construction construction = new Construction(inputLine);

        int longestPath = construction.getMapSquareDistance().entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .orElseGet(() -> new SimpleEntry<>(null, -1))
                .getValue();
        System.out.println("\npart 1:\nlongestPath = " + longestPath);

        LocalTime finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());

        // part 2

        start = LocalTime.now();

        long nrPathsAtLeast1000 = construction.getMapSquareDistance().entrySet().stream()
                .filter(e -> e.getValue() >= 1000)
                .count();
        System.out.println("\npart 2:\nnrPathsAtLeast1000 = " + nrPathsAtLeast1000);

        finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
    }

    private static List<String> readFile(String[] args) throws IOException {
        String fileName;
        if (args.length == 0) {
            fileName = FILENAME;
        } else {
            fileName = args[0];
        }

        System.out.println("reading file: " + fileName);
        // get the input lines
        List<String> input = Files.lines(Paths.get(fileName)).collect(toList());
        System.out.println(String.format("read file: %s (#lines: %d)", fileName, input.size()));
        return input;
    }

}