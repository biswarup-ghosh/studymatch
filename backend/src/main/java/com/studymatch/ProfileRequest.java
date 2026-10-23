package com.studymatch;

import java.util.List;

public record ProfileRequest(
        String name,
        List<CourseEntry> courses,
        List<SlotEntry> availability,
        String style,
        String goal) {

    public record CourseEntry(String course, String level, String session) {
    }

    public record SlotEntry(int day, int hour) {
    }
}