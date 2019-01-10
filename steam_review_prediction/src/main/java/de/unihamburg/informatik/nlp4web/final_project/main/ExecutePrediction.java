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
import org.cleartk.util.cr.FilesCollectionReader;

import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import de.unihamburg.informatik.nlp4web.final_project.annotator.NewsClassificationAnnotator;
import de.unihamburg.informatik.nlp4web.final_project.reader.JsonReviewReader;
import de.unihamburg.informatik.nlp4web.final_project.reader.NewsTitleReader;
import de.unihamburg.informatik.nlp4web.final_project.writer.NewsClassificationAnalyzer;
import de.unihamburg.informatik.nlp4web.final_project.writer.TestWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

public class ExecutePrediction {
	
//    public static void writeModel(File saTrain, File modelDirectory, File stopwordsFile, boolean newFeatureIncluded)
//            throws ResourceInitializationException, UIMAException, IOException {
//        System.err.println("Step 1: Extracting features and writing raw instances data");
//
//        if (newFeatureIncluded) {
//        	runPipeline(
//                    FilesCollectionReader.getCollectionReaderWithSuffixes(saTrain.getAbsolutePath(),
//                            NewsTitleReader.NEWS_CSV_VIEW, saTrain.getName()),
//                    createEngine(NewsTitleReader.class, NewsTitleReader.PARAM_STOP_WORDS, stopwordsFile),
//                    createEngine(StanfordPosTagger.class, StanfordPosTagger.PARAM_LANGUAGE, "en"),
//                    createEngine(StanfordLemmatizer.class),
//                    createEngine(AnalysisEngineFactory.createEngineDescription(
//                            NewsClassificationAnnotator.class,
//                            NewsClassificationAnnotator.PARAM_IS_TRAINING, true,
//                            NewsClassificationAnnotator.PARAM_NEW_FEATURE_ACTIVE, true,
//                            NewsClassificationAnnotator.PARAM_DIRECTORY_NAME, modelDirectory,
//                            DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
//                            InstanceDataWriter.class.getName(),
//                            DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
//                            modelDirectory))
//            );
//        } else {
//        	runPipeline(
//                    FilesCollectionReader.getCollectionReaderWithSuffixes(saTrain.getAbsolutePath(),
//                            NewsTitleReader.NEWS_CSV_VIEW, saTrain.getName()),
//                    createEngine(NewsTitleReader.class, NewsTitleReader.PARAM_STOP_WORDS, stopwordsFile),
//                    createEngine(AnalysisEngineFactory.createEngineDescription(
//                            NewsClassificationAnnotator.class,
//                            NewsClassificationAnnotator.PARAM_IS_TRAINING, true,
//                            NewsClassificationAnnotator.PARAM_NEW_FEATURE_ACTIVE, false,
//                            NewsClassificationAnnotator.PARAM_DIRECTORY_NAME, modelDirectory,
//                            DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
//                            InstanceDataWriter.class.getName(),
//                            DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
//                            modelDirectory))
//            );
//        }
//    }
//
//    public static void trainModel(File modelDirectory, String[] arguments) throws Exception {
//        /** Stage 3: Train and write model
//         * Now that the features have been extracted and normalized, we can proceed
//         *in running machine learning to train and package a model **/
//
//        System.err.println("Train model and write model.jar file.");
//        HideOutput hider = new HideOutput();
//        JarClassifierBuilder.trainAndPackage(modelDirectory, arguments);
//        hider.restoreOutput();
//    }
//
//    public static void classifyTestFile(File modelDirectory, File saTest, File result, File resultScores, File stopwordsFile, boolean newFeatureIncluded)
//            throws ResourceInitializationException, UIMAException, IOException {
//
//    	if (newFeatureIncluded) {
//    		runPipeline(
//                    FilesCollectionReader.getCollectionReaderWithSuffixes(saTest.getAbsolutePath(),
//                            NewsTitleReader.NEWS_CSV_VIEW, saTest.getName()),
//                    createEngine(NewsTitleReader.class, NewsTitleReader.PARAM_STOP_WORDS, stopwordsFile),
//                    createEngine(StanfordPosTagger.class, StanfordPosTagger.PARAM_LANGUAGE, "en"),
//                    createEngine(StanfordLemmatizer.class),
//                    createEngine(AnalysisEngineFactory.createEngineDescription(
//                            NewsClassificationAnnotator.class,
//                            NewsClassificationAnnotator.PARAM_IS_TRAINING, false,
//                            NewsClassificationAnnotator.PARAM_NEW_FEATURE_ACTIVE, true,
//                            NewsClassificationAnnotator.PARAM_DIRECTORY_NAME, modelDirectory,
//                            NewsClassificationAnnotator.PARAM_TF_IDF_URI,
//                            NewsClassificationAnnotator.createTokenTfIdfDataURI(modelDirectory),
//                            NewsClassificationAnnotator.PARAM_TF_IDF_CENTROID_SIMILARITY_URI,
//                            NewsClassificationAnnotator.createIdfCentroidSimilarityDataURI(modelDirectory),
//                            NewsClassificationAnnotator.PARAM_MINMAX_URI,
//                            NewsClassificationAnnotator.createMinMaxDataURI(modelDirectory),
//                            GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, modelDirectory + "/model.jar")
//                    ),
//                    createEngine(NewsClassificationAnalyzer.class,
//                    		NewsClassificationAnalyzer.PARAM_OUTPUT_DIR, result.getAbsolutePath(),
//                    		NewsClassificationAnalyzer.PARAM_OUTPUT_SCORES, resultScores.getAbsolutePath()));
//    	} else {
//    		runPipeline(
//                    FilesCollectionReader.getCollectionReaderWithSuffixes(saTest.getAbsolutePath(),
//                            NewsTitleReader.NEWS_CSV_VIEW, saTest.getName()),
//                    createEngine(NewsTitleReader.class, NewsTitleReader.PARAM_STOP_WORDS, stopwordsFile),
//                    createEngine(AnalysisEngineFactory.createEngineDescription(
//                            NewsClassificationAnnotator.class,
//                            NewsClassificationAnnotator.PARAM_IS_TRAINING, false,
//                            NewsClassificationAnnotator.PARAM_NEW_FEATURE_ACTIVE, false,
//                            NewsClassificationAnnotator.PARAM_DIRECTORY_NAME, modelDirectory,
//                            NewsClassificationAnnotator.PARAM_TF_IDF_URI,
//                            NewsClassificationAnnotator.createTokenTfIdfDataURI(modelDirectory),
//                            NewsClassificationAnnotator.PARAM_TF_IDF_CENTROID_SIMILARITY_URI,
//                            NewsClassificationAnnotator.createIdfCentroidSimilarityDataURI(modelDirectory),
//                            NewsClassificationAnnotator.PARAM_MINMAX_URI,
//                            NewsClassificationAnnotator.createMinMaxDataURI(modelDirectory),
//                            GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, modelDirectory + "/model.jar")
//                    ),
//                    createEngine(NewsClassificationAnalyzer.class,
//                    		NewsClassificationAnalyzer.PARAM_OUTPUT_DIR, result.getAbsolutePath(),
//                    		NewsClassificationAnalyzer.PARAM_OUTPUT_SCORES, resultScores.getAbsolutePath()));
//    	}
//    }
//    
//    public static void execute(String train, String test, String name, boolean includesNewFeature) throws Exception {
//    	String generalPath = "src/main/resources/";
//    	
//        String[] trainingArguments = new String[]{"-t", "0"};
//        long start = System.currentTimeMillis();
//
//        String modelPath = generalPath + "model" + name + "/";
//        new File(modelPath).mkdirs();
//        File modelDir = new File(modelPath);
//        
//        String resltPath = generalPath + "results" + name + "/";
//        new File(resltPath).mkdirs();
//        File resultDir = new File(resltPath+"news.csv");
//        File resultScores = new File(resltPath+"test-scores_" + includesNewFeature + ".txt");
//
//        File newsTrain = new File(generalPath + train);
//        File newsTest = new File(generalPath + test);
//        
//        File stopWordsFile = new File(generalPath + "stopwords.txt");
//       
//        writeModel(newsTrain, modelDir, stopWordsFile, includesNewFeature);
//        trainModel(modelDir, trainingArguments);
//        classifyTestFile(modelDir, newsTest, resultDir, resultScores, stopWordsFile, includesNewFeature);
//        long now = System.currentTimeMillis();
//        UIMAFramework.getLogger().log(Level.INFO, "Time: " + (now - start) + "ms");
//    }
    
    public static void main(String[] args) throws UIMAException, IOException {
    	
    	CollectionReader reader = CollectionReaderFactory.createReader(JsonReviewReader.class,
    			JsonReviewReader.PARAM_DIRECTORY, Paths.get("src/main/resources/steam-reviews").toString(),
    			JsonReviewReader.PARAM_LANGUAGE, "en");
    	
    	AnalysisEngine segmenter = createEngine(StanfordSegmenter.class);
    	AnalysisEngine writer = createEngine(TestWriter.class);
    	
    	SimplePipeline.runPipeline(
    			reader,
    			segmenter,
    			writer
    	);
    	
//    	execute("news/uci-news-aggregator.csv.train.small", "news/uci-news-aggregator.csv.test.small", "small", false);
//    	execute("news/uci-news-aggregator-b-e.csv.train", "news/uci-news-aggregator-b-e.csv.test", "-b-e", false);
//    	execute("news/uci-news-aggregator-b-m.csv.train", "news/uci-news-aggregator-b-m.csv.test", "-b-m", false);
//    	execute("news/uci-news-aggregator-b-t.csv.train", "news/uci-news-aggregator-b-t.csv.test", "-b-t", false);
//    	execute("news/uci-news-aggregator-e-m.csv.train", "news/uci-news-aggregator-e-m.csv.test", "-e-m", false);
//    	execute("news/uci-news-aggregator-t-e.csv.train", "news/uci-news-aggregator-t-e.csv.test", "-t-e", false);
//    	execute("news/uci-news-aggregator-t-m.csv.train", "news/uci-news-aggregator-t-m.csv.test", "-t-m", false);
//    	
//    	execute("news/uci-news-aggregator.csv.train.small", "news/uci-news-aggregator.csv.test.small", "small", true);
//    	execute("news/uci-news-aggregator-b-e.csv.train", "news/uci-news-aggregator-b-e.csv.test", "-b-e", true);
//    	execute("news/uci-news-aggregator-b-m.csv.train", "news/uci-news-aggregator-b-m.csv.test", "-b-m", true);
//    	execute("news/uci-news-aggregator-b-t.csv.train", "news/uci-news-aggregator-b-t.csv.test", "-b-t", true);
//    	execute("news/uci-news-aggregator-e-m.csv.train", "news/uci-news-aggregator-e-m.csv.test", "-e-m", true);
//    	execute("news/uci-news-aggregator-t-e.csv.train", "news/uci-news-aggregator-t-e.csv.test", "-t-e", true);
//    	execute("news/uci-news-aggregator-t-m.csv.train", "news/uci-news-aggregator-t-m.csv.test", "-t-m", true);
    }
}

