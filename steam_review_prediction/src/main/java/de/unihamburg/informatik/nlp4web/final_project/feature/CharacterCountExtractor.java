package de.unihamburg.informatik.nlp4web.final_project.feature;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.NamedFeatureExtractor1;

public class CharacterCountExtractor<T extends Annotation> implements
NamedFeatureExtractor1<T> {

	private Class<? extends Annotation> annotationType;
    private String name;
    
    public CharacterCountExtractor(Class<? extends Annotation> annotationType) {
        this.annotationType = annotationType;
        this.name = "Count_Characters";
    }
	
	@Override
	public List<Feature> extract(JCas view, T focusAnnotation) throws CleartkExtractorException {
//		System.out.println(focusAnnotation.getCoveredText());
		int review_length = focusAnnotation.getCoveredText().length();
        return Arrays.asList(new Feature(this.name, review_length));
	}

	@Override
	public String getFeatureName() {
		return this.name;
	}

}
