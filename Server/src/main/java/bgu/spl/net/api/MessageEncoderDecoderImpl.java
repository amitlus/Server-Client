package bgu.spl.net.api;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageEncoderDecoderImpl implements MessageEncoderDecoder<Message> {

    private byte[] bytes = new byte[1 << 10]; //1k byte array
    private int len = 0;

    //DECODE the next Byte of the Encoded message the Client sent
    public Message decodeNextByte(byte nextByte) {
        //When pressing ENTER in the Terminal
        if (nextByte==';') {
            System.out.println("finished decoding");
            ClientMessage returnMessage = initClientMessage();
            popString();
            return returnMessage;
        }
        pushByte(nextByte);
        return null;
    }

    private ClientMessage initClientMessage() {
        byte[] opcodeBytes = Arrays.copyOfRange(bytes, 0, 2);
        short opcode = bytesToShort(opcodeBytes);
        ClientMessage clientMessage = new ClientMessage(opcode);

        //Register
        if (opcode == 1) {
            System.out.println("DECODE REGISTER");

            //Constructs a new String by decoding the specified sub array of bytes using the UTF_8 charset
            //Offset equals 2 because we skip the first 2 bytes which represents the opcode
            String str = new String(bytes,2,len-3, StandardCharsets.UTF_8);
            String [] register = str.split("\0");

            clientMessage.setUsername(register[0]);
            clientMessage.setPassword(register[1]);
            clientMessage.setBirthday(register[2]);

        }

        //Login
        else if (opcode == 2) {
            System.out.println("DECODE LOGIN");
            String str = new String(bytes,2,len-4, StandardCharsets.UTF_8);
            String [] login = str.split("\0");
            clientMessage.setUsername(login[0]);
            clientMessage.setPassword(login[1]);

            //Captcha byte
            String cap = new String(bytes,len-1,1, StandardCharsets.UTF_8);
            char[] capArr = cap.toCharArray(); //Converts this string to a new character array
            char captcha = capArr[0];
            clientMessage.setCaptcha(captcha);

        }

        //Logout | Logstat

        else if(opcode == 3 || opcode == 7)
            System.out.println("DECODE LOGOUT/LOGSTAT");


        //Follow
        else if(opcode == 4) {
            System.out.println("DECODE FOLLOW");

            //Follow | unfollow
            String str = new String(bytes,2,1, StandardCharsets.UTF_8);
            System.out.println(str);
            char[] followArr = str.toCharArray(); //Converts this string to a new character array
            char follow = followArr[0];
            clientMessage.setFollow(follow);

            //Username
            String username = new String(bytes,3,len-3, StandardCharsets.UTF_8);
            clientMessage.setUsername(username);

        }

        //Post
        else if(opcode == 5){
            System.out.println("DECODE POST");
            String str = new String(bytes, 2, len - 3, StandardCharsets.UTF_8); //Remove \0 byte
            clientMessage.setContent(str);

        }

        //PM
        else if (opcode == 6) {
            System.out.println("DECODE PM");

            //Constructs a new String by decoding the specified subarray of bytes using the UTF_8 charset
            String str = new String(bytes,2,len-3, StandardCharsets.UTF_8);
            String [] strArr = str.split("\0");

            clientMessage.setUsername(strArr[0]);
            clientMessage.setContent(strArr[1]);
            clientMessage.setSendingDate(strArr[2]);

        }

        //Stat
        else if(opcode == 8){
            System.out.println("DECODE STAT");

            String str = new String(bytes, 2, len - 3, StandardCharsets.UTF_8); //Remove \0 byte
            clientMessage.setUsersNamesList(str);

        }

        //Block
        else if(opcode == 12){
            System.out.println("DECODE BLOCK");

            String str = new String(bytes, 2, len - 3, StandardCharsets.UTF_8); //Remove \0 byte
            clientMessage.setUsername(str);

        }
        len = 0;
        System.out.println("Message sent from ENCDED");
        return clientMessage;

    }

    //ENCODE the Server's Message before sending it to the Client
    @Override
    public byte[] encode(Message message) {
        ServerMessage serverMessage = (ServerMessage) message;
        short firstOpcode = serverMessage.getFirstOpcode();

        //Notification
        if (firstOpcode == 9) {
            System.out.println("ENCODE NOTIFICATION");

            //First opcode
            byte[] firstOpcodeByte = shortToBytes(firstOpcode);

            //NotificationType
            char notificationType = serverMessage.getNotificationType();
            byte[] notificationTypeByte = {(byte) notificationType};

            //PostingUser
            String postingUser = serverMessage.getPostingUser();
            byte [] postingUserByte = postingUser.getBytes();//getBytes method converts String into Bytes

            //Content
            String content = serverMessage.getContent();
            byte [] contentByte = content.getBytes();

            //End zero byte
            byte[] zero = {'\0'};

            //End Nekuda-Psik
            byte[] nekuda = {';'};

            //Combine all byte arrays
            byte[] b1 = combineBytes(firstOpcodeByte,notificationTypeByte);
            byte[] b2 = combineBytes(b1,postingUserByte);
            byte[] b3 = combineBytes(b2,zero);
            byte[] b4 = combineBytes(b3,contentByte);
            byte[] b5 = combineBytes(b4,zero);
            byte[] bMessage = combineBytes(b5,nekuda);

            return bMessage;
        }

        //Ack
        else if(firstOpcode == 10) {
            System.out.println("ENCODE ACK");

            byte[] firstOpcodeByte = shortToBytes(firstOpcode);
            short secondOpcode = serverMessage.getSecondOpcode();
            byte[] secondOpcodeByte = shortToBytes(secondOpcode);

            //Follow
            if(secondOpcode==4) {
                System.out.println("ENCODE FOLLOW ACK");

                String username = serverMessage.getUsername();
                byte [] usernameByte = username.getBytes();
                byte[] zero = {'\0'};
                //End Nekuda-Psik
                byte[] nekuda = {';'};

                byte[] b1 = combineBytes(firstOpcodeByte,secondOpcodeByte);
                byte[] b2 = combineBytes(b1, usernameByte);
                byte[] b3 = combineBytes(b2,zero);
                byte[] bMessage = combineBytes(b3, nekuda);

                return bMessage;
            }

            //Logstat | Stat

            if (secondOpcode == 7 || secondOpcode == 8) {
                System.out.println("ENCODE LOGSTAT/STAT ACK");

                short age = serverMessage.getAge();
                byte[] ageByte = shortToBytes(age);

                short numPosts = serverMessage.getNumPosts();
                byte[] numPostsByte = shortToBytes(numPosts);

                short numFollowers = serverMessage.getNumFollowers();
                byte[] numFollowersByte = shortToBytes(numFollowers);

                short numFollowing = serverMessage.getNumFollowing();
                byte[] numFollowingByte = shortToBytes(numFollowing);

                //End Nekuda-Psik
                byte[] nekuda = {';'};

                byte[] b1 = combineBytes(firstOpcodeByte,secondOpcodeByte);
                byte[] b2 = combineBytes(b1,ageByte);
                byte[] b3 = combineBytes(b2,numPostsByte);
                byte[] b4 = combineBytes(b3,numFollowersByte);
                byte[] b5 = combineBytes(b4,numFollowingByte);
                byte[] bMessage = combineBytes(b5,nekuda);

                return bMessage;
            }

            //Generic Ack
            else {
                System.out.println("ENCODE GENERIC ACK");
                //End Nekuda-Psik
                byte[] nekuda = {';'};

                byte[] b1 = combineBytes(firstOpcodeByte,secondOpcodeByte);
                byte[] bMessage = combineBytes(b1, nekuda);
                return bMessage;
            }
        }

        //Error
        else{
            System.out.println("ENCODE ERROR");
            //End Nekuda-Psik
            byte[] nekuda = {';'};

            byte[] firstOpcodeByte = shortToBytes(firstOpcode);
            byte[] secondOpcodeByte = shortToBytes(serverMessage.getSecondOpcode());
            byte[] b1 = combineBytes(firstOpcodeByte,secondOpcodeByte);
            byte[] errorMessage = combineBytes(b1,nekuda);

            return errorMessage;
        }
    }

    private void pushByte(byte nextByte) {
        //If there is no enough space in the array, copy bytes to new array of double size
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len] = nextByte;
        len++;
    }

    public short bytesToShort(byte[] byteArr){
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    public static byte[] shortToBytes(short num){
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    public static byte[] combineBytes(byte[] arr1, byte[] arr2) {
        byte[] arr3 = new byte[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, arr3, 0, arr1.length);
        System.arraycopy(arr2, 0, arr3, arr1.length, arr2.length);
        return arr3;
    }

    private String popString(){
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        return result;
    }



}

