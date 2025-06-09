
package com.example.AI.mapmaker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.json.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DataFetcher {
    private final String[] HIGHWAYS = {"motorway", "trunk", "primary", "secondary", "tertiary", "unclassified", "residential", "motorway_link", "trunk_link", "primary_link", "secondary_link", "tertiary_link", "living_street"};

    private String query;
    public DataFetcher(float[] bounds) {
        this.query = this.constructQuery(bounds);
    }

    public JsonObject getData() {
        HttpURLConnection conn = null;

        try {
            URL url = new URL("http://overpass-api.de/api/interpreter");
            conn = (HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept-Charset", "utf-8;q=0.7,*;q=0.7");
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(this.query);
            wr.close();

            InputStream is = conn.getInputStream();
            JsonReader rdr = Json.createReader(is);

            return rdr.readObject();
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public String constructQuery(float[] boundsArray) {
        String q = "[out:json];(";
        String bounds = "(";
        for (int i = 0; i < 4; i++) {
            bounds += boundsArray[i];
            if (i < 3) {
                bounds += ",";
            } else {
                bounds += ")";
            }
        }

        for (String s : HIGHWAYS) {
            q += "way[\"highway\"=\"" + s + "\"]" + bounds + ";";
        }

        q += "); (._;>;); out;";

        return q;
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