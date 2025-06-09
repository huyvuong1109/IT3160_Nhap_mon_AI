package com.example.AI.Service;

import com.example.AI.DTO.MapRequest;

import com.example.AI.geography.GeographicPoint;
import com.example.AI.mapmaker.MapMaker;
import com.example.AI.roadgraph.MapGraph;
import com.example.AI.util.GraphLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapService {
    public String createMap(MapRequest request){
        log.info("Service: Create map");
        String mapContent = request.getMap();
        String osmFilePath = "D:/Nam3/ky2/AI/Map/BackEnd/AI/src/main/java/com/example/AI/html/AI.osm";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(osmFilePath))) {
            writer.write(mapContent);
        } catch (IOException e) {
            log.error("Lỗi khi ghi file OSM: ", e);
            return "Lỗi ghi file OSM";
        }
        float[] bound_arr = MapMaker.parseBoundsFromXML(osmFilePath);
        MapMaker map = new MapMaker(bound_arr);
        map.parseData("D:/Nam3/ky2/AI/Map/BackEnd/AI/src/main/java/com/example/AI/html/AI.map");
        GraphLoader.createIntersectionsFile("D:/Nam3/ky2/AI/Map/BackEnd/AI/src/main/java/com/example/AI/html/AI.map", "D:/Nam3/ky2/AI/Map/BackEnd/AI/src/main/java/com/example/AI/html/AI.intersections");
        return "Load map done";
    }

    public String saveLocation(MapRequest request) {
        log.info("Service: Save location");
        String loaction=request.getMap();
        String filename = "src/main/java/com/example/AI/html/toado.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(loaction);
        } catch (IOException e) {
            log.error("Lỗi khi ghi file location: ", e);
            return "Lỗi ghi file location";
        }
        return "Save loaction";
    }

    public String findRoute() {
        MapGraph theMap = new MapGraph();
        System.out.print("DONE. \nLoading the map...");
        GraphLoader.loadRoadMap("src/main/java/com/example/AI/html/AI.map", theMap);
        System.out.println("DONE.");
        String filename = "src/main/java/com/example/AI/html/toado.txt";
        List<Double> coordinates = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\\s+"); // Tách theo khoảng trắng
                if (parts.length == 2) {
                    double lat = Double.parseDouble(parts[0]);
                    double lon = Double.parseDouble(parts[1]);
                    coordinates.add(lat);
                    coordinates.add(lon);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        if (coordinates.size() >= 4) {
            GeographicPoint start = new GeographicPoint(coordinates.get(0) ,coordinates.get(1));
            GeographicPoint end = new GeographicPoint(coordinates.get(2),coordinates.get(3));
//		    List<GeographicPoint> route = theMap.dijkstra(start,end);
//			List<GeographicPoint> route = theMap.aStarSearch(start,end);
            List<GeographicPoint> route = theMap.bfs(start,end);
            System.out.println(route);
            theMap.saveRouteEachPointPerLine(route,"src/main/java/com/example/AI/html/route.txt");
        } else {
            throw new RuntimeException("Không đủ dữ liệu trong file toado.txt");
        }
        return "Find route done";
    }
}
