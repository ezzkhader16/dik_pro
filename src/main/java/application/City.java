package application;

public class City {
    // City number inside the arrays, used for quick access to the table and adjacency list.
    private final int index;
    // City name exactly as written in map_data.txt.
    private final String name;
    // Real latitude of the city.
    private final double latitude;
    // Real longitude of the city.
    private final double longitude;

    public City(int index, String name, double latitude, double longitude) {
        // Store the values read from the input file.
        this.index = index;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getIndex() {
        // Return the city index.
        return index;
    }

    public String getName() {
        // Return the city name.
        return name;
    }

    public double getLatitude() {
        // Return latitude for display or distance calculation.
        return latitude;
    }

    public double getLongitude() {
        // Return longitude for display or distance calculation.
        return longitude;
    }

    @Override
    public String toString() {
        // ComboBox calls toString, so return the name shown in the list.
        return name;
    }
}
