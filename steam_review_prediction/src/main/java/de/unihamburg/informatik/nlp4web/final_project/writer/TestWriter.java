package de.unihamburg.informatik.nlp4web.final_project.writer;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.util.Level;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TestWriter extends JCasConsumer_ImplBase{

	public static final String LF = System.getProperty("line.separator");
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		
		StringBuilder sb = new StringBuilder();
		
//		for (Annotation a : JCasUtil.select(aJCas, Token.class)) {
//			sb.append(a.getCoveredText());
//			sb.append(LF);
//		}
		
		sb.append(LF);
		sb.append(aJCas.getDocumentText());
		sb.append(LF);
		
		getContext().getLogger().log(Level.INFO, sb.toString());
	}

}
