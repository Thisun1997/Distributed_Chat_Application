package Core;

public class Member {



    private String room;
    private String id;
    private boolean isRoomOwner;
    public Member(String id,String room){
        this.id=id;
        this.room=room;
    }
    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setIsRoomOwner(boolean isRoomOwner) {
        this.isRoomOwner = isRoomOwner;
    }

    public boolean getIsRoomOwner() {
        return this.isRoomOwner;
    }
}
