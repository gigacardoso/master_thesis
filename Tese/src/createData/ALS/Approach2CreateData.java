package createData.ALS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Approach2CreateData {

	private String approach2Output;
	private String diagnosisData;
	private int steps;

	public Approach2CreateData(String output,String diag, int steps) {
		approach2Output= output;
		diagnosisData = diag;
		this.steps = steps;
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

	public void createData(int stp) throws IOException{
		System.out.println("A2 - CreateData");
		steps = stp;
		String svc = approach2Output +  stp+ "DiagnoseData.csv";

		remove("DiagnoseData.csv",stp+"DiagnoseData.csv", approach2Output, stp);
		System.out.println("removed");
		BufferedReader inSVC = new BufferedReader(new FileReader(svc));
		BufferedWriter[] writers = new BufferedWriter[10];
		writers[0] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_SVC2_"+steps+".csv"));
		writers[1] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_SVC5_"+steps+".csv"));
		writers[2] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_SVC6_"+steps+".csv"));
		writers[3] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_SVC7_"+steps+".csv"));
		writers[4] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_Vitals2_"+steps+".csv"));
		writers[5] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_Vitals3_"+steps+".csv"));
		writers[6] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_Vitals6_"+steps+".csv"));
		writers[7] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_Vitals7_"+steps+".csv"));
		writers[8] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_Vitals8_"+steps+".csv"));
		writers[9] = new BufferedWriter(new FileWriter(approach2Output+File.separator+"approach2_Vitals9_"+steps+".csv"));

		String line1;
		line1 = inSVC.readLine();
		String[] split = line1.split(",",-1);
		int classe = 5; // with class - 4 without- 5
		String[] exams = new String[split.length-classe];
		for(int i= 0; i< exams.length; i++){
			exams[i] = split[4+i];
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
						att += split[4+j] + ",";
					}
				}
				split = lines1.get(((lines1.size()-1))).split(",",-1);
				for(int i=0 ; i < writers.length; i++){
					if(!split[i].isEmpty()){
						String a = att + split[4+i] + "\n";
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

	public static void main(String[] args) {

	}

}
