CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lboost_thread -pthread

all: BGSclient
	g++ -o bin/BGSclient bin/connectionHandler.o bin/Client.o bin/ConnectToServer.o bin/ConnectToUser.o $(LDFLAGS)

BGSclient: bin/connectionHandler.o bin/Client.o bin/ConnectToServer.o bin/ConnectToUser.o

bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/Client.o: src/Client.cpp
	g++ $(CFLAGS) -o bin/Client.o src/Client.cpp

bin/ConnectToServer.o: src/ConnectToServer.cpp
	g++ $(CFLAGS) -o bin/ConnectToServer.o src/ConnectToServer.cpp

bin/ConnectToUser.o: src/ConnectToUser.cpp
	g++ $(CFLAGS) -o bin/ConnectToUser.o src/ConnectToUser.cpp


.PHONY: clean
clean:
	rm -f bin/*
