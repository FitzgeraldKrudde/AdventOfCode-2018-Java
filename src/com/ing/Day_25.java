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
import java.util.List;

import static java.util.stream.Collectors.toList;

@AllArgsConstructor
@Data
class SpaceTimePoint {
    private int x;
    private int y;
    private int z;
    private int r;

    public int manhattanDistance(SpaceTimePoint other) {
        int distanceX = Math.abs(x - other.getX());
        int distanceY = Math.abs(y - other.getY());
        int distanceZ = Math.abs(z - other.getZ());
        int distanceR = Math.abs(r - other.getR());

        return distanceX + distanceY + distanceZ + distanceR;
    }

    public boolean isConstellationCombineable(List<SpaceTimePoint> constellation) {
        boolean canCombine;

        canCombine = constellation.stream()
                .anyMatch(point -> manhattanDistance(point) <= 3);

        return canCombine;
    }
}

public class Day_25 {

    private static String FILENAME = "input_25.txt";

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        List<String> inputLines = readFile(args);

        List<SpaceTimePoint> spaceTimePoints = new ArrayList<>();
        inputLines.stream()
                .forEach(
                        line -> {
                            String[] words = line.replace(" ", "")
                                    .replace(",", " ")
                                    .split(" ");
                            spaceTimePoints.add(new SpaceTimePoint(
                                    Integer.parseInt(words[0]),
                                    Integer.parseInt(words[1]),
                                    Integer.parseInt(words[2]),
                                    Integer.parseInt(words[3])
                            ));
                        }
                );
        System.out.println("\n#spaceTimePoints = " + spaceTimePoints.size());
        List<List<SpaceTimePoint>> constellations = new ArrayList<>();

        // initially put all spacepoint in each own constellation
        spaceTimePoints.stream()
                .forEach(spacetimepoint -> constellations.add(new ArrayList<>(Arrays.asList(spacetimepoint))));

        // try to combine constellations
        List<List<SpaceTimePoint>> newConstellations = new ArrayList<>();
        List<List<SpaceTimePoint>> constellationsAlreadyMerged = new ArrayList<>();

        int iterations=0;
        boolean stillMerging = true;
        while (stillMerging) {
            stillMerging = false;
            for (int i = 0; i < constellations.size(); i++) {
                List<SpaceTimePoint> newConstellation = new ArrayList<>();
                List<SpaceTimePoint> l1 = constellations.get(i);
                if (!constellationsAlreadyMerged.contains(l1)) {
                    newConstellation.addAll(l1);
                    for (int j = i + 1; j < constellations.size(); j++) {
                        List<SpaceTimePoint> l2 = constellations.get(j);
                        if (!constellationsAlreadyMerged.contains(l2)) {
                            // is there a point in Constellation 1 which can connect to any point in Constellation 2
                            if (l1.stream().anyMatch(spaceTimePoint -> spaceTimePoint.isConstellationCombineable(l2))) {
                                newConstellation.addAll(l2);
                                constellationsAlreadyMerged.add(l2);
                            }
                        }
                    }
                    newConstellations.add(newConstellation);
                }
            }

            if (constellations.size() != newConstellations.size()) {
                stillMerging = true;
                iterations++;
                constellations.clear();
                constellations.addAll(newConstellations);
                newConstellations.clear();
            }
        }

        constellations.stream().forEach(c -> System.out.println("c = " + c));
        System.out.println("#constellations = " + constellations.size());
        System.out.println("iterations = " + iterations);

        LocalTime finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
        System.out.println();

        // part 2

        start = LocalTime.now();


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