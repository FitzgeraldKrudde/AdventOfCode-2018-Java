package com.ing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Day_21 {

    private static String FILENAME = "input_21.txt";

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        // create a device with 6 registers and the instruction pointer operation
        Device device = new Device(6);

        // read samples from input lines: remove empty lines
        List<String> inputLines = new ArrayList<>(readFile(args));

        // read program
        Program program = readProgramAndConfigureDevice(device, inputLines);

        // find the crucial eqrr statement and there the other register (which is checked for quality with register 0)
        Statement statement = program.getStatements().stream()
                .filter(stmt -> "eqrr".equals(stmt.getOperation()))
                .collect(toList())
                .get(0);
        if (statement.getArguments()[0] != 0) {
            device.setRegisterWhichGetsComparedWithRegister0(statement.getArguments()[0]);
        } else if (statement.getArguments()[1] != 0) {
            device.setRegisterWhichGetsComparedWithRegister0(statement.getArguments()[1]);
        } else {
            throw new IllegalStateException("no register found");
        }
        device.setDay21InstructionNr(program.getStatements().indexOf(statement));

        // part 1
        device.setCheckDay21Condition(true);
        device.executeProgram(program);
        System.out.println("lowest value for register[0] = " + device.getDay21Part1Output());

        LocalTime finish = LocalTime.now();
        System.out.println("\nduration (ms): " + Duration.between(start, finish).toMillis());

        // part 2
        start = LocalTime.now();

        device.clearRegister();
        device.setCheckDay21Condition(true);
        device.setDay21PartTwo(true);
        device.executeProgram(program);
        System.out.println("\nlowest value for register[0] = " + device.getDay21Part2Output());

        finish = LocalTime.now();
        System.out.println("\nduration (ms): " + Duration.between(start, finish).toMillis());
    }

    private static Program readProgramAndConfigureDevice(Device device, List<String> inputLines) {
        Program program = new Program();
        for (String line : inputLines.stream()
                .filter(l -> l.trim().length() > 0)
                .collect(toList())) {
            String[] words = line.split(" ");
            if ("#ip".equals(words[0])) {
                device.setRegisterAsInstructionPointer(Integer.parseInt(words[1]));
            } else {
                Statement statement = new Statement();
                statement.setOperation(words[0]);
                statement.setArguments(new int[]{Integer.parseInt(words[1]), Integer.parseInt(words[2]), Integer.parseInt(words[3])});
                program.getStatements().add(statement);
            }
        }
        return program;
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
        System.out.println(String.format("read file: %s (#lines: %d)", fileName, input.size()));
        return input;
    }

}