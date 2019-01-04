package com.ing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ing.Day_24.AttackTypes;
import static java.util.stream.Collectors.toList;

@NoArgsConstructor
@AllArgsConstructor
@Data
class Group {
    @ToString.Exclude
    private Army army;
    private String name;
    private int units;
    private int attackDamage;
    private int hitpoints;
    private int initiative;
    private AttackTypes attackType;
    private List<AttackTypes> weaknesses = new ArrayList<>();
    private List<AttackTypes> immunities = new ArrayList<>();

    public int getEffectivePower() {
        return units * attackDamage;
    }
}

@Data
class Army {
    private String name;
    private List<Group> groups = new ArrayList<>();
    private List<AttackPlan> attackPlans = new ArrayList<>();
}

@AllArgsConstructor
@Data
class AttackPlan {
    private Group myAttackingerGroup;
    private Group enemyDefenderGroup;
}

public class Day_24 {
    private static String FILENAME = "input_24.txt";

    enum AttackTypes {RADIATION, BLUDGEONING, SLASHING, FIRE, COLD}

    ;

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        List<String> inputLines = readFile(args);

        Army armyImmuneSystem = new Army();
        armyImmuneSystem.setName("Immune System");
        Army armyInfection = new Army();
        armyInfection.setName("Infection");

        constructArmies(inputLines, armyImmuneSystem, armyInfection);

        System.out.println("armyImmuneSystem:");
        armyImmuneSystem.getGroups().stream().forEach(g -> System.out.println("group = " + g));
        System.out.println();
        System.out.println("armyInfection:");
        armyInfection.getGroups().stream().forEach(g -> System.out.println("group = " + g));
        System.out.println();

        battle(armyInfection, armyImmuneSystem);

        Army army = null;
        // determine winner
        if (armyImmuneSystem.getGroups().size() > 0) {
            System.out.println("\nImmune system won!");
            army = armyImmuneSystem;
        } else if (armyInfection.getGroups().size() > 0) {
            System.out.println("\nInfection won!");
            army = armyInfection;
        } else {
            System.out.println("a tie??");
        }

        // total #units winner
        int unitsLeft = army.getGroups().stream()
                .collect(Collectors.summingInt(g -> g.getUnits()));
        System.out.println(String.format("winning army has %d units left", unitsLeft));

        LocalTime finish = LocalTime.now();
        System.out.println("\nduration (ms): " + Duration.between(start, finish).toMillis());
        System.out.println();

        // part 2

        start = LocalTime.now();

        boolean immunitySystemWon = false;
        int boost = 0;

        // binary search to find optimum
        while (!immunitySystemWon) {
            boost++;

            armyImmuneSystem = new Army();
            armyImmuneSystem.setName("Immune System");
            armyInfection = new Army();
            armyInfection.setName("Infection");

            constructArmies(inputLines, armyImmuneSystem, armyInfection);

            // add boost
            final int currentBoost = boost;
            armyImmuneSystem.getGroups().stream()
                    .forEach(group -> {
                        group.setAttackDamage(group.getAttackDamage() + currentBoost);
                    });

            // battle
            immunitySystemWon = battle(armyInfection, armyImmuneSystem);
            unitsLeft = armyImmuneSystem.getGroups().stream()
                    .collect(Collectors.summingInt(g -> g.getUnits()));

            System.out.println(String.format("immunitySystemWon: %s for boost: %d with #units left: %d", immunitySystemWon, currentBoost, unitsLeft));
        }

        // total #units winner
        System.out.println("unitsLeft = " + unitsLeft);

        finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
    }

    private static boolean battle(Army armyInfection, Army armyImmuneSystem) {
        int round = 0;
        long totalUnits = 0;
        // fight till the end
        while (armyImmuneSystem.getGroups().size() > 0 && armyInfection.getGroups().size() > 0) {
//            System.out.println("\nround = " + round);
            selectTargets(armyImmuneSystem, armyInfection);
            selectTargets(armyInfection, armyImmuneSystem);

            fight(armyImmuneSystem, armyInfection);
            round++;

            // count total units of both armies
            int currentTotalUnits = armyImmuneSystem.getGroups().stream().collect(Collectors.summingInt(g -> g.getUnits())) +
                    armyInfection.getGroups().stream().collect(Collectors.summingInt(g -> g.getUnits()));
            // detect stalemate i.e. no units killed in this round
            if (currentTotalUnits == totalUnits) {
                System.out.println("stalemate, quitting this battle..");
                return false;
            }
            totalUnits = currentTotalUnits;
        }
        if (armyImmuneSystem.getGroups().size() > 0) {
            return true;
        }

        return false;
    }

    private static void constructArmies(List<String> inputLines, Army armyImmuneSystem, Army armyInfection) {
        Army army = null;
        for (String line : inputLines) {
            if (line.startsWith("Immune System")) {
                army = armyImmuneSystem;
            } else if (line.startsWith("Infection")) {
                army = armyInfection;
            } else {
                Group group = parseGroup(line);
                group.setArmy(army);
                army.getGroups().add(group);
                group.setName(army.getName() + "-" + (army.getGroups().indexOf(group) + 1));
            }
        }
    }

    private static void fight(Army armyImmuneSystem, Army armyInfection) {
        // get all attack plans
        List<AttackPlan> allAttackPlans = new ArrayList<>();
        allAttackPlans.addAll(armyImmuneSystem.getAttackPlans());
        allAttackPlans.addAll(armyInfection.getAttackPlans());

        // sort all groups on initiative and perform attack
        allAttackPlans.stream()
                .sorted(Comparator.<AttackPlan>comparingInt(attackPlan -> attackPlan.getMyAttackingerGroup().getInitiative()).reversed())
                .forEach(attackPlan -> performAttack(attackPlan.getMyAttackingerGroup(), attackPlan.getEnemyDefenderGroup()));

        // clear killed groups
        armyImmuneSystem.getGroups().removeIf(group -> group.getUnits() <= 0);
        armyInfection.getGroups().removeIf(group -> group.getUnits() <= 0);

//        armyImmuneSystem.getGroups().stream()
//                .forEach(group -> System.out.println(String.format("%s: contains %d units", group.getName(), group.getUnits())));
//        armyInfection.getGroups().stream()
//                .forEach(group -> System.out.println(String.format("%s: contains %d units", group.getName(), group.getUnits())));

        // clear attack plans
        armyImmuneSystem.getAttackPlans().clear();
        armyInfection.getAttackPlans().clear();
    }

    private static void performAttack(Group attackerGroup, Group defenderGroup) {
        // check if attacking group has units left i.e. is not "dead"
        if (attackerGroup.getUnits() <= 0) {
            return;
        }

        // double check immunities
        if (defenderGroup.getImmunities().contains(attackerGroup.getAttackType())) {
            throw new IllegalStateException(String.format("attacking immune %s by group: %s to group: %s", attackerGroup.getAttackType(), attackerGroup, defenderGroup));
        }

        int damagePoints = attackerGroup.getEffectivePower();
        // check defender weakness for this attack type
        if (defenderGroup.getWeaknesses().contains(attackerGroup.getAttackType())) {
            damagePoints *= 2;
        }

        // attack and kill some units
        int unitsToKill = damagePoints / defenderGroup.getHitpoints();
        if (unitsToKill > defenderGroup.getUnits()) {
            unitsToKill = defenderGroup.getUnits();
        }
        defenderGroup.setUnits(defenderGroup.getUnits() - unitsToKill);
//        System.out.println(String.format("%s attacks %s, killing %d units", attackerGroup.getName(), defenderGroup.getName(), unitsToKill));
    }

    private static void selectTargets(Army armyAttacker, Army armyDefender) {
        armyAttacker.getAttackPlans().clear();

        armyAttacker.getGroups().stream()
                .sorted(Comparator.<Group>comparingInt(group -> group.getEffectivePower())
                        .thenComparingInt(group -> group.getInitiative())
                        .reversed())
                .forEach(attackGroup -> {
                    Optional<Group> defendingGroup = determineTarget(attackGroup, armyDefender);
                    if (defendingGroup.isPresent()) {
                        AttackPlan attackPlan = new AttackPlan(attackGroup, defendingGroup.get());
                        armyAttacker.getAttackPlans().add(attackPlan);
                    }
                });
    }

    private static Optional<Group> determineTarget(Group attackingGroup, Army armyDefender) {
        Optional<Group> targetedGroup =
                armyDefender.getGroups().stream()
                        // filter out target groups already in the AttackList of the Attacking army
                        .filter(defenderGroup -> attackingGroup.getArmy().getAttackPlans().stream()
                                .noneMatch(ap -> ap.getEnemyDefenderGroup().equals(defenderGroup)))
                        // filter targets which are immune for this attacker attacking type
                        .filter(group -> !group.getImmunities().contains(attackingGroup.getAttackType()))
                        .max(Comparator.<Group>comparingInt(defenderGroup -> {
                                    int attackDamage = attackingGroup.getEffectivePower();
                                    if (defenderGroup.getWeaknesses().contains(attackingGroup.getAttackType())) {
                                        attackDamage *= 2;
                                    }
                                    return attackDamage;
                                })
                                        .thenComparingInt(group -> group.getEffectivePower())
                                        .thenComparingInt(group -> group.getInitiative())
                        );
        return targetedGroup;
    }

    private static Group parseGroup(String line) {
        Group group = new Group();
        String[] words = line.split("\\s+");
        group.setUnits(Integer.valueOf(words[0]));
        group.setHitpoints(Integer.valueOf(words[4]));
        group.setInitiative(Integer.valueOf(words[words.length - 1]));
        group.setAttackDamage(Integer.valueOf(words[words.length - 6]));
        group.setAttackType(AttackTypes.valueOf(words[words.length - 5].toUpperCase()));
        int posOpen = line.indexOf("(");
        int posClose = line.indexOf(")");
        if (posOpen != -1 && posClose != -1) {
            String attackSpecifics = line
                    .substring(posOpen + 1, posClose)
                    .replaceAll("to", "")
                    .replaceAll(",", "")
                    .replaceAll(";", "");
            List<AttackTypes> weaknesses = group.getWeaknesses();
            List<AttackTypes> immunes = group.getImmunities();
            List<AttackTypes> specifics = null;
            for (String w : attackSpecifics.split("\\s+")) {
                if (w.equals("weak")) {
                    specifics = weaknesses;
                } else if (w.equals("immune")) {
                    specifics = immunes;
                } else {
                    specifics.add(AttackTypes.valueOf(w.toUpperCase()));
                }
            }
        }

        return group;
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
        List<String> input = Files.lines(Paths.get(fileName))
                .filter(line -> line.length() > 0)
                .collect(toList());
        System.out.println("read file: " + fileName);
        return input;
    }
}