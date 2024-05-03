package net.jchad.server.model.networking.packets;

public class NotWhitelistedPacket extends DefaultPacket{
    public NotWhitelistedPacket() {
        super(PacketType.NOT_WHITELISTED, "You are not whitelisted on this server. Contact the server admin if you believe this is a mistake.");
    }
}
