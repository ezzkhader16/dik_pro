package application;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class GazaMapApp extends Application {
    // Full application window size.
    private static final int WINDOW_WIDTH = 1040;
    private static final int WINDOW_HEIGHT = 940;
    // Map area size.
    private static final int CANVAS_WIDTH = 740;
    private static final int CANVAS_HEIGHT = 918;
    // Radius of the city point/button.
    private static final double CITY_RADIUS = 6.5;
    // General projection coefficients used only for new cities without pixel calibration.
    private static final double PROJECT_X_LON = 2353.69909956;
    private static final double PROJECT_X_LAT = -705.88745472;
    private static final double PROJECT_X_OFFSET = -58398.55913950;
    private static final double PROJECT_Y_LON = -880.65639939;
    private static final double PROJECT_Y_LAT = -1606.52289273;
    private static final double PROJECT_Y_OFFSET = 81234.59497129;
    private static final double SOURCE_IMAGE_WIDTH = 1126.0;
    private static final double SOURCE_IMAGE_HEIGHT = 1397.0;
    // Size of the Button that represents a city on the map.
    private static final double CITY_BUTTON_SIZE = 17.0;

    // Number format used when displaying distance.
    private final DecimalFormat distanceFormat = new DecimalFormat("0.00");
    // Screen position of each city after projection.
    private Point2D[] screenPoints;
    // Map image loaded from resources.
    private Image backgroundImage;

    // Graph containing cities and roads.
    private Graph graph;
    // Algorithm object.
    private Dijkstra dijkstra;
    // Canvas used to draw the background, path, and labels.
    private Canvas canvas;
    // Pane that contains the canvas and city buttons.
    private Pane mapPane;
    // Source selection combo box.
    private ComboBox<City> sourceBox;
    // Target selection combo box.
    private ComboBox<City> targetBox;
    // Area used to display the path and distance.
    private TextArea pathArea;
    // Area used to display city information when its button is clicked.
    private TextArea cityInfoArea;
    // Small field for the distance value.
    private TextField distanceField;
    // Last calculated path, kept so it can be drawn on the map.
    private MyLinkedList<City> currentPath = new MyLinkedList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            // Read cities and roads from the input file.
            graph = new MapFileReader().read(Path.of("data", "map_data.txt"));
            // Prepare the Dijkstra object after building the graph.
            dijkstra = new Dijkstra(graph);
            // Array of city locations on the screen.
            screenPoints = new Point2D[graph.getCities().size()];
            // Load the map image from resources.
            backgroundImage = new Image(getClass().getResourceAsStream("/images/map-background.png"));
        } catch (IOException | RuntimeException ex) {
            // Show an error message if reading or loading data fails.
            showError("Cannot load map file", ex.getMessage());
            return;
        }

        // Canvas is the layer where the map and path are drawn.
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        // Clicking the map shows the nearest city, but does not select source/target.
        canvas.setOnMouseClicked(this::handleMapClick);
        // Pane allows city buttons to be placed above the Canvas.
        mapPane = new Pane(canvas);
        mapPane.setPrefSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        mapPane.setMinSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        mapPane.setMaxSize(CANVAS_WIDTH, CANVAS_HEIGHT);

        BorderPane root = new BorderPane();
        // Map is placed on the left.
        root.setLeft(mapPane);
        // Controls are placed on the right.
        root.setRight(createControlPanel());
        root.setStyle("-fx-background-color: white;");

        // Initial map drawing.
        drawMap();

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setTitle("Gaza Dijkstra Shortest Path");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createControlPanel() {
        // Create the combo boxes.
        sourceBox = new ComboBox<>();
        targetBox = new ComboBox<>();
        // Fill the combo boxes with cities read from the file.
        for (int i = 0; i < graph.getCities().size(); i++) {
            City city = graph.getCities().get(i);
            sourceBox.getItems().add(city);
            targetBox.getItems().add(city);
        }
        sourceBox.setMaxWidth(Double.MAX_VALUE);
        targetBox.setMaxWidth(Double.MAX_VALUE);
        // When source is selected, show its coordinates and redraw.
        sourceBox.setOnAction(event -> {
            if (sourceBox.getValue() != null) {
                showCityInfo(sourceBox.getValue());
            }
            drawMap();
        });
        // When target is selected, show its coordinates and redraw.
        targetBox.setOnAction(event -> {
            if (targetBox.getValue() != null) {
                showCityInfo(targetBox.getValue());
            }
            drawMap();
        });

        // Button that runs Dijkstra.
        Button runButton = new Button("Run");
        runButton.setMaxWidth(Double.MAX_VALUE);
        runButton.setOnAction(event -> runDijkstra());

        // Button that clears selections and results.
        Button clearButton = new Button("Clear");
        clearButton.setMaxWidth(Double.MAX_VALUE);
        clearButton.setOnAction(event -> clearSelection());

        // TextArea used to display the path.
        pathArea = new TextArea();
        pathArea.setEditable(false);
        pathArea.setWrapText(true);
        pathArea.setPrefRowCount(8);

        // TextArea used to display the city name and coordinates when its button is clicked.
        cityInfoArea = new TextArea();
        cityInfoArea.setEditable(false);
        cityInfoArea.setWrapText(true);
        cityInfoArea.setPrefRowCount(4);

        // Separate field for distance in kilometers.
        distanceField = new TextField();
        distanceField.setEditable(false);

        // Arrange the right-side UI elements vertically.
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(18, 16, 18, 16));
        panel.setPrefWidth(280);
        panel.setStyle("-fx-border-color: black; -fx-border-width: 0 0 0 3; -fx-background-color: white;");
        panel.getChildren().addAll(
                label("Source:"),
                sourceBox,
                label("Target:"),
                targetBox,
                new HBox(8, runButton, clearButton),
                label("City Info:"),
                cityInfoArea,
                label("Path:"),
                pathArea,
                label("Distance (km):"),
                distanceField
        );
        VBox.setVgrow(pathArea, Priority.ALWAYS);
        return panel;
    }

    private Label label(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 19));
        return label;
    }

    private void runDijkstra() {
        // Read source and target only from the combo boxes.
        City source = sourceBox.getValue();
        City target = targetBox.getValue();

        // Do not run the algorithm unless source and target are selected.
        if (source == null || target == null) {
            showError("Missing selection", "Choose source and target by mouse or from the lists.");
            return;
        }

        // Run Dijkstra and return the path and distance.
        PathResult result = dijkstra.shortestPath(source, target);
        currentPath = result.getPath();

        // Show a message if no path exists.
        if (!result.exists()) {
            pathArea.setText("No path found.");
            distanceField.setText("");
        } else {
            // Display the path and distance.
            String distanceText = distanceFormat.format(result.getDistanceKm());
            pathArea.setText(formatPath(currentPath) + "\n\nDistance: " + distanceText + " km");
            distanceField.setText(distanceText);
        }

        // Redraw so the path appears on the map.
        drawMap();
    }

    private String formatPath(MyLinkedList<City> path) {
        // Convert the city list to text like A -> B -> C.
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) {
                builder.append(" -> ");
            }
            builder.append(path.get(i).getName());
        }
        return builder.toString();
    }

    private void clearSelection() {
        // Clear source, target, and results.
        sourceBox.setValue(null);
        targetBox.setValue(null);
        currentPath = new MyLinkedList<>();
        pathArea.clear();
        distanceField.clear();
        drawMap();
    }

    private void handleMapClick(MouseEvent event) {
        // When the map is clicked, find the nearest city to the click.
        City nearest = findNearestCity(event.getX(), event.getY());
        if (nearest == null) {
            return;
        }

        // Clicking the map displays coordinates only.
        showCityInfo(nearest);
    }

    private City findNearestCity(double x, double y) {
        // Search for the nearest city within 16 pixels.
        City best = null;
        double bestDistance = 16.0;

        for (int i = 0; i < graph.getCities().size(); i++) {
            City city = graph.getCities().get(i);
            Point2D point = screenPoints[city.getIndex()];
            double distance = point.distance(x, y);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = city;
            }
        }

        return best;
    }

    private void drawMap() {
        // Draw the background.
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawBackground(gc);

        // Calculate city positions on the screen.
        computeScreenPoints();
        // Draw the path only if it exists.
        drawCurrentPath(gc);
        // Draw city names.
        drawCityLabels(gc);
        // Place city buttons above the map.
        refreshCityButtons();
    }

    private void drawBackground(GraphicsContext gc) {
        // Fallback color if the image is not displayed.
        gc.setFill(Color.rgb(29, 36, 50));
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        if (backgroundImage == null || backgroundImage.isError()) {
            return;
        }

        // Draw the map image over the whole Canvas.
        gc.drawImage(backgroundImage, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    private void computeScreenPoints() {
        // Calculate each city position on the Canvas.
        for (int i = 0; i < graph.getCities().size(); i++) {
            City city = graph.getCities().get(i);
            screenPoints[city.getIndex()] = projectCity(city);
        }
    }

    private Point2D projectCity(City city) {
        // If the city has a calibrated pixel point for the current image, use it.
        Point2D calibratedPoint = calibratedImagePoint(city.getName());
        if (calibratedPoint != null) {
            return calibratedPoint;
        }

        // If a new city is added from the file and has no calibration,
        // use a general conversion from longitude/latitude to x/y.
        double x = PROJECT_X_LON * city.getLongitude()
                + PROJECT_X_LAT * city.getLatitude()
                + PROJECT_X_OFFSET;
        double y = PROJECT_Y_LON * city.getLongitude()
                + PROJECT_Y_LAT * city.getLatitude()
                + PROJECT_Y_OFFSET;
        // keepPointOnMap prevents the point from falling into the sea as much as possible.
        return keepPointOnMap(new Point2D(x, y));
    }

    private Point2D calibratedImagePoint(String cityName) {
        // These points are taken from the map image itself so they appear over the correct labels.
        switch (cityName) {
            case "ErezCrossing":
                return pointFromSourceImage(980, 125);
            case "BeitLahia":
                return pointFromSourceImage(845, 185);
            case "BeitHanoun":
                return pointFromSourceImage(965, 230);
            case "Jabalia":
                return pointFromSourceImage(795, 255);
            case "GazaCity":
                return pointFromSourceImage(735, 345);
            case "AlShatiCamp":
                return pointFromSourceImage(600, 445);
            case "SheikhRadwan":
                return pointFromSourceImage(560, 505);
            case "Shujaiyya":
                return pointFromSourceImage(805, 415);
            case "AlZeitoun":
                return pointFromSourceImage(700, 530);
            case "Nuseirat":
                return pointFromSourceImage(560, 595);
            case "WadiGaza":
                return pointFromSourceImage(565, 625);
            case "Netzarim":
                return pointFromSourceImage(700, 650);
            case "DeirAlBalah":
                return pointFromSourceImage(430, 700);
            case "AlMaghazi":
                return pointFromSourceImage(520, 700);
            case "AlZawaida":
                return pointFromSourceImage(480, 805);
            case "AlQarara":
                return pointFromSourceImage(490, 840);
            case "AlMawasi":
                return pointFromSourceImage(150, 1010);
            case "KhanYunis":
                return pointFromSourceImage(310, 990);
            case "BaniSuheila":
                return pointFromSourceImage(585, 960);
            case "AbasanAlKabira":
                return pointFromSourceImage(590, 1065);
            case "Khuzaa":
                return pointFromSourceImage(482, 1115);
            case "TelAlSultan":
                return pointFromSourceImage(310, 1170);
            case "Rafah":
                return pointFromSourceImage(100, 1215);
            case "RafahCrossing":
                return pointFromSourceImage(230, 1320);
            case "KeremAbuSalem":
                return pointFromSourceImage(392, 1325);
            default:
                return null;
        }
    }

    private Point2D pointFromSourceImage(double sourceX, double sourceY) {
        return new Point2D(
                sourceX * CANVAS_WIDTH / SOURCE_IMAGE_WIDTH,
                sourceY * CANVAS_HEIGHT / SOURCE_IMAGE_HEIGHT
        );
    }

    private Point2D keepPointOnMap(Point2D point) {
        // Keep the point inside Canvas bounds.
        double x = clamp(point.getX(), 8, CANVAS_WIDTH - 8);
        double y = clamp(point.getY(), 8, CANVAS_HEIGHT - 8);
        // Approximate coastline; if the point is in the sea, shift it right onto land.
        double coastX = coastXAtY(y);

        if (x < coastX + 12) {
            x = coastX + 12;
        }

        return new Point2D(clamp(x, 8, CANVAS_WIDTH - 8), y);
    }

    private double coastXAtY(double y) {
        // Approximate coastline points on the image, used only for new cities.
        double[] ys = {0, 120, 230, 350, 470, 590, 700, 810, 918};
        double[] xs = {545, 485, 430, 365, 300, 230, 160, 80, 0};

        if (y <= ys[0]) {
            return xs[0];
        }
        if (y >= ys[ys.length - 1]) {
            return xs[xs.length - 1];
        }

        for (int i = 0; i < ys.length - 1; i++) {
            if (y >= ys[i] && y <= ys[i + 1]) {
                double t = (y - ys[i]) / (ys[i + 1] - ys[i]);
                return xs[i] + (xs[i + 1] - xs[i]) * t;
            }
        }
        return 0;
    }

    private double clamp(double value, double min, double max) {
        // Helper method to keep a value between min and max.
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private Point2D rotateAroundCenter(double x, double y, double degrees) {
        double centerX = CANVAS_WIDTH / 2.0;
        double centerY = CANVAS_HEIGHT / 2.0;
        double angle = Math.toRadians(degrees);
        double dx = x - centerX;
        double dy = y - centerY;
        return new Point2D(
                centerX + dx * Math.cos(angle) - dy * Math.sin(angle),
                centerY + dx * Math.sin(angle) + dy * Math.cos(angle)
        );
    }

    private void drawLand(GraphicsContext gc) {
        gc.setFill(Color.rgb(244, 241, 232));
        gc.setStroke(Color.rgb(95, 95, 95));
        gc.setLineWidth(3);

        double[] x = {125, 470, 690, 620, 585, 260, 85, 52};
        double[] y = {700, 105, 54, 585, 710, 712, 720, 665};
        gc.fillPolygon(x, y, x.length);
        gc.strokePolygon(x, y, x.length);

        gc.setFill(Color.rgb(230, 226, 216));
        gc.fillRect(640, 0, 120, CANVAS_HEIGHT);
    }

    private void drawRoads(GraphicsContext gc) {
        gc.setLineWidth(5);
        gc.setStroke(Color.rgb(0, 0, 0, 0.85));
        for (int i = 0; i < graph.getAllAdjacencyLists().size(); i++) {
            MyLinkedList<Edge> list = graph.getAllAdjacencyLists().get(i);
            for (int j = 0; j < list.size(); j++) {
                Edge edge = list.get(j);
                drawLine(gc, edge.getFrom(), edge.getTo());
            }
        }

        gc.setLineWidth(3.4);
        gc.setStroke(Color.rgb(0, 136, 255, 0.98));
        for (int i = 0; i < graph.getAllAdjacencyLists().size(); i++) {
            MyLinkedList<Edge> list = graph.getAllAdjacencyLists().get(i);
            for (int j = 0; j < list.size(); j++) {
                Edge edge = list.get(j);
                drawLine(gc, edge.getFrom(), edge.getTo());
            }
        }
    }

    private void drawCurrentPath(GraphicsContext gc) {
        // If no path has been calculated yet, draw nothing.
        if (currentPath.size() < 2) {
            return;
        }

        // Draw a black shadow under the path so it is visible over the map.
        gc.setLineWidth(9);
        gc.setStroke(Color.rgb(0, 0, 0, 0.80));
        for (int i = 0; i < currentPath.size() - 1; i++) {
            drawLine(gc, currentPath.get(i), currentPath.get(i + 1));
        }

        // Draw the actual path in pink.
        gc.setLineWidth(5.8);
        gc.setStroke(Color.rgb(255, 0, 120));
        for (int i = 0; i < currentPath.size() - 1; i++) {
            drawLine(gc, currentPath.get(i), currentPath.get(i + 1));
        }
    }

    private void drawLine(GraphicsContext gc, City first, City second) {
        // Get the two city positions and draw a line between them.
        Point2D a = screenPoints[first.getIndex()];
        Point2D b = screenPoints[second.getIndex()];
        gc.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());
    }

    private void drawCityLabels(GraphicsContext gc) {
        // Draw only city names; the city points themselves are Buttons.
        City source = sourceBox == null ? null : sourceBox.getValue();
        City target = targetBox == null ? null : targetBox.getValue();

        for (int i = 0; i < graph.getCities().size(); i++) {
            City city = graph.getCities().get(i);
            Point2D point = screenPoints[city.getIndex()];
            boolean selected = city == source || city == target;

            gc.setFont(Font.font("Arial", selected ? FontWeight.BOLD : FontWeight.NORMAL, selected ? 13 : 11));
            gc.setLineWidth(3.2);
            gc.setStroke(Color.rgb(0, 0, 0, 0.90));
            gc.strokeText(city.getName(), point.getX() + 9, point.getY() - 6);
            gc.setFill(selected ? Color.rgb(255, 243, 144) : Color.WHITE);
            gc.fillText(city.getName(), point.getX() + 8, point.getY() - 5);
        }
    }

    private void refreshCityButtons() {
        // Remove old city buttons before adding them again, so they do not duplicate.
        if (mapPane == null) {
            return;
        }

        mapPane.getChildren().removeIf(node -> node instanceof Button && "city-button".equals(node.getUserData()));

        // Know selected cities so their button color can change.
        City source = sourceBox == null ? null : sourceBox.getValue();
        City target = targetBox == null ? null : targetBox.getValue();

        // Create one Button for each city.
        for (int i = 0; i < graph.getCities().size(); i++) {
            City city = graph.getCities().get(i);
            Point2D point = screenPoints[city.getIndex()];
            Button cityButton = new Button();
            cityButton.setUserData("city-button");
            cityButton.setPrefSize(CITY_BUTTON_SIZE, CITY_BUTTON_SIZE);
            cityButton.setMinSize(CITY_BUTTON_SIZE, CITY_BUTTON_SIZE);
            cityButton.setMaxSize(CITY_BUTTON_SIZE, CITY_BUTTON_SIZE);
            cityButton.setLayoutX(point.getX() - CITY_BUTTON_SIZE / 2.0);
            cityButton.setLayoutY(point.getY() - CITY_BUTTON_SIZE / 2.0);
            // Tooltip appears when the mouse hovers over the button.
            cityButton.setTooltip(new Tooltip(city.getName() + "\nLat: " + city.getLatitude()
                    + "\nLon: " + city.getLongitude()));
            cityButton.setStyle(cityButtonStyle(city == source || city == target));
            cityButton.setOnAction(event -> handleCityButtonClick(city));
            mapPane.getChildren().add(cityButton);
        }
    }

    private String cityButtonStyle(boolean selected) {
        // If the city is source or target it appears yellow; otherwise red.
        String fill = selected ? "#ffd100" : "#ff4848";
        return "-fx-background-color: " + fill + ";"
                + "-fx-background-radius: 20;"
                + "-fx-border-color: white;"
                + "-fx-border-width: 2;"
                + "-fx-border-radius: 20;"
                + "-fx-padding: 0;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.85), 4, 0.6, 0, 0);";
    }

    private void handleCityButtonClick(City city) {
        // The button only displays city information and does not change source/target.
        showCityInfo(city);
    }

    private void showCityInfo(City city) {
        // Display the city name and coordinates in the right panel.
        cityInfoArea.setText(
                "City: " + city.getName()
                        + "\nLatitude: " + city.getLatitude()
                        + "\nLongitude: " + city.getLongitude()
        );
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message == null ? "" : message);
        alert.showAndWait();
    }
}
