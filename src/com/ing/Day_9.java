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
    final List<Long> playersScore = new ArrayList<>();

    // current marble
    int currentMarble = 0;

    // optimisation, needed for part 2
    // use a sublist to work on with the following initial size
    int workingListSize = 30000;
    // and 3 lists for the splitted circle: before, working area and after
    ArrayList<Integer> before = new ArrayList<>();
    ArrayList<Integer> workingList = new ArrayList<>(workingListSize);
    ArrayList<Integer> after = new ArrayList<>();
    private boolean usingWorkingList = false;

    Board(int highestMarble, int nrPlayers) {
        circle.ensureCapacity(highestMarble + 1);
        initialisePlayersScore(nrPlayers);
    }

    private void initialisePlayersScore(int nrPlayers) {
        IntStream.range(0, nrPlayers)
                .forEach(p -> playersScore.add(0L));
    }

    public void placeMarble(final int marble) {
        showProgress(marble);

        // verify if the working area is safe or we should join and split
        verifyOrRecreateWorkarea();

        if (marble == 0) {
            circle.add(0);
            currentMarble = 0;
        } else if (marble == 1) {
            circle.add(1);
            currentMarble = 1;
        } else if (marble % 23 != 0) {
            if (usingWorkingList) {
                // find the new place for the marble
                int newPositionInWorkingList = calculatePositionInWorkingList() + 2;
                // place/insert the marble
                workingList.add(newPositionInWorkingList, marble);
                currentMarble += 2;
            } else {
                // find the new place for the marble
                int newPosition = (currentMarble + 2) % circle.size();
                // place/insert the marble
                circle.add(newPosition, marble);
                currentMarble = newPosition;
            }
        } else {
            int currentPlayer = marble % playersScore.size();
            long currentScore = playersScore.get(currentPlayer);
            int marble7PositionCounterClockwise;
            int marble7PositionCounterClockwiseValue;
            if (usingWorkingList) {
                // get the value of the marble in the working list,7 positions counter clockwise
                marble7PositionCounterClockwise = calculatePositionInWorkingList() - 7;
                marble7PositionCounterClockwiseValue = workingList.remove(marble7PositionCounterClockwise);
                currentMarble = currentMarble - 7;
            } else {
                // get the value of the marble in the circle,7 positions counter clockwise
                marble7PositionCounterClockwise = (currentMarble - 7 + circle.size()) % circle.size();
                marble7PositionCounterClockwiseValue = circle.remove(marble7PositionCounterClockwise);
                // check if last position was removed, nasty edge case
                if (marble7PositionCounterClockwise == circle.size()) {
                    currentMarble = 0;
                } else {
                    currentMarble = marble7PositionCounterClockwise;
                }
            }
            // calculate the new score, add:
            // - the marble (to be placed)
            // - the value of the marble 7 positions counter clockwise
            long newScore = currentScore + marble + marble7PositionCounterClockwiseValue;
            playersScore.set(currentPlayer, newScore);
        }
    }

    private int calculatePositionInWorkingList() {
        return currentMarble - before.size();
    }

    private void verifyOrRecreateWorkarea() {
        if (usingWorkingList) {
            // check if the current marble gets too close to the start or end of the working area
            // as convenience use a safe distance of 10
            if (((currentMarble - before.size() < 10)
                    || ((before.size() + workingList.size() - currentMarble) < 10))
            ) {
                join();
                if (canWeSplit()) {
                    split();
                }
            }
        } else {
            if (canWeSplit()) {
                split();
            }
        }
    }

    private void join() {
        System.out.print('j');
        circle.clear();
        circle.ensureCapacity(before.size() + workingList.size() + after.size());
        circle.addAll(before);
        System.out.print('+');
        circle.addAll(workingList);
        System.out.print('+');
        circle.addAll(after);
        System.out.print('+');

        usingWorkingList = false;
        System.out.print('J');
    }

    private void split() {
        System.out.print('s');

        int workAreaMinIndex = currentMarble - 10;
        int workAreaMaxIndex = currentMarble + (workingListSize - 10);

        before.clear();
        before.ensureCapacity(workAreaMinIndex);
        before.addAll(circle.subList(0, workAreaMinIndex));

        workingList.clear();
        workingList.addAll(circle.subList(workAreaMinIndex, workAreaMaxIndex));

        after.clear();
        after.ensureCapacity(circle.size() - workAreaMaxIndex);
        after.addAll(circle.subList(workAreaMaxIndex, circle.size()));

        circle.clear();
        usingWorkingList = true;

        System.out.print('S');
    }

    private boolean canWeSplit() {
        boolean canSplit = circle.size() > workingListSize + 100 && currentMarble > 10 && currentMarble < (circle.size() - workingListSize + 30);
        return canSplit;
    }

    private void showProgress(int marble) {
        if (marble > 0) {
            if (marble % 100000 == 0) {
                System.out.print('.');
            }
            if (marble % 1000000 == 0) {
                System.out.print("\n#");
            }
        }
    }

    public long getHighestScore() {
        return playersScore.stream()
                .max(Comparator.comparingLong(ps -> ps.longValue()))
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
        final Board board = new Board(highestMarble, nrPlayers);

        // place the marbles
        IntStream.range(0, highestMarble + 1).forEach(marble -> board.placeMarble(marble));

        long highestScore = board.getHighestScore();
        System.out.println("\nhighestScore: " + highestScore);

        LocalTime finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());

        // part 2

        start = LocalTime.now();

        // a bit (..) more marbles
        highestMarble *= 100;
        System.out.println("\nnrPlayers: " + nrPlayers);
        System.out.println("highest marble: " + highestMarble);

        // clear the board
        board.clear(highestMarble + 1, nrPlayers);

        // place the marbles
        IntStream.range(0, highestMarble + 1).forEach(marble -> board.placeMarble(marble));

        highestScore = board.getHighestScore();
        System.out.println("\nhighestScore: " + highestScore);

        finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
    }
}