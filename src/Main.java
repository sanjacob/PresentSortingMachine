public class Main {
    public static void main(String[] args) {
        String filePath = "scenario5.txt";

        // Read in file
        var machine = new PresentSortingMachine(filePath);
        machine.run();
        machine.printReport();
    }
}