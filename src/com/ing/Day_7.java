package com.ing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@ToString
@AllArgsConstructor
@Data
class Worker {
    String workerName;
    Integer workAmount;
}

public class Day_7 {

    private final static String IDLE = ".";
    private static String fileName = "input_7.txt";
    private static Map<String, List<String>> preReqs;
    private static int baseWaittime = 60;
    // # workers
    private static int maxWorkers = 5;
    // worker map: <workitem, seconds left>
    private static List<Worker> workerPool = new ArrayList<>();
    // my first custom collector
    private static Collector<String, List<String>, List<String>> mySkippingNullCollector =
            Collector.of(
                    () -> new ArrayList<String>(),
                    (preReqs, preReq) -> {
                        if (preReq != null) {
                            preReqs.add(preReq);
                        }
                    },
                    (preReq1, preReq2) -> {
                        ArrayList al = new ArrayList<>();
                        al.add(preReq1);
                        al.add(preReq2);
                        return al;
                    }
            );

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        // read prereqs
        preReqs = readPreReqs(fileName);

        // result String
        StringBuilder result = new StringBuilder(preReqs.size());

        while (!preReqs.isEmpty()) {
            // find first element alphabetically without prereqs
            String firstElementsWithoutPreReq = getFirstElementWithoutPreReq(preReqs);

            // append the element to the result
            result.append(firstElementsWithoutPreReq);

            // remove the element
            preReqs = removeElement(preReqs, firstElementsWithoutPreReq);

            // remove the element as prereq
            removePrereq(preReqs, firstElementsWithoutPreReq);
        }

        System.out.println("\nresult: " + result);

        LocalTime finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());

        //part 2

        start = LocalTime.now();

        // read prereqs
        preReqs = readPreReqs(fileName);

        // initialize worker pool with idle workers
        IntStream.range(0, maxWorkers)
                .forEach(i -> workerPool.add(new Worker(IDLE, 0)));

        int elapsedSeconds = 0;

        while (!preReqs.isEmpty()) {
            // put idle workers, if any, to work
            giveWorkToWorker(workerPool);

            // another second gone
            elapsedSeconds++;

            // update work done in worker pool: decrement all 1 second
            // and check for work done
            workerPool.stream()
                    .filter(e -> e.getWorkAmount() > 0)
                    .forEach(e -> {
                                e.setWorkAmount(e.getWorkAmount() - 1);
                                if (e.getWorkAmount() == 0) {
                                    // remove the element
                                    preReqs = removeElement(preReqs, e.getWorkerName());

                                    // remove the element as prereq
                                    removePrereq(preReqs, e.getWorkerName());

                                    // set the worker to idle
                                    e.setWorkerName(IDLE);
                                }
                            }
                    );
        }

        System.out.println("\nelapsed worker seconds: " + elapsedSeconds);

        finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
    }


    private static void giveWorkToWorker(List<Worker> workerPool) {
        workerPool.stream()
                .filter(e -> e.getWorkAmount() == 0)
                .forEach(e -> {
                    String work = getFirstFreeElementWithoutPreReq(preReqs);
                    if (work != null) {
                        e.setWorkerName(work);
                        // calculate  work effort
                        int workEffort = baseWaittime + work.charAt(0) - 'A' + 1;
                        e.setWorkAmount(workEffort);
                    }
                });
    }

    private static void removePrereq(Map<String, List<String>> preReqs, String preReq) {
        preReqs.entrySet()
                .stream()
                .forEach(se -> se.getValue().removeIf(e -> e.equals(preReq)));
    }

    private static Map<String, List<String>> removeElement(Map<String, List<String>> preReqs, String preReq) {
        return preReqs.entrySet()
                .stream()
                .filter(se -> !se.getKey().equals(preReq))
                .collect(toMap(se -> se.getKey(), se -> se.getValue()));
    }

    private static String getFirstElementWithoutPreReq(Map<String, List<String>> preReqs) {
        return preReqs.entrySet()
                .stream()
                .filter(se -> se.getValue().size() == 0)
                .map(se -> se.getKey())
                .sorted()
                .findFirst()
                .get();
    }

    private static String getFirstFreeElementWithoutPreReq(Map<String, List<String>> preReqs) {
        return preReqs.entrySet()
                .stream()
                .filter(se -> se.getValue().size() == 0)
                .filter(se -> (workerPool.stream()
                        .filter(worker -> worker.getWorkerName().equals(se.getKey()))
                        .count() == 0)
                )
                .map(se -> se.getKey())
                .sorted()
                .findFirst()
                .orElse(null);
    }

    private static Map<String, List<String>> readPreReqs(final String fileName) throws IOException {
        return
                Files.lines(Paths.get(fileName))
                        .flatMap(s -> {
                            String[] words = s.split(" ");
                            String req = words[7];
                            String preReq = words[1];
                            SimpleEntry<String, String> seReq = new SimpleEntry<>(req, preReq);
                            SimpleEntry<String, String> sePreReq = new SimpleEntry<>(preReq, null);
                            return Stream.of(seReq, sePreReq);
                        })
                        .collect(groupingBy(
                                k -> k.getKey(),
                                Collectors.mapping(
                                        se -> se.getValue(), mySkippingNullCollector)
                        ));
    }
}