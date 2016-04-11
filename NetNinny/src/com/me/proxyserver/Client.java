package com.me.proxyserver;

import com.sun.corba.se.spi.orbutil.fsm.Input;

import java.awt.image.DataBufferByte;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.Buffer;
import java.nio.ByteBuffer;
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

//            System.out.println("\n\n------------------BROWSER REQUEST-------------------\n\n");
//            System.out.println(getRequest);

            String parsedHostname = Util.extractStringByPrefix(getRequest, "Host: ", "\n"); //extract important headers
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

            PrintWriter proxyServerStream = new PrintWriter(output, true);
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
//            System.out.println("----Server Response-----");

            boolean allowed = true;
            if (isText) {
                String response = builder.toString().toLowerCase();
                if (Util.containsBannedWords(response)) {
                    String redirect = "HTTP/1.1 301 Moved Permanently\r\nConnection: keep-alive\r\nLocation: http://www.ida.liu.se/~TDTS04/labs/2011/ass2/error2.html\r\n\r\n ";
                    byte[] he = redirect.getBytes();
                    output.write(he);
                    allowed = false;
                }
            }

            if (allowed) {
                for (byte[] b : bufferBytes) {
                    //System.out.println(builder.toString());
                    output.write(b);
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
