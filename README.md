# Movie Ticket Booking System (Java)

A beginner-friendly console application that demonstrates 2D arrays, loops, methods, and input/condition handling.

## Features

- Displays a 5-row by 8-seat theatre map (`O` = available, `X` = booked).
- Books a selected seat and prevents double-booking.
- Cancels a booking and makes the seat available again.
- Automatically charges INR 150 on weekdays and INR 200 on Saturdays/Sundays, based on the computer's current date.
- Accepts coupon `MOVIE10` for 10% off a ticket.
- Collects movie ratings from 1 to 5 and displays the session average.

## Run

```bash
javac MovieTicketBookingSystem.java
java MovieTicketBookingSystem
```

Bookings and ratings are kept in memory for the duration of the program; they reset when the application exits. This sample simulates booking and does not process payments or refunds.
