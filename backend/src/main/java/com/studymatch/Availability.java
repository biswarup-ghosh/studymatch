package com.studymatch;

public class Availability {
    public static final int FIRST_HOUR = 8;
    public static final int LAST_HOUR = 22;

    private final long[] days = new long[7];

    private void check(int day, int hour) {
        if (day < 0 || day > 6) {
            throw new IllegalArgumentException("Day must be 0 to 6 (0 = Monday): " + day);
        }
        if (hour < FIRST_HOUR || hour >= LAST_HOUR) {
            throw new IllegalArgumentException("Hour must be " + FIRST_HOUR + " to " + (LAST_HOUR - 1) + ": " + hour);
        }
    }

    public void add(int day, int hour) {
        check(day, hour);
        days[day] |= 1L << (hour - FIRST_HOUR);
    }

    public boolean isFree(int day, int hour) {
        check(day, hour);
        return (days[day] & (1L << (hour - FIRST_HOUR))) != 0;
    }

    public int sharedHours(Availability other) {
        int total = 0;
        for (int day = 0; day < 7; day++) {
            total += Long.bitCount(days[day] & other.days[day]);
        }
        return total;
    }

    public int longestSharedRun(Availability other) {
        int longest = 0;
        for (int day = 0; day < 7; day++) {
            long common = days[day] & other.days[day];
            int length = 0;
            while (common != 0) {
                common &= common << 1;
                length++;
            }
            if (length > longest) {
                longest = length;
            }
        }
        return longest;
    }
}