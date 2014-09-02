package createData.ALS;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Approach1CreateDataDiscret {

	private String approach1Output;
	private int steps;
	
	public Approach1CreateDataDiscret(String output, int steps) {
		approach1Output= output;
		this.steps = steps;
	}

	public void createDataSVC(int stp) throws IOException{
		System.out.println("A1 - CreateDataSVC");
		steps = stp;
		String svc = approach1Output +  steps+ "_SVC_Data.csv";

		BufferedReader inSVC = new BufferedReader(new FileReader(svc));
		//		BufferedReader inALS = new BufferedReader(new FileReader(alsfrs));
		//		BufferedReader inVitals = new BufferedReader(new FileReader(Vitals));
		BufferedWriter outSVC2 = new BufferedWriter(new FileWriter(approach1Output+File.separator+"approach1_SVC2_"+steps+".csv"));
		BufferedWriter outSVC5 = new BufferedWriter(new FileWriter(approach1Output+File.separator+"approach1_SVC5_"+steps+".csv"));
		BufferedWriter outSVC6 = new BufferedWriter(new FileWriter(approach1Output+File.separator+"approach1_SVC6_"+steps+".csv"));
		BufferedWriter outSVC7 = new BufferedWriter(new FileWriter(approach1Output+File.separator+"approach1_SVC7_"+steps+".csv"));

		String line1;
		line1 = inSVC.readLine();
		//		line2 = inALS.readLine();
		//		line3 = inVitals.readLine();
		String[] split = line1.split(",",-1);
		String att2 = split[0]+ ",";
		String att5 = split[0]+ ",";
		String att6 = split[0]+ ",";
		String att7 = split[0]+ ",";
		//		patient += ",";
		for(int i=0 ; i < steps-1; i++){
			att2 += split[2]+"_"+i + ",";
			att5 += split[5]+"_"+i + ",";
			att6 += split[6]+"_"+i + ",";
			att7 += split[7]+"_"+i + ",";
		}
		att2 += split[2]+"_"+(steps-1);
		att5 += split[5]+"_"+(steps-1);
		att6 += split[6]+"_"+(steps-1);
		att7 += split[7]+"_"+(steps-1);
		outSVC2.write(att2+'\n');
		outSVC5.write(att5+'\n');
		outSVC6.write(att6+'\n');
		outSVC7.write(att7+'\n');
		CreateDataDiscret t = new CreateDataDiscret();

		//just the class of n+1
		int last = 0;
		ArrayList<String> lines1 = new ArrayList<String>();
		while ((line1 = inSVC.readLine()) != null) {
			String[] splited = line1.split(",",-1);
			if(last == 0){
				last = Integer.parseInt(splited[0]);
				lines1.add(line1);
				continue;
			}
			if(last == Integer.parseInt(splited[0])){
				lines1.add(line1);
			}else{
				att2 = last+ ",";
				att5 = last+ ",";
				att6 = last+ ",";
				att7 = last+ ",";

				for(int i=0 ; i < steps-1; i++){
					split = lines1.get(((lines1.size()-steps)+i)).split(",",-1);
					att2 += t.discretize(3,split[2]) + ",";
					att5 += t.discretize(4,split[5]) + ",";
					att6 += t.discretize(5,split[6])+ ",";
					att7 += t.discretize(6,split[7]) + ",";
				}
				split = lines1.get((lines1.size()-1)).split(",",-1);
				att2 += t.discretize(3,split[2]);
				att5 += t.discretize(4,split[5]);
				att6 += t.discretize(5,split[6]);
				att7 += t.discretize(6,split[7]);
				if(!split[2].isEmpty()){
					outSVC2.write(att2+'\n');
				}
				if(!split[5].isEmpty()){
					outSVC5.write(att5+'\n');
				}
				if(!split[6].isEmpty()){
					outSVC6.write(att6+'\n');
				}
				if(!split[7].isEmpty()){
					outSVC7.write(att7+'\n');
				}

				lines1 = new ArrayList<String>();
				last = Integer.parseInt(splited[0]);
				lines1.add(line1);
			}
		}
		inSVC.close();
		outSVC2.close();
		outSVC5.close();
		outSVC6.close();
		outSVC7.close();
		
		CreateDataDiscret c = new CreateDataDiscret();
		
		c.CSV2arff(approach1Output+File.separator,"approach1_SVC2_"+steps);
		c.CSV2arff(approach1Output+File.separator,"approach1_SVC5_"+steps);
		c.CSV2arff(approach1Output+File.separator,"approach1_SVC6_"+steps);
		c.CSV2arff(approach1Output+File.separator,"approach1_SVC7_"+steps);
	}

	public void createDataVitals(int stp) throws IOException{
		System.out.println("A1 - CreateDataVitals");
		steps = stp;
		String Vitals = approach1Output +  steps+ "_Vitals_Data.csv";

		//		BufferedReader inSVC = new BufferedReader(new FileReader(svc));
		//		BufferedReader inALS = new BufferedReader(new FileReader(alsfrs));
		BufferedReader inVitals = new BufferedReader(new FileReader(Vitals));
		BufferedWriter outSVC2 = new BufferedWriter(new FileWriter(approach1Output+File.separator+"approach1_Vitals2_"+steps+".csv"));
		BufferedWriter outSVC5 = new BufferedWriter(new FileWriter(approach1Output+File.separator+"approach1_Vitals3_"+steps+".csv"));
		BufferedWriter outSVC6 = new BufferedWriter(new FileWriter(approach1Output+File.separator+"approach1_Vitals6_"+steps+".csv"));
		BufferedWriter outSVC7 = new BufferedWriter(new FileWriter(approach1Output+File.separator+"approach1_Vitals7_"+steps+".csv"));
		BufferedWriter outSVC8 = new BufferedWriter(new FileWriter(approach1Output+File.separator+"approach1_Vitals8_"+steps+".csv"));
		BufferedWriter outSVC9 = new BufferedWriter(new FileWriter(approach1Output+File.separator+"approach1_Vitals9_"+steps+".csv"));

		String line1;
		line1 = inVitals.readLine();
		
		String[] split = line1.split(",",-1);
		String att2 = split[0]+ ",";
		String att3 = split[0]+ ",";
		String att6 = split[0]+ ",";
		String att7 = split[0]+ ",";
		String att8 = split[0]+ ",";
		String att9 = split[0]+ ",";
		//		patient += ",";
		for(int i=0 ; i < steps-1; i++){
			att2 += split[2]+"_"+i + ",";
			att3 += split[3]+"_"+i + ",";
			att6 += split[6]+"_"+i + ",";
			att7 += split[7]+"_"+i + ",";
			att8 += split[8]+"_"+i + ",";
			att9 += split[9]+"_"+i + ",";
		}
		att2 += split[2]+"_"+(steps-1);
		att3 += split[3]+"_"+(steps-1);
		att6 += split[6]+"_"+(steps-1);
		att7 += split[7]+"_"+(steps-1);
		att8 += split[8]+"_"+(steps-1);
		att9 += split[9]+"_"+(steps-1);
		outSVC2.write(att2+'\n');
		outSVC5.write(att3+'\n');
		outSVC6.write(att6+'\n');
		outSVC7.write(att7+'\n');
		outSVC8.write(att8+'\n');
		outSVC9.write(att9+'\n');
		
		CreateDataDiscret t = new CreateDataDiscret();
		

		int last = 0;
		ArrayList<String> lines1 = new ArrayList<String>();
		while ((line1 = inVitals.readLine()) != null) {
			String[] splited = line1.split(",",-1);
			if(last == 0){
				last = Integer.parseInt(splited[0]);
				lines1.add(line1);
				continue;
			}
			if(last == Integer.parseInt(splited[0])){
				lines1.add(line1);
			}else{
				 att2 = last+ ",";
				 att3 = last+ ",";
				 att6 = last+ ",";
				 att7 = last+ ",";
				 att8 = last+ ",";
				 att9 = last+ ",";
				for(int i=0 ; i < steps-1; i++){
					split = lines1.get(((lines1.size()-steps)+i)).split(",",-1);
					att2 += t.discretize(7,split[2]) + ",";
					att3 += t.discretize(8,split[3]) + ",";
					att6 += t.discretize(9,split[6]) + ",";
					att7 += t.discretize(10,split[7]) + ",";
					att8 += t.discretize(11,split[8]) + ",";
					att9 += t.discretize(12,split[9]) + ",";
				}
				split = lines1.get((lines1.size()-1)).split(",",-1);
				att2 += t.discretize(7,split[2]);
				att3 += t.discretize(8,split[3]);
				att6 += t.discretize(9,split[6]);
				att7 += t.discretize(10,split[7]);
				att8 += t.discretize(11,split[8]);
				att9 += t.discretize(12,split[9]);
				if(!split[2].isEmpty()){
					outSVC2.write(att2+'\n');
				}
				if(!split[3].isEmpty()){
					outSVC5.write(att3+'\n');
				}
				if(!split[6].isEmpty()){
					outSVC6.write(att6+'\n');
				}
				if(!split[7].isEmpty()){
					outSVC7.write(att7+'\n');
				}
				if(!split[8].isEmpty()){
					outSVC8.write(att8+'\n');
				}
				if(!split[9].isEmpty()){
					outSVC9.write(att9+'\n');
				}

				lines1 = new ArrayList<String>();
				last = Integer.parseInt(splited[0]);
				lines1.add(line1);
			}
		}
		inVitals.close();
		outSVC2.close();
		outSVC5.close();
		outSVC6.close();
		outSVC7.close();
		outSVC8.close();
		outSVC9.close();
		
		CreateDataDiscret c = new CreateDataDiscret();
		
		c.CSV2arff(approach1Output+File.separator,"approach1_Vitals2_"+steps);
		c.CSV2arff(approach1Output+File.separator,"approach1_Vitals3_"+steps);
		c.CSV2arff(approach1Output+File.separator,"approach1_Vitals6_"+steps);
		c.CSV2arff(approach1Output+File.separator,"approach1_Vitals7_"+steps);
		c.CSV2arff(approach1Output+File.separator,"approach1_Vitals8_"+steps);
		c.CSV2arff(approach1Output+File.separator,"approach1_Vitals9_"+steps);
		
	}
	
	public static void main(String[] args) {

//		try {
//			createDataSVC(5);
//			createDataVitals(5);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	public void createDataDemo(int stp) throws IOException {
		System.out.println("A1 - CreateDataDemo");
		steps = stp;
		String Vitals = approach1Output +  steps+ "DiagnoseData.csv";

		BufferedReader inVitals = new BufferedReader(new FileReader(Vitals));
		BufferedWriter outSVC2 = new BufferedWriter(new FileWriter(approach1Output+File.separator+"approach1_Demo1_"+steps+".csv"));
		BufferedWriter outSVC5 = new BufferedWriter(new FileWriter(approach1Output+File.separator+"approach1_Demo2_"+steps+".csv"));
		BufferedWriter outSVC6 = new BufferedWriter(new FileWriter(approach1Output+File.separator+"approach1_Demo3_"+steps+".csv"));

		CreateDataDiscret t = new CreateDataDiscret();
		
		String line1;
		line1 = inVitals.readLine();
		
		String[] split = line1.split(",",-1);
		String att2 = split[0]+ ",";
		String att3 = split[0]+ ",";
		String att6 = split[0]+ ",";
		//		patient += ",";
		for(int i=0 ; i < steps-1; i++){
			att2 += split[1]+"_"+i + ",";
			att3 += split[2]+"_"+i + ",";
			att6 += split[3]+"_"+i + ",";
		}
		att2 += split[1]+"_"+(steps-1);
		att3 += split[2]+"_"+(steps-1);
		att6 += split[3]+"_"+(steps-1);
		outSVC2.write(att2+'\n');
		outSVC5.write(att3+'\n');
		outSVC6.write(att6+'\n');
		
		
		

		int last = 0;
		ArrayList<String> lines1 = new ArrayList<String>();
		while ((line1 = inVitals.readLine()) != null) {
			String[] splited = line1.split(",",-1);
			if(last == 0){
				last = Integer.parseInt(splited[0]);
				lines1.add(line1);
				continue;
			}
			if(last == Integer.parseInt(splited[0])){
				lines1.add(line1);
			}else{
				 att2 = last+ ",";
				 att3 = last+ ",";
				 att6 = last+ ",";
				for(int i=0 ; i < steps-1; i++){
					split = lines1.get(((lines1.size()-steps)+i)).split(",",-1);
					att2 += split[1] + ",";
					att3 += split[2] + ",";
					att6 += split[3] + ",";
				}
				split = lines1.get((lines1.size()-1)).split(",",-1);
				att2 += split[1];
				att3 +=split[2];
				att6 += split[3];
				if(!split[2].isEmpty()){
					outSVC2.write(att2+'\n');
				}
				if(!split[3].isEmpty()){
					outSVC5.write(att3+'\n');
				}
				if(!split[6].isEmpty()){
					outSVC6.write(att6+'\n');
				}

				lines1 = new ArrayList<String>();
				last = Integer.parseInt(splited[0]);
				lines1.add(line1);
			}
		}
		inVitals.close();
		outSVC2.close();
		outSVC5.close();
		outSVC6.close();
		
		CreateDataDiscret c = new CreateDataDiscret();
		
		c.CSV2arff(approach1Output+File.separator,"approach1_Demo1_"+steps);
		c.CSV2arff(approach1Output+File.separator,"approach1_Demo2_"+steps);
		c.CSV2arff(approach1Output+File.separator,"approach1_Demo3_"+steps);
	}

}
