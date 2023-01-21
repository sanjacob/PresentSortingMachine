import java.util.Scanner;

/**
 *
 * @author Nick
 */
public class Hopper extends Thread
{
    private final int id;
    private final Conveyor belt;
    private final int speed;

    private final Present[] collection;
    private int numPresents;

    synchronized public int getHopperId() {
        return id;
    }

    synchronized public boolean canFit(int count) {
        return collection.length >= count;
    }

    synchronized public int count() {
        return numPresents;
    }

    public Hopper(int id, Conveyor con, int capacity, int speed)
    {
        collection = new Present[capacity];
        this.id = id;
        belt = con;
        this.speed = speed;
        numPresents = 0;
    }

    synchronized public void fill(Present p)
    {
        if (numPresents < collection.length){
            //System.out.println("Inserting present " + p.destination() + " at position " + numPresents);
            collection[numPresents] = p;
            numPresents++;
        }
    }

    @Override
    public void run()
    {
        synchronized (this) {
            // While hopper has gifts
            while (numPresents > 0) {
                // Attempt to put in conveyor
                if (belt.putPresent(collection[numPresents - 1])) {
                    numPresents--;
                } else {
                    // Release mutex until there is more space
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                try {
                    sleep(1000/speed);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
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