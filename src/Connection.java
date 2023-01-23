import net.jcip.annotations.Immutable;

/**
 * @author Jacob
 * @author npmitchell
 */
@Immutable
public class Connection {
    public final ConnectionType connType;
    public final Conveyor belt;
    public final Sack sack;

    public Connection(ConnectionType ct, Conveyor c, Sack s)
    {
        connType = ct;
        belt = c;
        sack = s;
    }
}
