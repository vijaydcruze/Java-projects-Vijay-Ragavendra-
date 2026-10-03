import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Scanner;

/**
 * A simple console-based movie ticket booking system.
 * Seats are stored in a 2D boolean array: false = available, true = booked.
 */
public class MovieTicketBookingSystem {
    private static final int ROWS = 5;
    private static final int SEATS_PER_ROW = 8;
    private static final double WEEKDAY_PRICE = 150.00;
    private static final double WEEKEND_PRICE = 200.00;
    private static final String COUPON_CODE = "MOVIE10";
    private static final double COUPON_DISCOUNT = 0.10;
    private static final int MAX_RATINGS = 1000;

    // A seat is true if it is booked and false if it is available.
    private static final boolean[][] seats = new boolean[ROWS][SEATS_PER_ROW];
    private static final int[] ratings = new int[MAX_RATINGS];
    private static int ratingCount = 0;
    private static final Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Movie Ticket Booking System ===");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ", 1, 6);

            switch (choice) {
                case 1:
                    showAvailableSeats();
                    break;
                case 2:
                    bookTicket();
                    break;
                case 3:
                    cancelTicket();
                    break;
                case 4:
                    showPricing();
                    break;
                case 5:
                    rateMovie();
                    break;
                case 6:
                    running = false;
                    System.out.println("Thank you. Goodbye!");
                    break;
                default:
                    // readInt prevents an out-of-range menu choice.
                    System.out.println("Invalid option.");
            }
            System.out.println();
        }

        input.close();
    }

    private static void printMenu() {
        System.out.println("\n1. Show available seats");
        System.out.println("2. Book a ticket");
        System.out.println("3. Cancel a ticket");
        System.out.println("4. Show pricing and coupon details");
        System.out.println("5. Rate the movie");
        System.out.println("6. Exit");
    }

    private static void showAvailableSeats() {
        System.out.println("\nSeats (O = available, X = booked)");
        System.out.print("    ");
        for (int seatNumber = 1; seatNumber <= SEATS_PER_ROW; seatNumber++) {
            System.out.printf("%2d ", seatNumber);
        }
        System.out.println();

        for (int row = 0; row < ROWS; row++) {
            System.out.print((char) ('A' + row) + " | ");
            for (int seat = 0; seat < SEATS_PER_ROW; seat++) {
                System.out.print(seats[row][seat] ? " X " : " O ");
            }
            System.out.println();
        }

        System.out.println("Available seats: " + countAvailableSeats() + " of "
                + (ROWS * SEATS_PER_ROW));
    }

    private static void bookTicket() {
        if (countAvailableSeats() == 0) {
            System.out.println("Sorry, all seats are booked.");
            return;
        }

        showAvailableSeats();
        int[] seatPosition = readSeatCode("Enter a seat (for example, A1): ");
        if (seatPosition == null) {
            System.out.println("Invalid seat code. Choose a row A-E and a seat number 1-8.");
            return;
        }

        int row = seatPosition[0];
        int seat = seatPosition[1];
        if (seats[row][seat]) {
            System.out.println("That seat is already booked. Please choose another seat.");
            return;
        }

        double basePrice = getCurrentTicketPrice();
        System.out.printf("Ticket price: INR %.2f (%s pricing)%n", basePrice,
                isWeekend(LocalDate.now().getDayOfWeek()) ? "weekend" : "weekday");
        String coupon = readLine("Coupon code (or press Enter to skip): ").trim();

        double discount = 0.0;
        if (coupon.equalsIgnoreCase(COUPON_CODE)) {
            discount = basePrice * COUPON_DISCOUNT;
            System.out.println("Coupon applied: " + COUPON_CODE + " (10% off).");
        } else if (!coupon.isEmpty()) {
            System.out.println("Coupon not recognized. No discount applied.");
        }

        double finalPrice = basePrice - discount;
        System.out.printf("Discount: INR %.2f%nTotal: INR %.2f%n", discount, finalPrice);

        if (!readYesNo("Confirm booking? (Y/N): ")) {
            System.out.println("Booking cancelled; the seat remains available.");
            return;
        }

        seats[row][seat] = true;
        System.out.println("Booking confirmed for seat " + seatCode(row, seat) + ".");
    }

    private static void cancelTicket() {
        int[] seatPosition = readSeatCode("Enter the booked seat to cancel (for example, A1): ");
        if (seatPosition == null) {
            System.out.println("Invalid seat code. Choose a row A-E and a seat number 1-8.");
            return;
        }

        int row = seatPosition[0];
        int seat = seatPosition[1];
        if (!seats[row][seat]) {
            System.out.println("Seat " + seatCode(row, seat) + " is not currently booked.");
            return;
        }

        seats[row][seat] = false;
        System.out.println("Ticket for seat " + seatCode(row, seat)
                + " has been cancelled. The seat is available again.");
    }

    private static void showPricing() {
        LocalDate today = LocalDate.now();
        boolean weekend = isWeekend(today.getDayOfWeek());
        double todayPrice = weekend ? WEEKEND_PRICE : WEEKDAY_PRICE;

        System.out.println("\nPricing for today (" + today + ", "
                + today.getDayOfWeek() + "):");
        System.out.printf("Weekday ticket: INR %.2f%n", WEEKDAY_PRICE);
        System.out.printf("Weekend ticket (Saturday/Sunday): INR %.2f%n", WEEKEND_PRICE);
        System.out.printf("Today's ticket price: INR %.2f (%s)%n", todayPrice,
                weekend ? "weekend" : "weekday");
        System.out.println("Coupon " + COUPON_CODE + " gives 10% off one ticket.");
    }

    private static void rateMovie() {
        if (ratingCount == ratings.length) {
            System.out.println("The rating limit for this session has been reached.");
            return;
        }

        int rating = readInt("Rate the movie from 1 to 5 stars: ", 1, 5);
        ratings[ratingCount] = rating;
        ratingCount++;
        System.out.println("Thank you for rating the movie!");
        System.out.printf("Average rating: %.2f/5 (%d rating%s)%n",
                calculateAverageRating(), ratingCount, ratingCount == 1 ? "" : "s");
    }

    private static double calculateAverageRating() {
        int total = 0;
        for (int i = 0; i < ratingCount; i++) {
            total += ratings[i];
        }
        return ratingCount == 0 ? 0.0 : (double) total / ratingCount;
    }

    private static int countAvailableSeats() {
        int available = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int seat = 0; seat < SEATS_PER_ROW; seat++) {
                if (!seats[row][seat]) {
                    available++;
                }
            }
        }
        return available;
    }

    private static double getCurrentTicketPrice() {
        return isWeekend(LocalDate.now().getDayOfWeek())
                ? WEEKEND_PRICE : WEEKDAY_PRICE;
    }

    private static boolean isWeekend(DayOfWeek day) {
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    /** Returns {rowIndex, seatIndex}, or null when the code is invalid. */
    private static int[] readSeatCode(String prompt) {
        String code = readLine(prompt).trim().toUpperCase();
        if (code.length() < 2 || code.charAt(0) < 'A' || code.charAt(0) >= 'A' + ROWS) {
            return null;
        }

        try {
            int seatNumber = Integer.parseInt(code.substring(1));
            if (seatNumber < 1 || seatNumber > SEATS_PER_ROW) {
                return null;
            }
            return new int[] { code.charAt(0) - 'A', seatNumber - 1 };
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static String seatCode(int row, int seat) {
        return "" + (char) ('A' + row) + (seat + 1);
    }

    private static int readInt(String prompt, int minimum, int maximum) {
        while (true) {
            String text = readLine(prompt).trim();
            try {
                int value = Integer.parseInt(text);
                if (value >= minimum && value <= maximum) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Show the same helpful message for non-numeric and out-of-range input.
            }
            System.out.println("Please enter a number from " + minimum + " to " + maximum + ".");
        }
    }

    private static boolean readYesNo(String prompt) {
        while (true) {
            String answer = readLine(prompt).trim();
            if (answer.equalsIgnoreCase("Y") || answer.equalsIgnoreCase("YES")) {
                return true;
            }
            if (answer.equalsIgnoreCase("N") || answer.equalsIgnoreCase("NO")) {
                return false;
            }
            System.out.println("Please enter Y or N.");
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return input.nextLine();
    }
}
