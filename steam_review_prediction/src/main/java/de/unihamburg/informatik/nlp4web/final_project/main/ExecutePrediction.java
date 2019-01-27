package de.unihamburg.informatik.nlp4web.final_project.main;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.testing.util.HideOutput;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.cleartk.ml.feature.transform.InstanceDataWriter;
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.ml.jar.JarClassifierBuilder;

import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import de.unihamburg.informatik.nlp4web.final_project.annotator.ReviewClassificationAnnotator;
import de.unihamburg.informatik.nlp4web.final_project.annotator.ReviewAnnotator;
import de.unihamburg.informatik.nlp4web.final_project.reader.JsonReviewReader;
import de.unihamburg.informatik.nlp4web.final_project.writer.ReviewClassificationAnalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

public class ExecutePrediction {
	
	public static void writeModel(File saTrain, File modelDirectory)
			throws ResourceInitializationException, UIMAException, IOException {
		System.err.println("Step 1: Extracting features and writing raw instances data");
		runPipeline(
				CollectionReaderFactory.createReader(JsonReviewReader.class,
		    			JsonReviewReader.PARAM_DIRECTORY, saTrain.getAbsolutePath(),
		    			JsonReviewReader.PARAM_LANGUAGE, "en"),
				createEngine(ReviewAnnotator.class),
//				createEngine(StanfordPosTagger.class, StanfordPosTagger.PARAM_LANGUAGE, "en"),
				createEngine(AnalysisEngineFactory.createEngineDescription(
						ReviewClassificationAnnotator.class,
						ReviewClassificationAnnotator.PARAM_IS_TRAINING, true,
						ReviewClassificationAnnotator.PARAM_DIRECTORY_NAME, modelDirectory,
						DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME, InstanceDataWriter.class.getName(),
						DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY, modelDirectory)));
	}

	public static void trainModel(File modelDirectory, String[] arguments) throws Exception {
		/** Stage 3: Train and write model
		 * Now that the features have been extracted and normalized, we can proceed
		 *in running machine learning to train and package a model **/

		System.err.println("Train model and write model.jar file.");
		HideOutput hider = new HideOutput();
		JarClassifierBuilder.trainAndPackage(modelDirectory, arguments);
		hider.restoreOutput();
	}

	public static void classifyTestFile(File modelDirectory, File saTest, File result, File resultScores)
			throws ResourceInitializationException, UIMAException, IOException {
		runPipeline(
				CollectionReaderFactory.createReader(JsonReviewReader.class,
		    			JsonReviewReader.PARAM_DIRECTORY, saTest.getAbsolutePath(),
		    			JsonReviewReader.PARAM_LANGUAGE, "en"),
				createEngine(ReviewAnnotator.class),
//				createEngine(StanfordPosTagger.class, StanfordPosTagger.PARAM_LANGUAGE, "en"),
				createEngine(AnalysisEngineFactory.createEngineDescription(
						ReviewClassificationAnnotator.class,
						ReviewClassificationAnnotator.PARAM_IS_TRAINING, false,
						ReviewClassificationAnnotator.PARAM_DIRECTORY_NAME, modelDirectory,
						ReviewClassificationAnnotator.PARAM_TF_IDF_URI,
						ReviewClassificationAnnotator.createTokenTfIdfDataURI(modelDirectory),
						ReviewClassificationAnnotator.PARAM_TF_IDF_CENTROID_SIMILARITY_URI,
						ReviewClassificationAnnotator.createIdfCentroidSimilarityDataURI(modelDirectory),
						ReviewClassificationAnnotator.PARAM_MINMAX_URI,
						ReviewClassificationAnnotator.createMinMaxDataURI(modelDirectory),
						GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, modelDirectory + "/model.jar")
          ),
          createEngine(ReviewClassificationAnalyzer.class,
          		ReviewClassificationAnalyzer.PARAM_OUTPUT_DIR, result.getAbsolutePath(),
          		ReviewClassificationAnalyzer.PARAM_OUTPUT_SCORES, resultScores.getAbsolutePath()));
	}

	public static void execute(String train, String test, String name) throws Exception {
		String generalPath = "src/main/resources/";
		String[] trainingArguments = new String[]{"-t", "0", "-b", "1"};
		long start = System.currentTimeMillis();

		String modelPath = generalPath + "model" + name + "/";
		new File(modelPath).mkdirs();
		File modelDir = new File(modelPath);

		String resltPath = generalPath + "results" + name + "/";
		new File(resltPath).mkdirs();
		File resultDir = new File(resltPath+"review_sentiment_prediction.csv");
		File resultScores = new File(resltPath+"test-scores.txt");

		File reviewsTrain = new File(generalPath + train);
		File reviewsTest = new File(generalPath + test);

		writeModel(reviewsTrain, modelDir);
		trainModel(modelDir, trainingArguments);
		classifyTestFile(modelDir, reviewsTest, resultDir, resultScores);
		long now = System.currentTimeMillis();
		UIMAFramework.getLogger().log(Level.INFO, "Time: " + (now - start) + "ms");
	}
	
    public static void main(String[] args) throws UIMAException, IOException {
    	
    	try {
			execute("steam-reviews/", "evaluation-reviews/", "_review_sentiment_prediction");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
//    	execute("reviews/uci-reviews-aggregator.csv.train.small", "reviews/uci-reviews-aggregator.csv.test.small", "small", false);
//    	execute("reviews/uci-reviews-aggregator-b-e.csv.train", "reviews/uci-reviews-aggregator-b-e.csv.test", "-b-e", false);
//    	execute("reviews/uci-reviews-aggregator-b-m.csv.train", "reviews/uci-reviews-aggregator-b-m.csv.test", "-b-m", false);
//    	execute("reviews/uci-reviews-aggregator-b-t.csv.train", "reviews/uci-reviews-aggregator-b-t.csv.test", "-b-t", false);
//    	execute("reviews/uci-reviews-aggregator-e-m.csv.train", "reviews/uci-reviews-aggregator-e-m.csv.test", "-e-m", false);
//    	execute("reviews/uci-reviews-aggregator-t-e.csv.train", "reviews/uci-reviews-aggregator-t-e.csv.test", "-t-e", false);
//    	execute("reviews/uci-reviews-aggregator-t-m.csv.train", "reviews/uci-reviews-aggregator-t-m.csv.test", "-t-m", false);
//    	
//    	execute("reviews/uci-reviews-aggregator.csv.train.small", "reviews/uci-reviews-aggregator.csv.test.small", "small", true);
//    	execute("reviews/uci-reviews-aggregator-b-e.csv.train", "reviews/uci-reviews-aggregator-b-e.csv.test", "-b-e", true);
//    	execute("reviews/uci-reviews-aggregator-b-m.csv.train", "reviews/uci-reviews-aggregator-b-m.csv.test", "-b-m", true);
//    	execute("reviews/uci-reviews-aggregator-b-t.csv.train", "reviews/uci-reviews-aggregator-b-t.csv.test", "-b-t", true);
//    	execute("reviews/uci-reviews-aggregator-e-m.csv.train", "reviews/uci-reviews-aggregator-e-m.csv.test", "-e-m", true);
//    	execute("reviews/uci-reviews-aggregator-t-e.csv.train", "reviews/uci-reviews-aggregator-t-e.csv.test", "-t-e", true);
//    	execute("reviews/uci-reviews-aggregator-t-m.csv.train", "reviews/uci-reviews-aggregator-t-m.csv.test", "-t-m", true);
    }
}

