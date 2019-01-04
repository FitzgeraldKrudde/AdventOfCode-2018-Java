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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;


@Data
class Square implements Comparable {
    private int x;
    private int y;

    public Square(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int compareTo(Object o) {
        Square other = (Square) o;
        if (this.y == other.y) {
            return x - other.x;
        } else {
            return y - other.y;
        }
    }
}

@ToString(callSuper = true)
@Getter
@Setter
class Combatant extends Square {
    public final char id = 'X';
    public final static int HITPOINTS = 200;

    int attackPower = 3;
    int hitPoints = HITPOINTS;
    boolean killed = false;

    public Combatant(int x, int y) {
        super(x, y);
    }

    // TODO nicer?!?
    public boolean isEnemy(Combatant combatant) {
        return
                (this instanceof Elf && combatant instanceof Goblin)
                        || (this instanceof Goblin && combatant instanceof Elf);
    }
}

class Elf extends Combatant {
    public char id = 'E';

    public Elf(int x, int y) {
        super(x, y);
    }

    public Elf(int x, int y, int attackPower) {
        super(x, y);
        this.attackPower = attackPower;
    }

}

class Goblin extends Combatant {
    public char id = 'G';

    public Goblin(int x, int y) {
        super(x, y);
    }
}

@Getter
@Setter
@ToString
class CombatArea {
    private static final int INFINITE = 9999;
    @ToString.Exclude
    public final char WALL = '#';
    @ToString.Exclude
    public final char OPEN_AREA = '.';
    @ToString.Exclude
    public final char GOBLIN = 'G';
    @ToString.Exclude
    public final char ELF = 'E';

    private char[][] combatArea;
    List<Elf> elfs = new ArrayList<>();
    List<Goblin> goblins = new ArrayList<>();
    private int elfAttackPower = 3;

    public CombatArea(int x, int y) {
        combatArea = new char[x][y];
    }

    public void print() {
        System.out.print("    ");
        IntStream.range(0, combatArea.length)
                .forEach(i -> System.out.print(i % 10));
        System.out.println();

        IntStream.range(0, combatArea[0].length)
                .forEach(y -> {
                    System.out.print(String.format("%03d ", y));
                    IntStream.range(0, combatArea.length)
                            .forEach(x -> {
                                if (elfs.stream()
                                        .filter(e -> e.getX() == x && e.getY() == y)
                                        .count() > 0) {
                                    System.out.print(ELF);
                                } else if (goblins.stream()
                                        .filter(e -> e.getX() == x && e.getY() == y)
                                        .count() > 0) {
                                    System.out.print(GOBLIN);
                                } else {
                                    System.out.print(combatArea[x][y]);
                                }
                            });
                    // print elfs/goblins
                    System.out.print(" ");
                    combatants().stream()
                            .filter(c -> c.getY() == y)
                            .sorted()
                            .forEach(c -> {
                                if (c instanceof Goblin) {
                                    System.out.print(GOBLIN);
                                } else {
                                    System.out.print(ELF);
                                }
                                System.out.print("(" + c.getHitPoints() + ") ");
                            });
                    ;
                    System.out.println();
                });
    }

    private List<Combatant> combatants() {
        List<Combatant> al = new ArrayList<>();
        al.addAll(goblins);
        al.addAll(elfs);
        return al;
    }

    public void takeTurn(Combatant combatant) {
        //  check if this combatant has not been killed already during this round
        if (combatant.isKilled()) {
            System.out.println("combatant has already been killed: " + combatant);
            return;
        }

        // attack immediately if possible
        //  otherwise move and then attack if possible
        Optional<Combatant> enemy = canAttack(combatant);
        if (enemy.isPresent()) {
            attack(combatant, enemy.get());
        } else if (canMove(combatant)) {
            // use move or moveAlt
//            move(combatant);
            moveAlt(combatant);
            enemy = canAttack(combatant);
            if (enemy.isPresent()) {
                attack(combatant, enemy.get());
            }
        }
    }

    private void attack(Combatant attacker, Combatant victim) {
        victim.setHitPoints(victim.getHitPoints() - attacker.getAttackPower());

        if (victim.getHitPoints() <= 0) {
            // victim dead, mark as dead and remove
            victim.setKilled(true);
            combatArea[victim.getX()][victim.getY()] = OPEN_AREA;

            // remove from the Elf or Goblin list
            if (!elfs.removeIf(elf -> elf.equals(victim))) {
                if (!goblins.removeIf(goblin -> goblin.equals(victim))) {
                    throw new IllegalStateException("could not find combatant to remove");
                }
            }
        }
    }

    private void moveAlt(Combatant combatant) {
        Optional<Square> chosenEnemyNeighbourSquare =
                enemies(combatant).parallelStream()
                        .flatMap(e -> getNeighbourSquares(e).stream()) // we now got the neighbour squares around all enemies
                        .filter(ens -> isFreeSquare(ens.getX(), ens.getY())) // only use free squares
                        .distinct()
                        .collect(toMap(k -> k,
                                ens -> calculateShortestDistance(combatant, (Square) ens) // ens=enemy neighbour square
                        ))
                        .entrySet() // now we have a set of <enemy neighbour square, distance>
                        .stream()
                        .filter(me -> me.getValue() != INFINITE) // filter Infinite
                        // get minimum based on: distance, enemy neighbour square
                        .min(Comparator.comparingInt(me -> ((Map.Entry<Square, Integer>) me).getValue())
                                .thenComparing(me -> ((Map.Entry<Square, Integer>) me).getKey()))
                        .map(me -> ((Map.Entry<Square, Integer>) me).getKey());

        if (chosenEnemyNeighbourSquare.isPresent()) {
            Optional<Square> chosenMyNeighbourSquare =
                    getNeighbourSquares(combatant).parallelStream() // we now got all our neighbour squares
                            .filter(e -> isFreeSquare(e.getX(), e.getY())) // only use free squares
                            .collect(toMap(k -> k,
                                    ns -> calculateShortestDistance(ns, chosenEnemyNeighbourSquare.get()) // ens->enemy neighbour square
                            ))
                            .entrySet() // now we have a set of <our own neighbour square, <src neighbour square, distance>>
                            .stream()
                            .filter(me -> me.getValue() != INFINITE) // filter Infinite
                            // get minimum based on: distance, our own neighbour square
                            .min(Comparator.comparingInt(m -> ((Map.Entry<Square, Integer>) m).getValue())
                                    .thenComparing(m -> ((Map.Entry<Square, Integer>) m).getKey())
                            )
                            .map(me -> ((Map.Entry<Square, Integer>) me).getKey());

            if (chosenMyNeighbourSquare.isPresent()) {
                // move
                combatant.setX(chosenMyNeighbourSquare.get().getX());
                combatant.setY(chosenMyNeighbourSquare.get().getY());
            }
        }
    }

    private void move(Combatant combatant) {
        // find square around enemy with shortest path
        Optional<Map.Entry<Square, Map.Entry<Square, Integer>>> mapEntry =
                enemies(combatant).parallelStream()
                        .flatMap(e -> getNeighbourSquares(e).stream()) // we now got the neighbour squares around all enemies
                        .filter(e -> isFreeSquare(e.getX(), e.getY())) // only use free squares
                        .distinct()
                        .collect(toMap(k -> k,
                                ens -> neighbourSquareInClosestPath(combatant, (Square) ens) // ens->enemy neighbour square
                        ))
                        .entrySet() // now we have a set of <enemy neighbour square, <src neighbour square, distance>>
                        .stream()
                        // get minimum based on: distance, enemy neighbour square, src neighbour square
                        .min(Comparator.comparingInt(m -> ((Map.Entry<Square, Map.Entry<Square, Integer>>) m).getValue().getValue())
                                .thenComparing(m -> ((Map.Entry<Square, Map.Entry<Square, Integer>>) m).getKey())
                                .thenComparing(m -> ((Map.Entry<Square, Map.Entry<Square, Integer>>) m).getValue().getKey())
                        );

        if (mapEntry.isPresent()) {
            if (mapEntry.get().getValue().getValue() != INFINITE) {
                Square closestSquareInShortestPathToEnemy = mapEntry.get().getValue().getKey();
                // move
                combatant.setX(closestSquareInShortestPathToEnemy.getX());
                combatant.setY(closestSquareInShortestPathToEnemy.getY());
            }
        }
    }

    private List<Combatant> enemies(Combatant combatant) {
        return
                combatants().stream()
                        .filter(c -> c.isEnemy(combatant))
                        .collect(Collectors.toList());
    }

    private Optional<Combatant> canAttack(Combatant combatant) {
        // check if there is an enemy unit on an adjacent square
        return getNeighbourSquares(combatant)
                .stream()
                .flatMap(ns -> combatants().stream()
                        .filter(c -> c.getX() == ns.getX() && c.getY() == ns.getY())
                )
                .filter(c -> c.isEnemy(combatant))
                .min(Comparator.comparingInt(e -> ((Combatant) e).getHitPoints())
                        .thenComparing(e -> (Square) e));
    }

    private boolean isFreeSquare(int x, int y) {
        if (!isValidSquare(x, y)) return false;
        if (combatArea[x][y] == WALL) return false;

        if (combatants().stream()
                .filter(c -> c.getX() == x && c.getY() == y)
                .count() > 0) return false;

        if (combatArea[x][y] == OPEN_AREA) {
            return true;
        }

        throw new IllegalStateException("could not determine isFreeSquare for x/y: " + x + "/" + y);
    }

    public int calculateShortestDistance(Square src, Square dst) {
        // use the Dijkstra algorithm

        // create a set with unvisited squares
        Set<Square> unvisitedSquares = new HashSet<>();
        for (int x = 0; x < combatArea.length; x++) {
            for (int y = 0; y < combatArea[0].length; y++) {
                if (combatArea[x][y] == OPEN_AREA && !isCombattantAtSquare(x, y)) {
                    unvisitedSquares.add(new Square(x, y));
                }
            }
        }
        // add the src and dst
        unvisitedSquares.add(src);
        unvisitedSquares.add(dst);

        // Map with points and (minimum) distance, initially "infinite", except destination which has distance 0
        Map<Square, Integer> mapSquareWithDistance = new TreeMap<>();
        unvisitedSquares.stream()
                .forEach(un -> {
                    if (un.equals(dst)) {
                        mapSquareWithDistance.put(un, 0);
                    } else {
                        mapSquareWithDistance.put(un, INFINITE);
                    }
                });

        // start with destination
        Square currentSquare = dst;

        boolean reachedSource = false;
        boolean noRoutePossible = false;

        while (!unvisitedSquares.isEmpty() && !reachedSource && !noRoutePossible) {
            List<Square> freeNeighbours = getFreeNeighboursIncludingDestination(currentSquare, src);
            // remove the free neighbours which have already been visited
            freeNeighbours.removeIf(fn -> !unvisitedSquares.contains(fn));
            // check if there are any..

            // update the distance to these neighbours if closer through this node
            int currentDistanceToNeighbour = mapSquareWithDistance.get(currentSquare) + 1;
            freeNeighbours.stream()
                    .forEach(fn -> {
                        if (currentDistanceToNeighbour < mapSquareWithDistance.get(fn)) {
                            mapSquareWithDistance.put(fn, currentDistanceToNeighbour);
                        }
                    });
            // remove current point from unvisited set
            unvisitedSquares.remove(currentSquare);

            // check if we are done
            if (currentSquare.equals(src)) {
                reachedSource = true;
            } else {
                noRoutePossible = unvisitedSquares.stream()
                        .filter(s -> !(mapSquareWithDistance.get(s) == INFINITE))
                        .count() == 0;

                if (!noRoutePossible) {
                    // set new current square to the unvisited node with the smallest distance
                    currentSquare = (Square) unvisitedSquares.stream()
                            .min(Comparator.comparingInt(un -> mapSquareWithDistance.get(un))
                            )
                            .get();
                }
            }
        }

        return mapSquareWithDistance.get(src);
    }

    public Map.Entry<Square, Integer> neighbourSquareInClosestPath(Square src, Square dst) {
        // use the Dijkstra algorithm

        // create a set with unvisited squares
        Set<Square> unvisitedSquares = new HashSet<>();
        for (int x = 0; x < combatArea.length; x++) {
            for (int y = 0; y < combatArea[0].length; y++) {
                if (combatArea[x][y] == OPEN_AREA && !isCombattantAtSquare(x, y)) {
                    unvisitedSquares.add(new Square(x, y));
                }
            }
        }
        // add the src and dst
        unvisitedSquares.add(src);
        unvisitedSquares.add(dst);

        // Map with points and (minimum) distance, initially "infinite", except destination which has distance 0
        Map<Square, Integer> mapSquareWithDistance = new TreeMap<>();
        unvisitedSquares.stream()
                .forEach(un -> {
                    if (un.equals(dst)) {
                        mapSquareWithDistance.put(un, 0);
                    } else {
                        mapSquareWithDistance.put(un, INFINITE);
                    }
                });

        // start with destination
        Square currentSquare = dst;

        boolean reachedSource = false;
        boolean noRoutePossible = false;

        while (!unvisitedSquares.isEmpty() && !reachedSource && !noRoutePossible) {
            List<Square> freeNeighbours = getFreeNeighboursIncludingDestination(currentSquare, src);
            // remove the free neighbours which have already been visited
            freeNeighbours.removeIf(fn -> !unvisitedSquares.contains(fn));
            // check if there are any..

            // update the distance to these neighbours if closer through this node
            int currentDistanceToNeighbour = mapSquareWithDistance.get(currentSquare) + 1;
            freeNeighbours.stream()
                    .forEach(fn -> {
                        if (currentDistanceToNeighbour < mapSquareWithDistance.get(fn)) {
                            mapSquareWithDistance.put(fn, currentDistanceToNeighbour);
                        }
                    });
            // remove current point from unvisited set
            unvisitedSquares.remove(currentSquare);

            // check if we are done
            if (currentSquare.equals(src)) {
                reachedSource = true;
            } else {
                noRoutePossible = unvisitedSquares.stream()
                        .filter(s -> !(mapSquareWithDistance.get(s) == INFINITE))
                        .count() == 0;

                if (!noRoutePossible) {
                    // set new current square to the unvisited node with the smallest distance
                    currentSquare = (Square) unvisitedSquares.stream()
                            .min(Comparator.comparingInt(un -> mapSquareWithDistance.get(un))
                                    .thenComparing(fn -> (Square) fn)
                            )
                            .get();
                }
            }
        }

        // get the neighbour square (around the source) with the minimum distance
        Optional<Map.Entry<Square, Integer>> closestSquareWithDistance = mapSquareWithDistance.entrySet()
                .stream()
                .filter(s -> getNeighbourSquares((Square) src).stream()
                        .filter(ns -> ns.equals(s.getKey())).count() > 0)
                .min(Comparator.comparingInt(me -> (Integer) (((Map.Entry) me).getValue()))
                        .thenComparing(me -> (Square) (((Map.Entry) me).getKey()))
                );

        if (closestSquareWithDistance.isEmpty()) {
            // TODO dirty hack as the calling lambda cannot handle Optionals
            // TODO look further into it
            closestSquareWithDistance = Optional.of(new AbstractMap.SimpleEntry<>(new Square(0, 0), INFINITE));
        }
        return closestSquareWithDistance.get();
    }

    private List<Square> getFreeNeighboursIncludingDestination(Square square, Square target) {
        List<Square> freeSquares = getNeighbourSquares(square).stream()
                .filter(p -> isFreeSquare((int) p.getX(), (int) p.getY()) || p.equals(target))
                .collect(Collectors.toList());

        return freeSquares;
    }

    private List<Square> getNeighbourSquares(Square square) {
        List<Square> ls = List.of(
                new Square((int) square.getX() - 1, (int) square.getY()),
                new Square((int) square.getX() + 1, (int) square.getY()),
                new Square((int) square.getX(), (int) square.getY() - 1),
                new Square((int) square.getX(), (int) square.getY() + 1)
        ).stream()
                .filter(sq -> isValidSquare(sq.getX(), sq.getY()))
                .collect(Collectors.toList());

        return ls;
    }

    private boolean isValidSquare(int x, int y) {
        if (x < 0) return false;
        if (y < 0) return false;
        if (x >= combatArea.length) return false;
        if (y >= combatArea[0].length) return false;
        return true;
    }

    private boolean isCombattantAtSquare(int x, int y) {
        return (combatants().stream()
                .filter(e -> e.getX() == x && e.getY() == y)
                .count() > 0);
    }

    private boolean canMove(Combatant combattant) {
        return getNeighbourSquares(combattant).stream()
                .filter(ns -> isFreeSquare(ns.getX(), ns.getY()))
                .count() > 0;
    }

    public void addElement(int x, int y, char c) {
        switch (c) {
            case OPEN_AREA:
            case WALL:
                combatArea[x][y] = c;
                break;
            case GOBLIN:
                combatArea[x][y] = OPEN_AREA;
                goblins.add(new Goblin(x, y));
                break;
            case ELF:
                combatArea[x][y] = OPEN_AREA;
                elfs.add(new Elf(x, y, elfAttackPower));
                break;
            case ' ':
                break;
            default:
                throw new IllegalArgumentException("unknown element: " + c);
//                break;
        }

    }

    public boolean battle() {
        return battle(false);
    }

    public boolean battle(boolean stopWhenAnElfDies) {
        boolean elfsWonWithoutLosses = false;
        boolean battleFinished = false;
        int turns = 0;

        // nr of initial Elfs
        int initialElfs = elfs.size();

        while (!battleFinished) {
            System.out.println("starting turn# " + (turns + 1));

            List<Combatant> sortedCombatants = combatants();
            Collections.sort(sortedCombatants);
            for (Combatant combatant : sortedCombatants) {
                if (goblins.size() == 0
                        || elfs.size() == 0
                        || (stopWhenAnElfDies && elfs.size() != initialElfs)) {
                    battleFinished = true;
                } else {
                    takeTurn(combatant);
                }
            }

            if (!battleFinished) {
                turns++;
                System.out.println("completed turn# " + turns);
                System.out.println("Round: " + turns);
            }

            print();
        }

        System.out.println("\nbattle finished in " + turns + " turns...");

        if (elfs.size() > 0) {
            System.out.println("ELF's WIN!");
            System.out.println("remaining elfs: " + elfs.size());
            int elfsSum = elfs.stream()
                    .mapToInt(elf -> elf.getHitPoints())
                    .sum();
            System.out.println("elfsSum: " + elfsSum);
            System.out.println("outcome (" + turns + " rounds * sum " + elfsSum + "): " + turns * elfsSum);
        }

        if (goblins.size() > 0) {
            System.out.println("GOBLIN's WIN!");
            System.out.println("remaining goblins: " + goblins.size());
            int goblinsSum = goblins.stream()
                    .mapToInt(goblin -> goblin.getHitPoints())
                    .sum();
            System.out.println("goblinsSum: " + goblinsSum);
            System.out.println("outcome (" + turns + " rounds * sum " + goblinsSum + "): " + turns * goblinsSum);
        }

        if (elfs.size() == initialElfs) {
            elfsWonWithoutLosses = true;
        }
        return elfsWonWithoutLosses;
    }
}

public class Day_15 {

    private static String FILENAME = "input_15.txt";

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        String fileName;
        if (args.length == 0) {
            fileName = FILENAME;
        } else {
            fileName = args[0];
        }

        System.out.println("reading file: " + fileName);
        // get the input lines and construct the combat area
        List<String> combatAreaLines =
                Files.lines(Paths.get(fileName))
                        .collect(Collectors.toList());

        //determine size
        int maxX = combatAreaLines.stream()
                .max(Comparator.comparingInt(line -> line.length())).get().length();
        int maxY = combatAreaLines.size();

        CombatArea combatArea = new CombatArea(maxX, maxY);

        for (int y = 0; y < maxY; y++) {
            for (int x = 0; x < combatAreaLines.get(y).length(); x++) {
                combatArea.addElement(x, y, combatAreaLines.get(y).charAt(x));
            }
        }

        combatArea.print();
        combatArea.battle();

        LocalTime finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
        System.out.println();
        // part 2

        start = LocalTime.now();

        int elfAttackPower = 4;
        boolean elfsWonWithoutLosses = false;
        boolean optimumFound = false;

        int currentMin = 4;
        int currentMax = Combatant.HITPOINTS;

        // binary search to find optimum
        while (!optimumFound) {
            if (currentMax - currentMin != 1) {
                elfAttackPower = (currentMin + currentMax + 1) / 2;
            }
            System.out.println("trying Elf's attacking power: " + elfAttackPower + " (min: " + currentMin + " max: " + currentMax + ")");

            combatArea = new CombatArea(maxX, maxY);
            combatArea.setElfAttackPower(elfAttackPower);

            for (int y = 0; y < maxY; y++) {
                for (int x = 0; x < combatAreaLines.get(y).length(); x++) {
                    combatArea.addElement(x, y, combatAreaLines.get(y).charAt(x));
                }
            }

            combatArea.print();

            elfsWonWithoutLosses = combatArea.battle(true);
            if (currentMin == currentMax && elfsWonWithoutLosses) {
                // we are done
                optimumFound = true;
            } else if (currentMax - currentMin == 1) {
                // edge case for the almost end when there are 2 candidate elements
                if (elfsWonWithoutLosses) {
                    if (currentMin == elfAttackPower) {
                        optimumFound = true;
                    } else {
                        elfAttackPower = currentMin;
                    }
                } else {
                    currentMin = currentMax;
                }
            } else if (elfsWonWithoutLosses) {
                // binary search, take lower part
                currentMax = elfAttackPower;
            } else {
                // binary search, take higher part
                currentMin = elfAttackPower;
            }
        }

        System.out.println("needed Elf's attackPower: " + elfAttackPower);

        finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).

                toMillis());
    }
}