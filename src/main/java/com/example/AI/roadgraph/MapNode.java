
package com.example.AI.roadgraph;

import com.example.AI.geography.GeographicPoint;

import java.util.ArrayList;
import java.util.List;

public class MapNode implements Comparable<MapNode>{

    // -----------------------------------------------------
    //			Member Variables
    // -----------------------------------------------------
    private com.example.AI.geography.GeographicPoint location;
    private List<MapEdge> roadList;
    private List<MapNode> neighbors;
    private Double distance;
    private Double heuristicCost;
    private boolean astarFlag;

    public MapNode() {
        this.location = null;
        this.roadList = new ArrayList<MapEdge>();
        this.neighbors = new ArrayList<MapNode>();
        this.distance = null;
        this.heuristicCost = null;
        this.astarFlag = false;
    }

    public MapNode(com.example.AI.geography.GeographicPoint loc) {
        this.location = loc;
        this.roadList = new ArrayList<MapEdge>();
        this.neighbors = new ArrayList<MapNode>();
        this.distance = null;
        this.heuristicCost = null;
        this.astarFlag = false;
    }

    public GeographicPoint getLocation() {
        return this.location;
    }

    public List<MapEdge> getRoadList() {
        return this.roadList;
    }

    public List<MapNode> getNeighbors() {
        return this.neighbors;
    }

    public Double getDistance() {
        return this.distance;
    }

    public void setDistance(Double DistVal) {
        this.distance = DistVal;
    }

    public Double getHCost() {
        return this.heuristicCost;
    }

    public void setHCost(MapNode other) {
        this.heuristicCost = this.location.distance(other.getLocation());
    }

    public void setAstar() {
        this.astarFlag = true;
    }

    public String toString() {
        String s = "location of MapNode: " + this.location;
        s += "\nRoad List: \n";
        for (MapEdge e: this.roadList) {
            s += e;
        }

        return s;
    }

    @Override
    public int compareTo(MapNode other) {
        if (this.astarFlag) {
            Double this_cost = this.getHCost() + this.getDistance();
            Double other_cost = other.getHCost() + other.getDistance();

            return this_cost.compareTo(other_cost);
        }
        return this.getDistance().compareTo(other.getDistance());
    }

}