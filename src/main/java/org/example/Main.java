package org.example;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        TextHandler handler = new TextHandler("src/main/resources/texts.txt", 7);
        handler.makeShinglesVocabulary();
        handler.oneHotEncoding();
        handler.countMinHash();
        handler.createDistanceMatrix();
    }
}