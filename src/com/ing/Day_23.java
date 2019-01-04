package com.ing;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@AllArgsConstructor
@Data
class NanoBot {
    private long x;
    private long y;
    private long z;
    private long r;

    public long distanceCenters(NanoBot other) {
        return
                Long.max(x, other.getX()) - Long.min(x, other.getX()) +
                        Long.max(y, other.getY()) - Long.min(y, other.getY()) +
                        Long.max(z, other.getZ()) - Long.min(z, other.getZ());
    }

    public boolean inRange(NanoBot other) {
        return distanceCenters(other) <= r;
    }

    public boolean intersect(NanoBot other) {
        return distanceCenters(other) <= r + other.getR();
    }

    public long intersectLength(NanoBot other) {
        return r + other.getR() - distanceCenters(other);
    }
}

public class Day_23 {

    private static String FILENAME = "input_23.txt";

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        List<String> inputLines = readFile(args);

        List<NanoBot> nanobots = new ArrayList<>();
        inputLines.stream()
                .forEach(
                        line -> {
                            String[] words = line.replace("pos=<", "")
                                    .replace(">, r=", " ")
                                    .replaceAll(",", " ")
                                    .split(" ");
                            nanobots.add(new NanoBot(
                                    Long.parseLong(words[0]),
                                    Long.parseLong(words[1]),
                                    Long.parseLong(words[2]),
                                    Long.parseLong(words[3])
                            ));
                        }
                );
        System.out.println("\n#nanobots = " + nanobots.size());
        NanoBot strongestNanobot = nanobots.parallelStream()
                .max(Comparator.comparingLong(n -> n.getR()))
                .get();
        System.out.println("strongestNanobot = " + strongestNanobot);

        long nrBotsInRange =
                nanobots.stream()
                        .filter(n -> strongestNanobot.inRange(n))
                        .count();
        System.out.println("nrBotsInRange = " + nrBotsInRange);

        LocalTime finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
        System.out.println();

        // part 2

        start = LocalTime.now();

        Map<String, List<Integer>> mapListIntersections = new TreeMap<>();
        for (int i = 0; i < nanobots.size(); i++) {
            List<Integer> intersections = new ArrayList<>();
            for (int j = 0; j < nanobots.size(); j++) {
                if (nanobots.get(i).intersect(nanobots.get(j))) {
                    intersections.add(j);
                }
            }
            mapListIntersections.put("set" + i, intersections);
        }

        mapListIntersections.entrySet().stream()
                .forEach(me -> System.out.println("nanobot: " + me.getKey() + " intersects with #" + me.getValue().size() + " other nanobots"));

        List<Map.Entry<Integer, String>> listElementSet =
                mapListIntersections.entrySet().stream()
                        .flatMap(me -> me.getValue().stream()
                                .map(i -> new AbstractMap.SimpleEntry<>(i, me.getKey())
                                ))
                        .collect(toList());

        Map<Integer, List<String>> mapElementSets =
                listElementSet.stream()
                        .collect(Collectors.toMap(
                                k -> k.getKey(),
                                v -> new ArrayList<>(Collections.singletonList(v.getValue())),
                                (o, n) -> {
                                    o.addAll(n);
                                    return o;
                                }));

        List<Map.Entry<List<String>, Integer>> listSetsElement = mapElementSets.entrySet().stream()
                .map(me -> new AbstractMap.SimpleEntry<>(
                        me.getValue(),
                        me.getKey())
                ).collect(toList());

        Map<List<String>, List<Integer>> mapSetsElements =
                listSetsElement.stream()
                        .collect(Collectors.toMap(
                                k -> k.getKey(),
                                v -> new ArrayList<>(Arrays.asList(v.getValue())),
                                (o, n) -> {
                                    o.addAll(n);
                                    return o;
                                }));

        System.out.println();
        Map.Entry<List<String>, List<Integer>> heatSet = mapSetsElements.entrySet().stream()
                .filter(lse -> lse.getValue().size() > 1)
                .sorted((lse1, lse2) -> lse1.getValue().size() - lse2.getValue().size())
                .max(Comparator.comparingLong(lse -> lse.getValue().size()))
                .get();
        System.out.println("heatSet = " + heatSet);

        List<Integer> heatSpheres = heatSet.getValue();

        Map<Long, Map.Entry<Integer, Integer>> map = new TreeMap<>();
        for (int i = 0; i < heatSpheres.size(); i++) {
            for (int j = 0; j < heatSpheres.size(); j++) {
                NanoBot nanoBot1 = nanobots.get(heatSpheres.get(i));
                NanoBot nanoBot2 = nanobots.get(heatSpheres.get(j));
                if (!nanoBot1.equals(nanoBot2)) {
                    if (nanoBot1.intersect(nanoBot2)) {
                        map.put(nanoBot1.intersectLength(nanoBot2), new AbstractMap.SimpleEntry<>(heatSpheres.get(i), heatSpheres.get(j)));
                    } else {
                        throw new IllegalStateException("dubious");
                    }
                }
            }
        }

        Map.Entry<Long, Map.Entry<Integer, Integer>> min = map.entrySet().stream()
                .min(Comparator.comparingLong(me -> me.getKey()))
                .get();

        System.out.println("\nmin = " + min);
        NanoBot bot1 = nanobots.get(min.getValue().getKey());
        NanoBot bot2 = nanobots.get(min.getValue().getValue());
        System.out.println("bot1 = " + bot1);
        System.out.println("bot2 = " + bot2);
        System.out.println("intersect: " + bot1.intersect(bot2));
        System.out.println("intersectLength: " + bot1.intersectLength(bot2));

        double lineDistance = bot1.getR() + bot2.getR();
        double distanceFromBot1 = bot1.getR();
        double distanceFromBot2 = bot2.getR();
        double dD = distanceFromBot1 / lineDistance;
        double x3 = bot1.getX() + dD * (bot2.getX() - bot1.getX());
        double y3 = bot1.getY() + dD * (bot2.getY() - bot1.getY());
        double z3 = bot1.getZ() + dD * (bot2.getZ() - bot1.getZ());

        System.out.println(String.format("x3/y3/z3: %f/%f/%f", x3, y3, z3));
        System.out.println(String.format("manhattan distance: %f", x3 + y3 + z3));
        System.out.println();

         dD = distanceFromBot2 / lineDistance;
         x3 = bot2.getX() + dD * (bot1.getX() - bot2.getX());
         y3 = bot2.getY() + dD * (bot1.getY() - bot2.getY());
         z3 = bot2.getZ() + dD * (bot1.getZ() - bot2.getZ());

        System.out.println(String.format("x3/y3/z3: %f/%f/%f", x3, y3, z3));
        System.out.println(String.format("manhattan distance: %f", x3 + y3 + z3));



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