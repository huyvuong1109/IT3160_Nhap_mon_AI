
package com.example.AI.roadgraph;

import com.example.AI.geography.GeographicPoint;


public class MapEdge {

    private GeographicPoint start;
    private GeographicPoint end;
    private String name;
    private String type;
    private double length;

    public MapEdge() {
    }

    public MapEdge(GeographicPoint end1, GeographicPoint end2, String roadName,
                   String roadType, double length) {
        this.start = end1;
        this.end = end2;
        this.name = roadName;
        this.type = roadType;
        this.length = length;
    }

    public GeographicPoint getStartPoint() {
        return this.start;
    }

    public GeographicPoint getEndPoint() {
        return this.end;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public double getLength() {
        return this.length;
    }

    public String toString() {
        String s = "MapEdge Attributes: \n" ;
        s += "\tstartPoint: " + getStartPoint() + ",\n";
        s += "\tendPoint: " + getEndPoint() + ",\n";
        s += "\tStreetName: " + getName() + ",\n";
        s += "\tStreetType: " + getType() + ",\n";
        s += "\tlength: " + getLength();
        s += "\n";

        return s;
    }
}