
import java.util.Scanner;
import java.util.ArrayList;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class Main {

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("===== SMALL BUSINESS LEDGER =====");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment");
            System.out.println("L) Ledger");
            System.out.println("X) Exit");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().toUpperCase();

            switch (choice) {
                case "D":
                    addTransaction(true);
                    break;
                case "P":
                    addTransaction(false);
                    break;
                case "L":
                    showLedger();
                    break;
                case "X":
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
        scanner.close();
    }

    public static void addTransaction(boolean isDeposit) {
        if (isDeposit) {
            System.out.println("===== ADD DEPOSIT =====");
        } else {
            System.out.println("===== MAKE PAYMENT =====");
        }

        System.out.print("Description: ");
        String description = scanner.nextLine();

        System.out.print("Vendor: ");
        String vendor = scanner.nextLine();

        System.out.print("Amount: ");
        double amount;

        try {
            amount = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }

        if (amount <= 0) {
            System.out.println("Amount must be greater than zero.");
            return;
        }

        if (!isDeposit) {
            amount = -amount;
        }

        String date = LocalDate.now().toString();
        String time = LocalTime.now().withNano(0).toString();

        Transaction transaction = new Transaction(
                date, time, description, vendor, amount
        );

        try {
            FileWriter writer = new FileWriter("transactions.csv", true);

            writer.write(
                    transaction.getDate() + "|" +
                            transaction.getTime() + "|" +
                            transaction.getDescription() + "|" +
                            transaction.getVendor() + "|" +
                            transaction.getAmount() + "\n"
            );

            writer.close();
            System.out.println("Transaction saved successfully!");

        } catch (IOException e) {
            System.out.println("Error saving transaction.");
        }
    }

    public static ArrayList<Transaction> loadTransactions() {
        ArrayList<Transaction> transactions = new ArrayList<>();

        try {
            BufferedReader reader =
                    new BufferedReader(new FileReader("transactions.csv"));

            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");

                if (parts.length == 5) {
                    try {
                        Transaction transaction = new Transaction(
                                parts[0],
                                parts[1],
                                parts[2],
                                parts[3],
                                Double.parseDouble(parts[4])
                        );
                        transactions.add(transaction);
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping invalid transaction.");
                    }
                }
            }
            reader.close();

        } catch (IOException e) {
            System.out.println("No transactions found yet.");
        }

        return transactions;
    }

    public static void showLedger() {
        boolean viewing = true;

        while (viewing) {
            System.out.println();
            System.out.println("===== LEDGER =====");
            System.out.println("A) All Transactions");
            System.out.println("D) Deposits");
            System.out.println("P) Payments");
            System.out.println("R) Reports");
            System.out.println("H) Home");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().toUpperCase();

            switch (choice) {
                case "A":
                    displayTransactions("A");
                    break;
                case "D":
                    displayTransactions("D");
                    break;
                case "P":
                    displayTransactions("P");
                    break;
                case "R":
                    showReports();
                    break;
                case "H":
                    viewing = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    public static void displayTransactions(String filter) {
        ArrayList<Transaction> transactions = loadTransactions();

        System.out.println();
        System.out.println("===== TRANSACTIONS =====");

        boolean found = false;

        for (int i = transactions.size() - 1; i >= 0; i--) {
            Transaction t = transactions.get(i);

            boolean show = false;

            if (filter.equals("A")) {
                show = true;
            } else if (filter.equals("D") && t.getAmount() > 0) {
                show = true;
            } else if (filter.equals("P") && t.getAmount() < 0) {
                show = true;
            }

            if (show) {
                System.out.printf(
                        "%s | %s | %s | %s | $%.2f%n",
                        t.getDate(),
                        t.getTime(),
                        t.getDescription(),
                        t.getVendor(),
                        t.getAmount()
                );
                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching transactions.");
        }
    }

    public static void showReports() {
        boolean viewing = true;

        while (viewing) {
            System.out.println();
            System.out.println("===== REPORTS =====");
            System.out.println("1) Month To Date");
            System.out.println("2) Previous Month");
            System.out.println("3) Year To Date");
            System.out.println("4) Previous Year");
            System.out.println("5) Search by Vendor");
            System.out.println("0) Back");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                case "2":
                case "3":
                case "4":
                    displayReport(choice, "");
                    break;

                case "5":
                    System.out.print("Enter vendor name: ");
                    String vendor = scanner.nextLine();
                    displayReport(choice, vendor);
                    break;

                case "0":
                    viewing = false;
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    public static void displayReport(String choice, String vendor) {
        ArrayList<Transaction> transactions = loadTransactions();
        LocalDate today = LocalDate.now();
        boolean found = false;

        System.out.println();
        System.out.println("===== REPORT RESULTS =====");

        for (int i = transactions.size() - 1; i >= 0; i--) {
            Transaction t = transactions.get(i);
            LocalDate date = LocalDate.parse(t.getDate());
            boolean show = false;

            switch (choice) {
                case "1":
                    show = date.getYear() == today.getYear()
                            && date.getMonthValue() == today.getMonthValue()
                            && !date.isAfter(today);
                    break;

                case "2":
                    LocalDate previousMonth = today.minusMonths(1);
                    show = date.getYear() == previousMonth.getYear()
                            && date.getMonthValue() == previousMonth.getMonthValue();
                    break;

                case "3":
                    show = date.getYear() == today.getYear()
                            && !date.isAfter(today);
                    break;

                case "4":
                    show = date.getYear() == today.getYear() - 1;
                    break;

                case "5":
                    show = t.getVendor().toLowerCase()
                            .contains(vendor.toLowerCase());
                    break;
            }

            if (show) {
                System.out.printf(
                        "%s | %s | %s | %s | $%.2f%n",
                        t.getDate(),
                        t.getTime(),
                        t.getDescription(),
                        t.getVendor(),
                        t.getAmount()
                );
                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching transactions.");
        }
    }

}
