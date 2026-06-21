package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringTokenizer;

public class MapFileReader {
    public Graph read(Path path) throws IOException {
        // Create a new graph and fill it from the file.
        Graph graph = new Graph();

        // try-with-resources closes the file automatically after reading.
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            // The first real line contains the number of cities and roads.
            String header = nextDataLine(reader);
            if (header == null) {
                throw new IOException("Map file is empty.");
            }

            // StringTokenizer splits the line by spaces.
            StringTokenizer headerTokens = new StringTokenizer(header);
            int cityCount = Integer.parseInt(headerTokens.nextToken());
            int edgeCount = Integer.parseInt(headerTokens.nextToken());

            // Read cities: name latitude longitude.
            for (int i = 0; i < cityCount; i++) {
                String line = nextDataLine(reader);
                if (line == null) {
                    throw new IOException("Missing city line number " + (i + 1));
                }

                StringTokenizer tokens = new StringTokenizer(line);
                String name = tokens.nextToken();
                double latitude = Double.parseDouble(tokens.nextToken());
                double longitude = Double.parseDouble(tokens.nextToken());
                // The index equals the city order in the file.
                graph.addCity(new City(i, name, latitude, longitude));
            }

            // Read directed roads: FromCity ToCity.
            for (int i = 0; i < edgeCount; i++) {
                String line = nextDataLine(reader);
                if (line == null) {
                    throw new IOException("Missing road line number " + (i + 1));
                }

                StringTokenizer tokens = new StringTokenizer(line);
                // Add one directed road from the first city to the second city.
                graph.addDirectedRoad(tokens.nextToken(), tokens.nextToken());
            }
        }

        // Return a graph ready for the algorithm and UI.
        return graph;
    }

    private String nextDataLine(BufferedReader reader) throws IOException {
        String line;
        // Ignore empty lines and comments that start with #.
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                return line;
            }
        }
        return null;
    }
}
