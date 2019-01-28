package de.unihamburg.informatik.nlp4web.final_project.annotator;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unihamburg.informatik.nlp4web.final_project.type.ReviewAnnotation;

public class ReviewAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		JCas docView;
        String tbText;
        StringBuffer docText = new StringBuffer();
        try {
            docView = aJCas.getView(CAS.NAME_DEFAULT_SOFA);
            tbText = aJCas.getDocumentText();
            aJCas.setDocumentLanguage("en");
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }
        
        // a new review always starts with a new line
        if (tbText.charAt(0) != '\n') {
            tbText = "\n" + tbText;
        }

        String[] tweets = tbText.split("(\r\n|\n)");
        
        int idx = 0;
        Token token = null;
        ReviewAnnotation review = null;
        String reviewSentiment;
        Sentence sentence = null;

        for (String line : tweets) {
            if (line.trim().isEmpty()) {
                continue;
            }
            // new sentence if there's a new line
            String[] tag = line.split("\t");
            String tweet = tag[0];
            reviewSentiment = tag[1];
            
            docText.append(tweet);
            docText.append(" ");
            
            review = new ReviewAnnotation(docView, idx, idx + tweet.length());
            
            String[] sentences = tweet.split("[.||?||!]+\\s*");
            int current_idx = 0;
            int position_idx = 0;
            int sentenceStart = 0;
            int sentenceEnd = 0;
            
            for (String s : sentences){
            	do{
            		position_idx = tweet.indexOf(s, current_idx);
            	} while (position_idx < current_idx);
            	current_idx = position_idx + 1;
            	sentenceStart = idx + position_idx;
            	sentenceEnd = sentenceStart + s.length();
            	sentence = new Sentence(docView, sentenceStart, sentenceEnd);
            	sentence.addToIndexes();
            }
            
            String[] words = tweet.split("\\s+");
            int current = 0;
            int position = 0;
            int tokenStart = 0;
            int tokenEnd = 0;
            
            for (String word : words){
            	do{
            		position = tweet.indexOf(word, current);
            	} while (position < current);
            	current = position + 1;
            	tokenStart = idx + position;
            	tokenEnd = tokenStart + word.length();
            	token = new Token(docView, tokenStart, tokenEnd);
            	token.addToIndexes();
            }

            // skip over the three characters "\t0\n" for each review entry
            // the zero denotes a negative sentiment
            idx += 3;

            // increment actual index of text
            idx += tweet.length();
            review.setGoldValue(reviewSentiment);           
            // we need the original title here
            review.setTitle(tag[0]);
            review.addToIndexes();
        }
	}
}
