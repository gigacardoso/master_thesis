package createData.ALS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CreateVitals7 {

	private static String path = "C:\\PROACT_2013_08_27_ALL_FORMS\\FormatedData\\Approach1\\";
	private static String outpath = "C:\\ALSHMM\\new\\";
	private static String[] exams = {"Demo1","Demo2","Demo3","SVC2","SVC5","SVC6","SVC7","Vitals2","Vitals3","Vitals6","Vitals7","Vitals8","Vitals9"};
	
	public static void main(String[] args) {
		CreateVitals7 c = new CreateVitals7();
		int steps = 6;
		try {
			c.create(steps, "Vitals7");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void create(int steps, String exam) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader in = new BufferedReader(new FileReader(path + "DiagnoseData.csv"));
		BufferedWriter out = new BufferedWriter(new FileWriter(outpath + "approach1_"+exam+"_"+steps+".csv"));
		ArrayList<String> lines = new ArrayList<String>();
		
		int index = 0;
		for (int i = 0; i < exams.length; i++) {
			if(exam.equals(exams[i])){
				index = i+1;
				break;
			}
		}

		String line = in.readLine();
		String[] split = line.split(",",-1);
		String output = split[0]+ ",";
		for (int i = 0; i < steps; i++) {
			output += split[index] + "_" + i+ ",";
		}
		output = output.substring(0,output.length()-1);
		out.write(output + "\n");
		String last = null;
		while((line = in.readLine()) != null){
			split = line.split(",",-1);
			if(last == null){
				last = split[0];
				lines.add(split[index]);
			}else{
				if(last.equals(split[0])){
					lines.add(split[index]);
				}else{
					if(lines.size() >= steps){
						output = last + ",";
						int count = 0;
						for (int i = (lines.size() - steps); i < lines.size(); i++) {
							output += lines.get(i) + ",";
							if(lines.get(i).equals("missing")){
								count++;
							}
						}
						output = output.substring(0,output.length()-1);
						if(count < steps){
							out.write(output + "\n");
						}
					}
					last = split[0];
					lines = new ArrayList<String>();
					lines.add(split[index]);
				}
			}
		}
		if(lines.size() >= steps){
			output = last + ",";
			int count = 0;
			for (int i = (lines.size() - steps); i < lines.size(); i++) {
				output += lines.get(i) + ",";
				if(lines.get(i).equals("missing")){
					count++;
				}
			}
			output = output.substring(0,output.length()-1);
			if(count < steps){
				out.write(output + "\n");
			}
		}
		in.close();
		out.close();
	}

}
