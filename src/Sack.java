import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.util.Scanner;

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
    private static int totalPresents = 0;

    /**
     * Increase global count of Presents held in Sacks.
     */
    synchronized private static void increaseTotal() {
        ++totalPresents;
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
     * @return The amount.
     */
    synchronized public static int getTotalPresents() {
        return totalPresents;
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
     * @apiNote Does not check index is inside bounds.
     * @param present The Present to place in the Sack.
     */
    synchronized public void putPresent(Present present) {
        // Append item and increase count
        accumulation[numPresents] = present;
        numPresents++;
        increaseTotal();
    }

    /**
     * Resets array count, does not null-out elements.
     */
    synchronized public void clear() {
        numPresents = 0;
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
        return new Sack(id, capacity, age);
    }


}
