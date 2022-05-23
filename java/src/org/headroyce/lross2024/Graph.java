package org.headroyce.lross2024;

import java.util.*;

/**
 * graph object which stores data with nodes and edges
 *
 * @param <V> data type for node
 * @param <E> data type for edge
 */
public class Graph<V, E extends Comparable<E>> {
    //map stores each node and a pointer to a list to all nodes
    private LinkedHashMap<String, GNode<V>> nodes;

    /**
     * constructs a new graph with an empty map of nodes
     */
    public Graph() {
        nodes = new LinkedHashMap<>();
    }

    /**
     * adds a new node with data if it doesnt already exist in the graph
     *
     * @param data  data for the node
     * @param label string label of node (unique attribute)
     * @return true if node is added, false if it already exists
     */
    public boolean addNode(V data, String label) {
        if (nodes.get(label) == null) {
            GNode<V> node = new GNode<>(data, label);
            nodes.put(label, node);
            return true;
        }
        return false;
    }

    /**
     * removes a node and all edges pointing to it
     *
     * @param label data of node to remove
     * @return true if node removed successfully, false if it doesn't exist
     */
    public boolean removeNode(String label) {
        GNode<V> node = nodes.get(label);
        if (node == null) return false;

        nodes.remove(label, node);
        for (GNode<V> node2 : nodes.values()) {
            for (GEdge<E> edge : node2.getEdges()) {
                if (edge.getTo().equals(node)) {
                    node2.removeEdge(edge);
                }
            }
        }
        return true;
    }

    /**
     * removes a directed edge if it exists
     *
     * @param data data in edge
     * @param from node the edge points from
     * @param to   node the edge points to
     * @return true if removed successfully, false otherwise
     */
    public boolean removeDirectedEdge(E data, String from, String to) {
        for (GNode<V> node : nodes.values()) {
            if (from.equals(node.getLabel())) {
                for (GEdge<E> edge : node.getEdges()) {
                    if (edge.getData().equals(data) && edge.getTo().getLabel().equals(to)) {
                        //check if its undirected
                        if (edge.isUndirected()){
                            for (GNode<V> node2 : nodes.values()){
                                for (GEdge<E> edge2 : node2.getEdges()){
                                    if (edge2.getTo().equals(edge.getFrom()) && edge2.getFrom().equals(edge.getTo()) && edge2.getData() == edge.getData()) edge2.setDirection(false);
                                }
                            }
                        }
                        node.removeEdge(edge);
                        return true;
                    }
                }
            }

        }
        return false;
    }

    /**
     * removes undirected edge (2 directed edges with the same data) if it exists
     *
     * @param data data of edge(s)
     * @param from first node connected by edge
     * @param to   second node connected by edge
     * @return true if both are removed successfully, false otherwise
     */
    public boolean removeUndirectedEdge(E data, String from, String to) {
        return removeDirectedEdge(data, to, from) && removeDirectedEdge(data, from, to);
    }

    /**
     * adds an edge between 2 existing nodes
     *
     * @param data       data for edge
     * @param from_label data of the node to connect from
     * @param to_label   data of the node to connect to
     * @return true if edge added successfully, false otherwise
     */
    public boolean addDirectedEdge(E data, String from_label, String to_label, boolean undirected) {
        GNode<V> to = nodes.get(to_label);
        GNode<V> from = nodes.get(from_label);
        if (to == null || from == null) {
            return false;
        }
        //check if this makes an existing edge undirected
        for (GNode<V> node : nodes.values()){
            for (GEdge<E> edge : node.getEdges()){
                if (edge.getTo().equals(from) && edge.getFrom().equals(to) && edge.getData() == data){
                    edge.setDirection(true);
                    undirected = true;
                }
            }
        }
        GEdge<E> edge = new GEdge<>(data, from, to, undirected);
        from.addEdge(edge);
        return true;
    }

    /**
     * adds a 2-way undirected edge to the graph between 2 nodes
     *
     * @param data       data for edge
     * @param from_label label of first node
     * @param to_label   label of second node
     * @return true if both edges were added successfully, false otherwise
     */
    public boolean addUndirectedEdge(E data, String from_label, String to_label) {
        boolean direct1 = this.addDirectedEdge(data, from_label, to_label, true);
        boolean direct2 = this.addDirectedEdge(data, to_label, from_label, true);
        return (direct1 && direct2);
    }

    /**
     * prims algorithm (treat all edges as directed and weighted)
     *
     * @return graph of the smallest existing weighted and directed spanning tree, null if it doesnt exist
     */
    public Graph<V, E> smallest_spanning_tree() {
        Graph<V, E> rtn = new Graph<>();
        HashMap<String, GNode<V>> processed_nodes = new HashMap<>();

        //check if a spanning tree can exist
        if (this.nodes.size() <= 0) {
            return null;
        }
        //get a random node to start at
        int i = (int) Math.floor(Math.random() * nodes.size());
        for (GNode<V> node : nodes.values()) {
            if (i == 0) {
                processed_nodes.put(node.getLabel(), node);
                rtn.addNode(node.getData(), node.getLabel());
            }
            i -= 1;
        }

        boolean done = false;
        while (!done) {
            GEdge<E> smallest_edge = null;

            for (GNode<V> node : processed_nodes.values()) {
                for (GEdge<E> edge : node.getEdges()) {
                    //loop through each edge of each processed node, see if the weights are numbers (prims only works with number edges, so others are ignored)
                    if (edge.getData() instanceof Number) {
                        if (smallest_edge == null) {
                            if (processed_nodes.get(edge.getTo().getLabel()) == null) {
                                //points to a new node
                                smallest_edge = edge;
                            }
                        } else {
                            Number num1 = (Number) edge.getData();
                            Number num2 = (Number) smallest_edge.getData();
                            if ((int) num1 < (int) num2) {
                                if (processed_nodes.get(edge.getTo().getLabel()) == null) {
                                    //points to a new node
                                    smallest_edge = edge;
                                }

                            }
                        }

                    }
                }
            }
            //check to see if a smallest_edge exists (if not, there is no spanning tree or youre done)
            if (smallest_edge == null) {
                if (processed_nodes.size() == nodes.size()) {
                    //youre done!
                    break;
                } else {
                    //theres a sink or island (no spanning tree exists)
                    return null;
                }
            } else {
                //add the new node and edge to the final graph
                rtn.addNode(smallest_edge.getTo().getData(), smallest_edge.getTo().getLabel());

                //check if its undirected (if a directed edge from the "to" node to the "from" node exists with the same weight value
                GEdge<E> opposite_edge = null;
                for (GEdge<E> edge : smallest_edge.getTo().getEdges()) {
                    if (edge.getTo().getLabel().equals(smallest_edge.getFrom().getLabel())) {
                        opposite_edge = edge;
                    }
                }

                boolean isDirected = true;
                if (opposite_edge != null) {
                    if (opposite_edge.getData() == smallest_edge.getData()) isDirected = false;
                }

                if (isDirected) {
                    rtn.addDirectedEdge(smallest_edge.getData(), smallest_edge.getFrom().getLabel(), smallest_edge.getTo().getLabel(), false);
                } else
                    rtn.addUndirectedEdge(smallest_edge.getData(), smallest_edge.getFrom().getLabel(), smallest_edge.getTo().getLabel());
                }

            processed_nodes.put(smallest_edge.getTo().getLabel(), smallest_edge.getTo());

        }
        return rtn;
    }

    /**
     * Djikstra's Algorithm (ALWAYS "a" TO "f")
     *
     * @return null if no such path exists or incorrect inputs, an arraylist with the labels of the nodes in order otherwise
     */
    public LinkedHashMap<String, Number> shortest_path() {
        String from_label = "a";
        String to_label = "f";
        GNode<V> from = nodes.get(from_label);
        GNode<V> to = nodes.get(to_label);
        if (from == null || to == null) {
            return null;
        }
        LinkedHashMap<String, Number> rtn = new LinkedHashMap<>();

        //key = each node, value = where it came from
        HashMap<GNode<V>, GNode<V>> history = new HashMap<>();
        //key = each node thats been visited, value = shortest length to that node
        HashMap<GNode<V>, Number> visited = new HashMap<>();
        //key = each node, value = current shortest length
        HashMap<GNode<V>, Number> lengths = new HashMap<>();

        for (GNode<V> node : nodes.values()) {
            lengths.put(node, Integer.MAX_VALUE);
            history.put(node, null);
        }
        visited.put(from, 0);
        lengths.replace(from, 0);
        history.replace(from, from);

        GNode<V> curr = from;
        boolean done = false;

        while (!done) {
            //check if youre on the end node (if so youre done)
            if (curr.equals(to)) break;
            //go through edges
            for (GEdge<E> edge : curr.getEdges()) {
                if (edge.getData() instanceof Number) {
                    Number num = (Number) edge.getData();
                    if (((int) num + (int) visited.get(curr)) < (int) lengths.get(edge.getTo()) || (int) lengths.get(edge.getTo()) == Integer.MAX_VALUE) {
                        //replace the length, update the history
                        lengths.replace(edge.getTo(), (int) num + (int) visited.get(curr));
                        history.replace(edge.getTo(), curr);
                    }
                }
            }
            //see if all nodes have been analyzed
            if (visited.size() == nodes.size()) break;
            //find out which node to analyze next
            GNode<V> shortest = null;
            for (GNode<V> node : lengths.keySet()) {

                if (visited.get(node) == null) {
                    if ((int) lengths.get(node) != Integer.MAX_VALUE) {
                        if (shortest == null) shortest = node;
                        else if ((int) lengths.get(node) < (int) lengths.get(shortest)) {
                            shortest = node;
                        }
                    }
                }
            }
            curr = shortest;
            visited.put(shortest, lengths.get(shortest));
            if (shortest == null) {
                //no path exists
                return null;
            }
            if (shortest == to) {
                break;
            }

        }
        //now we have to back-track for the path
        GNode<V> temp = to;
        ArrayList<GNode<V>> path = new ArrayList<>();
        path.add(to);
        while (temp != from) {
            path.add(history.get(temp));
            temp = history.get(temp);
        }
        for (int i = path.size() - 1; i > -1; i--) {
            //get the weight between the two, given its not the last one
            boolean added = false;
            if (i > 0) {
                E data = null;
                for (GEdge<E> edge : nodes.get(path.get(i).getLabel()).getEdges()) {
                    if (edge.getTo().getLabel().equals(path.get(i - 1).getLabel())) data = edge.getData();
                }
                if (data != null) {
                    if (data instanceof Number) {
                        added = true;
                        rtn.put(path.get(i).getLabel(), (Number) data);
                    }
                }

            }

            if (!added) rtn.put(path.get(i).getLabel(), 0);
        }
        return rtn;
    }

    /**
     * returns weighted adjacency matrix in a string format
     *
     * @return string weighted adjacency matrix
     */
    public String toString() {
        ArrayList<ArrayList<Integer>> matrix = new ArrayList<>();
        //char 97 = "a", should always be first node
        int from = 97;
        String from_label = Character.toString((char) from);
        for (int i = 0; i < nodes.size(); i++) {
            from_label = Character.toString((char) from);
            int to = 97;
            String to_label = Character.toString((char) to);
            matrix.add(new ArrayList<>());
            for (int j = 0; j < nodes.size(); j++) {

                to_label = Character.toString((char) to);

                GEdge<E> found_edge = null;
                for (GEdge<E> edge : nodes.get(from_label).getEdges()) {
                    if (edge.getTo().getLabel().equals(to_label)) {
                        if (found_edge != null) {
                            if (found_edge.getData() instanceof Number && edge.getData() instanceof Number) {
                                if ((Integer) edge.getData() < (Integer) found_edge.getData()) found_edge = edge;
                            }
                        } else {
                            found_edge = edge;
                        }

                    }
                }
                if (found_edge != null) {
                    matrix.get(from - 97).add((Integer) found_edge.getData());
                } else {
                    matrix.get(from - 97).add(0);
                }
                to++;
            }
            from++;
        }

        StringBuilder rtn = new StringBuilder("");
        for (ArrayList<Integer> arr : matrix) {
            for (Integer i : arr) {
                rtn.append(i).append(" ");
            }
            rtn.replace(rtn.length() - 1, rtn.length(), "\n");
        }
        return rtn.toString();
    }

    /**
     * returns the cost of all the edges with numerical weights
     *
     * @return cost of all edges (check for undirected edge)
     */
    public double getCost() {
        ArrayList<GEdge<E>> edges = new ArrayList<>();
        for (GNode<V> node : nodes.values()) {
            for (GEdge<E> edge : node.getEdges()) {
                if (edge.getData() instanceof Number) {
                    //handle undirected edges
                    if (edge.isUndirected()){
                        boolean found = false;
                        for (GEdge<E> edge2 : edges){
                            if (edge2.getTo().equals(edge.getFrom()) && edge2.getFrom().equals(edge.getTo()) && edge.getData() == edge2.getData()){
                                found = true;
                            }
                        }
                        if (!found){
                            edges.add(edge);
                        }
                    }
                }
            }
        }

        double rtn = 0;
        for (GEdge<E> edge : edges){
            rtn += ((Number) edge.getData()).doubleValue();
        }
        return rtn;
    }

    /**
     * Node class which stores data and a list of edges from it
     * (can exist without edges)
     *
     * @param <V> data type to store
     */
    private class GNode<V> {
        private V data;
        private String label;
        private ArrayList<GEdge<E>> edges;

        /**
         * constructs a new node
         *
         * @param data  data for node
         * @param label label for node (unique attribute)
         */
        public GNode(V data, String label) {
            this.data = data;
            this.label = label;
            edges = new ArrayList<>();
        }

        /**
         * gets the data attribute
         *
         * @return data attribute
         */
        public V getData() {
            return data;
        }

        /**
         * sets the data attribute
         *
         * @param data new data value
         */
        public void setData(V data) {
            this.data = data;
        }

        /**
         * gets the label of this node
         *
         * @return label
         */
        public String getLabel() {
            return this.label;
        }

        /**
         * sets the label attribute
         *
         * @param label new label value
         */
        public void setLabel(String label) {
            this.label = label;
        }

        /**
         * adds a new edge to this node
         *
         * @param newEdge new edge to add
         * @return true if added successfully, false otherwise
         */
        public boolean addEdge(GEdge<E> newEdge) {
            if (newEdge != null) {
                return edges.add(newEdge);
            }
            return false;

        }

        /**
         * removes an existing edge given it exists
         *
         * @param edge edge to remove
         * @return true if edge was removed, false otherwise
         */
        public boolean removeEdge(GEdge<E> edge) {
            return edges.remove(edge);
        }

        /**
         * returns a list of the edges this node points from
         *
         * @return list of edges
         */
        public ArrayList<GEdge<E>> getEdges() {
            return edges;
        }

        /**
         * checks if this node is equal to another
         *
         * @param obj other node to check
         * @return true if they have the same label (unique attribute), false otherwise
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof GNode) {
                GNode g = (GNode) obj;
                //since labels are unique, if 2 nodes share a label they must be the same node
                if (this.label.equals(g.getLabel())) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * directed weighted edge object which stores a weight and a pointer to the 2 edges it connects
     * (cannot exist without 2 nodes to point to)
     *
     * @param <E> data for edge (Weight)
     */
    private class GEdge<E> {
        private E data;
        private GNode<V> to;
        private GNode<V> from;
        //if true, another with the same "data" exists from the "to" node to the "from" node
        private boolean isUndirected;

        /**
         * constructs a new edge between 2 nodes
         *
         * @param data weight of edge
         * @param from node it starts from
         * @param to   node it points to
         */
        public GEdge(E data, GNode<V> from, GNode<V> to, boolean isUndirected) {
            if (!(from == null || to == null)) {
                this.from = from;
                this.to = to;
            }
            this.data = data;
            this.isUndirected = isUndirected;
        }

        /**
         * gets the from node
         *
         * @return from node
         */
        public GNode<V> getFrom() {
            return from;
        }

        /**
         * sets the from node
         *
         * @param from new from node
         */
        public void setFrom(GNode<V> from) {
            this.from = from;
        }

        /**
         * gets the weight
         *
         * @return weight
         */
        public E getData() {
            return data;
        }

        /**
         * sets the data value
         *
         * @param data new data value
         */
        public void setData(E data) {
            this.data = data;
        }

        /**
         * gets the to node
         *
         * @return to node
         */
        public GNode<V> getTo() {
            return to;
        }

        /**
         * sets the to node
         *
         * @param to new to node
         */
        public void setTo(GNode<V> to) {
            this.to = to;
        }

        /**
         * checks if this edge and another edge are equal
         *
         * @param obj other object to check
         * @return true if they are equal (in terms of their attributes), false otherwise
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof GEdge) {
                GEdge g = (GEdge) obj;
                if (this.data.equals(g.getData()) && this.getTo().equals(g.getTo()) && this.getFrom().equals(g.getFrom())) {
                    return true;
                }
            }
            return false;
        }

        /**
         * sets the value of being directed or undirected
         * @param b true if undirected, false otherwise
         */
        public void setDirection(boolean b) {
            this.isUndirected = b;
        }

        /**
         * returns if this edge is directed
         * @return false if directed, true if undirected
         */
        public boolean isUndirected(){
            return this.isUndirected;
        }

    }

}