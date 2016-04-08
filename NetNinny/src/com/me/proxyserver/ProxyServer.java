package com.me.proxyserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

public class ProxyServer {
    public static void main(String[] args) throws IOException {
        System.out.println("Proxy started.");

        ServerSocket ss = null;
        int port = 1338;

//        Scanner input = new Scanner(System.in);
//        System.out.print("Please enter a port number: ");
//        String answer = input.nextLine();
//        if(!answer.equals("")){
//            port = Integer.parseInt(answer);
//        }




        ss = new ServerSocket(port);
//        String redirect = "HTTP/1.1 302 Found Location: http://www.ida.liu.se/~TDTS04/labs/2011/ass2/error1.html";


        System.out.println("Listening on port " + port);
        while (true) {
            new Client(ss.accept()).start();
        }
    }
}
