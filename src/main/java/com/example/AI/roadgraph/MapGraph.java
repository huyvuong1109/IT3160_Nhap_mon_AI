
package com.example.AI.roadgraph;


import com.example.AI.geography.GeographicPoint;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

public class MapGraph {
	private Map<GeographicPoint, MapNode> intersections;
	private int numVertices;
	private int numEdges;

	public MapGraph()
	{
		intersections = new HashMap<GeographicPoint, MapNode>();
		numVertices = 0;
		numEdges = 0;
	}

	public int getNumVertices()
	{
		return numVertices;
	}

	public Set<GeographicPoint> getVertices()
	{

		return intersections.keySet();
	}
	public int getNumEdges()
	{
		return numEdges;
	}

	public boolean addVertex(GeographicPoint location)
	{

		if (location == null || intersections.containsKey(location)) {
			return false;
		}
		MapNode intersection = new MapNode(location);
		intersections.put(location, intersection);
		numVertices++;
		return true;
	}

	public void addEdge(GeographicPoint from, GeographicPoint to, String roadName,
						String roadType, double length) throws IllegalArgumentException {

		if (from == null || to == null || roadName == null || roadType == null || length < 0) {
			throw new IllegalArgumentException();
		}

		if (!intersections.containsKey(from) || !intersections.containsKey(to)) {
			throw new IllegalArgumentException();
		}
		MapEdge road = new MapEdge(from, to, roadName, roadType, length);

		intersections.get(from).getNeighbors().add(intersections.get(to));
		intersections.get(from).getRoadList().add(road);

		numEdges++;
	}

	public void printGraph() {
		for (GeographicPoint key: intersections.keySet()) {
			System.out.println("vert: " + key);
			for (MapEdge e: intersections.get(key).getRoadList()) {
				System.out.println(e);
			}
			System.out.println("\n\n");
		}
	}

	public List<GeographicPoint> bfs(GeographicPoint start, GeographicPoint goal) {
		// Dummy variable for calling the search algorithms
		Consumer<GeographicPoint> temp = (x) -> {};
		return bfs(start, goal, temp);
	}

	public List<GeographicPoint> bfs(GeographicPoint start,
									 GeographicPoint goal, Consumer<GeographicPoint> nodeSearched)
	{
		
	}
	private List<GeographicPoint> getPath(MapNode start, MapNode goal,
										  Map<MapNode, MapNode> parent, boolean pathFound) {

		if (pathFound == false) {
			System.out.println("There is no path found from " + start + " to " + goal + ".");
			return null;
		}

		List<GeographicPoint> path = new LinkedList<GeographicPoint>();
		MapNode currNode = goal;

		// backtrace a node from goal until you reach to start
		while (true) {
			if (currNode.toString().equals(start.toString())) {
				break;
			}
			MapNode prevNode = parent.get(currNode);
			path.add(currNode.getLocation());
			currNode = prevNode;
		}

		path.add(start.getLocation());
		// reverse order to return a List from start to goal
		Collections.reverse(path);

		System.out.println("Path: " + path);
		return path;
	}

	public List<GeographicPoint> dijkstra(GeographicPoint start, GeographicPoint goal) {
		// Dummy variable for calling the search algorithms
		// You do not need to change this method.
		Consumer<GeographicPoint> temp = (x) -> {};
		return dijkstra(start, goal, temp);
	}

	public List<GeographicPoint> dijkstra(GeographicPoint start,
										  GeographicPoint goal, Consumer<GeographicPoint> nodeSearched)
	{

	}
	public List<GeographicPoint> aStarSearch(GeographicPoint start, GeographicPoint goal) {
		// Dummy variable for calling the search algorithms
		Consumer<GeographicPoint> temp = (x) -> {};
		return aStarSearch(start, goal, temp);
	}

	public List<GeographicPoint> aStarSearch(GeographicPoint start,
											 GeographicPoint goal, Consumer<GeographicPoint> nodeSearched)
	{
		HashSet<MapNode> visited = new HashSet<MapNode>();
		PriorityQueue<MapNode> priorityQueue = new PriorityQueue<>();
		HashMap<MapNode, MapNode> parent = new HashMap<MapNode, MapNode>();

		Integer inf = Integer.MAX_VALUE;
		boolean pathFound = false;
		MapNode startNode = intersections.get(start);
		MapNode goalNode = intersections.get(goal);

		// Set all distances and Heuristic Costs to be inf for all intersections
		for (GeographicPoint location: intersections.keySet()) {
			//distances.put(intersections.get(location), inf.doubleValue());
			intersections.get(location).setDistance(inf.doubleValue());
			// Add HCost for each node from Goal & activate astarFlag to modify compareTo
			// function of MapNode such that priorityQueue entry will be based on HCost+GCost
			intersections.get(location).setHCost(goalNode);
			intersections.get(location).setAstar();
		}

		// Set distance of start node to 0 and enqueue it
		intersections.get(start).setDistance(0.0);
		priorityQueue.add(startNode);

		int count = 0;

		while (!priorityQueue.isEmpty()) {
			//dequeue node from front of queue
			MapNode currentNode = priorityQueue.poll();
			count++;
			// Hook for visualization.  See writeup.
			nodeSearched.accept(currentNode.getLocation());

			if (!visited.contains(currentNode)) {
				visited.add(currentNode);

				//System.out.println("Node-visited: " + currentNode.getLocation());
				if (currentNode.toString().equals(goalNode.toString())) {
					pathFound = true;
					break;
				}

				for (MapEdge road: currentNode.getRoadList()) {
					MapNode neighbor = intersections.get(road.getEndPoint());
					System.out.println("Road-type: " + road.getType());
					// Ensure visit only to non-visited nodes
					if (!visited.contains(neighbor)) {

						Double minDist = currentNode.getDistance() + road.getLength();

						if (minDist < neighbor.getDistance()) {
							// Update neighbor's distance
							neighbor.setDistance(minDist);
							parent.put(neighbor, currentNode);
							// enqueue neighbor in priorityQueue
							priorityQueue.add(neighbor);
						}

					}
				}
			}

		}

		System.out.println("Astar total No. of Node-visited: " + count);
		return getPath(startNode, goalNode, parent, pathFound);
	}
	
	public static void saveRouteEachPointPerLine(List<GeographicPoint> route, String filename) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
			for (GeographicPoint point : route) {
				writer.write(String.format("%.7f, %.7f\n", point.getX(), point.getY()));
			}
			System.out.println("Đã lưu từng điểm vào file: " + filename);
		} catch (IOException e) {
			System.out.println("Lỗi ghi file: " + e.getMessage());
		}
	}

}