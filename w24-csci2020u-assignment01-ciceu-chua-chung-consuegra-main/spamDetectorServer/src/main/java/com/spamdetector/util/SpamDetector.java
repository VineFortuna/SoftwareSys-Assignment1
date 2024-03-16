package com.spamdetector.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spamdetector.domain.TestFile;
import jakarta.ws.rs.core.Response;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;


/**
 * TODO: This class will be implemented by you
 * You may create more methods to help you organize you strategy and make you code more readable
 */
public class SpamDetector {
    ObjectMapper objectMapper =new ObjectMapper();


    public int trainHamCount2 = totalFilesInFolder("train/ham");
    public int trainHamCount= totalFilesInFolder("train/ham2");
    public int totalHamCount = trainHamCount + trainHamCount2;
    public int trainSpamCount = totalFilesInFolder("train/spam");



    public Map<String, Integer> trainHamFreq1 = frequMap("train/ham");
    public Map<String, Integer> trainHamFreq2 = frequMap("train/ham2");
    public Map<String, Integer> trainHamFreq = addMaps(trainHamFreq1, trainHamFreq2);

    public Map<String, Integer> trainSpamFreq = frequMap("train/spam");



    //Training: spam/ham subdirectory

    //Pr(Wi|S)
    public Map<String, Double> probWordAppearsInSpam =
            calculateProb(trainSpamFreq, trainSpamCount);
    //Pr(Wi|H)
    public Map<String, Double> probWordAppearsInHam =
            calculateProb(trainHamFreq,totalHamCount);


    //Pr(S|Wi), Training: P(S|Wi)
    public Map<String, Double> probFileIsSpam = calcProbFileIsSpam
            (probWordAppearsInSpam, probWordAppearsInHam);




    public List<Map<String, Object>> result = finalResult("ham");


/*
    public List<TestFile> trainAndTest(File mainDirectory) {
       TODO: main method of loading the directories and files, training and testing the model;
        return new ArrayList<TestFile>();
    }

 */


    //Here We must pass probWordAppearsInHam, probWordAppearsInSpam
    public Map<String, Double> calcProbFileIsSpam(Map<String, Double> spamMap, Map<String, Double> hamMap){
        Map<String, Double> temp = new TreeMap<>();

        /**
         * Here we are retrieving every word in the spam and ham map provided
         * and adding the keys to the map 'probFileIsSpam' with value
         * of zero
         */
        for (Map.Entry<String, Double> entry: spamMap.entrySet()){
            String key = entry.getKey();
            temp.put(key, 0.0);
        }
        for (Map.Entry<String, Double> entry: hamMap.entrySet()){
            String key = entry.getKey();
            temp.put(key, 0.0);
        }

        /**
         * Now that we have every word from ham and spam we can
         * use calculateAndStorePrWords in our for loop
         */

        for (String word: temp.keySet()){
            calculateAndStorePrWords(word, temp);
        }

        return temp;

    }


    //Method to calculate and store Pr(S|Wi) for each word
    public void calculateAndStorePrWords(String word, Map<String, Double> temp){
        // this method PUTS the word and the frequency into the
        // probFileISpam

        /**
         *  probWordAppearsInHam = new TreeMap<>();
         *  probWordAppearsInSpam = new TreeMap<>();
         */
        double prSpamWord = probWordAppearsInSpam.getOrDefault(word,0.0);
        double prHamWord = probWordAppearsInHam.getOrDefault(word, 0.0);

        if (prSpamWord + prHamWord == 0){
            temp.put(word, 0.0);
        }else {
            temp.put(word, prSpamWord/ (prSpamWord + prHamWord));
        }
    }


    //Calculate probability that WORD appears in give map

    /**
     * Function calculateProb >>> calculates the probability of each word being in either
     * GIVEN  spam or ham map pass through as trainMap
     * with totalCount of files, Keep in mind Train folder has 2 folders for ham
     * @param trainMap
     * @param totalCount
     * @return
     */
    public Map<String, Double> calculateProb(Map<String, Integer> trainMap ,int totalCount){
        Map<String, Double> probabilityMap = new TreeMap<>();


        for(Map.Entry<String, Integer> entry: trainMap.entrySet()){
            String word = entry.getKey();
            int count = entry.getValue();

            double calcProbability = (double) count / totalCount;

            probabilityMap.put(word, calcProbability);
        }

        return probabilityMap;
    }


    /* loc = Location of which dir you would like to access
     FOR EXAMPLE: "test/ham" , can be inputted */
    public File getFileLocation(String loc){

        //Target the folder in which you want to tokenize each word
        URL url = this.getClass().getClassLoader().getResource("/data/"+loc);

        File emailDirectory = null;
        try{
            emailDirectory = new File(url.toURI());
        }
        catch (URISyntaxException e){
            throw new RuntimeException(e);
        }
        return emailDirectory;
    }


    //FOR EXAMPLE: "test/ham" , can be inputted
    public int totalFilesInFolder(String folderPath){

        File folder = getFileLocation(folderPath);
        File[] emailFiles = folder.listFiles();
        int count = emailFiles.length;
        return count;
    }



    /**
     * Slight difference in the function getTestWordFrequency
     * compared to getWordFrequency
         public Map<String, Integer> testMapFrequency(String loc){
        File emailDirectory = getFileLocation(loc);
        EmailParser emailParser = new EmailParser();
         return emailParser;

    }
     **/


    /**
     * Func Final Result will prepare our data
     * to this specific form.
     * [{"spamProbRounded":"0.00000","file":"00006.654c4","spamProbability":5.901957E-62,"actualClass":"Ham"}
     **/
     public List<Map<String, Object>> finalResult(String actualClass) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try {
            File emailDirectory = getFileLocation("test/ham");
            File[] emailFiles = emailDirectory.listFiles();

            if (emailFiles != null) {
                for (File email : emailFiles) {
                    Map<String, Double> temp = calculateEmailProb(email);

                    for (Map.Entry<String, Double> entry : temp.entrySet()) {
                        String file = entry.getKey();
                        Double spamProb = entry.getValue();

                        Map<String, Object> mapToBeInserted = new TreeMap<>();
                        mapToBeInserted.put("spamProbRounded", String.format("%.5f", spamProb));
                        mapToBeInserted.put("file", file);
                        mapToBeInserted.put("spamProbability", spamProb);
                        mapToBeInserted.put("actualClass", actualClass);

                        resultList.add(mapToBeInserted);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }



    /**
     * Here Func frequMap, we are using the Func getWordFrequency to Tokenize the data
     * returning a Map of word and count of word
     *
     * @param loc
     * @return emailParser.getWordFrequency(emailDirectory)
     */
    public Map<String, Integer> frequMap(String loc){

        File emailDirectory = getFileLocation(loc);
        EmailParser emailParser = new EmailParser();

        return  emailParser.getWordFrequency(emailDirectory);
    }


    // Func addMaps takes 2 maps and adds them together into one map
    public Map<String, Integer > addMaps(Map<String, Integer> map1,
                                         Map<String, Integer> map2){
        Map<String, Integer> resultMap = new TreeMap<>();

        for (Map.Entry<String, Integer> entry: map1.entrySet()){
            String key = entry.getKey();
            int value = entry.getValue();
            resultMap.put(key,resultMap.getOrDefault(key, 0) + value);
        }
        for (Map.Entry<String, Integer> entry: map2.entrySet()){
            String key = entry.getKey();
            int value = entry.getValue();
            resultMap.put(key,resultMap.getOrDefault(key, 0) + value);
        }
        return resultMap;

    }


    /**
     * Test ERROR found calculations are out of range
     * here we'll create responses to see our values going into
     * our calculations
     *
     * Update : Error found And fixed responses have been deleted
     */


    /**
     * Function calculateProb >>> calculates the probability of each word being in either
     *      * GIVEN  spam or ham map pass through as trainMap
     *      * with totalCount of files, Keep in mind Train folder has 2 folders for ham
     * @return
     */

    /**
     *         probFileIsSpam
     *         probWordAppearsInHam
     *         probWordAppearsInSpam
     *
     */

    //I want this function to return the file Name alongside with its probability score
    private Map<String, Double> calculateEmailProb(File email){
        Double probabilityScore = 0.0;
        Double n = 0.0;

        EmailParser emailParser = new EmailParser();


        Map<String, Double> emailWordFrequencyMap = new TreeMap<>();
        try {
            Scanner emailScanner = new Scanner(email);

            while (emailScanner.hasNext()){
                String word = emailScanner.next().toLowerCase();

                if(emailParser.isWord(word)){
                    // Here we'll start checking if the word currently exist
                    //here can we start checking probability
                    //Pr(S | Wi ) probFileIsSpam
                    double probIsSpam = probFileIsSpam.getOrDefault(word, 0.0);

                    //And we'll add it to the probability score
                    if (probIsSpam !=0 && probIsSpam != 1)
                        n += Math.log((1 - probIsSpam) / probIsSpam);


                }


            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        probabilityScore = 1 /(1 + Math.pow(Math.E,n));

        //After we have looped through every word we got a probability-score
        //and passed it as the value and the key is the file name
        emailWordFrequencyMap.put(email.getName(), probabilityScore);
        return emailWordFrequencyMap;
    }

    //getTestWord
    public Map<String, Double> getTestWordFrequency(String folder){

        //Target the folder in which you want to tokenize each word
        File emailDirectory = getFileLocation(folder);
        /**
         * This map is going to contain the file's name alongside their probability score
         */
        Map<String, Double> emailFrequencyMap = new TreeMap<>();
        //Read all files, return as a list of objects
        File[] emailFiles = emailDirectory.listFiles();
        int emailsFilesCount = emailFiles.length;

        for (File email: emailFiles){

            Map<String, Double> result = calculateEmailProb(email);

            emailFrequencyMap.putAll(result);


        }
        return emailFrequencyMap;

    }


    //public  Response getSpamResults()
    public Response resultForGetSpam() {

            try {
                return Response.status(200)
                        .header("Access-Control-Allow-Origin", "http://localhost:63342")
                        .header("Content-Type", "application/json")
                        .entity(objectMapper.writeValueAsString(result))
                        .build();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        }




    /**
     * Here I want to create a function will get the accuracy
     *
     * Function countCorrectPredictedEmails -> will either retrieve test/ham or test/spam
     *  data then put it through a for-loop  AND if type is consider **HAM** we will check
     *  every file and the probability if THE  probability of (IsTheFileSpam) less than 0.5
     *  we will increment the totalCorrectCount;
     *  If type is **SPAM** then we'll only increment the totalCorrectCount if the
     *  probability is ABOVE 0.5. ---> RETURNS totalCorrectCount
     *
     *
     * Function getTotalFilesInTestFolder -> uses the function totalFilesInFolder
     *  to get the int value of total files in each folder provided, for every
     *  folder in data/test. ---> RETURNS sum value
     *
     *
     */
    public int getTotalFilesInTestFolder(){
        int ham = totalFilesInFolder("test/ham");
        int spam = totalFilesInFolder("test/spam");
        return (ham + spam);
    }

    public int countCorrectPredictedEmails(String type){
        int totalCorrectCount = 0;

        if (type.toLowerCase().compareTo("ham") == 0){
            Map<String, Double> temp = getTestWordFrequency("test/ham");
            for (Map.Entry<String, Double> entry: temp.entrySet()){
                Double value = entry.getValue();
                if (value < 0.5)
                    totalCorrectCount += 1;
            }
            return totalCorrectCount;
        }
        Map<String, Double> temp = getTestWordFrequency("test/spam");
        //If String is not inputted as ham we'll take it as it's spam
        for (Map.Entry<String, Double> entry: temp.entrySet()){
            Double value = entry.getValue();
            if (value > 0.5)
                totalCorrectCount += 1;
        }

        return totalCorrectCount;
    }

    public Double accuracyResult(){
        int correctPredictedHamEmails = countCorrectPredictedEmails("ham");
        int correctPredictedSpamEmails = countCorrectPredictedEmails("spam");
        int totalFileCount = getTotalFilesInTestFolder();

        return (double)(correctPredictedHamEmails + correctPredictedHamEmails)/ totalFileCount;
    }

    public Response resultForAccuracy() {
        Double accuracy = accuracyResult();

        try {
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "http://localhost:63342")
                    .header("Content-Type", "application/json")
                    .entity(objectMapper.writeValueAsString("Accuracy value :" + accuracy))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
    // Accuracy SECTION above



    /**
     * Below I want to calculate PRECISION =)
     *
     * Here we'll use Func countCorrectPredictedEmails to get numTruePositive,
     *  so We'll create a slight modified version of countCorrectPredictedEmails
     *  EXPECT  now we'll change LESS THAN to GREATER THAN  ____OR____
     *  WE  can get the total amount of files in **HAM***, then
     *  we can SUBTRACT int value returned by countCorrectPredictedEmails.
     * SO
     *      we'll RETURN the Remainder
     *
     */
    public Double precisionResult(){
        int correctPredictedHamEmails = countCorrectPredictedEmails("ham");
        int totalHamFiles = totalFilesInFolder("test/ham");
        int incorrectPredictedHamEmails = totalHamFiles - correctPredictedHamEmails;

        return  (double)correctPredictedHamEmails /
                 (incorrectPredictedHamEmails + correctPredictedHamEmails);


    }

    public Response resultForPrecision() {
        Double precision = precisionResult();

        try {
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "http://localhost:63342")
                    .header("Content-Type", "application/json")
                    .entity(objectMapper.writeValueAsString("Precision value :" +precision))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }


}