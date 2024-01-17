package de.verdox.mccreativelab.debug;

import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        List<TestRecord> list = new LinkedList<>();

        list.stream().sorted(Comparator.comparingInt(TestRecord::x));
        Map<String, List<TestRecord>> a = list.stream().collect(Collectors.groupingBy(TestRecord::name));

        list.stream().toArray(value -> new TestRecord[value]);
        list.stream().toArray(TestRecord[]::new);
    }

    public record TestRecord(int x, String name){}
}
