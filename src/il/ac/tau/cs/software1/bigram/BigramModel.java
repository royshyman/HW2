package il.ac.tau.cs.software1.bigram;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

public class BigramModel {
    public static final int MAX_VOCABULARY_SIZE = 14500;
    public static final String VOC_FILE_SUFFIX = ".voc";
    public static final String COUNTS_FILE_SUFFIX = ".counts";
    public static final String SOME_NUM = "some_num";
    public static final int ELEMENT_NOT_FOUND = -1;

    String[] mVocabulary;
    int[][] mBigramCounts;

    // DO NOT CHANGE THIS !!!
    public void initModel(String fileName) throws IOException {
        mVocabulary = buildVocabularyIndex(fileName);
        mBigramCounts = buildCountsArray(fileName, mVocabulary);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helper methods - already implemented for you. Use them!
    ///////////////////////////////////////////////////////////////////////////

    /*
     * Converts a single token (a whitespace-free piece of text from the file)
     * into the form in which it is stored in the vocabulary:
     *   - a token with at least one English letter  -> the token in lowercase
     *   - a token made only of digits (an integer)  -> SOME_NUM
     *   - anything else is not a legal word         -> null
     *
     * @pre: token != null
     */
    public static String normalizeWord(String token) {
        boolean hasLetter = false;
        boolean allDigits = !token.isEmpty();
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                hasLetter = true;
            }
            if (c < '0' || c > '9') {
                allDigits = false;
            }
        }
        if (hasLetter) {
            return token.toLowerCase(Locale.ROOT);
        }
        if (allDigits) {
            return SOME_NUM;
        }
        return null;
    }

    /*
     * @pre: vocabulary != null
     * @post: $ret = the index of word in vocabulary, or ELEMENT_NOT_FOUND if
     * word is null or does not appear in it
     */
    public static int findIndex(String[] vocabulary, String word) {
        if (word == null) {
            return ELEMENT_NOT_FOUND;
        }
        for (int i = 0; i < vocabulary.length; i++) {
            if (word.equals(vocabulary[i])) {
                return i;
            }
        }
        return ELEMENT_NOT_FOUND;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Questions 1 - 5: implement these
    ///////////////////////////////////////////////////////////////////////////

    /*
     * @post: mVocabulary = prev(mVocabulary)
     * @post: mBigramCounts = prev(mBigramCounts)
     */
    public String[] buildVocabularyIndex(String fileName) throws IOException { // Question 1
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line;
        int count = 0;
        String[] vocab = new String[MAX_VOCABULARY_SIZE];
        while (count < MAX_VOCABULARY_SIZE && (line = br.readLine()) != null){
            String[] tokens = line.trim().split("\\s+");
            for (int i=0;i<tokens.length;i++){
                if (count < MAX_VOCABULARY_SIZE){
                    String curr = tokens[i];
                    curr = normalizeWord(curr);
                    if (curr != null && findIndex(vocab, curr) == ELEMENT_NOT_FOUND){
                        vocab[count] = curr;
                        count++;
                    }
                        
                }
            }
        }
        br.close();
        return Arrays.copyOf(vocab, count);
    }

    /*
     * @post: mVocabulary = prev(mVocabulary)
     * @post: mBigramCounts = prev(mBigramCounts)
     */
    public int[][] buildCountsArray(String fileName, String[] vocabulary) throws IOException { // Question 2
        int n = vocabulary.length;
        int[][] counts = new int[n][n];
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line;
        String curr;
        String next;
        while ((line = br.readLine()) != null){
            String[] tokens = line.trim().split("\\s+");
            for (int i=0;i<tokens.length-1;i++){
                curr = normalizeWord(tokens[i]);
                next = normalizeWord(tokens[i+1]);
                int x = -1;
                int y =-1;
                for (int k=0;k<n;k++){
                    if (vocabulary[k].equals(curr))
                        x = k;
                    if (vocabulary[k].equals(next))
                        y = k;
                    if (x != -1 && y != -1)
                        break;
                }
                if (x != -1 && y != -1)
                    counts[x][y]++;
            }
        }
        br.close();
        return counts;
    }

    /*
     * @pre: the method initModel was called (the language model is initialized)
     * @pre: fileName is a legal file path
     */
    public void saveModel(String fileName) throws IOException { // Question 3
        File voc_file = new File(fileName + VOC_FILE_SUFFIX);
        File counts_file = new File(fileName + COUNTS_FILE_SUFFIX);

        BufferedWriter bw_voc = new BufferedWriter(new FileWriter(voc_file));
        BufferedWriter bw_counts = new BufferedWriter(new FileWriter(counts_file));
        int [][] counts_arr = this.mBigramCounts;
        String[] vocab = this.mVocabulary;

        bw_voc.write(""+vocab.length+" words"+"\n");                                     //first line of voc file - num of words

        for (int i=0;i<vocab.length;i++){
            bw_voc.write(""+i+","+vocab[i]+"\n");
        }
        for (int i=0;i<counts_arr.length;i++){
            for (int k=0;k<counts_arr[i].length;k++){
                if (counts_arr[i][k] != 0)
                    bw_counts.write(""+i+","+k+":"+counts_arr[i][k]+"\n");
            }
        }
        bw_counts.close();
        bw_voc.close();
    }

    /*
     * @pre: fileName is a legal file path
     */
    public void loadModel(String fileName) throws IOException { // Question 4
        BufferedReader br_voc = new BufferedReader(new FileReader(fileName+VOC_FILE_SUFFIX));
        BufferedReader br_counts = new BufferedReader(new FileReader(fileName+COUNTS_FILE_SUFFIX));
        String line = br_voc.readLine();
        String[] first_line = line.trim().split("\\s+");
        int voc_length = Integer.parseInt(first_line[0]);
        int count = 0;
        String[] new_vocab = new String[voc_length];
        while (count < voc_length && (line = br_voc.readLine()) != null){
            String[] tokens = line.split(",",2);
            new_vocab[count] = tokens[1];
            count++;
        }
        br_voc.close();
        int[][] new_counts =new int[new_vocab.length][new_vocab.length];
        int x;
        int y;
        while ((line = br_counts.readLine()) != null){
            String[] tokens = line.trim().split("[,:]");
            x = Integer.parseInt(tokens[0]);
            y = Integer.parseInt(tokens[1]);
            new_counts[x][y] = Integer.parseInt(tokens[2]);
        }
        br_counts.close();
        mBigramCounts = new_counts;
        mVocabulary = new_vocab;
    }

    /*
     * @pre: sentence is in lowercase
     * @pre: the method initModel was called (the language model is initialized)
     * @pre: each two words in the sentence are separated with a single space
     * @post: if sentence is probable, according to the model, $ret = true, else, $ret = false
     */
    public boolean isLegalSentence(String sentence) { // Question 5
        if (sentence == null || sentence.equals(""))
            return true;
        String[] tokens = sentence.trim().split("\\s+");
        if (tokens.length == 1){
            return (getWordIndex(tokens[0]) != ELEMENT_NOT_FOUND);
        }
        for (int i=0;i<tokens.length-1;i++){
            if (getBigramCount(tokens[i], tokens[i+1]) == 0)
                return false;
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Queries on the model - already implemented for you.
    // They start working once Questions 1 - 2 are done.
    ///////////////////////////////////////////////////////////////////////////

    /*
     * @pre: word is in lowercase
     * @pre: the method initModel was called (the language model is initialized)
     * @post: $ret = -1 if word is not in vocabulary, otherwise $ret = the index of word in vocabulary
     */
    public int getWordIndex(String word) {
        return findIndex(mVocabulary, word);
    }

    /*
     * @pre: word1, word2 are in lowercase
     * @pre: the method initModel was called (the language model is initialized)
     * @post: $ret = the count for the bigram <word1, word2>. if one of the words does not
     * exist in the vocabulary, $ret = 0
     */
    public int getBigramCount(String word1, String word2) {
        int index1 = getWordIndex(word1);
        int index2 = getWordIndex(word2);
        if (index1 == ELEMENT_NOT_FOUND || index2 == ELEMENT_NOT_FOUND) {
            return 0;
        }
        return mBigramCounts[index1][index2];
    }

    /*
     * @pre: word is in lowercase, and is in mVocabulary
     * @pre: the method initModel was called (the language model is initialized)
     * @post: $ret = the word with the lowest vocabulary index that appears most frequently after word
     * (if a bigram starting with word was never seen, $ret will be null)
     */
    public String getMostFrequentProceeding(String word) {
        int[] row = mBigramCounts[getWordIndex(word)];
        int best = ELEMENT_NOT_FOUND;
        for (int i = 0; i < row.length; i++) {
            if (row[i] > 0 && (best == ELEMENT_NOT_FOUND || row[i] > row[best])) {
                best = i;
            }
        }
        return best == ELEMENT_NOT_FOUND ? null : mVocabulary[best];
    }

    /*
     * @pre: arr1.length = arr2.length
     * @post: if arr1 or arr2 are only filled with zeros, $ret = -1, otherwise calculates CosineSim
     */
    public static double calcCosineSim(int[] arr1, int[] arr2) {
        double dot = 0;
        double norm1 = 0;
        double norm2 = 0;
        for (int i = 0; i < arr1.length; i++) {
            dot += (double) arr1[i] * arr2[i];
            norm1 += (double) arr1[i] * arr1[i];
            norm2 += (double) arr2[i] * arr2[i];
        }
        if (norm1 == 0 || norm2 == 0) {
            return -1;
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /*
     * @pre: word is in vocabulary
     * @pre: the method initModel was called (the language model is initialized)
     * @post: $ret = w implies that w is the word with the largest cosineSimilarity(vector for word, vector for w)
     * among all the other words in vocabulary (the lowest index wins a tie)
     */
    public String getClosestWord(String word) {
        int wordIndex = getWordIndex(word);
        int best = ELEMENT_NOT_FOUND;
        double bestSim = 0;
        for (int i = 0; i < mVocabulary.length; i++) {
            if (i == wordIndex) {
                continue;
            }
            double sim = calcCosineSim(mBigramCounts[wordIndex], mBigramCounts[i]);
            if (best == ELEMENT_NOT_FOUND || sim > bestSim) {
                best = i;
                bestSim = sim;
            }
        }
        // A vocabulary with a single word: the closest word is the word itself
        return best == ELEMENT_NOT_FOUND ? word : mVocabulary[best];
    }
}
