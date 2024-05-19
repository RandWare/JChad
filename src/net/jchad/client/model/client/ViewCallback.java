package net.jchad.client.model.client;

/* Is used by the client to send messages to the view,
which then handles messages based on their types */
public interface ViewCallback {
    /**
     * When a fatal error occurs, the connection to the server can't be kept upright.
     * When this method is called, a prompt should be displayed with the exception message.
     * It should also be made clear to the user, that the connection was closed.
     *
     * @param e the exception that occurred
     */
    void handleFatalError(Exception e);

    /**
     * This error is non-fatal, but should be acknowledged.
     * When this method is called, a prompt should be displayed with the exception message.
     *
     * @param e the exception that occurred
     */
    void handleError(Exception e);

    /**
     * A warning should only be displayed in a debugging context, but can be ignored.
     *
     * (Maybe create a separate window that displays these messages?)
     *
     * @param warning the warning provided
     */
    void handleWarning(String warning);

    /**
     * The information should only be display in a debugging context.
     *
     * (Maybe create a separate window that displays these messages?)
     * @param info the information provided
     */
    void handleInfo(String info);

    /**
     * When this method is called, the backend expects a prompt of some sort
     * to be display with the given title and message. The input of this prompt
     * should then be returned.
     *
     * @param promptTitle the title of the displayed prompt.
     * @param promptMessage the full message of the prompt displayed.
     * @return the input of this prompt
     */
    String displayPrompt(String promptTitle, String promptMessage);
}
