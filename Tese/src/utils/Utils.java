package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class Utils {

	private String path = "C:\\Users\\Daniel\\Dropbox\\Bolsa\\Dissertacao\\Metrics\\";

	public void CSV2arff(String path,String str){ 
		// load CSV
		CSVLoader loader = new CSVLoader();
		try {
			loader.setSource(new File(path+str+".csv"));

			Instances data;
			data = loader.getDataSet();

			// save ARFF
			ArffSaver saver = new ArffSaver();
			saver.setInstances(data);
			saver.setFile(new File(path+str+".arff"));

			//saver.setDestination(new File(path+str+".arff"));
			saver.writeBatch();
			loader = new CSVLoader();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void metrics(int[][] matrix, String[] classes,String ds, Classifier j, String approach, int steps, String algorithm) {
		try {
			String[] split = j.getClass().toString().split("\\.");
			String classs = split[split.length-1];
			System.out.println(classs);
			BufferedWriter out = new BufferedWriter(new FileWriter(path+ds+"_"+classs+"_"+approach+"_"+algorithm+"_"+steps+".csv"));
			
			out.write(ds+"\n");
			out.write("Approach,Algorithm,Steps\n");
			out.write(approach+","+algorithm+","+steps+ "\n\n");
			if(algorithm.equals("AdaBoost")){
				int i = 0;
			}
			int size = matrix.length;
			double all = 0;
			double right = 0;
			for (int line = 0; line < size; line++) {
				for (int col = 0; col < size; col++) {
					int val = matrix[line][col];
					if(line == col){
						right += val;
					}
					all += val;
				}
			}
			double accuracy = (right/all);
			DecimalFormat df = new DecimalFormat("#.##");
			out.write("Accuracy,"+df.format(accuracy*100).replace(",",".")+ "%\n\n");
			
			double TP=0, TN=0, FP=0, FN = 0;
			for (int i = 0; i < classes.length; i++) {
				out.write(classes[i]+"\n");
				TP=0; TN=0; FP=0; FN = 0;
				for (int line = 0; line < size; line++) {
					for (int col = 0; col < size; col++) {
						int val = matrix[line][col];
						if(line == i && col == i){
							TP += val;
						}else{
							if(line == i){
								FP += val;
							}else{
								if (col == i) {
									FN += val;
								}else{
									TN += val;
								}
							}
						}
					}
				}
				//			System.out.println("TP - " + TP);
				//			System.out.println("FP - " + FP);
				//			System.out.println("TN - " + TN);
				//			System.out.println("FN - " + FN);
				double precision = (TP/(TP+FP));
				if(Double.isNaN(precision)){
					precision = 0;
				}
				double sensitivity = (TP/(TP+FN));
				if(Double.isNaN(sensitivity)){
					sensitivity = 0;
				}
				double specificity = (TN /(FP+TN));
				if(Double.isNaN(specificity)){
					specificity = 0;
				}
				double fm = 2* ((precision*sensitivity)/(precision+sensitivity));
				if(Double.isNaN(fm)){
					fm = 0;
				}
				out.write("Precision,"+ df.format(precision*100).replace(",",".")+ " %\n");
				out.write("Sensitivity,"+ df.format(sensitivity*100).replace(",",".")+ " %\n");
				out.write("Specificity,"+ df.format(specificity*100).replace(",",".")+ " %\n");
				out.write("F-measure,"+ df.format(fm*100).replace(",",".")+ " %\n");
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
