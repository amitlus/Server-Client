package bgu.spl.net.api;

/**
 * This class represents a message that the Server sends after it has been ENCODED
 */

public class ServerMessage implements Message{

    private short firstOpcode;
    private short secondOpcode;
    private char notificationType; //PM | Public
    private String postingUser;
    private String content;
    private String username;
    private short age;
    private short numPosts;
    private short numFollowers;
    private short numFollowing;

    public ServerMessage(short firstOpcode) {
        this.firstOpcode = firstOpcode;
    }

    public short getFirstOpcode() {
        return firstOpcode;
    }

    public short getSecondOpcode() {
        return secondOpcode;
    }

    public char getNotificationType() {
        return notificationType;
    }

    public String getPostingUser() {
        return postingUser;
    }

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
    }

    public short getAge() {
        return age;
    }

    public short getNumPosts() {
        return numPosts;
    }

    public short getNumFollowers() {
        return numFollowers;
    }

    public short getNumFollowing() {
        return numFollowing;
    }

    public void setFirstOP(short firstOpcode) {
        this.firstOpcode = firstOpcode;
    }

    public void setSecondOpcode(short secondOpcode) {
        this.secondOpcode = secondOpcode;
    }

    public void setNotificationType(char notificationType) {
        this.notificationType = notificationType;
    }

    public void setPostingUser(String postingUser) {
        this.postingUser = postingUser;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAge(short age) {
        this.age = age;
    }

    public void setNumPosts(short numPosts) {
        this.numPosts = numPosts;
    }

    public void setNumFollowers(short numFollowers) {
        this.numFollowers = numFollowers;
    }

    public void setNumFollowing(short numFollowing) {
        this.numFollowing = numFollowing;
    }

}
