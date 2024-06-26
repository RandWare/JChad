package net.jchad.server.model.users;

import net.jchad.server.model.chats.Chat;
import net.jchad.server.model.chats.ChatMessage;
import net.jchad.server.model.server.ServerThread;
import net.jchad.shared.cryptography.ImpossibleConversionException;
import net.jchad.shared.networking.packets.InvalidPacket;

import net.jchad.shared.networking.packets.PacketType;
import net.jchad.shared.networking.packets.messages.ClientMessagePacket;
import net.jchad.shared.networking.packets.messages.ServerMessagePacket;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class User {

    private static final ConcurrentHashMap<ServerThread, User> users = new ConcurrentHashMap<>();
    private final ServerThread connection;
    private final String username;
    private final Set<String> joinedChats = ConcurrentHashMap.newKeySet();
    private boolean readyToReceiveMessages;

    /**
     * Creates a new user and adds it and the given {@link ServerThread} into a {@link ConcurrentHashMap}.
     * Users can send messages to other users
     *
     * @param username The username of the user.
     * @param connection The {@link ServerThread} that gets associated with the user
     *
     *
     * @throws UsernameInvalidException If the username did not match with the configured regex
     * @throws UsernameTakenException If the username was already taken by another connection
     * @throws NullPointerException If one of the Parameters is null
     * @throws ConnectionExistsException If the connection is already associated with a user
     */
    public User(String username, ServerThread connection) {
            this(username, connection, new HashSet<>());
        }


    /**
     * Creates a new user and adds it and the given {@link ServerThread} into a {@link ConcurrentHashMap}.
     * Users can send messages to other users
     *
     * @param username The username of the user.
     * @param connection The {@link ServerThread} that gets associated with the user
     * @param joinedChats All chats that the user is in
     *
     *
     * @throws UsernameInvalidException If the username did not match with the configured regex
     * @throws UsernameTakenException If the username was already taken by another connection
     * @throws NullPointerException If one of the Parameters is null
     * @throws ConnectionExistsException If the connection is already associated with a user
     */
    public User(String username, ServerThread connection, Set<String> joinedChats) {
        this(username, connection, joinedChats, false);
    }

    /**
     * Creates a new user and adds it and the given {@link ServerThread} into a {@link ConcurrentHashMap}.
     * Users can send messages to other users
     *
     * @param username The username of the user.
     * @param connection The {@link ServerThread} that gets associated with the user
     * @param readyToReceiveMessages If the user is ready to receive chats, or if some further initialization steps have to be done.
     *                               Change this state with {@link User#setReadyToReceiveMessages(boolean)}.
     *
     *
     * @throws UsernameInvalidException If the username did not match with the configured regex
     * @throws UsernameTakenException If the username was already taken by another connection
     * @throws NullPointerException If one of the Parameters is null
     * @throws ConnectionExistsException If the connection is already associated with a user
     */
    public User(String username, ServerThread connection,Set<String> joinedChats, boolean readyToReceiveMessages) {
        //This section checks if any of the provided parameters are null
        if (username == null) {
            throw new NullPointerException("username can not be null");
        }

        if (connection == null) {
            throw new NullPointerException("the connection is not allowed to be null");
        }

        if (joinedChats == null) {
            throw new NullPointerException("the joinedChats is not allowed to be null");
        }

        //This checks if the requested username matches the regex that was set in the InternalConfig
        if (!username.matches(connection.getServer().getConfig().getInternalSettings().getUsernameRegex())) {
            throw new UsernameInvalidException(connection.getServer().getConfig().getInternalSettings().getUsernameRegexDescription() ,username);
        }

        //This loops through the blocked usernames in the ServerSettings and if it is blocked the usernameAllowed variable will get set to false.
        boolean usernameAllowed = true;
        boolean caseSensitive = connection.getConfig().getServerSettings().isCaseSensitive();
        for (String blocked : connection.getConfig().getServerSettings().getBlockedUsernames()) {
            if (caseSensitive) {
                if (blocked.equals(username)) {
                    usernameAllowed = false;
                    break;
                }
            } else {
                if (blocked.equalsIgnoreCase(username)) {
                    usernameAllowed = false;
                    break;
                }
            }
        }

        //This checks if the loop from above found the username in the blocked list
        if (!usernameAllowed) {
            throw new UsernameBlockedException("The requested username is blacklisted");
        }

        //This checks if the username already exists.
        boolean usernameExists = false;
        Collection<User> checkWithUsers = users.values();
        for (User currentUser : checkWithUsers) {
            if (currentUser.getUsername().equalsIgnoreCase(username)) {
                usernameExists = true;
                break;
            }
        }

        if (usernameExists) {
            throw new UsernameTakenException("The username: " + username + " is already taken.");
        }

        //This should never happen, except if the class that uses this doesn'
        if (users.containsValue(connection)) {
            throw new ConnectionExistsException("The connection (" + connection.getInetAddress().toString() + ") has already been associated with a username");
        }

        this.connection = connection;
        this.username = username;
        this.joinedChats.addAll(joinedChats);
        this.readyToReceiveMessages = readyToReceiveMessages;
        users.put(connection, this);

    }

    public void addJoinedChats(String... chats) {
        joinedChats.addAll(Arrays.asList(chats));
    }

    public void removeJoinedChats(String... chats) {
        joinedChats.removeAll(Arrays.asList(chats));
    }


    public Set<String> getJoinedChats() {
        return joinedChats;
    }

    public ServerThread getConnection() {
        return connection;
    }

    public String getUsername() {
        return username;
    }

    public boolean isReadyToReceiveMessages() {
        return readyToReceiveMessages;
    }

    public void setReadyToReceiveMessages(boolean readyToReceiveMessages) {
        this.readyToReceiveMessages = readyToReceiveMessages;
    }



    /**
     * This methode wraps the {@link ClientMessagePacket} to a {@link ServerMessagePacket} by inserting the current time and the username of this instance.
     * The given message gets sent to all valid users afterwards.
     * A valid user is someone that:
     * <ul>
     *     <li> Is ready to receive messages ({@link User#readyToReceiveMessages} has to be {@code true}) </li>
     *
     *     <li> Does not equal the current user. Object.equals(this, user) has to be false. </li>
     *
     *     <li> Has already joint the specified chat in the {@link ServerMessagePacket messagePacket} </li>
     * </ul>
     * @param messagePacket the packet that gets send to the valid users
     * @return to how many users the {@link ServerMessagePacket messagePacket} was sent
     */
    public int sendMessage(ClientMessagePacket messagePacket) {
        return sendMessage(new ServerMessagePacket(
                messagePacket.getMessage(),
                messagePacket.getChat(),
                getUsername(),
                System.currentTimeMillis()
        ));
    }

    /**
     * This methode sends the given message to all valid users.
     * A valid user is someone that:
     * <ul>
     *     <li> Is ready to receive messages ({@link User#readyToReceiveMessages} has to be {@code true}) </li>
     *
     *     <li> Does not equal the current user. Object.equals(this, user) has to be false. </li>
     *
     *     <li> Has already joint the specified chat in the {@link ServerMessagePacket messagePacket} </li>
     * </ul>
     * @param messagePacket the packet that gets sent to the valid users. If the username is null, it gets set to the username of this instance.
     * @return to how many users the {@link ServerMessagePacket messagePacket} was sent
     */
    public int sendMessage(ServerMessagePacket messagePacket) {
        int messagesSent = 0;
        if (messagePacket == null) {return 0;}
        if (messagePacket.getUsername() == null) messagePacket.setUsername(this.getUsername());
        if (!messagePacket.isValid()) {return 0;}
        //Encrypts message content if necessary




        if (connection.isEncryptMessages() && connection.getMessageAESkey() != null) {
            connection.useMessageKey();
                try {
                    messagePacket =
                            new ServerMessagePacket(
                                    connection.getCrypterManager().decryptAES(messagePacket.getMessage()),
                                    messagePacket.getChat(),
                                    messagePacket.getUsername(),
                                    messagePacket.getTimestamp()

                            );

                } catch (InvalidAlgorithmParameterException | NoSuchPaddingException|  NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
                    connection.getMessageHandler().handleDebug("An unknown error occurred while trying to decrypt the content of the received from %s".formatted(connection.getRemoteAddress()), e);
                    connection.write(new InvalidPacket(PacketType.CLIENT_MESSAGE, "The content of the client message could not be decrypted: " + e.getMessage()).toJSON());
                    connection.close("The content of the client message could not be decrypted");
                } catch (ImpossibleConversionException e) {
                    connection.getMessageHandler().handleDebug("The received message content from %s could not be encrypted, because it is not Base64 encoded".formatted(connection.getRemoteAddress()), e);
                    connection.write(new InvalidPacket(PacketType.CLIENT_MESSAGE, "The content of the client message was not Base64 encoded: " + e.getMessage()).toJSON());
                    connection.close("The content of the client message was not Base64 encoded");
                }

        }


        //Sends messages to all users
        Chat chat = connection.getServer().getChatManager().getChat(messagePacket.getChat());

        if (chat.getConfig().isAnonymous() || connection.getConfig().getServerSettings().isStrictlyAnonymous()) {
            messagePacket = new ServerMessagePacket(
                    messagePacket.getMessage(),
                    messagePacket.getChat(),
                    connection.getConfig().getInternalSettings().getAnonymousUserName(),
                    messagePacket.getTimestamp()
            );
        }

        ChatMessage chatMessage = ChatMessage.fromMessagePacket(messagePacket, connection.getRemoteAddress());
        if (chat == null) {
            connection.getMessageHandler().handleDebug("%s tried to send a message to a chat that does not exist".formatted(getConnection().getRemoteAddress()));
            return 0;
        }
        try {

            chat.addMessage(chatMessage);
        } catch (IOException e) {
            connection.getMessageHandler().handleError(new IOException("An IOException occurred while trying to add the message (from %s) to the chat (%s)"
                    .formatted(connection.getRemoteAddress(), chat.getName()), e));
        }
            Collection<User> userValues = users.values();
            for (User user : userValues) {
                if (!this.equals(user) && user.isReadyToReceiveMessages() && user.getJoinedChats().contains(messagePacket.getChat())) {
                    if (user.getConnection().isEncryptMessages()) {
                        user.getConnection().useMessageKey();
                        try {
                            ServerMessagePacket encryptedMessagePacket = new ServerMessagePacket(
                                    user.getConnection().getCrypterManager().encryptAES(messagePacket.getMessage()),
                                    messagePacket.getChat(),
                                    messagePacket.getUsername(),
                                    messagePacket.getTimestamp()
                            );
                            user.getConnection().write(encryptedMessagePacket.toJSON());
                        } catch (Exception e) {
                            //UHH SCARY!!! MAY CLOSE ALL CONNECTIONS IF THE CLIENT SENDS A STRING THAT IS NOT ENCRYPTABLE?
                            //CVE-1 JUST DROPPED?!?!?!?
                            getConnection().getMessageHandler().handleDebug("An error occurred while encrypting message data for " + user.getConnection().getRemoteAddress(), e);
                            user.getConnection().close("An error occurred while encrypting message data");
                        }
                    } else {
                        user.getConnection().write(messagePacket.toJSON());
                    }
                    messagesSent++;
                }

            }

        return messagesSent;
    }



    /**
     *
     * @param connection The connection that should get removed
     * @return true if an element was removed as a result of this call
     */
    public static boolean removeUser(ServerThread connection) {
        User removed;
        removed = users.remove(connection);
        if (removed == null) {return false;}
        else {return true;}
    }

    /**
     *
     * @param username The username that should get removed
     * @return true if an element was removed as a result of this call
     */
    public static boolean removeUser(String username) {
        boolean wasRemoved = false;
        Iterator<User> iterator = users.values().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getUsername().equals(username)) {
                iterator.remove();
                wasRemoved = true;
                break;
            }
        }

        return wasRemoved;

    }
    /**
     *
     * @param user The user that should get removed
     * @return true if an element was removed as a result of this call
     */
    public static boolean removeUser(User user) {
            return users.values().remove(user);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return readyToReceiveMessages == user.readyToReceiveMessages && Objects.equals(connection, user.connection) && Objects.equals(username, user.username) && Objects.equals(joinedChats, user.joinedChats);
    }



}
