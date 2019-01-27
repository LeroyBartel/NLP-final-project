package de.unihamburg.informatik.nlp4web.final_project.writer;

import org.apache.commons.io.IOUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import de.unihamburg.informatik.nlp4web.final_project.type.ReviewAnnotation;

import java.io.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.apache.uima.fit.util.JCasUtil.select;

public class ReviewClassificationAnalyzer extends JCasAnnotator_ImplBase {

	public static final String LF = System.getProperty("line.separator");
    public static final String PARAM_OUTPUT_DIR = "InputFile";
    public static final String PARAM_OUTPUT_SCORES = "ScoresFile";

    @ConfigurationParameter(name = PARAM_OUTPUT_DIR, mandatory = true)
    private String outputDir;
    
    @ConfigurationParameter(name = PARAM_OUTPUT_SCORES, mandatory = true)
    private String outputDirScores;

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {

    	FileHandler fhandler;
		try {
			Logger logger = Logger.getLogger("myLogger");
			fhandler = new FileHandler(outputDirScores, true);
			fhandler.setFormatter(new SimpleFormatter());
	    	logger.addHandler(fhandler);
	        
	        try {
	            FileOutputStream result = new FileOutputStream(outputDir);
	            IOUtils.write("TITLE\tGOLD\tPREDICTED\n", result,"UTF-8");
	        	for (ReviewAnnotation reviews : select(jCas, ReviewAnnotation.class)) {
	        		String predicted = reviews.getPredictValue();
	        		String gold = reviews.getGoldValue();
	        		IOUtils.write(reviews.getTitle() +"\t"+ gold +"\t"+predicted+"\n", result, "UTF-8");
	        	}
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        
	        int[][] confusion_mat = computeConfusionMatrix(jCas);
	        double acc = 100 * computeAccuracy(confusion_mat);
	        int total = computeTotalClassifications(confusion_mat);
	        int correct = computeCorrectClassifications(confusion_mat);
	        
	        logger.info(LF + "Total reviews " + total
	        		+ LF + "Total correct classifications " + correct
	        		+ LF + "Total incorrect classifications " + (total - correct)
	        		+ LF + "Accuracy " + String.format("%1$,.2f", acc) + "%"
	        		+ LF + "------------------------------------------------------------------------------------------------------------");
	        
	        logger.info(LF + "Precision \"voted_down\": " + computePrecision(confusion_mat, 0)
	        + LF + "Precision \"voted_up\": " + computePrecision(confusion_mat, 1)
	        + LF + "------------------------------------------------------------------------------------------------------------");
	        
	        logger.info(LF + "Recall \"voted_down\": " + computeRecall(confusion_mat, 0)
	        + LF + "Recall \"voted_up\": " + computeRecall(confusion_mat, 1)
	        + LF + "------------------------------------------------------------------------------------------------------------");
	        
	        logger.info(LF + "F1 \"voted_down\": " + computeF1(confusion_mat, 0)
	        + LF + "F1 \"voted_up\": " + computeF1(confusion_mat, 1)
	        + LF + "------------------------------------------------------------------------------------------------------------" + LF + LF);
	        
		fhandler.close();
		
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
    
    private int compConfMatHelper(String prediction) {
    	int index = -1;
    	switch (prediction) {
		case "0":
			index = 0;
			break;
		case "1":
			index = 1;
			break;
		default:
			break;
		}
    	return index;
    }
    
    private int[][] computeConfusionMatrix(JCas jCas) {
    	// Computes confusion matrix C[i,j] with fixed size 2x2 for all categories (voted_up/voted_down).
    	// Row indexes correspond to the actual true values, whereas columns correspond to the predictions.
    	// voted_down <-> idx 0; voted_up <-> idx 1; 
    	int[][] conf_mat = new int[2][2];
    	for (ReviewAnnotation reviews : select(jCas, ReviewAnnotation.class)) {
    		String predicted = reviews.getPredictValue();
    		String gold = reviews.getGoldValue();
    		
    		switch (gold) {
    		case "0":
    			conf_mat[0][compConfMatHelper(predicted)] += 1;
    			break;
    		case "1":
    			conf_mat[1][compConfMatHelper(predicted)] += 1;
    			break;
    		default:
    			break;
    		}
    	}
    	return conf_mat;
    }
    
    private int computeTotalClassifications(int[][] confusionMat) {
    	int total = 0;
    	int matdimension = confusionMat[0].length;
    	for (int i = 0; i < matdimension; i++) {
    		for (int j = 0; j < matdimension; j++) {
    			total += confusionMat[i][j];
    		}
    	}
    	return total;
    }
    
    private int computeCorrectClassifications(int[][] confusionMat) {
    	int correct = 0;
    	int matdimension = confusionMat[0].length;
    	for (int i = 0; i < matdimension; i++) {
    		for (int j = 0; j < matdimension; j++) {
    			if (i == j) {
    				correct += confusionMat[i][j];
    			}
    		}
    	}
    	return correct;
    }
    
    private double computeAccuracy(int[][] confusionMat) {
    	int total = computeTotalClassifications(confusionMat);
    	int correct = computeCorrectClassifications(confusionMat);
    	return ((double) correct / total);
    }
    
    private double computePrecision(int[][] confusionMat, int index) {
    	double precision = 0;
    	int true_pos = confusionMat[index][index];
    	int total_predicted_pos = 0;
    	for (int i = 0; i < confusionMat[0].length; i++) {
    		total_predicted_pos += confusionMat[i][index];
    	}
    	if (total_predicted_pos == 0) {
    		precision = 1.0;
    	} else {
    		precision = (double) true_pos / total_predicted_pos;
    	}
    	return precision;
    }
    
    private double computeRecall(int[][] confusionMat, int index) {
    	double recall = 0;
    	int true_pos = confusionMat[index][index];
    	int total_actual_pos = 0;
    	for (int j = 0; j < confusionMat[0].length; j++) {
    		total_actual_pos += confusionMat[index][j];
    	}
    	if (total_actual_pos == 0) {
    		recall = 1.0;
    	} else {
    		recall = (double) true_pos / total_actual_pos;
    	}
    	return recall;
    }
    
    private double computeF1(int[][] confusionMat, int index) {
    	double precision = computePrecision(confusionMat, index);
    	double recall = computeRecall(confusionMat, index);
    	double f1 = 2.0 * (precision * recall / (precision + recall));
    	return f1;
    }

}
