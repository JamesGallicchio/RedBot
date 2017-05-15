package com.thatredhead.redbot.permission;

import com.thatredhead.redbot.RedBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

public class GuildOrChannel {

    long id;
    boolean isChannel;

    transient IChannel channel;
    transient IGuild guild;

    public GuildOrChannel(IGuild g) {
        id = g.getLongID();
        isChannel = false;
        guild = g;
        channel = null;
    }

    public GuildOrChannel(IChannel c) {
        id = c.getLongID();
        isChannel = true;
        guild = null;
        channel = c;
    }

    public long getID() {
        return id;
    }

    public IChannel getChannel() {
        return isChannel?
                channel == null?
                        channel = RedBot.getClient().getChannelByID(id)
                        : channel
                : null;
    }

    public IGuild getGuild() {
        return isChannel?
                getChannel() == null ? null : channel.getGuild()
                : guild == null?
                        guild = RedBot.getClient().getGuildByID(id)
                        : guild;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof GuildOrChannel) {
            GuildOrChannel obj = (GuildOrChannel) o;
            return this.id == obj.id && this.isChannel == obj.isChannel;
        } if(o instanceof IChannel) {
            return isChannel && this.id == ((IChannel) o).getLongID();
        } if(o instanceof IGuild) {
            return !isChannel && this.id == ((IGuild) o).getLongID();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return isChannel?
                getChannel().hashCode()
                : getGuild() == null ? 0 : getGuild().hashCode();
    }
}
