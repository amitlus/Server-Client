#include "../include/ConnectToUser.h"
#include "../include/connectionHandler.h"
#include <thread>
#include <iostream>
#include <ctime>
using namespace std;

    ConnectToUser::ConnectToUser(ConnectionHandler* connectionHandler, bool* loggedIn):
        connectionHandler(connectionHandler), loggedIn(loggedIn){}


    ///this run should connect the thread to the user meaning it always need to get input from the keyboard and act accordingly.
    void ConnectToUser::run() {
        bool shouldStop = false;
        while (!shouldStop) {
            const short bufSize = 1024; ///keeping the max input to 1k
            char buf[bufSize];
            cin.getline(buf, bufSize);
            string line(buf);
            string encoded = "";
//            int space = line.find(" ");
//            if (space!=-1)
//                std::cout<<line.substr(0,space)<< "sent to the server"<<std::endl;
//            else
//                std::cout<<line<<" sent to the server"<<std::endl;
            encoded = convert(line, loggedIn, shouldStop);
            if (encoded.length() == 0)
                continue;
            if (!connectionHandler->sendLine(encoded)) {
                cout << "Disconnected due to send failure" << endl;
                break;
            }
            std::cout<<"What I Sent "+ encoded<<std::endl;
        }
    }


    string ConnectToUser::convert(string line, bool *loggedIn, bool shouldStop){
    string output="";
    string command;
    int f = line.find(" ");
    if(f!=-1) {//in case there were no spaces in the input
        command = line.substr(0, f);
        line = line.substr(f + 1);
    }
    else
        command = line;
    if(command.compare("REGISTER")==0){
        char arr[2];
        short num = 1;
        shortToBytes(num, arr);
        output+=(arr[0]);    //opcode
        output+=arr[1];
        int space = line.find(" ");
        output.append(line.substr(0,space)); //UserName
        output.append(1,'\0');
        line=line.substr(space+1);
        space = line.find(" ");
        output.append(line.substr(0,space));  //Password
        output.append(1,'\0');
        line = line.substr(space+1);
        space = line.find(" ");
        output.append(line.substr(0,space));  //Birthday
        output.append(1,'\0');
        return output;
    }

    else if(command.compare("LOGIN")==0){
        char arr[2];
        short num = 2;
        shortToBytes(num, arr);
        output+=arr[0];    //opcode
        output+=arr[1];
        int space =line.find(" ");
        output.append(line.substr(0,space)); //UserName
        output.append(1,'\0');
        line = line.substr(space+1);
        space = line.find(" ");
        output.append(line.substr(0,space));  //Password
        output.append(1,'\0');
        line = line.substr(space+1);
        space = line.find(" ");
        output.append(line.substr(0,line.length()));//  Capcha
        return output;
    }


    else if(command.compare("LOGOUT")==0){
        if(*loggedIn = true)// in case the client is logged in and there was a logout action we should stop the thread.
            shouldStop = true;
        char arr[2];
        short num = 3;
        shortToBytes(num, arr);
        output+=arr[0];    //opcode
        output+=arr[1];
        return output;
    }

    else if(command.compare("FOLLOW")==0){
        char arr[2];
        short num = 4;
        char ch[2];
        shortToBytes(num, arr);
        output+=arr[0];    //opcode
        output+=arr[1];
        int space = line.find(" ");
        short x= stoi(line.substr(0,space));// Follow or Unfollow (0/1)
        shortToBytes(x,ch);
        output+=ch[1];
        line = line.substr(space+1);
        output.append(line.substr(0,line.length())); //UserName
        return output;
    }


    else if(command.compare("POST")==0){
        char arr[2];
        short num = 5;
        shortToBytes(num, arr);
        output+=(arr[0]);    //opcode
        output+=arr[1];        output.append(line.substr(0,line.length())); //content
        output.append(1,'\0');
        return output;
    }

    else if(command.compare("PM")==0){///SHOULD ALSO ADD TIME AND DATE!
        char arr[2];
        short num = 6;
        shortToBytes(num, arr);
        output+=(arr[0]);    //opcode
        output+=arr[1];
        int space = line.find(" ");
        output.append(line.substr(0,space)); //User Name
        output.append(1,'\0');
        line =line.substr(space+1);
        output.append(line.substr(0,line.length())); //Content
        output.append(1,'\0');

        time_t rawTime;
        struct tm* timeInfo;
        time(&rawTime);
        timeInfo= localtime(&rawTime);
        std::string currentTime="";
        if(timeInfo->tm_mday<10) {///adding the current day
            currentTime+=("0");
            currentTime+=(std::to_string(timeInfo->tm_mday));

        }
        else
            currentTime+=(std::to_string(timeInfo->tm_mday));
//        output.append(1,'\0');
        currentTime+="-";
        if(timeInfo->tm_mon<9) {///adding the current month
            currentTime += "0";
            currentTime+=(std::to_string(timeInfo->tm_mon+1));
        }
        else
            currentTime+=(std::to_string(timeInfo->tm_mon));
        currentTime+="-";
        currentTime+=std::to_string(timeInfo->tm_year+1900);///getting the current year
        currentTime+=" ";
        currentTime+=std::to_string(timeInfo->tm_hour);
        currentTime.append(":");
        currentTime+=std::to_string(timeInfo->tm_min);
        output.append(currentTime);
        output.append(1,'\0');

    }
    else if(command.compare("LOGSTAT")==0){
        char arr[2];
        short num = 7;
        shortToBytes(num, arr);
        output+=(arr[0]);    //opcode
        output+=arr[1];
        return output;

    }
    else if(command.compare("STAT")==0){
        char arr[2];
        short num = 8;
        shortToBytes(num, arr);
        output+=(arr[0]);    //opcode
        output+=arr[1];
        output.append(line.substr(0,line.length())); // List Of User Names
        output.append(1,'\0');
        return output;
    }
    else if(command.compare("BLOCK")==0){
        char arr[2];
        short num = 12;
        shortToBytes(num, arr);
        output+=(arr[0]);    //opcode
        output+=arr[1];
        output.append(line.substr(0,line.length()));
        output.append(1,'\0');
        return output;
    }
}
void ConnectToUser:: shortToBytes(short num, char* bytesArr)
{
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}
