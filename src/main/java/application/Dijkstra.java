package application;

public class Dijkstra {
    // Very large value used as infinity, meaning the distance is not known yet.
    private static final double INF = Double.MAX_VALUE;

    // This is the Dijkstra table from the slides: known, dist, path.
    private static class TableEntry {
        // True means the shortest distance for this city is finalized.
        boolean known;
        // Shortest known distance from the source to this city.
        double dist = INF;
        // Previous city in the shortest path.
        City path;
        // Used to reset only changed cities instead of all cities.
        boolean touched;
    }

    // Item inside the priority queue; it stores the city and its distance priority.
    private static class QueueNode implements PriorityItem {
        City city;
        double distance;

        QueueNode(City city, double distance) {
            this.city = city;
            this.distance = distance;
        }

        @Override
        public double getPriority() {
            // MinPriorityQueue chooses the lowest priority, meaning the smallest distance.
            return distance;
        }
    }

    // The graph that contains cities and roads.
    private final Graph graph;
    // Dijkstra table, one entry for each city by city index.
    private final TableEntry[] table;
    // Cities changed during the last algorithm run.
    private final MyArrayList<City> touched = new MyArrayList<>();

    public Dijkstra(Graph graph) {
        this.graph = graph;
        // The table size equals the number of cities.
        this.table = new TableEntry[graph.getCities().size()];
        for (int i = 0; i < table.length; i++) {
            table[i] = new TableEntry();
        }
    }

    public PathResult shortestPath(City source, City target) {
        // Clear previous search results only for touched cities.
        resetTouchedEntries();
        // Mark the source as used in this search.
        touch(source);
        // The distance from the source to itself is zero.
        table[source.getIndex()].dist = 0.0;

        // The queue always returns the city with the smallest current distance.
        MinPriorityQueue<QueueNode> queue = new MinPriorityQueue<>();
        queue.add(new QueueNode(source, 0.0));

        // Continue while there are candidate cities to examine.
        while (!queue.isEmpty()) {
            // The city with the smallest distance.
            QueueNode currentNode = queue.poll();
            City v = currentNode.city;
            TableEntry vEntry = table[v.getIndex()];

            // If the city was finalized before, or this node is old, skip it.
            if (vEntry.known || currentNode.distance > vEntry.dist) {
                continue;
            }

            // Now the shortest distance for this city is finalized.
            vEntry.known = true;
            // Project optimization: stop as soon as the target is reached.
            if (v == target) {
                break;
            }

            // Check all cities adjacent to the current city.
            MyArrayList<Edge> edges = graph.getEdgesFrom(v);
            for (int i = 0; i < edges.size(); i++) {
                Edge edge = edges.get(i);
                City w = edge.getTo();
                TableEntry wEntry = table[w.getIndex()];
                // Mark w as touched or included in this search.
                touch(w);

                // Do not update a city that is already finalized.
                if (!wEntry.known) {
                    // Relaxation: is the path through v shorter than the old one?
                    double newDistance = vEntry.dist + edge.getDistanceKm();
                    if (newDistance < wEntry.dist) {
                        // A shorter distance was found.
                        wEntry.dist = newDistance;
                        // Store the predecessor so the path can be printed later.
                        wEntry.path = v;
                        // Insert the city into the queue again with its new distance.
                        queue.add(new QueueNode(w, newDistance));
                    }
                }
            }
        }

        // If the distance stays infinity, no path exists.
        if (table[target.getIndex()].dist == INF) {
            return new PathResult(new MyArrayList<City>(), INF);
        }
        // Return the path and final distance.
        return new PathResult(buildPath(source, target), table[target.getIndex()].dist);
    }

    private MyArrayList<City> buildPath(City source, City target) {
        // Build the path backward from the target using path, then insert at the front.
        MyArrayList<City> path = new MyArrayList<>();
        City current = target;

        while (current != null) {
            // add(0, current) makes the path start at the source and end at the target.
            path.add(0, current);
            if (current == source) {
                break;
            }
            // Move to the previous city in the shortest path.
            current = table[current.getIndex()].path;
        }

        return path;
    }

    private void resetTouchedEntries() {
        // Reset only the cities used in the previous search.
        for (int i = 0; i < touched.size(); i++) {
            City city = touched.get(i);
            TableEntry entry = table[city.getIndex()];
            entry.known = false;
            entry.dist = INF;
            entry.path = null;
            entry.touched = false;
        }
        touched.clear();
    }

    private void touch(City city) {
        TableEntry entry = table[city.getIndex()];
        // If it is not already in touched, add it once.
        if (!entry.touched) {
            entry.touched = true;
            touched.add(city);
        }
    }
}
