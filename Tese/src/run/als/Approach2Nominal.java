package run.als;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import utils.DefaultHashMap;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.unsupervised.attribute.Remove;
import createData.ALS.CreateData;
import createData.ALS.CreateDataDiscret;

public class Approach2Nominal {
	private  String path = "C:" + File.separator + "PROACT_2013_08_27_ALL_FORMS" +
			File.separator + "FormatedData"+ File.separator + "Approach2" + File.separator;
	private  HashMap<Integer,DefaultHashMap<Integer,String>> svcAll = new HashMap<Integer,DefaultHashMap<Integer,String>>();
	private  HashMap<Integer,DefaultHashMap<Integer,String>> vitalsAll = new HashMap<Integer,DefaultHashMap<Integer,String>>();
	private  HashMap<Integer,DefaultHashMap<Integer,String>> demoAll = new HashMap<Integer,DefaultHashMap<Integer,String>>();
	private  DefaultHashMap<String, String> heights = new DefaultHashMap<String, String>("");
	
	private static int steps = 5;
	private  int folds = 10;
	

	long[] vitalsTime = new long[6];
	long[] svcTime = new long[4];
	long[] demoTime = new long[3];
	static long start,vitals,SVC,predictions,startClassification, endNaive, endRF,endDT, endAdaboost, endLogistic;
	public static void main(String[] args){

		try {
			nominal();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void nominal() throws IOException, Exception,
			FileNotFoundException {
		Approach2Nominal a = new Approach2Nominal();
		start = System.currentTimeMillis();
		//System.out.println("start: " + start);
		
		vitals = System.currentTimeMillis();
		a.predictVitalsNominal(new J48());
		SVC = System.currentTimeMillis();
		a.predictSVCNominal(new J48());
		a.predictDemoNominal(new J48());
		predictions = System.currentTimeMillis();
		a.buildDataWithPredictionsNominal();
		a.evaluatePredictionsNominal();
		startClassification = System.currentTimeMillis();
		a.ClassifyData(new NaiveBayes());
		endNaive = System.currentTimeMillis();
		a.buildConfussionMatrix("Naive Bayes");
		a.ClassifyData(new RandomForest());
		endRF = System.currentTimeMillis();
		a.buildConfussionMatrix("RandomForest");
		a.ClassifyData(new J48());
		endDT = System.currentTimeMillis();
		a.buildConfussionMatrix("J48");
		a.ClassifyData(new AdaBoostM1());
		endAdaboost = System.currentTimeMillis();
		a.buildConfussionMatrix("AdaBoost");
		a.ClassifyData(new Logistic());
		endLogistic = System.currentTimeMillis();
		a.buildConfussionMatrix("Logistic");
		
		a.writeTimes();
		System.out.println("------------------\tDiagnostic\t------------------");
								a.ClassifyDiagnostic(new NaiveBayes());
		//			a.ClassifyDiagnostic(new J48());
		//			a.ClassifyDiagnostic(new AdaBoostM1());
		//			a.ClassifyDiagnostic(new MultilayerPerceptron());
		//			a.ClassifyDiagnostic(new Logistic());
	}

	private void evaluatePredictionsNominal() throws IOException {
		Integer[] examsSVC = {2,5,6,7};
		Integer[] examsVitals = {2,3,6,7,8,9};
		Integer[] examsDemo = {1,2,3};
		String[] exams = {"1_Demo","2_Demo","3_Demo","2_SVC","5_SVC","6_SVC","7_SVC","2_Vitals","3_Vitals","6_Vitals","7_Vitals","8_Vitals","9_Vitals"};
		ArrayList<String> examsIndexes = new ArrayList<String>();
		for(int i:examsDemo){
			examsIndexes.add(i+"_Demo");
		}
		for(int i:examsSVC){
			examsIndexes.add(i+"_SVC");
		}
		for(int i:examsVitals){
			examsIndexes.add(i+"_Vitals");
		}
		//Instances real = new Instances(new BufferedReader(new FileReader(path+"DiagnoseDataReal.arff")));
		//String[] exams = {/*"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL",*/"ALB","CHE"/*,"T-CHO","TP"*/,"Type"};//,"Activity"};
		BufferedReader real = new BufferedReader(new FileReader(path+"DiagnoseDataReal.csv"));
		real.readLine();
		String line = null;
		HashMap<String,Integer> correct = new HashMap<String, Integer>();
		HashMap<String,Integer> missing = new HashMap<String, Integer>();
		for(String s:exams){
			correct.put(s,0);
			missing.put(s, 0);
		}
		int total = 0;
		while((line = real.readLine()) != null){
			String[] split = line.split(",",-1);
			String id = split[0];
			int ID = Integer.parseInt(id);
			for(int i= 0; i<examsDemo.length;i++){
				DefaultHashMap<Integer, String> e = demoAll.get(examsDemo[i]);
				//				System.out.println("prediction - "+ e.get(ID));
				//				System.out.println("real       - " + split[1+examsIndexes.indexOf(examsDemo[i]+"_Demo")]);
				//				System.out.println("-----------------");
				if(split[1+examsIndexes.indexOf(examsDemo[i]+"_Demo")].equals(e.get(ID))){
					Integer count = correct.get(examsDemo[i]+"_Demo");
					count++;
					correct.put(examsDemo[i]+"_Demo",count);
				}else{
					if(split[1+examsIndexes.indexOf(examsDemo[i]+"_Demo")].equals("") || e.get(ID).equals("") ){
						Integer count = missing.get(examsDemo[i]+"_Demo");
						count++;
						missing.put(examsDemo[i]+"_Demo",count);
					}
				}
//				else{
//					System.out.println(ID + " --------"+examsDemo[i]+"---------");
//					System.out.println("prediction - "+ e.get(ID));
//					System.out.println("real       - " + split[1+examsIndexes.indexOf(examsDemo[i]+"_Demo")]);
//				}
			}
			for(int i= 0; i<examsSVC.length;i++){
				DefaultHashMap<Integer, String> e = svcAll.get(examsSVC[i]);
				if(split[1+examsIndexes.indexOf(examsSVC[i]+"_SVC")].equals(e.get(ID))){
					Integer count = correct.get(examsSVC[i]+"_SVC");
					count++;
					correct.put(examsSVC[i]+"_SVC",count);
				}else{
					if(split[1+examsIndexes.indexOf(examsSVC[i]+"_SVC")].equals("") || e.get(ID).equals("") ){
						Integer count = missing.get(examsSVC[i]+"_SVC");
						count++;
						missing.put(examsSVC[i]+"_SVC",count);
					}
				}
			}
			for(int i= 0; i<examsVitals.length;i++){
				DefaultHashMap<Integer, String> e = vitalsAll.get(examsVitals[i]);
				//System.out.println(e.get(ID));
				if(split[1+examsIndexes.indexOf(examsVitals[i]+"_Vitals")].equals(e.get(ID))){
					Integer count = correct.get(examsVitals[i]+"_Vitals");
					count++;
					correct.put(examsVitals[i]+"_Vitals",count);
				}else{
					if(split[1+examsIndexes.indexOf(examsVitals[i]+"_Vitals")].equals("") || e.get(ID).equals("") ){
						Integer count = missing.get(examsVitals[i]+"_Vitals");
						count++;
						missing.put(examsVitals[i]+"_Vitals",count);
					}
				}
			}
			total++;
		}
		real.close();
		System.out.println("\nTotal predictions ->\t" + total);
		double average = 0;
		DecimalFormat df = new DecimalFormat("#.##");
		for(String exam:exams){
			Integer count = correct.get(exam);
			double perc = (((double)count)/(total  - missing.get(exam))) *100;
			average += perc;
			System.out.println(exam + "\t->\t"+ /*count+" / "+ total+ "\t"+*/ df.format(perc) + " %" );
		}
		System.out.println("\nAverage - "+ df.format(average/exams.length) + " %");	
	}

	private void writeTimes() throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(path+"AP2_ALStimes_"+steps+".csv"));
		int[] vitalsIndex = {2,3,6,7,8,9};
		int[] svcIndex = {2,5,6,7};
		
		out.write("Overall Time"+"\n");
		out.write((predictions-start) + "\n\n");
		int i = 0;
		long prevL = vitals;
		for(long l:vitalsTime){
			out.write("vitals"+vitalsIndex[i]+";"+(l-prevL)+"\n");
			prevL = l;
			i++;
		}
		i = 0;
		prevL = SVC;
		for(long l:svcTime){
			out.write("svc"+svcIndex[i]+";"+(l-prevL)+"\n");
			prevL = l;
			i++;
		}
		out.write("\nClassifications\n\n");
		out.write("Naive"+";" + (endNaive - startClassification) + "\n");
		out.write("RandomForest"+";" + (endRF - endNaive) + "\n");
		out.write("DT"+";" + (endDT - endRF) + "\n");
		out.write("AdaBoost"+";" + (endAdaboost - endDT) + "\n");
		out.write("Logistics"+";" + (endLogistic - endAdaboost) + "\n");
		out.close();
	}

	private  void ClassifyDiagnostic(Classifier classifier) {
		System.out.println("Classify Diagnostic");
		try{
			Instances data = new Instances(new BufferedReader(new FileReader(path+"DiagnoseData.arff")));
			//
			// Create Classifier
			Remove remove = new Remove();                         // new instance of filter
			remove.setAttributeIndices("1");					// set options
			FilteredClassifier cModel = new FilteredClassifier();
			//			Classifier cModel = classifier;
			cModel.setFilter(remove);
			cModel.setClassifier(classifier);
			// train and make predictions
			//			Classifier cModel = classifier;   
			data.setClassIndex(data.numAttributes() - 1);
			Evaluation eval = new Evaluation(data);
			eval.crossValidateModel(cModel, data, 10, new Random(1));

			// Print the result à la Weka explorer:
			String strSummary = eval.toSummaryString();
			//				System.out.println(cModel.toString());

			//Get the confusion matrix
			//			double[][] matrix = eval.confusionMatrix();
			//			System.out.println("\n\t\t"+ classifier.getClass().toString());
			System.out.println(strSummary);
			//			String[] val = {"{36-48}","{24-36}","{12-24}","{0-12}"};
			//			System.out.println("\t\t{36-48}\t{24-36}\t{12-24}\t{0-12}");
			//			for(int i=0; i<4;i++){
			//				System.out.print(val[i] + "\t|");
			//				for(int j= 0;j<4;j++){
			//					System.out.print(matrix[i][j]+ "\t\t|");
			//				}
			//				System.out.println();
			//			}
			//			double tru = 0;
			//			double fal = 0;
			//			double bad = 0;
			//			for(int i=0; i<4;i++){
			//				for(int j= 0;j<4;j++){
			//					if(i==j){
			//						tru +=matrix[i][j]; 
			//					}else{
			//						bad +=matrix[i][j];
			//					}				
			//					fal += matrix[i][j];
			//				}
			//			}
			//			double accuracy = tru/fal;
			//			double errorRate = bad/fal;
			//			System.out.println("\nCorrectly Classified Instances\t\t"+ tru +"\t\t" + (accuracy*100) + " %");
			//			System.out.println("Inorrectly Classified Instances\t\t"+ bad +"\t\t" + (errorRate*100) + " %");
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	private  int[][] buildConfussionMatrix(String method) throws FileNotFoundException, IOException {
		System.out.println("build Confusion Matrix");
		Instances labeled = new Instances(new BufferedReader(new FileReader(path+"labeled.arff")));
		Instances real = new Instances(new BufferedReader(new FileReader(path+"DiagnoseDataReal.arff")));
		HashMap<String,Integer> indexes = new HashMap<String, Integer>();
		indexes.put("\'{0-12}\'",0);
		indexes.put("\'{12-24}\'",1);
		indexes.put("\'{24-36}\'",2);
		indexes.put("\'{36-48}\'",3); 
		int[][] matrix = new int[4][4];

		for(int i= 0; i<labeled.numInstances(); i++){
			Instance ins = labeled.get(i);
			String[] str = ins.toString().split(",",-1);
			for(int j= 0; j<real.numInstances(); j++){
				Instance ins2 = real.get(j);
				String[] str2 = ins2.toString().split(",",-1);
				if(str2[0].equals(str[0])){
					if(str2[ins2.numAttributes()-1].equals("?") || str[ins2.numAttributes()-1].equals("?")){
						break;
					}
					matrix[indexes.get(str2[ins2.numAttributes()-1])][indexes.get(str[ins2.numAttributes()-1])]++;
					break;
				}
			}
		}		
		System.out.println("\n\t\t"+ method+ "\n");
		String[] val = {"{0-12}","{12-24}","{24-36}","{36-48}"};
		System.out.println("\t\t{0-12}\t{12-24}\t{24-36}\t{36-48}");
		for(int i=0; i<4;i++){
			System.out.print(val[i] + "\t|");
			for(int j= 0;j<4;j++){
				System.out.print(matrix[i][j]+ "\t|");
			}
			System.out.println();
		}
		double tru = 0;
		double fal = 0;
		double bad = 0;
		for(int i=0; i<4;i++){
			for(int j= 0;j<4;j++){
				if(i==j){
					tru +=matrix[i][j]; 
				}else{
					bad +=matrix[i][j];
				}				
				fal += matrix[i][j];
			}
		}
		double accuracy = tru/fal;
		double errorRate = bad/fal;
		DecimalFormat df = new DecimalFormat("#.#####");
		System.out.println("\nCorrectly Classified Instances\t"+ tru +"\t\t" + df.format(accuracy*100) + " %");
		System.out.println("Incorrectly Classified Instances\t"+ bad +"\t\t" + df.format(errorRate*100) + " %");
		return matrix;
	}

	private  void ClassifyData(Classifier classifier) throws Exception {
		System.out.println("Classify Data");
		Instances train = new Instances(new BufferedReader(new FileReader(path+"DiagnoseData.arff")));
		//		CSVLoader loader = new CSVLoader();
		//		loader.setSource(new File(path+"PredictionDataWithDemo.csv"));
		//		Instances test = loader.getDataSet();
		Instances test = new Instances(new BufferedReader(new FileReader(path+"PredictionDataWithDemo"+steps+".arff")));

		// Set class index
		train.setClassIndex(train.numAttributes() - 1);
		test.setClassIndex(test.numAttributes() - 1);

		Instances labeled = new Instances(test);

		Remove remove = new Remove();                         // new instance of filter
		remove.setAttributeIndices("1");					// set options
		// Create Classifier
		FilteredClassifier cModel = new FilteredClassifier();
		cModel.setFilter(remove);
		cModel.setClassifier(classifier);
		// train and make predictions
		cModel.buildClassifier(train);

		// Test the model
		Evaluation eTest = new Evaluation(test);
		eTest.evaluateModel(cModel, test);

		// Print the result à la Weka explorer:
		//		String strSummary = eTest.toSummaryString();
		//		System.out.println(cModel.toString());
		//		System.out.println(strSummary);
		//		System.out.println("--------------------------------");

		for (int i = 0; i < test.numInstances(); i++) {
			double clsLabel = cModel.classifyInstance(test.instance(i));
			labeled.instance(i).setClassValue(clsLabel);
		}

		BufferedWriter writer = new BufferedWriter(
				new FileWriter(path+"labeled.arff"));
		writer.write(labeled.toString());
		writer.newLine();
		writer.flush();
		writer.close();

		//		for(Instance i:test){
		//			System.out.print(i.toString());
		//			Double d = cModel.classifyInstance(i);
		//			System.out.println("\t-> "+d);
		//		}
	}

	private  void buildDataWithPredictionsNominal() throws IOException {
		System.out.println("build Data With Predictions Nominal");
		String Demo = path +  "Demo_Data.csv";

		BufferedReader inDemo = new BufferedReader(new FileReader(Demo));
		BufferedReader diag = new BufferedReader(new FileReader(path +"DiagnoseData.csv"));
		String header = diag.readLine();
		diag.close();
		BufferedWriter outDemo = new BufferedWriter(new FileWriter(path+"PredictionDataWithDemo.csv"));
		BufferedWriter outNoDemo = new BufferedWriter(new FileWriter(path+"PredictionDataWithoutDemo.csv"));


		String[] heads = header.split(",");
		String headerDemo = heads[0]+ ","+heads[1]+ ","+heads[2]+ ","+heads[3]+ "," +heads[4]+ "," +heads[5]+ "," +heads[6]+ "," +heads[7]+ "," +heads[8]+ "," +heads[9]
				+ "," +heads[10]+ ","+ heads[11]+ "," +heads[12]+ "," +heads[13]+ "," +heads[14] ;
		String headerNo = heads[0]+ "," +heads[4]+ "," +heads[5]+ "," +heads[6]+ "," +heads[7]+ "," +heads[8]+ "," +heads[9]
				+ "," +heads[10]+ ","+ heads[11]+ "," +heads[12]+ "," +heads[13]+ "," +heads[14] ;
		outDemo.write(headerDemo+ '\n');
		outNoDemo.write(headerNo + '\n');
		String line1, patientWith,patientWithout;

		getHeight();

		line1 = inDemo.readLine();

		HashMap<Integer, String> svc2 = svcAll.get(2);
		HashMap<Integer, String> svc5 = svcAll.get(5); 
		HashMap<Integer, String> svc6 = svcAll.get(6); 
		HashMap<Integer, String> svc7 = svcAll.get(7);

		HashMap<Integer, String> vitals2 = vitalsAll.get(2);
		HashMap<Integer, String> vitals3 = vitalsAll.get(3);
		HashMap<Integer, String> vitals6 = vitalsAll.get(6);
		HashMap<Integer, String> vitals7 = vitalsAll.get(7);
		HashMap<Integer, String> vitals8 = vitalsAll.get(8);
		HashMap<Integer, String> vitals9 = vitalsAll.get(9);

		HashMap<Integer, String> demo1 = demoAll.get(1);
		HashMap<Integer, String> demo2 = demoAll.get(2);
		HashMap<Integer, String> demo3 = demoAll.get(3);

		//SubjectID	Sex	Age	Height	P_of_Normal_(Trial_1)	Subject_Liters_(Trial_1)	Subject_Liters_(Trial_2)	Subject_Liters_(Trial_3)	Blood_Pressure_(Diastolic)	Blood_Pressure_(Systolic)	Pulse	Respiratory_Rate	Temperature	Weight	ALSFRS-R_Total
		while((line1 = inDemo.readLine()) != null){
			String[] split = line1.split(",",-1);
			int id = Integer.parseInt(split[0]);
			patientWith = id+"," + demo1.get(id) +"," + demo2.get(id)+"," + demo3.get(id)+"," ;
			patientWithout = id + ","; 
			patientWith += svc2.get(id) + "," + svc5.get(id) + "," + svc6.get(id) + "," + svc7.get(id) + ",";
			patientWith += vitals2.get(id) +"," +vitals3.get(id) +"," +vitals6.get(id) +"," +vitals7.get(id) +"," +vitals8.get(id) +"," +vitals9.get(id);
			patientWithout += svc2.get(id) + "," + svc5.get(id) + "," + svc6.get(id) + "," + svc7.get(id) + ",";
			patientWithout += vitals2.get(id) +"," +vitals3.get(id) +"," +vitals6.get(id) +"," +vitals7.get(id) +"," +vitals8.get(id) +"," +vitals9.get(id);

			outDemo.write(patientWith +","+'\n');
			outNoDemo.write(patientWithout +","+'\n');
		}
		inDemo.close();
		outDemo.close();
		outNoDemo.close();
		CreateData create = new CreateData();
		create.CSV2arff(path,"PredictionDataWithDemo");
		create.CSV2arff(path,"PredictionDataWithoutDemo");
		File a = new File(path + "PredictionDataWithDemo.csv" );
		a.delete();
		a = new File(path + "PredictionDataWithoutDemo.csv" );
		a.delete();
		ChangeClassPreditions();
	}

	private  void ChangeClassPreditions() {
		System.out.println("changing class");
		try {
			BufferedReader data = new BufferedReader(new FileReader(path +"PredictionDataWithDemo.arff"));
			BufferedWriter out = new BufferedWriter(new FileWriter(path + "PredictionDataWithDemo"+steps+".arff"));
			String line;
			while((line = data.readLine()) != null){
				if(line.contains("@attribute ALSFRS-RTotal string")){
					out.write("@attribute ALSFRS-RTotal {'{36-48}','{24-36}','{12-24}','{0-12}'}\n");
				}
				else{
					out.write(line+ '\n');
				}
			}
			data.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private  void getHeight() throws IOException {

		BufferedReader inVitals = new BufferedReader(new FileReader(path+steps+"_VITALS_DATA.csv"));
		String line1 = inVitals.readLine();

		String last = null;
		while((line1 = inVitals.readLine()) != null){
			String[] split = line1.split(",",-1);
			if(null == last){
				last = split[0];
				heights.put(last, split[4]);
			}else{
				if(last.equals(split[0])){
					continue;
				}else {
					last = split[0];
					heights.put(last, split[4]);
				}
			}
		}
		inVitals.close();
	}

	private void predictDemoNominal(Classifier c) throws IOException {
		int[] demoIndex = {1,2,3};
		HashMap<String,HashMap<Double,String>> indexes = getIndexes(demoIndex,"Demo");
		try{
			int o = 0;
			for(int index:demoIndex){
				CSVLoader svcLoader = new CSVLoader();
				svcLoader.setSource(new File(path+"approach2_Demo"+index+"_"+steps+".csv"));
				Instances svcData = svcLoader.getDataSet();
				int seed = 1;

				Random rand = new Random(seed);   // create seeded number generator
				Instances randData = new Instances(svcData);   // create copy of original data
				randData.randomize(rand); 

				for (int n = 0; n < folds; n++) {
					Instances train = randData.trainCV(folds, n);
					Instances test = randData.testCV(folds, n);

					// Set class index
					train.setClassIndex(train.numAttributes() - 1);
					test.setClassIndex(test.numAttributes() - 1);

					// Create Classifier
					Classifier cModel = (Classifier) c;   
					cModel.buildClassifier(train);

					// Test the model
					Evaluation eTest = new Evaluation(test);
					eTest.evaluateModel(cModel, test);

					// Print the result à la Weka explorer:
//					String strSummary = eTest.toSummaryString();
					//					System.out.println(cModel.toString());
					//					System.out.println(strSummary);
					//					System.out.println("--------------------------------");
					DefaultHashMap<Integer,String> demo = demoAll.get(index);
					if(demo == null){
						demo = new DefaultHashMap<Integer, String>("");
					}
					for(Instance i:test){
						//						System.out.print(i.toString());
						Double d = cModel.classifyInstance(i);
						//						System.out.println("\t-> "+d);
						String ins = (i.toString().split(","))[0];
						demo.put(Integer.parseInt(ins) , indexes.get(index+"").get(d));
					}
					demoAll.put(index, demo);
				}
				demoTime[o] = System.currentTimeMillis();
				o++;
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private  void predictVitalsNominal(Classifier c) throws IOException {
		int[] vitalsIndex = {2,3,6,7,8,9};
		HashMap<String,HashMap<Double,String>> indexes = getIndexes(vitalsIndex,"Vitals");
		System.out.println(indexes.get(9+""));
		try{
			int o = 0;
			for(int index:vitalsIndex){
				CSVLoader loader = new CSVLoader();
				loader.setSource(new File(path+"approach2_Vitals"+index+"_"+steps+".csv"));
				Instances svcData = loader.getDataSet();
				int seed = 1;

				Random rand = new Random(seed);   // create seeded number generator
				Instances randData = new Instances(svcData);   // create copy of original data
				randData.randomize(rand); 

				for (int n = 0; n < folds; n++) {
					Instances train = randData.trainCV(folds, n);
					Instances test = randData.testCV(folds, n);

					// Set class index
					train.setClassIndex(train.numAttributes() - 1);
					test.setClassIndex(test.numAttributes() - 1);

					// Create Classifier
					Classifier cModel = (Classifier) c;   
					cModel.buildClassifier(train);

					// Test the model
					Evaluation eTest = new Evaluation(test);
					eTest.evaluateModel(cModel, test);

					// Print the result à la Weka explorer:
//					String strSummary = eTest.toSummaryString();
					//					System.out.println(cModel.toString());
					//					System.out.println(strSummary);
					//					System.out.println("--------------------------------");
					DefaultHashMap<Integer,String> vitals = vitalsAll.get(index);
					if(vitals == null){
						vitals = new DefaultHashMap<Integer, String>("");
					}
					for(Instance i:test){
						//						System.out.print(i.toString());
						Double d = cModel.classifyInstance(i);
						//						System.out.println("\t-> "+d);
						String ins = (i.toString().split(","))[0];
						vitals.put(Integer.parseInt(ins) , indexes.get(index+"").get(d));
					}

					vitalsAll.put(index, vitals);
				}
				vitalsTime[o] = System.currentTimeMillis();
				o++;
			}

		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	private  void predictSVCNominal(Classifier c) throws IOException {
		int[] svcIndex = {2,5,6,7};
		HashMap<String,HashMap<Double,String>> indexes = getIndexes(svcIndex,"SVC");
		try{
			int o = 0;
			for(int index:svcIndex){
				CSVLoader svcLoader = new CSVLoader();
				svcLoader.setSource(new File(path+"approach2_SVC"+index+"_"+steps+".csv"));
				Instances svcData = svcLoader.getDataSet();
				int seed = 1;

				Random rand = new Random(seed);   // create seeded number generator
				Instances randData = new Instances(svcData);   // create copy of original data
				randData.randomize(rand); 

				for (int n = 0; n < folds; n++) {
					Instances train = randData.trainCV(folds, n);
					Instances test = randData.testCV(folds, n);

					// Set class index
					train.setClassIndex(train.numAttributes() - 1);
					test.setClassIndex(test.numAttributes() - 1);

					// Create Classifier
					Classifier cModel = (Classifier) c;   
					cModel.buildClassifier(train);

					// Test the model
					Evaluation eTest = new Evaluation(test);
					eTest.evaluateModel(cModel, test);

					// Print the result à la Weka explorer:
//					String strSummary = eTest.toSummaryString();
					//					System.out.println(cModel.toString());
					//					System.out.println(strSummary);
					//					System.out.println("--------------------------------");
					DefaultHashMap<Integer,String> svc = svcAll.get(index);
					if(svc == null){
						svc = new DefaultHashMap<Integer, String>("");
					}
					for(Instance i:test){
						//						System.out.print(i.toString());
						Double d = cModel.classifyInstance(i);
						//						System.out.println("\t-> "+d);
						String ins = (i.toString().split(","))[0];
						svc.put(Integer.parseInt(ins) , indexes.get(index+"").get(d));
					}
					svcAll.put(index, svc);
				}
				svcTime[o] = System.currentTimeMillis();
				o++;
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	private HashMap<String, HashMap<Double, String>> getIndexes(int[] vitalsIndex,String type) throws IOException {
		HashMap<String, HashMap<Double, String>> indexes = new HashMap<String, HashMap<Double,String>>();
		for(int exam: vitalsIndex){
			CreateDataDiscret c = new CreateDataDiscret();
			c.CSV2arff(path,"approach2_"+type+exam+"_"+steps);
			BufferedReader arff = new BufferedReader(new FileReader(path +"approach2_"+type+exam+"_"+steps+".arff"));
			int skip = vitalsTime.length + svcTime.length + demoTime.length;
			for(int i=0; i< 3+(steps-1)*skip; i++){
				arff.readLine();
			}
			String line = arff.readLine();
			line = line.split(" ")[2];
			line = line.substring(1,line.length()-1);
			String[] split = line.split(",");
			HashMap<Double, String> e = new HashMap<Double, String>();
			for(int i = 0; i< split.length ; i++){
				e.put((double)i, split[i]);
			}
			indexes.put(exam+"", e);
			arff.close();
		}
		return indexes;
	}
	
}