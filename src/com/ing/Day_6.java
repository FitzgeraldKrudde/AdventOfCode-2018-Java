package com.ing;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

public class Day_6 {

    static String fileName = "input_6.txt";

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        // read points
        List<Point> points = readPoints(fileName);

        // create finite grid with the closest coordinate
        List<AbstractMap.SimpleEntry<Point, String>> grid = createGridWithClosestCoordinate(points);

        // determine all points at the edge i.e. which have infinite area
        List<String> edgeCoordinates = grid.stream()
                .filter(p -> p.getKey().getX() == 0
                        || p.getKey().getY() == 0
                        || p.getKey().getX() == getMaxX(points)
                        || p.getKey().getY() == getMaxY(points)
                )
                .map(p -> p.getValue())
                .distinct()
                .collect(Collectors.toList());

        // filter out all coordinates not at an edge and find the largest area for these coordinates
        int largestArea = grid.stream()
                .parallel()
                .filter(p -> !edgeCoordinates.contains(p.getValue()))
                .collect(Collectors.groupingBy(e -> e.getValue(), Collectors.counting()))
                .values()
                .stream()
                .max(Comparator.comparingLong(i -> i))
                .get()
                .intValue();

        System.out.println("\nlargestArea: " + largestArea);

        LocalTime finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());

        //part 2

        start = LocalTime.now();

        // create finite grid where coordinates with maximum distances < 10K marked as '#'
        grid = createGridWithMaximumDistances(points, 10000);

        // filter all coordinates at an edge and find the largest area for the remaining coordinates
        largestArea = (int) grid.stream()
                .parallel()
                .filter(p -> p.getValue() != ".")
                .count();

        System.out.println("\nlargestArea: " + largestArea);

        finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
    }

    private static List<AbstractMap.SimpleEntry<Point, String>> createGridWithClosestCoordinate(List<Point> points) {
        return IntStream.range(0, getMaxX(points))
                .boxed()
                .parallel()
                .flatMap(x -> IntStream.range(0, getMaxY(points))
                        .mapToObj(y -> {
                            // default is there are multiple closest coordinates i.e. '.'
                            AbstractMap.SimpleEntry<Point, String> coordinate = new AbstractMap.SimpleEntry<>(new Point(x, y), ".");

                            // calculate Manhattan distance for all points
                            Map<Point, Integer> mapPointsWithDistance = points.stream()
                                    .collect(toMap(
                                            k -> k,
                                            k -> calculateManhattanDistance(k, x, y)
                                    ));

                            // find minimum distance
                            int min = mapPointsWithDistance.entrySet()
                                    .stream()
                                    .min(Comparator.comparingInt(e -> e.getValue()))
                                    .get()
                                    .getValue();

                            // get the coordinate(s) with the minimum value
                            List<Point> listPointsWithMinimumDistance = mapPointsWithDistance.entrySet()
                                    .stream()
                                    .filter(e -> e.getValue() == min)
                                    .map(e -> e.getKey())
                                    .collect(Collectors.toList());

                            // check if there is 1 coordinate with the minimum value
                            if (listPointsWithMinimumDistance.size() == 1) {
                                // set this coordinate in the point
                                coordinate.setValue(listPointsWithMinimumDistance.get(0).toString());
                            }
                            return coordinate;
                        })
                ).collect(Collectors.toList());
    }

    private static List<AbstractMap.SimpleEntry<Point, String>> createGridWithMaximumDistances(List<Point> points, int maximumDistance) {
        return IntStream.range(0, getMaxX(points))
                .boxed()
                .parallel()
                .flatMap(x -> IntStream.range(0, getMaxY(points))
                        .mapToObj(y -> {
                            // default is that point has greater than maximum distance i.e. '.'
                            AbstractMap.SimpleEntry<Point, String> coordinate = new AbstractMap.SimpleEntry<>(new Point(x, y), ".");

                            // calculate total Manhattan distance for all points
                            int totalDistance = points.stream()
                                    .mapToInt(p -> calculateManhattanDistance(p, x, y))
                                    .sum();

                            // update point to '#' when below maximum distance
                            if (totalDistance < maximumDistance) {
                                coordinate.setValue(coordinate.toString());
                            }
                            return coordinate;
                        })
                ).collect(Collectors.toList());
    }

    private static int getMaxY(List<Point> points) {
        return points.stream()
                .map(p -> p.getY())
                .max(Comparator.comparingDouble(i -> i))
                .get()
                .intValue();
    }

    private static int getMaxX(List<Point> points) {
        return points.stream()
                .map(p -> p.getX())
                .max(Comparator.comparingDouble(i -> i))
                .get()
                .intValue();
    }

    private static int calculateManhattanDistance(Point e, int x, int y) {
        int distanceX = Integer.max(e.x, x) - Integer.min(e.x, x);
        int distanceY = Integer.max(e.y, y) - Integer.min(e.y, y);

        return distanceX + distanceY;
    }

    private static List<Point> readPoints(final String fileName) throws IOException {
        return Files.lines(Paths.get(fileName))
                .map(s -> {
                    String[] coords = s.chars()
                            .filter(c -> c != ' ')
                            .mapToObj(c -> String.valueOf((char) c))
                            .collect(Collectors.joining())
                            .split(",");
                    return new Point(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
                })
                .collect(Collectors.toList());
    }

}