//
// Created by 97250 on 30/12/2021.
//

#ifndef CLIENT_CONNECTTOSERVER_H
#define CLIENT_CONNECTTOSERVER_H
#include "connectionHandler.h"

class ConnectToServer {
private:
    ConnectionHandler* connectionHandler;
public:
    bool* loggedIn;
    ConnectToServer(ConnectionHandler* connectionHandler, bool *loggedIn);
    void run();
    short bytesToShort(char* bytesArr);
};


#endif //CLIENT_CONNECTTOSERVER_H
