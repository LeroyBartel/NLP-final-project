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
	
	int REVIEW_COUNT = 10000;
	StringBuilder sb;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		documents = new ArrayList<File>(FileUtils.listFiles(dir, new String[] {"json"}, false));
		this.sb = new StringBuilder();
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
		
		File f = documents.get(i);
		
		JsonElement root = new JsonParser().parse(new FileReader(f));
		JsonArray reviews = root.getAsJsonObject().get("reviews").getAsJsonArray();
				
		int total_positive = root.getAsJsonObject().get("total_positive").getAsInt();
		int total_negative =  root.getAsJsonObject().get("total_negative").getAsInt();
		
		int min_reviews = Math.min(total_positive, total_negative);
		
		
		if (REVIEW_COUNT < min_reviews * 2) {
			min_reviews = REVIEW_COUNT / 2;
			REVIEW_COUNT = 0;
		} else {
			REVIEW_COUNT -= min_reviews * 2;
		}
		
		
		total_positive = min_reviews;
		total_negative = min_reviews;
		
		for (JsonElement entry : reviews) {
			if (total_positive <= 0 && total_negative <= 0) {
				break;
			}
			
			String review = entry.getAsJsonObject().get("review").getAsString();
			boolean vote = entry.getAsJsonObject().get("voted_up").getAsBoolean();
			String voted_up = Integer.toString(vote ? 1 : 0);

			if (vote) {
				total_positive--;
				if(total_positive>=0) {
					review = review.trim();
					review = review.replaceAll("[\\n\\t]+", " ");
				
					sb.append(review);
					sb.append("\t");
					sb.append(voted_up);
					sb.append(LF);
				}
			} else {
				total_negative--;
				if(total_negative>=0) {
					review = review.trim();
					review = review.replaceAll("[\\n\\t]+", " ");
				
					sb.append(review);
					sb.append("\t");
					sb.append(voted_up);
					sb.append(LF);
				}
			}
		}
		
		jCas.setDocumentLanguage(language);
		jCas.setDocumentText(sb.toString());
		i++;
	}

}
