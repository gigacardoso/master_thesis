package createData.Power;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class NewClassGenerator {

	public  String path = "C:" + File.separator + "Power" + File.separator;
	HashMap<String,Double> MINS = new HashMap<String, Double>();
	HashMap<String,Double> MAXS = new HashMap<String, Double>();
	public static final int num_buckets = 20;
	public static final int steps = 100; //1435 (max)
	public static final int num_classes = 10;
	public static final Integer days = null;
	
	
	public static void main(String[] args) {
		try {
			System.out.println("Generate");
			System.out.println("Steps\t- " + steps);
			System.out.println("Num Buckets\t- " + num_buckets);
			System.out.println("Num Classes\t- " + num_classes);
			System.out.println("Days\t- " + days);
			
			NewClassGenerator t = new NewClassGenerator();
			t.createSum(days); //max - 1431 //700 - 25% impr J48 // 1400 - 35% impr
			t.create30min();
//			t.addSeason();
			t.addSimplePrice();
			t.addBiPrice();
			t.addTriPrice();
			//t.stats(60);
			t.createDailyData();
			t.addClass();
			t.countClass();
//			t.createDiagnosisDataWithClass(num_classes);
//			
//			t.createDiscretDiagnostic(num_buckets);
//			
//			t.createDiagnosisReal(steps);
//			t.countTimePoints("diagnosis_discret.csv");
//			t.createApproach1PredictionData(steps);
//			t.createApproach2PredictionData(steps);
//			t.createBaselineSingleOb(steps);
//			t.createBaselineMultipleOb(steps);
//			t.convertToArff();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	private void countClass() throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path + "power_class.csv" ));
		String line = in.readLine();
		String[] split;
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		int total = 0;
		while((line = in.readLine()) != null){
			split = line.split(",",-1);
			String classe = split[split.length-1];
			Integer c = counts.get(classe);
			if(c == null){
				c = 1;
			}else{
				c++;
			}
			counts.put(classe, c);
			total++;
		}
		in.close();
		System.out.println("Stats");
		System.out.println("Total \t- "+ total);
		for (String string : counts.keySet()) {
			System.out.println(string + "\t- "+ counts.get(string));			
		}
	}


	private void addClass() throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path + "power_daily.csv" ));
		BufferedWriter out = new BufferedWriter(new FileWriter(path+"power_class.csv"));
		String line = in.readLine();
		out.write(line+",RecommendedRate\n");
		String[] split;
		String classe = ""; 
		while((line = in.readLine()) != null){
			split = line.split(",",-1);
			double simple = Double.parseDouble(split[split.length -3 ]);
			double bi = Double.parseDouble(split[split.length -2 ]);
			double tri = Double.parseDouble(split[split.length -1 ]);
			if(simple < bi && simple < tri){
				classe = "Simple";
			}
			if(bi <= simple && bi < tri){
				classe = "Bi";
			}
			if(tri <= bi && tri <= simple){
				classe = "Tri";
			}
			out.write(line + "," + classe + "\n");
		}
		in.close();
		out.close();
	}


	private void createDailyData() throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path + "power_tri.csv" ));
		BufferedWriter out = new BufferedWriter(new FileWriter(path+"power_daily.csv"));
		String line = in.readLine();
		String[] split = line.split(",",-1);
		String output = split[0] + ",";
		for (int i = 2; i < split.length; i++) {
			output += split[i] + ",";
		}
		output = output.substring(0,output.length()-1);
		out.write(output + "\n");
		ArrayList<Double> sums = new ArrayList<Double>();
		for (int i = 2; i < split.length; i++) {
			sums.add(0.0);
		}
		String day = "";
		int sumed  = 0;
		boolean first = true;
		while((line = in.readLine()) != null){
			split = line.split(",",-1);
			if(first){
				day = split[0];
				first = false;
			}
			if(day.equals(split[0])){
				for (int i = 2; i < split.length; i++) {
					double d = sums.get(i-2);
					d = d + Double.parseDouble(split[i]);
					sums.set(i-2,d);
					sumed++;
				}
			}else{
				String averages = "";
				for (int i = 0; i < sums.size()-6; i++) {
					averages += (sums.get(i)/sumed) +",";
				}
				for (int i = sums.size()-6; i < sums.size(); i++) {
					averages += (sums.get(i)) +",";
				}
				averages = averages.substring(0,averages.length()-1);
				output = day + ","+ averages;
				out.write(output + "\n");
				
				day = split[0];
				sums = new ArrayList<Double>();
				for (int i = 2; i < split.length; i++) {
					sums.add(0.0);
				}
				sumed = 0;
				for (int i = 2; i < split.length; i++) {
					double d = sums.get(i-2);
					d = d + Double.parseDouble(split[i]);
					sums.set(i-2,d);
					sumed++;
				}
			}
			
		}
		in.close();
		out.close();
	}


	private void addSimplePrice() throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path + "power_30.csv" ));
		BufferedWriter out = new BufferedWriter(new FileWriter(path+"power_simple.csv"));
		Double price = 0.0764;
		String line = in.readLine();
		String[] split = line.split(",",-1);
		String output = line + ",simple";
		out.write(output + "\n");
		Integer[] indexes = {6,7,8};
		while((line = in.readLine()) != null){
			split = line.split(",",-1);
			double calculated = 0;
			for(int i:indexes){
				calculated += Double.parseDouble(split[i]);
			}
			calculated *= price;
			output = line +","+ calculated;
			out.write(output + "\n");			
		}
		in.close();
		out.close();
	}
	
	private void addBiPrice() throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path + "power_simple.csv" ));
		BufferedWriter out = new BufferedWriter(new FileWriter(path+"power_bi.csv"));
		String line = in.readLine();
		String[] split = line.split(",",-1);
		String output = line + ",bi";
		out.write(output + "\n");
		Integer[] indexes = {6,7,8};
		while((line = in.readLine()) != null){
			split = line.split(",",-1);
			double calculated = 0;
			for(int i: indexes){
				calculated += Double.parseDouble(split[i]);
			}
			calculated *= getBiPrice(split[1]);
			output = line +","+ calculated;
			out.write(output + "\n");			
		}
		in.close();
		out.close();
	}
	
	private double getBiPrice(String time) {
		double normal = 0.08925;
		double econ = 0.0473;
		//double super_econ = ;
		int hour = Integer.parseInt(time.split(":")[0]);
		if(hour >= 8 && hour <= 22){
			return normal;
		}else{
			return econ;
		}
	}


	private void addTriPrice() throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path + "power_bi.csv" ));
		BufferedWriter out = new BufferedWriter(new FileWriter(path+"power_tri.csv"));
		String line = in.readLine();
		String[] split = line.split(",",-1);
		String output = line + ",tri";
		out.write(output + "\n");
		Integer[] indexes = {6,7,8};
		while((line = in.readLine()) != null){
			split = line.split(",",-1);
			double calculated = 0;
			for(int i: indexes){
				calculated += Double.parseDouble(split[i]);
			}
			calculated *= getTriPrice(split[0],split[1]);
			output = line +","+ calculated;
			out.write(output + "\n");			
		}
		in.close();
		out.close();
	}

	private double getTriPrice(String datetime, String time) {
		
		String[] date = (datetime).split("/");
		int day = Integer.parseInt(date[0]);
		int month = Integer.parseInt(date[1]);
		if(month > 6 && month < 12){
			return getSummerTriPrice(time); 
		}else{
			if((month == 5 && day > 20)|| (month == 12 && day < 22)){
				return getSummerTriPrice(time); 
			}else{
				return getWinterTriPrice(time); 
			}
		}
	}

	private double getSummerTriPrice(String time) {
		double normal = 0.14425;
		double econ = 0.08065;
		double super_econ = 0.0473;
		int hour = Integer.parseInt(time.split(":")[0]);
		int min = Integer.parseInt(time.split(":")[1]);
		if(hour < 8 || hour >= 22){
			return super_econ;
		}
		if((hour>10 && hour < 13) || (hour == 10 && min == 30)){
			return normal;	
		}
		if((hour>19 && hour < 21) || (hour == 19 && min == 30)){
			return normal;	
		}
		return econ;
	}
	private double getWinterTriPrice(String time) {
		double normal = 0.14425;
		double econ = 0.08065;
		double super_econ = 0.0473;
		int hour = Integer.parseInt(time.split(":")[0]);
		int min = Integer.parseInt(time.split(":")[1]);
		if(hour < 8 || hour >= 22){
			return super_econ;
		}
		if((hour >= 9 && hour <= 10) || (hour == 10 && min == 30)){
			return normal;	
		}
		if((hour >= 18 && hour <= 20) || (hour == 20 && min == 30)){
			return normal;	
		}
		return econ;
	}
	
	private void create30min() throws NumberFormatException, IOException {
		BufferedReader in = new BufferedReader(new FileReader(path + "power.csv" ));
		BufferedWriter out = new BufferedWriter(new FileWriter(path+"power_30.csv"));
		String line = in.readLine();
		String[] split = line.split(",",-1);
		String output = line.substring(0,line.length()-4);
		out.write(output + "\n");
		ArrayList<Double> sums = new ArrayList<Double>();
		for (int i = 2; i < split.length; i++) {
			sums.add(0.0);
		}
		String day = "";
		String timeday = "";
		int sumed  = 0;
		boolean first = true;
		while((line = in.readLine()) != null){
			split = line.split(",",-1);
			if(first){
				day = split[0];
				String[] time = (split[1]).split(":");
				int min = Integer.parseInt(time[1]);
				if(min > 30){
					min = 30;
				}else{
					min = 00;
				}
				timeday = time[0]+":"+min+":00";
				first = false;
			}
			String[] time = (split[1]).split(":");
			int min = Integer.parseInt(time[1]);
			if(min == 0 || min == 30){
				String averages = "";
				for (int i = 0; i < sums.size()-4; i++) {
					averages += (sums.get(i)/sumed) +",";
				}
				for (int i = sums.size()-4; i < sums.size()-1; i++) {
					averages += (sums.get(i)) +",";
				}
				averages = averages.substring(0,averages.length()-1);
				output = day + "," + timeday + "," + averages;
				out.write(output + "\n");
				
				day = split[0];
				timeday = split[1];
				sums = new ArrayList<Double>();
				for (int i = 2; i < split.length; i++) {
					sums.add(0.0);
				}
				if(!split[2].equals("?")){
					for (int i = 2; i < split.length; i++) {
						double d = sums.get(i-2);
						d = d + Double.parseDouble(split[i]);
						sums.set(i-2,d);
						sumed = 1;
					}
				}else{
					sumed = 0;
				}
			}else{
				if(!split[2].equals("?")){
					for (int i = 2; i < split.length; i++) {
						double d = sums.get(i-2);
						d = d + Double.parseDouble(split[i]);
						sums.set(i-2,d);
						sumed++;
					}
				}
			}
			
		}
		in.close();
		out.close();
	}

//	private void addSeason() throws IOException {
//		System.out.println("addSeason");
//		// começa Verao 21 Junho
//		// acaba  Verao 21 Dezembro
//		// 16/12/2006
//		BufferedReader in = new BufferedReader(new FileReader(path + "power.csv" ));
//		BufferedWriter out = new BufferedWriter(new FileWriter(path+"power_seasons.csv"));
//		String line = in.readLine();
//		String[] split;
//		String output = line + ",Season";
//		out.write(output + "\n");
//		while((line = in.readLine()) != null){
//			split = line.split(",",-1);
//			output = line;
//			String season = "";
//			String[] date = (split[0]).split("/");
//			int day = Integer.parseInt(date[0]);
//			int month = Integer.parseInt(date[1]);
//			if(month > 6 && month < 12){
//				season = "Summer"; 
//			}else{
//				if((month == 5 && day > 20)|| (month == 12 && day < 22)){
//					season = "summer";
//				}else{
//					season = "winter";
//				}
//			}
//			out.write(output + ","+season+"\n");
//		}
//		in.close();
//		out.close();
//	}

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
		header += ",Sum";
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
			for(int i =split.length-4; i< split.length; i++){
				sum += Float.parseFloat(split[i]);
			}
			line += "," + sum;
			out.write(line + "\n");
			
		}
		in.close();
		out.close();
	}

}
