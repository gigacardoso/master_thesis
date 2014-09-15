package tdm.classification.run.als;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Random;

import tdm.classification.utils.Utils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;


public class Baseline {

	private static String path = "C:" + File.separator + "PROACT_2013_08_27_ALL_FORMS" +
			File.separator + "FormatedData"+ File.separator + "Baseline" + File.separator;
	private static String pathNoClass = path + "NoClass" + File.separator;
	private final static int folds = 10;
	private final static int steps = 3;

	public static void main(String[] args) {
		Baseline b = new Baseline();

//		for(int i=0;i<steps-1;i++){
		int i=steps-2;
		System.out.println("------------------\tBaseline "+ i + " -> " + (steps-1) + "\t------------------");
		System.out.println("\n--------------------------\tWITHOUT CLASS\t--------------------------------");
					b.Classify(new NaiveBayes(), folds, "baselineNoClass"+i, pathNoClass);
					b.Classify(new J48(), folds, "baselineNoClass"+i, pathNoClass);
//					b.Classify(new AdaBoostM1(), folds, "baselineNoClass"+i, pathNoClass);
					b.Classify(new Logistic(), folds, "baselineNoClass"+i, pathNoClass);
					b.Classify(new RandomForest(), folds, "baselineNoClass"+i, pathNoClass);
//	}
	}

	private void Classify(Classifier classifier, int folds, String file,String path){
		System.out.println(classifier.getClass());
		try{
			Instances data = new Instances(new BufferedReader(new FileReader(path+file+".arff")));
			//
			// Create Classifier
			Classifier cModel = classifier;   
			data.setClassIndex(data.numAttributes() - 1);
			Evaluation eval = new Evaluation(data);
			eval.crossValidateModel(cModel, data, 10, new Random(1));

			// Print the result à la Weka explorer:
			String strSummary = eval.toSummaryString();
			//				System.out.println(cModel.toString());

			//Get the confusion matrix
			double[][] m = eval.confusionMatrix();
			System.out.println("\n\t\t"+ classifier.getClass().toString());
			System.out.println(strSummary);
			String s = eval.toMatrixString();
			System.out.println(s);
			String[] split = s.split("=");
			String[] classes_simb = new String[4];
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
			u.metrics(matrix,classes_simb,"ALS",null,"BaselineSingle", steps, classs);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
}