package com.ing;

import lombok.Data;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@FunctionalInterface
interface OpCode {
    /**
     * Applies this opcode to the given input.
     *
     * @return the register result
     */
    int[] apply(int inputA, int inputB, int inputC);
}

class Device {
    @Getter
    public final Map<String, OpCode> operators = new TreeMap<>();
    @Getter
    public int registerAsInstructionPointer;
    @Getter
    int[] register;

    public Device(int nrRegisters) {
        register = new int[nrRegisters];
        clearRegister();

        operators.put("addr", this::addr);
        operators.put("addi", this::addi);
        operators.put("mulr", this::mulr);
        operators.put("muli", this::muli);
        operators.put("banr", this::banr);
        operators.put("bani", this::bani);
        operators.put("borr", this::borr);
        operators.put("bori", this::bori);
        operators.put("setr", this::setr);
        operators.put("seti", this::seti);
        operators.put("gtir", this::gtir);
        operators.put("gtri", this::gtri);
        operators.put("gtrr", this::gtrr);
        operators.put("eqir", this::eqir);
        operators.put("eqri", this::eqri);
        operators.put("eqrr", this::eqrr);
    }

    public void setRegister(int[] r) {
        System.arraycopy(r, 0, register, 0, r.length);
    }

    public void clearRegister() {
        Arrays.fill(register, 0);
    }

    public int[] addr(int a, int b, int c) {
        register[c] = register[a] + register[b];
        return register;
    }

    public int[] addi(int a, int b, int c) {
        register[c] = register[a] + b;
        return register;
    }

    public int[] mulr(int a, int b, int c) {
        register[c] = register[a] * register[b];
        return register;
    }

    public int[] muli(int a, int b, int c) {
        register[c] = register[a] * b;
        return register;
    }

    public int[] banr(int a, int b, int c) {
        register[c] = register[a] & register[b];
        return register;
    }

    public int[] bani(int a, int b, int c) {
        register[c] = register[a] & b;
        return register;
    }

    public int[] borr(int a, int b, int c) {
        register[c] = register[a] | register[b];
        return register;
    }

    public int[] bori(int a, int b, int c) {
        register[c] = register[a] | b;
        return register;
    }

    public int[] setr(int a, int b, int c) {
        register[c] = register[a];
        return register;
    }

    public int[] seti(int a, int b, int c) {
        register[c] = a;
        return register;
    }

    public int[] gtir(int a, int b, int c) {
        if (a > register[b]) {
            register[c] = 1;
        } else {
            register[c] = 0;
        }
        return register;
    }

    public int[] gtri(int a, int b, int c) {
        if (register[a] > b) {
            register[c] = 1;
        } else {
            register[c] = 0;
        }
        return register;
    }

    public int[] gtrr(int a, int b, int c) {
        if (register[a] > register[b]) {
            register[c] = 1;
        } else {
            register[c] = 0;
        }
        return register;
    }

    public int[] eqir(int a, int b, int c) {
        if (a == register[b]) {
            register[c] = 1;
        } else {
            register[c] = 0;
        }
        return register;
    }

    public int[] eqri(int a, int b, int c) {
        if (register[a] == b) {
            register[c] = 1;
        } else {
            register[c] = 0;
        }
        return register;
    }

    public int[] eqrr(int a, int b, int c) {
        if (register[a] == register[b]) {
            register[c] = 1;
        } else {
            register[c] = 0;
        }
        return register;
    }

    public int[] setRegisterToUseForInstructionPointer(int a, int b, int c) {
        registerAsInstructionPointer = a;
        return register;
    }

    public void executeProgram(Program inputProgram) {
        // read register to use as instruction pointer
        Statement statement = inputProgram.getStatements().get(0);
        setRegisterToUseForInstructionPointer(statement.getArguments()[0], 0, 0);

        // clone statements to use
        List<Statement> listStatements = (List<Statement>) ((ArrayList) inputProgram.getStatements()).clone();
        listStatements.remove(0);

        long counter = 0;
        while (true) {
            // verify instruction pointer pointing to valid program instruction
            if (validInstructionPointer(register[registerAsInstructionPointer], listStatements)) {
                statement = listStatements.get(register[registerAsInstructionPointer]);
                OpCode operation = operators.get(statement.getOperation());
                operation.apply(statement.getArguments()[0], statement.getArguments()[1], statement.getArguments()[2]);
            } else {
                throw new IllegalStateException(String.format("instruction pointer %d outside program (line 0..%d): ", register[registerAsInstructionPointer], inputProgram.getStatements().size()));
            }
            register[registerAsInstructionPointer]++;
            counter++;
            if (counter % 10000000 == 0) {
                System.out.println("counter = " + counter);
            } else if (counter % 100000 == 0) {
                System.out.print(".");
            }
        }
    }

    private boolean validInstructionPointer(int instructionPointer, List<Statement> listStatements) {
        if (instructionPointer < 0) {
            return false;
        }
        return instructionPointer < listStatements.size();
    }
}

@Data
class Sample {
    private int[] before;
    private int[] instruction;
    private int[] after;
}

@Data
class Statement {
    private String operation;
    private int[] arguments;
}

@Data
class Program {
    List<Statement> statements = new ArrayList<>();
}

public class Day_16 {

    private static String FILENAME = "input_16.txt";

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        // read samples from input lines: remove empty lines
        List<String> inputLines = new ArrayList<>(readFile(args));

        // read samples
        List<Sample> lSamples = new ArrayList<>();
        Sample sample = new Sample();
        for (String line : inputLines.stream()
                .filter(l -> l.trim().length() > 0)
                .collect(toList())) {
            if (line.startsWith("Before:")) {
                sample = new Sample();
                sample.setBefore(
                        Arrays.stream(line.replaceAll("Before: *\\[", "")
                                .replace("]", "")
                                .replace(" ", "")
                                .split(","))
                                .mapToInt(Integer::valueOf)
                                .toArray());
            } else if (line.startsWith("After:")) {
                sample.setAfter(
                        Arrays.stream(line.replaceAll("After: *\\[", "")
                                .replace("]", "")
                                .replace(" ", "")
                                .split(","))
                                .mapToInt(Integer::valueOf)
                                .toArray());
                lSamples.add(sample);
            } else {
                sample.setInstruction(
                        Arrays.stream(line.split(" "))
                                .mapToInt(Integer::valueOf)
                                .toArray());
            }
        }

        Device device = new Device(4);

        long countSamplesWith3OrMoreOpCodes = lSamples.stream()
                .filter(s -> device.getOperators().values().stream()
                        .filter(oper -> {
                            device.setRegister(s.getBefore());
                            return Arrays.equals(
                                    oper.apply(s.getInstruction()[1],
                                            s.getInstruction()[2], s.getInstruction()[3]), s.getAfter());
                        })
                        .count() >= 3
                )
                .count();
        System.out.println("\n#samples with 3 or more matching OpCodes: " + countSamplesWith3OrMoreOpCodes);


        LocalTime finish = LocalTime.now();
        System.out.println("\nduration (ms): " + Duration.between(start, finish).toMillis());

        // part 2

        start = LocalTime.now();

        System.out.println();

        Map<Integer, List<OpCode>> mapOpCodeNrToOperations =
                lSamples.stream()
                        .flatMap(s -> device.getOperators().values().stream()
                                .filter(oper -> {
                                    device.setRegister(s.getBefore());
                                    return Arrays.equals(oper.apply(s.getInstruction()[1], s.getInstruction()[2], s.getInstruction()[3]), s.getAfter());
                                })
                                .map(oper -> new AbstractMap.SimpleEntry<>(s.getInstruction()[0], oper))
                        )
                        .distinct()
                        .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        mapOpCodeNrToOperations.entrySet().stream().forEach(me -> System.out.println(String.format("opcodenr %d: #operations possible: %d", me.getKey(), me.getValue().size())));

        Optional<Map<Integer, OpCode>> mapOpCodeNrToOperation = findOpCodeMapping(mapOpCodeNrToOperations);

        mapOpCodeNrToOperation.get().entrySet().stream()
                .forEach(resultLine -> System.out.println("resultLine = " + resultLine));

        // read test statements
        List<int[]> program = new ArrayList<>();
        int nrBlankLines = 0;
        final int NEEDED_NR_OF_CONSECUTIVE_BLANK_LINES = 3;
        boolean readingProgram = false;
        for (String line : inputLines) {
            if (readingProgram) {
                program.add(Arrays.stream(line.split(" "))
                        .mapToInt(Integer::valueOf)
                        .toArray());
            } else if (line.length() == 0) {
                nrBlankLines++;
                if (nrBlankLines >= NEEDED_NR_OF_CONSECUTIVE_BLANK_LINES) {
                    readingProgram = true;
                }
            } else {
                nrBlankLines = 0;
            }
        }
        System.out.println(String.format("read test statements: #%d lines", program.size()));

        // run test statements
        int[] register;
        OpCode opcode;
        device.clearRegister();
        for (int i = 0; i < program.size(); i++) {
            int[] line = program.get(i);
            opcode = mapOpCodeNrToOperation.get().get(line[0]);
            opcode.apply(line[1], line[2], line[3]);
            register = device.getRegister();
            System.out.println(String.format("#%03d register: [%d, %d, %d, %d]", i, register[0], register[1], register[2], register[3]));
        }
        register = device.getRegister();
        System.out.println("\nregister[0] = " + register[0]);

        finish = LocalTime.now();
        System.out.println("\nduration (ms): " + Duration.between(start, finish).toMillis());
    }

    private static Optional<Map<Integer, OpCode>> findOpCodeMapping(final Map<Integer, List<OpCode>> mapOpCodeNrToOperations) {
        // shortcut when map is empty
        if (mapOpCodeNrToOperations.isEmpty()) {
            throw new IllegalStateException("invoked with empty map...");
        }
        // shortcut when not resolvable i.e. contains an empty list for any opcode
        if (mapOpCodeNrToOperations.values().stream().anyMatch(l -> l.size() == 0)) {
            return Optional.empty();
        }
        // shortcut when we have 1 opcode
        if (mapOpCodeNrToOperations.size() == 1) {
            Map.Entry<Integer, List<OpCode>> onlyEntry = mapOpCodeNrToOperations.entrySet().iterator().next();
            if (onlyEntry.getValue().size() == 1) {
                // return this success
                return Optional.of(new TreeMap<>() {
                    {
                        put(onlyEntry.getKey(), onlyEntry.getValue().get(0));
                    }
                });
            } else {
                // not solvable:  multiple options
                System.out.println(String.format("not solvable: multiple options for only entry %d: %s", onlyEntry.getKey(), onlyEntry.getValue()));
                return Optional.empty();
            }
        }

        // result Map
        Optional<Map<Integer, OpCode>> result = Optional.of(new TreeMap<>());

        // get an operation code with the lowest number of possibilities
        Map.Entry<Integer, List<OpCode>> opcodeOptions = mapOpCodeNrToOperations.entrySet().stream()
                .min(Comparator.comparingInt(m -> m.getValue().size()))
                .get();

        // try all options for this operation code
        Integer opcodeNumber = opcodeOptions.getKey();
        List<OpCode> opcodes = opcodeOptions.getValue();
        boolean solved = false;
        for (int i = 0; i < opcodes.size() && !solved; i++) {
            OpCode opcode = opcodes.get(i);
            System.out.println(String.format("trying opcodeNumber->opcode: %d->%s (%d options)", opcodeNumber, opcode.toString(), opcodes.size()));
            // filter the current opcode from the map, both as key and in the values (of other keys)
            Map<Integer, List<OpCode>> mapRemainder = removeOpCodeFromMap(mapOpCodeNrToOperations, opcodeNumber, opcode);
            // try to find solution for the remainder
            Optional<Map<Integer, OpCode>> mapRemainderResult = findOpCodeMapping(mapRemainder);
            if (mapRemainderResult.isPresent()) {
                // add the assumption to the result
                result = mapRemainderResult;
                result.get().put(opcodeNumber, opcode);
                solved = true;
            } else {
                System.out.println(String.format("not solvable: opcodeNumber->opcode: %d->%s (%d possible options)", opcodeNumber, opcode.toString(), opcodes.size()));
            }
        }
        if (!solved) {
            System.out.println(String.format("not solvable for opcodeNumber %d", opcodeNumber));
            return Optional.empty();
        }

        return result;
    }

    private static Map<Integer, List<OpCode>> removeOpCodeFromMap(Map<Integer, List<OpCode>> mapOpCodeNrToOperations, Integer opcodeNumber, OpCode opcode) {
        Map<Integer, List<OpCode>> result;
        result = mapOpCodeNrToOperations.entrySet().stream()
                .filter(me -> !me.getKey().equals(opcodeNumber))
                .collect(Collectors.toMap(k -> k.getKey(),
                        v -> v.getValue().stream()
                                .filter(val -> !val.equals(opcode))
                                .collect(Collectors.toList())
                ));
        return result;
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