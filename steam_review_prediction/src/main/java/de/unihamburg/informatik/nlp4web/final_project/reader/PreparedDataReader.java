package de.unihamburg.informatik.nlp4web.final_project.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

public class PreparedDataReader extends JCasCollectionReader_ImplBase {
	
	public static final String PARAM_DIRECTORY = "Directory";
	@ConfigurationParameter(name = PARAM_DIRECTORY,
			description = "Directory that contains the json review files",
			mandatory = true)
	private File dir;

	public static final String PARAM_LANGUAGE = "LanguageCode";
	@ConfigurationParameter(name = PARAM_LANGUAGE,
			description = "Language code of the reviews within the json files",
			mandatory = true)
	private String language;
	
//	public static final String PARAM_REVIEW_COUNT = "Total number of reviews";
//	@ConfigurationParameter(name = PARAM_REVIEW_COUNT,
//			description = "Total number of reviews to be read. Exactly 50% are positive and 50% are negative reviews.",
//			mandatory = true)
//	private int REVIEW_COUNT;
	
	
	public static final String LF = System.getProperty("line.separator");
	private List<File> documents;
	private int doc_idx = 0;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		documents = new ArrayList<File>(FileUtils.listFiles(dir, new String[] {"txt"}, false));
	}
	
	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return doc_idx < documents.size();
	}

	@Override
	public Progress[] getProgress() {
		return new Progress[] {new ProgressImpl(doc_idx, documents.size(), Progress.ENTITIES)};
	}

	@Override
	public void getNext(JCas jCas) throws IOException, CollectionException {
		File f = documents.get(doc_idx);
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(f));
		String review;
		while ((review = br.readLine()) != null) {
			sb.append(review);
			sb.append(LF);
		}
		br.close();
		
		jCas.setDocumentLanguage(language);
		jCas.setDocumentText(sb.toString());
		doc_idx++;
	}
}
