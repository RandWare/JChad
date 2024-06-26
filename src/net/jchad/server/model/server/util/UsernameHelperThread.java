package net.jchad.server.model.server.util;

import net.jchad.server.model.server.ServerThread;
import net.jchad.server.model.users.*;
import net.jchad.shared.networking.packets.PacketType;
import net.jchad.shared.networking.packets.username.UsernameServerPacket;
import net.jchad.shared.networking.packets.username.UsernameClientPacket;

public class UsernameHelperThread extends HelperThread {
    private final String usernameRegexDescription;

    public UsernameHelperThread(ServerThread serverThread) {
        super(serverThread);
        usernameRegexDescription = serverThread.getConfig().getInternalSettings().getUsernameRegexDescription();
    }

    public User arrangeUser() {
        getServerThread().getMessageHandler().handleDebug("%s started the UsernameHelperThread".formatted(getServerThread().getRemoteAddress()));
        User user = null;
        writePacket(new UsernameServerPacket(UsernameServerPacket.UsernameResponseType.PROVIDE_USERNAME, "Please enter a username."));
        for (int fails = 0; fails <= getRetries(); fails++) {
            UsernameClientPacket usernameClientPacket = readJSON(UsernameClientPacket.class, PacketType.USERNAME_CLIENT);
            try {

                user = new User(usernameClientPacket.getUsername(), getServerThread());
                writePacket(new UsernameServerPacket(UsernameServerPacket.UsernameResponseType.SUCCESS_USERNAME_SET, "The username was successfully set"));
                    return user;

            } catch (NullPointerException e) {
                int retriesLeft = getRetries() - fails;
                getServerThread().getMessageHandler().handleDebug("A NullPointerException occurred during the user arrangement. The connection get terminated "
                        + ((retriesLeft <= 0) ? "now": ("after " + retriesLeft + " more failed attempt(s)")), e);

                if (retriesLeft <= 0) {
                    getServerThread().close("A NullPointerException occurred during the user arrangement");
                }
            } catch (ConnectionExistsException e) {
                getServerThread().getMessageHandler().handleDebug("The ServerThread has already been associated with a username. The connection gets terminated now ");
                    getServerThread().close("The ServerThread has already been associated with a username");

            } catch (UsernameInvalidException e) {
                int retriesLeft = getRetries() - fails;
                getServerThread().getMessageHandler().handleDebug("The client entered an invalid username. The requested username from the client is "+ e.getInvalidUsername() +". The connection get terminated "
                        + ((retriesLeft <= 0) ? "now": ("after " + retriesLeft + " more failed attempt(s)")));
                writePacket(new UsernameServerPacket(UsernameServerPacket.UsernameResponseType.ERROR_USERNAME_INVALID, usernameRegexDescription));
                if (retriesLeft <= 0) {
                    getServerThread().close("Failed to choose a valid username");
                }
            }  catch (UsernameTakenException e) {
                int retriesLeft = getRetries() - fails;
                getServerThread().getMessageHandler().handleDebug("The client tried to get an existing username. The connection get terminated "
                        + ((retriesLeft <= 0) ? "now": ("after " + retriesLeft + " more failed attempt(s)")));
                writePacket(new UsernameServerPacket(UsernameServerPacket.UsernameResponseType.ERROR_USERNAME_TAKEN, "The username is already taken"));
                if (retriesLeft <= 0) {
                    getServerThread().close("Failed to choose a non existing username");
                }
            } catch (UsernameBlockedException e) {
                int retriesLeft = getRetries() - fails;
                getServerThread().getMessageHandler().handleDebug("The client tried to get an blacklisted username. The connection get terminated "
                        + ((retriesLeft <= 0) ? "now": ("after " + retriesLeft + " more failed attempt(s)")));
                writePacket(new UsernameServerPacket(UsernameServerPacket.UsernameResponseType.ERROR_USERNAME_BLOCKED, "The username is blacklisted"));
                if (retriesLeft <= 0) {
                    getServerThread().close("Failed to choose a non blocked username");
                }
            }
        }
        getServerThread().getMessageHandler().handleDebug("%s selected a username successfully".formatted(getServerThread().getRemoteAddress()));
            return user;

    }
}
