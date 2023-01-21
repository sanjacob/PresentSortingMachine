import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

/**
 * Represents a queue of presents
 * @author Nick
 * @author Jacob
 */
public class Conveyor
{
    private final int id;
    private final Present[] presents; // The requirements say this must be a fixed size array
    private final HashSet<Integer> destinations = new HashSet<>();

    /**
     * Index of next element to be placed
     */
    private int top;

    // TODO - add more members?

    public int getId() {
        return id;
    }

    /**
     * Constructs a Conveyor instance from an ID and size.
     * @param id ID of the Conveyor.
     * @param size Maximum capacity of the queue.
     */
    public Conveyor(int id, int size)
    {
        this.id = id;
        presents = new Present[size];
        top = 0;

        //TODO - more construction likely!
    }

    /**
     * Adds a hopper to the map of possible destinations.
     * @param hopperID The ID of the hopper.
     */
    public void addDestination(int hopperID)
    {
        destinations.add(hopperID);
    }

    /**
     * The destinations of the Conveyor.
     * @return The HashSet containing the possible destinations.
     */
    public HashSet<Integer> getDestinations() {
        return new HashSet<>(destinations);
    }

    /**
     * Check if conveyor is full and cannot accept any more presents.
     * @return True if the conveyor has enough space.
     */
    synchronized public boolean hasSpace() {
        return top < presents.length;
    }

    /**
     * Attempt to put a Present in the Conveyor queue.
     * @param present The Present to place in the queue.
     * @return True if there is space for the Present, otherwise false.
     */
    synchronized public boolean putPresent(Present present) {
        if (hasSpace()) {
            // Append item and increase count
            presents[top] = present;
            top++;
        }

        return hasSpace();
    }

    /**
     * Moves all elements of the queue forward, overwriting the element at the front.
     * @return Present at front of queue, or null if non-existent.
     */
    synchronized public Present popPresent() {
        Present head = null;

        if (top > 0) {
            head = presents[0];
            // Shift elements (overwrite previous head)
            for (int i = 1; i < top; ++i) {
                presents[i-1] = presents[i];
            }
            top--;
        }

        return head;
    }

    /**
     * A factory function that parses a string and constructs a Conveyor object with its destinations.
     * @param line The string to parse (e.g. 1 length 5 destinations 1 2).
     * @return A newly constructed Conveyor.
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
