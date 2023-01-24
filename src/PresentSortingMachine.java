import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

/**
 * Represents an instance of the Present Sorting Machine.
 * @author Jacob <jsanchez-perez@uclan.ac.uk>
 */
public class PresentSortingMachine {
    private Conveyor[] belts;
    private Hopper[] hoppers;
    private Sack[] sacks;
    private Elf elf;
    private Turntable[] tables;

    private int timerLength;
    private int totalPresents;
    private final String configFile;
    private long startTime = 0;
    private long endTime = 0;

    /**
     * The number of milliseconds to wait between updates.
     */
    private final int WAIT_INTERVAL = 10000;

    private final int SHUTDOWN_WAIT = 4000;

    private static final Logger LOGGER = Logger.getLogger(PresentSortingMachine.class.getName());

    synchronized static public void setLoggerLevel(Level level) {
        LOGGER.setLevel(level);
    }

    /**
     * Create a new Present Sorting Machine instance
     * @param fileName A configuration file to start the machine to.
     */
    public PresentSortingMachine(String fileName) {
        configFile = fileName;
        parseFile(fileName);

        // Create elves
        elf = new Elf(1, sacks);
    }

    public void run() throws InterruptedException {
        System.out.println("Starting Hoppers and Turntables...");

        startHoppersAndTables();

        long time = 0;
        long currentTime = 0;
        startTime = System.currentTimeMillis();
        System.out.println("*** Machine Started at ***");

        while (time < timerLength)
        {
            // sleep in 10 second bursts
            try
            {
                sleep(WAIT_INTERVAL);
            }
            catch (InterruptedException ex)
            {
                throw new RuntimeException(ex);
            }

            currentTime = System.currentTimeMillis();
            time = (currentTime - startTime) / 1000;
            System.out.println("\nInterim Report @ " + time + "s:");

            System.out.println(Hopper.getTotalPresents() + " presents remaining in hoppers;");
            System.out.println(Sack.getPresentsCollected() + " presents sorted into sacks.\n");
        }

        endTime = System.currentTimeMillis();

        System.out.println("*** Input Stopped after " + (endTime - startTime) / 1000 + "s. ***");
        stopHoppers();

        System.out.println("*** Releasing the Elves to empty the Sacks. ***");
        startElves();

        // Wait until Sack has finished
        while (!((totalPresents - Hopper.getTotalPresents()) == Sack.getPresentsCollected())) {
            System.out.println(Sack.getPresentsCollected() + " have reached the Sacks out of " + totalPresents);
            sleep(SHUTDOWN_WAIT);
        }

        stopTurntables();
        stopElves();

        // Wait until Sacks are full or all presents have been deposited
        joinHoppersAndTables();
        joinElves();
        endTime = System.currentTimeMillis();
        System.out.println("*** Machine completed shutdown after " + (endTime - startTime) / 1000 + "s. ***");

    }

    /**
     * Loop over all hoppers and turntables and start them.
     */
    private void startHoppersAndTables() {
        // Start turntables before hoppers since they are going to wait anyway.

        // START the turntables!
        for (Turntable table : tables) {
            table.start();
        }

        // START the hoppers!
        for (Hopper hopper : hoppers) {
            hopper.start();
        }
    }

    private void startElves() {
        elf.start();
    }

    private void stopElves() {
        elf.interrupt();
    }

    /**
     * Loop over all hoppers and interrupt them.
     */
    private void stopHoppers() {
        // Interrupt the hoppers!
        for (Hopper hopper : hoppers) {
            hopper.interrupt();
        }
    }

    /**
     * Loop over all turntables and interrupt them.
     */
    private void stopTurntables() {
        // START the turntables!
        for (Turntable table : tables) {
            table.interrupt();
        }
    }

    /**
     * Loop over all hoppers and turntables and wait for them to stop.
     */
    private void joinHoppersAndTables() throws InterruptedException {
        // START the hoppers!
        for (Hopper hopper : hoppers) {
            hopper.join();
        }

        // START the turntables!
        for (Turntable table : tables) {
            table.join();
        }
    }

    private void joinElves() throws InterruptedException {
        elf.join();
    }

    /**
     * Parse a configuration file following a specific format.
     * @param fileName The file name of the config file.
     */
    public void parseFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int itemIndex = 0;
            int sectionIndex = 1;
            ParseStage stage = ParseStage.CLEAR;
            ParserType parserType = ParserType.BELTS;

            while ((line = br.readLine()) != null) {
                switch(stage) {
                    case CLEAR:
                        String[] sectionHeader = line.trim().split(" ", 2);
                        parserType = ParserType.valueOf(sectionHeader[0]);
                        sectionIndex = (sectionHeader.length > 1) ? Integer.parseInt(sectionHeader[1]) : 1;
                        stage = ParseStage.COUNT;

                        if (parserType == ParserType.TIMER) {
                            stage = ParseStage.CLEAR;
                            timerLength = sectionIndex;
                            LOGGER.log(Level.INFO, "The machine will run for " + timerLength + "s");
                        }
                        break;
                    case COUNT:
                        setCount(Integer.parseInt(line), parserType, sectionIndex);
                        stage = ParseStage.CONTENT;
                        break;
                    case CONTENT:
                        if (line.isBlank()) {
                            stage = ParseStage.CLEAR;
                            itemIndex = 0;
                        } else {
                            parseLine(line, parserType, itemIndex, sectionIndex);
                            itemIndex++;
                        }
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println(ErrorCodes.INPUT_FILE_IO_ERROR.getMsg());
            System.exit(ErrorCodes.INPUT_FILE_IO_ERROR.getValue());
        }
    }

    /**
     * @return String representation of the machine setup (not exhaustive).
     */
    @Override
    public String toString() {
        return String.format("PSM (%d s): %d belts, %d hoppers, %d sacks, %d turntables.", timerLength,
                belts.length, hoppers.length, sacks.length, tables.length);
    }

    /**
     * Decides what to do with the second line in a section of the configuration file.
     * @param count The integer parsed from the line.
     * @param parserType The type of Object being parsed.
     * @param sectionIndex The index of the section.
     * @see ParserType
     */
    private void setCount(int count, ParserType parserType, int sectionIndex) {
        switch (parserType) {
            case BELTS:
                belts = new Conveyor[count];
                break;
            case HOPPERS:
                hoppers = new Hopper[count];
                break;
            case SACKS:
                sacks = new Sack[count];
                break;
            case TURNTABLES:
                tables = new Turntable[count];
                break;
            case PRESENTS:
                // Assert it can fit in the hopper
                if (!hoppers[sectionIndex - 1].canFit(count)) {
                    throw new IndexOutOfBoundsException(ErrorCodes.HOPPER_AT_CAPACITY.getMsg());
                }
                break;
        }
    }

    /**
     * Decides what to do with a line of the configuration file.
     * @param line The line from the configuration file.
     * @param parserType The type of Object being parsed.
     * @param itemIndex The number of line inside this section.
     * @param sectionIndex The section number.
     * @see ParserType
     */
    private void parseLine(String line, ParserType parserType, int itemIndex, int sectionIndex) {
        switch (parserType) {
            case BELTS:
                if (belts.length < itemIndex) {throw new IndexOutOfBoundsException(ErrorCodes.ITEM_OUT_OF_RANGE.getMsg());}
                belts[itemIndex] = Conveyor.parseString(line);
                break;
            case HOPPERS:
                if (hoppers.length < itemIndex) {throw new IndexOutOfBoundsException(ErrorCodes.ITEM_OUT_OF_RANGE.getMsg());}
                hoppers[itemIndex] = Hopper.parseString(line, belts);
                break;
            case SACKS:
                if (sacks.length < itemIndex) {throw new IndexOutOfBoundsException(ErrorCodes.ITEM_OUT_OF_RANGE.getMsg());}
                sacks[itemIndex] = Sack.parseString(line);
                Turntable.addDestination(sacks[itemIndex].getAgeRange(), sacks[itemIndex].getSackId());
                break;
            case TURNTABLES:
                if (tables.length < itemIndex) {throw new IndexOutOfBoundsException(ErrorCodes.ITEM_OUT_OF_RANGE.getMsg());}
                tables[itemIndex] = Turntable.parseString(line, belts, sacks);
                break;
            case PRESENTS:
                hoppers[sectionIndex - 1].fill(new Present(line));
                ++totalPresents;
                break;
        }
    }

    /**
     * Get count of all gifts held in belts.
     * Since other threads are not running getting the lock is not problematic.
     * @return The number of gifts.
     */
    private int getConveyorCount() {
        int count = 0;
        for (Conveyor conveyor : belts) {
            count += conveyor.getCount();
        }

        return count;
    }

    /**
     * Get count of all gifts held in Turntables.
     * Since other threads are not running getting the lock is not problematic.
     * @return The number of gifts.
     */
    private int getTurntableCount() {
        int count = 0;
        for (Turntable turntable : tables) {
            count += turntable.count();
        }

        return count;
    }

    /**
     * Prints a final report of the machine activity.
     */
    public void printReport() {
        System.out.println();
        System.out.println("\nFINAL REPORT\n");
        System.out.println("Configuration: " + configFile);
        System.out.println("Total Run Time: " + (endTime - startTime) / 1000 + "s.");

        for (Hopper hopper : hoppers) {
            System.out.print("Hopper " + hopper.getHopperId() + " deposited " + hopper.presentsDeposited());
            System.out.println(" presents and waited " + hopper.getWaitingTime() + "s.");
        }

        System.out.println();

        // Sum gifts in Hoppers, Conveyor belts, and Turntables
        int giftsOnMachine = Hopper.getTotalPresents() + getConveyorCount() + getTurntableCount();
        int giftsInSacks = Sack.getPresentsCollected();

        int deposited = totalPresents - Hopper.getTotalPresents();

        System.out.println("\nOut of " + deposited + " gifts deposited, and " + totalPresents + " gifts in total:");
        System.out.print(giftsOnMachine + " are still on the machine (incl. Hoppers), and ");
        System.out.println(giftsInSacks + " made it into the sacks");

        int missing = totalPresents - giftsInSacks - giftsOnMachine;
        System.out.println(missing + " gifts went missing.");

    }
}
