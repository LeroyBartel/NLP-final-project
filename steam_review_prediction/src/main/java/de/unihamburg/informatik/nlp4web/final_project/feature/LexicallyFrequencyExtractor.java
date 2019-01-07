package de.unihamburg.informatik.nlp4web.final_project.feature;

import org.apache.commons.io.FileUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.NamedFeatureExtractor1;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LexicallyFrequencyExtractor <T extends Annotation> implements
            		NamedFeatureExtractor1<T> {
	
	public static final String fileLocation = "src/main/resources/news/uci-news-aggregator-dictionary-";
	private Class<? extends Annotation> annotationType;
    private String name;
    private char[] categories = {'b', 't', 'e', 'm'};
    private Map<Character, List<String>> lexica = new HashMap<Character, List<String>>();

    public LexicallyFrequencyExtractor(Class<? extends Annotation> annotationType) {
        this.annotationType = annotationType;
        this.name = "Count_lexicon_matches_" + this.annotationType.getName();
        
        // read lexica files and set lexica here
        for (char category : categories) {
        	String text;
        	try {
        		text = FileUtils.readFileToString(new File(fileLocation + category + ".txt"), Charset.defaultCharset());
        	} catch (IOException e) {
        		e.printStackTrace();
        		return;
        	}
        	
        	// a new sentence always starts with a new line
        	if (text.charAt(0) != '\n') {
        		text = "\n" + text;
        	}
        	lexica.put(category, Arrays.asList(text.split("(\r\n|\n)")));
        }
        
    }

    @Override
    public String getFeatureName() {
        return this.name;
    }

    @Override
    public List<Feature> extract(JCas view, Annotation focusAnnotation)
            throws CleartkExtractorException {
        List<Lemma> annotations = JCasUtil.selectCovered(Lemma.class, focusAnnotation);
        List<Feature> lex_features = new ArrayList<Feature>();
        
    	if(annotations.size()<1) {
    		System.out.println("hre");
    	}
    	// feature logic here, i.e. count how many words of the news title are contained in the respective dict A/B/C/D
    	for (char category : categories) {
    		// add features
    		int nmatches = countMatches(lexica.get(category), annotations);
    		lex_features.add(new Feature(this.name + "_lexicon_" + category, nmatches));
    	}
        return lex_features;
    }
    
    private int countMatches(List<String> lexicon, List<Lemma> coveredWords) {
    	int nmatches = 0;
    	for (Lemma word_lemma : coveredWords) {
        	String word = word_lemma.getCoveredText();
        	if (lexicon.contains(word)) {
        		nmatches++;
        	}
        }
    	return nmatches;
    }
}
