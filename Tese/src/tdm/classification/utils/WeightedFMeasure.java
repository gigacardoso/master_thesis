package tdm.classification.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class WeightedFMeasure {

	private static String path = "C:\\Users\\Daniel\\Dropbox\\Bolsa\\Dissertacao\\Metrics\\";
	private static String als = path + "ALS\\";
	private static String als_linear = path + "ALS\\Linear\\";
	private static String als_j48 = path + "ALS\\J48\\";
	private static String als_hmm = path + "ALS\\HMM\\";
	private static String als_base = path + "ALS\\Baseline\\";
	private static String hep = path + "Hepatitis\\";
	private static String hmm = path + "Hepatitis\\HMM\\";
	private static String j48 = path + "Hepatitis\\J48\\";
	private static String log = path + "Hepatitis\\Logistic\\";
	private static String base = path + "Hepatitis\\Baseline\\";
	private static String averaged = path + "Averaged\\";
	private static String unaveraged = path + "Unaveraged\\";
	private String graph = averaged + "Graph\\";
	private String graph2 = unaveraged;
	private static int als_classes = 4;
	private static String[] als_classes_simb = {"{0-12}","{12-24}","{24-36}","{36-48}"};
	private static double[] als_weight = {0.059,0.223,0.291,0.427};
	private static int hep_classes = 5;
	private static String[] hep_classes_simb = {"F0","F1","F2","F3","F4"};
	private static double[] hep_weight = {0.0205,0.459,0.2135,0.1519,0.154};
	private static String[] techs = {"NaiveBayes","J48","RandomForest","Logistic"};
	private static String[] hep_steps = {"3",	"7",	"12"};
	private static String[] als_steps = {"3"	,"5",	"6"};

	public static void main(String[] args) {
		try {
//			createGraphFile(als,als_base,"ALS","F-measure",false,null);
			createGraphFile(als,als_hmm,"ALS","F-measure",false,null);
			createGraphFile(als,als_j48,"ALS","F-measure",true,null);
			createGraphFile(als,als_linear,"ALS","F-measure",false,"Linear");
//			createGraphFile(hep,base,"Hepatitis","F-measure",false,null);
			createGraphFile(hep,hmm,"Hepatitis","F-measure",false,null);
			createGraphFile(hep,j48,"Hepatitis","F-measure",false,null);
			createGraphFile(hep,log,"Hepatitis","F-measure",false,null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private static void createGraphFile(String output, String input, String type, String to_save, boolean off, String to_add) throws IOException {
		File file = new File(input);
		String[] myFiles;
		String lastapp = null;
		String estimation = to_add;
		String approach = null;
		// class --> tech -> step -> percentag
		int offset=0;
		if(off){
			offset++;
		}
		if(to_add != null){
			offset = -1;
		}
		HashMap<String,HashMap<String, HashMap<String, String>>> values = new HashMap<String,HashMap<String, HashMap<String, String>>>();  
		if (file.isDirectory()) {
			myFiles = file.list();
			for (int i = 0; i < myFiles.length; i++) {
				File myFile = new File(file, myFiles[i]);
				if (!myFile.isDirectory()) {
					String name = myFile.getName();
					String[] splited = name.split("_");
					if(to_add == null){
						estimation = splited[1];
						approach = splited[2];
					}else{
						approach = splited[1];
					}
					String tech = splited[3+offset];
					String step = splited[4+offset].split("\\.")[0];
					if(approach.equals(lastapp)){
						saveMetrics(myFile,tech, step, values, to_save);
					}
					else{
						if(lastapp == null){
							saveMetrics(myFile,tech, step, values, to_save);
							lastapp = approach;
						}else{
							printToGraph(output,type,estimation,lastapp,to_save, values);
							lastapp = approach;
							values = new HashMap<String, HashMap<String,HashMap<String,String>>>();
							saveMetrics(myFile,tech, step, values, to_save);
						}
					}

				}
			}
			printToGraph(output,type,estimation,lastapp,to_save, values);
		}
	}


	private static void printToGraph(String output_path, String type, String estim, String lastapp, String to_save, HashMap<String, HashMap<String, HashMap<String, String>>> values) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(output_path+"weighted\\" +to_save+"_"+ type +"_"+estim + "_" +lastapp + ".csv"));
		String[] stepss;
		String[] classes;
		double[] weight;
		if(type.equals("ALS")){
			stepss = als_steps;
			classes = als_classes_simb;
			weight = als_weight;
		}else{
			stepss = hep_steps;
			classes = hep_classes_simb;
			weight = hep_weight;
		}
		String colunas= ",";
		for (int i = 0; i < stepss.length; i++) {
			colunas += stepss[i] +" Steps,";
		}
		colunas = colunas.substring(0, colunas.length()-1);
		out.write(colunas+"\n");
		for (int i = 0; i < techs.length; i++) {
			String output = techs[i]+",";
			for (int j = 0; j < stepss.length; j++) {
				Double val = 0.0;
				for (int r = 0; r < classes.length; r++) {
					String c = classes[r];
					String t = techs[i];
					String s = stepss[j];
					HashMap<String, HashMap<String, String>> a = values.get(c);
					HashMap<String, String> b = a.get(t);
					String y = b.get(s);
					Double d = Double.parseDouble(y);
					val += d*weight[r];
				}
				output += val+"%,";
			}
			output = output.substring(0, output.length()-1);
			out.write(output+"\n");
		}
		out.close();
	}


	private static void saveMetrics(File myFile,
			String tech, String step, HashMap<String, HashMap<String, HashMap<String, String>>> values, String to_save) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(myFile));
		for (int j = 0; j < 6; j++) {
			in.readLine();
		}
		String line;
		String current_class = null;
		while( (line = in.readLine()) != null){
			String[] split = line.split(",");
			if(split.length > 1){
				String metric = split[0];
				if(metric.equals(to_save)){
					String perc = split[1];
					perc = perc.substring(0,perc.length()-1);
					HashMap<String, HashMap<String, String>> techtree = values.get(current_class);
					HashMap<String, String> steptree ;
					if(techtree == null){
						techtree = new HashMap<String, HashMap<String,String>>();
						steptree = new HashMap<String,String>();
						steptree.put(step, perc);
					}else{
						steptree = techtree.get(tech);
						if(steptree == null){
							steptree = new HashMap<String,String>();
						}
						steptree.put(step, perc);
					}
					techtree.put(tech,steptree);
					values.put(current_class,techtree);
				}
			}else{
				current_class = split[0];
			}
		}
		in.close();
	}

}
