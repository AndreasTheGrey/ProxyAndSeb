package com.me.proxyserver;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Util {
    private static String[] bannedWords = new String[]{"SpongeBob", "Britney Spears", "Paris Hilton", "Norrkoping", "Norrköping"};
    public static boolean containsBannedWords(String toCheck){
        toCheck = toCheck.toLowerCase();
        for(String word : bannedWords){
            if(toCheck.contains(word.toLowerCase())){
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
    public static String extractStringByPrefix(String stringToSearch, String starPrefix, String endPrefix) {
        int startIndex = stringToSearch.indexOf(starPrefix) + starPrefix.length();
        if(startIndex == -1){
            return "";
        }
        int endIndex = stringToSearch.indexOf(endPrefix, startIndex);
        if(endIndex == -1){
            return "";
        }
        return stringToSearch.substring(startIndex, endIndex).trim();
    }
}
