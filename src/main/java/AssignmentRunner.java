import java.util.List;

public class AssignmentRunner {
    public static void main(String[] args) {
        GraphAlgorithms algo = new GraphAlgorithms();

        // --- Scene 1: Normal Course Selection Process (DAG) ---
        Graph<String> schedule = new Graph<>(true);
        schedule.addEdge("Math101", "Algorithm");
        schedule.addEdge("JavaBasic", "Algorithm");
        schedule.addEdge("Algorithm", "AdvancedProject");

        System.out.println("=== Scenario 1: Normal Course Selection ===");
        List<String> result = algo.kahnTopologicalSort(schedule);
        if (result != null) {
            System.out.println("Recommended learning order: " + result);
        }

        // --- Scene 2: Infinite Loop Detection (Cyclic Graph) ---
        Graph<String> deadLock = new Graph<>(true);
        deadLock.addEdge("CourseA", "CourseB");
        deadLock.addEdge("CourseB", "CourseC");
        deadLock.addEdge("CourseC", "CourseA");

        System.out.println("\n=== Scene 2: Circular Dependency Detection ===");
        boolean hasCycle = algo.hasCycleDFS(deadLock);
        System.out.println("DFS checks whether there is a dead loop:" + hasCycle);

        List<String> kahnResult = algo.kahnTopologicalSort(deadLock);
        if (kahnResult == null) {
            System.out.println("Kahn algorithm conclusion: A cycle was detected, topological sorting cannot be generated.");
        }
    }
}