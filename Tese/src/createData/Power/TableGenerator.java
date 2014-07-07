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

import utils.Utils;

public class TableGenerator {

	public  String path = "C:" + File.separator + "Power" + File.separator;

	public static void main(String[] args) {
		try {
			int steps = 10;
			TableGenerator t = new TableGenerator();
			//t.createSum();
			//t.stats(60);

			//t.createDiscretDiagnosisData(5);
			t.createDiagnosisReal(steps);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void createDiagnosisReal(int steps) throws IOException {
		BufferedReader inFact = new BufferedReader(new FileReader(path + "diagnosis_discret.csv"));

		String line = inFact.readLine();
		String[] splited = line.split(",", -1);
		String headWithout = "";
		for(int i= 0;i<splited.length;i++){
			if(i<splited.length-1){
				headWithout += splited[i]+",";
			}
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
						System.out.println(id);
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

	private void createDiscretDiagnosisData(int num_buckets) throws IOException {
		ArrayList<Double> b = this.createDistributionBuckets(num_buckets, 2);
		ArrayList<String> classes = new ArrayList<String>();
		for (int i = 0; i < num_buckets; i++) {
			classes.add("["+i+"]");
		}
		BufferedReader in = new BufferedReader(new FileReader(path + "power.csv" ));
		BufferedWriter out = new BufferedWriter(new FileWriter(path+"diagnosis_discret.csv"));
		String line = in.readLine();
		String[] split = line.split(",",-1);
		String output = "Day,";
		for (int i = 2; i < split.length; i++) {
			output += split[i] + ",";
		}
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

		Utils u = new Utils();
		u.CSV2arff(path, "diagnosis_discret");
	}

	private ArrayList<Double> createDistributionBuckets(int n_buckets, int error) throws IOException {
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


		System.out.println("Total - "+ total);
		DecimalFormat df = new DecimalFormat("#.##");
		for(i = 0;i < n_buckets;i++){
			System.out.println("Bucket "+ i +" ["+df.format(borders.get(i))+";"+ df.format(borders.get(i+1)) + "] - " + bucket_counts.get(i) + " = " + ((((double) bucket_counts.get(i))/total)*100)+ "%" );
		}
		return borders;
	}

	private void createSum() throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path + "power_consumption.txt" ));
		BufferedWriter out = new BufferedWriter(new FileWriter(path+"power.csv"));

		String header = in.readLine();
		header = header.replace(";",",");
		header += ", PowerClass";
		out.write(header + "\n");
		String line;
		String[] split;
		String last = null;
		while((line = in.readLine()) != null){
			line = line.replace(";", ",");
			split = line.split(",",-1);
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
