package com.spamdetector.util;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class EmailParser {


    public Map<String, Integer> getWordFrequency(File emailDirectory){

        //2 Map we'll merger our UN
        Map<String, Integer> wordFrequencyMap = new TreeMap<>();

        //Read all files, return as a list of objects
        File[] emailFiles = emailDirectory.listFiles();
        int emailsFilesCount = emailFiles.length;

        for (File email: emailFiles){
            // Let's produce a word frequency map, for each file

           Map<String, Integer> emailFrequencyMap = calculateWordFrequency(email);

            Set<String> words = emailFrequencyMap.keySet();
            Iterator<String> iterator = words.iterator();

            while (iterator.hasNext()){
                String word = iterator.next();
                int wordCount = emailFrequencyMap.get(word);

                if (!wordFrequencyMap.containsKey(word)){
                    wordFrequencyMap.put(word, wordCount);
                } else {
                    int oldCount = wordFrequencyMap.get(word);
                    wordFrequencyMap.put(word, oldCount + wordCount);
                }
            }

        }

        return wordFrequencyMap;

    }
    private Map<String, Integer> calculateWordFrequency(File email){
        Map<String, Integer> emailWordFrequencyMap = new TreeMap<>();

        try {
            Scanner emailScanner = new Scanner(email);

            while (emailScanner.hasNext()){
                String word = emailScanner.next().toLowerCase();

                if(isWord(word)){
                    // Here we'll start checking if the word currently exist
                    if (!emailWordFrequencyMap.containsKey(word)){
                        emailWordFrequencyMap.put(word, 1);

                    }
                }


            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        //All files MUST merge them 1
        return emailWordFrequencyMap;
    }











    public boolean isWord(String word){
        if(word == null | "".equals(word))
            return false;

        String acceptablePattern = "^[a-z]*$";
        if (word.matches(acceptablePattern))
            return true;

        return false;
    }


















}
