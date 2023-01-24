import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hoppers have a collection of presents.
 * Hoppers are associated with a conveyor belt, and have a speed of working.
 * According to its pre-set speed of working, at appropriate intervals until it is empty, a hopper will
 * attempt to place presents onto the conveyor belt – as long as there is space on the belt.
 * @author Jacob
 * @author Nick
 */
@ThreadSafe
public class Hopper extends Thread
{
    /**
     * The Hopper ID.
     */
    private final int id;
    /**
     * The Conveyor to place Presents into.
     */
    private final Conveyor belt;
    /**
     * The number of Presents the Hopper deposits per second.
     */
    private final int speed;

    /**
     * Contains the Presents.
     */
    @GuardedBy("this")
    private final Present[] collection;

    /**
     * Count of Presents contained in the array.
     */
    @GuardedBy("this")
    private int numPresents;

    /**
     * Hold a global count of all presents instead of trying to acquire locks for every Hopper when computing count.
     */
    @GuardedBy("Hopper")
    private static int totalPresents = 0;

    /**
     * Holds total time waiting for buffer to become free.
     */
    private long waitingTime = 0;

    private static final Logger LOGGER = Logger.getLogger(Hopper.class.getName());

    synchronized static public void setLoggerLevel(Level level) {
        LOGGER.setLevel(level);
    }

    /**
     * @return The ID of the Hopper.
     */
    public int getHopperId() {
        return id;
    }

    synchronized private static void increaseTotal() {
        ++totalPresents;
    }

    synchronized private static void decreaseTotal() {
        --totalPresents;
    }

    /**
     * Get the global amount of presents inside all Hoppers.
     * @return The amount.
     */
    synchronized public static int getTotalPresents() {
        return totalPresents;
    }

    /**
     * Query that the Hopper can fit the number of Presents given.
     * @param count The number of Presents to fit.
     * @return True if that amount of Presents would fit.
     */
    synchronized public boolean canFit(int count) {
        return collection.length >= count;
    }

    /**
     * @return Current number of Presents contained in the instance.
     */
    synchronized public int count() {
        return numPresents;
    }

    /**
     * @return The number of presents that left the hopper.
     */
    synchronized public int presentsDeposited() {
        return collection.length - numPresents;
    }

    synchronized public long getWaitingTime() {
        return waitingTime/1000;
    }

    /**
     * Create a Hopper.
     * @param id The Hopper ID.
     * @param con The Conveyor to place Presents into.
     * @param capacity The maximum amount of Presents the Hopper can fit.
     * @param speed The number of Presents to be deposited every second.
     */
    public Hopper(int id, Conveyor con, int capacity, int speed)
    {
        collection = new Present[capacity];
        this.id = id;
        belt = con;
        this.speed = speed;
        numPresents = 0;
    }

    /**
     * Put Present in Hopper collection.
     * @param p Present to add.
     * @exception IndexOutOfBoundsException When the max capacity has been exceeded.
     */
    synchronized public void fill(Present p) throws IndexOutOfBoundsException
    {
        if (numPresents < collection.length){
            //System.out.println("Inserting present " + p.destination() + " at position " + numPresents);
            collection[numPresents] = p;
            numPresents++;
            increaseTotal();
        } else {
            throw new IndexOutOfBoundsException(ErrorCodes.HOPPER_AT_CAPACITY.getMsg());
        }
    }

    /**
     * Continuously attempt to place any presents in the Conveyor belt at the determined speed.
     */
    @Override
    public void run() {
        // Fill should not be called once the thread is active, therefore it should be synchronized
        for (Present present : collection){
            // Skip presents after thread is interrupted
            if (!this.isInterrupted()) {
                try {
                    sleep(1000 / speed);

                    // Start timer
                    long startTime = System.currentTimeMillis();

                    LOGGER.log(Level.INFO, String.format("Hopper %s deposited item (%s)", id, present.destination()));
                    belt.putPresent(present);

                    // Record wait
                    waitingTime += (System.currentTimeMillis() - startTime);

                    --numPresents;
                    decreaseTotal();
                } catch (InterruptedException e) {
                    System.err.printf("The hopper %s was stopped before it finished depositing presents.%n", id);
                    return;
                }
            }
        }

        if (!this.isInterrupted()) {
            LOGGER.log(Level.INFO, "Hopper " + id + " stopped because it ran out of presents.");
        }

    }

    /**
     * A factory that parses a string and constructs a Hopper object.
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

        LOGGER.log(Level.INFO, "Set up Hopper " + id);

        return new Hopper(id, belts[belt - 1], capacity, speed);
    }
}