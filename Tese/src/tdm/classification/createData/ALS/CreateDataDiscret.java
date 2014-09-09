package tdm.classification.createData.ALS;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class CreateDataDiscret {

	public  String path = "C:" + File.separator + "PROACT_2013_08_27_ALL_FORMS" + File.separator + "ToUse"+ File.separator;
	public static  String alternativeOutput = "C:" + File.separator + "PROACT_2013_08_27_ALL_FORMS" + File.separator + "AlternativeData"+ File.separator;
	public static  String approach1Output = "C:" + File.separator + "PROACT_2013_08_27_ALL_FORMS" + File.separator + "Approach1Data"+ File.separator;
	public static  String diagnosisOutput = "C:" + File.separator + "PROACT_2013_08_27_ALL_FORMS" + File.separator + "DiagnosisData"+ File.separator;
	private  HashMap<Integer,Boolean> _common = new HashMap<Integer, Boolean>();
	public static  int steps;

	// 10 buckets
	//	public static Float[] divider = {(float) 6.1,(float) 13.7, (float) 11.7, (float) 0.653,(float) 0.544,
	//			(float) 0.549,(float) 7.9,(float) 12.6,(float) 9.9,(float) 5.1,(float) 9.86,(float) 22.830 };

	// 6 buckets
	public static Float[] divider = {(float)10.167,(float)22.833,(float)19.5,(float)1.088,(float)0.907,
		(float)0.915,(float)13.167,(float)21,(float)16.5,(float)8.5,(float)16.433,(float)38.05};

	public static Float[] mins = {(float) 18,(float) 59,(float) 0,(float) 0,(float) 0.2,(float) 0,
		(float) 40,	(float) 72,(float) 45,(float) 9,(float) 0,(float) 36.7};

	public static int buckets = 6;	

	public static void main(String[] args) {
		CreateDataDiscret create = new CreateDataDiscret();
		try {
			steps = 5;
			create.diagnosticData();//DONE
			create.diagnosticData2();//DONE
			//
			//
			create.baseLineWithoutClass();//DONE
			AlternativeApproachCreateData aa = new AlternativeApproachCreateData(alternativeOutput,steps);
			create.alternativeApproach(steps);
			aa.createDataNoClass(steps);//DONE

			create.approach1(steps);
			Approach1CreateDataDiscret a1 = new Approach1CreateDataDiscret(approach1Output, steps); //DONE
			a1.createDataSVC(steps);
			a1.createDataVitals(steps);
			a1.createDataDemo(steps);

			Approach2CreateData a2 = new Approach2CreateData(approach1Output,diagnosisOutput,steps); 
			a2.createData(steps);//DONE


			MoveData move = new MoveData(alternativeOutput,approach1Output,diagnosisOutput,steps);
			move.MoveAllData(steps, true);
			//			create.CleanData();

			//			aa.createData(steps);
			//			create.baseLineWithClass();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private  void CleanData() {
		System.out.println("Cleaning Data");
		String cam = "C:" + File.separator + "PROACT_2013_08_27_ALL_FORMS" + File.separator;
		try {
			DeleteFiles(cam + "AlternativeData");
			DeleteFiles(cam + "Approach1Data");
			DeleteFiles(cam + "DiagnosisData");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public  void DeleteFiles(String folder) throws IOException {
		System.out.println("Called deleteFiles");
		File file = new File(folder);

		String[] myFiles;
		if (file.isDirectory()) {
			myFiles = file.list();
			for (int i = 0; i < myFiles.length; i++) {
				File myFile = new File(file, myFiles[i]);
				System.out.println(myFile);
				if (!myFile.isDirectory()) {
					myFile.delete();
				}
			}
		}

	}

	public void CSV2arff(String path,String str){ 
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
			loader = new CSVLoader();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private  void approach1(int num) throws IOException {
		steps = num;

		remove("ALSFRS_Data.csv", steps+"_ALSFRS_DATA.csv",approach1Output, steps);
		remove("SVC_Data.csv", steps+"_SVC_DATA.csv",approach1Output, steps);
		remove("Vitals_Data.csv", steps+"_Vitals_DATA.csv",approach1Output, steps);

	}

	// creates diagnosis data only with patients that have the required (steps) time points
	// diagnosis data only has steps-1 time points
	// diagnosis real has the last time point
	private  void diagnosticData() throws IOException {
		System.out.println("Diagnostic data");
		remove("ALSFRS_Data.csv", steps+"_ALSFRS_DATA.csv",path, steps);
		remove("SVC_Data.csv", steps+"_SVC_DATA.csv",path, steps);
		remove("Vitals_Data.csv", steps+"_Vitals_DATA.csv",path, steps);

		String alsfrs = path+ steps+ "_ALSFRS_Data.csv";
		String svc = path + steps +  "_SVC_Data.csv";
		String Demo = path +  "Demo_Data.csv";
		String Vitals = path + steps + "_Vitals_Data.csv";

		BufferedReader inALS = new BufferedReader(new FileReader(alsfrs));
		BufferedReader inDemo = new BufferedReader(new FileReader(Demo));
		BufferedReader inVitals = new BufferedReader(new FileReader(Vitals));
		BufferedReader inSVC = new BufferedReader(new FileReader(svc));

		BufferedWriter outTrain = new BufferedWriter(new FileWriter(diagnosisOutput+"DiagnoseData.csv"));
		BufferedWriter outReal = new BufferedWriter(new FileWriter(diagnosisOutput+"DiagnoseDataReal.csv"));

		String line1,line2,line3,line4;
		line1 = inSVC.readLine();
		line2 = inALS.readLine();
		line4 = inDemo.readLine();
		line3 = inVitals.readLine();


		String[] splitHeight = line3.split(",",-1);
		String[] split4 = line4.split(",",-1);
		String patient = split4[0].replace(' ', '_')+",";
		patient += split4[4].replace(' ', '_') + "," + split4[11].replace(' ', '_') + ","+ splitHeight[4].replace(' ', '_') + ",";

		String[] split = line1.split(",",-1);
		patient += split[2].replace(' ', '_').replace('%', 'P') + "," + split[5].replace(' ', '_')+ "," +
				split[6].replace(' ', '_') + "," + split[7].replace(' ', '_')+ ",";
		String[] split3 = line3.split(",",-1);
		patient += split3[2].replace(' ', '_') + "," + split3[3].replace(' ', '_')+ "," + split3[6].replace(' ', '_')+ ","
				+ split3[7].replace(' ', '_')+ "," + split3[8].replace(' ', '_')  + "," + split3[9].replace(' ', '_') + ",";
		String[] split2 = line2.split(",",-1);
		patient += split2[15].replace(' ', '_');
		outTrain.write(patient+'\n');
		outReal.write(patient+'\n');


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
													if(last == 168510){
														@SuppressWarnings("unused")
														int j = 0;
													}
													splitHeight = lines3.get(0).split(",",-1);
													split4 = lines4.get((lines4.size()-1)).split(",",-1);
													patient = last+ ",";
													patient += split4[4] + "," + split4[11] + ","+ splitHeight[4] + ",";

													for(int i=0 ; i < steps-1; i++){
														splitHeight = lines3.get(0).split(",",-1);
														split4 = lines4.get((lines4.size()-1)).split(",",-1);
														patient = last+ ",";
														patient += discretize(0,split4[4]) + "," + discretize(1,split4[11]) + ","+ discretize(2,splitHeight[4]) + ",";
														split = lines1.get(((lines1.size()-steps)+i)).split(",",-1);
														patient += discretize(3,split[2]) + "," + discretize(4,split[5]) + "," + discretize(5,split[6]) + "," + discretize(6,split[7])+",";
														split3 = lines3.get(((lines3.size()-steps)+i)).split(",",-1);
														patient += discretize(7,split3[2]) + "," + discretize(8,split3[3]) + "," + discretize(9,split3[6]) + ","
																+ discretize(10,split3[7]) + "," + discretize(11,split3[8]) + "," + discretize(12,split3[9]) + ",";
														split2 = lines2.get(((lines2.size()-steps)+i)).split(",",-1);
														String discret = discretize(13,split2[15]);
														patient += discret;

														outTrain.write(patient+'\n');
													}

													patient = last+ ",";
													patient += discretize(0,split4[4]) + "," + discretize(1,split4[11]) + ","+ discretize(2,splitHeight[4]) + ",";

													split = line1.split(",",-1);
													patient += discretize(3,split[2].replace(' ', '_').replace('%', 'P')) + "," + discretize(4,split[5].replace(' ', '_'))+ "," +
															discretize(5,split[6].replace(' ', '_')) + "," + discretize(6,split[7].replace(' ', '_'))+ ",";
													split3 = line3.split(",",-1);
													patient += discretize(7,split3[2].replace(' ', '_')) + "," + discretize(8,split3[3].replace(' ', '_'))+ "," +
															discretize(9,split3[6].replace(' ', '_'))+ ","
															+ discretize(10,split3[7].replace(' ', '_'))+ "," + discretize(11,split3[8].replace(' ', '_'))  + "," +
															discretize(12,split3[9].replace(' ', '_')) + ",";
													split2 = lines2.get((lines2.size()-1)).split(",",-1);
													String discret = discretize(13,split2[15]);
													patient +=  discret;
													outReal.write(patient+'\n');

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
		outTrain.close();
		outReal.close();

		CSV2arff(diagnosisOutput,"DiagnoseData");
		CSV2arff(diagnosisOutput,"DiagnoseDataReal");
	}

	// creates diagnosis data with everything (more or less steps and all timepoints)
	private  void diagnosticData2() throws IOException {
		System.out.println("Diagnostic data2");
		//		remove("ALSFRS_Data.csv", steps+"_ALSFRS_DATA.csv",path, steps);
		//		remove("SVC_Data.csv", steps+"_SVC_DATA.csv",path, steps);
		//		remove("Vitals_Data.csv", steps+"_Vitals_DATA.csv",path, steps);

		String alsfrs = path+ "ALSFRS_Data.csv";
		String svc = path + "SVC_Data.csv";
		String Demo = path + "Demo_Data.csv";
		String Vitals = path + "Vitals_Data.csv";

		BufferedReader inALS = new BufferedReader(new FileReader(alsfrs));
		BufferedReader inDemo = new BufferedReader(new FileReader(Demo));
		BufferedReader inVitals = new BufferedReader(new FileReader(Vitals));
		BufferedReader inSVC = new BufferedReader(new FileReader(svc));

		BufferedWriter outTrain = new BufferedWriter(new FileWriter(diagnosisOutput+"DiagnoseData.csv"));

		String line1,line2,line3,line4;
		line1 = inSVC.readLine();
		line2 = inALS.readLine();
		line4 = inDemo.readLine();
		line3 = inVitals.readLine();


		String[] splitHeight = line3.split(",",-1);
		String[] split4 = line4.split(",",-1);
		String patient = split4[0].replace(' ', '_')+",";
		patient += split4[4].replace(' ', '_') + "," + split4[11].replace(' ', '_') + ","+ splitHeight[4].replace(' ', '_') + ",";

		String[] split = line1.split(",",-1);
		patient += split[2].replace(' ', '_').replace('%', 'P') + "," + split[5].replace(' ', '_')+ "," +
				split[6].replace(' ', '_') + "," + split[7].replace(' ', '_')+ ",";
		String[] split3 = line3.split(",",-1);
		patient += split3[2].replace(' ', '_') + "," + split3[3].replace(' ', '_')+ "," + split3[6].replace(' ', '_')+ ","
				+ split3[7].replace(' ', '_')+ "," + split3[8].replace(' ', '_')  + "," + split3[9].replace(' ', '_') + ",";
		String[] split2 = line2.split(",",-1);
		patient += split2[15].replace(' ', '_');
		outTrain.write(patient+'\n');

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
						if(lines2.size() > 0){
							ArrayList<String> lines3 = new ArrayList<String>();
							while ((line3 = inVitals.readLine()) != null) {
								String[] splitedVitals = line3.split(",",-1);
								if(last == Integer.parseInt(splitedVitals[0])){
									lines3.add(line3);
								}else{
									if(lines3.size() > 0){
										ArrayList<String> lines4 = new ArrayList<String>();
										while ((line4 = inDemo.readLine()) != null) {
											String[] splitedDemo = line4.split(",",-1);
											if(last == Integer.parseInt(splitedDemo[0])){
												lines4.add(line4);
											}else{
												if(lines4.size() > 0){
													if(last == 168510){
														@SuppressWarnings("unused")
														int j = 0;
													}
													splitHeight = lines3.get(0).split(",",-1);
													split4 = lines4.get((lines4.size()-1)).split(",",-1);
													patient = last+ ",";
													patient += split4[4] + "," + split4[11] + ","+ splitHeight[4] + ",";
													int len = Math.min(lines1.size(), lines2.size());
													len = Math.min(len, lines3.size());
													for(int i=0 ; i < len; i++){
														splitHeight = lines3.get(0).split(",",-1);
														split4 = lines4.get((lines4.size()-1)).split(",",-1);
														patient = last+ ",";
														patient += discretize(0,split4[4]) + "," +  discretize(1,split4[11]) + ","+ discretize(2,splitHeight[4]) + ",";
														split = lines1.get(((lines1.size()-len)+i)).split(",",-1);
														patient += discretize(3,split[2]) + "," + discretize(4,split[5]) + "," + discretize(5,split[6])
																+ "," + discretize(6,split[7])+",";
														split3 = lines3.get(((lines3.size()-len)+i)).split(",",-1);
														patient += discretize(7,split3[2]) + "," + discretize(8,split3[3]) + "," + discretize(9,split3[6]) + ","
																+ discretize(10,split3[7]) + "," + discretize(11,split3[8]) + "," + discretize(12,split3[9]) + ",";
														split2 = lines2.get(((lines2.size()-len)+i)).split(",",-1);
														String discret = discretize(13,split2[15]);
														patient += discret;

														outTrain.write(patient+'\n');
													}
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
		outTrain.close();

		CSV2arff(diagnosisOutput,"DiagnoseData");
	}


	@SuppressWarnings("unused")
	private  void baseLineWithClass() throws IOException {
		System.out.println("Baseline With Class");
		remove("ALSFRS_Data.csv", steps+"_ALSFRS_DATA.csv",path, steps);
		remove("SVC_Data.csv", steps+"_SVC_DATA.csv",path, steps);
		remove("Vitals_Data.csv", steps+"_Vitals_DATA.csv",path, steps);

		String alsfrs = path+ steps+ "_ALSFRS_Data.csv";
		String svc = path + steps +  "_SVC_Data.csv";
		String Demo = path +  "Demo_Data.csv";
		String Vitals = path + steps + "_Vitals_Data.csv";

		BufferedReader inALS = new BufferedReader(new FileReader(alsfrs));
		BufferedReader inDemo = new BufferedReader(new FileReader(Demo));
		BufferedReader inVitals = new BufferedReader(new FileReader(Vitals));
		BufferedReader inSVC = new BufferedReader(new FileReader(svc));

		BufferedWriter[] out = new BufferedWriter[steps-1];

		for(int i=0; i< steps-1;i++){
			out[i] = new BufferedWriter(new FileWriter(diagnosisOutput+"baseline"+i+".csv"));
		}		

		String line1,line2,line3,line4;
		line1 = inSVC.readLine();
		line2 = inALS.readLine();
		line4 = inDemo.readLine();
		line3 = inVitals.readLine();


		String[] splitHeight = line3.split(",",-1);
		String[] split4 = line4.split(",",-1);
		String patient = split4[0].replace(' ', '_')+",";
		patient += split4[4].replace(' ', '_') + "," + split4[11].replace(' ', '_') + ","+ splitHeight[4].replace(' ', '_') + ",";

		String[] split = line1.split(",",-1);
		patient += split[2].replace(' ', '_').replace('%', 'P') + "," + split[5].replace(' ', '_')+ "," +
				split[6].replace(' ', '_') + "," + split[7].replace(' ', '_')+ ",";
		String[] split3 = line3.split(",",-1);
		patient += split3[2].replace(' ', '_') + "," + split3[3].replace(' ', '_')+ "," + split3[6].replace(' ', '_')+ ","
				+ split3[7].replace(' ', '_')+ "," + split3[8].replace(' ', '_')  + "," + split3[9].replace(' ', '_') + ",";
		String[] split2 = line2.split(",",-1);
		patient += split2[15].replace(' ', '_') + "_0,";
		patient += split2[15].replace(' ', '_');

		for(int i=0;i<steps-1;i++){
			out[i].write(patient+'\n');
		}

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
													patient += split4[4] + "," + split4[11] + ","+ splitHeight[4] + ",";

													for(int i=0 ; i < steps-1; i++){
														splitHeight = lines3.get(0).split(",",-1);
														split4 = lines4.get((lines4.size()-1)).split(",",-1);
														patient = last+ ",";
														patient += split4[4] + "," + split4[11] + ","+ splitHeight[4] + ",";
														split = lines1.get(((lines1.size()-steps)+i)).split(",",-1);
														patient += split[2] + "," + split[5] + "," + split[6] + "," + split[7]+",";
														split3 = lines3.get(((lines3.size()-steps)+i)).split(",",-1);
														patient += split3[2] + "," + split3[3] + "," + split3[6] + ","
																+ split3[7] + "," + split3[8] + "," + split3[9] + ",";
														split2 = lines2.get((lines2.size()-steps)+i).split(",",-1);
														String discret = discretize(13,split2[15]);
														patient +=  discret + ",";
														split2 = lines2.get((lines2.size()-1)).split(",",-1);
														discret = discretize(13,split2[15]);
														patient +=  discret;

														out[i].write(patient+'\n');
													}

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
		for(int i=0;i<steps-1;i++){
			out[i].close();
		}

		for(int i=0; i< steps-1;i++){
			CSV2arff(diagnosisOutput,"baseline"+i);
		}
	}	

	private  void baseLineWithoutClass() throws IOException {
		System.out.println("Baseline Without Class");

		remove("ALSFRS_Data.csv", steps+"_ALSFRS_DATA.csv",path, steps);
		remove("SVC_Data.csv", steps+"_SVC_DATA.csv",path, steps);
		remove("Vitals_Data.csv", steps+"_Vitals_DATA.csv",path, steps);

		String alsfrs = path+ steps+ "_ALSFRS_Data.csv";
		String svc = path + steps +  "_SVC_Data.csv";
		String Demo = path +  "Demo_Data.csv";
		String Vitals = path + steps + "_Vitals_Data.csv";

		BufferedReader inALS = new BufferedReader(new FileReader(alsfrs));
		BufferedReader inDemo = new BufferedReader(new FileReader(Demo));
		BufferedReader inVitals = new BufferedReader(new FileReader(Vitals));
		BufferedReader inSVC = new BufferedReader(new FileReader(svc));

		BufferedWriter[] out = new BufferedWriter[steps-1];

		for(int i=0; i< steps-1;i++){
			out[i] = new BufferedWriter(new FileWriter(diagnosisOutput+"baselineNoClass"+i+".csv"));
		}		

		String line1,line2,line3,line4;
		line1 = inSVC.readLine();
		line2 = inALS.readLine();
		line4 = inDemo.readLine();
		line3 = inVitals.readLine();


		String[] splitHeight = line3.split(",",-1);
		String[] split4 = line4.split(",",-1);
		String patient = split4[0].replace(' ', '_')+",";
		patient += split4[4].replace(' ', '_') + "," + split4[11].replace(' ', '_') + ","+ splitHeight[4].replace(' ', '_') + ",";

		String[] split = line1.split(",",-1);
		patient += split[2].replace(' ', '_').replace('%', 'P') + "," + split[5].replace(' ', '_')+ "," +
				split[6].replace(' ', '_') + "," + split[7].replace(' ', '_')+ ",";
		String[] split3 = line3.split(",",-1);
		patient += split3[2].replace(' ', '_') + "," + split3[3].replace(' ', '_')+ "," + split3[6].replace(' ', '_')+ ","
				+ split3[7].replace(' ', '_')+ "," + split3[8].replace(' ', '_')  + "," + split3[9].replace(' ', '_') + ",";
		String[] split2 = line2.split(",",-1);
		patient += split2[15].replace(' ', '_');

		for(int i=0;i<steps-1;i++){
			out[i].write(patient+'\n');
		}

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
													if(last == 168510){
														@SuppressWarnings("unused")
														int j = 0;
													}
													splitHeight = lines3.get(0).split(",",-1);
													split4 = lines4.get((lines4.size()-1)).split(",",-1);
													patient = last+ ",";
													patient += split4[4] + "," + split4[11] + ","+ splitHeight[4] + ",";

													for(int i=0 ; i < steps-1; i++){
														splitHeight = lines3.get(0).split(",",-1);
														split4 = lines4.get((lines4.size()-1)).split(",",-1);
														patient = last+ ",";
														patient += discretize(0,split4[4]) + "," +  discretize(1,split4[11]) + ","+ discretize(2,splitHeight[4]) + ",";
														split = lines1.get(((lines1.size()-steps)+i)).split(",",-1);
														patient += discretize(3,split[2]) + "," + discretize(4,split[5]) + "," + discretize(5,split[6])
																+ "," + discretize(6,split[7])+",";
														split3 = lines3.get(((lines3.size()-steps)+i)).split(",",-1);
														patient += discretize(7,split3[2]) + "," + discretize(8,split3[3]) + "," + discretize(9,split3[6]) + ","
																+ discretize(10,split3[7]) + "," + discretize(11,split3[8]) + "," + discretize(12,split3[9]) + ",";
														split2 = lines2.get((lines2.size()-1)).split(",",-1);
														String discret = discretize(13,split2[15]);
														patient += discret;

														out[i].write(patient+'\n');
													}

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
		for(int i=0;i<steps-1;i++){
			out[i].close();
		}

		for(int i=0; i< steps-1;i++){
			CSV2arff(diagnosisOutput,"baselineNoClass"+i);
		}
	}	

	/* discretize with bucket edges 0-0.75 */
	//	public String discretize(int var ,String string) {
	//		var--;
	//		if(var ==12){ 
	//			int i;
	//			try{
	//				i = Integer.parseInt(string);
	//			}catch(Exception e) {
	//				return "";
	//			}
	//			if(i<=10){
	//				return "{0-12}";
	//			}else {
	//				if(i<=20){
	//					return "{12-24}";
	//				}else{
	//					if(i<30){
	//						return "{24-36}";
	//					}else {
	//						return "{36-48}";
	//					}
	//				}
	//			}
	//		}else{
	//			if(string.length()>0){
	//				int c = (int)((Float.parseFloat(string)-mins[var])/divider[var]);
	//				if(c==buckets){
	//					c=buckets-1;
	//				}
	//				DecimalFormat df = new DecimalFormat("#.##");
	//				float min = mins[var];
	//				
	//				String s = df.format(min+(c*divider[var]))+ "-" + df.format(min+((c+1)*divider[var]));
	//				s = s.replace(",",".");
	//				return s;
	//			}else{
	//				return "missing"; //string;
	//			}
	//		}
	//	}

	/* discretize with N buckets and  NO var :  var-1, var-2 */
	public String discretize(int var ,String string) {
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
			if(var < 0){
				if(string.length()>0){
					return string;
				}else{
					return "missing";
				}
			}else{
				if(string.length()>0){
					int c = (int)((Float.parseFloat(string)-mins[var])/divider[var]);
					if(c==buckets){
						c=buckets-1;
					}	
					if(c == -1){
						System.out.println("wow <------------------------------------------------------ var - " + var);
						System.out.println("min - "+ mins[var] + " real - "+ string);
						c=0;
					}

					String s = "B"+c;
					return s;
				}else{
					return "missing"; //string;
				}
			}
		}
	}

	/* discretize with N buckets and  var :  var-1, var-2 */
	//	public String discretize(int var ,String string) {
	//		var--;
	//		if(var ==12){ 
	//			int i;
	//			try{
	//				i = Integer.parseInt(string);
	//			}catch(Exception e) {
	//				return "";
	//			}
	//			if(i<=10){
	//				return "{0-12}";
	//			}else {
	//				if(i<=20){
	//					return "{12-24}";
	//				}else{
	//					if(i<30){
	//						return "{24-36}";
	//					}else {
	//						return "{36-48}";
	//					}
	//				}
	//			}
	//		}else{
	//			if(string.length()>0){
	//				int c = (int)((Float.parseFloat(string)-mins[var])/divider[var]);
	//				if(c==buckets){
	//					c=buckets-1;
	//				}
	//				
	//				String s = var + "B"+c;
	//				return s;
	//			}else{
	//				return "missing"; //string;
	//			}
	//		}
	//	}

	private void alternativeApproach(int in) throws IOException, FileNotFoundException {
		steps = in;
		findCommon("ALSFRS_Data.csv","Demo_Data.csv","VITALS_Data.csv","SVC_Data.csv");

		ArrayList<Integer> com = new ArrayList<Integer>();
		for(Integer i:_common.keySet()){
			com.add(i);
		}
		Collections.sort(com);
		System.out.println(com.size());
		System.out.println(com);

		count("ALSFRS_DATA.csv" , "ALSFRS");
		count("SVC_DATA.csv", "SVC");
		count("VITALS_Data.csv", "Vitals");

		remove("ALSFRS_Data.csv", steps+"_ALSFRS_DATA.csv",path, steps);
		remove("SVC_Data.csv", steps+"_SVC_DATA.csv",path, steps);
		remove("Vitals_Data.csv", steps+"_Vitals_DATA.csv",path, steps);

		findCommon(steps+"_ALSFRS_Data.csv","Demo_Data.csv",steps+"_VITALS_Data.csv",steps+"_SVC_Data.csv");

		count(steps+"_ALSFRS_DATA.csv" , "ALSFRS");
		count(steps+"_SVC_DATA.csv", "SVC");
		count(steps+"_VITALS_Data.csv", "Vitals");

		removeNotInCommon(steps+"_ALSFRS_DATA.csv" /*where to remove*/,steps+"ALSFRS_DATA.csv");
		removeNotInCommon(steps+"_SVC_DATA.csv" /*where to remove*/,steps+"SVC_DATA.csv");
		removeNotInCommon(steps+"_VITALS_DATA.csv" /*where to remove*/,steps+"VITALS_DATA.csv");
		removeNotInCommon("Demo_DATA.csv" /*where to remove*/,"Demo_DATA.csv");
	}

	@SuppressWarnings("unused")
	private  void findCommon(String data1, String data2, String data3) throws IOException {
		System.out.println("findCommon");		
		_common =  new HashMap<Integer, Boolean>();
		HashMap<Integer,Boolean> Data1 = new HashMap<Integer, Boolean>();
		BufferedReader in = new BufferedReader(new FileReader(path+File.separator+data1));
		String line;
		in.readLine();
		while ((line = in.readLine()) != null) {
			String[] splited = line.split(",");
			Data1.put(Integer.parseInt(splited[0]),true);
		}
		in.close();

		HashMap<Integer,Boolean> Data2 = new HashMap<Integer, Boolean>();
		in = new BufferedReader(new FileReader(path+File.separator+data2));
		in.readLine();
		while ((line = in.readLine()) != null) {
			String[] splited = line.split(",");
			int id = Integer.parseInt(splited[0]);
			if(Data1.get(id) != null){
				Data2.put(id,true);
			}
		}
		in.close();

		in = new BufferedReader(new FileReader(path+File.separator+data3));
		in.readLine();
		while ((line = in.readLine()) != null) {
			String[] splited = line.split(",");
			int id = Integer.parseInt(splited[0]);
			if(Data2.get(id) != null){
				_common.put(id,true);
			}
		}
		in.close();
	}

	private  void findCommon(String data1, String data2, String data3, String data4) throws IOException{
		System.out.println("findCommon");
		HashMap<Integer,Boolean> Data1 = new HashMap<Integer, Boolean>();
		BufferedReader in = new BufferedReader(new FileReader(path+File.separator+data1));
		String line;
		in.readLine();
		while ((line = in.readLine()) != null) {
			String[] splited = line.split(",");
			Data1.put(Integer.parseInt(splited[0]),true);
		}
		in.close();

		HashMap<Integer,Boolean> Data2 = new HashMap<Integer, Boolean>();
		in = new BufferedReader(new FileReader(path+File.separator+data2));
		in.readLine();
		while ((line = in.readLine()) != null) {
			String[] splited = line.split(",");
			int id = Integer.parseInt(splited[0]);
			if(Data1.get(id) != null){
				Data2.put(id,true);
			}
		}
		in.close();

		HashMap<Integer,Boolean> Data3 = new HashMap<Integer, Boolean>();
		in = new BufferedReader(new FileReader(path+File.separator+data3));
		in.readLine();
		while ((line = in.readLine()) != null) {
			String[] splited = line.split(",");
			int id = Integer.parseInt(splited[0]);
			if(Data2.get(id) != null){
				Data3.put(id,true);
			}
		}
		in.close();

		in = new BufferedReader(new FileReader(path+File.separator+data4));
		in.readLine();
		while ((line = in.readLine()) != null) {
			String[] splited = line.split(",");
			int id = Integer.parseInt(splited[0]);
			if(Data3.get(id) != null){
				_common.put(id,true);
			}
		}
		in.close();
		System.out.println("commons found");
	}

	private  void removeNotInCommon(String inString, String outString) throws IOException {
		System.out.println("removeNotInCommon");

		BufferedReader in = new BufferedReader(new FileReader(path+File.separator+inString));
		BufferedWriter out = new BufferedWriter(new FileWriter(alternativeOutput+File.separator+outString));
		int last = 0;
		String head = in.readLine();
		out.write(head);
		ArrayList<String> lines = new ArrayList<String>();
		String line;
		while ((line = in.readLine()) != null) {
			String[] splited = line.split(",");
			if(last == 0){
				last = Integer.parseInt(splited[0]);
				lines.add(line);
				continue;
			}
			if(last == Integer.parseInt(splited[0])){
				lines.add(line);
			}else{
				if(_common.get(last) != null){
					for(String s:lines){
						out.write('\n'+s);
					}
				}
				last = Integer.parseInt(splited[0]);
				lines = new ArrayList<String>();
				lines.add(line);
			}
		}
		in.close();
		out.close();
	}

	private  void count(String name, String string) throws FileNotFoundException, IOException {
		HashMap<Integer,Integer> _counts = new HashMap<Integer,Integer>();
		System.out.println("count --------------- "+ string +" --------------");
		ArrayList<Integer> hash = new ArrayList<Integer>();
		BufferedReader in = new BufferedReader(new FileReader(path+File.separator+name));
		String line;
		int last = 0;
		int count = 0;
		in.readLine();
		while ((line = in.readLine()) != null) {
			String[] splited = line.split(",");
			if(last == 0){
				count = 1;
				last = Integer.parseInt(splited[0]);
				hash.add(last);
				continue;
			}
			if(last == Integer.parseInt(splited[0])){
				count++;
			}else{
				if(_counts.get(count) != null){
					int c = _counts.get(count);
					_counts.put(count, ++c);
				}else{
					_counts.put(count,1);
				}
				count = 1;
				last = Integer.parseInt(splited[0]);
				hash.add(last);
			}
		}
		in.close();
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
		Collections.sort(hash);
		ArrayList<Integer> com = new ArrayList<Integer>();
		for(Integer i:_common.keySet()){
			com.add(i);
		}
		Collections.sort(com);
		System.out.println(com);
		System.out.println(hash);
		System.out.println("-----------------------------");
	}

	//removes patients that dont have at least countToRemove entries
	private void remove(String pathToData, String string,String pathh, int countToRemove) throws FileNotFoundException, IOException {
		System.out.println("remove");
		BufferedReader in = new BufferedReader(new FileReader(path+File.separator+pathToData));
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
}