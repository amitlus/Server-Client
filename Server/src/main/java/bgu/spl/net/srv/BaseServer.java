package bgu.spl.net.srv;

import bgu.spl.net.api.DataBase;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.ConnectionsImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public abstract class BaseServer<T> implements Server<T> {

    private final int port;
    private final Supplier<BidiMessagingProtocol<T>> protocolFactory;
    private final Supplier<MessageEncoderDecoder<T>> encdecFactory;
    private ServerSocket sock;
    //We will update the hash map in connection when a handler made and when the handler close
    private final ConnectionsImpl connections = ConnectionsImpl.getInstance();
    private final DataBase dataBase = DataBase.getInstance();


    public BaseServer(
            int port,
            Supplier<BidiMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encdecFactory) {

        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
		this.sock = null;
    }

    @Override
    public void serve() {

        try (ServerSocket serverSock = new ServerSocket(port)) {
			System.out.println("Server started");

            this.sock = serverSock;

            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSock = serverSock.accept();
                System.out.println("Connection");

                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler<>(
                        dataBase.getNumOfConnections(),
                        clientSock,
                        encdecFactory.get(),
                        protocolFactory.get());

                //Adding the Connection handler's id to the HM and map it to the Connection handler itself
                connections.getConnidToCh().put(dataBase.getNumOfConnections(), handler);
                System.out.println("Handler made "+handler.getConnid());
                dataBase.increaseNumOfConnections();

                execute(handler);//Run the Connection Handler
            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
		if (sock != null)
			sock.close();
    }

    //TPC case, so run the Connection Handler as a Thread
    protected abstract void execute(BlockingConnectionHandler<T> handler);

}
