package il.ac.tau.cs.software1.bigram;

import java.io.IOException;
import java.util.Arrays;

/*
 * A small tester you can run locally (it is NOT graded, and it is NOT what GitHub Actions runs).
 * It prints an error code for every check that fails, and "done!" at the end.
 * Run it from the root of the repository, so that the relative paths below are found.
 * Feel free to add checks of your own.
 */
public class BigramModelTester {
    public static final String ALL_YOU_NEED_FILENAME = "resources/bigram/all_you_need.txt";
    public static final String ALL_YOU_NEED_MODEL_DIR = "resources/bigram/all_you_need_model";

    // Whole books (all of them are in the public domain). Add your own!
    // Moby Dick is the only one with more than MAX_VOCABULARY_SIZE different words;
    // its model needs about 1 GB of memory, so remove it from the list if you get an OutOfMemoryError.
    public static final String[] BOOKS = {
        "resources/bigram/emma.txt",
        "resources/bigram/alice.txt",
        "resources/bigram/sense_and_sensibility.txt",
        "resources/bigram/the_man_who_was_thursday.txt",
        "resources/bigram/moby_dick.txt",
    };
    public static final String[] WORDS_TO_TRY = {"good", "he", "said", "man", "day"};

    public static void main(String[] args) throws IOException {
        BigramModel sG = new BigramModel();
        String[] voc = sG.buildVocabularyIndex(ALL_YOU_NEED_FILENAME);
        
        


        if (voc.length != 5) {
            System.out.println("Error 1.1");
        }
        if (!Arrays.equals(voc, new String[]{"love", "all", "you", "need", "is"})) {
            System.out.println("Error 1.2");
        }

        int[][] counts = sG.buildCountsArray(ALL_YOU_NEED_FILENAME, voc);
        if (counts[0][0] != 3) { // count of "love love"
            System.out.println("Error 2.1");
        }
        if (counts[1][2] != 3) { // count of "all you"
            System.out.println("Error 2.2");
        }

        sG.initModel(ALL_YOU_NEED_FILENAME);
        sG.saveModel(ALL_YOU_NEED_MODEL_DIR);
        sG.loadModel(ALL_YOU_NEED_MODEL_DIR);

        if (!Arrays.equals(sG.mVocabulary, new String[]{"love", "all", "you", "need", "is"})) {
            System.out.println("Error 4.1");
        }
        if (sG.mBigramCounts[0][0] != 3) { // count of "love love"
            System.out.println("Error 4.2");
        }
        if (sG.mBigramCounts[1][2] != 3) { // count of "all you"
            System.out.println("Error 4.3");
        }

        if (!sG.isLegalSentence("love is all")) {
            System.out.println("Error 5.1");
        }
        if (sG.isLegalSentence("love is is")) {
            System.out.println("Error 5.2");
        }
        if (sG.isLegalSentence("love the beatles")) {
            System.out.println("Error 5.3");
        }

        // The methods below are already implemented, but they rely on your model
        if (sG.getWordIndex("love") != 0) {
            System.out.println("Error 6.1");
        }
        if (sG.getBigramCount("is", "love") != 2) {
            System.out.println("Error 6.2");
        }
        if (sG.getBigramCount("strawberry", "fields") != 0) {
            System.out.println("Error 6.3");
        }
        if (!"love".equals(sG.getMostFrequentProceeding("is"))) {
            System.out.println("Error 6.4");
        }

        // Whole books: this may take a while.
        // For example, in Emma the closest word to "good" should be "great".
        for (String book : BOOKS) {
            sG.initModel(book);
            System.out.println(book + ": " + sG.mVocabulary.length + " words in the vocabulary");
            for (String word : WORDS_TO_TRY) {
                if (sG.getWordIndex(word) != BigramModel.ELEMENT_NOT_FOUND) {
                    System.out.println("    closest word to '" + word + "': " + sG.getClosestWord(word));
                }
            }
        }

        System.out.println("done!");
    }
}
