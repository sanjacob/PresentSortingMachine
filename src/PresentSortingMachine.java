import java.io.*;

import static java.lang.Thread.sleep;

public class PresentSortingMachine {
    private Conveyor[] belts;
    private Hopper[] hoppers;
    private Sack[] sacks;
    private Turntable[] tables;

    private int timerLength;
    private String configFile;
    private long startTime = 0;
    private long endTime = 0;


    public PresentSortingMachine(String fileName) {
        configFile = fileName;
        parseFile(fileName);
    }

    public void run() {
        // START the hoppers!
        for (int h = 0; h < hoppers.length; h++)
        {
            hoppers[h].start();
        }

        // START the turntables!
        for (int t = 0; t < hoppers.length; t++)
        {
            tables[t].start();
        }

        long time = 0;
        long currentTime = 0;
        startTime = System.currentTimeMillis();
        System.out.println("*** Machine Started ***");
        while (time < timerLength)
        {
            // sleep in 10 second bursts
            try
            {
                sleep(10000);
            }
            catch (InterruptedException ex)
            {
            }

            currentTime = System.currentTimeMillis();
            time = (currentTime - startTime) / 1000;
            System.out.println("\nInterim Report @ " + time + "s:");

            int giftsInSacks = 0;
            // TODO - calculate number

            int giftsInHoppers = 0;
            // TODO - calculate number

            System.out.println(giftsInHoppers + " presents remaining in hoppers;\n" + giftsInSacks + " presents sorted into sacks.\n");

        }
        endTime = System.currentTimeMillis();
        System.out.println("*** Input Stopped after " + (endTime - startTime) / 1000 + "s. ***");

        // TODO
        // Stop the hoppers!
        // Stop the tables!
        // HINT - Wait for everything to finish...

        endTime = System.currentTimeMillis();
        System.out.println("*** Machine completed shutdown after " + (endTime - startTime) / 1000 + "s. ***");

    }

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

    @Override
    public String toString() {
        return String.format("PSM (%d s): %d belts, %d hoppers, %d sacks, %d turntables.", timerLength,
                belts.length, hoppers.length, sacks.length, tables.length);
    }

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
                if (hoppers[sectionIndex - 1].collection.length < count) {
                    throw new IndexOutOfBoundsException(ErrorCodes.HOPPER_AT_CAPACITY.getMsg());
                }
                break;
        }
    }

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
                Turntable.destinations.put(sacks[itemIndex].ageRange, sacks[itemIndex].id);
                break;
            case TURNTABLES:
                if (tables.length < itemIndex) {throw new IndexOutOfBoundsException(ErrorCodes.ITEM_OUT_OF_RANGE.getMsg());}
                tables[itemIndex] = Turntable.parseString(line, belts, sacks);
                break;
            case PRESENTS:
                hoppers[sectionIndex - 1].fill(new Present(line));
                break;
        }
    }

    public void printReport() {
        System.out.println();
        System.out.println("\nFINAL REPORT\n");
        System.out.println("Configuration: " + configFile);
        System.out.println("Total Run Time: " + (endTime - startTime) / 1000 + "s.");

        int giftsDeposited = 0;
        // TODO - calculate this number!

        for (Hopper hopper : hoppers) {
            System.out.println("Hopper " + hopper.id + " deposited " + /* TODO */ " presents and waited " + /* TODO */ "s.");
        }
        System.out.println();

        int giftsOnMachine = 0;
        int giftsInSacks = 0;
        // TODO - calculate these numbers!

        System.out.print("\nOut of " + giftsDeposited + " gifts deposited, ");
        System.out.print(giftsOnMachine + " are still on the machine, and ");
        System.out.println(giftsInSacks + " made it into the sacks");

        int missing = giftsDeposited - giftsInSacks - giftsOnMachine;
        System.out.println(missing + " gifts went missing.");

    }
}
