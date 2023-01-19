import java.util.Scanner;

/**
 *
 * @author Nick
 */
public class Sack
{
    int id;
    Present[] accumulation;

    String ageRange;
    private int numPresents;

    public Sack(int id, int capacity, String ageRange)
    {
        accumulation = new Present[capacity];
        this.id = id;
        this.ageRange = ageRange;
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

    synchronized public boolean isFull() {
        return numPresents >= accumulation.length;
    }

    //TODO - Add more methods

}
