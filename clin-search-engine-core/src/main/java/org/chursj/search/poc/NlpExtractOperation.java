package org.chursj.search.poc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import rita.RiString;
import rita.RiTa;

public class NlpExtractOperation {

    /**
     * This method will take initial content and apply the openNlp on that content
     *
     * @param content at input
     * @return formated ready to index content.
     * <p>
     * // in this serial exec, I : // - Catch the content. // - Apply
     * openNlp ( - propositions, duplicate words, summarization)
     * @throws IOException
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<ExtractContentBean> applyNlp(String pageContent, String Filename, String[] excludeWords,
                                                    String[] posList, int extractLevel, String mode) throws IOException {

        // adding config for text analysis.
        final Map ritaConfig = new HashMap();
        ritaConfig.put("wordCount", 1);
        ritaConfig.put("ignoreCase", true);
        ritaConfig.put("ignoreStopWords", true);
        ritaConfig.put("ignorePunctuation", true);
        ritaConfig.put("wordsToIgnore", excludeWords);

        String[] tokenContent = RiTa.tokenize(pageContent);

        List<ExtractContentBean> words = new ArrayList();
        for (String word : tokenContent) {
            final ExtractContentBean contentBo = new ExtractContentBean();
            contentBo.setWord(word);
            RiString myRiString = new RiString(word);
            String pos = myRiString.pos()[0];
            contentBo.setPartOfSpeech(pos);
            contentBo.setFile(Filename);
            words.add(contentBo);
        }

        return words;

    }

}
