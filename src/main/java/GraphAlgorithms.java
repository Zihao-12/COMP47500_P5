import java.util.*;

/**
 * Graph Theory Algorithm Utility Class
 * Includes: Kahn topological sort, DFS cycle detection, Dijkstra shortest path
 */
public class GraphAlgorithms {

    public <T> List<T> kahnTopologicalSort(Graph<T> graph) {
        Map<T, Integer> inDegree = new HashMap<>();
        Set<T> allVertices = graph.getAllVertices();

        for (T u : allVertices) {
            inDegree.putIfAbsent(u, 0);
            for (T v : graph.getNeighbors(u)) {
                inDegree.put(v, inDegree.getOrDefault(v, 0) + 1);
            }
        }

        Queue<T> queue = new LinkedList<>();
        for (T u : allVertices) {
            if (inDegree.get(u) == 0) {
                queue.add(u);
            }
        }

        List<T> topoOrder = new ArrayList<>();
        while (!queue.isEmpty()) {
            T u = queue.poll();
            topoOrder.add(u);

            for (T v : graph.getNeighbors(u)) {
                inDegree.put(v, inDegree.get(v) - 1);
                if (inDegree.get(v) == 0) {
                    queue.add(v);
                }
            }
        }

        if (topoOrder.size() != allVertices.size()) return null;
        return topoOrder;
    }

    public <T> boolean hasCycleDFS(Graph<T> graph) {
        Map<T, Integer> status = new HashMap<>();
        for (T vertex : graph.getAllVertices()) status.put(vertex, 0);

        for (T vertex : graph.getAllVertices()) {
            if (status.get(vertex) == 0) {
                if (dfsCheck(vertex, graph, status)) return true;
            }
        }
        return false;
    }

    private <T> boolean dfsCheck(T u, Graph<T> graph, Map<T, Integer> status) {
        status.put(u, 1);
        for (T v : graph.getNeighbors(u)) {
            int vStatus = status.getOrDefault(v, 0);
            if (vStatus == 1) return true;
            if (vStatus == 0 && dfsCheck(v, graph, status)) return true;
        }
        status.put(u, 2);
        return false;
    }

    private static class NodeDistance<T> implements Comparable<NodeDistance<T>> {
        T node;
        int distance;

        public NodeDistance(T node, int distance) {
            this.node = node;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodeDistance<T> other) {
            return Integer.compare(this.distance, other.distance); // 最小堆
        }
    }

    public <T> List<T> dijkstraShortestPath(Graph<T> graph, T startNode, T targetNode) {
        // 1. Distance table: records the shortest distance from the starting point to each node
        Map<T, Integer> distances = new HashMap<>();
        // 2. Path backtracking table: records from which predecessor node each node was reached
        Map<T, T> previousNodes = new HashMap<>();
        // 3. Priority queue (min-heap): automatically selects the node closest to the starting point each time
        PriorityQueue<NodeDistance<T>> minHeap = new PriorityQueue<>();
        // 4. Visit Tag Set: Prevent Backtracking
        Set<T> visited = new HashSet<>();

        for (T vertex : graph.getAllVertices()) {
            distances.put(vertex, Integer.MAX_VALUE);
        }
        distances.put(startNode, 0);
        minHeap.add(new NodeDistance<>(startNode, 0));

        while (!minHeap.isEmpty()) {
            NodeDistance<T> current = minHeap.poll();
            T u = current.node;

            if (visited.contains(u)) continue;
            visited.add(u);

            if (u.equals(targetNode)) break;

            for (T v : graph.getNeighbors(u)) {
                if (visited.contains(v)) continue;

                int edgeWeight = 1;
                int newDist = distances.get(u) + edgeWeight;

                if (newDist < distances.getOrDefault(v, Integer.MAX_VALUE)) {
                    distances.put(v, newDist);
                    previousNodes.put(v, u);
                    minHeap.add(new NodeDistance<>(v, newDist));
                }
            }
        }

        return buildPath(previousNodes, startNode, targetNode);
    }

    private <T> List<T> buildPath(Map<T, T> previousNodes, T startNode, T targetNode) {
        List<T> path = new ArrayList<>();
        T step = targetNode;

        if (previousNodes.get(step) == null && !step.equals(startNode)) {
            return path;
        }

        path.add(step);
        while (previousNodes.get(step) != null) {
            step = previousNodes.get(step);
            path.add(step);
        }

        Collections.reverse(path);
        return path;
    }
}