package de.unihamburg.informatik.nlp4web.final_project.reader;

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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonReviewReader extends JCasCollectionReader_ImplBase {
	
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
	
	
	public static final String LF = System.getProperty("line.separator");
	private List<File> documents;
	private int i = 0;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		documents = new ArrayList<File>(FileUtils.listFiles(dir, new String[] {"json"}, false));
	}
	
	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return i < documents.size();
	}

	@Override
	public Progress[] getProgress() {
		return new Progress[] {new ProgressImpl(i, documents.size(), Progress.ENTITIES)};
	}

	@Override
	public void getNext(JCas jCas) throws IOException, CollectionException {
		
		StringBuilder sb = new StringBuilder();
		File f = documents.get(i);
		
		JsonElement root = new JsonParser().parse(new FileReader(f));
		JsonArray reviews = root.getAsJsonObject().get("reviews").getAsJsonArray();
		for (JsonElement entry : reviews) {
			String review = entry.getAsJsonObject().get("review").getAsString();
			String voted_up = Integer.toString(entry.getAsJsonObject().get("voted_up").getAsBoolean() ? 1 : 0);
			
			review = review.trim();
			review = review.toLowerCase();
			review = review.replaceAll("[\\n\\t]+", " ");
			
			sb.append(review);
			sb.append(" ");
			sb.append(voted_up);
			sb.append(LF);
		}
		
		jCas.setDocumentLanguage(language);
		jCas.setDocumentText(sb.toString());
		i++;
	}

}
