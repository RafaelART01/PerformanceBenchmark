package org.vsu;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


public class PerformanceBenchmarkTests {
    private final PerformanceBenchmark benchmark = new PerformanceBenchmark();

    @Test
    void testAddFirst() {
        List<Integer> al = new ArrayList<>();
        al.addFirst(100);
        al.addFirst(200);
        assertEquals(List.of(200, 100), al);

        List<Integer> ll = new LinkedList<>();
        ll.addFirst( 100);
        ll.addFirst(200);
        assertEquals(List.of(200, 100), ll);
    }

    @Test
    void testIndexOf() {
        List<Integer> list = benchmark.createPopulatedList("ArrayList");
        assertEquals(5000, list.indexOf(5000));
        assertEquals(-1, list.indexOf(-1));
    }

    @Test
    void testRemoveByValue() {
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 2, 4));
        assertTrue(list.remove(Integer.valueOf(2)));
        assertEquals(List.of(1, 3, 2, 4), list);
        assertTrue(list.remove(Integer.valueOf(2)));
        assertEquals(List.of(1, 3, 4), list);
    }

    @Test
    void benchmarkMethodsDoNotThrow() {
        assertDoesNotThrow(() -> benchmark.benchmarkAddFirst("ArrayList"));
        assertDoesNotThrow(() -> benchmark.benchmarkAddFirst("LinkedList"));
        assertDoesNotThrow(() -> benchmark.benchmarkIndexOf("ArrayList"));
        assertDoesNotThrow(() -> benchmark.benchmarkRemoveByValue("LinkedList"));
    }

    /**
     * Гипотеза: get у ArrayList должен быть как минимум в 10 раз быстрее, чем у LinkedList.
     */
    @Test
    void benchmark_get_Test() {
        // Запускаем мини-бенчмарк (меньше итераций для скорости теста)
        long alTime = timeOperation(() -> {
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < 5_000; i++) list.add(i);
            for (int i = 0; i < 2_000; i++) list.get(i % list.size());
        });

        long llTime = timeOperation(() -> {
            List<Integer> list = new LinkedList<>();
            for (int i = 0; i < 5_000; i++) list.add(i);
            for (int i = 0; i < 2_000; i++) list.get(i % list.size());
        });

        System.out.printf("[BENCH] get: ArrayList=%.2f мс, LinkedList=%.2f мс%n",
                alTime / 1_000_000.0, llTime / 1_000_000.0);

        // Проверяем гипотезу: ArrayList должен быть хотя бы в 10× быстрее
        assertTrue(
                alTime * 10 < llTime,
                () -> String.format(
                        "Awaited: ArrayList в >= 10 times faster than get. Fact: ArrayList=%.2f ms, LinkedList=%.2f ms (x%.1f)",
                        alTime / 1_000_000.0, llTime / 1_000_000.0, (double) llTime / alTime)
        );
    }

    /**
     * Гипотеза: addFirst у LinkedList должен быть как минимум в 2 раза быстрее, чем у ArrayList.
     */
    @Test
    void benchmark_addFirst_Test() {
        long alTime = timeOperation(() -> {
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < 2_000; i++) list.addFirst(i);
        });

        long llTime = timeOperation(() -> {
            List<Integer> list = new LinkedList<>();
            for (int i = 0; i < 2_000; i++) list.addFirst(i);
        });

        System.out.printf("[BENCH] addFirst: ArrayList=%.2f ms, LinkedList=%.2f ms%n",
                alTime / 1_000_000.0, llTime / 1_000_000.0);

        assertTrue(
                llTime * 2 < alTime,
                () -> String.format(
                        "Awaited: LinkedList в >= 2 times faster addFirst. Fact: ArrayList=%.2f ms, LinkedList=%.2f ms (x%.1f)",
                        alTime / 1_000_000.0, llTime / 1_000_000.0, (double) alTime / llTime)
        );
    }

    /**
     * Гипотеза: remove(value) у LinkedList должен быть как минимум в 1.5 раза быстрее.
     */
    @Test
    void benchmark_removeByValue_Test() {
        // Готовим списки одинакового содержания
        List<Integer> al = new ArrayList<>();
        List<Integer> ll = new LinkedList<>();
        for (int i = 0; i < 3_000; i++) {
            al.add(i);
            ll.add(i);
        }

        // Копируем значения для удаления (чтобы не менять исходные)
        List<Integer> toRemove = new ArrayList<>();
        for (int i = 0; i < 1_000; i++) toRemove.add(i); // удаляем первые 1000

        long alTime = timeOperation(() -> {
            List<Integer> copy = new ArrayList<>(al);
            for (Integer v : toRemove) copy.remove(v);
        });

        long llTime = timeOperation(() -> {
            List<Integer> copy = new LinkedList<>(ll);
            for (Integer v : toRemove) copy.remove(v);
        });

        System.out.printf("[BENCH] remove(value): ArrayList=%.2f ms, LinkedList=%.2f ms%n",
                alTime / 1_000_000.0, llTime / 1_000_000.0);

        assertTrue(
                llTime * 3 < alTime,
                () -> String.format(
                        "Awaited: LinkedList в >= 3 times faster than remove(value). Fact: ArrayList=%.2f ms, LinkedList=%.2f ms (x%.1f)",
                        alTime / 1_000_000.0, llTime / 1_000_000.0, (double) alTime / llTime)
        );
    }

    /**
     * Замеряет время выполнения операции с прогревом (3 раунда по 100 итераций).
     * Возвращает время в наносекундах.
     */
    private long timeOperation(Runnable op) {
        for (int r = 0; r < 3; r++) {
            for (int i = 0; i < 100; i++) {
                op.run();
            }
        }
        // Основной замер
        long start = System.nanoTime();
        op.run();
        return System.nanoTime() - start;
    }
}
