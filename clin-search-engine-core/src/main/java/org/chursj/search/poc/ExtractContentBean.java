package org.chursj.search.poc;

import lombok.Data;

@Data
public class ExtractContentBean {


    String word;
    String partOfSpeech;
    String file;
    String wordsBefore;
    String wordsAfter;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getWordsBefore() {
        return wordsBefore;
    }

    public void setWordsBefore(String wordsBefore) {
        this.wordsBefore = wordsBefore;
    }

    public String getWordsAfter() {
        return wordsAfter;
    }

    public void setWordsAfter(String wordsAfter) {
        this.wordsAfter = wordsAfter;
    }


}
