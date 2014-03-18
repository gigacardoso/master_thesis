package run.als;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;

import utils.DefaultHashMap;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.unsupervised.attribute.Remove;
import createData.ALS.CreateData;

public class Approach2 {
	private  String path = "C:" + File.separator + "PROACT_2013_08_27_ALL_FORMS" +
			File.separator + "FormatedData"+ File.separator + "Approach2" + File.separator;
	private  HashMap<Integer,DefaultHashMap<Integer,String>> svcAll = new HashMap<Integer,DefaultHashMap<Integer,String>>();
	private  HashMap<Integer,DefaultHashMap<Integer,String>> vitalsAll = new HashMap<Integer,DefaultHashMap<Integer,String>>();
	private  DefaultHashMap<String, String> heights = new DefaultHashMap<String, String>("");
	private  int steps = 6;
	private  int folds = 10;

	public static void main(String[] args){

		try {
			Approach2 a = new Approach2();
			a.predictVitals();
			a.predictSVC();
			a.buildDataWithPredictions();
			a.evaluatePredictions();
			a.ClassifyData(new NaiveBayes());
			a.buildConfussionMatrix("Naive Bayes");
			a.ClassifyData(new J48());
			a.buildConfussionMatrix("J48");
			a.ClassifyData(new AdaBoostM1());
			a.buildConfussionMatrix("AdaBoost");
			//			a.ClassifyData(new MultilayerPerceptron());
			//			a.buildConfussionMatrix("NN");
			a.ClassifyData(new Logistic());
			a.buildConfussionMatrix("Logistic");

			System.out.println("------------------\tDiagnostic\t------------------");
			//						a.ClassifyDiagnostic(new NaiveBayes());
			//			a.ClassifyDiagnostic(new J48());
			//			a.ClassifyDiagnostic(new AdaBoostM1());
			//			a.ClassifyDiagnostic(new MultilayerPerceptron());
			//			a.ClassifyDiagnostic(new Logistic());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void evaluatePredictions() throws FileNotFoundException, IOException {
		Integer[] exams = {2,5,6,7,2,3,6,7,8,9};
		Instances real = new Instances(new BufferedReader(new FileReader(path+"DiagnoseDataReal.arff")));
		HashMap<String,Double> correct = new HashMap<String, Double>();
		HashMap<String,Integer> totals = new HashMap<String, Integer>();
		for(Instance ins:real){
			String[] split = ins.toString().split(",",-1);
			Integer id = Integer.parseInt(split[0]);
			for(int i= 0; i<10;i++){
				DefaultHashMap<Integer, String> e= null;
				String s = "";
				if(i<4){
					e = svcAll.get(exams[i]);
					s = "SVC";
				}else{
					e = vitalsAll.get(exams[i]);
					s = "Vitals";
				}
				if(!split[4+i].equals("?") && !e.get(id).equals("")){
					double d = Math.abs(Double.parseDouble(split[4+i])-Double.parseDouble(e.get(id)));
					Double count = correct.get(s+exams[i]);
					Integer tot = totals.get(s+exams[i]);
					if(count == null){
						count = 0.0;
					}
					if(tot == null){
						tot = 1;
					}

					correct.put(s+exams[i],count+d);
					totals.put(s+exams[i], tot+1);	
				}
			}
		}
		double average = 0;
		DecimalFormat df = new DecimalFormat("#.##");
		for(int i= 0; i<10;i++){
			String s = "";
			if(i<4){
				s = "SVC";
			}else{
				s = "Vitals";
			}
			Double count = correct.get(s+exams[i]);
			Integer total = totals.get(s+exams[i]);
			double perc = (count)/total;
			average += perc;
			System.out.println(s+exams[i] + "\t->\t"+ df.format(perc));
		}
		System.out.println("\nAverage - "+ df.format(average/exams.length));	
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

	private  void buildConfussionMatrix(String method) throws FileNotFoundException, IOException {
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

	private  void buildDataWithPredictions() throws IOException {
		System.out.println("build Data With Predictions");
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

		//SubjectID	Sex	Age	Height	P_of_Normal_(Trial_1)	Subject_Liters_(Trial_1)	Subject_Liters_(Trial_2)	Subject_Liters_(Trial_3)	Blood_Pressure_(Diastolic)	Blood_Pressure_(Systolic)	Pulse	Respiratory_Rate	Temperature	Weight	ALSFRS-R_Total
		while((line1 = inDemo.readLine()) != null){
			String[] split = line1.split(",",-1);
			patientWith = split[0]+"," + split[4]+"," + split[11]+"," + heights.get(split[0])+"," ;
			patientWithout = split[0] + ","; 
			int id = Integer.parseInt(split[0]);
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
				if(line.contains("@attribute ALSFRS-R_Total string")){
					out.write("@attribute ALSFRS-R_Total {'{36-48}','{24-36}','{12-24}','{0-12}'}\n");
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

	private  void predictVitals() {
		int[] vitalsIndex = {2,3,6,7,8,9};
		try{
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
					Classifier cModel = (Classifier) new LinearRegression();   
					cModel.buildClassifier(train);

					// Test the model
					Evaluation eTest = new Evaluation(test);
					eTest.evaluateModel(cModel, test);

					// Print the result à la Weka explorer:
					String strSummary = eTest.toSummaryString();
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
						vitals.put(Integer.parseInt(ins) , d+"");
					}
					vitalsAll.put(index, vitals);
				}
			}

		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	private  void predictSVC() {
		int[] svcIndex = {2,5,6,7};
		try{
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
					Classifier cModel = (Classifier) new LinearRegression();   
					cModel.buildClassifier(train);

					// Test the model
					Evaluation eTest = new Evaluation(test);
					eTest.evaluateModel(cModel, test);

					// Print the result à la Weka explorer:
					String strSummary = eTest.toSummaryString();
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
						svc.put(Integer.parseInt(ins) , d +"");
					}
					svcAll.put(index, svc);
				}
			}

			// Get the confusion matrix
			//			double[][] cmMatrix = eTest.confusionMatrix();
			//			for(int row_i=0; row_i<cmMatrix.length; row_i++){
			//				for(int col_i=0; col_i<cmMatrix.length; col_i++){
			//					System.out.print(cmMatrix[row_i][col_i]);
			//					System.out.print("|");
			//				}
			//				System.out.println();
			//			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
}