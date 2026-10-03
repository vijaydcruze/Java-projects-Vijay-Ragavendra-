# Personal Expense Tracker (Java)

A console application that records daily expenses, categorises them, and produces
analytics: total spending, highest expense, category-wise breakdown, and month-wise
breakdown.

**Concepts used:** `ArrayList` · `HashMap` · `Date/Time API (java.time)` · `Streams` · `Enum`

---

## 1. Project structure

```
expense-tracker/
├── PersonalExpenseTracker.java     <-- SINGLE-FILE version (paste & run anywhere)
├── src/
│   ├── Category.java               <-- Enum of spending categories
│   ├── Expense.java                <-- Model: id, description, amount, category, LocalDate
│   ├── ExpenseTracker.java         <-- ArrayList + HashMap + Streams analytics engine
│   └── Main.java                   <-- Menu-driven CLI + scripted demo
├── data/                           <-- expenses.csv is written here (auto-created)
└── README.md
```

Two ways to run it — pick whichever suits you:

| Version | Use it when |
|---|---|
| `PersonalExpenseTracker.java` (root) | You need **one file** to submit / paste into an online compiler |
| `src/*.java` (4 files) | You want the **clean, separated** design (e.g. a lab record) |

---

## 2. How to run

### Multi-file version
```bash
cd expense-tracker
javac -d out src/*.java
java -cp out Main            # interactive menu
java -cp out Main --demo     # scripted demo, no typing needed
```

### Single-file version
```bash
cd expense-tracker
javac PersonalExpenseTracker.java
java PersonalExpenseTracker          # interactive menu
java PersonalExpenseTracker --demo   # scripted demo
```

Requires JDK 11+ (the code avoids Java 16+ features like records/`Stream.toList()` so it
runs on Java 11 too).

---

## 3. Features → where the code lives

| # | Feature | Method | Concept shown |
|---|---|---|---|
| 1 | Add expense | `ExpenseTracker.addExpense(...)` | `ArrayList.add` |
| 2 | Categorize expense | `Category` enum (9 categories, chosen by number in the menu) | `Enum` with fields + methods |
| 3 | Delete expense | `ExpenseTracker.deleteExpense(int id)` | `ArrayList.remove` + `HashMap.remove` |
| 4 | Display all expenses | `getAllExpenses()` | `Stream.sorted(Comparator)` |
| 5 | Calculate total expense | `totalExpense()` | `mapToDouble(...).sum()` |
| 6 | Find highest expense | `highestExpense()` → `Optional<Expense>` | `Stream.max(Comparator)` |
| 7 | Category-wise expenses | `categoryWiseTotals()`, `categoryWiseCount()`, `categoryWiseSharePercent()`, `categoryRanking()` | `HashMap` + `Collectors.groupingBy` + `summingDouble` |
| + | Month-wise expenses | `monthlyTotals()` | `YearMonth` + `TreeMap` |
| + | Filter by category / search | `getExpensesByCategory()`, `search()` | `Stream.filter` |
| + | Save / load to CSV | `saveToCsv()`, `loadFromCsv()` | `java.nio.file.Files` |

---

## 4. Menu options

```
 1. Add expense                 6. Category-wise expenses
 2. Display all expenses        7. Month-wise expenses
 3. Delete expense (with undo)  8. Expenses of one category
 4. Total expense               9. Search description
 5. Highest expense            10. Full report
                               11. Save to file        0. Exit
```

---

## 5. Concept notes (what to say if you're asked to explain)

**Enum — `Category.java`**
A fixed set of constants that can't be misspelled, so a category is always valid.
It is a real class, so each constant carries a label: `FOOD("Food")`. Because it is an
enum it can safely be used as a `HashMap` key and in a `switch`.

**ArrayList — the main store**
`private final List<Expense> expenses = new ArrayList<>();`
Ordered, growable, O(1) append. Ordering is preserved, which is what lets the report
show expenses in the order they were added.

**HashMap — two jobs**
1. `Map<Integer, Expense> byId` makes delete/lookup O(1) instead of scanning the list.
2. The analytics result maps: `Map<Category, Double>` and `Map<YearMonth, Double>`.

```java
public Map<Category, Double> categoryWiseTotals() {          // Streams version
    return expenses.stream()
            .collect(Collectors.groupingBy(Expense::getCategory,
                     () -> new TreeMap<>(Comparator.comparingInt(Enum::ordinal)),
                     Collectors.summingDouble(Expense::getAmount)));
}
```
The class also ships the **same thing written imperatively** (`categoryWiseTotalsIterative()`)
so you can show how the stream version works under the hood:
```java
Map<Category, Double> result = new LinkedHashMap<>();
for (Expense e : expenses) {
    result.merge(e.getCategory(), e.getAmount(), Double::sum);
}
```

**Date/Time API — `java.time`**
`LocalDate` (immutable, date-only) for each expense, `YearMonth` for monthly grouping,
`DateTimeFormatter` for output (`03 Oct 2026`). Old `java.util.Date` is avoided because
it is mutable and timezone-shifted.

**Streams — all the analytics**
| Question | Stream operation |
|---|---|
| Total | `mapToDouble(Expense::getAmount).sum()` |
| Highest | `max(Comparator.comparingDouble(Expense::getAmount))` → `Optional` |
| Average | `.average()` → `OptionalDouble` |
| Per category | `collect(groupingBy(getCategory, summingDouble(getAmount)))` |
| Per month | `collect(groupingBy(YearMonth::from, summingDouble(...)))` |
| Ranking | `entrySet().stream().sorted(comparingByValue().reversed())` |
| Top N | `sorted(...).limit(n)` |

`Optional` is used instead of returning `null` — so an empty tracker prints
"No expenses yet" rather than throwing a `NullPointerException`.

---

## 6. Sample run (`--demo`)

```
--- 4. Category-wise expenses (HashMap + groupingBy) ---
  Food              5731.50   16.4%  (3)
  Transport         2800.00    8.0%  (2)
  Rent             17000.00   48.7%  (2)
  Utilities         2119.40    6.1%  (2)
  Health            1200.00    3.4%  (1)
  Education          499.00    1.4%  (1)
  Entertainment      750.00    2.1%  (1)
  Shopping          3299.00    9.5%  (1)
  Other             1500.00    4.3%  (1)
  => Biggest category: Rent at 17000.00

--- 5. Month-wise expenses (YearMonth + Streams) ---
  2026-08      1500.00
  2026-09     15399.25
  2026-10     17999.65

--- 7. Delete an expense (#2 metro recharge) ---
  Before: 14 expenses, total 34898.90
  Deleted: #2 06 Oct 2026 | Transport | 1000.00 | Metro card recharge
  After : 13 expenses, total 33898.90
```

Interactive menu sample:
```
CATEGORY-WISE EXPENSES (biggest first)
---------------------------------------------------------------------
CATEGORY            TOTAL   SHARE   ITEMS  BAR
Utilities         1450.75   69.0%       1  ########################
Education          320.00   15.2%       1  #####
Food               250.50   11.9%       1  ####
Transport           80.00    3.8%       1  #
---------------------------------------------------------------------
TOTAL             2101.25
```

---

## 7. Validation & edge cases handled

- Amount must be `> 0`; description cannot be blank (enforced in the `Expense` constructor).
- Menu input is re-prompted on non-numeric or out-of-range values.
- Unreadable dates fall back to today with a warning.
- Every analytics method works on an empty list (prints a friendly message, no crash).
- Amounts are rounded to 2 decimals on entry.
- Expenses are saved to `data/expenses.csv` on exit and reloaded on the next start.

## 8. Ideas to extend (good for extra marks)

- Budget per category with an over-budget warning.
- Sort options (by amount / date / category) in the display menu.
- Export the report to a `.txt`/`.csv` file with `Files.write`.
- Swap the CSV store for SQLite via JDBC.
