package application;

public class Edge {
    // The city where the road starts.
    private final City from;
    // The city where the road ends.
    private final City to;
    // Road weight in kilometers, calculated from coordinates.
    private final double distanceKm;

    public Edge(City from, City to) {
        // Store both road endpoints.
        this.from = from;
        this.to = to;
        // Do not store distance in the file; calculate it at runtime.
        this.distanceKm = GeoDistance.kilometers(from, to);
    }

    public City getFrom() {
        // Return the start of the road.
        return from;
    }

    public City getTo() {
        // Return the end of the road.
        return to;
    }

    public double getDistanceKm() {
        // Return the road weight used by Dijkstra.
        return distanceKm;
    }
}
