package tdm.classification.run.hepatitis;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

import tdm.classification.utils.Utils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;


public class BaselineMulti {

	private static String data = "C:\\hepat_data030704\\";
	private static String path = data +"data\\";
	private final static int folds = 10;
	private final static int steps = 12;

	public static void main(String[] args) {
		BaselineMulti aa = new BaselineMulti();
		aa.Classify(new NaiveBayes(), folds, "baselineMultiWithout");
		aa.Classify(new RandomForest(), folds, "baselineMultiWithout");
		aa.Classify(new J48(), folds, "baselineMultiWithout");
//		aa.Classify(new AdaBoostM1(), folds, "baselineMultiWithout");
		aa.Classify(new Logistic(), folds, "baselineMultiWithout");
//		System.out.println("---------------------------------------------------------------");
	}

	private void Classify(Classifier classifier, int folds, String file){
		try{
			Instances data = new Instances(new BufferedReader(new FileReader(path+file+".arff")));
			//
			// Create Classifier
			Classifier cModel = classifier;   
			data.setClassIndex(data.numAttributes() - 1);
			Evaluation eval = new Evaluation(data);
			eval.crossValidateModel(cModel, data, 10, new Random(1));

			// Print the result � la Weka explorer:
			String strSummary = eval.toSummaryString();
			//				System.out.println(cModel.toString());

			//Get the confusion matrix
			double[][] m = eval.confusionMatrix();
			System.out.println("\n\t\t"+ classifier.getClass().toString());
			System.out.println(strSummary);
			String s = eval.toMatrixString();
			String[] split = s.split("=");
			String[] classes_simb = new String[5];
			for (int i = 0; i < split.length-7; i++) {
				String tmp = split[7+i].split(" ")[1];
				classes_simb[i] = tmp.substring(0, tmp.length()-1);
			}
			Utils u = new Utils();
			String[] splitt = classifier.getClass().toString().split("\\.");
			String classs = splitt[splitt.length-1];
			int[][] matrix = new int[m.length][m[0].length];
			for (int i = 0; i < m.length; i++) {
				for (int j = 0; j < m[0].length; j++) {
					matrix[i][j] = (int) m[i][j];
				}
			}
			u.metrics(matrix,classes_simb,"Hepatitis",null,"BaselineMulti", steps, classs);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
}
