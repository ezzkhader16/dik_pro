package application;

public class PathResult {
    // List of cities that form the shortest path.
    private final MyArrayList<City> path;
    // Total distance of this path in kilometers.
    private final double distanceKm;

    public PathResult(MyArrayList<City> path, double distanceKm) {
        // Store the algorithm result.
        this.path = path;
        this.distanceKm = distanceKm;
    }

    public MyArrayList<City> getPath() {
        // Return the path so the UI can print and draw it.
        return path;
    }

    public double getDistanceKm() {
        // Return the distance to the UI.
        return distanceKm;
    }

    public boolean exists() {
        // If the path is empty, no path exists.
        return !path.isEmpty();
    }
}
