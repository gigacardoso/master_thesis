package tdm.classification.createData.ALS;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;


public class AlternativeApproachCreateData {
	private   String alternativeOutput;
	private   int steps;
	private CreateDataDiscret t;
		
	public AlternativeApproachCreateData(String alternativeOutput2, int steps2) {
		alternativeOutput = alternativeOutput2;
		steps = steps2;
		try {
			t = new CreateDataDiscret();
			t.findMinsDivider();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void createData(int stp) throws IOException{
		System.out.println("AA - CreateData");
		steps = stp;
		String alsfrs = alternativeOutput + steps+ "ALSFRS_Data.csv";
		String svc = alternativeOutput +  steps+ "SVC_Data.csv";
		String Demo = alternativeOutput +  "Demo_Data.csv";
		String Vitals = alternativeOutput +  steps+ "Vitals_Data.csv";

		BufferedReader inSVC = new BufferedReader(new FileReader(svc));
		BufferedReader inALS = new BufferedReader(new FileReader(alsfrs));
		BufferedReader inDemo = new BufferedReader(new FileReader(Demo));
		BufferedReader inVitals = new BufferedReader(new FileReader(Vitals));
		BufferedWriter out = new BufferedWriter(new FileWriter(alternativeOutput+File.separator+"approach1_"+steps+".csv"));

		String line1,line2,line3,line4;
		line1 = inSVC.readLine();
		line2 = inALS.readLine();
		line4 = inDemo.readLine();
		line3 = inVitals.readLine();
		
				
		String[] splitHeight = line3.split(",",-1);
		String[] split4 = line4.split(",",-1);
		String patient = split4[0].replace(' ', '_')+",";
		patient += split4[4].replace(' ', '_') + "," + split4[11].replace(' ', '_') + ","+ splitHeight[4].replace(' ', '_') + ",";
//		patient += ",";
		for(int i=0 ; i < steps-1; i++){
			String[] split = line1.split(",",-1);
			patient += split[2].replace(' ', '_').replace('%', 'P') +"_"+i + "," + split[5].replace(' ', '_') +"_"+i + "," + split[6].replace(' ', '_') +"_"+i + "," + split[7].replace(' ', '_') +"_"+i+",";
			String[] split3 = line3.split(",",-1);
			patient += split3[2].replace(' ', '_') +"_"+i + "," + split3[3].replace(' ', '_') +"_"+i + "," + split3[6].replace(' ', '_') +"_"+i + ","
					+ split3[7].replace(' ', '_') +"_"+i + "," + split3[8].replace(' ', '_') +"_"+i + "," + split3[9].replace(' ', '_') +"_"+i + ",";
			String[] split2 = line2.split(",",-1);
			patient += /*split2[14] + "," + */ split2[15].replace(' ', '_') +"_"+i + ",";
//													patient += "|,";
		}
		//just the class of n+1
		String[] split2 = line2.split(",",-1);
		patient += /*split2[14] + "," +*/ split2[15].replace(' ', '_') +"_"+steps;
		out.write(patient+'\n');
		
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
				ArrayList<String> lines2 = new ArrayList<String>();
				while ((line2 = inALS.readLine()) != null) {
					String[] splitedALS = line2.split(",",-1);
					if(last == Integer.parseInt(splitedALS[0])){
						lines2.add(line2);
					}else{
						if(lines2.size() > 2){
							ArrayList<String> lines3 = new ArrayList<String>();
							while ((line3 = inVitals.readLine()) != null) {
								String[] splitedVitals = line3.split(",",-1);
								if(last == Integer.parseInt(splitedVitals[0])){
									lines3.add(line3);
								}else{
									if(lines3.size() > 2){
										ArrayList<String> lines4 = new ArrayList<String>();
										while ((line4 = inDemo.readLine()) != null) {
											String[] splitedDemo = line4.split(",",-1);
											if(last == Integer.parseInt(splitedDemo[0])){
												lines4.add(line4);
											}else{
												if(lines4.size() > 0){
													splitHeight = lines3.get(0).split(",",-1);
													split4 = lines4.get((lines4.size()-1)).split(",",-1);
													patient = last+ ",";
													patient += split4[4] + "," + t.discretize(last,1,split4[11]) + ","+ t.discretize(last,2,splitHeight[4]) + ",";
//													patient += ",";
													for(int i=0 ; i < steps-1; i++){
														String[] split = lines1.get(((lines1.size()-steps)+i)).split(",",-1);
														patient += t.discretize(last,3,split[2]) + "," + t.discretize(last,4,split[5]) + ","
														+ t.discretize(last,5,split[6]) + "," + t.discretize(last,6,split[7])+",";
														String[] split3 = lines3.get(((lines3.size()-steps)+i)).split(",",-1);
														patient += t.discretize(last,7,split3[2]) + "," + t.discretize(last,8,split3[3]) + "," + t.discretize(last,9,split3[6]) + ","
																+ t.discretize(last,10,split3[7]) + "," + t.discretize(last,11,split3[8]) + "," + t.discretize(last,12,split3[9]) + ",";
														split2 = lines2.get(((lines2.size()-steps)+i)).split(",",-1);
														String discret = t.discretize(last,13,split2[15]);
														patient += /*split2[14] + "," + */ discret+",";//split2[15] + ",";
	//													patient += "|,";
													}
													//just the class of n+1
													split2 = lines2.get((lines2.size()-1)).split(",",-1);
													String discret = t.discretize(last,13,split2[15]);
													patient += /*split2[14] + "," + */ discret;//split2[15];
													out.write(patient+'\n');

													lines1 = new ArrayList<String>();
													lines2 = new ArrayList<String>();
													lines3 = new ArrayList<String>();
													lines4 = new ArrayList<String>();

													inDemo.close();
													break;
												}
											}					
										}
										inDemo = new BufferedReader(new FileReader(Demo));
										inDemo.readLine();
										inVitals.close();
										break;
									}
								}					
							}
							inVitals = new BufferedReader(new FileReader(Vitals));
							inVitals.readLine();
							inALS.close();
							break;
						}
					}					
				}
				inALS =  new BufferedReader(new FileReader(alsfrs));
				inALS.readLine();
				last = Integer.parseInt(splited[0]);
				lines1.add(line1);
			}
		}
		inALS.close();
		inSVC.close();
		inDemo.close();
		inVitals.close();
		out.close();
		

		CSV2arff(alternativeOutput,"approach1_"+steps);
	}

	public   void createDataNoClass(int stp) throws IOException{
		System.out.println("AA - CreateDataNoClass");
		steps = stp;
		String alsfrs = alternativeOutput + steps+ "ALSFRS_Data.csv";
		String svc = alternativeOutput +  steps+ "SVC_Data.csv";
		String Demo = alternativeOutput +  "Demo_Data.csv";
		String Vitals = alternativeOutput +  steps+ "Vitals_Data.csv";

		BufferedReader inSVC = new BufferedReader(new FileReader(svc));
		BufferedReader inALS = new BufferedReader(new FileReader(alsfrs));
		BufferedReader inDemo = new BufferedReader(new FileReader(Demo));
		BufferedReader inVitals = new BufferedReader(new FileReader(Vitals));
		BufferedWriter out = new BufferedWriter(new FileWriter(alternativeOutput+"approach1_NoClass_"+steps+".csv"));

		String line1,line2,line3,line4;
		line1 = inSVC.readLine();
		line2 = inALS.readLine();
		line4 = inDemo.readLine();
		line3 = inVitals.readLine();
		
				
		String[] splitHeight = line3.split(",",-1);
		String[] split4 = line4.split(",",-1);
		String patient = split4[0].replace(' ', '_')+",";
		patient += split4[4].replace(' ', '_') + "," + split4[11].replace(' ', '_') + ","+ splitHeight[4].replace(' ', '_') + ",";

		for(int i=0 ; i < steps-1; i++){
			String[] split = line1.split(",",-1);
			patient += split[2].replace(' ', '_').replace('%', 'P') +"_"+i + "," + split[5].replace(' ', '_') +"_"+i + "," + split[6].replace(' ', '_') +"_"+i + "," + split[7].replace(' ', '_') +"_"+i+",";
			String[] split3 = line3.split(",",-1);
			patient += split3[2].replace(' ', '_') +"_"+i + "," + split3[3].replace(' ', '_') +"_"+i + "," + split3[6].replace(' ', '_') +"_"+i + ","
					+ split3[7].replace(' ', '_') +"_"+i + "," + split3[8].replace(' ', '_') +"_"+i + "," + split3[9].replace(' ', '_') +"_"+i + ",";
//			String[] split2 = line2.split(",",-1);
//			patient += split2[15].replace(' ', '_') +"_"+i + ",";
		}
		//just the class of n+1
		String[] split2 = line2.split(",",-1);
		patient += split2[15].replace(' ', '_') +"_"+steps;
		out.write(patient+'\n');
		
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
				ArrayList<String> lines2 = new ArrayList<String>();
				while ((line2 = inALS.readLine()) != null) {
					String[] splitedALS = line2.split(",",-1);
					if(last == Integer.parseInt(splitedALS[0])){
						lines2.add(line2);
					}else{
						if(lines2.size() > 2){
							ArrayList<String> lines3 = new ArrayList<String>();
							while ((line3 = inVitals.readLine()) != null) {
								String[] splitedVitals = line3.split(",",-1);
								if(last == Integer.parseInt(splitedVitals[0])){
									lines3.add(line3);
								}else{
									if(lines3.size() > 2){
										ArrayList<String> lines4 = new ArrayList<String>();
										while ((line4 = inDemo.readLine()) != null) {
											String[] splitedDemo = line4.split(",",-1);
											if(last == Integer.parseInt(splitedDemo[0])){
												lines4.add(line4);
											}else{
												if(lines4.size() > 0){
													splitHeight = lines3.get(0).split(",",-1);
													split4 = lines4.get((lines4.size()-1)).split(",",-1);
													patient = last+ ",";
													patient += split4[4] + "," + t.discretize(last,1,split4[11]) + ","+ t.discretize(last,2,splitHeight[4]) + ",";

													for(int i=0 ; i < steps-1; i++){
														String[] split = lines1.get(((lines1.size()-steps)+i)).split(",",-1);
														patient += t.discretize(last,3,split[2]) + "," + t.discretize(last,4,split[5]) + ","
														+ t.discretize(last,5,split[6]) + "," + t.discretize(last,6,split[7])+",";
														String[] split3 = lines3.get(((lines3.size()-steps)+i)).split(",",-1);
														patient += t.discretize(last,7,split3[2]) + "," + t.discretize(last,8,split3[3]) + "," + t.discretize(last,9,split3[6]) + ","
																+ t.discretize(last,10,split3[7]) + "," + t.discretize(last,11,split3[8]) + "," + t.discretize(last,12,split3[9]) + ",";
													}
													//just the class of n+1
													split2 = lines2.get((lines2.size()-1)).split(",",-1);
													String discret = t.discretize(last,13,split2[15]);
													patient += discret;
													out.write(patient+'\n');

													lines1 = new ArrayList<String>();
													lines2 = new ArrayList<String>();
													lines3 = new ArrayList<String>();
													lines4 = new ArrayList<String>();

													inDemo.close();
													break;
												}
											}					
										}
										inDemo = new BufferedReader(new FileReader(Demo));
										inDemo.readLine();
										inVitals.close();
										break;
									}
								}					
							}
							inVitals = new BufferedReader(new FileReader(Vitals));
							inVitals.readLine();
							inALS.close();
							break;
						}
					}					
				}
				inALS =  new BufferedReader(new FileReader(alsfrs));
				inALS.readLine();
				last = Integer.parseInt(splited[0]);
				lines1.add(line1);
			}
		}
		inALS.close();
		inSVC.close();
		inDemo.close();
		inVitals.close();
		out.close();
		
		CSV2arff(alternativeOutput,"approach1_NoClass_"+steps);
	}
	
	private   void CSV2arff(String path,String str){ 
		// load CSV
		CSVLoader loader = new CSVLoader();
		try {
			loader.setSource(new File(path+str+".csv"));

			Instances data;
			data = loader.getDataSet();

			// save ARFF
			ArffSaver saver = new ArffSaver();
			saver.setInstances(data);
			saver.setFile(new File(path+str+".arff"));

			//saver.setDestination(new File(path+str+".arff"));
			saver.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
//		try {
//			createData(3);
//			createData(4);
//			createData(5);
//			createDataNoClass(5);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

}
