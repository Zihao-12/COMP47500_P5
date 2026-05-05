import java.util.*;

public class Graph<T> {
    private final Map<T, List<T>> adjacencyMap;
    private final boolean isDirected;

    public Graph(boolean isDirected) {
        this.adjacencyMap = new HashMap<>();
        this.isDirected = isDirected;
    }

    public void addVertex(T vertex) {
        adjacencyMap.putIfAbsent(vertex, new ArrayList<>());
    }

    public void addEdge(T source, T destination) {
        addVertex(source);
        addVertex(destination);

        adjacencyMap.get(source).add(destination);

        if (!isDirected) {
            adjacencyMap.get(destination).add(source);
        }
    }

    public List<T> getNeighbors(T vertex) {
        return adjacencyMap.getOrDefault(vertex, new ArrayList<>());
    }

    public Set<T> getAllVertices() {
        return adjacencyMap.keySet();
    }

    public void printGraph() {
        for (T v : adjacencyMap.keySet()) {
            System.out.print(v + " -> ");
            System.out.println(adjacencyMap.get(v));
        }
    }
}