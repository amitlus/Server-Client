package bgu.spl.net.api;

/**
 * This class represents a message that the Client sent after it has been DECODED
 */

public class ClientMessage implements Message{

    private short opcode;
    private String username;
    private String password;
    private String birthday;
    private char captcha;
    private char follow; //1-unfollow, 0-follow
    private String content;
    private String sendingDate;
    private String usersNamesList;


    public ClientMessage(short opcode) {
        this.opcode=opcode;
    }

    public short getOpcode() {
        return opcode;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getBirthday() {
        return birthday;
    }

    public char getCaptcha() {
        return captcha;
    }

    public char getFollow() {
        return follow;
    }

    public String getContent() {
        return content;
    }

    public String getSendingDate() {
        return sendingDate;
    }

    public String getUsersNamesList() {
        return usersNamesList;
    }

    public void setOpcode(short opcode) {
        this.opcode = opcode;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setCaptcha(char captcha) {
        this.captcha = captcha;
    }

    public void setFollow(char follow) {
        this.follow = follow;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSendingDate(String sendingDate) {
        this.sendingDate = sendingDate;
    }

    public void setUsersNamesList(String usersNamesList) {
        this.usersNamesList = usersNamesList;
    }

}
