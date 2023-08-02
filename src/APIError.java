public class APIError extends Exception {

    public APIError(Exception exception) {
        super(exception);
    }

    public APIError(String message) {
        super(message);
    }
}
