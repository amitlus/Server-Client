//
// Created by 97250 on 30/12/2021.
//

#ifndef CLIENT_CONNECTTOUSER_H
#define CLIENT_CONNECTTOUSER_H
#include "connectionHandler.h"

class ConnectToUser {
private:
    ConnectionHandler* connectionHandler;
public:
    bool* loggedIn;
    ConnectToUser(ConnectionHandler* connectionHandler, bool* loggedIn);
    void run();
    std::string convert(std::string line, bool *loggedIn, bool shouldStop);
    void shortToBytes(short num, char* bytesArr);
};


#endif //CLIENT_CONNECTTOUSER_H
