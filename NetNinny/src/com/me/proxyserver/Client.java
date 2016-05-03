package com.me.proxyserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Client extends Thread {

    private InputStream input;
    private OutputStream output;
    private String getRequest;

    public Client(Socket serversSocket, String getRequest) {

        try {
            input = serversSocket.getInputStream();
            output = serversSocket.getOutputStream();
            this.getRequest = getRequest;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        try {
            //Read the http request stream from the browser
            if (getRequest.isEmpty()) {
                return;
            }

            String parsedHostname = Util.extractStringByPrefix(getRequest, "Host: ", "\n"); //extract important headers

            boolean isGetRequest = getRequest.substring(0, getRequest.indexOf("\n")).contains("GET"); //extract important headers
            if (isGetRequest) {
                String link = Util.extractStringByPrefix(getRequest, "GET http://" + parsedHostname, " HTTP/"); //extract important headers
                String toReplace = Util.extractStringByPrefix(getRequest, "GET "," HTTP/");
                if (!toReplace.isEmpty()) {
                    getRequest = getRequest.replace(toReplace, link);
                }
            }
            /**
             *
             * Feature 6 is partly implemented here, header extraction.
             *
             */
            String parsedConnection = Util.extractStringByPrefix(getRequest, "Connection: ", "\n").toLowerCase(); //extract important headers
            if (parsedConnection.contains("keep-alive")) {
                getRequest = getRequest.replace(parsedConnection, "close"); //if Connection header has status "keep-alive", replace with "close"
            }

            Socket outgoingSocket = new Socket(parsedHostname, 80); //Connect to hostname with port 80

            OutputStream os = outgoingSocket.getOutputStream();

            PrintWriter webServerStream = new PrintWriter(os, true);
            webServerStream.println(getRequest); //sending http request to webserver
            webServerStream.flush();

            InputStream is = outgoingSocket.getInputStream();
            String header = Util.readHeader(is);


            String headerConnection = Util.extractStringByPrefix(header, "Connection: ", "\n").toLowerCase(); //extract important headers
            if (headerConnection.contains("keep-alive")) {
                header = header.replace(headerConnection, "close"); //if Connection header has status "keep-alive" sometimes forces this, replace with "close"
            }


            /**
             *
             * Check here if it's none gziped text, so that we can determine if we want to look through the content.
             *
             */
            boolean isText = true;
            String contentType = Util.extractStringByPrefix(header, "Content-Type: ", "\n");
            if (contentType.contains("text")) {
                String contentEncoding = Util.extractStringByPrefix(header, "Content-Encoding: ", "\n");
                if (contentEncoding.contains("gzip")) {
                    isText = false;
                }
            } else {
                isText = false;
            }

            /**
             *
             * Reads to the output stream, streams if not text, otherwise we convert it to String and then search for bad words.
             *
             */
            if (!isText) {
                output.write(header.getBytes() );
                Util.streamContent(is, output);
            } else {
                String content = Util.readBytes(is);

                if (Util.containsBannedWords(content)) {
                    String redirect = "HTTP/1.1 301 Moved Permanently\r\nConnection: keep-alive\r\nLocation: http://www.ida.liu.se/~TDTS04/labs/2011/ass2/error2.html\r\n\r\n ";
                    byte[] he = redirect.getBytes();
                    output.write(he);
                } else {
                    output.write(header.getBytes());
                    output.write(content.getBytes());
                }
            }

            output.flush();
            os.close();
            is.close();
            outgoingSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
