package com.me.proxyserver;

import com.sun.corba.se.spi.orbutil.fsm.Input;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

public class Client extends Thread {

    private InputStream input;
    private OutputStream output;

    public Client(Socket serversSocket) {

        try {
            input = serversSocket.getInputStream();
            output = serversSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        try {

            //Read the http request stream from the browser
            String getRequest = readStream(input, true);
            if (getRequest.isEmpty()) {
                return;
            }

            System.out.println("\n\n------------------BROWSER REQUEST-------------------\n\n");
            System.out.println(getRequest);

            String parsedHostname = extractStringByPrefix(getRequest, "Host: ", "\n"); //extract important headers
            String parsedConnection = extractStringByPrefix(getRequest, "Connection: ", "\n"); //extract important headers


            if (!parsedConnection.isEmpty())
                getRequest = getRequest.replaceAll(parsedConnection, "close"); //if Connection header has status "keep-alive", replace with "close"


            Socket outgoingSocket = new Socket(parsedHostname, 80); //Connect to hostname with port 80

            OutputStream os = outgoingSocket.getOutputStream();

            PrintWriter webServerStream = new PrintWriter(os, true);
            webServerStream.println(getRequest); //sending http request to webserver

            PrintWriter proxyServerStream = new PrintWriter(output, true);

            InputStream is = outgoingSocket.getInputStream();
            String response = readStream(is, false); //get webserver response
            proxyServerStream.println(response); //send the response back to browser

            System.out.println("\n\n------------------SERVER RESPONSE-------------------\n\n");
            System.out.println(response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Given an input stream, reads until the end
     */
    private String readStream(InputStream input, boolean breakIfEmpty) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        StringBuilder builder = new StringBuilder();

        String c;
        while ((c = br.readLine()) != null) {
            if (breakIfEmpty && c.isEmpty())
                break;
            builder.append(c + "\n");
        }

        return builder.toString();
    }

    /**
     * Extracts the string between two prefixes
     */

    public String extractStringByPrefix(String stringToSearch, String starPrefix, String endPrefix) {
        int startIndex = stringToSearch.indexOf(starPrefix) + starPrefix.length();
        int endIndex = stringToSearch.indexOf(endPrefix, startIndex);
        return stringToSearch.substring(startIndex, endIndex).trim();
    }

}
