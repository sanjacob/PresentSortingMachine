import java.util.Scanner;

/**
 *
 * @author Jacob
 * @author Nick
 */
public class Sack
{
    private final int id;
    private final Present[] accumulation;

    private final String ageRange;

    /**
     * Index of next element to be placed
     */
    private int numPresents;

    synchronized public int getSackId() {
        return id;
    }

    synchronized public String getAgeRange() {
        return ageRange;
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

    /** A factory that parses a string and constructs a Sack object.
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

    /**
     * Check if Sack is full and cannot accept any more presents.
     * @return True if the Sack has enough space.
     */
    synchronized public boolean hasSpace() {
        return numPresents < accumulation.length;
    }

    /**
     * Attempt to put a Present in the Sack.
     * @param present The Present to place in the Sack.
     * @return True if there is space for the Present, otherwise false.
     */
    synchronized public boolean putPresent(Present present) {
        if (hasSpace()) {
            // Append item and increase count
            accumulation[numPresents] = present;
            numPresents++;
        }

        return hasSpace();
    }

    /**
     * Removes all Presents from the collection.
     */
    synchronized public void clear() {
        numPresents = 0;
    }

    //TODO - Add more methods

}
