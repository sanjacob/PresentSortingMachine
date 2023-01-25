import java.util.logging.Level;

public class Main {
    /**
     * Creates a PresentSortingMachine and starts it, then prints its report.
     * @param args Command-line arguments (unused).
     */
    public static void main(String[] args) {
        String filePath = "scenarios/scenario5.txt";
        setLoggingLevels();

        // Read in file
        var machine = new PresentSortingMachine(filePath);
        try {
            machine.run();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        machine.printReport();
    }

    private static void setLoggingLevels() {
        Conveyor.setLoggerLevel(Level.WARNING);
        Elf.setLoggerLevel(Level.WARNING);
        Sack.setLoggerLevel(Level.WARNING);
        Turntable.setLoggerLevel(Level.WARNING);
        Hopper.setLoggerLevel(Level.WARNING);
    }
}