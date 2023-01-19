public enum ErrorCodes {
    INPUT_FILE_IO_ERROR(1, "Error reading file."),
    HOPPER_AT_CAPACITY(2, "Hopper must be large enough to contain gifts."),
    ITEM_OUT_OF_RANGE(3, "Item cannot be inserted since container is not large enough.");

    private final int value;
    private final String msg;

    ErrorCodes(int i, String msg) {
        this.value = i;
        this.msg = msg;
    }

    public int getValue() {
        return value;
    }

    public String getMsg() {
        return msg;
    }
}
