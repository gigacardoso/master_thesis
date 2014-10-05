package tdm.classification.createData.ALSNEW;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Approach2CreateData {

	private String approach2Output;
	private String diagnosisData;
	private int steps;
	private String[] exams;

	public Approach2CreateData(String output,String diag, int steps) {
		approach2Output= output;
		diagnosisData = diag;
		this.steps = steps;
	}

	public Approach2CreateData(String approach1Output, String diagnosisOutput,
			int steps2, String[] exams) {
		approach2Output= approach1Output;
		diagnosisData = diagnosisOutput;
		this.steps = steps2;
		this.exams = exams;
	}

	private void remove(String pathToData, String string,String pathh, int countToRemove) throws FileNotFoundException, IOException {
		System.out.println("remove");
		BufferedReader in = new BufferedReader(new FileReader(diagnosisData+File.separator+pathToData));
		BufferedWriter out = new BufferedWriter(new FileWriter(pathh+File.separator+string));
		String line;
		int last = 0;
		int count = 0;
		String head = in.readLine();
		out.write(head);
		ArrayList<String> lines = new ArrayList<String>();
		while ((line = in.readLine()) != null) {
			String[] splited = line.split(",");
			if(last == 0){
				count++;
				last = Integer.parseInt(splited[0]);
				lines.add(line);
				continue;
			}
			if(last == Integer.parseInt(splited[0])){
				lines.add(line);
				count++;
			}else{
				if(count >= countToRemove){
					for(String s:lines){
						out.write('\n'+s);
					}
				}
				count = 1;
				last = Integer.parseInt(splited[0]);
				lines = new ArrayList<String>();
				lines.add(line);
			}
		}
		in.close();
		out.close();
	}

	public void createData(int steps2) throws IOException{
		System.out.println("A2 - CreateData");
		int stp = steps;
		String svc = approach2Output +  stp+ "DiagnoseData.csv";

		remove("DiagnoseData.csv",stp+"DiagnoseData.csv", approach2Output, stp);

		BufferedReader inSVC = new BufferedReader(new FileReader(svc));
		BufferedWriter[] writers = new BufferedWriter[13];
		writers[0] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_Demo1_"+steps+".csv"));
		writers[1] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_Demo2_"+steps+".csv"));
		writers[2] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_Demo3_"+steps+".csv"));
		writers[3] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_SVC2_"+steps+".csv"));
		writers[4] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_SVC5_"+steps+".csv"));
		writers[5] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_SVC6_"+steps+".csv"));
		writers[6] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_SVC7_"+steps+".csv"));
		writers[7] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_Vitals2_"+steps+".csv"));
		writers[8] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_Vitals3_"+steps+".csv"));
		writers[9] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_Vitals6_"+steps+".csv"));
		writers[10] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_Vitals7_"+steps+".csv"));
		writers[11] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_Vitals8_"+steps+".csv"));
		writers[12] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_Vitals9_"+steps+".csv"));

		String line1;
		line1 = inSVC.readLine();
		String[] split = line1.split(",",-1);
		int classe = 2; // with class - 1 without- 2
		String[] exams = new String[split.length-classe];
		for(int i= 0; i< exams.length; i++){
			exams[i] = split[1+i];
		}
		String att = split[0]+ ",";
		//		patient += ",";
		for(int j=0; j< stp-1; j++){
			for(int i=0 ; i < exams.length; i++){
				att += exams[i]+"_"+j + ",";
			}
		}
		for(int i=0 ; i < writers.length; i++){
			String a = att + exams[i]+"_"+(stp-1) + "\n";
			writers[i].write(a);
		}

		//just the class of n+1
		int last = 0;
		ArrayList<String> lines1 = new ArrayList<String>();
		while (((line1 = inSVC.readLine()) != null)) {
			String[] splited = line1.split(",",-1);
			if(last == 0){
				last = Integer.parseInt(splited[0]);
				lines1.add(line1);
				continue;
			}
			if(last == Integer.parseInt(splited[0])){
				lines1.add(line1);
			}else{
				att = last+ ",";
				for(int i=0 ; i < steps-1; i++){
					split = lines1.get(((lines1.size()-steps)+i)).split(",",-1);
					for(int j=0; j<exams.length; j++){
						att += split[1+j] + ",";
					}
				}
				split = lines1.get(((lines1.size()-1))).split(",",-1);
				for(int i=0 ; i < writers.length; i++){
					if(!split[i].isEmpty()){
						String a = att + split[1+i] + "\n";
						writers[i].write(a);
					}
				}
				lines1 = new ArrayList<String>();
				last = Integer.parseInt(splited[0]);
				lines1.add(line1);
			}
		}
		inSVC.close();
		for(int i= 0; i<writers.length; i++){
			writers[i].close();
		}
	}

	public void createApproach2PredictionData() throws IOException {
		System.out.println("Create Approach 2");
		HashMap<String,Integer> indixes = new HashMap<String, Integer>();
		BufferedReader inFact = new BufferedReader(new FileReader(diagnosisData + "DiagnoseData.csv"));
		HashMap<Integer,BufferedWriter> writers = new HashMap<Integer,BufferedWriter>();
		String line = inFact.readLine();
		String[] splited = line.split(",");
		for(int i= 0; i< exams.length; i++){
			BufferedWriter table = new BufferedWriter(new FileWriter(approach2Output + "approach2_" +exams[i]+"_"+steps+".csv"));
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
			int steps) throws IOException {
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

}
