package com.ing;

import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day_12 {

    private static String fileName = "input_12.txt";

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        // get first line of input with the initial state
        char[] initialState = Files.lines(Paths.get(fileName))
                .findFirst()
                .get()
                .chars()
                .filter(c -> c == '.' || c == '#')
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining())
                .trim().toCharArray();

        // get the input lines starting from the third line for the rules
        List<String> rules =
                Files.lines(Paths.get(fileName))
                        .skip(2)
                        .map(line -> line.chars()
                                .filter(c -> c == '.' || c == '#')
                                .mapToObj(c -> String.valueOf((char) c))
                                .collect(Collectors.joining())
                                .trim()
                        )
                        .collect(Collectors.toList());

        int nrGenerations = 20;
        GrowFarm growFarm = new GrowFarm(nrGenerations, initialState, rules);

        System.out.println(String.format("%03d", 0) + growFarm.getPlants());
        for (int generation = 0; generation < nrGenerations; generation++) {
            growFarm.nextGeneration();
            System.out.println(String.format("%03d", generation) + growFarm.getPlants());
        }
        System.out.println("\nsum of the positions of pots with plants: " + growFarm.getSumOfPotsPositionWithPlants());

        LocalTime finish = LocalTime.now();
        System.out.println("\nduration (ms): " + Duration.between(start, finish).toMillis());

        // part 2

        start = LocalTime.now();

        nrGenerations = 200;
        growFarm = new GrowFarm(nrGenerations, initialState, rules);
        for (int generation = 0; generation < nrGenerations; generation++) {
            growFarm.nextGeneration();
            System.out.println(String.format("%03d", generation) + growFarm.getPlants());
        }

        // calculate the sum for 50000000000 generations
        long totalGenerations = 50000000000L;

        // a sample (200 generations) shows a repeating/moving pattern
        // i.e. calculate the extra point for the remaining generations
        int nrPlants = growFarm.getNrOfPlants();
        long sum = growFarm.getSumOfPotsPositionWithPlants();
        sum += (totalGenerations - nrGenerations) * nrPlants;
        System.out.println("\nsum after " + totalGenerations + " generations: " + sum);

        finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
    }

    @ToString
    @Getter
    static class GrowFarm {
        int nrInitialPots;
        int nrGenerations;
        private String plants;
        // compact rule: first 5 characters are the rule, 6th position is the result when the rule applies
        private String[] rules;

        GrowFarm(int nrGenerations, char[] initialPots, List<String> rules) {
            this.nrGenerations = nrGenerations;
            this.nrInitialPots = initialPots.length;
            this.rules = rules.toArray(new String[rules.size()]);

            // initialize the plants
            // as size use the number of number of plants + 2*generations (for growing space)
            StringBuilder sb = new StringBuilder();
            char[] noPlants = new char[nrGenerations];
            Arrays.fill(noPlants, '.');
            sb.append(new String(noPlants));
            sb.append(initialPots);
            sb.append(new String(noPlants));
            plants = sb.toString();
        }

        public void nextGeneration() {
            // initialise new plants with no plants
            StringBuilder newPlants = new StringBuilder();
            char[] noPlants = new char[plants.length()];
            Arrays.fill(noPlants, '.');
            newPlants.append(noPlants);

            for (int rule = 0; rule < rules.length; rule++) {
                String escapedRule = rules[rule].substring(0, rules[rule].length() - 1);
                Pattern pattern = Pattern.compile(escapedRule, Pattern.LITERAL);
                Matcher matcher = pattern.matcher(plants);
                int pos = 0;
                while (matcher.find(pos)) {
                    newPlants.setCharAt(matcher.start() + 2, rules[rule].charAt(5));
                    pos = matcher.start() + 1;
                }
            }

            plants = newPlants.toString();
        }

        public int getSumOfPotsPositionWithPlants() {
            int sum = 0;

            Pattern pattern = Pattern.compile("#");
            Matcher matcher = pattern.matcher(plants);
            while (matcher.find()) {
                sum += matcher.start() - nrGenerations;
            }

            return sum;
        }

        public int getNrOfPlants() {
            return (int) plants.chars()
                    .filter(c -> c == '#')
                    .count();
        }
    }
}