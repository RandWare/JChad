package net.jchad.server.model.config.store.internalSettings;

public class DefaultInternalSettings {
    private static final int maxConfigWatcherRestarts = 3;
    private static final int configWatcherRestartCountResetMilliseconds = 5000;
    private static final long connectionRefreshIntervalMillis = 100;

    public static InternalSettings get() {
        return new InternalSettings(
            maxConfigWatcherRestarts,
            configWatcherRestartCountResetMilliseconds,
            connectionRefreshIntervalMillis
        );
    }
}
