package de.unihamburg.informatik.nlp4web.final_project.reader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

public class ReviewCollector extends JCasConsumer_ImplBase{

	public static final String PARAM_OUTPUT_DIR = "outputDirectory";
	@ConfigurationParameter(name = PARAM_OUTPUT_DIR, mandatory = true)
    private String outputDir;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		File out = new File(outputDir);
		if (out.exists()) {
			out.delete();
		}
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputDir, true));
			writer.write(aJCas.getDocumentText());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
