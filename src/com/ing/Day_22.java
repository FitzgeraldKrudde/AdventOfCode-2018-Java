package com.ing;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.ing.RegionWithGear.Gear.*;

@Data
class RegionWithGear implements Comparable {
    public enum Gear {CLIMBING_GEAR, TORCH, NEITHER}

    private int x;
    private int y;
    private Gear gear;

    public RegionWithGear(int x, int y, Gear gear) {
        this.x = x;
        this.y = y;
        this.gear = gear;
    }

    @Override
    public int compareTo(Object o) {
        RegionWithGear other = (RegionWithGear) o;

        if (this.y != other.y) {
            return y - other.y;
        }

        if (this.x != other.x) {
            return x - other.x;
        }

        return getGear().compareTo(other.getGear());
    }
}

@Getter
@Setter
@ToString
class Cave {
    private static final int INFINITE = 99999;
    @ToString.Exclude
    public final char ROCKY = '.';
    @ToString.Exclude
    public final char WET = '=';
    @ToString.Exclude
    public final char NARROW = '|';
    @ToString.Exclude
    public final char MOUTH = 'M';
    @ToString.Exclude
    public final char TARGET = 'T';

    private char[][] cave;
    private long[][] erosionLevel;
    private long depth;
    private Square mouth;
    private Square target;
    private List<RegionWithGear> listRegionWithGear = new ArrayList<>();

    public Cave(Square target, int depth) {
        this.depth = depth;
        this.target = target;
        this.mouth = new Square(0, 0);
        // use some extra space for the shortest route
        final double SIZE_FACTOR = 2;
        cave = new char[(int) (SIZE_FACTOR * target.getX() + 1)][(int) (SIZE_FACTOR * target.getY() + 1)];
        erosionLevel = new long[(int) (SIZE_FACTOR * target.getX() + 1)][(int) (SIZE_FACTOR * target.getY() + 1)];

        buildCave();
        createRegionsWithGear();
    }

    private void buildCave() {
        // initialise erosion levels
        for (long[] row : erosionLevel)
            Arrays.fill(row, -1);

        for (int x = 0; x < cave.length; x++) {
            for (int y = 0; y < cave[0].length; y++) {
                cave[x][y] = determineType(x, y);
            }
        }
    }

    public void print() {
        System.out.print("    ");
        IntStream.range(0, cave.length)
                .forEach(i -> System.out.print(i % 10));
        System.out.println();

        IntStream.range(0, cave[0].length)
                .forEach(y -> {
                    System.out.print(String.format("%03d ", y));
                    IntStream.range(0, cave.length)
                            .forEach(x -> {
                                if (x == 0 && y == 0) {
                                    System.out.print(MOUTH);
                                } else if (x == target.getX() && y == target.getY()) {
                                    System.out.print(TARGET);
                                } else {
                                    System.out.print(cave[x][y]);
                                }
                            });
                    System.out.println();
                });
    }

    public long geologicalIndex(int x, int y) {
        if (x == 0 && y == 0) {
            return 0;
        }
        if (target.getX() == x && target.getY() == y) {
            return 0;
        }
        if (y == 0) {
            return x * 16807;
        }
        if (x == 0) {
            return y * 48271;
        }
        return calculateErosionLevel(x - 1, y) * calculateErosionLevel(x, y - 1);
    }

    private long calculateErosionLevel(int x, int y) {
        if (erosionLevel[x][y] == -1) {
            erosionLevel[x][y] = (geologicalIndex(x, y) + depth) % 20183L;
        }
        return erosionLevel[x][y];
    }

    int calculateRiskLevel(int x, int y) {
        switch (cave[x][y]) {
            case ROCKY:
                return 0;
            case WET:
                return 1;
            case NARROW:
                return 2;
            default:
                throw new IllegalArgumentException(String.format("could not determine risk level for x,y: %s,%s", x, y));
        }
    }

    long calculateRegionRisklevel(int topX, int topY, int bottomX, int bottomY) {
        long sum = 0;

        for (int x = topX; x <= bottomX; x++) {
            for (int y = topY; y <= bottomY; y++) {
                sum += calculateRiskLevel(x, y);
            }
        }

        return sum;
    }

    char determineType(int x, int y) {
        switch ((int) (calculateErosionLevel(x, y) % 3)) {
            case 0:
                return ROCKY;
            case 1:
                return WET;
            case 2:
                return NARROW;
            default:
                throw new IllegalArgumentException(String.format("could not determine type for x,y: %s,%s", x, y));
        }
    }

    private int calculateCost(RegionWithGear from, RegionWithGear to) {
        int cost = 1; // default cost if gear from/to is OK

        if (!from.getGear().equals(to.getGear())) {
            cost += 7;
        }

        return cost;
    }

    private void createRegionsWithGear() {
        for (int x = 0; x < cave.length; x++) {
            for (int y = 0; y < cave[0].length; y++) {
                if (x == 0 && y == 0) {
                    // mouth
                    listRegionWithGear.add(new RegionWithGear(x, y, TORCH));
                } else {
                    switch (cave[x][y]) {
                        case ROCKY:
                            listRegionWithGear.add(new RegionWithGear(x, y, CLIMBING_GEAR));
                            listRegionWithGear.add(new RegionWithGear(x, y, TORCH));
                            break;
                        case WET:
                            listRegionWithGear.add(new RegionWithGear(x, y, CLIMBING_GEAR));
                            listRegionWithGear.add(new RegionWithGear(x, y, NEITHER));
                            break;
                        case NARROW:
                            listRegionWithGear.add(new RegionWithGear(x, y, TORCH));
                            listRegionWithGear.add(new RegionWithGear(x, y, NEITHER));
                            break;
                        default:
                            throw new IllegalArgumentException("unknown element: " + cave[x][y]);
                    }
                }
            }
        }
    }

    public int calculateShortestDistance(RegionWithGear src, RegionWithGear dst) {
        // use the Dijkstra algorithm

        // create a set with unvisited regions
        Set<RegionWithGear> unvisitedRegions = new HashSet<>();
        unvisitedRegions.addAll(listRegionWithGear);

        // create a map with points and (minimum) distance, initially "infinite", except source which has distance 0
        Map<RegionWithGear, Integer> mapRegionWithDistance = new TreeMap<>();
        unvisitedRegions.stream()
                .forEach(ur -> {
                    if (ur.equals(src)) {
                        mapRegionWithDistance.put(ur, 0);
                    } else {
                        mapRegionWithDistance.put(ur, INFINITE);
                    }
                });

        // create a map with the parent
        Map<RegionWithGear, RegionWithGear> mapParents = new TreeMap<>();
        // src is its own parent
        mapParents.put(src, src);

        // remove the source from unvisited nodes
        unvisitedRegions.removeIf(fn -> fn.equals(src));

        // start with source
        RegionWithGear currentRegion = src;

        boolean reachedDestination = false;
        boolean noRoutePossible = false;

        while (!unvisitedRegions.isEmpty() && !reachedDestination && !noRoutePossible) {
            if (unvisitedRegions.size() % 100 == 0) {
                System.out.println("#unvisitedRegions = " + unvisitedRegions.size());
            }
            List<RegionWithGear> unvisitedNeighbours = getNeighbours(currentRegion);
            // remove the neighbours which have already been visited
            unvisitedNeighbours.removeIf(un -> !unvisitedRegions.contains(un));

            // update the distance to these neighbours if closer through this node
            final RegionWithGear cr = currentRegion;
            unvisitedNeighbours
                    .forEach(un -> {
                        int currentDistanceToNeighbour = mapRegionWithDistance.get(cr) + calculateCost(cr, un);
                        if (currentDistanceToNeighbour < mapRegionWithDistance.get(un)) {
                            mapRegionWithDistance.put(un, currentDistanceToNeighbour);
                            // add parent
                            mapParents.put(un, cr);
                        }
                    });

            // remove current region from unvisited set
            unvisitedRegions.remove(currentRegion);

            // check if we are done
            if (currentRegion.equals(dst)) {
//                reachedDestination = true;
            }

            noRoutePossible = unvisitedRegions.stream()
                    .filter(s -> !(mapRegionWithDistance.get(s) == INFINITE))
                    .count() == 0;

            if (!noRoutePossible) {
                // set new current region to the unvisited node with the smallest distance
                currentRegion = unvisitedRegions.stream()
                        .min(Comparator.comparingInt(ur -> mapRegionWithDistance.get(ur))
                        )
                        .get();
            }
        }

        printShortestPath(mapParents, dst, src);

        return mapRegionWithDistance.get(dst);
    }

    private void printShortestPath(Map<RegionWithGear, RegionWithGear> mapParents, RegionWithGear dst, RegionWithGear src) {
        System.out.println(mapParents.get(dst) + "->" + dst);
        if (!src.equals(dst)) {
            printShortestPath(mapParents, mapParents.get(dst), src);
        }
    }

    private boolean inParentChain(Map<RegionWithGear, RegionWithGear> mapParents, RegionWithGear region) {
        if (mapParents.containsKey(region)) {
            return true;
        } else if (region.equals(mapParents.get(region))) {
            return false;
        } else if (mapParents.get(region) == null) {
            return false;
        } else {
            return inParentChain(mapParents, mapParents.get(region));
        }
    }

    private List<RegionWithGear> getNeighbours(RegionWithGear currentRegion) {
        List<RegionWithGear> neighbours =
                listRegionWithGear.stream()
                        .filter(r -> areNeighbours(currentRegion, r))
                        .collect(Collectors.toList());

        return neighbours;
    }

    private boolean areNeighbours(RegionWithGear r1, RegionWithGear r2) {
        if (r1.getX() == r2.getX() && Math.abs(r1.getY() - r2.getY()) == 1) return true;
        if (r1.getY() == r2.getY() && Math.abs(r1.getX() - r2.getX()) == 1) return true;

        return false;
    }

}


public class Day_22 {

    private static String FILENAME = "input_22.txt";

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        List<String> inputLines = readFile(args);

        int depth = Integer.valueOf((inputLines.get(0).split(" ")[1]));
        String[] words = inputLines.get(1).replace(",", " ").split(" ");
        int targetX = Integer.valueOf(words[1]);
        int targetY = Integer.valueOf(words[2]);

        Cave cave = new Cave(new Square(targetX, targetY), depth);

        cave.print();

        long regionRisklevel = cave.calculateRegionRisklevel(0, 0, targetX, targetY);
        System.out.println("regionRisklevel = " + regionRisklevel);

        LocalTime finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
        System.out.println();

        // part 2

        start = LocalTime.now();

        RegionWithGear startRegion = new RegionWithGear(0, 0, TORCH);
        RegionWithGear finishRegion = new RegionWithGear(targetX, targetY, TORCH);
        int duration = cave.calculateShortestDistance(startRegion, finishRegion);
        System.out.println("\nshortest duration in minutes= " + duration);

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
        List<String> input=Files.lines(Paths.get(fileName)).collect(Collectors.toList());
        System.out.println("read file: " + fileName);
        return input;
    }
}