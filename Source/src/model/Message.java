package model;

public class Message {


    /*--- Variables ---*/

    protected final String timestamp;
    protected final String content;



    /*--- Constructor ---*/

    public Message(String timestamp, String content) {
        this.timestamp = timestamp;
        this.content = content;
    }
}
