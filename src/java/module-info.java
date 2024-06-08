module net.jchad {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires org.kordamp.ikonli.javafx;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;
    requires org.jline;
    requires jdk.unsupported;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires java.rmi;
    requires org.fusesource.jansi;
    requires javafx.media;
    requires org.fxmisc.richtext;

    opens net.jchad.server.model.command.commands.version;
    exports net.jchad.client.view;
    opens net.jchad.client to javafx.fxml;
    opens net.jchad.shared.networking.packets to com.google.gson;
    opens net.jchad.shared.networking.ip to com.google.gson;
    opens net.jchad.server.model.config to com.fasterxml.jackson.databind;
    exports net.jchad.server.model.chats to com.fasterxml.jackson.databind;
    exports net.jchad.installer.gui;
    exports net.jchad.installer.serializable to com.google.gson;
    opens net.jchad.client.model.client to javafx.fxml;
    exports net.jchad.server.view to javafx.graphics;
    opens net.jchad.server.model.config.store to com.fasterxml.jackson.databind;
    opens net.jchad.server.model.chats to com.google.gson;
    exports net.jchad.tests.shared;
    opens net.jchad.server.view to org.fxmisc.richtext;
    opens net.jchad.shared.networking.packets.password to com.google.gson;
    opens net.jchad.shared.networking.packets.username to com.google.gson;
    opens net.jchad.shared.networking.packets.encryption to com.google.gson;
    opens net.jchad.shared.networking.packets.messages to com.google.gson;
    opens net.jchad.shared.networking.packets.defaults to com.google.gson;
    opens net.jchad.client.model.client.config to com.google.gson;
    opens net.jchad.client.model.store.connection to com.google.gson;
    exports net.jchad.client.view.gui;
    opens net.jchad.shared.common;
}