import net.jcip.annotations.GuardedBy;

import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jacob
 * @author Nick
 */
public class Turntable extends Thread
{
    private final String id;

    static final int N = 0;
    static final int E = 1;
    static final int S = 2;
    static final int W = 3;

    private final Connection[] connections = new Connection[4];

    // global lookup: age-range -> SackID
    @GuardedBy("Turntable")
    private static final HashMap<String, Integer> destinations = new HashMap<>();

    // this individual table's lookup: SackID -> output port
    private final HashMap<Integer, Integer> outputMap = new HashMap<>();

    private static final int ROTATION_DELAY = 500;
    private static final int MOVE_DELAY = 750;

    @GuardedBy("this")
    private boolean hasPresent = false;

    @GuardedBy("this")
    private boolean blocked = false;

    private static final Logger LOGGER = Logger.getLogger(Turntable.class.getName());

    synchronized static public void setLoggerLevel(Level level) {
        LOGGER.setLevel(level);
    }

    synchronized public int count() {
        return hasPresent ? 1 : 0;
    }

    synchronized boolean isBlocked() {
        return blocked;
    }

    /**
     * Create a new Turntable.
     * @param ID The ID of the Turntable.
     */
    public Turntable (String ID)
    {
        id = ID;
    }

    /**
     * Add a new destination to the global lookup.
     * @param key The destination age-range.
     * @param destination The Turntable ID?
     * @return Result of put operation.
     */
    synchronized public static Integer addDestination(String key, int destination) {
        return destinations.put(key, destination);
    }

    /**
     * Add a local connection to the Turntable.
     * @param port The port to connect to.
     * @param conn A Connection object as a proxy to the connection.
     * @see Connection
     */
    synchronized public void addConnection(int port, Connection conn) {
        connections[port] = conn;

        if (conn != null) {
            if(conn.connType == ConnectionType.OutputBelt) {
                for (Integer destination : conn.belt.getDestinations()) {
                    outputMap.put(destination, port);
                }
            }
            else if(conn.connType == ConnectionType.OutputSack) {
                outputMap.put(conn.sack.getSackId(), port);
            }
        }
    }

    public void run() {
        // Run until thread is interrupted
        while (!this.isInterrupted()) {
            // Iterate over all ports
            for (int port = 0; port < connections.length; ++port) {
                Connection conn = connections[port];

                // If port has an input connection
                if (conn != null) {
                    if (conn.connType == ConnectionType.InputBelt) {
                        // Check if it has any Presents
                        if (!conn.belt.isEmpty()) {
                            // This condition won't change since one Conveyor can only be emptied by this Turntable.
                            try {
                                Present present = conn.belt.takePresent();
                                hasPresent = true;

                                final String dest = present.destination();

                                // Check if destination can be reached here
                                final int outputPort = outputMap.get(destinations.get(dest));

                                // Move present in
                                move();
                                // Turn if necessary
                                turn(port, outputPort);
                                // Move present out
                                move();

                                // Put present in either Sack or Conveyor
                                if (connections[outputPort].connType == ConnectionType.OutputSack) {
                                    // Will wait until Sack is empty
                                    connections[outputPort].sack.putPresent(present);
                                } else {
                                    connections[outputPort].belt.putPresent(present);
                                }

                                hasPresent = false;


                            } catch (InterruptedException e) {
                                System.out.println("Turntable " + id + " is stopping.");
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    void turn(int inputPort, int outputPort) throws InterruptedException {
        if (Math.abs(inputPort - outputPort) % 2 != 0) {
            sleep(ROTATION_DELAY);
        }
    }

    void move() throws InterruptedException {
        sleep(MOVE_DELAY);
    }

    /**
     * Factory that constructs a Turntable from a String.
     * @param line String containing data about Turntable.
     * @param belts An array of Conveyor objects that the Turntable may connect with.
     * @param sacks An array of Sack objects that the Turntable may connect with.
     * @return The newly constructed Turntable.
     * @see Sack
     * @see Conveyor
     */
    public static Turntable parseString(String line, Conveyor[] belts, Sack[] sacks) {
        Scanner inputStream = new Scanner(line);
        String tableId = inputStream.next();
        Turntable turntable = new Turntable(tableId);
        int connId = 0;

        for(int i = 0; i < 4; i++){
            inputStream.next(); // Skip direction
            String type = inputStream.next();

            if (!"null".equals(type)){
                connId = inputStream.nextInt();

                Connection connection = null;
                switch (type) {
                    case "os":
                        connection = new Connection(ConnectionType.OutputSack, null, sacks[connId - 1]);
                        break;
                    case "ib":
                        connection = new Connection(ConnectionType.InputBelt, belts[connId - 1], null);
                        break;
                    case "ob":
                        connection = new Connection(ConnectionType.OutputBelt, belts[connId - 1], null);
                        break;
                }
                turntable.addConnection(i, connection);
            }
        }

        LOGGER.log(Level.INFO, "Set up Turntable " + tableId);
        return turntable;
    }
}
