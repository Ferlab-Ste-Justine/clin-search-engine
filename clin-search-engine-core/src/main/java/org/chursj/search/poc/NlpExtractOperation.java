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
	 * 
	 *         // in this serial exec, I : // - Catch the content. // - Apply
	 *         openNlp ( - propositions, duplicate words, summarization)
	 * @throws IOException
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
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
		int len = tokenContent.length;
		List<ExtractContentBean> words = new ArrayList();
		for (int i = 0; i < len; ++i) {
			String word = tokenContent[i];
			final ExtractContentBean contentBo = new ExtractContentBean();
			contentBo.setWord(word);
			RiString myRiString = new RiString(tokenContent[i]);
			String pos = myRiString.pos()[0];
			contentBo.setPartOfSpeech(pos);
			contentBo.setFile(Filename);
			words.add(contentBo);
		}
		
		return words;
		/*Map<String, Integer> nlpTextMap = RiTa.concordance(pageContent, ritaConfig);
		if (mode.equals("word")) {
			try {
				Map<String, String> pageMap = new HashMap<String, String>();
				nlpTextMap.keySet().stream().forEach(nlpElmt -> {
					RiString myRiString = new RiString(nlpElmt);
					String pos = myRiString.pos()[0];
					int count = nlpTextMap.get(nlpElmt);
					String key = nlpElmt;
					String value = String.valueOf(count) + "_" + pos;
					boolean contains = Arrays.stream(posList).anyMatch(pos::equals);
					if ((contains == true) && (count >= extractLevel)) {
						pageMap.put(key, value);
					} else if ((posList[0].length() == 0) && (count >= extractLevel)) {
						pageMap.put(key, value);
					}
				});
				return pageMap;
			} catch (Exception e) {
				return null;
			}
		} else {
			try {
				// Map<String, Integer> nlpTextMap = RiTa.kwic(text, word)(pageContent,
				// ritaConfig);

				Map<String, String> pageMap = new HashMap<String, String>();
				nlpTextMap.keySet().stream().forEach(nlpElmt -> {
					RiString myRiString = new RiString(nlpElmt);
					String pos = myRiString.pos()[0];
					int count = nlpTextMap.get(nlpElmt);
					String key = nlpElmt;
					String[] value = RiTa.kwic(pageContent, key);
					int len = value.length;
					boolean contains = Arrays.stream(posList).anyMatch(pos::equals);
					if ((contains == true) && (count >= extractLevel)) {
						for (int x = 0; x < len; ++x) {
							pageMap.put(key, value[x]);
						}
					} else if ((posList[0].length() == 0) && (count >= extractLevel)) {
						for (int x = 0; x < len; ++x) {
							pageMap.put(key, value[x]);
						}
					}
				});
				return pageMap;
			} catch (Exception e) {
				return null;
			}
		}*/

	}

}
