package com.ing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ToString
@AllArgsConstructor
@Data
@Builder
class Node {
    final List<Node> childs = new ArrayList<>();
    @ToString.Exclude
    Node parent;
    List<Integer> metadata;
}

public class Day_8 {

    private static String fileName = "input_8.txt";

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        // get 1 line of input
        String input = Files.lines(Paths.get(fileName))
                .findFirst()
                .get();

        // convert to Integer list
        List<Integer> li = Arrays.stream(input.split(" "))
                .map(s -> Integer.valueOf(s))
                .collect(Collectors.toList());

        // make a root node
        Node root = new Node.NodeBuilder().parent(null).build();

        // add the children and calculate the metadata
        addChildrenAndCalculateMetadata(root, li);

        // flatten all nodes into an List
        List<Node> nodes = flattenTree(root);

        // calculate the metadata sum
        int sum = nodes.stream()
                .mapToInt(n -> n.getMetadata().stream()
                        .mapToInt(i -> i).sum())
                .sum();
        System.out.println("\nsum: " + sum);

        LocalTime finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());

        //part 2

        start = LocalTime.now();

        sum = calculateSumWithMetadataAsIndexToChild(root);
        System.out.println("\nsum: " + sum);

        finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
    }

    private static int calculateSumWithMetadataAsIndexToChild(Node node) {
        if (node.getChilds().size() == 0) {
            // no children -> sum the metadata
            return node.getMetadata().stream()
                    .mapToInt(i -> i).sum();
        } else {
            // with children the metadata is the index to a child (if exists for the index)
            // and then the sum of the child the same way
            return node.getMetadata().stream()
                    .mapToInt(metadataIndex -> {
                        if (metadataIndex > 0 && metadataIndex <= node.getChilds().size()) {
                            return calculateSumWithMetadataAsIndexToChild(node.getChilds().get(metadataIndex - 1));
                        } else {
                            return 0;
                        }
                    })
                    .sum();
        }
    }

    private static List<Node> flattenTree(Node node) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(node);
        node.childs.stream()
                .forEach(n -> nodes.addAll(flattenTree(n)));

        return nodes;
    }

    private static void addChildrenAndCalculateMetadata(Node parent, List<Integer> li) {
        // process the header
        int nrChildren = li.remove(0);
        int nrMetadata = li.remove(0);

        // process the children, if any
        if (nrChildren > 0) {
            for (int i = 0; i < nrChildren; i++) {
                Node child = new Node.NodeBuilder().parent(parent).build();
                parent.getChilds().add(child);
                addChildrenAndCalculateMetadata(child, li);
            }
        }

        // remainder is the metadata
        List<Integer> md = new ArrayList<>();
        for (int i = 0; i < nrMetadata; i++) {
            md.add(li.remove(0));
        }

        // set the metadata in the parent
        parent.setMetadata(md);
    }
}