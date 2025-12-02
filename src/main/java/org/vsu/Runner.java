package org.vsu;

import java.util.List;

public class Runner {
    public static void main(String[] args) {
        System.out.println("Starting benchmark ArrayList vs LinkedList...");
        System.out.println("Parameters: LIST_SIZE=" + 10_000 + ", ITERATIONS=" + 5_000);
        System.out.println();

        PerformanceBenchmark benchmark = new PerformanceBenchmark();
        List<PerformanceBenchmark.Result> results = benchmark.runBenchmark();
        benchmark.printResults(results);
    }
}
