package com.ing;

import lombok.ToString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.ing.GuardSleepStatus.ASLEEP;
import static com.ing.GuardSleepStatus.AWAKE;

enum GuardSleepStatus {
    AWAKE, ASLEEP
}

@ToString
class GuardsLogs {
    private Map<String, Map<LocalDateTime, List<GuardSleepStatus>>> guardsLogs = new HashMap<>();

    public void guardStatusEvent(String guard, LocalDateTime dt, GuardSleepStatus status) {
        Map<LocalDateTime, List<GuardSleepStatus>> guardLogs = getGuardLogs(guard);
        List<GuardSleepStatus> guardLog = getGuardHourLog(dt, guardLogs);

        // update all statuses starting from the given time
        IntStream.range(dt.getMinute(), 60)
                .forEach(i -> guardLog.set(i, status));
    }

    public String getGuardMostMinutesAsleep() {
        Optional<Map.Entry<String, Map<LocalDateTime, List<GuardSleepStatus>>>> sleepyGuard =
                guardsLogs.entrySet()
                        .stream()
                        .max((e1, e2) ->
                                (int) e1.getValue().values().stream().flatMap(al -> al.stream().filter(s -> s == ASLEEP)).count() -
                                        (int) e2.getValue().values().stream().flatMap(al -> al.stream().filter(s -> s == ASLEEP)).count()
                        );
        return sleepyGuard.get().getKey();
    }

    public int getMostSleepyMinuteForGuard(String guard) {
        Map<LocalDateTime, List<GuardSleepStatus>> guardLogs = getGuardLogs(guard);
//        guardLog.entrySet().stream().forEach(System.out::println);

        int minute =
                (Integer)
                        guardLogs.values()
                                .stream()
                                .flatMap(al -> IntStream.range(0, 60)
                                        .mapToObj(i -> new SimpleEntry<>(i, al.get(i)))
                                ).filter(se -> se.getValue() == ASLEEP)
                                .map(se -> se.getKey())
                                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                                .entrySet()
                                .stream()
                                .max(Comparator.comparingInt(e -> e.getValue().intValue()))
                                .get().getKey();
        ;
        return minute;
    }

    public String getGuardWhichSleepsMostAtAMinute() {
        return guardsLogs.entrySet()
                .stream()
                .max(Comparator.comparingInt(e -> (Integer) e.getValue()
                                .values()
                                .stream()
                                .flatMap(al -> IntStream.range(0, 60)
                                        .mapToObj(i -> new SimpleEntry<>(i, al.get(i)))
                                ).filter(se -> se.getValue() == ASLEEP)
                                .map(se -> se.getKey())
                                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                                .entrySet()
                                .stream()
                                .max(Comparator.comparingInt(e2 -> e2.getValue().intValue()))
                                .get().getKey()
                        )
                )
                .get().getKey();
    }

    private Map<LocalDateTime, List<GuardSleepStatus>> getGuardLogs(String guard) {
        // get the logs for this guard, make new log if it does not exist yet
        Map<LocalDateTime, List<GuardSleepStatus>> guardLog = guardsLogs.get(guard);
        if (guardLog == null) {
            guardLog = new HashMap<>();
            guardsLogs.put(guard, guardLog);
        }
        return guardLog;
    }

    private List<GuardSleepStatus> getGuardHourLog(LocalDateTime ld, Map<LocalDateTime, List<GuardSleepStatus>> guardLog) {
        // get the statuses for this hour, make new log if does not exist yet (ignore the minutes)
        LocalDateTime ldTrimmed = ld.withMinute(0);
        List<GuardSleepStatus> guardHourLog = guardLog.get(ldTrimmed);
        if (guardHourLog == null) {
            // initialize with AWAKE
            guardHourLog = Stream
                    .generate(() -> AWAKE)
                    .limit(60)
                    .collect(Collectors.toList());
            guardLog.put(ldTrimmed, guardHourLog);
        }
        return guardHourLog;
    }
}

public class Day_4 {
    static String fileName = "input_4.txt";

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    static String guard = null;

    public static void main(String[] args) throws IOException {

        LocalTime start = LocalTime.now();

        GuardsLogs guardsLogs = new GuardsLogs();

        // read and sort file
        Files.lines(Paths.get(fileName)).sorted().forEach(s -> {
                    String[] fields = s.replace("[", "")
                            .replace("]", "")
                            .replace("#", "")
                            .split(" ");
                    LocalDateTime dateTime = LocalDateTime.parse(fields[0] + " " + fields[1], formatter);

                    String action = fields[2];
                    switch (action) {
                        case "wakes":
                            guardsLogs.guardStatusEvent(guard, dateTime, AWAKE);
                            break;
                        case "falls":
                            guardsLogs.guardStatusEvent(guard, dateTime, ASLEEP);
                            break;
                        case "Guard":
                            guard = fields[3];
                            break;
                        default:
                            throw new IllegalArgumentException("unknown action: " + s);
                    }
                }
        );

        String sleepyGuard = guardsLogs.getGuardMostMinutesAsleep();
        System.out.println("\nguard id max minutes asleep: " + sleepyGuard);
        int minute = guardsLogs.getMostSleepyMinuteForGuard(sleepyGuard);
        System.out.println("at minute: " + minute);
        System.out.println("guard x minute= " + Integer.valueOf(sleepyGuard) * minute);

        LocalTime finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());

        // part 2

        start = LocalTime.now();

        sleepyGuard = guardsLogs.getGuardWhichSleepsMostAtAMinute();
        System.out.println("\nguard id which sleeps most at one minute: " + sleepyGuard);
        minute = guardsLogs.getMostSleepyMinuteForGuard(sleepyGuard);
        System.out.println("at minute: " + minute);
        System.out.println("guard x minute= " + Integer.valueOf(sleepyGuard) * minute);

        finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
    }
}