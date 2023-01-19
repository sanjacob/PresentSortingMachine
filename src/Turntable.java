import java.util.HashMap;
import java.util.Scanner;

/**
 *
 * @author Nick
 */
public class Turntable extends Thread
{
    String id;

    static int N = 0;
    static int E = 1;
    static int S = 2;
    static int W = 3;

    Connection[] connections = new Connection[4];

    // global lookup: age-range -> SackID
    static HashMap<String, Integer> destinations = new HashMap<>();

    // this individual table's lookup: SackID -> output port
    HashMap<Integer, Integer> outputMap = new HashMap<>();

    public Turntable (String ID)
    {
        id = ID;
    }

    public void addConnection(int port, Connection conn)
    {
        connections[port] = conn;

        if(conn != null)
        {
            if(conn.connType == ConnectionType.OutputBelt)
            {
                for (Integer destination : conn.belt.destinations)
                {
                    outputMap.put(destination, port);
                }
            }
            else if(conn.connType == ConnectionType.OutputSack)
            {
                outputMap.put(conn.sack.id, port);
            }
        }
    }

    public void run()
    {
        // TODO
    }

    public static Turntable parseString(String line, Conveyor[] belts, Sack[] sacks) {
        Scanner inputStream = new Scanner(line);
        String tableId = inputStream.next();
        Turntable turntable = new Turntable(tableId);
        int connId = 0;

        for(int i = 0; i < 4; i++){
            String direction = inputStream.next();
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

        return turntable;
    }
}
