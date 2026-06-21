package application;

public class Graph {
    // List of all cities, using our custom MyLinkedList.
    private final MyLinkedList<City> cities = new MyLinkedList<>();
    // Adjacency list: each city has a list of outgoing roads.
    private final MyLinkedList<MyLinkedList<Edge>> adjacency = new MyLinkedList<>();

    public void addCity(City city) {
        // Add the city to the city list.
        cities.add(city);
        // Create an empty neighbor list with the same city index.
        adjacency.add(new MyLinkedList<Edge>());
    }

    public void addDirectedRoad(String fromName, String toName) {
        // Find both cities by the names written in the file.
        City from = findCity(fromName);
        City to = findCity(toName);
        // The graph is directed, so add only from -> to.
        adjacency.get(from.getIndex()).add(new Edge(from, to));
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

    public MyLinkedList<Edge> getEdgesFrom(City city) {
        // Safety check for an invalid index.
        if (city.getIndex() < 0 || city.getIndex() >= adjacency.size()) {
            return new MyLinkedList<Edge>();
        }
        // Return all outgoing roads from this city.
        return adjacency.get(city.getIndex());
    }

    public MyLinkedList<City> getCities() {
        // Return all cities so the UI and lists can use them.
        return cities;
    }

    public MyLinkedList<MyLinkedList<Edge>> getAllAdjacencyLists() {
        // Return all adjacency lists if we need to draw or inspect roads.
        return adjacency;
    }
}
