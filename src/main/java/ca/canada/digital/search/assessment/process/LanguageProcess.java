package ca.canada.digital.search.assessment.process;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.fr.FrenchLightStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.canada.digital.search.assessment.object.Language;

public class LanguageProcess {
	private static Logger LOG = LoggerFactory.getLogger(LanguageProcess.class);
	private static final String TAG_START = "<em>";
	private static final String TAG_END = "</em>";
	private Language lang;
	private Analyzer analyzer;
	private TokenStream ts;
	private String processedTerm;

	public LanguageProcess(String term, Language lang) {
		this.lang = lang;
		this.analyzer = new StandardAnalyzer();
		this.processedTerm = applyFilters(term, lang);
	}

	private String applyFilters(String term, Language lang) {
		ts = this.analyzer.tokenStream("term", new StringReader(term));
		ts = new LowerCaseFilter(ts);
		if (Language.FRENCH == lang) {
			ts = new FrenchLightStemFilter(ts);
		} else {
			ts = new PorterStemFilter(ts);
		}

		try {

			CharTermAttribute token = ts.getAttribute(CharTermAttribute.class);

			ts.reset();

			StringBuilder stringBuilder = new StringBuilder();

			while (ts.incrementToken()) {
				if (stringBuilder.length() > 0) {
					stringBuilder.append(" ");
				}

				stringBuilder.append(token.toString());
			}

			ts.end();
			ts.close();

			return stringBuilder.toString();

		} catch (IOException e) {
			LOG.error("Error while tokenizing the terms: ", e);
		}

		return null;
	}

	public Map<String, Object> getMatches(String text) {
		Map<String, Object> result = new HashMap<>();
		String[] processedTerms = processedTerm.split("\\s+");
		
		if (StringUtils.isEmpty(text)) {
			Map<String, Object> terms = new HashMap<>();
			for (String term : processedTerms) {
				terms.put(term, 0);
			}
			result.put("matches", terms);
			result.put("matchingScore", 0);
			return result;
		}

		String[] processedText = applyFilters(text, lang).split("\\s+");

		Map<String, Integer> matchesMap = new HashMap<>();
		int zeros = 0;

		for (String termToken : processedTerms) {
			int matches = 0;
			for (String textToken : processedText) {
				if (termToken.equalsIgnoreCase(textToken)) {
					matches++;
				}
			}
			if (matches == 0) {
				zeros = zeros + 1;
			}
			matchesMap.put(termToken, matches);
		}
		result.put("matches", matchesMap);

		int score = 2; // all marched
		if (zeros > 0) {
			if (zeros < matchesMap.size()) {
				score = 1; // some matched
			} else if (zeros == matchesMap.size()) {
				score = 0; // no matches
			}
		}
		result.put("matchingScore", score);

		return result;

	}

	public String getHighlights(String text) {
		if (StringUtils.isEmpty(text)) {
			return null;
		}

		String[] processedTerms = processedTerm.split("\\s+");
		String[] words = text.strip().toLowerCase().split("\\s+");

		for (String termToken : processedTerms) {
			for (int i = 0; i < words.length; i++) {
				String token = words[i].replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
				if (token.equalsIgnoreCase(termToken)) {
					words[i] = tag(words[i]);
				} else if (termToken.length() > 2 && token.startsWith(termToken)) {
					words[i] = tag(words[i]);
				}
			}
		}
		return String.join(" ", words);

	}

	private String tag(String text) {
		return String.format("%s%s%s", TAG_START, text.strip(), TAG_END);
	}

	public String getProcessedTerm() {
		return processedTerm;
	}

}
