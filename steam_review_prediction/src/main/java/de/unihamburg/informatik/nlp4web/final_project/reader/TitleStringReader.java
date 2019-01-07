package de.unihamburg.informatik.nlp4web.final_project.reader;

import java.io.IOException;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

public class TitleStringReader extends JCasCollectionReader_ImplBase {
	public static final String INPUT_STRING = "Input String";
	@ConfigurationParameter(name = INPUT_STRING,
			description = "String to be read as input",
			mandatory = true)
	private String input;
	
	boolean full = true;
	
	@Override
	public boolean hasNext() throws IOException, CollectionException {
		// TODO Auto-generated method stub
		return full;
	}

	@Override
	public Progress[] getProgress() {
		// TODO Auto-generated method stub
		int progress = full ? 0 : 1;
		return new Progress[] {new ProgressImpl(progress, 1, Progress.ENTITIES)};
	}

	@Override
	public void getNext(JCas jCas) throws IOException, CollectionException {
		// TODO Auto-generated method stub
		full = false;
		jCas.setDocumentLanguage("en");
		jCas.setDocumentText(input);
	}
	
}
