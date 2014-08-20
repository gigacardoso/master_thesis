package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class AverageMetrics {

	private static String path = "C:\\Users\\Daniel\\Dropbox\\Bolsa\\Dissertacao\\Metrics\\";
	private static String als = path + "ALS\\";
	private static String hmm = path + "Hepatitis\\HMM\\";
	private static String j48 = path + "Hepatitis\\J48\\";
	private static String log = path + "Hepatitis\\Logistic\\";
	private String output = path + "Averaged\\";
	private static int als_classes = 4;
	private static int hep_classes = 5;
	private int steps = 3;
		
	public static void main(String[] args) {
		try {
			AverageMetrics a = new AverageMetrics();
			a.average(als,"ALS", als_classes);
			a.average(hmm,"Hepatitis_HMM", hep_classes);
			a.average(j48,"Hepatitis_J48", hep_classes);
			a.average(log,"Hepatitis_Logistic", hep_classes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void average(String input, String type, int classes) throws IOException {
		File file = new File(input);
		String[] myFiles;
		String lastapp = null;
		String lasttech = null;
		File lastFile = null;
		int offset = 0;
		if(!type.equals("ALS")){
			offset = 1;
		}				
		ArrayList<Double> sums = new ArrayList<Double>(); 
		if (file.isDirectory()) {
			myFiles = file.list();
			for (int i = 0; i < myFiles.length; i++) {
				File myFile = new File(file, myFiles[i]);
				if (!myFile.isDirectory()) {
					String name = myFile.getName();
					String[] splited = name.split("_");
					String approach = splited[1+offset];
					String tech = splited[2+offset];
					BufferedReader in = new BufferedReader(new FileReader(myFile));
					for (int j = 0; j < 6; j++) {
						in.readLine();
					}
					if(approach.equals(lastapp) && tech.equals(lasttech)){
						sums = addValues(in, sums);
					}else{
						if(lastapp != null){
							printAverage(type,lastapp,lasttech, sums, lastFile);
						}
						lastapp = approach;
						lasttech = tech;
						sums = new ArrayList<Double>();
						for (int j = 0; j < classes*4; j++) {
							sums.add(0.0);
						}
						sums = addValues(in, sums);
					}
					lastFile = myFile;
					in.close();
				}
			}
			printAverage(type,lastapp,lasttech, sums, lastFile);
		}
	}

	private void printAverage(String string, String lastapp, String lasttech, ArrayList<Double> sums, File lastFile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(lastFile));
		BufferedWriter out = new BufferedWriter(new FileWriter(output + string + "_" +lastapp + "_"+lasttech+".csv"));
		for (int j = 0; j < 6; j++) {
			in.readLine();
		}
		String line;
		int index = 0;
		while( (line = in.readLine()) != null){
			String[] split = line.split(",");
			if(split.length > 1){
				Double d = sums.get(index);
				index++;
				out.write(split[0]+"," + (d/steps) + "%\n");
			}else{
				out.write(line + "\n");
			}
		}
		out.close();
		in.close();
	}

	private ArrayList<Double> addValues(BufferedReader in, ArrayList<Double> sums) throws IOException {
		int index = 0;
		String line;
		while( (line = in.readLine()) != null){
			String[] split = line.split(",");
			if(split.length > 1){
				double d = Double.parseDouble(split[1].substring(0, split[1].length()-1));
				d += sums.get(index);
				sums.set(index, d);
				index++;
			}
		}
		return sums;
	}
}
