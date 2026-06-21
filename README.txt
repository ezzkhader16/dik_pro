Gaza Dijkstra JavaFX - VS Code Version
======================================

Open this exact folder in VS Code:
GazaDijkstraVSCodeMaven

Do not open src or application alone.

How to run in VS Code:
1. Open VS Code.
2. File -> Open Folder.
3. Choose GazaDijkstraVSCodeMaven.
4. Press F5 and choose:
   F5 - Run Without Maven

This project includes pom.xml, but Maven is not required for F5.
The VS Code task compiles directly with javac and uses:
C:\JavaSDK\javafx-sdk-21.0.10\lib

If JavaFX is installed somewhere else, edit:
.vscode\launch.json
.vscode\tasks.json

Main class:
application.Main

JavaFX application class:
application.GazaMapApp

Data file:
data/map_data.txt

Coordinate notes:
data/coordinate_sources.txt

Map background image:
src/main/resources/images/map-background.png

The program draws city points, labels, and the selected shortest path on top of this image.
Each city point is a JavaFX Button. Clicking a city button displays its latitude and longitude.
Source and target are selected only from the combo boxes.
Roads in data/map_data.txt are directed: each line means FromCity ToCity.

Coordinate source:
The Gaza locations in data/map_data.txt were checked using OpenStreetMap/Nominatim where available.

Notes:
- The UI does not use FXML.
- Maven is only used for dependencies and running JavaFX.
- The algorithm uses custom MyLinkedList and custom MinPriorityQueue.
- It does not use java.util.LinkedList, java.util.PriorityQueue, HashMap, Map, or List.
