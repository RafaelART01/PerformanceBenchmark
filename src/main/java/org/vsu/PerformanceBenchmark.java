package org.vsu;

import java.util.*;

public class PerformanceBenchmark {
    private static final int LIST_SIZE = 10_000;
    private static final int ITERATIONS = 5_000;
    private static final int WARMUP_ROUNDS = 3;

    public static class Result {
        public final String listType;
        public final String operation;
        public final int iterations;
        public final long nanos;

        public Result(String listType, String operation, int iterations, long nanos) {
            this.listType = listType;
            this.operation = operation;
            this.iterations = iterations;
            this.nanos = nanos;
        }
    }

    public List<Result> runBenchmark() {
        List<Result> results = new ArrayList<>();

        results.add(benchmarkAddLast("ArrayList"));
        results.add(benchmarkAddFirst("ArrayList"));
        results.add(benchmarkGet("ArrayList"));
        results.add(benchmarkIndexOf("ArrayList"));
        results.add(benchmarkRemoveByValue("ArrayList"));
        results.add(benchmarkDeleteLast("ArrayList"));

        results.add(benchmarkAddLast("LinkedList"));
        results.add(benchmarkAddFirst("LinkedList"));
        results.add(benchmarkGet("LinkedList"));
        results.add(benchmarkIndexOf("LinkedList"));
        results.add(benchmarkRemoveByValue("LinkedList"));
        results.add(benchmarkDeleteLast("LinkedList"));

        return results;
    }

    public List<Integer> createPopulatedList(String type) {
        List<Integer> list = "ArrayList".equals(type)
                ? new ArrayList<Integer>()
                : new LinkedList<Integer>();
        for (int i = 0; i < LIST_SIZE; i++) {
            list.add(i);
        }
        return list;
    }

    public List<Integer> createEmptyList(String type) {
        return "ArrayList".equals(type)
                ? new ArrayList<Integer>()
                : new LinkedList<Integer>();
    }

    public Result benchmarkAddLast(String listType) {
        List<Integer> list = createEmptyList(listType);
        warmup(() -> list.add(0));
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            list.add(i);
        }
        long end = System.nanoTime();
        return new Result(listType, "addLast", ITERATIONS, end - start);
    }

    public Result benchmarkAddFirst(String listType) {
        List<Integer> list = createEmptyList(listType);
        warmup(() -> list.addFirst(0));
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            list.addFirst(i);
        }
        long end = System.nanoTime();
        return new Result(listType, "addFirst", ITERATIONS, end - start);
    }

    public Result benchmarkGet(String listType) {
        List<Integer> list = createPopulatedList(listType);
        int size = list.size();
        warmup(() -> list.getFirst());
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            list.get(i % size);
        }
        long end = System.nanoTime();
        return new Result(listType, "get", ITERATIONS, end - start);
    }

    public Result benchmarkIndexOf(String listType) {
        List<Integer> list = createPopulatedList(listType);
        int[] targets = new int[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            targets[i] = LIST_SIZE / 2 + (i % (LIST_SIZE / 2));
        }
        warmup(() -> list.indexOf(LIST_SIZE / 2));
        long start = System.nanoTime();
        for (int target : targets) {
            list.indexOf(target);
        }
        long end = System.nanoTime();
        return new Result(listType, "indexOf", ITERATIONS, end - start);
    }

    public Result benchmarkRemoveByValue(String listType) {
        List<Integer> list = createPopulatedList(listType);
        List<Integer> toRemove = new ArrayList<>();
        for (int i = 0; i < ITERATIONS && i < LIST_SIZE; i++) {
            toRemove.add(i);
        }
        warmup(() -> list.remove(Integer.valueOf(0)));

        long start = System.nanoTime();
        for (Integer val : toRemove) {
            list.remove(val);
        }
        long end = System.nanoTime();
        return new Result(listType, "remove(value)", toRemove.size(), end - start);
    }

    public Result benchmarkDeleteLast(String listType) {
        List<Integer> list = createPopulatedList(listType);
        warmup(() -> {
            if (!list.isEmpty()) list.removeLast();
        });
        long start = System.nanoTime();
        int removed = 0;
        for (int i = 0; i < ITERATIONS && !list.isEmpty(); i++) {
            list.removeLast();
            removed++;
        }
        long end = System.nanoTime();
        return new Result(listType, "deleteLast", removed, end - start);
    }

    private void warmup(Runnable op) {
        for (int r = 0; r < WARMUP_ROUNDS; r++) {
            for (int i = 0; i < 100; i++) {
                op.run();
            }
        }
    }

    public void printResults(List<Result> results) {
        Map<String, Map<String, Result>> grouped = new LinkedHashMap<>();
        for (Result r : results) {
            grouped.computeIfAbsent(r.operation, k -> new HashMap<>())
                    .put(r.listType, r);
        }

        System.out.println("## Check performance time: ArrayList vs LinkedList");
        System.out.println();
        System.out.printf("%-12s | %-14s | %-15s | %s%n",
                "Operation", "ArrayList, ms", "LinkedList, ms", "Speedup");
        System.out.println("-------------|----------------|-----------------|----------------------------");

        for (Map.Entry<String, Map<String, Result>> entry : grouped.entrySet()) {
            String op = entry.getKey();
            Map<String, Result> byType = entry.getValue();

            Result alRes = byType.get("ArrayList");
            Result llRes = byType.get("LinkedList");

            if (alRes == null || llRes == null) {
                System.out.printf("%-12s | %-14s | %-15s | %s%n", op, "—", "—", "no data");
                continue;
            }

            double alMs = alRes.nanos / 1_000_000.0;
            double llMs = llRes.nanos / 1_000_000.0;

            String winner;
            if (alMs < llMs && llMs > 0) {
                double ratio = llMs / alMs;
                winner = String.format("ArrayList (x%.1f)", ratio);
            } else if (llMs < alMs && alMs > 0) {
                double ratio = alMs / llMs;
                winner = String.format("LinkedList (x%.1f)", ratio);
            } else {
                winner = "equal";
            }

            System.out.printf("%-12s | %-14.2f | %-15.2f | %s%n",
                    op, alMs, llMs, winner);
        }
    }
}
