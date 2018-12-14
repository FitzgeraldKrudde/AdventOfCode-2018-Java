package com.ing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;

public class Day_14 {

    private static String fileName = "input_14.txt";

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        // get first line of input with the initial state
        int nrRecipes = Integer.valueOf(Files.lines(Paths.get(fileName))
                .findFirst()
                .get());

        StringBuilder scoreboard = new StringBuilder("37");
        int positionElfe0 = 0;
        int positionElfe1 = 1;

        while (scoreboard.length() < nrRecipes + 10) {
            int scorePositionElfe0 = Character.getNumericValue(scoreboard.charAt(positionElfe0));
            int scorePositionElfe1 = Character.getNumericValue(scoreboard.charAt(positionElfe1));
            scoreboard = scoreboard.append((scorePositionElfe0 + scorePositionElfe1));
            positionElfe0 = (positionElfe0 + 1 + Character.getNumericValue(scoreboard.charAt(positionElfe0))) % scoreboard.length();
            positionElfe1 = (positionElfe1 + 1 + Character.getNumericValue(scoreboard.charAt(positionElfe1))) % scoreboard.length();
        }

        // get the scores of the 10 recipes after the #nrRecipes
        String scoresTenRecipes = scoreboard.substring(nrRecipes, nrRecipes + 10);
        System.out.println("\nscores 10 recipes: " + scoresTenRecipes);

        LocalTime finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());

        // part 2

        start = LocalTime.now();

        String scoreToFind = String.valueOf(nrRecipes);
        scoreboard = new StringBuilder("37");
        positionElfe0 = 0;
        positionElfe1 = 1;

        boolean found = false;
        while (!found) {
            int scorePositionElfe0 = Character.getNumericValue(scoreboard.charAt(positionElfe0));
            int scorePositionElfe1 = Character.getNumericValue(scoreboard.charAt(positionElfe1));
            scoreboard = scoreboard.append((scorePositionElfe0 + scorePositionElfe1));
            positionElfe0 = (positionElfe0 + 1 + Character.getNumericValue(scoreboard.charAt(positionElfe0))) % scoreboard.length();
            positionElfe1 = (positionElfe1 + 1 + Character.getNumericValue(scoreboard.charAt(positionElfe1))) % scoreboard.length();

            if (scoreboard.length() > 2 * scoreToFind.length()) {
                // performance optimisation: do not indexOf on the whole (huge) String
                found = scoreboard.substring(scoreboard.length() - scoreToFind.length() - 5).indexOf(scoreToFind) != -1;
            }
        }

        // get the number of scores of the recipes before the #nrRecipes
        int startPos = scoreboard.indexOf(String.valueOf(nrRecipes));
        System.out.println("\n#recipes before: " + startPos);

        finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
    }
}