import java.util.HashSet;
import java.util.Scanner;

/**
 * Represents a queue of presents
 * @author Nick
 */
public class Conveyor
{
    int id;
    private Present[] presents; // The requirements say this must be a fixed size array
    public HashSet<Integer> destinations = new HashSet();

    private int top = 0;

    // TODO - add more members?

    public Conveyor(int id, int size)
    {
        this.id = id;
        presents = new Present[size];

        //TODO - more construction likely!
    }

    public void addDestination(int hopperID)
    {
        destinations.add(hopperID);
    }


    /** A factory that parses a string and constructs a Conveyor object.
     * @param line The string to parse (e.g. 1 length 5 destinations 1 2)
     */
    public static Conveyor parseString(String line) {
        Scanner beltStream = new Scanner(line);
        int id = beltStream.nextInt();
        beltStream.next(); // skip "length"

        int length = beltStream.nextInt();
        Conveyor conveyor = new Conveyor(id, length);
        beltStream.next(); // skip "destinations"

        while (beltStream.hasNextInt())
        {
            int dest = beltStream.nextInt();
            conveyor.addDestination(dest);
        }
        return conveyor;
    }

    // TODO - add more functions

}
