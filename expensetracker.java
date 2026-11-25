import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class expensetracker {

    private static final String FILE_NAME = "expenses.txt";
    private static int nextIdCounter = 101; // Auto-generates X101, X102, ...

    // 1. Node class for the Custom Linked List
    static class ExpenseNode {
        String id;
        String category;
        int amount;
        ExpenseNode next;

        ExpenseNode(String id, String category, int amount) {
            this.id = id;
            this.category = category;
            this.amount = amount;
            this.next = null;
        }
    }

    // 2. Custom Singly Linked List
    static class ExpenseLinkedList {
        private ExpenseNode head;
        private ExpenseNode tail;
        private int size;

        ExpenseLinkedList() {
            this.head = null;
            this.tail = null;
            this.size = 0;
        }

        // Add new expense at the end of the list - O(1)
        public void addExpense(String id, String category, int amount) {
            ExpenseNode newNode = new ExpenseNode(id, category, amount);
            if (head == null) {
                head = newNode;
                tail = newNode;
            } else {
                tail.next = newNode;
                tail = newNode;
            }
            size++;
        }

        // Option 2: Traverse and display all expenses - O(N)
        public void viewAll() {
            if (head == null || size == 0) {
                System.out.println("No expenses found. The list is currently empty.");
                return;
            }

            System.out.println("--- All Recorded Expenses (" + size + ") ---");
            ExpenseNode current = head;
            while (current != null) {
                System.out.println(current.id + " | " + current.category + " | " + current.amount);
                current = current.next;
            }
        }

        // Option 3: Sort expenses by amount (Low to High) using Merge Sort - O(N log N)
        public void sortByAmount() {
            if (head == null || head.next == null) {
                return;
            }
            head = mergeSort(head);

            // Re-align tail pointer after sorting
            ExpenseNode temp = head;
            while (temp.next != null) {
                temp = temp.next;
            }
            tail = temp;
        }

        private ExpenseNode mergeSort(ExpenseNode node) {
            if (node == null || node.next == null) {
                return node;
            }
            ExpenseNode mid = getMiddle(node);
            ExpenseNode rightHalf = mid.next;
            mid.next = null;

            ExpenseNode leftSorted = mergeSort(node);
            ExpenseNode rightSorted = mergeSort(rightHalf);

            return merge(leftSorted, rightSorted);
        }

        private ExpenseNode getMiddle(ExpenseNode node) {
            ExpenseNode slow = node;
            ExpenseNode fast = node.next;
            while (fast != null && fast.next != null) {
                slow = slow.next;
                fast = fast.next.next;
            }
            return slow;
        }

        private ExpenseNode merge(ExpenseNode a, ExpenseNode b) {
            if (a == null) return b;
            if (b == null) return a;

            if (a.amount <= b.amount) {
                a.next = merge(a.next, b);
                return a;
            } else {
                b.next = merge(a, b.next);
                return b;
            }
        }

        // Option 4: Binary Search by amount (sorts list first) - O(log N) search
        public ExpenseNode binarySearch(int target) {
            if (head == null || size == 0) {
                return null;
            }

            sortByAmount();

            ExpenseNode[] nodes = new ExpenseNode[size];
            ExpenseNode current = head;
            int idx = 0;
            while (current != null && idx < size) {
                nodes[idx++] = current;
                current = current.next;
            }

            int low = 0;
            int high = size - 1;

            while (low <= high) {
                int mid = low + (high - low) / 2;
                if (nodes[mid].amount == target) {
                    return nodes[mid];
                } else if (nodes[mid].amount < target) {
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }
            return null;
        }

    
        public boolean removeById(String id) {
            if (head == null) {
                return false;
            }

            if (head.id.equals(id)) {
                head = head.next;
                if (head == null) {
                    tail = null;
                }
                size--;
                return true;
            }

            ExpenseNode prev = head;
            ExpenseNode curr = head.next;

            while (curr != null) {
                if (curr.id.equals(id)) {
                    prev.next = curr.next;
                    if (curr == tail) {
                        tail = prev;
                    }
                    size--;
                    return true;
                }
                prev = curr;
                curr = curr.next;
            }
            return false;
        }

        public int getSize() {
            return size;
        }

        public ExpenseNode getHead() {
            return head;
        }
    }

    // 3. Custom Stack for Undo functionality (LIFO)
    static class UndoStack {
        private static class StackNode {
            String expenseId;
            StackNode next;

            StackNode(String expenseId) {
                this.expenseId = expenseId;
            }
        }

        private StackNode top;

        public void push(String id) {
            StackNode node = new StackNode(id);
            node.next = top;
            top = node;
        }

        public String pop() {
            if (isEmpty()) {
                return null;
            }
            String id = top.expenseId;
            top = top.next;
            return id;
        }

        public boolean isEmpty() {
            return top == null;
        }
    }

    // 4. File Handling: Load records from expenses.txt
    public static void loadFromFile(String filename, ExpenseLinkedList list) {
        File file = new File(filename);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    String id = parts[0].trim();
                    String category = parts[1].trim();
                    int amount = Integer.parseInt(parts[2].trim());

                    if (amount > 0) {
                        list.addExpense(id, category, amount);
                        if (id.startsWith("X")) {
                            try {
                                int num = Integer.parseInt(id.substring(1));
                                if (num >= nextIdCounter) {
                                    nextIdCounter = num + 1;
                                }
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not read previous data from " + filename);
        }
    }

    // 5. File Handling: Save records to expenses.txt
    public static void saveToFile(String filename, ExpenseLinkedList list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            ExpenseNode current = list.getHead();
            while (current != null) {
                bw.write(current.id + "|" + current.category + "|" + current.amount);
                bw.newLine();
                current = current.next;
            }
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    // 6. Main Menu Loop
    public static void main(String[] args) {
        ExpenseLinkedList expenseList = new ExpenseLinkedList();
        UndoStack undoStack = new UndoStack();
        Scanner sc= new Scanner(System.in);

        loadFromFile(FILE_NAME, expenseList);

        while (true) {
            System.out.println("\nEXPENSE TRACKER MENU");
            System.out.println("1. Add Expense");
            System.out.println("2. View All Expenses");
            System.out.println("3. Sort by Amount (Low to High)");
            System.out.println("4. Search Amount (Binary Search)");
            System.out.println("5. Undo Last Expense");
            System.out.println("6. Save & Exit");
            System.out.print("Enter choice: ");

            if (!sc.hasNextLine()) {
                break;
            }

            String rawChoice = sc.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(rawChoice);
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice. Please enter a number from 1 to 6.");
                continue;
            }

            if (choice == 1) {
                System.out.print("Enter Category: ");
                String category = sc.nextLine().trim();
                if (category.isEmpty()) {
                    System.out.println("Category cannot be empty.");
                    continue;
                }

                System.out.print("Enter Amount: ");
                int amount;
                try {
                    amount = Integer.parseInt(sc.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid amount. Please enter a valid integer.");
                    continue;
                }

                if (amount <= 0) {
                    System.out.println("Invalid amount. Amount must be greater than 0.");
                    continue;
                }

                String id = "X" + nextIdCounter++;
                expenseList.addExpense(id, category, amount);
                undoStack.push(id);
                System.out.println("Expense added. ID: " + id + " | " + category + " | " + amount);

            } else if (choice == 2) {
                expenseList.viewAll();

            } else if (choice == 3) {
                if (expenseList.getSize() == 0) {
                    System.out.println("No expenses to sort.");
                } else {
                    expenseList.sortByAmount();
                    System.out.println("Expenses sorted by amount (low to high):");
                    expenseList.viewAll();
                }

            } else if (choice == 4) {
                if (expenseList.getSize() == 0) {
                    System.out.println("No expenses recorded to search.");
                    continue;
                }

                System.out.print("Enter amount to search: ");
                int target;
                try {
                    target = Integer.parseInt(sc.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid amount. Please enter a valid integer.");
                    continue;
                }

                ExpenseNode result = expenseList.binarySearch(target);
                if (result != null) {
                    System.out.println("Found: " + result.id + " | " + result.category + " | " + result.amount);
                } else {
                    System.out.println("No expense found with amount " + target + ".");
                }

            } else if (choice == 5) {
                if (undoStack.isEmpty()) {
                    System.out.println("Nothing to undo.");
                } else {
                    String lastId = undoStack.pop();
                    if (expenseList.removeById(lastId)) {
                        System.out.println("Last expense " + lastId + " removed. Total expenses: " + expenseList.getSize());
                    } else {
                        System.out.println("Could not find expense " + lastId + " to remove.");
                    }
                }

            } else if (choice == 6) {
                saveToFile(FILE_NAME, expenseList);
                System.out.println("Expenses saved to " + FILE_NAME + ". Exiting...");
                break;

            } else {
                System.out.println("Invalid option. Please enter a number between 1 and 6.");
            }
        }

        sc.close();
    }
}