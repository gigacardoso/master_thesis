package run.als;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;


public class AlternativeApproach {

	private static String path = "C:" + File.separator + "PROACT_2013_08_27_ALL_FORMS" +
			File.separator + "FormatedData"+ File.separator + "AlternativeApproach" + File.separator;
	private final static int folds = 10;
	private final static int steps = 6;

	public static void main(String[] args) {
		AlternativeApproach aa = new AlternativeApproach();
		System.out.println("------------------\tWITH CLASS\t------------------");
		//		aa.Classify(new NaiveBayes(), folds, "approach1_"+steps);
		//		aa.Classify(new J48(), folds, "approach1_"+steps);
		//		aa.Classify(new AdaBoostM1(), folds, "approach1_"+steps);
		//		aa.Classify(new MultilayerPerceptron(), folds, "approach1_"+steps);
		//		aa.Classify(new Logistic(), folds, "approach1_"+steps);
		System.out.println("----------------------------------------------------------");
		System.out.println("------------------\tWITHOUT CLASS\t------------------");
		aa.Classify(new NaiveBayes(), folds, "approach1_NoClass_"+steps);
		aa.Classify(new RandomForest(), folds, "approach1_NoClass_"+steps);
		//		aa.Classify(new J48(), folds, "approach1_NoClass_"+steps);
		//		aa.Classify(new AdaBoostM1(), folds, "approach1_NoClass_"+steps);
		//		aa.Classify(new MultilayerPerceptron(), folds, "approach1_NoClass_"+steps);
		//		aa.Classify(new Logistic(), folds, "approach1_NoClass_"+steps);
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

			// Print the result à la Weka explorer:
			String strSummary = eval.toSummaryString();
			//				System.out.println(cModel.toString());

			//Get the confusion matrix
			double[][] matrix = eval.confusionMatrix();
			System.out.println("\n\t\t"+ classifier.getClass().toString());
			System.out.println(strSummary);
			//			String[] val = {"{36-48}","{24-36}","{12-24}","{0-12}"};
			//			System.out.println("\t\t{36-48}\t{24-36}\t{12-24}\t{0-12}");
			//			for(int i=0; i<4;i++){
			//				System.out.print(val[i] + "\t|");
			//				for(int j= 0;j<4;j++){
			//					System.out.print(matrix[i][j]+ "\t\t|");
			//				}
			//				System.out.println();
			//			}
			//			double tru = 0;
			//			double fal = 0;
			//			double bad = 0;
			//			for(int i=0; i<4;i++){
			//				for(int j= 0;j<4;j++){
			//					if(i==j){
			//						tru +=matrix[i][j]; 
			//					}else{
			//						bad +=matrix[i][j];
			//					}				
			//					fal += matrix[i][j];
			//				}
			//			}
			//			double accuracy = tru/fal;
			//			double errorRate = bad/fal;
			//						System.out.println("\nCorrectly Classified Instances\t\t"+ tru +"\t\t" + (accuracy*100) + " %");
			//						System.out.println("Inorrectly Classified Instances\t\t"+ bad +"\t\t" + (errorRate*100) + " %");
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
}
