package com.example.AI.mapmaker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.json.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

public class MapMaker {
    float[] bounds;
    HashMap<Integer, Location> nodes = new HashMap<Integer, Location>();

    public MapMaker(float[] bounds) {
        this.bounds = bounds;
    }

    public boolean parseData(String filename) {
        DataFetcher fetcher = new DataFetcher(bounds);
        JsonObject data = fetcher.getData();

        JsonArray elements = data.getJsonArray("elements");

        for (JsonObject elem : elements.getValuesAs(JsonObject.class)) {
            if (elem.getString("type").equals("node")) {
                nodes.put(elem.getInt("id"), new Location(elem.getJsonNumber("lat").doubleValue(), elem.getJsonNumber("lon").doubleValue()));
            }
        }

        PrintWriter outfile;
        try {
            outfile = new PrintWriter(filename);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        for (JsonObject elem : elements.getValuesAs(JsonObject.class)) {
            if (elem.getString("type").equals("way")) {
                String street = elem.getJsonObject("tags").getString("name", "");
                String type = elem.getJsonObject("tags").getString("highway", "");
                String oneway = elem.getJsonObject("tags").getString("oneway", "no");
                List<JsonNumber> nodelist = elem.getJsonArray("nodes").getValuesAs(JsonNumber.class);
                for (int i = 0; i < nodelist.size() - 1; i++) {
                    Location start = nodes.get(nodelist.get(i).intValue());
                    Location end = nodes.get(nodelist.get(i + 1).intValue());
                    if (start.outsideBounds(bounds) || end.outsideBounds(bounds)) {
                        continue;
                    }

                    outfile.println("" + start + end + "\"" + street + "\" " + type);
                    if (oneway.equals("no")) {
                        outfile.println("" + end + start + "\"" + street + "\" " + type);
                    }
                }
            }
        }
        outfile.close();
        return true;
    }
    public static float[] parseBoundsFromXML(String xmlFilePath) {
        try {
            File xmlFile = new File(xmlFilePath);
            if (!xmlFile.exists()) {
                System.out.println("Error: File not found: " + xmlFilePath);
                return null;
            }
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            Element boundsElement = (Element) doc.getElementsByTagName("bounds").item(0);
            if (boundsElement == null) {
                System.out.println("Error: No <bounds> element found in XML file");
                return null;
            }

            float minlat = Float.parseFloat(boundsElement.getAttribute("minlat"));
            float minlon = Float.parseFloat(boundsElement.getAttribute("minlon"));
            float maxlat = Float.parseFloat(boundsElement.getAttribute("maxlat"));
            float maxlon = Float.parseFloat(boundsElement.getAttribute("maxlon"));

            // Return in format [south, west, north, east]
            return new float[]{minlat, minlon, maxlat, maxlon};

        } catch (Exception e) {
            System.out.println("Error parsing XML file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

class Location {
    private double lat;
    private double lon;

    public Location(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }
    public String toString() {
        return "" + lat + " " + lon + " ";
    }

    public boolean outsideBounds(float[] bounds) {
        return (lat < bounds[0] || lat > bounds[2] || lon < bounds[1] || lon > bounds[3]);
    }
}
