package createData.PredictionChallenge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class TableGenerator {

	private String data = "C:\\PredictionChallenge\\";
	private String diabetes = "diabetes\\";
	//private String read = data + "trainingSet\\";
	//private String read = data + diabetes + "trainingSet\\";
	private String write = data + "data\\";
	private String read = write;
	private HashMap<String,HashSet<String>> patients = new HashMap<String, HashSet<String>>();
	private HashMap<String,HashSet<String>> patientSteps = new HashMap<String, HashSet<String>>();
	private HashMap<String,Boolean> delete = new HashMap<String, Boolean>();
	private HashMap<String, String> headers = new HashMap<String, String>();

	public static void main(String[] args) {
		TableGenerator table = new TableGenerator();
		try {
			table.countSteps();
			//table.countDiagnoses();
			table.printCounts();
			//table.countGapYears();
			table.countStepsDisease(290,null);//250 diabetes, 410-414 Heart disease, 290 alzheimers
			table.printCountsPerDisease();
			//table.RemoveSmallCounts(2);
			//table.createDiagnosis();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void countStepsDisease(Integer start, Integer finish) throws IOException {
		HashMap<String,String> diag = new HashMap<String, String>();
		HashMap<String,String> trans = new HashMap<String, String>();
		HashMap<Integer,Integer> count = new HashMap<Integer, Integer>();
		BufferedReader inFact = new BufferedReader(new FileReader(read+
				"Diagnosis.csv"));
		String line = inFact.readLine();
		String[] split;
		while((line = inFact.readLine()) != null){
			split = line.split(",",-1);
			String id= split[0].replace("\"", "");
			diag.put(id, line);
		}
		inFact.close();
		inFact = new BufferedReader(new FileReader(read+
				"Transcript.csv"));
		line = inFact.readLine();
		while((line = inFact.readLine()) != null){
			split = line.split(",",-1);
			String id= split[0].replace("\"", "");
			trans.put(id, line);
		}
		inFact.close();
		inFact = new BufferedReader(new FileReader(read+
				"TranscriptDiagnosis.csv"));
		line = inFact.readLine();
		while((line = inFact.readLine()) != null){
			split = line.split(",",-1);
			String id= split[1].replace("\"", "");
			line = trans.get(id);
			if(line != null){
				String[] t = line.split(",",-1);
				id = split[2].replace("\"", "");
				line = diag.get(id);
				if(line != null){
					String[] d = line.split(",",-1);
					String disease = d[2]
							.replace("\"", "")
							.split("\\.",-1)[0];
					if(disease.equals("250")){
						int o = 0;
					}
					Integer i = validDisease(disease,start,finish);
					if(i != null){
						id= t[1];
						String year = t[2];
						HashSet<String> years = patientSteps.get(id);
						if(years == null){
							years = new HashSet<String>();
						}
						if(!year.equals("NULL")){
							years.add(year);
							patientSteps.put(id, years);
						}
					}
				}
			}
		}
	}

	private Integer validDisease(String disease, Integer s, Integer f) {
		int start = s.intValue();
		int i;
		try{
			i = Integer.parseInt(disease);
		}catch(NumberFormatException nfe)  
		{  
			//nfe.printStackTrace();
		    return null;  
		}
		if(f == null){
			if(i == start){
				return i;
			}
		}else{
			int finish = f.intValue();
			if(i >= start && i<= finish){
				return i;
			}
		}
		return null;
	}

	private void countGapYears() throws IOException {
		HashMap<String,String> diag = new HashMap<String, String>();
		HashMap<String,String> trans = new HashMap<String, String>();
		HashMap<Integer,Integer> count = new HashMap<Integer, Integer>();
		BufferedReader inFact = new BufferedReader(new FileReader(read+
				"Diagnosis.csv"));
		String line = inFact.readLine();
		String[] split;
		while((line = inFact.readLine()) != null){
			split = line.split(",",-1);
			String id= split[0].replace("\"", "");
			diag.put(id, line);
		}
		inFact.close();
		inFact = new BufferedReader(new FileReader(read+
				"Transcript.csv"));
		line = inFact.readLine();
		while((line = inFact.readLine()) != null){
			split = line.split(",",-1);
			String id= split[0].replace("\"", "");
			trans.put(id, line);
		}
		inFact.close();
		inFact = new BufferedReader(new FileReader(read+
				"TranscriptDiagnosis.csv"));
		line = inFact.readLine();
		int nulls = 0;
		int noStart = 0;
		int noTransc = 0;
		int twoDates = 0;
		while((line = inFact.readLine()) != null){
			split = line.split(",",-1);
			String id= split[1].replace("\"", "");
			line = trans.get(id);
			if(line != null){
				String[] t = line.split(",",-1);
				id = split[2].replace("\"", "");
				line = diag.get(id);
				if(line != null){
					String[] d = line.split(",",-1);
					// DO STUFF
					Integer visit;
					Integer start;
					if(t[2].replace("\"", "").equals("NULL")){
						noTransc++;
					}else{
						visit = Integer.parseInt(t[2].replace("\"", ""));
						if(d[4].replace("\"", "").equals("NULL")){
							noStart++;
						}else{
							start = Integer.parseInt(d[4].replace("\"", ""));
							twoDates++;
							Integer i = count.get(visit-start);
							if(i == null){
								i = 1;
							}else{
								i++;
							}
							count.put(visit-start, i);
						}
					}
				}else{
					nulls++;
				}				
			}else{
				nulls++;
			}
		}
		inFact.close();
		System.out.println("not present in both files\t" + nulls);
		System.out.println("dont have visit date\t" + noTransc);
		System.out.println("dont have start\t\t" + noStart);
		System.out.println("have two dates\t\t" + twoDates);
		System.out.println();
		for (Integer id : count.keySet()) {
			System.out.println(id+"\t\t"+ count.get(id));
		}
	}

	private void countDiagnoses() throws IOException {
		HashMap<String,Integer> counts = new HashMap<String, Integer>();
		BufferedReader inFact = new BufferedReader(new FileReader(read+
				"Diagnosis.csv"));
		String line = inFact.readLine();
		String[] split;
		int pat = 0;
		while((line = inFact.readLine()) != null){
			split = line.split(",",-1);
			String id= split[2];
			id = id.replace("\"", "");
			id = id.split("\\.",-1)[0];
			Integer years = counts.get(id);
			if(years == null){
				pat++;
				years = 1;
			}else{
				years++;
			}
			counts.put(id, years);
		}
		inFact.close();
		System.out.println("Total Diagnosis - "+ pat);
		System.out.println("\nSteps\t\tCount");
		List<String> s = new ArrayList<String>();
		s.addAll( counts.keySet());
		Collections.sort(s);
		for (String id : s) {
			System.out.println(id+"\t\t"+ counts.get(id));
		}
	}

	private void createDiagnosis() {

	}

	private void RemoveSmallCounts(int rem) throws IOException {
		toDelete(rem);

		File file = new File(read);
		String[] myFiles;
		if (file.isDirectory()) {
			myFiles = file.list();
			for (int i = 0; i < myFiles.length; i++) {
				File myFile = new File(file, myFiles[i]);
				if (!myFile.isDirectory()) {
					String name = myFile.getName();//.split("\\.")[0];
					removeOneFile(name);
				}
			}
		}		
	}

	private void removeOneFile(String name) throws IOException {
		System.out.println(name);
		BufferedReader in = new BufferedReader(new FileReader(read+
				name));
		BufferedWriter out = new BufferedWriter(new FileWriter(write+
				name.substring(13)));
		String line = in.readLine();
		headers.put(name,line);
		String[] split = line.split(",",-1);
		int j;
		boolean found = false;
		for (j = 0; j < split.length; j++) {
			if(split[j].equals("\"PatientGuid\"")){
				System.out.println("found");
				found = true;
				break;
			}
		}
		out.write(line+"\n");
		while((line = in.readLine()) != null){
			split = line.split(",",-1);
			if(found){
				String id= split[j];
				if(!delete.get(id)){
					out.write(line+"\n");
				}
			}
			else{
				out.write(line+"\n");
			}
		}	
		in.close();
		out.close();
	}

	private void toDelete(int i) {
		for(String id:patients.keySet()){
			HashSet<String> years = patients.get(id);
			int count = years.size();
			if(count>i){
				delete.put(id,false);
			}else {
				delete.put(id,true);
			}
		}
	}

	private void printCountsPerDisease() {
		HashMap<Integer,Integer> counts = new HashMap<Integer, Integer>();
		int pat = 0;
		for(String id:patientSteps.keySet()){
			pat++;
			HashSet<String> years = patientSteps.get(id);
			int count = years.size();
			Integer i = counts.get(count);
			if(i == null){
				i = 1;
			}else{
				i++;
			}
			counts.put(count, i);
		}
		System.out.println("Total Patients - "+ pat);
		System.out.println("\nSteps\t\tCount");
		for (Integer id : counts.keySet()) {
			System.out.println(id+"\t\t"+ counts.get(id));
		}
	}
	
	private void printCounts() {
		HashMap<Integer,Integer> counts = new HashMap<Integer, Integer>();
		int pat = 0;
		for(String id:patients.keySet()){
			pat++;
			HashSet<String> years = patients.get(id);
			int count = years.size();
			if(count>2){
				delete.put(id,false);
			}else {
				delete.put(id,true);
			}
			Integer i = counts.get(count);
			if(i == null){
				i = 1;
			}else{
				i++;
			}
			counts.put(count, i);
		}
		System.out.println("Total Patients - "+ pat);
		System.out.println("\nSteps\t\tCount");
		for (Integer id : counts.keySet()) {
			System.out.println(id+"\t\t"+ counts.get(id));
		}
	}

	private void countSteps() throws IOException {
		BufferedReader inFact = new BufferedReader(new FileReader(read+
				"Transcript.csv"));
		String line = inFact.readLine();
		String[] split;
		while((line = inFact.readLine()) != null){
			split = line.split(",",-1);
			String id= split[1];
			String year = split[2];
			HashSet<String> years = patients .get(id);
			if(years == null){
				years = new HashSet<String>();
			}
			if(!year.equals("NULL")){
				years.add(year);
				patients.put(id, years);
			}
		}
		inFact.close();

	}

}
