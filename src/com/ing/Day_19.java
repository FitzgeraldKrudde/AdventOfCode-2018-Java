package com.ing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Day_19 {

    private static String FILENAME = "input_19.txt";

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        // create a device with 6 registers and the instruction pointer operation
        Device device = new Device(6);

        // read samples from input lines: remove empty lines
        List<String> inputLines = new ArrayList<>(readFile(args));

        // read program
        Program program = new Program();
        for (String line : inputLines.stream()
                .filter(l -> l.trim().length() > 0)
                .collect(toList())) {
            String[] words = line.split(" ");
            if ("#ip".equals(words[0])) {
                device.setRegisterAsInstructionPointer(Integer.valueOf(words[1]));
            } else {
                Statement statement = new Statement();
                statement.setOperation(words[0]);
                statement.setArguments(new int[]{Integer.valueOf(words[1]), Integer.valueOf(words[2]), Integer.valueOf(words[3])});
                program.getStatements().add(statement);
            }
        }

        try {
            device.executeProgram(program);
        } catch (IllegalStateException e) {
            System.out.println("caught IllegalStateException: " + e.getMessage());
        }
        System.out.println("register[0] = " + device.getRegister()[0]);

        LocalTime finish = LocalTime.now();
        System.out.println("\nduration (ms): " + Duration.between(start, finish).toMillis());

        // part 2

        start = LocalTime.now();

        System.out.println();
        device.clearRegister();
        int[] r = new int[6];
        Arrays.fill(r, 0);
        r[0] = 1;
        device.setRegister(r);
        device.setMaxInstructionsToExecute(25);
        device.setPrintDebugging(true);
        try {
            device.executeProgram(program);
        } catch (IllegalStateException e) {
            System.out.println("caught IllegalStateException: " + e.getMessage());
        }
        System.out.println("register[0] = " + device.getRegister()[0]);


        finish = LocalTime.now();
        System.out.println("\nduration (ms): " + Duration.between(start, finish).toMillis());
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