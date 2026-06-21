package application;

public class Graph {
    // List of all cities, using our custom MyArrayList.
    private final MyArrayList<City> cities = new MyArrayList<>();
    // Adjacency list: each city has a list of outgoing roads.
    private final MyArrayList<MyArrayList<Edge>> adjacency = new MyArrayList<>();

    public void addCity(City city) {
        // Add the city to the city list.
        cities.add(city);
        // Create an empty neighbor list with the same city index.
        adjacency.add(new MyArrayList<Edge>());
    }

    public void addUndirectedRoad(String firstName, String secondName) {
        // Find both cities by the names written in the file.
        City first = findCity(firstName);
        City second = findCity(secondName);
        // The road is undirected, so add it in both directions.
        adjacency.get(first.getIndex()).add(new Edge(first, second));
        adjacency.get(second.getIndex()).add(new Edge(second, first));
    }

    public City findCity(String name) {
        // Linear search because HashMap is not used.
        for (int i = 0; i < cities.size(); i++) {
            City city = cities.get(i);
            if (city.getName().equals(name)) {
                return city;
            }
        }
        throw new IllegalArgumentException("Unknown city in map file: " + name);
    }

    public MyArrayList<Edge> getEdgesFrom(City city) {
        // Safety check for an invalid index.
        if (city.getIndex() < 0 || city.getIndex() >= adjacency.size()) {
            return new MyArrayList<Edge>();
        }
        // Return all roads adjacent to this city.
        return adjacency.get(city.getIndex());
    }

    public MyArrayList<City> getCities() {
        // Return all cities so the UI and lists can use them.
        return cities;
    }

    public MyArrayList<MyArrayList<Edge>> getAllAdjacencyLists() {
        // Return all adjacency lists if we need to draw or inspect roads.
        return adjacency;
    }
}
