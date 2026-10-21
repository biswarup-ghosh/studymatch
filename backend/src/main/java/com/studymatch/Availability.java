package com.studymatch;

import java.util.BitSet;

public class Availability {
    public static final int FIRST_HOUR = 8;
    public static final int LAST_HOUR = 22;
    private static final int HOURS_PER_DAY = LAST_HOUR - FIRST_HOUR;
    private static final int TOTAL_SLOTS = 7 * HOURS_PER_DAY;

    private final BitSet slots = new BitSet(TOTAL_SLOTS);

    private int index(int day, int hour) {
        if (day < 0 || day > 6) {
            throw new IllegalArgumentException("Day must be 0 to 6 (0 = Monday): " + day);
        }
        if (hour < FIRST_HOUR || hour >= LAST_HOUR) {
            throw new IllegalArgumentException("Hour must be " + FIRST_HOUR + " to " + (LAST_HOUR - 1) + ": " + hour);
        }
        return day * HOURS_PER_DAY + (hour - FIRST_HOUR);
    }

    public void add(int day, int hour) {
        slots.set(index(day, hour));
    }

    public boolean isFree(int day, int hour) {
        return slots.get(index(day, hour));
    }

    public int sharedHours(Availability other) {
        BitSet common = (BitSet) slots.clone();
        common.and(other.slots);
        return common.cardinality();
    }
}