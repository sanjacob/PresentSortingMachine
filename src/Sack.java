import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sacks should be implemented as a fixed size array, and act as a buffer for depositing Presents.
 * @author Jacob
 * @author Nick
 */
@ThreadSafe
public class Sack
{
    /** ID of Sack */
    private final int id;

    /** Array containing Presents */
    private final Present[] accumulation;

    /** Age range of presents contained in Sack */
    private final String ageRange;

    /** Number of presents contained */
    @GuardedBy("this")
    private int numPresents;

    /**
     * Hold a global count of all presents instead of trying to acquire locks for every Sack when computing count.
     */
    @GuardedBy("Sack")
    private static int presentsCollected = 0;

    private static final Logger LOGGER = Logger.getLogger(Sack.class.getName());

    synchronized static public void setLoggerLevel(Level level) {
        LOGGER.setLevel(level);
    }

    /**
     * Increase global count of Presents held in Sacks.
     */
    synchronized private static void increaseTotal() {
        ++presentsCollected;
    }

    /**
     * @return The ID of the Sack.
     */
    public int getSackId() {
        return id;
    }

    /**
     * @return Obtain the age range of the sack.
     */
    public String getAgeRange() {
        return ageRange;
    }

    /**
     * Get the global amount of presents inside all Sacks.
     *
     * @return The amount.
     */
    synchronized public static int getPresentsCollected() {
        return presentsCollected;
    }

    /**
     * @return The number of presents in the sack.
     */
    synchronized public int count() {
        return numPresents;
    }

    public Sack(int id, int capacity, String ageRange)
    {
        accumulation = new Present[capacity];
        this.id = id;
        this.ageRange = ageRange;
        numPresents = 0;
    }


    /**
     * Check if Sack is full and cannot accept any more presents.
     * @return True if the Sack has enough space.
     */
    synchronized public boolean isFull() {
        return numPresents >= accumulation.length;
    }

    /**
     * Puts present in Sack array.
     * @apiNote Waits until Sack is not full.
     * @param present The Present to place in the Sack.
     */
    synchronized public void putPresent(Present present) throws InterruptedException {
        while(isFull()) {
            wait();
        }

        // Append item and increase count
        accumulation[numPresents] = present;
        numPresents++;
        increaseTotal();

        notifyAll();
    }

    /**
     * Resets array count, does not null-out elements.
     */
    synchronized public boolean empty(int timeout) throws InterruptedException {
        if (!isFull()) {
            wait(timeout);
        }

        boolean wasFull = isFull();

        if (wasFull) {
            numPresents = 0;
            notifyAll();
        }

        return wasFull;
    }

    //TODO - Add more methods

    /**
     * A factory that parses a string and constructs a Sack object.
     * @param line The string to parse (e.g. 1 capacity 20 age 0-3)
     */
    public static Sack parseString(String line) {
        Scanner sackStream = new Scanner(line);

        int id = sackStream.nextInt();
        sackStream.next(); // skip "capacity"

        int capacity = sackStream.nextInt();
        sackStream.next(); // skip "age"

        String age = sackStream.next();

        LOGGER.log(Level.INFO, "Set up Sack " + id);
        return new Sack(id, capacity, age);
    }


}
