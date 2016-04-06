package com.me.proxyserver;

import java.io.IOException;
import java.net.ServerSocket;

public class ProxyServer {
    public static void main(String[] args) throws IOException {
        System.out.println("Proxy started.");

        ServerSocket ss = null;
        int port = 1338;

        ss = new ServerSocket(port);
        System.out.println("Listening on port " + port);
        while (true) {
            new Client(ss.accept()).start();
        }
    }
}
