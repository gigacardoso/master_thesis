package tdm.classification.createData.ALSNEW;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class CreateData {

	public static  String path = "C:" + File.separator + "PROACT_2013_08_27_ALL_FORMS" + File.separator + "ToUse"+ File.separator;
	public static  String alternativeOutput = "C:" + File.separator + "PROACT_2013_08_27_ALL_FORMS" + File.separator + "AlternativeData"+ File.separator;
	public static  String approach1Output = "C:" + File.separator + "PROACT_2013_08_27_ALL_FORMS" + File.separator + "Approach1Data"+ File.separator;
	public static  String diagnosisOutput = "C:" + File.separator + "PROACT_2013_08_27_ALL_FORMS" + File.separator + "DiagnosisData"+ File.separator;
	private HashMap<String,Double> heights = new HashMap<String, Double>();

	private static String[] exams = {"Demo1","Demo2","Demo3","SVC2","SVC5","SVC6","SVC7","Vitals2","Vitals3","Vitals6","Vitals7","Vitals8","Vitals9"}; 
	public static  int steps;
	public static int buckets = 6;

	private static ArrayList<Double> mins;
	private static ArrayList<Double> maxs;
	private static ArrayList<Double> divider;

	public static void main(String[] args) {
		CreateData create = new CreateData();
		try {
			steps = 5;
			create.diagnosticData(null);
			create.findMinsMaxsDivider();
			create.saveheights();
			create.diagnosticData("Normalize");
//			create.diagnosticData("Discretize");

			Approach1CreateData a1 = new Approach1CreateData(approach1Output,diagnosisOutput, steps, exams);
			a1.createApproach1PredictionData();
			Approach2CreateData a2 = new Approach2CreateData(approach1Output,diagnosisOutput,steps, exams);
			a2.createApproach2PredictionData();

			create.createBaselineSingleOb();
			create.createBaselineMultipleOb();

			create.convertToArff(diagnosisOutput);
			create.convertToArff(approach1Output);
			create.convertToArff(alternativeOutput);

			MoveData move = new MoveData(path,alternativeOutput,approach1Output,diagnosisOutput,steps);
			move.MoveAllData(steps, false);
			create.CleanData();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void saveheights() throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path+"VITALS_Data.csv"));
		String line = in.readLine();
		String[] split = line.split(",",-1);
		int index = 0;
		for (int i = 0; i < split.length; i++) {
			if(split[i].equals("Height")){
				index = i;
				break;
			}
		}
		while((line = in.readLine()) != null){
			split = line.split(",",-1);
			String tmp = split[index];
			if(tmp.length()>0){
				Double i = Double.parseDouble(tmp);
				heights.put(split[0], i);
			}
		}
		in.close();
	}
	
	public void findMinsMaxsDivider() throws IOException {
		System.out.println("findMinsDivider");
		int skip = 2;
		mins = new ArrayList<Double>();
		maxs = new ArrayList<Double>();
		divider = new ArrayList<Double>();

		BufferedReader in = new BufferedReader(new FileReader(diagnosisOutput+"DiagnoseData.csv"));
		String line = in.readLine();
		String[] split = line.split(",",-1);
		System.out.print("\t");
		for (int i = 0+skip; i < split.length-1; i++) {
			mins.add(Double.MAX_VALUE);
			maxs.add(Double.MIN_VALUE);
			System.out.print(split[i] + "\t");
		}
		System.out.println();
		while((line = in.readLine()) != null){
			split = line.split(",",-1);
			for (int i = 0+skip; i < split.length-1; i++) {
				if(split[i].length()>0){
					double d = Double.parseDouble(split[i]);
					if(d < mins.get(i-skip)){
						mins.set(i-skip, d);
					}
					if(d > maxs.get(i-skip)){
						maxs.set(i-skip,d);
					}
				}
			}
		}
		for (int i = 0; i < mins.size(); i++) {
			double min = mins.get(i);
			double max = maxs.get(i);
			double div = (max-min)/buckets;
			divider.add(div);
		}
		in.close();
		System.out.print("MIN-\t");
		for (int i = 0; i < mins.size(); i++) {
			System.out.print(mins.get(i) + " \t");
		}
		System.out.println();
		System.out.print("MAX-\t");
		for (int i = 0; i < maxs.size(); i++) {
			System.out.print(maxs.get(i) + "\t");
		}
		System.out.println();
		System.out.print("Div-\t");
		for (int i = 0; i < divider.size(); i++) {
			System.out.print(divider.get(i) + "\t");
		}
		System.out.println();
	}

	private void convertToArff(String out) {
		File file = new File(out);
		CreateData create =  new CreateData();
		String[] myFiles;
		if (file.isDirectory()) {
			myFiles = file.list();
			for (int i = 0; i < myFiles.length; i++) {
				File myFile = new File(file, myFiles[i]);
				if (!myFile.isDirectory()) {
					create.CSV2arff(out, myFile.getName().split("\\.")[0]);
					try{
						myFile.delete();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void createBaselineMultipleOb() throws IOException {
		System.out.println("Create Baseline Multi");
		BufferedReader inFact = new BufferedReader(new FileReader(diagnosisOutput + "DiagnoseData.csv"));

		String line = inFact.readLine();
		String[] splited = line.split(",", -1);
		String headWithout = "";
		for(int j= 0;j<steps-1;j++){
			for(int i= 0;i<splited.length;i++){
				if(i<splited.length-1){
					headWithout += splited[i]+"_"+j+",";
				}
			}
		}
		headWithout += splited[splited.length-1]+"_class\n";
		BufferedWriter without = new BufferedWriter(new FileWriter(alternativeOutput + "approach1_NoClass_"+steps+".csv"));
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
						if(id.equals("77")){
							@SuppressWarnings("unused")
							int j= 0;
						}
						outputLineMultiBaseline(id,without,lines,steps);
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

	private void outputLineMultiBaseline(String id, BufferedWriter without,
			List<String> lines, int steps) throws IOException {
		int size = lines.size();

		String entryWithoutClass = "";
		for(int i =0 ; i< steps-1; i++){
			String line = lines.get(size-steps+i);
			String[] split = line.split(",",-1);
			for(int j= 0; j<split.length; j++){
				if(j < split.length-1){
					entryWithoutClass += split[j]+ ",";
				}
			}
		}
		String[] last = lines.get(size-1).split(",",-1);
		entryWithoutClass += last[last.length-1]+ "\n";
		without.write(entryWithoutClass);
	}

	private void createBaselineSingleOb() throws IOException {
		System.out.println("Create Baseline Single");
		BufferedReader inFact = new BufferedReader(new FileReader(diagnosisOutput + "DiagnoseData.csv"));

		HashMap<Integer,BufferedWriter> without_all = new HashMap<Integer,BufferedWriter>();
		String line = inFact.readLine();
		String[] splited = line.split(",", -1);
		String headWithout = "";
		for(int i= 0;i<splited.length;i++){
			if(i<splited.length-1){
				headWithout += splited[i]+",";
			}
		}
		headWithout += splited[splited.length-1]+"_class\n";
		for(int i= 0;i<steps-1;i++){
			BufferedWriter without = new BufferedWriter(new FileWriter(diagnosisOutput + "baselineNoClass"+i+".csv"));
			without.write(headWithout);
			without_all.put(i, without);
		}

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
						if(id.equals("77")){
							@SuppressWarnings("unused")
							int j= 0;
						}
						for(int i= 0;i<steps-1;i++){
							BufferedWriter without = without_all.get(i);
							outputLineSingleBaseline(id,without,lines,i,steps);
						}
					}
					id = splited[0];
					lines = new ArrayList<String>();
					lines.add(line);
				}				
			}
		}
		for(int i= 0;i<steps-1;i++){
			without_all.get(i).close();
		}
		inFact.close();
	}

	private void outputLineSingleBaseline(String id, BufferedWriter without,
			List<String> lines, int i, int steps) throws IOException {
		int size = lines.size();
		String line = lines.get(size-steps+i);
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

	public  void CSV2arff(String path,String str){ 
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

	//	// creates diagnosis data only with patients that have the required (steps) time points
	//	// diagnosis data only has steps-1 timepoins
	//	// diagnosis real has the last time point
	private  void diagnosticData(String m) throws IOException {
		System.out.println("Diagnostic data - " + m);
		String alsfrs = path+ "ALSFRS_Data.csv";
		String svc = path + "SVC_Data.csv";
		String Demo = path +  "Demo_Data.csv";
		String Vitals = path + "Vitals_Data.csv";

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
		int size = 0;
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
				size = lines1.size();
				ArrayList<String> lines2 = new ArrayList<String>();
				while ((line2 = inALS.readLine()) != null) {
					String[] splitedALS = line2.split(",",-1);
					if(Integer.parseInt(splitedALS[0]) <= last || lines2.size()>0){
						if(last == Integer.parseInt(splitedALS[0])){
							lines2.add(line2);
						}else{
							if(lines2.size() > 0){
								if(lines2.size() < size){
									size = lines2.size();
								}
								ArrayList<String> lines3 = new ArrayList<String>();
								while ((line3 = inVitals.readLine()) != null) {
									String[] splitedVitals = line3.split(",",-1);
									if(Integer.parseInt(splitedVitals[0]) <= last  || lines3.size()>0){
										if(last == Integer.parseInt(splitedVitals[0])){
											lines3.add(line3);
										}else{
											if(lines3.size() > 0){
												if(lines3.size() < size){
													size = lines3.size();
												}
												ArrayList<String> lines4 = new ArrayList<String>();
												while ((line4 = inDemo.readLine()) != null) {
													String[] splitedDemo = line4.split(",",-1);
													if(Integer.parseInt(splitedDemo[0]) <= last || lines4.size()>0){
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

																for(int i=0 ; i < size; i++){
																	splitHeight = lines3.get(0).split(",",-1);
																	split4 = lines4.get((lines4.size()-1)).split(",",-1);
																	patient = last+ ",";
																	patient += method(m,last,0,split4[4]) + "," + method(m,last,1,split4[11]) + ","+ method(m,last,2,splitHeight[4]) + ",";
																	split = lines1.get(((lines1.size()-size)+i)).split(",",-1);
																	patient += method(m,last,3,split[2]) + "," + method(m,last,4,split[5]) + "," + method(m,last,5,split[6]) + "," + method(m,last,6,split[7])+",";
																	split3 = lines3.get(((lines3.size()-size)+i)).split(",",-1);
																	patient += method(m,last,7,split3[2]) + "," + method(m,last,8,split3[3]) + "," + method(m,last,9,split3[6]) + ","
																			+ method(m,last,10,split3[7]) + "," + method(m,last,11,split3[8]) + "," + method(m,last,12,split3[9]) + ",";
																	split2 = lines2.get(((lines2.size()-size)+i)).split(",",-1);
																	String discret = discretize(last,13,split2[15]);
																	patient += discret;

																	outTrain.write(patient+'\n');
																}
																if(size >= steps){
																	patient = last+ ",";
																	patient += method(m,last,0,split4[4]) + "," + method(m,last,1,split4[11]) + ","+ method(m,last,2,splitHeight[4]) + ",";

																	split = line1.split(",",-1);
																	patient += method(m,last,3,split[2].replace(' ', '_').replace('%', 'P')) + "," + method(m,last,4,split[5].replace(' ', '_'))+ "," +
																			method(m,last,5,split[6].replace(' ', '_')) + "," + method(m,last,6,split[7].replace(' ', '_'))+ ",";
																	split3 = line3.split(",",-1);
																	patient += method(m,last,7,split3[2].replace(' ', '_')) + "," + method(m,last,8,split3[3].replace(' ', '_'))+ "," +
																			method(m,last,9,split3[6].replace(' ', '_'))+ ","
																			+ method(m,last,10,split3[7].replace(' ', '_'))+ "," + method(m,last,11,split3[8].replace(' ', '_'))  + "," +
																			method(m,last,12,split3[9].replace(' ', '_')) + ",";
																	split2 = lines2.get((lines2.size()-1)).split(",",-1);
																	String discret = discretize(last,13,split2[15]);
																	patient +=  discret;
																	outReal.write(patient+'\n');
																}
																lines1 = new ArrayList<String>();
																lines2 = new ArrayList<String>();
																lines3 = new ArrayList<String>();
																lines4 = new ArrayList<String>();

																inDemo.close();
																break;
															}
														}
													}else{
														break;
													}
												}
												inDemo = new BufferedReader(new FileReader(Demo));
												inDemo.readLine();
												inVitals.close();
												lines1 = new ArrayList<String>();
												break;
											}
										}	
									}else{
										break;
									}
								}
								inVitals = new BufferedReader(new FileReader(Vitals));
								inVitals.readLine();
								inALS.close();
								lines1 = new ArrayList<String>();
								break;
							}
						}	
					}else{
						break;
					}
				}
				inALS =  new BufferedReader(new FileReader(alsfrs));
				inALS.readLine();
				last = Integer.parseInt(splited[0]);
				lines1 = new ArrayList<String>();
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

	private String method(String m, int last, int var, String string) {
		if(m == null){
			return string;
		}else{
			if(m.equals("Normalize")){
				return normalize(var,string);
			}else{
				if(m.equals("Discretize")){
					return discretize(last,var,string);
				}
			}
		}
		return string;
	}

	public String normalize(int var ,String string) {
		if(var >= 3){
			var = var - 1;
			if(string.length()>0){
				int a = 0;
				int b = 1;
				Double Xmin = mins.get(var);
				Double Xmax = maxs.get(var);
				Double d = Double.parseDouble(string);
				Double normalized = a + ((d - Xmin)*(b-a))/(Xmax - Xmin); 
				return normalized + "";
			}
		}
		return string;
	}

	public String discretize(int id,int var ,String string) {
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
					String s = null;
					switch(var){
					case(0)://Age
						s = discretDivide(var,string);
					break;
					case(1)://Height
						s = discretDivide(var,string);
					break;
					case(2)://P_norm_trial1
						s = discretPnorm(string);
					break;
					case(3)://subj_liters_1
						s = discretDivide(var,string);
					break;
					case(4)://subj_liters_2
						s = discretDivide(var,string);
					break;
					case(5)://subj_liters_3
						s = discretDivide(var,string);
					break;
					case(6)://bp_diastolic
						s = discretBPDias(string);
					break;
					case(7)://bp_systolic
						s = discretBPSyst(string);
					break;
					case(8)://pulse
						s = discretPulse(string);
					break;
					case(9)://respiratory rate
						s = discretRR(string);
					break;
					case(10)://temp
						s = discretTemp(string);
					break;
					case(11)://weight
						s = discretWeight(id,string);
					break;
					}					
					return s;
				}else{
					return "missing"; //string;
				}
			}
		}
	}
	
	private String discretDivide(int var, String string) {
		int c = (int)((Float.parseFloat(string)-mins.get(var))/divider.get(var));
		if(c==buckets){
			c=buckets-1;
		}

		String s = "B"+c;
		return s;
	}


	private String discretWeight(int id, String string) {
		String[][] matrix = getWeightMatrix(); 
		Double w = Double.parseDouble(string);
		Double h = heights.get(id+"");
		if( h == null){
			h = 169.0;
		}
		int height = (int) ((double) h);
		int weight = (int) ((double) w);

		int row = normalizeHeight(height);
		int column = normalizeWeight(weight);

		String val = matrix[20-row][column];
		//		System.out.println("Height - " + height + " Weight - " + weight + " -> " + val);
		return val;
	}

	private int normalizeWeight(int weight) {
		double Xmin = 40.0;
		double Xmax = 170.0;
		if(weight < Xmin){
			weight = (int) Xmin;
		}
		if(weight > Xmax){
			weight = (int) Xmax;
		}
		int a = 0;
		int b = 20;
		double normalized = a + ((weight - Xmin)*(b-a))/(Xmax - Xmin); 
		return (int) normalized;
	}

	private int normalizeHeight(int height) {
		double Xmin = 148.0;
		double Xmax = 200.0;
		if(height < Xmin){
			height = (int) Xmin;
		}
		if(height > Xmax){
			height = (int) Xmax;
		}
		int a = 0;
		int b = 20;
		double normalized = a + ((height - Xmin)*(b-a))/(Xmax - Xmin); 
		return (int) normalized;
	}

	private String[][] getWeightMatrix() {
		String[][] m = {{"L","L","L","L","L","L","N","N","N","N","H","H","H","VH","VH","VH","VH","VH","VH","SH","SH"},
				{"L","L","L","L","L","N","N","N","N","H","H","H","VH","VH","VH","VH","VH","VH","SH","SH","SH"},
				{"L","L","L","L","L","N","N","N","N","H","H","H","VH","VH","VH","VH","VH","VH","SH","SH","SH"},
				{"L","L","L","L","L","N","N","N","H","H","H","VH","VH","VH","VH","VH","VH","SH","SH","SH","SH"},
				{"L","L","L","L","N","N","N","N","H","H","H","VH","VH","VH","VH","VH","VH","SH","SH","SH","SH"},

				{"L","L","L","L","N","N","N","N","H","H","H","VH","VH","VH","VH","VH","SH","SH","SH","SH","SH"},
				{"L","L","L","L","N","N","N","H","H","H","VH","VH","VH","VH","VH","SH","SH","SH","SH","SH","SH"},
				{"L","L","L","L","N","N","N","H","H","H","VH","VH","VH","VH","VH","SH","SH","SH","SH","SH","SH"},
				{"L","L","L","N","N","N","H","H","H","VH","VH","VH","VH","VH","SH","SH","SH","SH","SH","SH","SH"},
				{"L","L","L","N","N","N","H","H","H","VH","VH","VH","VH","VH","SH","SH","SH","SH","SH","SH","SH"},

				{"L","L","L","N","N","N","H","H","VH","VH","VH","VH","VH","SH","SH","SH","SH","SH","SH","SH","SH"},
				{"L","L","N","N","N","H","H","H","VH","VH","VH","VH","VH","SH","SH","SH","SH","SH","SH","SH","SH"},
				{"L","L","N","N","N","H","H","VH","VH","VH","VH","VH","SH","SH","SH","SH","SH","SH","SH","SH","SH"},
				{"L","L","N","N","N","H","H","VH","VH","VH","VH","VH","SH","SH","SH","SH","SH","SH","SH","SH","SH"},
				{"L","L","N","N","N","H","H","VH","VH","VH","VH","SH","SH","SH","SH","SH","SH","SH","SH","SH","SH"},

				{"L","L","N","N","H","H","VH","VH","VH","VH","SH","SH","SH","SH","SH","SH","SH","SH","SH","SH","SH"},
				{"L","N","N","N","H","H","VH","VH","VH","VH","SH","SH","SH","SH","SH","SH","SH","SH","SH","SH","SH"},
				{"L","N","N","N","H","H","VH","VH","VH","VH","SH","SH","SH","SH","SH","SH","SH","SH","SH","SH","SH"},
				{"L","N","N","H","H","VH","VH","VH","VH","SH","SH","SH","SH","SH","SH","SH","SH","SH","SH","SH","SH"},
				{"L","N","N","H","H","VH","VH","VH","SH","SH","SH","SH","SH","SH","SH","SH","SH","SH","SH","SH","SH"},

				{"N","N","N","H","VH","VH","VH","VH","SH","SH","SH","SH","SH","SH","SH","SH","SH","SH","SH","SH","SH"},
		};
		return m;
	}

	private String discretTemp(String string) {
		Double perc = Double.parseDouble(string);
		if(perc > 37.2){
			return "tempH";
		}
		if(perc < 35){
			return "tempL";
		}else{
			return "tempN";
		}
	}

	private String discretRR(String string) {
		Double perc = Double.parseDouble(string);
		if(perc < 12){
			return "rrL";
		}
		if(perc < 20){
			return "rrN";
		}
		if(perc < 24){
			return "rrH";
		}else{
			return "rrVH";
		}
	}

	private String discretPulse(String string) {
		Double perc = Double.parseDouble(string);
		if(perc < 60){
			return "pulseL";
		}
		if(perc > 100){
			return "pulseH";
		}else{
			return "pulseN";
		}

	}

	private String discretBPSyst(String string) {
		Double perc = Double.parseDouble(string);
		if(perc < 120){
			return "bpN";
		}
		if(perc < 140){
			return "bpH1";
		}
		if(perc < 160){
			return "bpH2";
		}
		if(perc < 180){
			return "bpVH";
		}else{
			return "bpC";
		}
	}

	private String discretBPDias(String string) {
		Double perc = Double.parseDouble(string);
		if(perc < 80){
			return "bpN";
		}
		if(perc < 90){
			return "bpH1";
		}
		if(perc < 100){
			return "bpH2";
		}
		if(perc < 110){
			return "bpVH";
		}else{
			return "bpC";
		}
	}

	private String discretPnorm(String string) {
		Double perc = Double.parseDouble(string);
		if(perc>120){
			return "PNormVH";
		}
		if(perc > 100){
			return "PNormH";
		}
		if(perc > 80){
			return "PNormN";
		}	
		if(perc > 50){
			return "PNormL";
		}else{
			return "PNormVL";
		}
	}
}