package bgu.spl.net.impl;

import bgu.spl.net.api.DataBase;
import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.srv.Server;

public class ReactorMain {

    public static void main (String[] args) {

        Server server = Server.reactor(7,7077, () -> new BidiMessagingProtocolImpl(),()-> new MessageEncoderDecoderImpl());
        server.serve();
    }

}
