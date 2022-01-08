#include <stdlib.h>
#include <mutex>
#include <thread>
#include <iostream>
#include "../include/connectionHandler.h"
#include "../include/ConnectToServer.h"
#include "../include/ConnectToUser.h"
using namespace std;
int main (int argc, char *argv[]) {
    //if (argc < 3) {
    //    cerr << "Usage: " << argv[0] << " host port" << endl << endl;
    //    return -1;
    //}
//    string host = argv[1];
//    short port = atoi(argv[2]);
    short port = 7077;
    string host = "127.0.0.1";
    ConnectionHandler* connectionHandler= new ConnectionHandler(host, port);
    if (!connectionHandler->connect()) {
        cerr << "Cannot connect to " << host << ":" << port << endl;
        return 1;
    }

    /// if we manage to connect we should create two different threads - one to connect the server and one to connect to the user keyboard.

    bool* loggedIn=new bool(false);
    ConnectToServer cts(connectionHandler, loggedIn);
    ConnectToUser ctu(connectionHandler, loggedIn);
    thread t2(&ConnectToServer:: run, &cts);
    thread t1(&ConnectToUser:: run, &ctu);


    t2.join();
    delete connectionHandler;
    delete loggedIn;
    return 0;
}