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

            System.out.println("\n\n----BROWSER REQUEST----\n\n");
            System.out.println(getRequest);


                String parsedHostname = Util.extractStringByPrefix(getRequest, "Host: ", "\n"); //extract important headers

                boolean isGetRequest = getRequest.substring(0,getRequest.indexOf("\n")).contains("GET"); //extract important headers
                if(isGetRequest) {
                    String link = Util.extractStringByPrefix(getRequest, "GET http://" + parsedHostname, " HTTP/"); //extract important headers
                    System.out.println("THE LINK WE WANT INSERTED IS " + link);
                    String toReplace = Util.extractStringByPrefix(getRequest, "GET ", " HTTP/");
                    System.out.println("WHAT TO REPLACE: " + toReplace);
                    if (!toReplace.isEmpty()) {
                        System.out.println("REPLACING!");
                        if (getRequest.contains(toReplace)) {
                            System.out.println("IT CONTAINS THE LINK");
                        } else {
                            System.out.println("IT DOES NOTTTT CONTAINS THE LINK");
                        }


                        getRequest = getRequest.replace(toReplace, link);

                    }
                    /**
                     *
                     * Feature 6 is partly implemented here, header extraction.
                     *
                     */
                    System.out.println("MODIFIED HEADER\n" + getRequest);
            }else{
                System.out.println("Its a POST request");

            }
            String parsedConnection = Util.extractStringByPrefix(getRequest, "Connection: ", "\n").toLowerCase(); //extract important headers
            if (parsedConnection.contains("keep-alive")) {
                getRequest = getRequest.replace(parsedConnection, "close"); //if Connection header has status "keep-alive", replace with "close"
            }


            Socket outgoingSocket = new Socket(parsedHostname, 80); //Connect to hostname with port 80

            OutputStream os = outgoingSocket.getOutputStream();

            PrintWriter webServerStream = new PrintWriter(os, true);

            webServerStream.println(getRequest); //sending http request to webserver

            InputStream is = outgoingSocket.getInputStream();



            String header = Util.readHeader(is);


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


            System.out.println("----SERVER RESPONSE----");

//            System.out.println(header);

            if (!isText) {
                output.write(header.getBytes());
                Util.streamContent(is, output);
            } else {
                //we know this is text
                String content = Util.readBytes(is);
//                System.out.println(content);
                if (Util.containsBannedWords(content)) {
                    //contains bad words
                    String redirect = "HTTP/1.1 301 Moved Permanently\r\nConnection: keep-alive\r\nLocation: http://www.ida.liu.se/~TDTS04/labs/2011/ass2/error2.html\r\n\r\n ";
                    byte[] he = redirect.getBytes();
//                    System.out.println(content);
                    output.write(he);
                } else {
//                    System.out.println(content);

                    output.write(header.getBytes());
                    output.write(content.getBytes());
                    //contains legit data
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
