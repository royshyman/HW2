package il.ac.tau.cs.software1.bigram;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

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
        visualizeSimilarities("resources/bigram/emma.txt");
    }

    /*
     * Optional "Try it" visualization (not graded): builds a model for one book and opens a window
     * with (1) the top-5 most similar words for a few query words, and (2) a heatmap of pairwise
     * cosine similarity among the query words and their closest words.
     * Everything is drawn onto a single BufferedImage (no custom classes); does nothing when headless.
     */
    private static void visualizeSimilarities(String bookPath) throws IOException {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Headless environment: skipping the similarity visualization.");
            return;
        }
        String[] queries = {"he", "she", "good", "great", "man", "day"};
        final int topK = 5;
        final int maxN = 12;

        BigramModel model = new BigramModel();
        model.initModel(bookPath);
        int vocSize = model.mVocabulary.length;

        // Top-5 most similar words per query, kept with insertion into fixed-size arrays (descending)
        int[][] topIdx = new int[queries.length][topK];
        double[][] topSim = new double[queries.length][topK];
        for (int q = 0; q < queries.length; q++) {
            for (int k = 0; k < topK; k++) {
                topIdx[q][k] = BigramModel.ELEMENT_NOT_FOUND;
                topSim[q][k] = -1;
            }
            int qi = model.getWordIndex(queries[q]);
            if (qi == BigramModel.ELEMENT_NOT_FOUND) {
                continue;
            }
            for (int i = 0; i < vocSize; i++) {
                if (i == qi) {
                    continue;
                }
                double sim = BigramModel.calcCosineSim(model.mBigramCounts[qi], model.mBigramCounts[i]);
                // sim == -1 means an all-zero row; strict comparison keeps the lower index on ties
                if (sim < 0 || sim <= topSim[q][topK - 1]) {
                    continue;
                }
                int pos = topK - 1;
                while (pos > 0 && topSim[q][pos - 1] < sim) {
                    topSim[q][pos] = topSim[q][pos - 1];
                    topIdx[q][pos] = topIdx[q][pos - 1];
                    pos--;
                }
                topSim[q][pos] = sim;
                topIdx[q][pos] = i;
            }
        }

        // Heatmap words: the query words first, then their closest words by rank, without duplicates
        int[] heatIdx = new int[maxN];
        int n = 0;
        for (int rank = -1; rank < topK && n < maxN; rank++) {
            for (int q = 0; q < queries.length && n < maxN; q++) {
                int cand = rank < 0 ? model.getWordIndex(queries[q]) : topIdx[q][rank];
                if (cand == BigramModel.ELEMENT_NOT_FOUND) {
                    continue;
                }
                boolean seen = false;
                for (int j = 0; j < n; j++) {
                    if (heatIdx[j] == cand) {
                        seen = true;
                    }
                }
                if (!seen) {
                    heatIdx[n++] = cand;
                }
            }
        }
        double[][] heat = new double[n][n];
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                double sim = BigramModel.calcCosineSim(model.mBigramCounts[heatIdx[r]], model.mBigramCounts[heatIdx[c]]);
                heat[r][c] = Math.max(0, Math.min(1, sim));
            }
        }

        // Layout
        final int barPanelX = 20, panelTop = 70, blockW = 290, blockH = 150;
        final int labelW = 80, barMaxW = 140, barH = 16, barGap = 22;
        final int heatX = 650, heatLabelW = 80, cell = 36, heatTop = panelTop + 70;
        int width = heatX + heatLabelW + maxN * cell + 30;
        int height = Math.max(panelTop + 3 * blockH, heatTop + maxN * cell + 70) + 20;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        Font titleFont = new Font(Font.SANS_SERIF, Font.BOLD, 16);
        Font boldFont = new Font(Font.SANS_SERIF, Font.BOLD, 13);
        Font plainFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        Font smallFont = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
        Color textColor = new Color(40, 40, 40);
        Color barColor = new Color(66, 120, 190);

        g.setColor(textColor);
        g.setFont(titleFont);
        g.drawString("Closest words (cosine similarity) - " + bookPath, barPanelX, 30);
        g.drawString("Pairwise similarity", heatX, 30);
        g.setFont(smallFont);
        g.drawString(vocSize + " words in the vocabulary; top " + topK + " per query word", barPanelX, 50);

        // Panel 1: horizontal bar charts, 2 columns x 3 rows
        for (int q = 0; q < queries.length; q++) {
            int x0 = barPanelX + (q % 2) * blockW;
            int y0 = panelTop + (q / 2) * blockH;
            g.setColor(textColor);
            g.setFont(boldFont);
            g.drawString("'" + queries[q] + "'", x0, y0 + 14);
            if (topIdx[q][0] == BigramModel.ELEMENT_NOT_FOUND) {
                g.setFont(plainFont);
                g.drawString("(not in vocabulary / no data)", x0, y0 + 36);
                continue;
            }
            for (int k = 0; k < topK && topIdx[q][k] != BigramModel.ELEMENT_NOT_FOUND; k++) {
                int y = y0 + 24 + k * barGap;
                String word = model.mVocabulary[topIdx[q][k]];
                g.setFont(plainFont);
                FontMetrics fm = g.getFontMetrics();
                g.setColor(textColor);
                g.drawString(word, x0 + labelW - 6 - fm.stringWidth(word), y + barH - 4);
                g.setColor(new Color(235, 238, 243));
                g.fillRect(x0 + labelW, y, barMaxW, barH);
                int w = (int) Math.round(topSim[q][k] * barMaxW);
                g.setColor(barColor);
                g.fillRect(x0 + labelW, y, w, barH);
                g.setColor(textColor);
                g.setFont(smallFont);
                g.drawString(String.format("%.2f", topSim[q][k]), x0 + labelW + barMaxW + 6, y + barH - 4);
            }
        }

        // Panel 2: heatmap, light (0) to dark (1)
        int[] light = {247, 251, 255};
        int[] dark = {8, 48, 107};
        g.setFont(plainFont);
        FontMetrics fm = g.getFontMetrics();
        int gridX = heatX + heatLabelW;
        for (int r = 0; r < n; r++) {
            String word = model.mVocabulary[heatIdx[r]];
            g.setColor(textColor);
            g.drawString(word, gridX - 6 - fm.stringWidth(word), heatTop + r * cell + cell / 2 + 5);
            // Column label, rotated
            int lx = gridX + r * cell + cell / 2 + 5;
            int ly = heatTop - 6;
            g.rotate(-Math.PI / 2, lx, ly);
            g.drawString(word, lx, ly);
            g.rotate(Math.PI / 2, lx, ly);
            for (int c = 0; c < n; c++) {
                double v = heat[r][c];
                g.setColor(new Color(
                        (int) Math.round(light[0] + v * (dark[0] - light[0])),
                        (int) Math.round(light[1] + v * (dark[1] - light[1])),
                        (int) Math.round(light[2] + v * (dark[2] - light[2]))));
                g.fillRect(gridX + c * cell, heatTop + r * cell, cell, cell);
                g.setColor(Color.WHITE);
                g.drawRect(gridX + c * cell, heatTop + r * cell, cell, cell);
                g.setFont(smallFont);
                g.setColor(v > 0.5 ? Color.WHITE : textColor);
                String val = String.format("%.2f", v);
                g.drawString(val, gridX + c * cell + (cell - g.getFontMetrics().stringWidth(val)) / 2,
                        heatTop + r * cell + cell / 2 + 4);
                g.setFont(plainFont);
            }
        }

        // Color scale legend under the heatmap
        int legendX = gridX;
        int legendY = heatTop + n * cell + 20;
        int legendW = 200;
        for (int i = 0; i < legendW; i++) {
            double v = (double) i / (legendW - 1);
            g.setColor(new Color(
                    (int) Math.round(light[0] + v * (dark[0] - light[0])),
                    (int) Math.round(light[1] + v * (dark[1] - light[1])),
                    (int) Math.round(light[2] + v * (dark[2] - light[2]))));
            g.fillRect(legendX + i, legendY, 1, 12);
        }
        g.setColor(textColor);
        g.drawRect(legendX, legendY, legendW, 12);
        g.setFont(smallFont);
        g.drawString("0", legendX, legendY + 26);
        g.drawString("1", legendX + legendW - 6, legendY + 26);
        g.drawString("cosine similarity", legendX + legendW + 10, legendY + 11);
        g.dispose();

        JFrame frame = new JFrame("Bigram word similarity - " + bookPath);
        frame.add(new JScrollPane(new JLabel(new ImageIcon(image))));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
