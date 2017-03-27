package com.thatredhead.redbot.permission;

import com.thatredhead.redbot.RedBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

public class GuildOrChannel {
    String id;
    boolean isChannel;

    public GuildOrChannel(IGuild g) {
        id = g.getID();
        isChannel = false;
    }

    public GuildOrChannel(IChannel c) {
        id = c.getID();
        isChannel = true;
    }

    public String getID() {
        return id;
    }

    public IChannel getChannel() {
        if(isChannel)
            return RedBot.getClient().getChannelByID(id);
        return null;
    }

    public IGuild getGuild() {
        if(isChannel)
            return RedBot.getClient().getChannelByID(id).getGuild();
        return RedBot.getClient().getGuildByID(id);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof GuildOrChannel) {
            GuildOrChannel obj = (GuildOrChannel) o;
            return this.id.equals(obj.id) && this.isChannel == obj.isChannel;
        } if(o instanceof IChannel) {
            return isChannel && this.id.equals(((IChannel) o).getID());
        } if(o instanceof IGuild) {
            return !isChannel && this.id.equals(((IGuild) o).getID());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (id + (isChannel ? "1" : "0")).hashCode();
    }
}
