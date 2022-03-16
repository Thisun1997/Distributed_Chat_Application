package Messages;


public abstract class CoordinationMessage extends Message {
    private String option;
    CoordinationMessage(){
        this.option=getTypeOrOption();
    }
    abstract void handle();

}
