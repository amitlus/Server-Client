package bgu.spl.net.api;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds DATA about Users & Maintain the numOfUsers field which we use to determine the ID of a new registered User
 */

public class DataBase {

    private int numOfUsers;
    private int numOfConnections;
    private ConcurrentHashMap<String, User> usernameToUser;
    private ConcurrentHashMap<Integer, User> userIdToUser;
    private ArrayList<String> postsAndPms;



    //Private constructor suppresses generation of a (public) default constructor
    DataBase(){
        numOfUsers = 0;
        numOfConnections = 0;
        usernameToUser = new ConcurrentHashMap<String, User>();
        userIdToUser = new ConcurrentHashMap<Integer, User>();
        postsAndPms = new ArrayList<String>();
    }

    private static class SingletonHolder{
        private static DataBase instance = new DataBase();
    }

    public static DataBase getInstance() {
        return SingletonHolder.instance;
    }

    public int getNumOfUsers() {
        return numOfUsers;
    }

    public void increaseNumOfUsers() {
        numOfUsers++;
    }

    public ConcurrentHashMap<String,User> getUsernameToUser() {
        return usernameToUser;
    }

    public int getNumOfConnections() {
        return numOfConnections;
    }

    public void increaseNumOfConnections() {
        numOfConnections++;
    }

    public ConcurrentHashMap<Integer, User> getUserIdToUser() {
        return userIdToUser;
    }

    public ArrayList<String> getPostsAndPms() {
        return postsAndPms;
    }





}
