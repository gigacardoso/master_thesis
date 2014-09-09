package tdm.classification.run.power;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;

import tdm.classification.createData.ALS.CreateData;
import tdm.classification.createData.Power.TableGeneratorDiscret;
import tdm.classification.createData.Power.TableGeneratorDiscretEDP;
import tdm.classification.utils.DefaultHashMap;
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

public class Approach1DiscretEDP {
	private  String path = "C:" + File.separator + "Power" + File.separator;
	private  HashMap<String,DefaultHashMap<Integer,String>> examsAll = new HashMap<String,DefaultHashMap<Integer,String>>();
	private HashMap<String, String> discret_symbols = new HashMap<String, String>();
	private  int steps = TableGeneratorDiscretEDP.steps;
	private  int folds = 10;
	private String[] final_class = {"Simple", "Bi", "Tri"};

	long[] examsTime;
	static long start,vitals,SVC,predictions,startClassification, endNaive, endRF,endDT, endAdaboost, endLogistic;

	public static void main(String[] args){
		try {
			System.out.println("Approach1Discret - EDP");
			System.out.println("Steps\t- " + TableGeneratorDiscret.steps);
			System.out.println("Num Buckets\t- " + TableGeneratorDiscret.num_buckets);
			System.out.println("Days\t- " + TableGeneratorDiscret.days);
			
			Approach1DiscretEDP a = new Approach1DiscretEDP();
			a.examsTime = new long[a.getVariables().length];
			a.examsAll = new HashMap<String,DefaultHashMap<Integer,String>>();

			start = System.currentTimeMillis();
			a.predictExams(new Logistic());

			a.readClasses();

			//			for (String string : a.examsAll.keySet()) {
			//				DefaultHashMap<Integer, String> b = a.examsAll.get(string);
			//				System.out.println(string);
			//				for (Integer i : b.keySet()) {
			//					String c = b.get(i);
			//					System.out.println("\t" + c);
			//				}
			//			}


			predictions = System.currentTimeMillis();
			a.buildDataWithPredictions();
			a.evaluatePredictionsNominal();

			a.changeClassesInAttributes("diagnosis_discret.arff");
			a.changeClassesInAttributes("diagnosis_discret_real.arff");
			a.changeClassesInAttributes("PredictionData.arff");

			//			startClassification = System.currentTimeMillis();
			//			
									a.ClassifyData(new NaiveBayes());
									endNaive = System.currentTimeMillis();
									a.buildConfussionMatrix("Naive Bayes");
			
									a.ClassifyData(new RandomForest());
									endRF = System.currentTimeMillis();
									a.buildConfussionMatrix("RandomForest");

			a.ClassifyData(new J48());
			endDT = System.currentTimeMillis();
			a.buildConfussionMatrix("J48");
			//			
						a.ClassifyData(new AdaBoostM1());
						endAdaboost = System.currentTimeMillis();
						a.buildConfussionMatrix("AdaBoost");
						
						a.ClassifyData(new Logistic());
						endLogistic = System.currentTimeMillis();
						a.buildConfussionMatrix("Logistic");
			//
			//			a.writeTimes();
			//			System.out.println("------------------\tDiagnostic\t------------------");
			//			a.ClassifyDiagnostic(new NaiveBayes());
			//						a.ClassifyDiagnostic(new J48());
			//			a.ClassifyDiagnostic(new AdaBoostM1());
			//			a.ClassifyDiagnostic(new Logistic());
			//			a.ClassifyDiagnostic(new RandomForest());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void readClasses() throws IOException {
		String[] exams = getVariables();
		int num = TableGeneratorDiscret.num_buckets;
		for (String exam : exams) {
			String s = "{";
			for (int i = 0; i < num; i++) {
				s += "["+exam+"_"+i+"],";
			}
			s = s.substring(0, s.length()-1);
			s += "}";
			discret_symbols.put(exam, s);
		}
	}
	private void changeClassesInAttributes(String file) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(path + file));
		BufferedWriter out = new BufferedWriter(new FileWriter(path + file+"tmp"));
		String line;
		for(int i=0;i<4;i++){
			out.write(in.readLine()+ '\n');
		}
		
		String class_att = "@attribute RecommendedRate {";
		for (int i = 0; i < final_class.length; i++) {
			class_att += final_class[i]+",";
		}
		class_att = class_att.substring(0,class_att.length()-1);
		class_att += "}";
//		System.out.println(class_att);
		while((line = in.readLine()) != null){
			if (line.contains("@attribute")){
				if(line.contains("@attribute RecommendedRate")){
					out.write(class_att+"\n");
				}else{
					String feat = line.split(" ")[1];
					out.write("@attribute "+ feat + " " + discret_symbols.get(feat) + '\n');
				}
			}else{
				out.write(line+ '\n');
			}
		}
		in.close();
		out.close();
		File source = new File(path + file+"tmp");
		File dest = new File(path + file);
		dest.delete();
		Files.copy(source.toPath(),dest.toPath());
		source.delete();
	}

	@SuppressWarnings("unused")
	private void writeTimes() throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(path+"AP1_Power_"+steps+".csv"));
		String[] exams = getVariables();

		out.write("Overall Time"+"\n");
		out.write((predictions-start) + "\n\n");
		int i = 0;
		long prevL = vitals;
		for(long l:examsTime){
			out.write(exams[i]+";"+(l-prevL)+"\n");
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

	private void evaluatePredictionsNominal() throws FileNotFoundException, IOException {
		String[] exams = getVariables();
		//String[] exams = {/*"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL",*/"ALB","CHE"/*,"T-CHO","TP"*/,"Type"};//,"Activity"};
		BufferedReader real = new BufferedReader(new FileReader(path+"diagnosis_discret_real.csv"));
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
			Integer id = Integer.parseInt(split[0]);
			for(int i= 0; i<exams.length;i++){
				DefaultHashMap<Integer, String> e = examsAll.get(exams[i]);
				if(split[(1+i)].equals(e.get(id))){
					Integer count = correct.get(exams[i]);
					if(count == null){
						count = 1;
					}else{
						count++;
					}
					correct.put(exams[i],count);
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

	@SuppressWarnings("unused")
	private  void ClassifyDiagnostic(Classifier classifier) {
		System.out.println("Classify Diagnostic");
		try{
			Instances data = new Instances(new BufferedReader(new FileReader(path+"diagnosis_discret.arff")));
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

	private  void buildConfussionMatrix(String method) throws FileNotFoundException, IOException {
		System.out.println("build Confusion Matrix ");
		Instances labeled = new Instances(new BufferedReader(new FileReader(path+"labeled.arff")));
		Instances real = new Instances(new BufferedReader(new FileReader(path+"diagnosis_discret_real.arff")));
		HashMap<String,Integer> indexes = new HashMap<String, Integer>();
		String[] classes = final_class;
		int u=0;
		for(String c:classes){
			indexes.put(c,u);
			u++;
		}
		int[][] matrix = new int[classes.length][classes.length];

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
		String[] val = classes;
		for(String c:classes){
			System.out.print("\t"+c);
		}
		System.out.println();
		for(int i=0; i<classes.length;i++){
			System.out.print(val[i] + "\t|");
			for(int j= 0;j<classes.length;j++){
				System.out.print(matrix[i][j]+ "\t|");
			}
			System.out.println();
		}
		double tru = 0;
		double fal = 0;
		double bad = 0;
		for(int i=0; i<classes.length;i++){
			for(int j= 0;j<classes.length;j++){
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

	}

	private  void ClassifyData(Classifier classifier) throws Exception {
		System.out.println("Classify Data");
		//		changeClassesInAttributes("DiagnoseData.arff","DiagnoseDataNominal.arff");
		//		changeClassesInAttributes("PredictionDataWithDemo"+steps+".arff","PredictionDataWithDemoNominal"+steps+".arff");
		Instances train = new Instances(new BufferedReader(new FileReader(path+"diagnosis_discret.arff")));
		//		CSVLoader loader = new CSVLoader();
		//		loader.setSource(new File(path+"PredictionDataWithDemo.csv"));
		//		Instances test = loader.getDataSet();
		Instances test = new Instances(new BufferedReader(new FileReader(path+"PredictionData.arff")));

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

		//		BufferedWriter m = new BufferedWriter(new FileWriter(path+"model.txt"));
		//		m.write(cModel.toString());
		//		m.close();

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

	private  void buildDataWithPredictions() throws IOException {
		System.out.println("build Data With Predictions");
		String[] exams = getVariables();
		BufferedReader diag = new BufferedReader(new FileReader(path +"diagnosis_discret_real.csv"));

		BufferedWriter outDemo = new BufferedWriter(new FileWriter(path+"PredictionData.csv"));
		String header = diag.readLine();
		String line1, patientWith;
		outDemo.write(header + "\n");
		//		ArrayList<HashMap<Integer, String>> examss = new ArrayList<HashMap<Integer,String>>();
		//		for (int i = 0; i < exams.length; i++) {
		//			examss.add(examsAll.get(exams[i]));
		//		}

		//SubjectID	Sex	Age	Height	P_of_Normal_(Trial_1)	Subject_Liters_(Trial_1)	Subject_Liters_(Trial_2)	Subject_Liters_(Trial_3)	Blood_Pressure_(Diastolic)	Blood_Pressure_(Systolic)	Pulse	Respiratory_Rate	Temperature	Weight	ALSFRS-R_Total
		while((line1 = diag.readLine()) != null){
			String[] split = line1.split(",",-1);
			Integer id = Integer.parseInt(split[0]);
			patientWith = split[0]+",";
			for (int i = 0; i < exams.length; i++) {
				patientWith += examsAll.get(exams[i]).get(id) +",";
			}
			outDemo.write(patientWith +'\n');
		}
		diag.close();
		outDemo.close();
		CreateData create = new CreateData();
		create.CSV2arff(path,"PredictionData");
		//ChangeClassPreditions();
	}

//	private  void ChangeClassPreditions() {
//		System.out.println("changing class");
//		String class_att = "@attribute RecommendedRate_class {";
//		for (int i = 0; i < final_class.length; i++) {
//			class_att += final_class[i]+",";
//		}
//		class_att = class_att.substring(1,class_att.length()-1);
//		class_att += "}";
//		try {
//			BufferedReader data = new BufferedReader(new FileReader(path +"PredictionData.arff"));
//			BufferedWriter out = new BufferedWriter(new FileWriter(path + "PredictionData"+steps+".arff"));
//			String line;
//			while((line = data.readLine()) != null){
//				if(line.contains("@attribute RecommendedRate_class")){
//					// o numero de classes pode variar
//					out.write(class_att+"\n");
//				}
//				else{
//					out.write(line+ '\n');
//				}
//			}
//			data.close();
//			out.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	private  void predictExams(Classifier c) throws IOException {
		String[] exams = getVariables();
		HashMap<String,HashMap<Double,String>> indexes = getIndexes(exams);
//		int count = 0;
		try{
			int o = 0;
			for(String exam:exams){
				CSVLoader loader = new CSVLoader();
				loader.setSource(new File(path+exam+".csv"));
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
					//					Evaluation eTest = new Evaluation(test);
					//					eTest.evaluateModel(cModel, test);
					// Print the result à la Weka explorer:
					//					String strSummary = eTest.toSummaryString();
					//					System.out.println(cModel.toString());
					//					System.out.println(strSummary);
					//					System.out.println("--------------------------------");
					DefaultHashMap<Integer,String> vitals = examsAll.get(exam);
					if(vitals == null){
						vitals = new DefaultHashMap<Integer, String>("");
					}
					for(Instance i:test){
						//						System.out.print(i.toString());
						Double d = cModel.classifyInstance(i);
						//						System.out.println("\t-> "+d);
						String ins = (i.toString().split(","))[0];
//						int inteiro = (int) Math.round(d);
//						String att = train.attribute(train.numAttributes() - 1).value(inteiro);
//						String ind = indexes.get(exam).get(d);
//						if(!att.equals(ind)){
//							count++;
//						}
						vitals.put(Integer.parseInt(ins) , indexes.get(exam).get(d));
					}					
					examsAll.put(exam, vitals);
				}
				examsTime[o] = System.currentTimeMillis();
				o++;
			}
//			System.out.println(count);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	private HashMap<String, HashMap<Double, String>> getIndexes(String[] exams) throws IOException {
		HashMap<String, HashMap<Double, String>> indexes = new HashMap<String, HashMap<Double,String>>();
		for(String exam: exams){
			System.out.println(exam);
			BufferedReader arff = new BufferedReader(new FileReader(path +exam +".arff"));
			for(int i=0; i< 2+steps; i++){
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
			indexes.put(exam, e);
			arff.close();
		}
		return indexes;
	}

	@SuppressWarnings("unused")
	private String examType(String exam, Double d) {
		String result = "";
		switch(exam){
		case "Sub_metering_1":
			int i = (int) Math.round(d);
			result = i+"";
			break;
		case "Sub_metering_2":
			i = (int) Math.round(d);
			result = i+"";
			break;
		case "Sub_metering_3":
			i = (int) Math.round(d);
			result = i+"";
			break;
		default:
			result = d+"";
			break;
		}
		return result;
	}

	private String[] getVariables() throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path + "diagnosis_discret.csv"));
		String header = in.readLine();
		in.close();
		String[] split = header.split(",",-1);
		String[] result = new String[split.length-2];
		for (int i = 1; i < split.length-1; i++) {
			result[i-1] = split[i];
		}
		return result;
	}

}