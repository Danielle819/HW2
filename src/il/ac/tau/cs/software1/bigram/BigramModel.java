package il.ac.tau.cs.software1.bigram;

import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
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
        File vocFile = new File(fileName);
        BufferedReader bufRead = new BufferedReader(new FileReader(vocFile));
        String vocab = bufRead.readAllAsString();
        bufRead.close();
        if(vocab == null)
            return null;
        String[] vocabArr = vocab.trim().split("\\s+");
        int n = 0;
        String normStr; boolean found;
        for (String str : vocabArr){
            normStr = normalizeWord(str);
            found = false;
            for(int i = 0; i < n; ++i){
                if(vocabArr[i].equals(normStr)){
                    found = true;
                    break;
                }
            }
            if(!found && normStr != null){
                vocabArr[n] = normStr;
                n++;
                if(n > MAX_VOCABULARY_SIZE)
                    break;
            }
        }
        String[] finalVocArr = new String[n];
        for (int i = 0; i < n; ++i){
            finalVocArr[i] = vocabArr[i];
        }
        return finalVocArr;
    }

    /*
     * @post: mVocabulary = prev(mVocabulary)
     * @post: mBigramCounts = prev(mBigramCounts)
     */
    public int[][] buildCountsArray(String fileName, String[] vocabulary) throws IOException { // Question 2
        File vocFile = new File(fileName);
        BufferedReader bufRead = new BufferedReader(new FileReader(vocFile));
        String line; String[] lineArr; int m; String prevToken; int prevIdx = ELEMENT_NOT_FOUND - 1; String token; int idx;
        int n = vocabulary.length;
        int[][] cntArr = new int[n][n];
        while((line = bufRead.readLine()) != null){
            lineArr = line.trim().split("\\s+");
            m = lineArr.length;
            prevToken = normalizeWord(lineArr[0]);
            prevIdx = findIndex(vocabulary, prevToken);
            for(int i = 1; i < m; ++i){
                token = normalizeWord(lineArr[i]);
                idx = findIndex(vocabulary, token);
                if(prevIdx != ELEMENT_NOT_FOUND && idx != ELEMENT_NOT_FOUND)
                    ++cntArr[prevIdx][idx];
                prevToken = token;
                prevIdx = idx;
            }
        }
        bufRead.close();
        if(prevIdx == (ELEMENT_NOT_FOUND - 1))
            return null;
        
        return cntArr;
    }

    /*
     * @pre: the method initModel was called (the language model is initialized)
     * @pre: fileName is a legal file path
     */
    public void saveModel(String fileName) throws IOException { // Question 3
        File vocFile = new File(fileName + VOC_FILE_SUFFIX);
        BufferedWriter vocBufWriter = new BufferedWriter(new FileWriter(vocFile));
        int n = this.mVocabulary.length ;
        String line = n + " words\r\n";
        vocBufWriter.write(line);
        for(int i = 0; i<n; ++i){
            vocBufWriter.write(i + ","+this.mVocabulary[i] + "\r\n");
        }
        vocBufWriter.close();

        int c;
        File cntFile = new File(fileName + COUNTS_FILE_SUFFIX);
        BufferedWriter cntBufWriter = new BufferedWriter(new FileWriter(cntFile));
        for(int i=0; i<n; ++i){
            for (int j=0; j<n; j++){
                if((c = this.mBigramCounts[i][j]) != 0)
                    cntBufWriter.write(i+","+j+":"+c+"\r\n");
            }
        }
        cntBufWriter.close();
    }

    /*
     * @pre: fileName is a legal file path
     */
    public void loadModel(String fileName) throws IOException { // Question 4
        File vocFile = new File(fileName + VOC_FILE_SUFFIX);
        BufferedReader vocBufRead = new BufferedReader(new FileReader(vocFile));
        String line = vocBufRead.readLine();
        int n = line.charAt(0) - '0';
        this.mVocabulary = new String[n];
        this.mBigramCounts = new int[n][n];
        
        String[] tup;
        while((line=vocBufRead.readLine()) != null){
            tup = line.split(",");
            this.mVocabulary[Integer.parseInt(tup[0])] = tup[1];
        }
        vocBufRead.close();
        
        File cntFile = new File(fileName + COUNTS_FILE_SUFFIX);
        BufferedReader cntBufRead = new BufferedReader(new FileReader(cntFile));
        int c; String[] idx; int i; int j;
        while((line=cntBufRead.readLine()) != null){
            tup = line.split(":");
            c = Integer.parseInt(tup[1]);
            idx = tup[0].split(",");
            i = Integer.parseInt(idx[0]); 
            j = Integer.parseInt(idx[1]);
            this.mBigramCounts[i][j] = c;
        }
        cntBufRead.close();
    }

    /*
     * @pre: sentence is in lowercase
     * @pre: the method initModel was called (the language model is initialized)
     * @pre: each two words in the sentence are separated with a single space
     * @post: if sentence is probable, according to the model, $ret = true, else, $ret = false
     */
    public boolean isLegalSentence(String sentence) { // Question 5
        if(sentence == "") return true;
        String[] words = sentence.split(" ");
        int n = words.length;
        if(n==1) {
            if(findIndex(this.mVocabulary, words[0])!= ELEMENT_NOT_FOUND) return true;
            return false;
        } 
        String prevToken = words[0]; int prevIdx = findIndex(this.mVocabulary, prevToken);
        if (prevIdx == ELEMENT_NOT_FOUND) return false;
        String token; int idx;
        for (int i=1; i< n; ++i){
            token = words[i];
            idx = findIndex(this.mVocabulary, token);
            if (idx == ELEMENT_NOT_FOUND) return false;
            if(this.mBigramCounts[prevIdx][idx] == 0) return false;
            prevToken = token;
            prevIdx = idx;
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
