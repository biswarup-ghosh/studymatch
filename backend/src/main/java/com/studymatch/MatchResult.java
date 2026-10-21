package com.studymatch;

public class MatchResult implements Comparable<MatchResult> {
    private final Student student;
    private final double score;
    private final int sharedHours;

    public MatchResult(Student student, double score, int sharedHours) {
        this.student = student;
        this.score = score;
        this.sharedHours = sharedHours;
    }

    public Student getStudent() {
        return student;
    }

    public double getScore() {
        return score;
    }

    public int getSharedHours() {
        return sharedHours;
    }

    @Override
    public int compareTo(MatchResult other) {
        return Double.compare(this.score, other.score);
    }

    @Override
    public String toString() {
        return student.getName() + " (score " + score + ", " + sharedHours + " shared hours)";
    }
}