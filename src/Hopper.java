import java.util.Scanner;

/**
 *
 * @author Nick
 */
public class Hopper extends Thread
{
    int id;
    Conveyor belt;
    int speed;

    Present[] collection;
    private int numPresents;

    public Hopper(int id, Conveyor con, int capacity, int speed)
    {
        collection = new Present[capacity];
        this.id = id;
        belt = con;
        this.speed = speed;
        numPresents = 0;
    }

    public void fill(Present p)
    {
        if (numPresents < collection.length){
            System.out.println("Inserting present " + p.destination() + " at position " + numPresents);
            collection[numPresents] = p;
            numPresents++;
        }
    }

    @Override
    public void run()
    {
        // TODO
    }

    /** A factory that parses a string and constructs a Hopper object.
     * @param line The string to parse (e.g. 1 belt 1 capacity 10 speed 1)
     * @param belts An array of conveyor belts of the system
     */
    public static Hopper parseString(String line, Conveyor[] belts) {
        Scanner hopperStream = new Scanner(line);

        int id = hopperStream.nextInt();
        hopperStream.next(); // skip "belt"

        int belt = hopperStream.nextInt();
        hopperStream.next(); // skip "capacity"

        int capacity = hopperStream.nextInt();
        hopperStream.next(); // skip "speed"

        int speed = hopperStream.nextInt();

        return new Hopper(id, belts[belt - 1], capacity, speed);
    }

    // TODO Add more methods?
}