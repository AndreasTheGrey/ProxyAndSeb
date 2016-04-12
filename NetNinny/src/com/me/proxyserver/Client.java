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
            boolean isText = false;
            //Read the http request stream from the browser
            if (getRequest.isEmpty()) {
                return;
            }

            System.out.println("\n\n----BROWSER REQUEST----\n\n");
            System.out.println(getRequest);

            String parsedHostname = Util.extractStringByPrefix(getRequest, "Host: ", "\n"); //extract important headers
            /**
             *
             * Feature 6 is partly implemented here, header extraction.
             *
             */
            String parsedConnection = Util.extractStringByPrefix(getRequest, "Connection: ", "\n"); //extract important headers
            String parsedAcceptedFormat = Util.extractStringByPrefix(getRequest, "Accept: ", "/");

            if (parsedAcceptedFormat.toLowerCase().equals("text")) {
                isText = true;
            }


            if (!parsedConnection.isEmpty()) {
                getRequest = getRequest.replaceAll(parsedConnection.toLowerCase(), "close"); //if Connection header has status "keep-alive", replace with "close"
            }


            Socket outgoingSocket = new Socket(parsedHostname, 80); //Connect to hostname with port 80

            OutputStream os = outgoingSocket.getOutputStream();

            PrintWriter webServerStream = new PrintWriter(os, true);

            webServerStream.println(getRequest); //sending http request to webserver

            InputStream is = outgoingSocket.getInputStream();
            StringBuilder builder = new StringBuilder();


            List<byte[]> bufferBytes = new ArrayList<byte[]>();
            byte by[] = new byte[256];
            int index = is.read(by, 0, 256);
            while (index != -1) {
                byte[] bytes = new byte[index];
                System.arraycopy(by, 0, bytes, 0, index);
                bufferBytes.add(bytes);
                builder.append(new String(bytes, StandardCharsets.UTF_8));
                index = is.read(by, 0, 256);
            }
            System.out.println("----SERVER RESPONSE----");
            System.out.println(builder.toString());

            boolean doWrite = true;
            if (isText) {
                String response = builder.toString().toLowerCase();
                /**
                 *
                 * Feature 8 is implemented here, content redirect.
                 *
                 */
                if (Util.containsBannedWords(response)) {
                    System.out.println("CONTAINS BAD WORDS IN CONTENT");
                    String redirect = "HTTP/1.1 301 Moved Permanently\r\nConnection: keep-alive\r\nLocation: http://www.ida.liu.se/~TDTS04/labs/2011/ass2/error2.html\r\n\r\n ";
                    byte[] he = redirect.getBytes();
                    output.write(he);
                    doWrite = false;
                }
            }

            if (doWrite) {
                for (byte[] b : bufferBytes) {
                    try {
                        output.write(b);
                    } catch (IOException e) {
                        output.close();
                    }
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
