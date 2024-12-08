package org.example;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import info.debatty.java.lsh.MinHash;


public class TextHandler {
    private List<String> _texts = new ArrayList<String>(); // initial canonized texts
    private int _shingleSize; // k - size of one shingle
    private HashSet<String> _shingles = new HashSet<String>(); // vocabulary of shingles
    private List<List<Boolean>> _vectors = new ArrayList<List<Boolean>>(); // one-hot coded texts
    private MinHash _minhash;
    private List<int[]> _signatures = new ArrayList<int[]>();

    public TextHandler(String filename, int k) {
        this._shingleSize = k;
        try {
            File file = new File(filename);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                JSONObject object = new JSONObject(line);
                String text = (String) object.get("text");
                // remove all punctuation and digits
                this._texts.add(text.replaceAll("[\\pP\\d]", ""));
            }
            br.close();
            fr.close();
        } catch (IOException | NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<String> getTexts() {
        return this._texts;
    }

    public void makeShinglesVocabulary() {
        for (String text : this._texts) {
            for (int i = 0; i < (text.length() - this._shingleSize - 1); ++i) {
                this._shingles.add(text.substring(i, i + this._shingleSize));
            }
        }
        System.out.println("Creating vocabulary of k-shingles with k = " + this._shingleSize + " ended. Number of shingles = " + this._shingles.size());
    }

    private int getIndexOfShingle(String shingle) {
        int index = 0;
        for (String current : this._shingles) {
            if (shingle.equals(current)) {
                return index;
            }
            ++index;
        }
        return -1;
    }

    public void oneHotEncoding() {
        int counter = 0;
        for (String text : this._texts) {
            List<Boolean> vector = new ArrayList<Boolean>();
            for (int i = 0; i < this._shingles.size(); ++i) {
                vector.add(false);
            }
            for (int i = 0; i < (text.length() - this._shingleSize - 1); ++i) {
                String shingle = text.substring(i, i + this._shingleSize);
                int index = getIndexOfShingle(shingle);
                vector.set(index, true);
            }
            this._vectors.add(vector);
            System.out.println("Vector #" + counter + " created.");
            ++counter;
        }
    }

    public void countMinHash() {
        this._minhash = new MinHash(0.1, this._shingles.size());
        for (List<Boolean> vector : _vectors) {
            boolean[] vec = new boolean[vector.size()];
            for (int i = 0; i < vector.size(); ++i) {
                vec[i] = vector.get(i);
            }
            int[] sig = this._minhash.signature(vec);
            _signatures.add(sig);
            System.out.println(Arrays.toString(sig));
        }
    }

    public void createDistanceMatrix() {
        double[][] matrix = new double[this._signatures.size()][this._signatures.size()];
        for (int i = 0; i < this._signatures.size(); ++i) {
            for (int j = 0; j < this._signatures.size(); ++j) {
                if (i == j) {
                    matrix[i][j] = 0;
                }
                else {
                    matrix[i][j] = this._minhash.similarity(_signatures.get(i), _signatures.get(j));
                }
            }
        }
        System.out.println(Arrays.deepToString(matrix));
    }
}
