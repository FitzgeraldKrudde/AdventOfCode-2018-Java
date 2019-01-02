package com.ing;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@AllArgsConstructor
@Data
class ClayInput {
    public static final char FIXED_COORDINATE_TYPE_X = 'x';
    public static final char FIXED_COORDINATE_TYPE_Y = 'y';

    private char fixedCoordinateType;
    private int fixedCoordinate;
    private int rangeStart;
    private int rangeEnd;
}

class Ground {
    public static final char CLAY = '#';
    public static final char SAND = '.';
    public static final char WATER = '|';
    public static final char SETTLED_WATER = '~';
    public static final char WATER_SPRING = '+';

    private char[][] ground;
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private int lowestWaterlevel;

    Ground(int minX, int maxX, int minY, int maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;

        lowestWaterlevel = minY;

        ground = new char[maxX + 10][maxY + 2];
        for (char[] row : ground)
            Arrays.fill(row, SAND);
    }

    public void addClay(ClayInput clayInput) {
        switch (clayInput.getFixedCoordinateType()) {
            case ClayInput.FIXED_COORDINATE_TYPE_X:
                for (int y = clayInput.getRangeStart(); y <= clayInput.getRangeEnd(); y++) {
                    ground[clayInput.getFixedCoordinate()][y] = CLAY;
                }
                break;
            case ClayInput.FIXED_COORDINATE_TYPE_Y:
                for (int x = clayInput.getRangeStart(); x <= clayInput.getRangeEnd(); x++) {
                    ground[x][clayInput.getFixedCoordinate()] = CLAY;
                }
                break;
        }

    }

    public void print() {
        System.out.println("\n");
        for (int y = 0; y <= lowestWaterlevel + 1 && y < ground[0].length; y++) {
            for (int x = Math.max(minX - 5, 0); x < ground.length; x++) {
                if (x == 500 && y == 0) {
                    System.out.print(WATER_SPRING);
                } else {
                    System.out.print(ground[x][y]);
                }
            }
            System.out.println("");
        }
        System.out.println("\n");
    }

    public int countTotalWater() {
        int total = 0;
        for (int y = 0; y < ground[0].length; y++) {
            for (int x = 0; x < ground.length; x++) {
                if (ground[x][y] == WATER || ground[x][y] == SETTLED_WATER) {
                    total++;
                }
            }
        }
        return total;
    }

    public int countWater() {
        return countType(WATER) + countType(SETTLED_WATER);
    }

    public int countType(char type) {
        int nrType = 0;
        for (int y = minY; y <= maxY; y++) {
            for (int x = Math.max(minX - 50, 0); x < ground.length; x++) {
                if (ground[x][y] == type) {
                    nrType++;
                }
            }
        }
        return nrType;
    }

    public void addWater(int x, int y) {
        // check how far down the water can flow down
        while (y < ground[0].length && ground[x][y] == SAND) {
            ground[x][y] = WATER;
            y++;
            if (y > lowestWaterlevel) {
                lowestWaterlevel = y;
            }
        }
    }

    public void settleWater(int x, int y) {
        // check if bounded left and right by clay and on solid ground
        int start = x;
        while (ground[start][y] == WATER && isStableGround(start, y + 1)) {
            start--;
        }
        if (ground[start][y] == CLAY && isStableGround(start, y + 1)) {
            int end = x;
            while (ground[end][y] == WATER && isStableGround(end, y + 1)) {
                end++;
            }
            if (ground[end][y] == CLAY && isStableGround(end, y + 1)) {
                // turn this water into settled
                for (int i = start + 1; i < end; i++) {
                    ground[i][y] = SETTLED_WATER;
                }
            }


        }
    }

    public void flowWaterLeft(int x, int y) {
        while (x >= minX && ground[x][y] == SAND && ground[x][y + 1] != WATER) {
            ground[x][y] = WATER;
            if (isStableGround(x, y + 1)) {
                x--;
            } else {
                // let water flow down
                addWater(x, y + 1);
            }
        }
    }


    public void flowWaterRight(int x, int y) {
        while (x <= ground.length && ground[x][y] == SAND && ground[x][y + 1] != WATER) {
            ground[x][y] = WATER;
            if (isStableGround(x, y + 1)) {
                x++;
            } else {
                // let water flow down
                addWater(x, y + 1);
            }
        }

    }

    private boolean isStableGround(int x, int y) {
        return ground[x][y] == CLAY || ground[x][y] == SETTLED_WATER;
    }

    public boolean waterStillFlowing() {
        int nrWaterFlowing = countType(WATER);
        int nrWaterSettled = countType(SETTLED_WATER);

        for (int y = Math.min(lowestWaterlevel, ground[0].length - 2); y >= 0; y--) {
            for (int x = ground.length - 1; x >= 0 && x >= minX - 10; x--) {
                if (ground[x][y] == WATER) {
                    if (ground[x][y + 1] != WATER) {
                        flowWaterLeft(x - 1, y);
                        flowWaterRight(x + 1, y);
                    }
                    settleWater(x, y);
                }
            }
        }
        return nrWaterFlowing != countType(WATER) || nrWaterSettled != countType(SETTLED_WATER);
    }
}

public class Day_17 {

    private static String FILENAME = "input_17.txt";

    static List<ClayInput> clayList = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        List<String> inputLines = readFile(args);

        inputLines.stream()
                .forEach(
                        line -> {
                            String[] words = line.replace(",", "")
                                    .split(" ");
                            char fixedCoordinateType = words[0].charAt(0);
                            int fixedCoordinate = Integer.valueOf(words[0].split("=")[1]);
                            int rangeStart = Integer.valueOf(words[1].split("=")[1].replace("..", " ").split(" ")[0]);
                            int rangeEnd = Integer.valueOf(words[1].split("=")[1].replace("..", " ").split(" ")[1]);
                            clayList.add(new ClayInput(fixedCoordinateType, fixedCoordinate, rangeStart, rangeEnd));
                        });

        int minX = determineMinX(clayList);
        System.out.println("minX = " + minX);
        int maxX = determineMaxX(clayList);
        System.out.println("maxX = " + maxX);
        int minY = determineMinY(clayList);
        System.out.println("minY = " + minY);
        int maxY = determineMaxY(clayList);
        System.out.println("maxY = " + maxY);

        Ground ground = new Ground(minX, maxX, minY, maxY);
        clayList.stream()
                .forEach(line -> {
                    ground.addClay(line);
                });

        System.out.println("\n#clayList = " + clayList.size());
        ground.print();

        ground.addWater(500, 0);
        ground.print();

        int iterations = 0;
        while (ground.waterStillFlowing()) {
            ground.print();
            System.out.println("ground.countWater() = " + ground.countWater());
            iterations++;
        }
        int nrWater = ground.countWater();
        System.out.println("nrWater = " + nrWater);
        System.out.println("countTotalWater = " + ground.countTotalWater());
        System.out.println("iterations = " + iterations);

        LocalTime finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
        System.out.println();

        // part 2

        start = LocalTime.now();


        finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
    }

    private static int determineMaxY(List<ClayInput> listInput) {
        return listInput.stream()
                .map(li -> {
                    if (li.getFixedCoordinateType() == ClayInput.FIXED_COORDINATE_TYPE_Y) {
                        return li.getFixedCoordinate();
                    } else {
                        return li.getRangeEnd();
                    }
                })
                .max(Comparator.comparing(i -> i))
                .get();
    }

    private static int determineMaxX(List<ClayInput> listInput) {
        return listInput.stream()
                .map(li -> {
                    if (li.getFixedCoordinateType() == ClayInput.FIXED_COORDINATE_TYPE_X) {
                        return li.getFixedCoordinate();
                    } else {
                        return li.getRangeEnd();
                    }
                })
                .max(Comparator.comparing(i -> i))
                .get();
    }

    private static int determineMinX(List<ClayInput> listInput) {
        return listInput.stream()
                .map(li -> {
                    if (li.getFixedCoordinateType() == ClayInput.FIXED_COORDINATE_TYPE_X) {
                        return li.getFixedCoordinate();
                    } else {
                        return li.getRangeEnd();
                    }
                })
                .min(Comparator.comparing(i -> i))
                .get();
    }

    private static int determineMinY(List<ClayInput> listInput) {
        return listInput.stream()
                .map(li -> {
                    if (li.getFixedCoordinateType() == ClayInput.FIXED_COORDINATE_TYPE_Y) {
                        return li.getFixedCoordinate();
                    } else {
                        return li.getRangeStart();
                    }
                })
                .min(Comparator.comparing(i -> i))
                .get();
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