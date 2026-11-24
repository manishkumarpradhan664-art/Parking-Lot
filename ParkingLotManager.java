import java.util.*;
import java.io.*;

public class ParkingLotManager {

    // Parking lot capacity
    private static final int CAPACITY = 3;

    // Stack for parked vehicles (LIFO)
    private static Stack<String> parkedVehicles = new Stack<>();

    // Queue for waiting vehicles (FIFO)
    private static Queue<String> waitingVehicles = new LinkedList<>();

    // HashMap for quick vehicle lookup
    private static HashMap<String, String> vehicleMap = new HashMap<>();

    private static final String FILE_NAME = "parking.txt";

    private static Scanner sc = new Scanner(System.in);

    // Rebuild HashMap after every parking change
    private static void updateHashMap() {

        vehicleMap.clear();

        for (int i = 0; i < parkedVehicles.size(); i++) {
            vehicleMap.put(
                parkedVehicles.get(i),
                "Parked - Slot " + (i + 1)
            );
        }

        int position = 1;

        for (String vehicle : waitingVehicles) {
            vehicleMap.put(
                vehicle,
                "Waiting - Position " + position
            );
            position++;
        }
    }

    // Validate vehicle number
    private static boolean isValidVehicle(String vehicle) {
        return vehicle.matches("[A-Z0-9]{4,15}");
    }

    // Park a vehicle
    private static void parkVehicle() {

        System.out.print("Enter vehicle number: ");
        String vehicle = sc.nextLine().trim().toUpperCase();

        if (!isValidVehicle(vehicle)) {
            System.out.println("Invalid vehicle number!");
            return;
        }

        updateHashMap();

        // Prevent duplicate vehicles
        if (vehicleMap.containsKey(vehicle)) {
            System.out.println(
                "Vehicle " + vehicle + " is already registered: "
                + vehicleMap.get(vehicle)
            );
            return;
        }

        // Check parking availability
        if (parkedVehicles.size() < CAPACITY) {

            parkedVehicles.push(vehicle);

            System.out.println(
                "Vehicle " + vehicle +
                " parked. Slot: " + parkedVehicles.size()
            );

        } else {

            waitingVehicles.offer(vehicle);

            System.out.println(
                "Lot full. Vehicle added to waiting queue. Position: "
                + waitingVehicles.size()
            );
        }

        updateHashMap();
        saveData();
    }

    // Exit a vehicle
    private static void exitVehicle() {

        System.out.print("Enter vehicle number to exit: ");
        String vehicle = sc.nextLine().trim().toUpperCase();

        updateHashMap();

        if (!parkedVehicles.contains(vehicle)) {

            if (waitingVehicles.contains(vehicle)) {
                waitingVehicles.remove(vehicle);

                System.out.println(
                    "Vehicle " + vehicle +
                    " removed from waiting queue."
                );

                updateHashMap();
                saveData();
            } else {
                System.out.println(
                    "Vehicle " + vehicle + " not found."
                );
            }

            return;
        }

        // Stack follows LIFO.
        // Only the top vehicle can exit directly.
        if (!parkedVehicles.peek().equals(vehicle)) {

            System.out.println(
                "Vehicle cannot exit. Another vehicle is blocking it."
            );

            return;
        }

        String exitedVehicle = parkedVehicles.pop();

        System.out.println(
            "Vehicle " + exitedVehicle + " exited."
        );

        // Automatically park next waiting vehicle
        if (!waitingVehicles.isEmpty()) {

            String nextVehicle = waitingVehicles.poll();

            parkedVehicles.push(nextVehicle);

            System.out.println(
                nextVehicle + " parked from queue. Slot: "
                + parkedVehicles.size()
            );
        }

        updateHashMap();
        saveData();
    }

    // Search vehicle using HashMap
    private static void searchVehicle() {

        System.out.print("Enter vehicle number to search: ");
        String vehicle = sc.nextLine().trim().toUpperCase();

        updateHashMap();

        if (vehicleMap.containsKey(vehicle)) {

            System.out.println(
                "Vehicle " + vehicle + " found."
            );

            System.out.println(vehicleMap.get(vehicle));

        } else {

            System.out.println(
                "Vehicle " + vehicle + " not found."
            );
        }
    }

    // Display parking lot and waiting queue
    private static void displayParking() {

        System.out.println("\n========== PARKING LOT ==========");

        if (parkedVehicles.isEmpty()) {
            System.out.println("Parking lot is empty.");
        } else {

            for (int i = 0; i < CAPACITY; i++) {

                if (i < parkedVehicles.size()) {
                    System.out.println(
                        "Slot " + (i + 1) + ": "
                        + parkedVehicles.get(i)
                    );
                } else {
                    System.out.println(
                        "Slot " + (i + 1) + ": Empty"
                    );
                }
            }
        }

        System.out.println("\n========== WAITING QUEUE ==========");

        if (waitingVehicles.isEmpty()) {
            System.out.println("Waiting: 0");
        } else {

            int position = 1;

            for (String vehicle : waitingVehicles) {
                System.out.println(
                    "Position " + position + ": " + vehicle
                );
                position++;
            }
        }

        System.out.println("\nParked vehicles: "
                + parkedVehicles.size());

        System.out.println("Waiting vehicles: "
                + waitingVehicles.size());

        System.out.println("==================================");

        updateHashMap();
    }

    // Save data to file
    private static void saveData() {

        try (PrintWriter writer =
                new PrintWriter(new FileWriter(FILE_NAME))) {

            // Save parked vehicles
            for (String vehicle : parkedVehicles) {
                writer.println("P|" + vehicle);
            }

            // Save waiting vehicles
            for (String vehicle : waitingVehicles) {
                writer.println("W|" + vehicle);
            }

        } catch (IOException e) {

            System.out.println(
                "Error saving data: " + e.getMessage()
            );
        }
    }

    // Load data when program starts
    private static void loadData() {

        File file = new File(FILE_NAME);

        if (!file.exists()) {
            System.out.println("No previous parking data found.");
            return;
        }

        try (BufferedReader reader =
                new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split("\\|", 2);

                if (parts.length != 2) {
                    continue;
                }

                String type = parts[0];
                String vehicle = parts[1].trim();

                if (!isValidVehicle(vehicle)) {
                    continue;
                }

                if (type.equals("P")) {

                    if (parkedVehicles.size() < CAPACITY
                            && !vehicleMap.containsKey(vehicle)) {

                        parkedVehicles.push(vehicle);
                        vehicleMap.put(vehicle, "Parked");
                    }

                } else if (type.equals("W")) {

                    if (!vehicleMap.containsKey(vehicle)) {

                        waitingVehicles.offer(vehicle);
                        vehicleMap.put(vehicle, "Waiting");
                    }
                }
            }

            updateHashMap();

            System.out.println("Parking data loaded successfully.");

        } catch (IOException e) {

            System.out.println(
                "Error loading data: " + e.getMessage()
            );
        }
    }

    // Main menu
    public static void main(String[] args) {

        loadData();

        int choice = 0;

        do {

            System.out.println("\n================================");
            System.out.println("       PARKING LOT MANAGER");
            System.out.println("================================");
            System.out.println("1. Park Vehicle");
            System.out.println("2. Exit Vehicle");
            System.out.println("3. Search Vehicle");
            System.out.println("4. Display Parking Status");
            System.out.println("5. Save and Exit");
            System.out.println("================================");

            System.out.print("Enter your choice: ");

            try {
                choice = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid menu option.");
                continue;
            }

            switch (choice) {

                case 1:
                    parkVehicle();
                    break;

                case 2:
                    exitVehicle();
                    break;

                case 3:
                    searchVehicle();
                    break;

                case 4:
                    displayParking();
                    break;

                case 5:
                    saveData();
                    System.out.println(
                        "Data saved. Exiting program. Thank you!"
                    );
                    break;

                default:
                    System.out.println("Invalid choice! Try again.");
            }

        } while (choice != 5);

        sc.close();
    }
}