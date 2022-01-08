package bgu.spl.net.srv;

import bgu.spl.net.api.ClientMessage;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.ConnectionsImpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private int connid;

    public BlockingConnectionHandler(int connid, Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.connid = connid;
        this.protocol.start(connid, ConnectionsImpl.getInstance());
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {

                T nextMessage = encdec.decodeNextByte((byte) read);

                if (nextMessage != null) {
                    System.out.println("Message sent to process");
                    protocol.process(nextMessage);
                    if(!sock.isClosed())
                        in = new BufferedInputStream(sock.getInputStream());
                }

                if(protocol.shouldTerminate())
                    ConnectionsImpl.getInstance().disconnect(connid);

            }


        } catch (IOException ex) {
            ex.printStackTrace();
        }}


        @Override
        public void close () throws IOException {
            connected = false;
            sock.close();
        }

        @Override
        public void send (T msg){

            try {
                if (msg != null) {
                    out.write(encdec.encode(msg));
                    System.out.println("Write to Client");
                    out.flush();
                    System.out.println("Message sent to Client" + connid);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    public int getConnid() {
        return connid;
    }

    }


