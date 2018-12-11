package com.ing;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day_10 {

    private static String fileName = "input_10.txt";

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        // get 1 line of input
        String input = Files.lines(Paths.get(fileName))
                .findFirst()
                .get();

        int gridSerialNumber = Integer.parseInt(input);
        System.out.println("\ngridSerialNumber: " + gridSerialNumber);
        FuelTank fueltank = new FuelTank(gridSerialNumber, 3000);
        Point point = fueltank.getBestSquare(3).getKey();
        System.out.println("point: " + point);

        LocalTime finish = LocalTime.now();
        System.out.println("\nduration (ms): " + Duration.between(start, finish).toMillis());

        // part 2

        start = LocalTime.now();

        SimpleEntry se = fueltank.getBestSquareAndDimension();
        System.out.println("\npoint: " + se.getKey());
        System.out.println("dimension: " + se.getValue());

        finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
    }

    static class FuelTank {
        int dimension;
        int gridSerialNumber;
        private int[][] fuelCell;

        FuelTank(int gridSerialNumber, int dimension) {
            this.gridSerialNumber = gridSerialNumber;
            this.dimension = dimension;

            fuelCell = new int[dimension + 1][this.dimension + 1];
            for (int i = 1; i <= dimension; i++) {
                for (int j = 1; j < dimension; j++) {
                    fuelCell[i][j] = calculatePowerLevel(i, j);
                }
            }
        }

        private int calculatePowerLevel(int x, int y) {
            int rackId = x + 10;
            int powerlevel = rackId * y;
            powerlevel += gridSerialNumber;
            powerlevel *= rackId;
            powerlevel = powerlevel / 100 % 10;
            powerlevel -= 5;

            return powerlevel;
        }

        private int calculateTotalPowerOfSquare(int x, int y, int dimension) {
            int totalPower = 0;

            for (int i = x; i < x + dimension; i++) {
                for (int j = y; j < y + dimension; j++) {
                    {
                        totalPower += fuelCell[i][j];
                    }
                }
            }

            return totalPower;
        }

        /**
         * @return entry with point and dimension (which has the maximum total power)
         */
        public SimpleEntry<Point, Integer> getBestSquareAndDimension() {
            SimpleEntry<Point, SimpleEntry<Integer, Integer>> tuplePointAndTupleDimensionAndPower = IntStream.range(1, dimension)
                    .parallel()
                    .mapToObj(currentDimension -> {
                        SimpleEntry<Point, Integer> tuplePointAndPower = getBestSquare(currentDimension);
                        SimpleEntry<Integer, Integer> tupleDimensionAndTotalPower = new SimpleEntry<>(currentDimension, tuplePointAndPower.getValue());
                        return new SimpleEntry<>(tuplePointAndPower.getKey(), tupleDimensionAndTotalPower);
                    })
                    .max(Comparator.comparingInt(se -> se.getValue().getValue()))
                    .get();

            return new SimpleEntry<>(tuplePointAndTupleDimensionAndPower.getKey(), tuplePointAndTupleDimensionAndPower.getValue().getKey());
        }

        /**
         * @param squareDimension
         * @return entry with Point and amount of total power
         */
        public SimpleEntry<Point, Integer> getBestSquare(int squareDimension) {
            SimpleEntry tuplePointAndTotalPower = IntStream.range(1, dimension - squareDimension + 1)
                    .boxed()
                    .flatMap(x -> IntStream.range(1, dimension - squareDimension + 1)
                            .mapToObj(y -> {
                                return new SimpleEntry<>(new Point(x, y), calculateTotalPowerOfSquare(x, y, squareDimension));
                            })
                    ).collect(Collectors.toList())
                    .stream()
                    .max(Comparator.comparingInt(se -> se.getValue()))
                    .get();
            return tuplePointAndTotalPower;
        }
    }
}