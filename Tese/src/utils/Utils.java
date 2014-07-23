package utils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class Utils {

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

	public void metrics(int[][] matrix, String[] classes) {
		int size = matrix.length;
		for (int i = 0; i < classes.length; i++) {
			System.out.println(classes[i]);
			double TP=0, TN=0, FP=0, FN = 0;
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
			double sensitivity = (TP/(TP+FN));
			double specificity = (TN /(FP+TN));
			double fm = 2* ((precision*sensitivity)/(precision+sensitivity));
			DecimalFormat df = new DecimalFormat("#.##");
			System.out.println("Precision - "+ df.format(precision*100)+ " %");
			System.out.println("Sensitivity - "+ df.format(sensitivity*100)+ " %");
			System.out.println("Specificity - "+ df.format(specificity*100)+ " %");
			System.out.println("F-measure - "+ df.format(fm*100)+ " %");
		}
	}
}
