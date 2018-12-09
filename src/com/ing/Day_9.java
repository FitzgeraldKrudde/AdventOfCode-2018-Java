package com.ing;

import lombok.ToString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@ToString
class Board {
    // "circle" represented by a List
    final ArrayList<Integer> circle = new ArrayList<>();

    // players score, 0-based
    final List<Integer> playersScore = new ArrayList<>();

    // current marble
    int currentMarble = 0;

    Board(int nrMarbles, int nrPlayers) {
        circle.ensureCapacity(nrMarbles);
        initialisePlayersScore(nrPlayers);
    }

    private void initialisePlayersScore(int nrPlayers) {
        IntStream.range(0, nrPlayers)
                .forEach(p -> playersScore.add(0));
    }

    public void placeMarble(int marble) {
        showProgress(marble);

        if (marble == 0) {
            circle.add(0);
            currentMarble = 0;
        } else if (marble == 1) {
            circle.add(1);
            currentMarble = 1;
        } else if (marble % 23 != 0) {
            // find the new place for the marble
            int newPosition = (currentMarble + 2) % circle.size();
            // place/insert the marble
            circle.add(newPosition, marble);
            // update the current marble
            currentMarble = newPosition;
        } else {
            int currentPlayer = marble % playersScore.size();
            int currentScore = playersScore.get(currentPlayer);
            // get the value of the marble 7 positions counter clockwise
            int marble7PositionCounterClockwise = (currentMarble - 7 + circle.size()) % circle.size();
            int marble7PositionCounterClockwiseValue = circle.remove(marble7PositionCounterClockwise);
            // calculate the new score, add:
            // - the marble (to be placed)
            // - the value of the marble 7 positions counter clockwise
            int newScore = currentScore + marble + marble7PositionCounterClockwiseValue;
            playersScore.set(currentPlayer, newScore);
            // update the current marble, 1 clockwise of the removed marble
            currentMarble = (marble7PositionCounterClockwise + circle.size()) % circle.size();
        }
    }

    private void showProgress(int marble) {
        if (marble > 0) {
            if (marble % 100000 == 0) {
                System.out.print('.');
            }
            if (marble % 1000000 == 0) {
                System.out.print('#');
            }
        }
    }

    public int getHighestScore() {
        return playersScore.stream()
                .max(Comparator.comparingInt(ps -> ps.intValue()))
                .get();
    }

    public void clear(int nrMarbles, int nrPlayers) {
        circle.clear();
        circle.ensureCapacity(nrMarbles);
        playersScore.clear();
        currentMarble = 0;
        initialisePlayersScore(nrPlayers);
    }
}

public class Day_9 {

    private static String fileName = "input_9.txt";

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

//        432 players; last marble is worth 71019 points

        // get 1 line of input
        String input = Files.lines(Paths.get(fileName))
                .findFirst()
                .get();

        String[] words = input.split(" ");
        int nrPlayers = Integer.parseInt(words[0]);
        int highestMarble = Integer.parseInt(words[6]);
        System.out.println("\nnrPlayers: " + nrPlayers);
        System.out.println("highest marble: " + highestMarble);

        // create the board
        final Board board = new Board(highestMarble + 1, nrPlayers);

        // place the marbles
        IntStream.range(0, highestMarble).forEach(marble -> board.placeMarble(marble));

        int highestScore = board.getHighestScore();
        System.out.println("highestScore: " + highestScore);

        LocalTime finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());

        //part 2

        start = LocalTime.now();

        // a bit (..) more marbles
        highestMarble *= 100;
        System.out.println("\nnrPlayers: " + nrPlayers);
        System.out.println("highest marble: " + highestMarble);

        // clear the board
        board.clear(highestMarble + 1, nrPlayers);

        // place the marbles
        IntStream.range(0, highestMarble).forEach(marble -> board.placeMarble(marble));

        highestScore = board.getHighestScore();
        System.out.println("\nhighestScore: " + highestScore);

        finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
        System.out.println("duration (ms): " + Duration.between(start, finish).toSeconds());
        System.out.println("duration (ms): " + Duration.between(start, finish).toMinutes());
    }

}