package Messages;


public abstract class Message {


    public String getTypeOrOption() {
        String s = this.getClass().getSimpleName().substring(0, this.getClass().getSimpleName().length() - "Message".length()).toLowerCase();
        String typeOrOption = null;
        if (s.contains("reply")) {
            typeOrOption = s.replace("reply", "");
        } else if (s.contains("request")) {
            typeOrOption = s.replace("request", "");
        } else if (s.contains("response")) {
            typeOrOption = s.replace("response", "");
        } else {
            typeOrOption = s;
        }

        return typeOrOption;

    }

}

