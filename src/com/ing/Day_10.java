package com.ing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@ToString
@AllArgsConstructor
@Getter
@Setter
class SkyElement {
    Point location;
    Point velocity;
}

@ToString
class Sky {
    List<SkyElement> sky = new ArrayList<>();

    public void addToSky(Collection skyElements) {
        sky.addAll(skyElements);
    }

    private List<Point> getPoints() {
        return sky.stream()
                .map(skyElement -> skyElement.getLocation())
                .collect(Collectors.toList());
    }

    public void nextSecond() {
        sky.stream()
                .forEach(skyElement -> {
                            skyElement.getLocation().setLocation(skyElement.getLocation().getX() + skyElement.getVelocity().getX()
                                    , skyElement.getLocation().getY() + skyElement.getVelocity().getY());
                        }
                );
    }

    public boolean printMessage() {
        // as the message i.e. letters have a "top" and "bottom" line
        // sort the sky elements at most occurring y coordinates
        // and get the top and bottom Y coordinate
        List<Integer> top_Y_Coordinates =
                sky.stream()
                        .collect(toMap(
                                p -> (int) p.getLocation().getY(),
                                v -> 1,
                                Integer::sum))
                        .entrySet()
                        .stream()
                        .sorted(Comparator.comparingInt(e -> -e.getValue()))
                        .map(e -> e.getKey())
                        .limit(2)
                        .sorted(Comparator.comparingInt(e -> e))
                        .collect(Collectors.toList());
        int minY = top_Y_Coordinates.get(0);
        int maxY = top_Y_Coordinates.get(1);
        System.out.println("min/max Y: " + minY + " " + maxY);

        // shortcut when the distance between min and max is too great (probably too few hits per y coordinate)
        if (maxY - minY > 20) {
            return false;
        }

        // find the X coordinates with a high hit on these 2 Y coordinates
        // and pick the min and max x coordinate
        IntSummaryStatistics stats =
                sky.stream()
                        .filter(skyElement -> skyElement.getLocation().getY() >= minY && skyElement.getLocation().getY() <= maxY)
                        .collect(toMap(
                                p -> (int) p.getLocation().getX(),
                                v -> 1,
                                Integer::sum))
                        .entrySet()
                        .stream()
                        .filter(e -> e.getValue() > 3)
                        .sorted(Comparator.comparingInt(e -> -e.getValue()))
                        .map(e -> e.getKey())
                        .collect(Collectors.summarizingInt(i -> i));
        if (stats.getCount() == 0) {
            return false;
        }
        int minX = stats.getMin();
        int maxX = stats.getMax();

        // shortcut when the distance between min and max is too small (probably too many hits)
        if (maxX - minX < 5) {
            return false;
        }

        System.out.println("min/max X: " + minX + " " + maxX);

        // now we have a serious potential window
        // check if there is a reasonable number of recognisable "letters" in this area
        // i.e. the number of parts with at least some adjacent points (horizontal or vertical)
        int adjacentParts = calculateNumberOfAdjacentParts(minY, maxY, minX, maxX);
        System.out.println("adjacentParts: " + adjacentParts);
        // fail when not enough adjacent parts
        if (adjacentParts < 5) {
            return false;
        }

        int border = 5;
        printMessage(minY, maxY, minX, maxX, border);

        return true;
    }

    private int calculateNumberOfAdjacentParts(int minY, int maxY, int minX, int maxX) {
        int minimumAdjacentPointsNeeded = 3;

        List<Point> listPoints = getPoints();
        int nrAdjacentParts = 0;
        int currentAdjacentPoints = 0;

        // horizontal
        boolean previousLocationWasAPoint = false;
        for (int j = minY; j <= maxY; j++) {
            for (int i = minX; i <= maxX; i++) {
                if (listPoints.contains(new Point(i, j))) {
                    if (previousLocationWasAPoint) {
                        if (++currentAdjacentPoints >= minimumAdjacentPointsNeeded) {
                            nrAdjacentParts++;
                        }
                    } else {
                        previousLocationWasAPoint = true;
                        currentAdjacentPoints = 0;
                    }
                } else {
                    previousLocationWasAPoint = false;
                    currentAdjacentPoints = 0;
                }
            }
            previousLocationWasAPoint = false;
            currentAdjacentPoints = 0;
        }

        // vertical
        previousLocationWasAPoint = false;
        for (int i = minX; i <= maxX; i++) {
            for (int j = minY; j <= maxY; j++) {
                if (listPoints.contains(new Point(i, j))) {
                    if (previousLocationWasAPoint) {
                        if (++currentAdjacentPoints >= minimumAdjacentPointsNeeded) {
                            nrAdjacentParts++;
                        }
                    } else {
                        previousLocationWasAPoint = true;
                        currentAdjacentPoints = 0;
                    }
                } else {
                    previousLocationWasAPoint = false;
                    currentAdjacentPoints = 0;
                }
            }
            previousLocationWasAPoint = false;
            currentAdjacentPoints = 0;
        }

        return nrAdjacentParts;
    }

    private void printMessage(int minY, int maxY, int minX, int maxX, int border) {
        List<Point> listPoints = getPoints();
        for (int j = minY - border; j <= maxY + border; j++) {
            for (int i = minX - border; i <= maxX + border; i++) {
                if (listPoints.contains(new Point(i, j))) {
                    System.out.print('#');
                } else {
                    System.out.print('.');
                }
            }
            System.out.println();
        }
    }
}

public class Day_10 {

    private static String fileName = "input_10.txt";

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        //position=< 9,  1> velocity=< 0,  2>

        // read input
        List<SkyElement> skyElements =
                Files.lines(Paths.get(fileName))
                        .map(line -> {
                            List<Integer> numbers =
                                    Arrays.stream(
                                            line.trim()
                                                    .chars()
                                                    .filter(c -> "=<>,".indexOf(c) == -1)
                                                    .filter(c -> c == '-' || Character.isDigit(c) || Character.isSpaceChar(c))
                                                    .mapToObj(c -> String.valueOf((char) c))
                                                    .collect(Collectors.joining())
                                                    .trim()
                                                    .split("\\s+")
                                    )
                                            .mapToInt(w -> Integer.valueOf(w))
                                            .boxed()
                                            .collect(Collectors.toList());
                            return new SkyElement(new Point(numbers.get(0), numbers.get(1))
                                    , new Point(numbers.get(2), numbers.get(3)));
                        })
                        .collect(Collectors.toList());

        // create the Sky with points
        Sky sky = new Sky();
        sky.addToSky(skyElements);

        int seconds = 0;
        while (!sky.printMessage()) {
            sky.nextSecond();
            seconds++;
            System.out.println("\nsecond: " + seconds);
        }

        LocalTime finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());

        // part 2

        start = LocalTime.now();
        System.out.println("\nsecond: " + seconds);

        finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
    }
}

