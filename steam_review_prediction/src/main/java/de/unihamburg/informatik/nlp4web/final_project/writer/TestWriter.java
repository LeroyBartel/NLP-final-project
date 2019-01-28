package de.unihamburg.informatik.nlp4web.final_project.writer;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Level;

public class TestWriter extends JCasConsumer_ImplBase{

	public static final String LF = System.getProperty("line.separator");
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(LF);
		sb.append(aJCas.getDocumentText());
		sb.append(LF);
		
		getContext().getLogger().log(Level.INFO, sb.toString());
	}

}
