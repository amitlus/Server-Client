# Server-Client  

My third project in SPL (system programing langueges) course in the 3rd semester in my Computer Science Degree.
This is a Server-Client project which simulates the behaviour of "social media network".

---I CODED THE *SERVER SIDE* & MY PARTNER CODED THE CLIENT SIDE---

The assingment's pdf is added

Final grade 100 


HOW TO RUN OUR CODE:  
***Run on Linux machine***  
boost should be installed!  

***FROM THE TERMINAL***
1. Open 2 Terminals - 1 for the Server and 1 for the Client

2. In the "Server Terminal":

    cd Server
    mvn clean
    mvn compile
    
    FOR TPC:
    mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.TPCMain" -Dexec.args="7077"
    
    FOR REACTOR:
    mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.ReactorMain" -Dexec.args="7077 5" // You can change the number of threads
    
3. In the "Client Terminal":
    make
    ./bin/BGSclient 127.0.0.1 7077
    

    
***FROM THE IDE***
1. Server- go to src/main/java/bgu/spl/net/impl 
2. Choose between TPCMain and ReactorMain
2.1 For TPCMain - Edit Configuration -> Program Arguments -> insert 7077
2.2 For ReactorMain - Already declared in the code itself- just run it. (You can edit the number of threads if you want)
3. In the Client side- when you enter a command, the command itself should be written using CAPITAL LETTERS ONLY.
   The seperation of the DATE in the REGISTER command should be written with '-'.
4. The forbidden words located in the BidiMessagingProtocol as an String array, and you can add/remove words as you wish.
