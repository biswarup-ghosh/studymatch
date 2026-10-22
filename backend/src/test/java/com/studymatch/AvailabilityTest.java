package com.studymatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AvailabilityTest {

    @Test
    void sharedHoursCountsOnlyCommonHours() {
        Availability a = new Availability();
        a.add(0, 19);
        a.add(0, 20);
        a.add(1, 10);

        Availability b = new Availability();
        b.add(0, 19);
        b.add(0, 20);
        b.add(0, 21);
        b.add(1, 10);

        assertEquals(3, a.sharedHours(b));
    }

    @Test
    void scatteredSharedHoursGiveRunOfOne() {
        Availability a = new Availability();
        Availability b = new Availability();
        int[][] slots = {{0, 10}, {2, 12}, {4, 9}};
        for (int[] slot : slots) {
            a.add(slot[0], slot[1]);
            b.add(slot[0], slot[1]);
        }

        assertEquals(3, a.sharedHours(b));
        assertEquals(1, a.longestSharedRun(b));
    }

    @Test
    void consecutiveSharedHoursGiveLongerRun() {
        Availability a = new Availability();
        Availability b = new Availability();
        for (int hour = 10; hour <= 12; hour++) {
            a.add(0, hour);
            b.add(0, hour);
        }

        assertEquals(3, a.sharedHours(b));
        assertEquals(3, a.longestSharedRun(b));
    }

    @Test
    void runDoesNotWrapBetweenDays() {
        Availability a = new Availability();
        Availability b = new Availability();
        a.add(0, 21);
        a.add(1, 8);
        b.add(0, 21);
        b.add(1, 8);

        assertEquals(1, a.longestSharedRun(b));
    }

    @Test
    void hoursOutsideTheRangeAreRejected() {
        Availability a = new Availability();

        assertThrows(IllegalArgumentException.class, () -> a.add(0, 7));
        assertThrows(IllegalArgumentException.class, () -> a.add(0, 22));
        assertThrows(IllegalArgumentException.class, () -> a.add(7, 10));
    }
}