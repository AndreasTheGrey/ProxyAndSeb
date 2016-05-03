package com.me.proxyserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ProxyServer {
    public static void main(String[] args) throws IOException {
        System.out.println("Proxy started.");

        ServerSocket ss;
        int port = 1338;

        /**
         *
         * Feature 7 is implemented here, custom port.
         *
         */
        Scanner input = new Scanner(System.in);
        System.out.print("Please enter a port number: ");
        String answer = input.nextLine();
        if (!answer.equals("")) {
            port = Integer.parseInt(answer);
        }


        ss = new ServerSocket(port);
        while (true) {
            Socket clientServerSocket = ss.accept();
            String getRequest = Util.readHeader(clientServerSocket.getInputStream());
            /**
             *
             * Feature 6 is partly implemented here, header extraction.
             *
             */
            String url = Util.extractStringByPrefix(getRequest, "www", "\n");
            /**
             *
             * Feature 3 is implemented here, URL redirect.
             *
             */
            if (Util.containsBannedWords(url)) {
                String redirectURL = "HTTP/1.1 301 Moved Permanently\r\nConnection: keep-alive\r\nLocation: http://www.ida.liu.se/~TDTS04/labs/2011/ass2/error1.html\r\n\r\n ";
                byte[] byteReponse = redirectURL.getBytes();
                clientServerSocket.getOutputStream().write(byteReponse);
                clientServerSocket.getOutputStream().flush();
            } else {
                new Client(clientServerSocket, getRequest).start();
            }
        }
    }
}
