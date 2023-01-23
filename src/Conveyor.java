import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.util.HashSet;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a circular buffer queue of presents (self eating snake)
 * @author Nick
 * @author Jacob
 */
@ThreadSafe
public class Conveyor
{
    private final int id;
    private final Present[] presents; // The requirements say this must be a fixed size array
    private final HashSet<Integer> destinations;


    @GuardedBy("this")
    private int head;
    @GuardedBy("this")
    private int tail;
    @GuardedBy("this")
    private int count;

    private static final Logger LOGGER = Logger.getLogger(Turntable.class.getName());

    /**
     * @return ID of Conveyor.
     */
    public int getConveyorId() {
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
        destinations = new HashSet<>();
        tail = 0;
        head = 0;
        count = 0;
        LOGGER.log(Level.INFO, String.format("Created Conveyor %s", id));
    }

    synchronized static public void setLoggerLevel(Level level) {
        LOGGER.setLevel(level);
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
     * @return Current amount of items inside Conveyor.
     */
    synchronized public int getCount() { return count; }

    /**
     * The destinations of the Conveyor.
     * @apiNote Not sure if a shallow copy is problematic, requires testing.
     * @return The HashSet containing the possible destinations.
     */
    public HashSet<Integer> getDestinations() {
        return new HashSet<>(destinations);
    }

    /**
     * Check if conveyor is empty.
     * @return True if the conveyor has no more presents.
     */
    synchronized public boolean isEmpty() {
        return count == 0;
    }

    /**
     * Check if conveyor is full and cannot accept any more presents.
     * @return True if the conveyor is full.
     */
    synchronized public boolean isFull() {
        return count >= presents.length;
    }

    /**
     * Attempt to put a Present in the Conveyor queue.
     * The operation will call `wait()` until there is enough space in the queue.
     * Then, `notifyAll()` will be called once the element was deposited.
     * @param timeout The number of milliseconds to wait before giving up.
     * @param present The Present to place in the queue.
     */
    synchronized public void putPresent(Present present, int timeout) throws InterruptedException {
        // Wait until there is space in the Conveyor
        while (isFull()) {
            wait(timeout);
        }

        // At this point the lock was acquired and there is space in the Conveyor
        // Perform put operation
        put(present);

        // Notify that put operation has finished
        notifyAll();
    }

    /**
     * Attempt to put a Present in the Conveyor queue.
     * The operation will call `wait()` until there is enough space in the queue.
     * Then, `notifyAll()` will be called once the element was deposited.
     * @param present The Present to place in the queue.
     */
    synchronized public void putPresent(Present present) throws InterruptedException {
        // Wait until there is space in the Conveyor
        while (isFull()) {
            wait();
        }

        // At this point the lock was acquired and there is space in the Conveyor
        // Perform put operation
        put(present);

        // Notify that put operation has finished
        notifyAll();
    }

    /**
     * Puts a present in the Conveyor.
     * Not to be used directly.
     * @param present The Present to insert.
     */
    synchronized private void put(Present present) {
        LOGGER.log(Level.INFO, String.format("Putting a present (%s) in belt %s.", present, id));
        // Append item and increase count
        presents[tail] = present;

        // Wrap around (increase before comparing)
        if (++tail == presents.length) {
            tail = 0;
        }
        ++count;
    }

    /**
     * Takes a present from the queue.
     * The operation will call `wait()` until there is a Present to take.
     * Then, `notifyAll()` will be called once operation is completed.
     * @param timeout The number of milliseconds to wait before giving up.
     * @return Present at front of queue.
     */
    synchronized public Present takePresent(int timeout) throws InterruptedException {
        // Wait until the Conveyor contains elements, then re-acquire lock
        while (isEmpty()) {
            wait(timeout);
        }
        Present present = take();
        // Notify there is an element in the Conveyor
        notifyAll();
        return present;
    }

    /**
     * Takes a present from the queue.
     * The operation will call `wait()` until there is a Present to take.
     * Then, `notifyAll()` will be called once operation is completed.
     * @return Present at front of queue.
     */
    synchronized public Present takePresent() throws InterruptedException {
        // Wait until the Conveyor contains elements, then re-acquire lock
        while (isEmpty()) {
            wait();
        }
        Present present = take();
        // Notify there is an element in the Conveyor
        notifyAll();
        return present;
    }

    /**
     * Takes a present out of the Conveyor.
     * Should not be used directly, as it does not check count first.
     * @return The Present just taken.
     */
    synchronized private Present take() {
        // Take element from circular buffer
        Present present = presents[head];
        LOGGER.log(Level.INFO, String.format("Taking a present (%s) from belt %s.", present, id));

        // Don't think strictly necessary
        presents[head] = null;

        // Wrap around (increase before comparing)
        if (++head == presents.length) {
            head = 0;
        }
        --count;

        return present;
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
}
