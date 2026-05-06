import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ExperimentRunner {
    private static final GraphAlgorithms algo = new GraphAlgorithms();

    public static void main(String[] args) {
        // -----------------------------------------------------------
        // Test one: Random data pressure test
        // -----------------------------------------------------------
        int[] sizes = {10000, 50000, 100000, 500000, 1000000};

        System.out.println("==================== Test one: Random data test ====================");
        System.out.printf("%-10s | %-8s | %-8s | %-8s | %-25s\n",
                "Scale(V)", "Kahn(ms)", "DFS(ms)", "Dij(ms)", "AuxMem(MB) [K|D|DJ]");
        System.out.println("-----------------------------------------------------------------------------");

        for (int size : sizes) {
            Graph<Integer> graph = generateRandomDAG(size, size * 3);

            int warmUps = (size <= 10000) ? 10 : 2;
            int measures = 3;

            long tKahn = measureTimeWithWarmup(() -> algo.kahnTopologicalSort(graph), warmUps, measures);
            long tDFS = measureTimeWithWarmup(() -> algo.hasCycleDFS(graph), warmUps, measures);
            long tDij = measureTimeWithWarmup(() -> algo.dijkstraShortestPath(graph, 0, -1), warmUps, measures);

            // 2. measure every algorithm memory
            double memK = measureMemorySpecific(() -> algo.kahnTopologicalSort(graph));
            double memD = measureMemorySpecific(() -> algo.hasCycleDFS(graph));
            double memDJ = measureMemorySpecific(() -> algo.dijkstraShortestPath(graph, 0, -1));

            System.out.printf("%-10d | %-8d | %-8d | %-8d | K:%.1f, D:%.1f, DJ:%.1f\n",
                    size, tKahn, tDFS, tDij, memK, memD, memDJ);
        }

        // -----------------------------------------------------------
        // Test2: Twitter real network test
        // -----------------------------------------------------------
        System.out.println("\n\n==================== Test2: Twitter real network test ====================");
        String twitterFilePath = "src/main/resources/twitter_combined.txt";
        Graph<Integer> twitterGraph = loadTwitterData(twitterFilePath);

        if (twitterGraph != null) {
            System.out.printf("%-10s | %-8s | %-8s | %-10s | %-10s | %-10s\n",
                    "Dataset", "Kahn(ms)", "DFS(ms)", "Full-Dij(MB)", "Real-Dij(MB)", "AuxMem(MB)[K|D|DJF|DJR]");
            System.out.println("--------------------------------------------------------------------------------------");

            int warmUps = 1;
            int measures = 2;

            long tKahn = measureTimeWithWarmup(() -> algo.kahnTopologicalSort(twitterGraph), warmUps, measures);
            long tDFS = measureTimeWithWarmup(() -> algo.hasCycleDFS(twitterGraph), warmUps, measures);

            // fake data test pressure
            Integer start = twitterGraph.getAllVertices().iterator().next();
            long tDijFull = measureTimeWithWarmup(() -> algo.dijkstraShortestPath(twitterGraph, start, -1), warmUps, measures);

            // real data test real
            long tDijReal = measureTimeWithWarmup(() -> algo.dijkstraShortestPath(twitterGraph, 21432, 21435), warmUps, measures);
            double memK = measureMemorySpecific(() -> algo.kahnTopologicalSort(twitterGraph));
            double memD = measureMemorySpecific(() -> algo.hasCycleDFS(twitterGraph));
            double memDijFull = measureMemorySpecific(() -> algo.dijkstraShortestPath(twitterGraph, start, -1));
            double memDijReal = measureMemorySpecific(() -> algo.dijkstraShortestPath(twitterGraph, 21432, 21435));

            System.out.printf("%-10s | %-8d | %-8d | %-10d | %-10d | K:%.1f, D:%.1f, DF:%.1f, DR:%.1f\n",
                    "Twitter", tKahn, tDFS, tDijFull, tDijReal, memK, memD, memDijFull, memDijReal);
            System.out.println("======================================================================================");
        }

        // -----------------------------------------------------------
        // Test3: Demonstrate the capability of Kahn's algorithm
        // -----------------------------------------------------------
        System.out.println("\n\n==================== Test3: Demonstrate the capability of Kahn's algorithm ====================");
        Graph<Integer> demoGraph = generateRandomDAG(10, 15);
        List<Integer> result = algo.kahnTopologicalSort(demoGraph);

        if (result != null) {
            System.out.println("Successfully generated a valid linear execution sequence:");
            System.out.println(result.toString());
            System.out.println("Physical significance: This sequence ensures that all prerequisite tasks are completed before subsequent tasks.");
        }


    }

    private static Graph<Integer> generateRandomDAG(int vCount, int eCount) {
        Graph<Integer> graph = new Graph<>(true);
        Random rand = new Random();

        for (int i = 0; i < vCount; i++) {
            graph.addVertex(i);
        }

        int edgesAdded = 0;
        while (edgesAdded < eCount) {
            int u = rand.nextInt(vCount);
            int v = rand.nextInt(vCount);

            if (u < v) {
                graph.addEdge(u, v);
                edgesAdded++;
            }
        }
        return graph;
    }

    private static double measureMemorySpecific(Runnable task) {
        forceGC();
        long startMem = getUsedMemory();
        task.run();
        long endMem = getUsedMemory();
        return Math.max(0, (endMem - startMem) / (1024.0 * 1024.0));
    }


    private static void forceGC() {
        for (int i = 0; i < 3; i++) {
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }


    private static long measureTimeWithWarmup(Runnable task, int warmUpRuns, int measureRuns) {
        for (int i = 0; i < warmUpRuns; i++) {
            task.run();
        }

        long totalTimeNs = 0;
        for (int i = 0; i < measureRuns; i++) {
            long start = System.nanoTime();
            task.run();
            long end = System.nanoTime();
            totalTimeNs += (end - start);
        }

        return (totalTimeNs / measureRuns) / 1_000_000;
    }

    private static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }


    public static Graph<Integer> loadTwitterData(String filePath) {
        System.out.println("================================================================");
        System.out.println("Start loading Twitter true dataset，This may take a few seconds...");
        long startTime = System.currentTimeMillis();

        // The Twitter following network is a directed graph, so pass in true
        Graph<Integer> graph = new Graph<>(true);
        int edgeCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    int source = Integer.parseInt(parts[0]);
                    int target = Integer.parseInt(parts[1]);

                    graph.addEdge(source, target);
                    edgeCount++;
                }
            }
        } catch (IOException e) {
            System.err.println("Wrong: " + e.getMessage());
            return null;
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Twitter data load finish!");
        System.out.println("   - V - : " + graph.getAllVertices().size());
        System.out.println("   - E - : " + edgeCount);
        System.out.println("   - TIME -     : " + (endTime - startTime) + " ms");
        System.out.println("================================================================\n");

        return graph;
    }
}