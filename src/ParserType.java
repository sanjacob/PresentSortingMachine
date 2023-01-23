/**
 * Represents a different section of the configuration file.
 */
public enum ParserType {
    BELTS(0),
    HOPPERS(1),
    SACKS(2),
    TURNTABLES(3),
    PRESENTS(4),
    TIMER(5);

    private final int index;
    ParserType(int index) {
        this.index = index;
    }
}
