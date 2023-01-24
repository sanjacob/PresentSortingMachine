import java.util.logging.Level;
import java.util.logging.Logger;

public class Elf extends Thread {
    private final int id;

    /** Every Sack the Elf has to empty. */
    private final Sack[] sacks;

    /** Time to wait for each sack to get filled. */
    private final int SACK_WAIT = 400;

    private static final Logger LOGGER = Logger.getLogger(Elf.class.getName());

    synchronized static public void setLoggerLevel(Level level) {
        LOGGER.setLevel(level);
    }

    public Elf(int id, Sack[] sacks) {
        this.id = id;
        this.sacks = sacks;
    }

    /**
     * Periodically empty Sacks.
     */
    @Override
    public void run() {
        while (!isInterrupted()) {
            for (Sack sack : sacks) {
                try {
                    // Wait on sack for some time, then move on to next sack.
                    boolean emptied = sack.empty(SACK_WAIT);

                    if (emptied) {
                        LOGGER.log(Level.INFO, "Elf " + id + " emptied Sack " + sack.getSackId());
                    }
                } catch (InterruptedException e) {
                    // By convention, any method that exits by throwing an InterruptedException clears interrupt status when it does so.
                    // However, it's always possible that interrupt status will immediately be set again, by another thread invoking interrupt.
                    System.out.printf("Elf %s was stopped while waiting.%n", id);
                    return;
                }
            }
        }
    }
}
