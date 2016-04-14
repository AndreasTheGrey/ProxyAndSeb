package com.me.proxyserver;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Util {
    private static String[] bannedWords = new String[]{"SpongeBob", "Britney Spears", "Paris Hilton", "Norrkoping", "Norrköping"};


    /**
     * Given a string, checks if that string contains one of the words in bannedWords list.
     */
    public static boolean containsBannedWords(String toCheck) {
        toCheck = toCheck.toLowerCase();
        for (String word : bannedWords) {
            if (toCheck.contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Given an input stream, reads until the end
     */
    public static String readStream(InputStream input, boolean breakIfEmpty) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        StringBuilder builder = new StringBuilder();

        String c;
        while ((c = br.readLine()) != null) {
            if (c.isEmpty()) {
                if (breakIfEmpty) {
                    break;
                }

            }
            builder.append(c + "\n");
        }
        return builder.toString();
    }


    public static String readHeader(InputStream input) throws IOException{
        StringBuilder stringBuilder = new StringBuilder();

        int c;
        int counter = 0;
        while((c =input.read()) != -1){
            stringBuilder.append(Character.toString((char) c));

            if(c == 0x0D || c == 0x0A){
                counter++;
            }else{
                counter = 0;
            }

            if(counter > 3){
                return stringBuilder.toString();
            }

        }
        return stringBuilder.toString();
    }


    public static String readBytes(InputStream is) throws  IOException{
        StringBuilder builder = new StringBuilder();
        byte by[] = new byte[256];
        int index = is.read(by, 0, 256);
        while (index != -1) {
            byte[] bytes = new byte[index];
            System.arraycopy(by, 0, bytes, 0, index);
            builder.append(new String(bytes, StandardCharsets.UTF_8));
            index = is.read(by, 0, 256);
        }
        return builder.toString();
    }


    public static void streamContent(InputStream input, OutputStream output) throws IOException{

        byte by[] = new byte[256];
        int index = input.read(by, 0, 256);
        while (index != -1) {
            output.write(by, 0, index);
            index = input.read(by, 0, 256);
        }
    }

    /**
     * Extracts the string between two prefixes
     */
    public static String extractStringByPrefix(String stringToSearch, String starPrefix, String endPrefix) {
        int startIndex = stringToSearch.indexOf(starPrefix) + starPrefix.length();
        if (startIndex == -1) {
            return "";
        }
        int endIndex = stringToSearch.indexOf(endPrefix, startIndex);
        if (endIndex == -1) {
            return "";
        }
        return stringToSearch.substring(startIndex, endIndex).trim();
    }
}
