package bgu.spl.net.impl;

import bgu.spl.net.api.DataBase;
import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.srv.Server;

public class TPCMain {

    public static void main(String[] args){
        DataBase.getInstance();
        Server server= Server.threadPerClient(Integer.parseInt(args[0]),()-> new BidiMessagingProtocolImpl(),()-> new MessageEncoderDecoderImpl());
        server.serve();
    }
}
