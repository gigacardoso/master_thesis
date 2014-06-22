package createData.Hepatitis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import utils.DefaultHashMap;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.PropositionalToMultiInstance;
import createData.ALS.CreateData;

public class TableGeneratorCategories {

	private String data = "C:\\hepat_data030704\\";
	private String andreia = data + "andreia\\";
	private String out = data +"data\\";
	private HashMap<String,Integer> examsIndexes = new HashMap<String, Integer>();
	private DefaultHashMap<String,String> exams = new DefaultHashMap<String, String>("");
	private DefaultHashMap<String, String> biopsys = new DefaultHashMap<String, String>("");
	private DefaultHashMap<String, String> patients = new DefaultHashMap<String, String>("");
	private DefaultHashMap<String, String> headers = new DefaultHashMap<String, String>("");
	public static int steps = 3;

	private void examsPut(String s, int i){
		examsIndexes.put(s, i);
	}

	public static void main(String[] args) {
		TableGeneratorCategories table = new TableGeneratorCategories();
		String[] exams = {"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL","ALB","CHE","T-CHO","TP"};
		for(int i=0; i< exams.length; i++){
			table.examsPut(exams[i], 5+i);
		}
		try {
			table.readExams();
			table.readBiopsy();
			table.readPatients();
			table.createDiagnostic();
			table.createDiagnosisReal(steps);
			table.countTimePoints("Diagnosis.csv");
			table.createApproach1PredictionData(steps);
			table.createApproach2PredictionData(steps);
			table.createBaselineSingleOb(steps);
			table.createBaselineMultipleOb(steps);
			table.convertToArff();
			//table.convertToMultiInstanceArff();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createApproach2PredictionData(int steps) throws IOException {
		String[] exams = {"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL","ALB","CHE","T-CHO","TP","Type","Activity"/*,"Fibrosis"comment for no class*/};
		HashMap<String,Integer> indixes = new HashMap<String, Integer>();
		BufferedReader inFact = new BufferedReader(new FileReader(out + "Diagnosis.csv"));
		HashMap<Integer,BufferedWriter> writers = new HashMap<Integer,BufferedWriter>();
		String line = inFact.readLine();
		String[] splited = line.split(",");
		for(int i= 0; i< exams.length; i++){
			BufferedWriter table = new BufferedWriter(new FileWriter(out + exams[i]+"_2.csv"));
			String entry = splited[0]+",";
			for(int j= 0 ; j<steps-1; j++){
				for(int k = 0; k< exams.length; k++){
					entry += exams[k] +"_" + j + ",";
				}
			}
			entry += exams[i] +"_" + (steps-1) + "\n";
			table.write(entry);
			writers.put(i, table);
			indixes.put(exams[i], i);
		}
		String id= null;
		List<List<String>> group = new ArrayList<List<String>>(exams.length);

		while((line = inFact.readLine()) != null){
			splited = line.split(",");
			if(id == null){
				id = splited[0];
				for(int i= 0; i<exams.length; i++){
					ArrayList<String> list = new ArrayList<String>();
					list.add(splited[5+i]);
					group.add(list);
				}				
				continue;
			}else {
				if(splited[0].equals(id)){
					for(int i= 0; i<exams.length; i++){
						group.get(i).add(splited[5+i]);
					}	
				}else{
					if(	group.get(0).size() >= steps){
						System.out.println(id);
						if(id.equals("77")){
							@SuppressWarnings("unused")
							int j= 0;
						}
						outputLineExams2(id,writers,group,steps);
					}
					id = splited[0];
					group = new ArrayList<List<String>>(11);
					for(int i= 0; i<exams.length; i++){
						ArrayList<String> list = new ArrayList<String>();
						list.add(splited[5+i]);
						group.add(list);
					}				
				}
			}
		}
		for(int i= 0; i< exams.length; i++){
			writers.get(i).close();
		}
		inFact.close();
	}

	private void outputLineExams2(String id,
			HashMap<Integer, BufferedWriter> writers, List<List<String>> group,
			int steps) throws IOException {
		String[] exams = {"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL","ALB","CHE","T-CHO","TP","Type","Activity"/*,"Fibrosis"*/};
		int size = writers.keySet().size();
		for(int i=0; i < size ; i++){
			List<String> list = group.get(i);
			int length = list.size();
			String entry = id +",";
			for(int j= length-steps ; j<length-1; j++){
				for(int k = 0; k< exams.length; k++){
					List<String> exam = group.get(k);
					entry += exam.get(j)+ ",";
				}
			}
			entry += list.get(length-1)+ "\n";
			writers.get(i).write(entry);
		}
	}

	private void convertToArff() {
		File file = new File(out);
		CreateData create =  new CreateData();
		String[] myFiles;
		if (file.isDirectory()) {
			myFiles = file.list();
			for (int i = 0; i < myFiles.length; i++) {
				File myFile = new File(file, myFiles[i]);
				if (!myFile.isDirectory()) {
					create.CSV2arff(out, myFile.getName().split("\\.")[0]);
					try{
						myFile.delete();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void convertToMultiInstanceArff() throws Exception {
		File file = new File(out);
		String[] a = {"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL","ALB","CHE","T-CHO","TP","Type","Activity","Fibrosis"};
		ArrayList<String> exams = new ArrayList<String>(Arrays.asList(a));
		CreateData create =  new CreateData();
		String[] myFiles;
		if (file.isDirectory()) {
			myFiles = file.list();
			for (int i = 0; i < myFiles.length; i++) {
				File myFile = new File(file, myFiles[i]);
				if (!myFile.isDirectory() && exams.contains(myFile.getName().split("\\.")[0])) {
					if(!myFile.getName().split("\\.")[0].contains("_")){
						if(myFile.getName().split("\\.")[1].equals("arff")){
							create.CSV2arff(out, myFile.getName().split("\\.")[0]);
							System.out.println(myFile.getName().split("\\.")[0]);
							Instances d = new Instances(new BufferedReader(new FileReader(out+myFile.getName())));
							d.setClassIndex(d.numAttributes()-1);
							NumericToNominal num = new NumericToNominal();
							String[] options = new String[2];
							options[0] = "-R";                                    // "range"
							options[1] = "1";
							num.setOptions(options);
							num.setInputFormat(d);
							Instances tmp = Filter.useFilter(d, num);
							PropositionalToMultiInstance filter = new PropositionalToMultiInstance();
							options = new String[2];
							options[0] = "-S";                                    // "range"
							options[1] = "1";
							filter.setOptions(options);
							filter.setInputFormat(tmp);
							tmp.setClassIndex(tmp.numAttributes()-1);
							Instances newData = Filter.useFilter(tmp, filter);
							ArffSaver saver = new ArffSaver();
							saver.setInstances(newData);
							saver.setFile(new File(out+myFile.getName().split("\\.")[0]+"_Multi"+".arff"));
							saver.writeBatch();
						}
					}
					//					try{
					//						myFile.delete();
					//					}catch(Exception e){
					//						e.printStackTrace();
					//					}
				}
			}
		}
	}

	private void createBaselineMultipleOb(int steps) throws IOException {
		BufferedReader inFact = new BufferedReader(new FileReader(out + "Diagnosis.csv"));

		String line = inFact.readLine();
		String[] splited = line.split(",", -1);
		String headWith = "";
		String headWithout = "";
		for(int j= 0;j<steps-1;j++){
			for(int i= 0;i<splited.length;i++){
				headWith += splited[i]+"_"+j+",";
				if(i<splited.length-1){
					headWithout += splited[i]+"_"+j+",";
				}
			}
		}
		headWith += splited[splited.length-1]+"_class\n";
		headWithout += splited[splited.length-1]+"_class\n";
		BufferedWriter with = new BufferedWriter(new FileWriter(out + "baselineMultiWith.csv"));
		BufferedWriter without = new BufferedWriter(new FileWriter(out + "baselineMultiWithout.csv"));
		with.write(headWith);
		without.write(headWithout);		
		String id= null;
		List<String> lines = new ArrayList<String>();
		while((line = inFact.readLine()) != null){
			splited = line.split(",");
			if(id == null){
				id = splited[0];
				lines.add(line);			
			}else {
				if(splited[0].equals(id)){
					lines.add(line);
				}else{
					if(	lines.size() >= steps){
						System.out.println(id);
						if(id.equals("77")){
							@SuppressWarnings("unused")
							int j= 0;
						}
						outputLineMultiBaseline(id,with,without,lines,steps);
					}
					id = splited[0];
					lines = new ArrayList<String>();
					lines.add(line);
				}				
			}
		}
		with.close();
		without.close();
		inFact.close();
	}

	private void outputLineMultiBaseline(String id, BufferedWriter with,
			BufferedWriter without, List<String> lines, int steps) throws IOException {
		int size = lines.size();

		String entryWithClass = "";
		String entryWithoutClass = "";
		for(int i =0 ; i< steps-1; i++){
			String line = lines.get(size-steps+i);
			String[] split = line.split(",",-1);
			for(int j= 0; j<split.length; j++){
				entryWithClass += split[j]+ ",";
				if(j < split.length-1){
					entryWithoutClass += split[j]+ ",";
				}
			}
		}
		String[] last = lines.get(size-1).split(",",-1);
		entryWithClass += last[last.length-1]+ "\n";
		entryWithoutClass += last[last.length-1]+ "\n";
		with.write(entryWithClass);
		without.write(entryWithoutClass);
	}

	private void createDiagnosisReal(int steps) throws IOException {
		BufferedReader inFact = new BufferedReader(new FileReader(out + "Diagnosis.csv"));

		String line = inFact.readLine();
		String[] splited = line.split(",", -1);
		String headWithout = "";
		for(int i= 0;i<splited.length;i++){
			if(i<splited.length-1){
				headWithout += splited[i]+",";
			}
		}
		headWithout += splited[splited.length-1]+"_class\n";
		BufferedWriter without = new BufferedWriter(new FileWriter(out + "DiagnosisReal.csv"));
		without.write(headWithout);

		String id= null;
		List<String> lines = new ArrayList<String>();
		while((line = inFact.readLine()) != null){
			splited = line.split(",");
			if(id == null){
				id = splited[0];
				lines.add(line);			
			}else {
				if(splited[0].equals(id)){
					lines.add(line);
				}else{
					if(	lines.size() >= steps){
						System.out.println(id);
						if(id.equals("77")){
							@SuppressWarnings("unused")
							int j= 0;
						}
						outputLineDiagnosisReal(id,without,lines);
					}
					id = splited[0];
					lines = new ArrayList<String>();
					lines.add(line);
				}				
			}
		}
		without.close();
		inFact.close();
	}

	private void outputLineDiagnosisReal(String id, BufferedWriter without,
			List<String> lines) throws IOException {
		int size = lines.size();
		String line = lines.get(size-1);
		String[] split = line.split(",",-1);
		String entryWithoutClass = "";
		for(int j= 0; j<split.length; j++){
			if(j < split.length-1){
				entryWithoutClass += split[j]+ ",";
			}
		}
		String[] last = lines.get(size-1).split(",",-1);
		entryWithoutClass += last[last.length-1]+ "\n";
		without.write(entryWithoutClass);
	}

	private void createBaselineSingleOb(int steps) throws IOException {
		BufferedReader inFact = new BufferedReader(new FileReader(out + "Diagnosis.csv"));

		HashMap<Integer,BufferedWriter> with_all = new HashMap<Integer,BufferedWriter>();
		HashMap<Integer,BufferedWriter> without_all = new HashMap<Integer,BufferedWriter>();
		String line = inFact.readLine();
		String[] splited = line.split(",", -1);
		String headWith = "";
		String headWithout = "";
		for(int i= 0;i<splited.length;i++){
			headWith += splited[i]+",";
			if(i<splited.length-1){
				headWithout += splited[i]+",";
			}
		}
		headWith += splited[splited.length-1]+"_class\n";
		headWithout += splited[splited.length-1]+"_class\n";
		for(int i= 0;i<steps-1;i++){
			BufferedWriter with = new BufferedWriter(new FileWriter(out + "baselineSingleWith_"+i+"_.csv"));
			BufferedWriter without = new BufferedWriter(new FileWriter(out + "baselineSingleWithout_"+i+"_.csv"));
			with.write(headWith);
			without.write(headWithout);
			with_all.put(i, with);
			without_all.put(i, without);
		}

		String id= null;
		List<String> lines = new ArrayList<String>();
		while((line = inFact.readLine()) != null){
			splited = line.split(",");
			if(id == null){
				id = splited[0];
				lines.add(line);			
			}else {
				if(splited[0].equals(id)){
					lines.add(line);
				}else{
					if(	lines.size() >= steps){
						System.out.println(id);
						if(id.equals("77")){
							@SuppressWarnings("unused")
							int j= 0;
						}
						for(int i= 0;i<steps-1;i++){
							BufferedWriter with = with_all.get(i);
							BufferedWriter without = without_all.get(i);
							outputLineSingleBaseline(id,with,without,lines,i,steps);
						}
					}
					id = splited[0];
					lines = new ArrayList<String>();
					lines.add(line);
				}				
			}
		}
		for(int i= 0;i<steps-1;i++){
			with_all.get(i).close();
			without_all.get(i).close();
		}
		inFact.close();
	}

	private void outputLineSingleBaseline(String id, BufferedWriter with, BufferedWriter without,
			List<String> lines, int i, int steps) throws IOException {
		int size = lines.size();
		String line = lines.get(size-steps+i);
		String[] split = line.split(",",-1);
		String entryWithClass = "";
		String entryWithoutClass = "";
		for(int j= 0; j<split.length; j++){
			entryWithClass += split[j]+ ",";
			if(j < split.length-1){
				entryWithoutClass += split[j]+ ",";
			}
		}
		String[] last = lines.get(size-1).split(",",-1);
		entryWithClass += last[last.length-1]+ "\n";
		entryWithoutClass += last[last.length-1]+ "\n";
		with.write(entryWithClass);
		without.write(entryWithoutClass);

	}

	private void createApproach1PredictionData(int steps) throws IOException {
		String[] exams = {"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL","ALB","CHE","T-CHO","TP","Type","Activity"};
		HashMap<String,Integer> indixes = new HashMap<String, Integer>();
		BufferedReader inFact = new BufferedReader(new FileReader(out + "Diagnosis.csv"));
		HashMap<Integer,BufferedWriter> writers = new HashMap<Integer,BufferedWriter>();
		String line = inFact.readLine();
		String[] splited = line.split(",");
		for(int i= 0; i< exams.length; i++){
			BufferedWriter table = new BufferedWriter(new FileWriter(out + exams[i]+".csv"));
			String entry = splited[0]+",";
			for(int j= 0 ; j<steps-1; j++){
				entry += exams[i] +"_" + j + ",";
			}
			entry += exams[i] +"_" + (steps-1) + "\n";
			table.write(entry);
			writers.put(i, table);
			indixes.put(exams[i], i);
		}
		String id= null;
		List<List<String>> group = new ArrayList<List<String>>(exams.length);

		while((line = inFact.readLine()) != null){
			splited = line.split(",");
			if(id == null){
				id = splited[0];
				for(int i= 0; i<exams.length; i++){
					ArrayList<String> list = new ArrayList<String>();
					list.add(splited[5+i]);
					group.add(list);
				}				
				continue;
			}else {
				if(splited[0].equals(id)){
					for(int i= 0; i<exams.length; i++){
						group.get(i).add(splited[5+i]);
					}	
				}else{
					if(	group.get(0).size() >= steps){
						System.out.println(id);
						if(id.equals("77")){
							@SuppressWarnings("unused")
							int j= 0;
						}
						outputLineExams(id,writers,group,steps);
					}
					id = splited[0];
					group = new ArrayList<List<String>>(11);
					for(int i= 0; i<exams.length; i++){
						ArrayList<String> list = new ArrayList<String>();
						list.add(splited[5+i]);
						group.add(list);
					}				
				}
			}
		}
		for(int i= 0; i< exams.length; i++){
			writers.get(i).close();
		}
		inFact.close();
	}

	private void outputLineExams(String id, HashMap<Integer, BufferedWriter> writers, List<List<String>> group, int steps) throws IOException {
		int size = writers.keySet().size();
		for(int i=0; i < size ; i++){
			List<String> list = group.get(i);
			int length = list.size();
			String entry = id +",";
			for(int j= length-steps ; j<length-1; j++){
				entry += list.get(j)+ ",";
			}
			entry += list.get(length-1)+ "\n";
			writers.get(i).write(entry);
		}
	}

	private void countTimePoints(String string) throws IOException {
		BufferedReader inFact = new BufferedReader(new FileReader(out + string));
		HashMap<Integer,Integer> _counts = new HashMap<Integer,Integer>();
		String line = inFact.readLine();
		String id= null;
		int count = 0 ;
		while((line = inFact.readLine()) != null){
			String[] splited = line.split(",");
			if(id == null){
				count = 1;
				id = splited[0];
				continue;
			}else {
				if(splited[0].equals(id)){
					count++;
				}else{
					if(_counts.get(count) != null){
						int c = _counts.get(count);
						_counts.put(count, ++c);
					}else{
						_counts.put(count,1);
					}
					count = 1;
					id = splited[0];
				}
			}
		}
		inFact.close();
		double total = 0.0;
		for(Integer i:_counts.keySet() ){
			total += _counts.get(i);
		}
		System.out.println("Total - \t" + total);
		double prev = 0;
		DecimalFormat df = new DecimalFormat("#.#####");
		for(Integer i:_counts.keySet() ){
			int j = _counts.get(i);
			double perc = (j*100)/total;
			prev += perc;
			System.out.println("Count "+ i + " - "+ j+"\t---> "+ df.format(perc) + " %\t| " + df.format(prev) +" %");
		}
		System.out.println("-----------------------------");
	}

	private void readExams() throws IOException{
		BufferedReader inFact = new BufferedReader(new FileReader(andreia+
				"DimExam.table"));
		String line = inFact.readLine();
		headers.put("Exams", line);
		String[] split;
		while((line = inFact.readLine()) != null){
			split = line.split("\t",-1);
			exams.put(split[0], line);
		}
		inFact.close();
	}

	private void readBiopsy() throws IOException{
		BufferedReader inFact = new BufferedReader(new FileReader(andreia+
				"DimBiopsy.table"));
		String line = inFact.readLine();
		headers.put("Biopsy", line);
		String[] split;
		while((line = inFact.readLine()) != null){
			split = line.split("\t",-1);
			biopsys.put(split[0], line);
		}
		inFact.close();
	}

	private void readPatients() throws IOException{
		BufferedReader inFact = new BufferedReader(new FileReader(andreia+
				"DimPatient.table"));
		String line = inFact.readLine();
		headers.put("Patients", line);
		String[] split;
		while((line = inFact.readLine()) != null){
			split = line.split("\t",-1);
			patients.put(split[0], line);
		}
		inFact.close();
	}

	private void createDiagnostic() throws IOException {
		BufferedReader inFact = new BufferedReader(new FileReader(andreia+
				"FactHepatitis2.table"));
		BufferedWriter table = new BufferedWriter(new FileWriter(out + "Diagnosis.csv"));
		String line= inFact.readLine();
		String header = "";
		String[] split = headers.get("Patients").split("\t",-1);
		for(int i=0; i< split.length; i++){
			header += split[i] + ",";
		}
		//		header += line.split("\t",-1)[4]+ ","; // "Date,";
		String[] examsi = {"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL","ALB","CHE","T-CHO","TP"};
		for(int i=0; i< examsi.length; i++){
			header += examsi[i] + ",";
		}
		split = headers.get("Biopsy").split("\t",-1);
		header += split[1] + ",";
		header += split[3] + ",";
		header += split[2];

		table.write(header+ "\n");
		String date = null;
		String id = null;
		int att = 19;
		String[] entry = new String[att];
		while((line = inFact.readLine()) != null){
			split = line.split("\t",-1);
			if(date == null){
				date = split[4];
				id = split[1];
				entry = new String[att];
				String patient_all = patients.get(split[1]);
				String exam_all = exams.get(split[2]);
				String biopsy_all = biopsys.get(split[3]);
				String[] patient = patient_all.split("\t",-1);
				String[] exam = exam_all.split("\t",-1);
				String[] biopsy = biopsy_all.split("\t",-1);
				for(int i=0; i<patient.length; i++){
					entry[i] = patient[i];
				}
				//				entry[patient.length] = split[4]; // DATE
				System.out.println(biopsy_all);
				entry[att-3]= biopsy[1];
				if(biopsy[3].equals("")){
					entry[att-2]= biopsy[3];
				}else{
					entry[att-2]= "A"+biopsy[3];
				}
				entry[att-1]=/* biopsy[1]*/ "F"+biopsy[2];//Concat type with fibrose
				String e = exam[1].split("_")[0];
				String c = exam[1].split("_")[1];
				if(examsIndexes.get(e) != null){
					entry[examsIndexes.get(e)] = c;
				}
			}else {
				if(split[4].equals(date) && split[1].equals(id)){
					String exam_all = exams.get(split[2]);
					String[] exam = exam_all.split("\t",-1);
					String e = exam[1].split("_")[0];
					String c = exam[1].split("_")[1];
					if(examsIndexes.get(e) != null){
						entry[examsIndexes.get(e)] = c;
					}
				}else{
					String tmp = "";
					for(int i=0;i<entry.length-1;i++){
						if(entry[i] == null){
							tmp += ",";
						}else{
							tmp += entry[i]+ ",";
						}
					}
					tmp += entry[entry.length-1] +"\n";
					table.write(tmp);
					date = split[4];
					id = split[1];
					entry = new String[att];
					String patient_all = patients.get(split[1]);
					String exam_all = exams.get(split[2]);
					String biopsy_all = biopsys.get(split[3]);
					String[] patient = patient_all.split("\t",-1);
					String[] exam = exam_all.split("\t",-1);
					String[] biopsy = biopsy_all.split("\t",-1);
					for(int i=0; i<patient.length; i++){
						entry[i] = patient[i];
					}
					//					entry[patient.length] = split[4];// DATE
					System.out.println(biopsy_all);
					entry[att-3]= biopsy[1];
					if(biopsy[3].equals("")){
						entry[att-2]= biopsy[3];
					}else{
						entry[att-2]= "A"+biopsy[3];
					}
					entry[att-1]= /* biopsy[1]*/ "F"+biopsy[2];//Concat type with fibrose
					String e = exam[1].split("_")[0];
					String c = exam[1].split("_")[1];
					if(examsIndexes.get(e) != null){
						entry[examsIndexes.get(e)] = c;
					}
				}
			}			
		}
		table.close();
		inFact.close();
	}

}
