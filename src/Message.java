public enum Message {
    NO_LOGIN("You have not logged in"),
    INVALID_MONEY("Invalid money"),
    INVALID_PRODUCT("Invalid product"),
    OUT_OF_STOCK("Out of stock"),
    INVALID_QUANTITY("Invalid quantity"),
    INSUFFICIENT_MONEY("Insufficient money"),
    INVALID_ID("Invalid id"),
    INVALID_NAME("Invalid name"),
    INVALID_COMMAND("Invalid command"),
    UNABLE_TO_CONNECT("Unable to connect to server"),
    UNABLE_TO_OPEN_SERVER("Unable to open server"),
    UNABLE_TO_SEND("Unable to send message"),
    UNABLE_TO_RECEIVE("Unable to receive message"),
    UNABLE_TO_DISCONNECT("Unable to end connection"),
    END_OF_CONNECTION("End of connection"),
    UNABLE_TO_LISTEN("Unable to start listening"),
    UNABLE_TO_HANDLE("Unable to start handling");
    private final String message;
    Message(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
}
