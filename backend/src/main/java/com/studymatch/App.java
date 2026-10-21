package com.studymatch;

public class App {
    public static void main(String[] args) {
        Course calculus = new Course("Calculus 1", 1, "Information Engineering");
        Level myLevel = Level.NEED_HELP;

        System.out.println(calculus);
        System.out.println("My level: " + myLevel);
    }
}