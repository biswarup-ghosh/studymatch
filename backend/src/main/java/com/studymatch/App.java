package com.studymatch;
import java.util.List;

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

        me.getAvailability().add(0, 19);
        me.getAvailability().add(0, 20);
        me.getAvailability().add(1, 10);

        Student anna = new Student("Anna");
        anna.setLevel(calculus, Level.STRONG);
        anna.setLevel(dsa, Level.NEED_HELP);
        anna.getAvailability().add(0, 19);
        anna.getAvailability().add(0, 20);

        Student luca = new Student("Luca");
        luca.setLevel(calculus, Level.NEED_HELP);
        luca.getAvailability().add(0, 19);

        Student sara = new Student("Sara");
        sara.setLevel(CourseCatalog.find("Physics 1"), Level.STRONG);
        sara.getAvailability().add(0, 19);

        List<Student> everyone = List.of(me, anna, luca, sara);
        List<MatchResult> top = MatchFinder.findTopMatches(me, everyone, 3);

        for (MatchResult result : top) {
            System.out.println(result);
        }
    }
}