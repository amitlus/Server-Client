package bgu.spl.net.api;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class User {

    private String userName;
    private String password;
    private String birthday;
    private int userId;
    private boolean loggedIn;
    private ArrayList<String> following;
    private ArrayList<String> followers;
    private ConcurrentLinkedQueue<Message> unreadMessages;
    private short numberOfPosts;
    private short age;
    private ArrayList<String> usersBlockedMe;
    private ArrayList<String> usersIBlocked;



    public User(String username, String password, String birthday, int userId ){
        this.userName = username;
        this.password = password;
        this.birthday = birthday;
        this.userId = userId;
        loggedIn = false;
        following = new ArrayList<String>();
        followers = new ArrayList<String>();
        unreadMessages = new ConcurrentLinkedQueue<Message>();
        numberOfPosts = (short)0;
        age = calculateAge(birthday);
        usersBlockedMe = new ArrayList<String>();
        usersIBlocked = new ArrayList<String>();
    }

    private short calculateAge(String birthday) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate birthdayDate = LocalDate.parse(birthday, formatter);
        Period age = Period.between(birthdayDate, LocalDate.now());
        return (short)age.getYears();
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getBirthday() {
        return birthday;
    }

    public int getUserId() {
        return userId;
    }

    public ArrayList<String> getFollowing() {
        return following;
    }

    public ArrayList<String> getFollowers() {
        return followers;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public ConcurrentLinkedQueue<Message> getUnreadMessages() {
        return unreadMessages;
    }

    public short getNumberOfPosts() {
        return numberOfPosts;
    }

    public void increaseNumberOfPosts(){
        numberOfPosts++;
    }

    public ArrayList<String> getUsersBlockedMe() {
        return usersBlockedMe;
    }

    public ArrayList<String> getUsersIBlocked() {
        return usersIBlocked;
    }

    public short getAge() {
        return age;
    }

}

