package com.studymatch;

public class ProfileFactory {
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_SLOTS = 7 * (Availability.LAST_HOUR - Availability.FIRST_HOUR);

    public static Student build(ProfileRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Empty request");
        }

        String name = request.name() == null ? "" : request.name().trim();
        if (name.isEmpty() || name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Name must be 1 to " + MAX_NAME_LENGTH + " characters");
        }
        if (request.courses() == null || request.courses().isEmpty()) {
            throw new IllegalArgumentException("Pick at least one course");
        }
        if (request.courses().size() > CourseCatalog.ALL.size()) {
            throw new IllegalArgumentException("Too many courses");
        }
        if (request.availability() == null || request.availability().isEmpty()) {
            throw new IllegalArgumentException("Add at least one free hour");
        }
        if (request.availability().size() > MAX_SLOTS) {
            throw new IllegalArgumentException("Too many free hours");
        }

        Student student = new Student(name);

        for (ProfileRequest.CourseEntry entry : request.courses()) {
            if (entry == null || entry.course() == null || entry.level() == null) {
                throw new IllegalArgumentException("Each course needs a name and a level");
            }
            Course course = CourseCatalog.find(entry.course());
            student.setLevel(course, parse(Level.class, entry.level(), "level"));
            if (entry.session() != null) {
                student.setExamSession(course, parse(ExamSession.class, entry.session(), "exam session"));
            }
        }

        for (ProfileRequest.SlotEntry slot : request.availability()) {
            if (slot == null) {
                throw new IllegalArgumentException("Empty time slot");
            }
            student.getAvailability().add(slot.day(), slot.hour());
        }

        if (request.style() != null) {
            student.setStyle(parse(StudyStyle.class, request.style(), "study style"));
        }
        if (request.goal() != null) {
            student.setGoal(parse(StudyGoal.class, request.goal(), "study goal"));
        }
        return student;
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String text, String label) {
        try {
            return Enum.valueOf(type, text);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown " + label + ": " + text);
        }
    }
}