package Core;


import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.ArrayList;


public class Room implements Cloneable{
    private ArrayList<String> members;
    private ChannelGroup memberGroup;
    private String id;
    private String owner;

    public Room(String id,String owner){
        this.memberGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        this.members=new ArrayList<String>();
        this.owner = owner;
        this.id=id;
    }


    public ArrayList<String> getMembers(){
         return this.members;
    }
    public void removeMember(String id){
        members.remove(id);
    }
    public void removeMemberChannel(Channel channel){
        memberGroup.remove(channel);
    }
    public void setMember(String id) {
        this.members.add(id);
    }
    public void setMemberChannel(Channel channel) {
        this.memberGroup.add(channel);
    }
    public void setMemberChannels(ChannelGroup channelGroup) {
        this.memberGroup=channelGroup;
    }
    public String getOwner() {
        return owner;
    }

    public String getId() {
        return id;
    }

    public ChannelGroup getMemberGroup() {
        return memberGroup;
    }

    public Room clone() throws CloneNotSupportedException
    {
        return (Room) super.clone();
    }
}
