package de.unihamburg.informatik.nlp4web.final_project.feature;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.NamedFeatureExtractor1;

public class PunctuationCountExtractor<T extends Annotation> implements
NamedFeatureExtractor1<T> {

	private Class<? extends Annotation> annotationType;
    private String name;
    
    public PunctuationCountExtractor(Class<? extends Annotation> annotationType) {
        this.annotationType = annotationType;
        this.name = "Count_Punctuation_Marks";
    }
	
	@Override
	public List<Feature> extract(JCas view, T focusAnnotation) throws CleartkExtractorException {
		char[] review = focusAnnotation.getCoveredText().toCharArray();
		int p_count = 0;
		for (char c : review) {
			if (c == '.' || c == '?' || c == '!' || c == '$') {
				p_count++;
			}
		}
        return Arrays.asList(new Feature(this.name, p_count));
	}

	@Override
	public String getFeatureName() {
		return this.name;
	}

}
