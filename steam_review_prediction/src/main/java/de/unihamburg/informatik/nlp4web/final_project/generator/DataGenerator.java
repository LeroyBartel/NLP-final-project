package de.unihamburg.informatik.nlp4web.final_project.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import de.unihamburg.informatik.nlp4web.final_project.reader.TitleStringReader;
import de.unihamburg.informatik.nlp4web.final_project.writer.DictionaryWriter;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

public class DataGenerator {
	
	Map<Character, List<String>> titles = new HashMap<Character, List<String>>();
	Map<Character, List<String>> train = new HashMap<Character, List<String>>();
	Map<Character, List<String>> test = new HashMap<Character, List<String>>();
	Map<Character, List<String>> discarded = new HashMap<Character, List<String>>();

	public static final String LF = System.getProperty("line.separator");
	public static final String fileLocation = "src/main/resources/news/uci-news-aggregator";
	public static final int maxListLength = 10000;
	    
	public static void main(String[] args) throws Exception {
        File news = new File(fileLocation+".csv");
        DataGenerator data = new DataGenerator();
        
        data.PrepareCategoryDatasets(news);
        data.LimitDatasetlength(maxListLength);
        data.SplitTestAndTrainData();
        data.CreateTrainingAndTestDataset();
        data.CreateBinaryClassificationDatasets();
        data.GenerateDictionaries();
	}
	
	public DataGenerator() {
		titles.put('b', new ArrayList<String>());
		titles.put('t', new ArrayList<String>());
		titles.put('e', new ArrayList<String>());
		titles.put('m', new ArrayList<String>());
		
		train.put('b', new ArrayList<String>());
		train.put('t', new ArrayList<String>());
		train.put('e', new ArrayList<String>());
		train.put('m', new ArrayList<String>());
		
		test.put('b', new ArrayList<String>());
		test.put('t', new ArrayList<String>());
		test.put('e', new ArrayList<String>());
		test.put('m', new ArrayList<String>());
		
		discarded.put('b', new ArrayList<String>());
		discarded.put('t', new ArrayList<String>());
		discarded.put('e', new ArrayList<String>());
		discarded.put('m', new ArrayList<String>());
	}

	
	public void CreateTrainingAndTestDataset() throws IOException {
		List<String> training = new ArrayList<String>();
		List<String> testing = new ArrayList<String>();
		
		for (char key : titles.keySet()) {
			training.addAll(train.get(key));
			testing.addAll(test.get(key));
		}
		
		Collections.shuffle(training);
		Collections.shuffle(testing);
		
		StringBuilder trainingData = new StringBuilder();
		StringBuilder testData = new StringBuilder();

		for (String line : training) {
			trainingData.append(line).append(LF);			
		}
		
		for (String line : testing) {
			testData.append(line).append(LF);			
		}
		
		FileWriter trainWriter = new FileWriter(fileLocation+".csv.train");
		trainWriter.write(trainingData.toString());
		trainWriter.close();
		
		FileWriter testWriter = new FileWriter(fileLocation+".csv.test");
		testWriter.write(testData.toString());
		testWriter.close();

	}
	
	public void LimitDatasetlength(int max) throws Exception {
		for (char key : titles.keySet()) {
			int length = titles.get(key).size();
			if(length < max) {
				throw new Exception("Dataset" + key + "too small");
			}
			
			discarded.get(key).addAll(titles.get(key).subList(max, titles.get(key).size()));
			titles.get(key).retainAll(titles.get(key).subList(0, max));
		}
	}
	
	public void SplitTestAndTrainData() {
		for (char key : titles.keySet()) {
			int firstTrain = 0;
			int lastTest = titles.get(key).size() - 1;
			int lastTrain = (int) (((double) lastTest) * 0.8);
			int firstTest = lastTrain + 1;
			
			train.get(key).addAll(titles.get(key).subList(firstTrain, lastTrain));
			test.get(key).addAll(titles.get(key).subList(firstTest, lastTest));
		}
	}
	
	public void CreateBinaryClassificationDatasets() throws IOException {
		Character[] cat = titles.keySet().toArray(new Character[titles.keySet().size()]);
		for (int i = 0; i < cat.length - 1; i++) {
			for (int j = i + 1; j < cat.length; j++) {
				List<String> training = new ArrayList<String>();
				List<String> testing = new ArrayList<String>();
				
				training.addAll(train.get(cat[i]));
				testing.addAll(test.get(cat[i]));				
				training.addAll(train.get(cat[j]));
				testing.addAll(test.get(cat[j]));
				
				Collections.shuffle(training);
				Collections.shuffle(testing);
				
				StringBuilder trainingData = new StringBuilder();
				StringBuilder testData = new StringBuilder();

				for (String line : training) {
					trainingData.append(line).append(LF);			
				}
				
				for (String line : testing) {
					testData.append(line).append(LF);			
				}
				
				String fileName = fileLocation+"-"+cat[i]+"-"+cat[j]+".csv";
				
				FileWriter trainWriter = new FileWriter(fileName + ".train");
				trainWriter.write(trainingData.toString());
				trainWriter.close();
				
				FileWriter testWriter = new FileWriter(fileName + ".test");
				testWriter.write(testData.toString());
				testWriter.close();
			}
		}
	}
	
	public void GenerateDictionaries() throws ResourceInitializationException, UIMAException, IOException {
		for (char key : titles.keySet()) {
			List<String> lines = new ArrayList<String>();
			//lines.addAll(discarded.get(key));
			lines.addAll(train.get(key));
			
			StringBuilder StringLines = new StringBuilder();
			
			for (String line : lines) {
				StringLines.append(line).append(LF);
			}
			
			CollectionReader reader = createReader(TitleStringReader.class, TitleStringReader.INPUT_STRING, StringLines.toString());
			AnalysisEngine writer = createEngine(DictionaryWriter.class, DictionaryWriter.KEY, key);
	        AnalysisEngine seg = createEngine(StanfordSegmenter.class);
	        AnalysisEngine pos = createEngine(StanfordPosTagger.class);
	        AnalysisEngine lem = createEngine(StanfordLemmatizer.class);
	        runPipeline(reader, seg, pos, lem, writer);
		}
	}
	
	public void PrepareCategoryDatasets(File f) throws IOException {
		// partially inspired be the reader class
		String text;
		
		try {
			text = FileUtils.readFileToString(f, Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
        // a new sentence always starts with a new line
        if (text.charAt(0) != '\n') {
        	text = "\n" + text;
        }
        
        String[] tweets = text.split("(\r\n|\n)");

	    for (String line : tweets) {
	        String[] tag = line.split(",");
	        if (tag.length < 5) {
	            continue;
	        }

	        String title = tag[1];
	            
	        String newsCategory = tag[4];
	            
		    if (newsCategory.isEmpty()) {
		        continue;
		    }
		        
	        char category = newsCategory.charAt(0);

	        if (titles.containsKey(category)) {
		        titles.get(category).add(title+"\t"+category);
	        }
	    }
	    
	}
}