package com.studymatch;

public class App {
    public static void main(String[] args) {
        Course calculus = CourseCatalog.find("Calculus 1");
        Course dsa = CourseCatalog.find("Data Structures and Algorithms");

        Student me = new Student("Biswarup");
        me.setLevel(dsa, Level.STRONG);
        me.setLevel(calculus, Level.NEED_HELP);

        System.out.println(me);
        System.out.println("DSA level: " + me.getLevel(dsa));
    }
}