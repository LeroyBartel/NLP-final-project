package de.unihamburg.informatik.nlp4web.final_project.writer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;

public class DictionaryWriter extends JCasConsumer_ImplBase {
	public static final String KEY = "Input String";
	@ConfigurationParameter(name = KEY,
			description = "String to be read as input",
			mandatory = true)
	private char key;
	
	public static final String LF = System.getProperty("line.separator");
	public static final String fileLocation = "src/main/resources/news/uci-news-aggregator";
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		SortedSet<String> dictionary = new TreeSet<String>();
		for (Lemma l : JCasUtil.select(aJCas, Lemma.class)) {
			dictionary.add(l.getCoveredText());
		}
		
		StringBuilder dictionaryData = new StringBuilder();
		for (String word : dictionary) {
			dictionaryData.append(word).append(LF);			
		}
		
		String fileName = fileLocation + "-dictionary-" + key + ".txt";
		
		FileWriter dictionaryWriter;
		try {
			dictionaryWriter = new FileWriter(fileName);
			dictionaryWriter.write(dictionaryData.toString());
			dictionaryWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}