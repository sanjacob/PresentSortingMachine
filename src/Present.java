import net.jcip.annotations.Immutable;

/**
 * Represents a single present.
 * Present objects are created at the start of the simulation and loaded into the hopper
 * to simulate the toys to be distributed.
 * @author Jacob
 * @author Nick
 */
@Immutable
public class Present
{
    // Helper field to check it is working
    private final int id;
    private final String ageRange;
    private static int count = 0;
    //private final String type;

    /**
     * Creates a Present.
     * @param destination The target age group of the present.
     */
    public Present(String destination)
    {
        ageRange = destination;
        id = count;
        ++count;
    }

    /**
     * The target age range of the Present.
     * @return String containing the age range.
     */
    public String destination()
    {
        return ageRange;
    }

    @Override
    public String toString() {
        return String.format("Present %s (%s)", id, ageRange);
    }
}