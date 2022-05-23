package org.headroyce.lross2024;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * main class which handles file reading and printing to console
 */
public class Main {

    /**
     * exectuable main method
     *
     * @param args command line arguments (input file)
     */
    public static void main(String[] args) {

        //2d array (Adj matrix)
        //text.get(row).get(col)
        ArrayList<ArrayList<Integer>> text = new ArrayList<>();

        if (args.length != 1) throw new Error("invalid usage. correct command usage is \"<input text file>\"");
        String input_file_type = args[0].substring(args[0].length() - 4);

        if (input_file_type.equals(".txt")) {
            File file = new File(args[0]);
            try {
                Scanner sc = new Scanner(file);
                int i = 0;
                while (sc.hasNextLine()) {
                    text.add(new ArrayList<>());
                    String line = sc.nextLine();

                    for (String s : line.split("")) {
                        if (Character.isDigit(s.charAt(0))) {
                            text.get(i).add(Integer.parseInt(s));
                        }
                    }

                    i++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (text.size() > 26) {
            throw new Error("too many nodes to be processed. maximum is 26");
        }

        Graph<Boolean, Integer> graph = new Graph<>();

        //char 97 = "a", char 122 = "z"
        //any more than 26 nodes = too much
        final int starting_ascii_value = 97;

        //fill the graph with alphabetical nodes
        for (int i = starting_ascii_value; i < text.size() + starting_ascii_value; i++) {
            graph.addNode(true, Character.toString((char) i));
        }

        //now we traverse the 2d arraylist and connect the nodes
        for (int row = 0; row < text.size(); row++) {
            for (int col = 0; col < text.get(row).size(); col++) {
                int weight = text.get(row).get(col);
                String from_label = Character.toString((char) (row + starting_ascii_value));
                String to_label = Character.toString((char) (col + starting_ascii_value));

                if (weight > 0) graph.addDirectedEdge(weight, from_label, to_label, false);
            }
        }

        HashMap<String, Number> shortest_path = graph.shortest_path();
        Graph<Boolean, Integer> spanning_tree = graph.smallest_spanning_tree();

        String test = spanning_tree.toString();
        System.out.println("Minimum Spanning Tree\n" +
                "---------------------");
        System.out.print(test);
        System.out.println("Cost: " + spanning_tree.getCost() + "\n");

        System.out.println("Shortest Path\n" +
                "-------------");
        StringBuilder path = new StringBuilder("");
        double cost = 0;
        for (String s : shortest_path.keySet()) {
            path.append(s).append("->");
            cost += shortest_path.get(s).doubleValue();
        }
        path.delete(path.length() - 2, path.length());
        System.out.println(path);
        System.out.println("Cost: " + cost);
    }
}
