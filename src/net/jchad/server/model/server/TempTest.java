package net.jchad.server.model.server;

import net.jchad.server.model.error.MessageHandler;

public class TempTest implements MessageHandler {
    public static void main(String[] args) {
        MainSocket ms = new MainSocket(13814, new TempTest());
        new Thread(ms).start();
    }

    @Override
    public void handleFatalError(Exception e) {

    }

    @Override
    public void handleError(Exception e) {

    }

    @Override
    public void handleWarning(String warning) {

    }

    @Override
    public void handleInfo(String info) {

    }
}
