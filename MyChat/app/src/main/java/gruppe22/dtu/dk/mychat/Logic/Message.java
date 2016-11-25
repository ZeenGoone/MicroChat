package gruppe22.dtu.dk.mychat.Logic;

/**
 * Created by zeeng on 24/04/2016.
 */
public class Message {
    private String message;
    private String name;

    public Message(){ }
    public Message(String name, String message){
        this.message = message;
        this.name = name;
    }

    public String getName(){
        return name;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String toString(){
        return "name: " + name + ", message: " + message;
    }
}
