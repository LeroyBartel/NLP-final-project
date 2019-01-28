package de.unihamburg.informatik.nlp4web.final_project.main;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
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

import de.unihamburg.informatik.nlp4web.final_project.annotator.ReviewClassificationAnnotator;
import de.unihamburg.informatik.nlp4web.final_project.annotator.ReviewAnnotator;
import de.unihamburg.informatik.nlp4web.final_project.reader.JsonReviewReader;
import de.unihamburg.informatik.nlp4web.final_project.reader.PreparedDataReader;
import de.unihamburg.informatik.nlp4web.final_project.reader.ReviewCollector;
import de.unihamburg.informatik.nlp4web.final_project.writer.ReviewClassificationAnalyzer;

import java.io.File;
import java.io.IOException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

public class ExecutePrediction {
	
	public static void prepareReviewData(File dataSourceDir, File dataTargetFile, int nReviews) 
			throws ResourceInitializationException, UIMAException, IOException {
		SimplePipeline.runPipeline(
    			CollectionReaderFactory.createReader(JsonReviewReader.class,
		    			JsonReviewReader.PARAM_DIRECTORY, dataSourceDir.getAbsolutePath(),
		    			JsonReviewReader.PARAM_LANGUAGE, "en",
		    			JsonReviewReader.PARAM_REVIEW_COUNT, nReviews),
    			createEngine(ReviewCollector.class,
    					ReviewCollector.PARAM_OUTPUT_DIR, dataTargetFile.getAbsolutePath()));
	}
	
	public static void writeModel(File saTrain, File modelDirectory)
			throws ResourceInitializationException, UIMAException, IOException {
		System.err.println("Step 1: Extracting features and writing raw instances data");
		runPipeline(
				CollectionReaderFactory.createReader(PreparedDataReader.class,
		    			PreparedDataReader.PARAM_DIRECTORY, saTrain.getAbsolutePath(),
		    			PreparedDataReader.PARAM_LANGUAGE, "en"),
				createEngine(ReviewAnnotator.class),
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
		System.err.println("training finished.");
	}

	public static void classifyTestFile(File modelDirectory, File saTest, File result, File resultScores)
			throws ResourceInitializationException, UIMAException, IOException {
		runPipeline(
				CollectionReaderFactory.createReader(PreparedDataReader.class,
		    			PreparedDataReader.PARAM_DIRECTORY, saTest.getAbsolutePath(),
		    			PreparedDataReader.PARAM_LANGUAGE, "en"),
				createEngine(ReviewAnnotator.class),
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
		String[] trainingArguments = new String[]{"-t", "0"};
		long start = System.currentTimeMillis();

		String modelPath = generalPath + "model" + name + "/";
		new File(modelPath).mkdirs();
		File modelDir = new File(modelPath);

		String resltPath = generalPath + "results" + name + "/";
		new File(resltPath).mkdirs();
		File resultDir = new File(resltPath+"review_sentiment_prediction.txt");
		File resultScores = new File(resltPath+"test-scores.txt");

		String preparedDataPath = generalPath + "machine-learning-data/";
		new File(preparedDataPath).mkdirs();
		File reviewsTrainDir = new File(preparedDataPath + "training/");
		File reviewsTestDir = new File(preparedDataPath + "testing/");
		reviewsTrainDir.mkdirs();
		reviewsTestDir.mkdirs();
		
		File reviewsTrainFile = new File(preparedDataPath + "training/" + "training-reviews.txt");
		File reviewsTestFile = new File(preparedDataPath + "testing/" + "testing-reviews.txt");
		
		File reviewsTrainSourceDir = new File(generalPath + train);
		File reviewsTestSourceDir = new File(generalPath + test);
		
		prepareReviewData(reviewsTrainSourceDir, reviewsTrainFile, 1000);
		prepareReviewData(reviewsTestSourceDir, reviewsTestFile, 2000);
		writeModel(reviewsTrainDir, modelDir);
		trainModel(modelDir, trainingArguments);
		classifyTestFile(modelDir, reviewsTestDir, resultDir, resultScores);
		
		long now = System.currentTimeMillis();
		UIMAFramework.getLogger().log(Level.INFO, "Time: " + (now - start) + "ms");
	}
	
    public static void main(String[] args) {
    	String testFiles_dir = "evaluation-reviews/";
    	String trainingFiles_dir = "steam-reviews/";
    	try {
			execute(trainingFiles_dir, testFiles_dir, "_review_sentiment_prediction");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}

