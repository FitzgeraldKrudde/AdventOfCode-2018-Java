package com.ing;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

class Landscape {
    public static final char TREES = '|';
    public static final char OPEN_GROUND = '.';
    public static final char LUMBERYARD = '#';

    @Getter
    private char[][] acres;
    List<String> inputLines;

    Landscape(List<String> inputLines) {
        this.inputLines = inputLines;
        constructLandscapeFromInputlines(inputLines);
    }

    public void reset() {
        constructLandscapeFromInputlines(inputLines);
    }

    private void constructLandscapeFromInputlines(List<String> inputLines) {
        acres = new char[inputLines.get(0).length()][inputLines.size()];
        for (int y = 0; y < acres[0].length; y++) {
            for (int x = 0; x < acres.length; x++) {
                acres[x][y] = inputLines.get(y).charAt(x);
            }
        }
    }

    public void print() {
        System.out.println("\n");
        for (int y = 0; y < acres[0].length; y++) {
            for (int x = 0; x < acres.length; x++) {
                System.out.print(acres[x][y]);
            }
            System.out.println("");
        }
        System.out.println("\n");
    }

    public int countType(char type) {
        int nrType = 0;
        for (int y = 0; y < acres[0].length; y++) {
            for (int x = 0; x < acres.length; x++) {
                if (acres[x][y] == type) {
                    nrType++;
                }
            }
        }
        return nrType;
    }

    private List<Square> getNeighbourSquares(int x, int y) {
        List<Square> ls = List.of(
                new Square(x - 1, y - 1),
                new Square(x, y - 1),
                new Square(x + 1, y - 1),
                new Square(x - 1, y),
                new Square(x + 1, y),
                new Square(x - 1, y + 1),
                new Square(x, y + 1),
                new Square(x + 1, y + 1)
        ).stream()
                .filter(sq -> isValidSquare(sq.getX(), sq.getY()))
                .collect(Collectors.toList());

        return ls;
    }

    private boolean isValidSquare(int x, int y) {
        if (x < 0) return false;
        if (y < 0) return false;
        if (x >= acres.length) return false;
        if (y >= acres[0].length) return false;
        return true;
    }

    public void nextMinute() {
        char[][] newAcres = new char[acres[0].length][acres.length];
        // copy old array
        for (int x = 0; x < acres[0].length; x++) {
            System.arraycopy(acres[x], 0, newAcres[x], 0, acres[x].length);
        }

        for (int y = 0; y < acres[0].length; y++) {
            for (int x = 0; x < acres.length; x++) {
                List<Square> listNeighbours = getNeighbourSquares(x, y);
                switch (acres[x][y]) {
                    case OPEN_GROUND:
                        if (listNeighbours.stream()
                                .filter(sq -> acres[sq.getX()][sq.getY()] == TREES)
                                .count() >= 3) {
                            newAcres[x][y] = TREES;
                        }
                        break;
                    case TREES:
                        if (listNeighbours.stream()
                                .filter(sq -> acres[sq.getX()][sq.getY()] == LUMBERYARD)
                                .count() >= 3) {
                            newAcres[x][y] = LUMBERYARD;
                        }
                        break;
                    case LUMBERYARD:
                        if (listNeighbours.stream()
                                .filter(sq -> acres[sq.getX()][sq.getY()] == LUMBERYARD)
                                .count() >= 1
                                && listNeighbours.stream()
                                .filter(sq -> acres[sq.getX()][sq.getY()] == TREES)
                                .count() >= 1) {
                            newAcres[x][y] = LUMBERYARD;
                        } else {
                            newAcres[x][y] = OPEN_GROUND;
                        }
                        break;
                    default:
                        throw new IllegalStateException(String.format("unknown acre type: %s at [%d][%d]", acres[x][y], x, y));
                }
            }
        }
        acres = newAcres;
    }

    public int getNrOfTreeAcres() {
        return countType(TREES);
    }

    public int getNrOfLumberyard() {
        return countType(LUMBERYARD);
    }
}

public class Day_18 {

    private static String FILENAME = "input_18.txt";

    static List<ClayInput> clayList = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        List<String> inputLines = readFile(args);
        final Landscape landscape = new Landscape(inputLines);
        landscape.print();

        int minute;
        for (minute = 1; minute <= 10; minute++) {
            landscape.nextMinute();
            System.out.println("minute = " + minute);
            landscape.print();
        }
        System.out.println("minutes passed = " + (minute - 1));
        landscape.print();

        int nrTreeAcres = landscape.getNrOfTreeAcres();
        System.out.println("nrTreeAcres = " + nrTreeAcres);
        int nrLumberyardAcres = landscape.getNrOfLumberyard();
        System.out.println("nrLumberyardAcres = " + nrLumberyardAcres);
        int resourceValue = nrTreeAcres * nrLumberyardAcres;
        System.out.println("resourceValue = " + resourceValue);

        LocalTime finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
        System.out.println();

        // part 2

        start = LocalTime.now();

        landscape.reset();
        landscape.print();

        List<char[][]> listAcres = new ArrayList();
        minute = 0;
        int duplicateMinute = -1;
        boolean noDuplicate = true;
        while (noDuplicate) {
            minute++;
            listAcres.add(landscape.getAcres());
            landscape.nextMinute();
            System.out.println("minute = " + minute);
            landscape.print();
            // check for duplicate
            for (int i = 0; i < listAcres.size(); i++) {
                if (Arrays.deepEquals(listAcres.get(i), landscape.getAcres())) {
                    duplicateMinute = i;
                    noDuplicate = false;
                }
            }
        }
        // we have repeating acres
        System.out.println(String.format("repeating: %d and %d", duplicateMinute, minute));
        // calculate interval
        int repeatingIntervalInMinutes = minute - duplicateMinute;
        System.out.println("repeatingIntervalInMinutes = " + repeatingIntervalInMinutes);
        long totalMinutes = 1000000000L;
        // calculate intervals to skip
        long nrMinutesToSkipForAcresAtTotalMinutes = (totalMinutes - 525) % repeatingIntervalInMinutes;
        System.out.println("nrMinutesToSkipForAcresAtTotalMinutes = " + nrMinutesToSkipForAcresAtTotalMinutes);
        for (int i = 0; i < nrMinutesToSkipForAcresAtTotalMinutes; i++) {
            landscape.nextMinute();
        }
        // get the #resources
        nrTreeAcres = landscape.getNrOfTreeAcres();
        System.out.println("nrTreeAcres = " + nrTreeAcres);
        nrLumberyardAcres = landscape.getNrOfLumberyard();
        System.out.println("nrLumberyardAcres = " + nrLumberyardAcres);
        resourceValue = nrTreeAcres * nrLumberyardAcres;
        System.out.println("resourceValue = " + resourceValue);

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
        System.out.println("read file: " + fileName);
        return input;
    }
}