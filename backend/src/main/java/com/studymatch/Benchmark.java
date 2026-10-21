package com.studymatch;

import java.util.List;
import java.util.Set;

public class Benchmark {
    public static void main(String[] args) {
        int[] sizes = {1000, 10000, 100000};
        int queries = 20;

        for (int size : sizes) {
            List<Student> students = FakeData.generate(size, 42);

            long buildStart = System.nanoTime();
            StudentIndex index = new StudentIndex(students);
            double buildMs = (System.nanoTime() - buildStart) / 1_000_000.0;

            Student first = students.get(0);
            MatchFinder.findTopMatches(first, students, 5);
            MatchFinder.findTopMatches(first, index.candidatesFor(first), 5);

            boolean same = true;
            long start = System.nanoTime();
            String[] naiveScores = new String[queries];
            for (int q = 0; q < queries; q++) {
                List<MatchResult> result = MatchFinder.findTopMatches(students.get(q), students, 5);
                naiveScores[q] = scores(result);
            }
            double naiveMs = (System.nanoTime() - start) / 1_000_000.0 / queries;

            long totalCandidates = 0;
            start = System.nanoTime();
            for (int q = 0; q < queries; q++) {
                Set<Student> candidates = index.candidatesFor(students.get(q));
                totalCandidates += candidates.size();
                List<MatchResult> result = MatchFinder.findTopMatches(students.get(q), candidates, 5);
                if (!scores(result).equals(naiveScores[q])) {
                    same = false;
                }
            }
            double indexedMs = (System.nanoTime() - start) / 1_000_000.0 / queries;

            System.out.println(size + " students:");
            System.out.println("  naive:   " + naiveMs + " ms per search");
            System.out.println("  indexed: " + indexedMs + " ms per search");
            System.out.println("  index build: " + buildMs + " ms (once)");
            System.out.println("  average candidates: " + (totalCandidates / queries));
            System.out.println("  same results: " + same);
        }
    }

    private static String scores(List<MatchResult> results) {
        StringBuilder text = new StringBuilder();
        for (MatchResult result : results) {
            text.append(result.getScore()).append(' ');
        }
        return text.toString();
    }
}