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

    public Hopper(int id, Conveyor con, int capacity, int speed)
    {
        collection = new Present[capacity];
        this.id = id;
        belt = con;
        this.speed = speed;
    }

    public void fill(Present p)
    {
        // TODO
    }

    public void run()
    {
        // TODO
    }

    // TODO Add more methods?
}