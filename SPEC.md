# StudyMatch: Version 1 spec

## Who is it for?
University of Padua students in Information Engineering. Launch with a small
set of large, shared courses (mostly first and second year), since many
students take the same ones. The data model stores a department for every
course, so other departments can be added later.

## What problem does it solve?
Students who want to study with someone have no easy way to find a partner who
takes the same course, is free at the same time, and studies in a similar way.

## What does a student enter?
- Sign-up with a university email.
- Courses they take, with a level for each: need help / okay / strong.
- Weekly availability in 1-hour blocks.
- 3 short habit questions (time of day, quiet or discussion, goal).
- Exam date per course, where known.

## What does a student get?
A ranked list of the top 5 matches, each with a short reason
("same course, 6 shared hours per week").

## What does Version 1 NOT do?
No chat, no tutoring requests, no ratings, no groups, no photos, no payments,
no other departments, no mobile app.

## How will I know it worked?
20 real users with completed profiles, and at least 5 study sessions
arranged through the app.

## How we will judge match quality (later, from real use)
Signals to record, from strongest to weakest:
1. A study session confirmed by both students.
2. A match request accepted.
3. A match request sent (weak: may reflect looks or list position, not fit).

Also record which matches were shown, to correct for people clicking the
top of the list. No photos in version 1, to keep choices based on courses
and schedules. Weights are tuned only from signals 1 and 2.