package createData.ALS;
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
	
	Float[] divider = {(float) 6.1,(float) 13.7, (float) 11.7, (float) 0.653,(float) 0.544,
			(float) 0.549,(float) 7.9,(float) 12.6,(float) 9.9,(float) 5.1,(float) 9.86,(float) 22.830 };
	Float[] mins = {(float) 18,(float) 59,(float) 0,(float) 0,(float) 0.2,(float) 0,(float) 40,
			(float) 72,(float) 45,(float) 9,(float) 0,(float) 36.7};
	
	public AlternativeApproachCreateData(String alternativeOutput2, int steps2) {
		alternativeOutput = alternativeOutput2;
		steps = steps2;
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
													patient += split4[4] + "," + discretize(1,split4[11]) + ","+ discretize(2,splitHeight[4]) + ",";
//													patient += ",";
													for(int i=0 ; i < steps-1; i++){
														String[] split = lines1.get(((lines1.size()-steps)+i)).split(",",-1);
														patient += discretize(3,split[2]) + "," + discretize(4,split[5]) + ","
														+ discretize(5,split[6]) + "," + discretize(6,split[7])+",";
														String[] split3 = lines3.get(((lines3.size()-steps)+i)).split(",",-1);
														patient += discretize(7,split3[2]) + "," + discretize(8,split3[3]) + "," + discretize(9,split3[6]) + ","
																+ discretize(10,split3[7]) + "," + discretize(11,split3[8]) + "," + discretize(12,split3[9]) + ",";
														split2 = lines2.get(((lines2.size()-steps)+i)).split(",",-1);
														String discret = discretize(13,split2[15]);
														patient += /*split2[14] + "," + */ discret+",";//split2[15] + ",";
	//													patient += "|,";
													}
													//just the class of n+1
													split2 = lines2.get((lines2.size()-1)).split(",",-1);
													String discret = discretize(13,split2[15]);
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
													patient += split4[4] + "," + discretize(1,split4[11]) + ","+ discretize(2,splitHeight[4]) + ",";

													for(int i=0 ; i < steps-1; i++){
														String[] split = lines1.get(((lines1.size()-steps)+i)).split(",",-1);
														patient += discretize(3,split[2]) + "," + discretize(4,split[5]) + ","
														+ discretize(5,split[6]) + "," + discretize(6,split[7])+",";
														String[] split3 = lines3.get(((lines3.size()-steps)+i)).split(",",-1);
														patient += discretize(7,split3[2]) + "," + discretize(8,split3[3]) + "," + discretize(9,split3[6]) + ","
																+ discretize(10,split3[7]) + "," + discretize(11,split3[8]) + "," + discretize(12,split3[9]) + ",";
													}
													//just the class of n+1
													split2 = lines2.get((lines2.size()-1)).split(",",-1);
													String discret = discretize(13,split2[15]);
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
	
	private  String discretize(int var ,String string) {
		var--;
		if(var ==12){ 
			int i;
			try{
				i = Integer.parseInt(string);
			}catch(Exception e) {
				return "";
			}
			if(i<=10){
				return "{0-12}";
			}else {
				if(i<=20){
					return "{12-24}";
				}else{
					if(i<30){
						return "{24-36}";
					}else {
						return "{36-48}";
					}
				}
			}
		}else{
			if(string.length()>0){
				int c = (int)((Float.parseFloat(string)-mins[var])/divider[var]);
				if(c==10){
					c=9;
				}
				float min = mins[var];
				return (min+(c*divider[var]))+ "-" + (min+((c+1)*divider[var]));
			}else{
				return string;
			}
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
