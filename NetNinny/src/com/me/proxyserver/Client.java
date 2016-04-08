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
            boolean isImage = false;
            //Read the http request stream from the browser
            String getRequest = readStream(input, true);
            if (getRequest.isEmpty()) {
                return;
            }

            System.out.println("\n\n------------------BROWSER REQUEST-------------------\n\n");
            System.out.println(getRequest);

            String parsedHostname = extractStringByPrefix(getRequest, "Host: ", "\n"); //extract important headers
            String parsedConnection = extractStringByPrefix(getRequest, "Connection: ", "\n"); //extract important headers
            String parsedAcceptedFormat = extractStringByPrefix(getRequest, "Accept: ", "/");
            if (parsedAcceptedFormat.toLowerCase().equals("image")) {
                System.out.println("YEAH ITS A FUCKING IMAGE!");
                isImage = true;
            }


            if (!parsedConnection.isEmpty()) {
                getRequest = getRequest.replaceAll(parsedConnection, "close"); //if Connection header has status "keep-alive", replace with "close"
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
            System.out.println("----Server Response-----");

            boolean allowed = true;
            if (!isImage) {
                System.out.println("About to check bad words");
                String response = builder.toString().toLowerCase();
                String[] bannedWords = new String[]{"SpongeBob", "Britney Spears", "Paris Hilton", "Norrköping"};
                System.out.println("About to check the following string for bad words");
                for (String bannedWord : bannedWords) {

                    System.out.println("Checking if contains " + bannedWord);
                    if (response.contains(bannedWord.toLowerCase())) {
                        System.out.println("Detected bad words");
                        String redirect = "HTTP/1.1 301 Moved Permanently\r\nConnection: keep-alive\r\nLocation: http://www.ida.liu.se/~TDTS04/labs/2011/ass2/error2.html\r\n\r\n ";
                        byte[] he = redirect.getBytes();
                        output.write(he);
                        allowed = false;
                    }
                }
            }

            if (allowed) {
                System.out.println("EWDACESREFGMJHNTRFHGJ");
                for (byte[] b : bufferBytes) {
                    System.out.println(builder.toString());
                    output.write(b);
                }
            }
            output.flush();
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
            if (c.isEmpty()) {
                if (breakIfEmpty) {
                    break;
                } else {
                    String parsedContentType = extractStringByPrefix(builder.toString(), "Content-Type: ", "/");
                    if (parsedContentType.toLowerCase().contains("image")) {

                    }
                }
            }
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
