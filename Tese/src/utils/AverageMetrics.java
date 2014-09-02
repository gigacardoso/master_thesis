package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class AverageMetrics {

	private static String path = "C:\\Users\\Daniel\\Dropbox\\Bolsa\\Dissertacao\\Metrics\\";
	private static String als = path + "ALS\\";
	private static String hmm = path + "Hepatitis\\HMM\\";
	private static String j48 = path + "Hepatitis\\J48\\";
	private static String log = path + "Hepatitis\\Logistic\\";
	private static String averaged = path + "Averaged\\";
	private static String unaveraged = path + "Unaveraged\\";
	private String graph = averaged + "Graph\\";
	private String graph2 = unaveraged;
	private static int als_classes = 4;
	private static int hep_classes = 5;
	private int steps = 3;
		
	public static void main(String[] args) {
		try {
			AverageMetrics a = new AverageMetrics();
//			a.average(als,"ALS", als_classes);
//			a.average(hmm,"Hepatitis_HMM", hep_classes);
//			a.average(j48,"Hepatitis_J48", hep_classes);
//			a.average(log,"Hepatitis_Logistic", hep_classes);
//			a.graphs(averaged);
			a.unaveragedGraph(hmm, hep_classes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void unaveragedGraph(String input, int hep_classes) throws NumberFormatException, IOException {
		File file = new File(input);
		String[] myFiles;
		String lastapp = null;
		String lasttype = null;
		File lastFile = null;
		int offset = 0;
		HashMap<String,ArrayList<Double>> values = new HashMap<String,ArrayList<Double>>();
		ArrayList<String> techs = new ArrayList<String>();
		if (file.isDirectory()) {
			myFiles = file.list();
			for (int i = 0; i < myFiles.length; i++) {
				File myFile = new File(file, myFiles[i]);
				if (!myFile.isDirectory()) {
					String name = myFile.getName();
					String[] splited = name.split("_");
					String type = splited[0];
					if(!type.equals("ALS")){
						offset = 1;
						type += "_" + splited[1];
					}else{
						offset = 0;
					}
					String approach = splited[1+offset];
					String tech = splited[2+offset].split("\\.")[0];
					BufferedReader in = new BufferedReader(new FileReader(myFile));
					for (int j = 0; j < 6; j++) {
						in.readLine();
					}
					if(approach.equals(lastapp)){
						values = saveValues(in, tech, values);
						techs.add(tech);
					}else{
						if(lastapp != null){
							printGraph2(lasttype,lastapp,techs ,values, lastFile);
						}
						lastapp = approach;
						lasttype = type;
						techs = new ArrayList<String>();
						techs.add(tech);
						values = saveValues(in, tech, values);
					}
					lastFile = myFile;
					in.close();
				}
			}
			printGraph2(lasttype,lastapp,techs, values, lastFile);
		}
	}

	private void printGraph2(String type, String lastapp,
			ArrayList<String> techs, HashMap<String, ArrayList<Double>> values,
			File lastFile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(lastFile));
		for (int j = 0; j < 6; j++) {
			in.readLine();
		}
		BufferedWriter out = new BufferedWriter(new FileWriter(graph2 + type + "_" +lastapp + ".csv"));
		String line;
		int index = 0;
		String tecnicas = ",";
		for (int i = 0; i < techs.size(); i++) {
			tecnicas += techs.get(i) + ",";
		}
		tecnicas = tecnicas.substring(0,tecnicas.length()-1);
		String classe = null;
		ArrayList<String> classes = new ArrayList<String>();
		Set<String> metrics = new HashSet<String>();
		HashMap<String, HashMap<String, String>> new_values = new HashMap<String, HashMap<String, String>>();
		while( (line = in.readLine()) != null){
			String[] split = line.split(",");
			if(split.length > 1){
				metrics.add(split[0]);
				String output = classe+",";
				for (int i = 0; i < techs.size(); i++) {
					output += values.get(techs.get(i)).get(index) + "%,";
				}
				output = output.substring(0,output.length()-1);
				HashMap<String, String> metric = new_values.get(split[0]);
				if(metric == null){
					metric = new HashMap<String, String>();
				}
				metric.put(classe,output);
				new_values.put(split[0], metric);
				index++;
			}else{
				classe = split[0];
				classes.add(classe);
			}
		}
		
		for(String m: metrics){
			out.write(m+"\n");
			out.write(tecnicas + "\n");
			for(String c: classes){
				out.write(new_values.get(m).get(c)+"\n");
			}
			out.write("\n");
		}
		out.close();
		in.close();
	}

	private void graphs(String input) throws NumberFormatException, IOException {
		File file = new File(input);
		String[] myFiles;
		String lastapp = null;
		String lasttype = null;
		File lastFile = null;
		int offset = 0;
		HashMap<String,ArrayList<Double>> values = new HashMap<String,ArrayList<Double>>();
		ArrayList<String> techs = new ArrayList<String>();
		if (file.isDirectory()) {
			myFiles = file.list();
			for (int i = 0; i < myFiles.length; i++) {
				File myFile = new File(file, myFiles[i]);
				if (!myFile.isDirectory()) {
					String name = myFile.getName();
					String[] splited = name.split("_");
					String type = splited[0];
					if(!type.equals("ALS")){
						offset = 1;
						type += "_" + splited[1];
					}else{
						offset = 0;
					}
					String approach = splited[1+offset];
					String tech = splited[2+offset].split("\\.")[0];
					BufferedReader in = new BufferedReader(new FileReader(myFile));
					if(approach.equals(lastapp)){
						values = saveValues(in, tech, values);
						techs.add(tech);
					}else{
						if(lastapp != null){
							printGraph(lasttype,lastapp,techs ,values, lastFile);
						}
						lastapp = approach;
						lasttype = type;
						techs = new ArrayList<String>();
						techs.add(tech);
						values = saveValues(in, tech, values);
					}
					lastFile = myFile;
					in.close();
				}
			}
			printGraph(lasttype,lastapp,techs, values, lastFile);
		}
	}

	private void printGraph(String type, String lastapp,
			ArrayList<String> techs, HashMap<String, ArrayList<Double>> values,
			File lastFile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(lastFile));
		BufferedWriter out = new BufferedWriter(new FileWriter(graph + type + "_" +lastapp + ".csv"));
		String line;
		int index = 0;
		String tecnicas = ",";
		for (int i = 1; i < techs.size(); i++) {
			tecnicas += techs.get(i) + ",";
		}
		tecnicas = tecnicas.substring(0,tecnicas.length()-1);
		String classe = null;
		ArrayList<String> classes = new ArrayList<String>();
		Set<String> metrics = new HashSet<String>();
		HashMap<String, HashMap<String, String>> new_values = new HashMap<String, HashMap<String, String>>();
		while( (line = in.readLine()) != null){
			String[] split = line.split(",");
			if(split.length > 1){
				metrics.add(split[0]);
				String output = classe+",";
				for (int i = 1; i < techs.size(); i++) {
					output += values.get(techs.get(i)).get(index) + "%,";
				}
				output = output.substring(0,output.length()-1);
				HashMap<String, String> metric = new_values.get(split[0]);
				if(metric == null){
					metric = new HashMap<String, String>();
				}
				metric.put(classe,output);
				new_values.put(split[0], metric);
				index++;
			}else{
				classe = split[0];
				classes.add(classe);
			}
		}
		
		for(String m: metrics){
			out.write(m+"\n");
			out.write(tecnicas + "\n");
			for(String c: classes){
				out.write(new_values.get(m).get(c)+"\n");
			}
			out.write("\n");
		}
		out.close();
		in.close();
	}

	private HashMap<String, ArrayList<Double>> saveValues(BufferedReader in,
			String tech, HashMap<String, ArrayList<Double>> values) throws NumberFormatException, IOException {
		String line;
		ArrayList<Double> sums = new ArrayList<Double>();
		while( (line = in.readLine()) != null){
			String[] split = line.split(",");
			if(split.length > 1){
				double d = Double.parseDouble(split[1].substring(0, split[1].length()-1));
				sums.add(d);
			}
		}
		values.put(tech, sums);
		return values;
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
		BufferedWriter out = new BufferedWriter(new FileWriter(averaged + string + "_" +lastapp + "_"+lasttech+".csv"));
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
