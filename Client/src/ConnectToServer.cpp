#include "../include/ConnectToServer.h"
#include "../include/connectionHandler.h"
#include <thread>
#include <iostream>
#include <mutex>

using namespace std;

///CONSTRUCTOR
ConnectToServer::ConnectToServer(ConnectionHandler *connectionHandler, bool *loggedIn) :
        connectionHandler(connectionHandler), loggedIn(loggedIn) {}


///this run should connect to the server and start the connection with him until the termination of the specific client or until the program is terminated.
void ConnectToServer::run() {
    char ch;
    char delimiter = ';';
    bool shouldStop = false;
    while (!shouldStop) {
        char byte[2];
        std::string frame;
        short opcode = 0;
        short subject = 0;
        connectionHandler->getBytes(byte, 2);
        opcode = bytesToShort(byte);
        if(opcode!=9 && opcode!=11) {
            connectionHandler->getBytes(byte, 2);
            subject = bytesToShort(byte);
        }
        if (opcode == 10 || opcode == 11) {
            if(subject==3)
                shouldStop= true;
            if (opcode == 10) {
                if (subject != 7 && subject != 8) {
                    std::string content;
                    connectionHandler->getFrameAscii(content, ';');
                    frame += "ACK " + std::to_string(subject) + " ";
                }
            }
                if (subject == 7 || subject == 8) {
                    frame+="ACK "+ std::to_string(opcode)+" ";
                    if(subject == 7)
                        frame+= "LOGSTAT 7 ";
                    else
                        frame+="STAT 8 ";
                    connectionHandler->getBytes(byte, 2) + " ";
                    short age = bytesToShort(byte);
                    frame+=std::to_string(age);
                    frame+=" ";
                    connectionHandler->getBytes(byte, 2);
                    short numPost = bytesToShort(byte);
                    frame+=std::to_string(numPost);
                    frame+=" ";
                    connectionHandler->getBytes(byte, 2);
                    short numFollowers = bytesToShort(byte);
                    frame+=std::to_string(numFollowers);
                    frame+=" ";
                    connectionHandler->getBytes(byte, 2);
                    short numFollowing = bytesToShort(byte);
                    frame+=std::to_string(numFollowing);
                    frame+=" ";
                    std::string content;
                    connectionHandler->getFrameAscii(content, ';');
                }
            }
            if (opcode == 11) {
                frame += "ERROR " + std::to_string(subject);
                std::string content;
                connectionHandler->getFrameAscii(content, ';');
            }
        else if (opcode == 9) {
            frame += "NOTIFICATION ";
            connectionHandler->getBytes(&ch, 1);
            if(ch == '1')
                frame += "Public ";
            else
                frame += "PM ";
            std::string content;
            connectionHandler->getFrameAscii(content, ';');
            frame += content;
        }
        std::cout << frame.substr(0,frame.length()-1) << std::endl;
    }
}

short ConnectToServer::bytesToShort(char *bytesArr) {
    short result = (short) ((bytesArr[0] & 0xff) << 8);
    result += (short) (bytesArr[1] & 0xff);
    return result;

}



