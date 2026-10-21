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

                Availability mine = new Availability();
        mine.add(0, 18);
        mine.add(0, 19);
        mine.add(0, 20);
        mine.add(1, 10);

        Availability theirs = new Availability();
        theirs.add(0, 19);
        theirs.add(0, 20);
        theirs.add(0, 21);
        theirs.add(1, 10);

        System.out.println("Shared hours: " + mine.sharedHours(theirs));
    }
}