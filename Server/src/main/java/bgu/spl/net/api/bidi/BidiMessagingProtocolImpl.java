package bgu.spl.net.api.bidi;

import bgu.spl.net.api.*;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.api.bidi.ConnectionsImpl;
import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.ConnectionHandler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<Message> {

    private boolean shouldTerminate = false;
    private DataBase database;
    private ConnectionsImpl<Message> connections;
    private int connectionId;
    private User currentUser;
    private String[] forbiddenWords = new String[]{"war", "Trump"};


    public void start(int connectionId, Connections<Message> connections) {
        this.database = DataBase.getInstance();
        this.connections = ConnectionsImpl.getInstance();
        this.connectionId = connectionId;
        this.currentUser = null;
    }

    public void process(Message message) {
        ClientMessage clientMessage = ((ClientMessage)(message));
        short opcode = clientMessage.getOpcode();
        ServerMessage response; //Will hold the Server's response - Error/Ack

        //Get the current Connection Handler
        ConnectionHandler<Message> currentConnection = connections.getConnidToCh().get(connectionId);

        //If the current connection is logged in to a User, save it for later use
        if (connections.getChToUserId().containsKey(currentConnection))
            currentUser = database.getUserIdToUser().get(connections.getChToUserId().get(currentConnection));


        //Register
        if (opcode == 1) {
            System.out.println("REGISTER request");
            String username = clientMessage.getUsername();
            //If there is a User with this username already
            if (database.getUsernameToUser().containsKey(username)) {
                response = new ServerMessage((short) 11); //Error opcode
                response.setSecondOpcode(opcode);
                connections.send(connectionId, response);

            } else {
                User newUser = new User(username, clientMessage.getPassword(), clientMessage.getBirthday(), database.getNumOfUsers());
                database.getUsernameToUser().put(username, newUser); //Map a new user with his username
                database.getUserIdToUser().put(newUser.getUserId(), newUser); //Map the new user by his id
                database.increaseNumOfUsers();
                response = new ServerMessage((short) 10); //Sends generic Ack message
                response.setSecondOpcode(opcode);
                connections.send(connectionId, response);
            }
        }

        //Login
        if (opcode == 2) {
            System.out.println("LOGIN request");
            String username = clientMessage.getUsername();
            String password = clientMessage.getPassword();
            char captcha = clientMessage.getCaptcha();
            boolean userExist = database.getUsernameToUser().containsKey(username);

            //If Captcha=0 OR User dosen't exist OR the Connection Handler is already logged in to a User
            if (captcha == '0' || !userExist || connections.getChToUserId().containsKey(currentConnection)) {
                response = new ServerMessage((short) 11); //Error opcode
                response.setSecondOpcode(opcode);
                connections.send(connectionId, response);
            }

            //If there is a User with this username and the Connection Handler is "Free"
            else {
                User maybeCurrentUser = database.getUsernameToUser().get(username);

                //If the User's pw doesn't match OR the User already logged in
                if (!maybeCurrentUser.getPassword().equals(password) || maybeCurrentUser.isLoggedIn()) {
                    response = new ServerMessage((short) 11); //Error opcode

                    response.setSecondOpcode(opcode);
                    connections.send(connectionId, response);

                } else {
                    currentUser = maybeCurrentUser; //Now we log in to the user, so we update the current user
                    connections.getUserIdToCh().put(currentUser.getUserId(), currentConnection);
                    connections.getChToUserId().put(currentConnection, currentUser.getUserId());
                    currentUser.setLoggedIn(true);

                    while(!currentUser.getUnreadMessages().isEmpty()){
                        ServerMessage msg = ((ServerMessage) (currentUser.getUnreadMessages().remove()));
                        response = new ServerMessage((short)9);
                        response.setFirstOP(msg.getFirstOpcode());
                        response.setPostingUser(msg.getPostingUser());

                        response.setContent(msg.getContent());
                        connections.send(connectionId, response);
                    }

                    response = new ServerMessage((short) 10); //Sends generic Ack message
                    response.setSecondOpcode(opcode);
                    connections.send(connectionId, response);
                }
            }
        }

        //Logout
        if (opcode == 3) {
            System.out.println("LOGOUT request");

            //If the Connection handler isn't logged in to any User
            if (currentUser == null) {
                response = new ServerMessage((short) 11); //Error opcode
                response.setSecondOpcode(opcode);
                connections.send(connectionId, response);

            } else {
                currentUser.setLoggedIn(false);
                shouldTerminate = true;
                currentUser = null; //We log out from the user

                response = new ServerMessage((short) 10); //Sends generic Ack message
                response.setSecondOpcode(opcode);
                connections.send(connectionId, response);
            }
        }

        //Follow
        if (opcode == 4) {
            System.out.println("FOLLOW request");

            char f = clientMessage.getFollow();
            String username = clientMessage.getUsername();

            //Follow
            if (f == 0) {
                boolean userToFollowExist = database.getUsernameToUser().containsKey(username);

                //If the User we want to follow doesn't exist OR the current connection doesn't logged in to any User
                if (!userToFollowExist || currentUser == null) {
                    response = new ServerMessage((short) 11); //Error opcode
                    response.setSecondOpcode(opcode);
                    connections.send(connectionId, response);

                } else {
                    User userToFollow = database.getUsernameToUser().get(username);
                    //If the logged in User already follows the other User
                    if (currentUser.getFollowing().contains(userToFollow.getUserName()) || currentUser.getUsersBlockedMe().contains(username) || currentUser.getUsersIBlocked().contains(username)) {
                        response = new ServerMessage((short) 11); //Error opcode
                        response.setSecondOpcode(opcode);
                        connections.send(connectionId, response);

                    } else {
                        currentUser.getFollowing().add(userToFollow.getUserName());
                        userToFollow.getFollowers().add(currentUser.getUserName());

                        response = new ServerMessage((short) 10); //Sends Follow Ack message
                        response.setSecondOpcode(opcode);
                        response.setUsername(username);
                        connections.send(connectionId, response);
                    }
                }
            }

            if (f == 1) {
                boolean userToUnfollowExist = database.getUsernameToUser().containsKey(username);
                //If the User we want to unfollow doesn't exist OR the current connection doesn't logged in to any User
                if (!userToUnfollowExist || currentUser == null) {
                    response = new ServerMessage((short) 11); //Error opcode
                    response.setSecondOpcode(opcode);
                    connections.send(connectionId, response);
                } else {
                    User userToFollow = database.getUsernameToUser().get(username);
                    //If the logged in User already NOT follows the other User
                    if (!currentUser.getFollowing().contains(userToFollow.getUserName())) {
                        response = new ServerMessage((short) 11); //Error opcode
                        response.setSecondOpcode(opcode);
                        connections.send(connectionId, response);
                    } else {
                        currentUser.getFollowing().remove(username);
                        userToFollow.getFollowers().remove(currentUser.getUserName());

                        response = new ServerMessage((short) 10); //Sends Follow Ack message
                        response.setSecondOpcode(opcode);
                        response.setUsername(username);
                        connections.send(connectionId, response);
                    }
                }
            }
        }

        //Post
        if (opcode == 5) {
            System.out.println("POST request");

            //If the Connection handler isn't logged in to any User
            if (currentUser == null) {
                response = new ServerMessage((short) 11); //Error opcode
                response.setSecondOpcode(opcode);
                connections.send(connectionId, response);

            } else {
                String postContent = clientMessage.getContent();
                database.getPostsAndPms().add(postContent);

                //Send to Users who were mentioned in the post or are Followers
                //We use HashSet to prevent duplicates
                HashSet<String> recipients = new HashSet<String>(currentUser.getFollowers());

                //Find Users who were mentioned in the post
                HashSet<String> mentionedUsers = new HashSet<String>();

                int index = 0;
                //Iterate through the content until we reach '0' eg. the end of it
                while (postContent.indexOf('@', index) != -1) {
                    //Reach to the next '@' sign
                    int start = postContent.indexOf('@', index);
                    //Reach to the end of the username mentioned
                    int firstSpace = postContent.indexOf(' ', start + 1);
                    //If the name ends at the end of the content string
                    if (firstSpace == -1)
                        firstSpace = postContent.length();
                    //The username mentioned
                    String username = postContent.substring(start + 1, firstSpace);

                    //Check if blocked
                    if (!currentUser.getUsersBlockedMe().contains(username) && !currentUser.getUsersIBlocked().contains(username))
                        mentionedUsers.add(username);
                    //Handle the case in which firstSpace = content.length
                    index = firstSpace - 1;
                }

                //Merge Followers and Mentioned Users. No duplicate possible because Set holds unique elements
                recipients.addAll(mentionedUsers);

                //Set the Message object
                ServerMessage notification = new ServerMessage((short) 9);
                notification.setNotificationType('1');
                notification.setPostingUser(currentUser.getUserName());
                notification.setContent(postContent);

                for (String recipient : recipients) {
                    User recipientUser = database.getUsernameToUser().get(recipient);
                    int recipientConnid = connections.getUserIdToCh().get(recipientUser.getUserId()).getConnid();

                    //If the recipient doesn't logged in, we add the notification to his unreadMessages (THE SEND METHOD WILL RETURN FALSE)
                    if (!connections.send(recipientConnid, notification))
                        recipientUser.getUnreadMessages().add(notification);
                }

                currentUser.increaseNumberOfPosts();

                response = new ServerMessage((short) 10); //Sends generic Ack message
                response.setSecondOpcode(opcode);
                connections.send(connectionId, response);
            }
        }

        //PM
        if (opcode == 6) {
            System.out.println("PM request");

            String content = clientMessage.getContent();
            String username = clientMessage.getUsername();

            //If the Connection handler isn't logged in to any User OR the recipient User isn't registered OR the current User not following the recipient User
            if (currentUser == null || !database.getUsernameToUser().containsKey(username) || !currentUser.getFollowing().contains(username) || currentUser.getUsersBlockedMe().contains(username) || currentUser.getUsersIBlocked().contains(username) || content.contains("@")) {
                response = new ServerMessage((short) 11); //Error opcode
                response.setSecondOpcode(opcode);
                connections.send(connectionId, response);

            } else {
                String filteredContent = filterMessage(content);
                database.getPostsAndPms().add(filteredContent);

                //Compose the notification
                ServerMessage notification = new ServerMessage((short) 9);
                notification.setNotificationType('0');
                notification.setPostingUser(currentUser.getUserName());
                notification.setContent(filteredContent);

                User recipientUser = database.getUsernameToUser().get(username);


                //If the recipient doesn't logged in, we add the notification to his unreadMessages (THE SEND METHOD WILL RETURN FALSE)

               ConnectionHandler recipientConn = connections.getUserIdToCh().get(recipientUser.getUserId());
               if(recipientConn == null)
                   recipientUser.getUnreadMessages().add(notification);

               else
                   connections.send(recipientConn.getConnid(), notification);


                response = new ServerMessage((short) 10); //Sends generic Ack message
                response.setSecondOpcode(opcode);
                connections.send(connectionId, response);
            }
        }

        //Logstat
        if (opcode == 7) {
            System.out.println("LOGSTAT request");

            //If the Connection handler isn't logged in to any User
            if (currentUser == null) {
                response = new ServerMessage((short) 11); //Error opcode
                response.setSecondOpcode(opcode);
                connections.send(connectionId, response);

            } else {
                User user;
                Iterator<User> valueIterator = database.getUserIdToUser().values().iterator();

                while(valueIterator.hasNext()){
                    user = valueIterator.next();

                    if(!currentUser.getUsersIBlocked().contains(user.getUserName()) && !currentUser.getUsersBlockedMe().contains(user.getUserName())){
                        //Sends Logstat Ack message

                        response = new ServerMessage((short) 10);
                        response.setSecondOpcode(opcode);
                        response.setAge(user.getAge());
                        response.setNumPosts(user.getNumberOfPosts());
                        response.setNumFollowers((short)user.getFollowers().size());
                        response.setNumFollowing((short)user.getFollowing().size());

                        connections.send(connectionId, response);
                    }
                }
            }

        }

        //Stat
        if (opcode == 8) {
            System.out.println("STAT request");

            //If the Connection handler isn't logged in to any User
            if (currentUser == null) {
                response = new ServerMessage((short) 11); //Error opcode
                response.setSecondOpcode(opcode);
                connections.send(connectionId, response);

            } else {
                Vector<String> usersList = splitString(clientMessage.getUsersNamesList(), '|');
                for(String username : usersList){
                    if(!currentUser.getUsersIBlocked().contains(username) && !currentUser.getUsersBlockedMe().contains(username) && database.getUsernameToUser().containsKey(username)){
                        User user = database.getUsernameToUser().get(username);
                        //Sends Stat Ack message
                        response = new ServerMessage((short) 10);
                        response.setSecondOpcode(opcode);
                        response.setAge(user.getAge());
                        response.setNumPosts(user.getNumberOfPosts());
                        response.setNumFollowers((short)user.getFollowers().size());
                        response.setNumFollowing((short)user.getFollowing().size());

                        connections.send(connectionId, response);
                    }
                    else{
                        response = new ServerMessage((short) 11); //Error opcode
                        response.setSecondOpcode(opcode);
                        connections.send(connectionId, response);
                    }
                }
            }
        }

        //Block
        if (opcode == 12) {
            System.out.println("BLOCK request");

            String userToBlock = clientMessage.getUsername();

            //If the Connection handler isn't logged in to any User or the user we want to block doesn't exist
            if (currentUser == null || !database.getUsernameToUser().containsKey(userToBlock)) {
                response = new ServerMessage((short) 11); //Error opcode
                response.setSecondOpcode(opcode);
                connections.send(connectionId, response);

            } else {

                if(currentUser.getFollowers().contains(userToBlock))
                    currentUser.getFollowers().remove(userToBlock);
                if(currentUser.getFollowing().contains(userToBlock))
                    currentUser.getFollowing().remove(userToBlock);

                User blockedUser = database.getUsernameToUser().get(userToBlock);

                if(blockedUser.getFollowers().contains(currentUser.getUserName()))
                    blockedUser.getFollowers().remove(currentUser.getUserName());
                if(blockedUser.getFollowing().contains(currentUser.getUserName()))
                    blockedUser.getFollowing().remove(currentUser.getUserName());

                currentUser.getUsersIBlocked().add(userToBlock);
                blockedUser.getUsersBlockedMe().add(currentUser.getUserName());

                response = new ServerMessage((short)10); //Sends Generic Ack
                response.setSecondOpcode(opcode);
                connections.send(connectionId, response);
            }
        }
    }

    public String filterMessage(String message) {
        //Split the content into words
        String [] contentWords = message.split(" ");

        //Iterate through the forbidden Words and compare them to the content words
        Iterator wordsIter = Arrays.stream(forbiddenWords).iterator();

        while(wordsIter.hasNext()){
            String forbiddenWord = (String)wordsIter.next();
            for(int j=0; j<contentWords.length; j++) {
                if(contentWords[j].equals(forbiddenWord))
                    contentWords[j] = "<filtered>";
            }
        }

        //Build the Filtered Message
        StringBuilder output = new StringBuilder();
        for (int i=0; i <= contentWords.length-1; i++)
            output.append(contentWords[i]+" ");
        //Remove the last space
        output.substring(0, output.length()-1);

        return output.toString();
    }

    private Vector<String> splitString(String str, char delimiter) {
        Vector<String> vec = new Vector<>();
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            if(str.charAt(i) != delimiter)
                tmp.append(str.charAt(i));

            else {
                vec.add(tmp.toString());
                //Reset the tmp variable
                tmp = new StringBuilder();
            }
        }
        //For the last username
        if (tmp.length() != 0)
            vec.add(tmp.toString());

        return vec;
    }

    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
