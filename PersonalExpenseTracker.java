import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * PERSONAL EXPENSE TRACKER -- single-file version
 * ===============================================
 * Everything (enum, model class, tracker, CLI) lives in one file so it can be
 * pasted straight into an IDE / online compiler and run with no setup.
 *
 * Concepts demonstrated: ArrayList, HashMap, Date/Time API (java.time),
 * Streams, Enum.
 *
 * Compile & run:
 *   javac PersonalExpenseTracker.java
 *   java PersonalExpenseTracker          (interactive menu)
 *   java PersonalExpenseTracker --demo   (scripted demo, no input needed)
 */
public class PersonalExpenseTracker {

    // ==================================================================
    // CONCEPT: ENUM -- the fixed set of spending categories
    // ==================================================================
    enum Category {
        FOOD("Food"), TRANSPORT("Transport"), RENT("Rent"), UTILITIES("Utilities"),
        HEALTH("Health"), EDUCATION("Education"), ENTERTAINMENT("Entertainment"),
        SHOPPING("Shopping"), OTHER("Other");

        private final String label;

        Category(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        static Category byNumber(int number) {
            if (number < 1 || number > values().length) {
                throw new IllegalArgumentException("Category number out of range: " + number);
            }
            return values()[number - 1];
        }

        static String menu() {
            StringBuilder sb = new StringBuilder();
            Category[] all = values();
            for (int i = 0; i < all.length; i++) {
                sb.append(i + 1).append('=').append(all[i].label);
                if (i < all.length - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return label;
        }
    }

    // ==================================================================
    // CONCEPT: DATE/TIME API -- immutable LocalDate + a reusable formatter
    // ==================================================================
    static class Expense {
        static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

        private final int id;
        private final String description;
        private final double amount;
        private final Category category;
        private final LocalDate date;

        Expense(int id, String description, double amount, Category category, LocalDate date) {
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("Description must not be empty");
            }
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be > 0");
            }
            this.id = id;
            this.description = description.trim();
            this.amount = amount;
            this.category = category;
            this.date = date;
        }

        int getId() { return id; }
        String getDescription() { return description; }
        double getAmount() { return amount; }
        Category getCategory() { return category; }
        LocalDate getDate() { return date; }

        boolean isInMonth(int year, int month) {
            return date.getYear() == year && date.getMonthValue() == month;
        }

        String toRow() {
            return String.format("%-4d %-11s %-14s %10.2f   %s", id, date.format(FMT), category, amount, description);
        }

        @Override
        public String toString() {
            return String.format("#%-3d %s | %-14s | %9.2f | %s", id, date.format(FMT), category, amount, description);
        }
    }

    // ==================================================================
    // CONCEPT: ArrayList + HashMap + Streams -- the expense store
    // ==================================================================
    static class Tracker {
        private final List<Expense> expenses = new ArrayList<>();          // ordered storage
        private final Map<Integer, Expense> byId = new java.util.HashMap<>(); // fast id lookup
        private int nextId = 1;

        // ---------- 1. ADD (with category) ----------
        Expense add(String description, double amount, Category category, LocalDate date) {
            Expense e = new Expense(nextId++, description, amount, category, date);
            expenses.add(e);
            byId.put(e.getId(), e);
            return e;
        }

        Expense add(String description, double amount, Category category) {
            return add(description, amount, category, LocalDate.now());
        }

        // ---------- 2. DELETE ----------
        boolean delete(int id) {
            Expense found = byId.remove(id);
            return found != null && expenses.remove(found);
        }

        Optional<Expense> findById(int id) {
            return Optional.ofNullable(byId.get(id));
        }

        // ---------- 3. DISPLAY ----------
        List<Expense> all() {                       // Streams: sorted by date, newest first
            return expenses.stream()
                    .sorted(Comparator.comparing(Expense::getDate).reversed())
                    .collect(Collectors.toList());
        }

        List<Expense> ofCategory(Category category) {   // Streams: filter
            return expenses.stream()
                    .filter(e -> e.getCategory() == category)
                    .sorted(Comparator.comparing(Expense::getDate))
                    .collect(Collectors.toList());
        }

        int size() { return expenses.size(); }
        boolean isEmpty() { return expenses.isEmpty(); }

        // ---------- 4. TOTAL ----------
        double total() {                            // Streams: mapToDouble + sum
            return expenses.stream().mapToDouble(Expense::getAmount).sum();
        }

        double totalOfCategory(Category c) {
            return expenses.stream().filter(e -> e.getCategory() == c)
                    .mapToDouble(Expense::getAmount).sum();
        }

        OptionalDoubleHolder average() {
            return new OptionalDoubleHolder(expenses.stream().mapToDouble(Expense::getAmount).average());
        }

        // ---------- 5. HIGHEST ----------
        Optional<Expense> highest() {               // Streams: max
            return expenses.stream().max(Comparator.comparingDouble(Expense::getAmount));
        }

        Optional<Expense> lowest() {                // Streams: min
            return expenses.stream().min(Comparator.comparingDouble(Expense::getAmount));
        }

        List<Expense> topN(int n) {                 // Streams: sorted + limit
            return expenses.stream()
                    .sorted(Comparator.comparingDouble(Expense::getAmount).reversed())
                    .limit(n)
                    .collect(Collectors.toList());
        }

        // ---------- 6. CATEGORY-WISE (HashMap + Streams) ----------
        Map<Category, Double> categoryTotals() {
            return expenses.stream()
                    .collect(Collectors.groupingBy(
                            Expense::getCategory,
                            () -> new TreeMap<>(Comparator.comparingInt(Enum::ordinal)),
                            Collectors.summingDouble(Expense::getAmount)));
        }

        /** Same result written with a plain loop + HashMap.merge() for comparison. */
        Map<Category, Double> categoryTotalsIterative() {
            Map<Category, Double> result = new LinkedHashMap<>();
            for (Expense e : expenses) {
                result.merge(e.getCategory(), e.getAmount(), Double::sum);
            }
            return result;
        }

        Map<Category, Long> categoryCounts() {
            return expenses.stream()
                    .collect(Collectors.groupingBy(Expense::getCategory, Collectors.counting()));
        }

        List<Map.Entry<Category, Double>> categoryRanking() {
            return categoryTotals().entrySet().stream()
                    .sorted(Map.Entry.<Category, Double>comparingByValue().reversed())
                    .collect(Collectors.toList());
        }

        Optional<Map.Entry<Category, Double>> topCategory() {
            return categoryRanking().stream().findFirst();
        }

        // ---------- 7. MONTH-WISE (YearMonth + Streams) ----------
        Map<YearMonth, Double> monthlyTotals() {
            return expenses.stream()
                    .collect(Collectors.groupingBy(e -> YearMonth.from(e.getDate()),
                            TreeMap::new,
                            Collectors.summingDouble(Expense::getAmount)));
        }
    }

    /** Tiny wrapper so a missing average prints as "-" instead of crashing. */
    static class OptionalDoubleHolder {
        private final java.util.OptionalDouble value;
        OptionalDoubleHolder(java.util.OptionalDouble value) { this.value = value; }
        double orElse(double fallback) { return value.orElse(fallback); }
        void ifPresent(java.util.function.DoubleConsumer action) { value.ifPresent(action); }
    }

    // ==================================================================
    // CLI
    // ==================================================================
    private static final Tracker tracker = new Tracker();
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("--demo")) {
            demo();
        } else {
            menu();
        }
    }

    private static void menu() {
        System.out.println("===== PERSONAL EXPENSE TRACKER =====");
        boolean running = true;
        while (running) {
            System.out.println("\n 1.Add  2.Display  3.Delete  4.Total  5.Highest");
            System.out.println(" 6.Category-wise  7.Month-wise  8.By category  9.Search  10.Report  0.Exit");
            int choice = readInt("Choice: ", 0, 10);
            switch (choice) {
                case 0: running = false; break;
                case 1: addFlow(); break;
                case 2: displayAll(); break;
                case 3: deleteFlow(); break;
                case 4: totalFlow(); break;
                case 5: highestFlow(); break;
                case 6: categoryFlow(); break;
                case 7: monthFlow(); break;
                case 8: byCategoryFlow(); break;
                case 9: searchFlow(); break;
                case 10: report(); break;
                default: System.out.println("Invalid option.");
            }
        }
        System.out.println("Bye!");
    }

    private static void addFlow() {
        System.out.println("Categories: " + Category.menu());
        Category c = Category.byNumber(readInt("Category number: ", 1, Category.values().length));
        String desc = readText("Description: ");
        double amount = readDouble("Amount: ");
        String dateText = readText("Date [dd-MM-yyyy, Enter = today]: ");
        LocalDate date = dateText.isEmpty()
                ? LocalDate.now()
                : LocalDate.parse(dateText, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        Expense e = tracker.add(desc, amount, c, date);
        System.out.println("Added -> " + e);
    }

    private static void displayAll() {
        if (guard()) return;
        System.out.println(String.format("%-4s %-11s %-14s %10s   %s", "ID", "DATE", "CATEGORY", "AMOUNT", "DESCRIPTION"));
        tracker.all().forEach(e -> System.out.println(e.toRow()));
        System.out.println(tracker.size() + " expense(s).");
    }

    private static void deleteFlow() {
        if (guard()) return;
        displayAll();
        int id = readInt("Id to delete: ", 1, Integer.MAX_VALUE);
        Optional<Expense> found = tracker.findById(id);
        if (found.isPresent()) {
            tracker.delete(id);
            System.out.println("Deleted " + found.get());
        } else {
            System.out.println("No expense with id " + id + ".");
        }
    }

    private static void totalFlow() {
        if (guard()) return;
        System.out.printf("TOTAL EXPENSE = %.2f%n", tracker.total());
        tracker.average().ifPresent(avg -> System.out.printf("Average = %.2f%n", avg));
    }

    private static void highestFlow() {
        if (guard()) return;
        tracker.highest().ifPresent(h -> System.out.println("HIGHEST EXPENSE -> " + h));
        tracker.lowest().ifPresent(l -> System.out.println("Lowest expense  -> " + l));
    }

    private static void categoryFlow() {
        if (guard()) return;
        Map<Category, Double> totals = tracker.categoryTotals();
        double total = tracker.total();
        System.out.println("CATEGORY-WISE EXPENSES");
        System.out.println(String.format("%-14s %10s %8s %7s", "CATEGORY", "TOTAL", "SHARE", "ITEMS"));
        Map<Category, Long> counts = tracker.categoryCounts();
        for (Map.Entry<Category, Double> entry : tracker.categoryRanking()) {
            System.out.printf("%-14s %10.2f %7.1f%% %7d%n",
                    entry.getKey().getLabel(), entry.getValue(),
                    total == 0 ? 0.0 : entry.getValue() / total * 100.0, counts.getOrDefault(entry.getKey(), 0L));
        }
        System.out.printf("%-14s %10.2f%n", "TOTAL", total);
        tracker.topCategory().ifPresent(t ->
                System.out.printf("Biggest category: %s (%.2f)%n", t.getKey().getLabel(), t.getValue()));
    }

    private static void monthFlow() {
        if (guard()) return;
        System.out.println("MONTH-WISE EXPENSES");
        tracker.monthlyTotals().forEach((month, sum) -> System.out.printf("%-9s %10.2f%n", month, sum));
    }

    private static void byCategoryFlow() {
        if (guard()) return;
        System.out.println("Categories: " + Category.menu());
        Category c = Category.byNumber(readInt("Category number: ", 1, Category.values().length));
        List<Expense> list = tracker.ofCategory(c);
        if (list.isEmpty()) {
            System.out.println("No expenses in " + c);
            return;
        }
        list.forEach(e -> System.out.println(e.toRow()));
        System.out.printf("Subtotal = %.2f (%d items)%n", tracker.totalOfCategory(c), list.size());
    }

    private static void searchFlow() {
        if (guard()) return;
        String keyword = readText("Keyword: ").toLowerCase();
        List<Expense> hits = tracker.all().stream()
                .filter(e -> e.getDescription().toLowerCase().contains(keyword))
                .collect(Collectors.toList());
        if (hits.isEmpty()) {
            System.out.println("No match.");
        } else {
            hits.forEach(e -> System.out.println(e.toRow()));
        }
    }

    private static void report() {
        if (guard()) return;
        System.out.println("------------------------------");
        System.out.println("EXPENSE REPORT");
        System.out.println("------------------------------");
        displayAll();
        totalFlow();
        highestFlow();
        categoryFlow();
        monthFlow();
    }

    private static boolean guard() {
        if (tracker.isEmpty()) {
            System.out.println("No expenses yet -- add one first (option 1).");
            return true;
        }
        return false;
    }

    // ---------------- input helpers ----------------
    private static String readText(String prompt) {
        System.out.print(prompt);
        return in.hasNextLine() ? in.nextLine().trim() : "";
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            try {
                int n = Integer.parseInt(readText(prompt));
                if (n < min || n > max) {
                    System.out.printf("Enter a number between %d and %d.%n", min, max);
                } else {
                    return n;
                }
            } catch (NumberFormatException ex) {
                System.out.println("Not a whole number, try again.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            try {
                double d = Double.parseDouble(readText(prompt));
                if (d > 0) {
                    return Math.round(d * 100.0) / 100.0;
                }
                System.out.println("Amount must be > 0.");
            } catch (NumberFormatException ex) {
                System.out.println("Not a valid amount, try again.");
            }
        }
    }

    // ==================================================================
    // SCRIPTED DEMO
    // ==================================================================
    private static void demo() {
        LocalDate today = LocalDate.now();
        LocalDate thisMonth = today.withDayOfMonth(1);
        LocalDate lastMonth = today.minusMonths(1).withDayOfMonth(1);

        tracker.add("Vegetables & groceries", 2450.75, Category.FOOD, thisMonth.plusDays(4));
        tracker.add("Metro card recharge", 1000.00, Category.TRANSPORT, thisMonth.plusDays(5));
        tracker.add("Hostel rent", 8500.00, Category.RENT, thisMonth.plusDays(6));
        tracker.add("Electricity bill", 1320.40, Category.UTILITIES, thisMonth.plusDays(7));
        tracker.add("Java course", 499.00, Category.EDUCATION, thisMonth.plusDays(9));
        tracker.add("Movie with friends", 750.00, Category.ENTERTAINMENT, thisMonth.plusDays(11));
        tracker.add("Running shoes", 3299.00, Category.SHOPPING, today.minusDays(2));
        tracker.add("Dentist visit", 1200.00, Category.HEALTH, lastMonth.plusDays(11));
        tracker.add("Monthly groceries", 3100.25, Category.FOOD, lastMonth.plusDays(15));
        tracker.add("Petrol", 1800.00, Category.TRANSPORT, lastMonth.plusDays(20));
        tracker.add("Hostel rent", 8500.00, Category.RENT, lastMonth.plusDays(2));
        tracker.add("Internet bill", 799.00, Category.UTILITIES, lastMonth.plusDays(5));
        tracker.add("Lunch at canteen", 180.50, Category.FOOD, today);

        System.out.println("=== DEMO: " + tracker.size() + " expenses added ===\n");
        displayAll();
        System.out.println();
        totalFlow();
        System.out.println();
        highestFlow();
        System.out.println();
        System.out.println("Top 3:");
        tracker.topN(3).forEach(e -> System.out.println("  " + e));
        System.out.println();
        categoryFlow();
        System.out.println();
        monthFlow();
        System.out.println();
        System.out.println("-- Imperative HashMap version (loop + merge) --");
        tracker.categoryTotalsIterative().forEach((c, sum) ->
                System.out.printf("%-14s %10.2f%n", c.getLabel(), sum));
        System.out.println();
        System.out.println("-- Delete #2 and re-check the total --");
        System.out.printf("Before: %d expenses, total %.2f%n", tracker.size(), tracker.total());
        tracker.findById(2).ifPresent(e -> {
            tracker.delete(2);
            System.out.println("Deleted: " + e);
        });
        System.out.printf("After : %d expenses, total %.2f%n", tracker.size(), tracker.total());
    }
}
