/**
 *
 * @author Jacob
 * @author Nick
 */
public class Present
{
    private final String ageRange;

    public Present(String destination)
    {
        ageRange = destination;
    }

    synchronized public String destination()
    {
        return ageRange;
    }

}