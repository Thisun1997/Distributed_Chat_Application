package Core;

public class Member {



    private String room;
    private String id;
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
}
