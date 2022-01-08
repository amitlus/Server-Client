package bgu.spl.net.api.bidi;
import bgu.spl.net.api.DataBase;
import bgu.spl.net.api.User;
import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.ConnectionHandler;

import java.sql.SQLOutput;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<Message> implements Connections<Message> {

    private ConcurrentHashMap<Integer,ConnectionHandler> connidToCh; //Map connection handler id to connection handler
    private ConcurrentHashMap<Integer,ConnectionHandler> userIdToCh; //Map userId to the connection handler where he logged in
    private ConcurrentHashMap<ConnectionHandler, Integer> chToUserId; //Map connection handler to the userId he is logged to
    private DataBase database = DataBase.getInstance();


    ConnectionsImpl(){
        connidToCh = new ConcurrentHashMap<Integer, ConnectionHandler>();
        userIdToCh = new ConcurrentHashMap<Integer, ConnectionHandler>();
        chToUserId = new ConcurrentHashMap<ConnectionHandler, Integer>();
    }

    private static class SingletonHolder{
        private static ConnectionsImpl instance = new ConnectionsImpl();
    }

    public static ConnectionsImpl getInstance() {
        return ConnectionsImpl.SingletonHolder.instance;
    }

    public ConcurrentHashMap<Integer, ConnectionHandler> getUserIdToCh() {
        return userIdToCh;
    }

    public ConcurrentHashMap<ConnectionHandler, Integer> getChToUserId() {
        return chToUserId;
    }

    public ConcurrentHashMap<Integer, ConnectionHandler> getConnidToCh() {
        return connidToCh;
    }


    @Override
    public boolean send(int connectionId, Message msg) {
        if(!connidToCh.containsKey(connectionId))
            return false;
        ConnectionHandler handler = connidToCh.get(connectionId);
        System.out.println("Message sent to handler");
        handler.send(msg);
        return true;
    }

    @Override
    public void broadcast(Message msg) {
        User user;
        Iterator<User> usersIterator = database.getUserIdToUser().values().iterator();
        int connid;

        while(usersIterator.hasNext()){
            user = usersIterator.next();
            ConnectionHandler handler = userIdToCh.get(user.getUserId());
            connid = handler.getConnid();
            send(connid, msg);
        }
    }

    @Override
    public void disconnect(int connectionId) {
        ConnectionHandler handler = connidToCh.get(connectionId);
        int userConnectedId = chToUserId.get(handler);
        chToUserId.remove(handler);
        userIdToCh.remove(userConnectedId);
    }
}
