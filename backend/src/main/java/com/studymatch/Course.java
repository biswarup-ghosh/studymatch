package com.studymatch;

public class Course {
    private final String name;
    private final int year;
    private final String department;

    public Course(String name, int year, String department) {
        this.name = name;
        this.year = year;
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public int getYear() {
        return year;
    }

    public String getDepartment() {
        return department;
    }

    @Override
    public String toString() {
        return name + " (year " + year + ", " + department + ")";
    }
}