package createData.Power;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import createData.ALS.CreateData;

public class TableGeneratorDiscretEDP {

	public  String path = "C:" + File.separator + "Power" + File.separator;
	HashMap<String,Double> MINS = new HashMap<String, Double>();
	HashMap<String,Double> MAXS = new HashMap<String, Double>();
	public static final int num_buckets = 10;
	public static final int steps = 7; //1435 (max) // EDP Max - 7
	public static final Integer days = null;
	
	public static void main(String[] args) {
		try {
			System.out.println("Generate");
			System.out.println("Steps\t- " + steps);
			System.out.println("Num Buckets\t- " + num_buckets);
			System.out.println("Days\t- " + days);
			
			NewClassGenerator n =  new NewClassGenerator();
			n.generateEDPClassData(days,"diagnosis_class.csv");
			
			TableGeneratorDiscretEDP t = new TableGeneratorDiscretEDP();
			t.createDiscretDiagnostic(num_buckets);
			
			t.createDiagnosisReal(steps);
//			t.countTimePoints("diagnosis_discret.csv");
			t.createApproach1PredictionData(steps);
			t.createApproach2PredictionData(steps);
			t.createBaselineSingleOb(steps);
			t.createBaselineMultipleOb(steps);
			t.convertToArff();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void createDiscretDiagnostic(int num_buckets) throws IOException {
		System.out.println("createDiscretDiagnostic");
		
		findMinMax();
		
		BufferedReader in = new BufferedReader(new FileReader(path + "diagnosis_class.csv" ));
		BufferedWriter out = new BufferedWriter(new FileWriter(path + "diagnosis_discret.csv"));
		String tmp = in.readLine();
		String[] header = tmp.split(",",-1);
		out.write(tmp+ "\n");
		
		String line, output;
		String[] split;
		while((line = in.readLine()) != null){
			split = line.split(",",-1);
			output = split[0] + ",";
			for (int i = 1; i < split.length-1; i++) {
				Double d = Double.parseDouble(split[i]);
				String discretized = discretizeVariable(header[i], d, num_buckets);
				output += discretized + ",";
			}
			output += split[split.length-1];
			out.write(output + "\n");
		}
		in.close();
		out.close();
	}

	private String discretizeVariable(String variable, Double d, Integer num_buckets) {
//		System.out.println("\tdiscretizeVariable");
		Double min = MINS.get(variable);
		Double max = MAXS.get(variable);
		double bucket_size = (max-min)/num_buckets;
		double g = (d-min)/bucket_size;
		int bucket = (int) (g);
		if(bucket == num_buckets){
			bucket--;
		}
		return "["+variable+"_"+bucket+"]";
	}

	private void findMinMax() throws IOException {
		System.out.println("\t findMinMax");
		BufferedReader in = new BufferedReader(new FileReader(path + "diagnosis_class.csv" ));
		String[] header = in.readLine().split(",",-1);
		for (int i = 1; i < header.length-1; i++) {
			String var = header[i];
			MINS.put(var, Double.MAX_VALUE);
			MAXS.put(var, Double.MIN_VALUE);
		}
		
		String line;
		String[] split;
		while((line = in.readLine()) != null){
			split = line.split(",",-1);
			for (int i = 1; i < split.length-1; i++) {
				Double d = Double.parseDouble(split[i]);
				if(d > MAXS.get(header[i])){
					MAXS.put(header[i], d);
				}
				if(d < MINS.get(header[i])){
					MINS.put(header[i], d);
				}
			}
		}
		in.close();
//		for (int i = 1; i < header.length-1; i++) {
//			String string = header[i];
//			System.out.println(string);
//			System.out.println("min - " + MINS.get(string));
//			System.out.println("max - " + MAXS.get(string));
//		}
	}

	private void convertToArff() {
		System.out.println("convertToArff");
		File file = new File(path);
		CreateData create =  new CreateData();
		String[] myFiles;
		if (file.isDirectory()) {
			myFiles = file.list();
			for (int i = 0; i < myFiles.length; i++) {
				File myFile = new File(file, myFiles[i]);
				if (!myFile.isDirectory() && myFile.getName().split("\\.")[1].equals("csv")) {
					create.CSV2arff(path, myFile.getName().split("\\.")[0]);
					try{
						myFile.delete();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void createBaselineMultipleOb(int steps) throws IOException {
		System.out.println("createBaselineMultipleOb");
		BufferedReader inFact = new BufferedReader(new FileReader(path + "diagnosis_discret.csv"));

		String line = inFact.readLine();
		String[] splited = line.split(",", -1);
		String headWithout = "";
		for(int j= 0;j<steps-1;j++){
			for(int i= 0;i<splited.length;i++){
				if(i<splited.length-1){
					headWithout += splited[i]+"_"+j+",";
				}
			}
		}
		headWithout += splited[splited.length-1]+"_class\n";
		BufferedWriter without = new BufferedWriter(new FileWriter(path + "baselineMultiWithout.csv"));
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
//						System.out.println(id);
						if(id.equals("77")){
							@SuppressWarnings("unused")
							int j= 0;
						}
						outputLineMultiBaseline(id,without,lines,steps);
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

	private void outputLineMultiBaseline(String id,
			BufferedWriter without, List<String> lines, int steps) throws IOException {
//		System.out.println("\t outputLineMultiBaseline");
		int size = lines.size();

		String entryWithoutClass = "";
		for(int i =0 ; i< steps-1; i++){
			String line = lines.get(size-steps+i);
			String[] split = line.split(",",-1);
			for(int j= 0; j<split.length; j++){
				if(j < split.length-1){
					entryWithoutClass += split[j]+ ",";
				}
			}
		}
		String[] last = lines.get(size-1).split(",",-1);
		entryWithoutClass += last[last.length-1]+ "\n";
		without.write(entryWithoutClass);
	}
	
	private void createBaselineSingleOb(int steps) throws IOException {
		BufferedReader inFact = new BufferedReader(new FileReader(path + "diagnosis_discret.csv"));
		System.out.println("createBaselineSingleOb");
		HashMap<Integer,BufferedWriter> without_all = new HashMap<Integer,BufferedWriter>();
		String line = inFact.readLine();
		String[] splited = line.split(",", -1);
		String headWithout = "";
		for(int i= 0;i<splited.length;i++){
			if(i<splited.length-1){
				headWithout += splited[i]+",";
			}
		}
		headWithout += splited[splited.length-1]+"_class\n";
		for(int i= 0;i<steps-1;i++){
			BufferedWriter without = new BufferedWriter(new FileWriter(path + "baselineSingleWithout_"+i+"_.csv"));
			without.write(headWithout);
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
//						System.out.println(id);
						if(id.equals("77")){
							@SuppressWarnings("unused")
							int j= 0;
						}
						for(int i= 0;i<steps-1;i++){
							BufferedWriter without = without_all.get(i);
							outputLineSingleBaseline(id,without,lines,i,steps);
						}
					}
					id = splited[0];
					lines = new ArrayList<String>();
					lines.add(line);
				}				
			}
		}
		for(int i= 0;i<steps-1;i++){
			without_all.get(i).close();
		}
		inFact.close();
	}

	private void outputLineSingleBaseline(String id, BufferedWriter without,
			List<String> lines, int i, int steps) throws IOException {
//		System.out.println("\t outputLineSingleBaseline");
		int size = lines.size();
		String line = lines.get(size-steps+i);
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
	
	private void createApproach2PredictionData(int steps) throws IOException {
		System.out.println("createApproach2PredictionData");
		String[] exams = getVariables();
		HashMap<String,Integer> indixes = new HashMap<String, Integer>();
		BufferedReader inFact = new BufferedReader(new FileReader(path + "diagnosis_discret.csv"));
		HashMap<Integer,BufferedWriter> writers = new HashMap<Integer,BufferedWriter>();
		String line = inFact.readLine();
		String[] splited = line.split(",");
		for(int i= 0; i< exams.length; i++){
			BufferedWriter table = new BufferedWriter(new FileWriter(path + exams[i]+"_2.csv"));
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
					list.add(splited[1+i]);
					group.add(list);
				}				
				continue;
			}else {
				if(splited[0].equals(id)){
					for(int i= 0; i<exams.length; i++){
						group.get(i).add(splited[1+i]);
					}	
				}else{
					if(	group.get(0).size() >= steps){
//						System.out.println(id);
						if(id.equals("77")){
							@SuppressWarnings("unused")
							int j= 0;
						}
						outputLineExams2(id,writers,group,steps, exams);
					}
					id = splited[0];
					group = new ArrayList<List<String>>(11);
					for(int i= 0; i<exams.length; i++){
						ArrayList<String> list = new ArrayList<String>();
						list.add(splited[1+i]);
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
			int steps, String[] exams) throws IOException {
//		System.out.println("\t outputLineExams2");
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
	
	private void createApproach1PredictionData(int steps) throws IOException {
		System.out.println("createApproach1PredictionData");
		HashMap<String,Integer> indixes = new HashMap<String, Integer>();
		BufferedReader inFact = new BufferedReader(new FileReader(path + "diagnosis_discret.csv"));
		String[] exams = getVariables();
		HashMap<Integer,BufferedWriter> writers = new HashMap<Integer,BufferedWriter>();
		String line = inFact.readLine();
		String[] splited = line.split(",");
		for(int i= 0; i< exams.length; i++){
			BufferedWriter table = new BufferedWriter(new FileWriter(path + exams[i]+".csv"));
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
					list.add(splited[1+i]);
					group.add(list);
				}				
				continue;
			}else {
				if(splited[0].equals(id)){
					for(int i= 0; i<exams.length; i++){
						group.get(i).add(splited[1+i]);
					}	
				}else{
					if(	group.get(0).size() >= steps){
//						System.out.println(id);
						outputLineExams(id,writers,group,steps);
					}
					id = splited[0];
					group = new ArrayList<List<String>>(11);
					for(int i= 0; i<exams.length; i++){
						ArrayList<String> list = new ArrayList<String>();
						list.add(splited[1+i]);
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
	
	private String[] getVariables() throws IOException {
		System.out.println("\t\t getVariables");
		BufferedReader in = new BufferedReader(new FileReader(path + "diagnosis_discret.csv"));
		String header = in.readLine();
		in.close();
		String[] split = header.split(",",-1);
		String[] result = new String[split.length-2];
		for (int i = 1; i < split.length-1; i++) {
			result[i-1] = split[i];
		}
		return result;
	}

	private void outputLineExams(String id, HashMap<Integer, BufferedWriter> writers, List<List<String>> group, int steps) throws IOException {
//		System.out.println("\t outputLineExams");
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
		System.out.println("countTimePoints");
		BufferedReader inFact = new BufferedReader(new FileReader(path + string));
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
	
	private void createDiagnosisReal(int steps) throws IOException {
		System.out.println("createDiagnosisReal");
		BufferedReader inFact = new BufferedReader(new FileReader(path + "diagnosis_discret.csv"));

		String line = inFact.readLine();
		String[] splited = line.split(",", -1);
		String headWithout = "";
		for(int i= 0;i<splited.length-1;i++){
				headWithout += splited[i]+",";
		}
		headWithout += splited[splited.length-1]+"\n";
		BufferedWriter without = new BufferedWriter(new FileWriter(path + "diagnosis_discret_real.csv"));
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
//						System.out.println(id);
						outputLineDiagnosisReal(id,without,lines);
					}else{
						throw new IllegalArgumentException("Too many Steps");
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
//		System.out.println("\t outputLineDiagnosisReal");
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

	private void createDiagnosisDataWithClass(int num_buckets) throws IOException {
		System.out.println("createDiagnosisDataWithClass");
		ArrayList<Double> b = this.createDistributionBuckets(num_buckets, 2);
		ArrayList<String> classes = new ArrayList<String>();
		for (int i = 0; i < num_buckets; i++) {
			classes.add("["+i+"]");
		}
		BufferedReader in = new BufferedReader(new FileReader(path + "power.csv" ));
		BufferedWriter out = new BufferedWriter(new FileWriter(path+"diagnosis_class.csv"));
		String line = in.readLine();
		String[] split = line.split(",",-1);
		String output = "Day,";
		for (int i = 2; i < split.length-1; i++) {
			output += split[i] + ",";
		}
		output += split[split.length-1];
		out.write(output + "\n");
		String last = null;
		int day = -1;
		while((line = in.readLine()) != null){
			split = line.split(",",-1);
			double num = Double.parseDouble(split[split.length-1]);
			
			if(!split[0].equals(last)){
				++day;
				last = split[0];
			}
			output = day + ",";
			for (int i = 2; i < split.length-1; i++) {
				output += split[i] + ",";
			}
			int clss = 0;
			for (int i = 1; i < b.size(); i++) {
				Double d = b.get(i);
				if (num < d){
					output += classes.get(clss);
					break;
				}
				clss++;
			}
			out.write(output + "\n");
		}
		in.close();
		out.close();

		//Utils u = new Utils();
		//u.CSV2arff(path, "diagnosis_discret");
	}

	private ArrayList<Double> createDistributionBuckets(int n_buckets, int error) throws IOException {
		System.out.println("\t createDistributionBuckets");
		BufferedReader in = new BufferedReader(new FileReader(path + "power.csv" ));
		//BufferedWriter out = new BufferedWriter(new FileWriter(path+"power_stats.csv"));
		int classes = 100;
		in.readLine();

		String line;
		String[] split;

		double min = 225.254;//Double.MAX_VALUE;
		double max = 414.28;//Double.MIN_VALUE;
		double tmp_bucket_size = (max-min) / classes;

		HashMap<Integer,Integer> counts = new HashMap<Integer, Integer>();
		for(int i = 0; i< classes; i++){
			counts.put(i, 0);
		}

		int total = 0;

		while((line = in.readLine()) != null){
			total++;
			split = line.split(",",-1);
			double num = Double.parseDouble(split[split.length-1]);
			num = num - min;
			int bucket = (int) (num/tmp_bucket_size);
			if(bucket == classes){
				bucket--;
			}
			int c = counts.get(bucket);
			counts.put(bucket, ++c);
		}
		in.close();

		int size = 100/n_buckets;
		int bucket_size = (int) (total*((double) size/100));
		int error_count = (int) (total*0.02);

		HashMap<Integer,Integer> bucket_counts = new HashMap<Integer, Integer>();
		for(int i = 0; i< n_buckets; i++){
			counts.put(i, 0);
		}		
		ArrayList<Double> borders = new ArrayList<Double>();
		borders.add(min);

		int b = 0, current = 0, i = 0;
		for(;i < classes;i++){
			Double max_border = (min+tmp_bucket_size*(i+1));
			current += counts.get(i);
			if(current > (bucket_size-error_count)){
				bucket_counts.put(b, current);
				current = 0;
				b++;
				borders.add(max_border);
			}			
		}
		Double max_border = (min+tmp_bucket_size*(i+1));
		if(current > 0){
			bucket_counts.put(b, current);
			current = 0;
			b++;
			borders.add(max_border);
		}

//		TODO
//		System.out.println("Total - "+ total);
//		DecimalFormat df = new DecimalFormat("#.##");
//		for(i = 0;i < n_buckets;i++){
//			System.out.println("Bucket "+ i +" ["+df.format(borders.get(i))+";"+ df.format(borders.get(i+1)) + "] - " + bucket_counts.get(i) + " = " + ((((double) bucket_counts.get(i))/total)*100)+ "%" );
//		}
		return borders;
	}

	private void createSum(Integer days) throws IOException {
		System.out.println("createSum");
		BufferedReader in = new BufferedReader(new FileReader(path + "Use\\"+"power_consumption.txt"));
		BufferedWriter out = new BufferedWriter(new FileWriter(path+"power.csv"));
		
		if(days == null || days > 1431){
			days = 1431;
		}
		if(days < 10){
			days = 10;
		}

		String header = in.readLine();
		header = header.replace(";",",");
		header += ",PowerClass";
		out.write(header + "\n");
		String line;
		String[] split;
		int count = 0;
		String last = null;
		while((line = in.readLine()) != null){
			line = line.replace(";", ",");
			split = line.split(",",-1);
			
			if(last == null){
				last = split[0];
				count++;
			}else{
				if(!split[0].equals(last)){
					last = split[0];
					count++;
				}
			}
			
			if(count > days){
				break;
			}
			
			float sum = 0;
			//			System.out.println(line);
			if(split[2].equals("?")){
				continue;
			}
			for(int i =2; i< split.length; i++){
				sum += Float.parseFloat(split[i]);
			}
			line += "," + sum;
			out.write(line + "\n");
			
		}
		in.close();
		out.close();
	}

	//Gives distribution of classes  
	private void stats(int classes) throws IOException {
		System.out.println("Stats");
		BufferedReader in = new BufferedReader(new FileReader(path + "power.csv" ));
		//BufferedWriter out = new BufferedWriter(new FileWriter(path+"power_stats.csv"));
		in.readLine();

		String line;
		String[] split;

		double min = 225.254;//Float.MAX_VALUE;
		double max = 414.28;//MIN_VALUE;
		double bucket_size = (max-min) / classes;

		HashMap<Integer,Integer> counts = new HashMap<Integer, Integer>();
		for(int i = 0; i< classes; i++){
			counts.put(i, 0);
		}

		int total = 0;

		while((line = in.readLine()) != null){
			total++;
			split = line.split(",",-1);
			double num = Double.parseDouble(split[split.length-1]);
			num = num - min;
			int bucket = (int) (num/bucket_size);
			if(bucket == classes){
				bucket--;
			}
			int c = counts.get(bucket);
			counts.put(bucket, ++c);
		}
		in.close();
		System.out.println("Total - "+ total);
		DecimalFormat df = new DecimalFormat("#.##");
		for(int i = 0;i < classes;i++){
			String min_border = df.format(min+bucket_size*i);
			String max_border =  df.format(min+bucket_size*(i+1));
			System.out.println("Bucket "+ i +" ["+min_border+";"+ max_border + "] - " + counts.get(i) + " = " + ((((double) counts.get(i))/total)*100)+ "%" );
		}
	}

}
