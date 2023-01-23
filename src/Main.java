import java.util.logging.Level;

public class Main {
    /**
     * Creates a PresentSortingMachine and starts it, then prints its report.
     * @param args Command-line arguments (unused).
     */
    public static void main(String[] args) {
        String filePath = "scenario1.txt";
        Conveyor.setLoggerLevel(Level.WARNING);

        // Read in file
        var machine = new PresentSortingMachine(filePath);
        try {
            machine.run();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        machine.printReport();
    }
}