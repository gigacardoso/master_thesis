package run.hepatitis;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import utils.DefaultHashMap;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.HMM;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;
import createData.ALS.CreateData;

public class Approach1 {
	private String data = "C:\\hepat_data030704\\";
	private String path = data +"data\\";
	private String andreia = data + "andreia\\";
	//String[] exams = {"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL"/*,"ALB","CHE","T-CHO","TP"*/,"Type"};//,"Activity"};
	private String[] indices = {"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL","ALB","CHE","T-CHO","TP","Type","Activity"};
	private String[] exams = {"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL","ALB","CHE","T-CHO","TP","Type","Activity"};
	private DefaultHashMap<String, String> patients = new DefaultHashMap<String, String>("");
	private  HashMap<String,DefaultHashMap<String,String>> predictions = new HashMap<String,DefaultHashMap<String,String>>();
	private  int steps = 7;
	private  int folds = 10;

	public static void main(String[] args){
		try {
			Approach1 a = new Approach1();
			a.predictExams(new Logistic());
			a.evaluatePredictions();
			//			a.buildDataWithPredictionsSorted();
			//			a.buildDataWithPredictionsUnsorted();
			//			a.ClassifyData(new NaiveBayes(), "");
			//			a.buildConfussionMatrix("Naive Bayes", "");
			//			J48 j48 = new J48();
			//			j48.setMinNumObj(50);
			//			a.ClassifyData(new J48(), "");
			//			a.buildConfussionMatrix("J48","");
			//			a.ClassifyData(j48, "Unsorted");
			//			a.buildConfussionMatrix("J48","Unsorted");
			//			a.compareLabeled();
			//			a.ClassifyData(new AdaBoostM1(), "");
			//			a.buildConfussionMatrix("AdaBoost","");
			//			a.ClassifyData(new MultilayerPerceptron());
			//			a.buildConfussionMatrix("NN");
			//			a.ClassifyData(new Logistic(), "");
			//			a.buildConfussionMatrix("Logistic", "");
			//						a.ClassifyData(new Logistic(), "Unsorted");

			System.out.println("------------------\tDiagnostic\t------------------");
			//			a.ClassifyDiagnostic(new NaiveBayes());
			//			j48 = new J48();
			//			j48.setMinNumObj(50);
			//			a.ClassifyDiagnostic(new J48());
			//			a.ClassifyDiagnostic(new AdaBoostM1());
			//			a.ClassifyDiagnostic(new MultilayerPerceptron());
			//			a.ClassifyDiagnostic(new Logistic());
		} catch (Exception e) {
			e.printStackTrace();
		}



	}

	private void compareLabeled() throws FileNotFoundException, IOException {
		System.out.println("\nCompare Labeled");
		Instances labeled = new Instances(new BufferedReader(new FileReader(path+"PredictionDataWithDemo"+steps+".arff")));
		Instances real = new Instances(new BufferedReader(new FileReader(path+"PredictionDataWithDemoUnsorted"+steps+".arff")));
		HashMap<String,Integer> indexes = new HashMap<String, Integer>();
		String[] classes ={"F0","F1","F2","F3","F4"}; //{"B1","B2","B3","B4","C0","C1","C2","C3","C4"};
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
					System.out.println("s- "+ ins2);
					System.out.println("u- "+ins);
					System.out.println("---------------------------------");
					if(str2[ins2.numAttributes()-1].equals("?") || str[ins2.numAttributes()-1].equals("?")){
						break;
					}
					matrix[indexes.get(str2[ins2.numAttributes()-1])][indexes.get(str[ins2.numAttributes()-1])]++;
					break;
				}
			}
		}		
	}

	private void evaluatePredictions() throws IOException {
		ArrayList<String> examsIndexes = new ArrayList<String>( Arrays.asList(indices));
		//String[] exams = {/*"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL",*/"ALB","CHE"/*,"T-CHO","TP"*/,"Type"};//,"Activity"};
		BufferedReader real = new BufferedReader(new FileReader(path+"DiagnosisReal.csv"));
		real.readLine();
		String line = null;
		HashMap<String,Integer> correct = new HashMap<String, Integer>();
		int total = 0;
		while((line = real.readLine()) != null){
			String[] split = line.split(",",-1);
			String id = split[0];
			for(int i= 0; i<exams.length;i++){
				DefaultHashMap<String, String> e = predictions.get(exams[i]);
				if(split[5+examsIndexes.indexOf(exams[i])].equals(e.get(id))){
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
			System.out.println(exam);
			Integer count = correct.get(exam);
			double perc = ((double)count)/total *100;
			average += perc;
			System.out.println(exam + "\t->\t"+ /*count+" / "+ total+ "\t"+*/ df.format(perc) + " %" );
		}
		System.out.println("\nAverage - "+ df.format(average/exams.length) + " %");


	}

	private  void ClassifyDiagnostic(Classifier classifier) {
		System.out.println("Classify Diagnostic");
		try{
			Instances data = new Instances(new BufferedReader(new FileReader(path+"Diagnosis.arff")));
			data.setClassIndex(data.numAttributes() - 1);

			// Create Classifier
			Remove remove = new Remove();                         // new instance of filter
			remove.setAttributeIndices("1");					// set options

			FilteredClassifier cModel = new FilteredClassifier();
			//			Classifier cModel = classifier;
			cModel.setFilter(remove);
			cModel.setClassifier(classifier);
			// train and make predictions
			//			cModel.buildClassifier(data);

			Evaluation eval = new Evaluation(data);
			eval.crossValidateModel(cModel, data, 10, new Random(1));
			// Print the result à la Weka explorer:
			String strSummary = eval.toSummaryString();
			//				System.out.println(cModel.toString());

			//Get the confusion matrix
			//			double[][] matrix = eval.confusionMatrix();
			System.out.println("\n\t\t"+ classifier.getClass().toString());
			System.out.println(strSummary);
			System.out.println(eval.toMatrixString());
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

	private  void buildConfussionMatrix(String method, String string) throws FileNotFoundException, IOException {
		System.out.println("build Confusion Matrix " + string);
		Instances labeled = new Instances(new BufferedReader(new FileReader(path+"labeled"+string+".arff")));
		Instances real = new Instances(new BufferedReader(new FileReader(path+"DiagnosisReal.arff")));
		HashMap<String,Integer> indexes = new HashMap<String, Integer>();
		String[] classes ={"F0","F1","F2","F3","F4"}; //{"B1","B2","B3","B4","C0","C1","C2","C3","C4"};
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
					//					System.out.println(ins2);
					//					System.out.println(ins);
					//					System.out.println("---------------------------------");
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
		System.out.print("\t");
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

	private  void ClassifyData(Classifier classifier, String string) throws Exception {
		System.out.println("Classify Data " + string);
		Instances train = new Instances(new BufferedReader(new FileReader(path+"Diagnosis.arff")));
		Instances test = new Instances(new BufferedReader(new FileReader(path+"PredictionDataWithDemo"+string+steps+".arff")));

		// Set class index
		train.setClassIndex(train.numAttributes() - 1);
		test.setClassIndex(test.numAttributes() - 1);


		// first attribute
		Remove remove = new Remove();                         // new instance of filter
		remove.setAttributeIndices("1");					// set options

		Instances labeled = new Instances(test);

		FilteredClassifier cModel = new FilteredClassifier();
		cModel.setFilter(remove);
		cModel.setClassifier(classifier);
		// train and make predictions
		cModel.buildClassifier(train);
		//		System.out.println(cModel);
		// Create Classifier
		//		Classifier cModel = classifier;
		//		cModel.buildClassifier(train);

		// Test the model
		//		Evaluation eTest = new Evaluation(test);
		//		eTest.evaluateModel(cModel, test);

		// Print the result à la Weka explorer:
		//		String strSummary = eTest.toSummaryString();
		//		System.out.println(cModel.toString());
		//		System.out.println(strSummary);
		//		System.out.println("--------------------------------");

		for (int i = 0; i < test.numInstances(); i++) {
			double clsLabel = cModel.classifyInstance(test.instance(i));
			//			System.out.println(test.instance(i));
			//			System.out.println(labeled.instance(i));
			//			System.out.println("---------------");
			labeled.instance(i).setClassValue(clsLabel);
		}

		BufferedWriter writer = new BufferedWriter(
				new FileWriter(path+"labeled"+string+".arff"));
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

	private void readPatients() throws IOException{
		BufferedReader inFact = new BufferedReader(new FileReader(andreia+
				"DimPatient.table"));
		inFact.readLine();
		String[] split;
		String line;
		while((line = inFact.readLine()) != null){
			split = line.split("\t",-1);
			patients.put(split[0], line);
		}
		inFact.close();
	}

	private  void buildDataWithPredictionsSorted() throws IOException {
		System.out.println("build Data With Predictions");
		String[] exams = {"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL","ALB","CHE","T-CHO","TP","Type","Activity"};
		BufferedReader diag = new BufferedReader(new FileReader(path +"Diagnosis.csv"));
		String header = diag.readLine();
		diag.close();
		BufferedWriter outPredictions = new BufferedWriter(new FileWriter(path+"PredictionDataWithDemo.csv"));
		outPredictions.write(header+ '\n');
		String patientWith = "";

		readPatients();

		Set<String> keys = predictions.get(exams[0]).keySet();
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for(String key: keys){
			ids.add(Integer.parseInt(key));
		}
		Collections.sort(ids);
		//		List<String> ids = new ArrayList<String>();
		//		ids.addAll(keys);
		//		Collections.sort(ids);
		for(Integer id: ids){
			//			System.out.println(id);
			patientWith = "";
			String key = id+"";
			String[] split = patients.get(key).split("\t",-1);
			for(int i=0; i< split.length; i++){
				patientWith += split[i]+",";
			}
			for(String exam:exams){
				patientWith += predictions.get(exam).get(key) + ",";
			}
			outPredictions.write(patientWith +'\n');
		}
		outPredictions.close();
		CreateData create = new CreateData();
		create.CSV2arff(path,"PredictionDataWithDemo");
		//		File a = new File(path + "PredictionDataWithDemo.csv" );
		//		a.delete();
		ChangeClassPreditions("PredictionDataWithDemo");
	}

	private  void buildDataWithPredictionsUnsorted() throws IOException {
		System.out.println("build Data With Predictions");
		String[] exams = {"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL","ALB","CHE","T-CHO","TP","Type","Activity"};
		BufferedReader diag = new BufferedReader(new FileReader(path +"Diagnosis.csv"));
		String header = diag.readLine();
		diag.close();
		BufferedWriter outPredictions = new BufferedWriter(new FileWriter(path+"PredictionDataWithDemoUnsorted.csv"));
		outPredictions.write(header+ '\n');
		String patientWith = "";

		readPatients();

		Set<String> keys = predictions.get(exams[0]).keySet();
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for(String key: keys){
			ids.add(Integer.parseInt(key));
		}
		//		Collections.sort(ids);
		//		List<String> ids = new ArrayList<String>();
		//		ids.addAll(keys);
		//		Collections.sort(ids);
		for(Integer id: ids){
			//			System.out.println(id);
			patientWith = "";
			String key = id+"";
			String[] split = patients.get(key).split("\t",-1);
			for(int i=0; i< split.length; i++){
				patientWith += split[i]+",";
			}
			for(String exam:exams){
				patientWith += predictions.get(exam).get(key) + ",";
			}
			outPredictions.write(patientWith +'\n');
		}
		outPredictions.close();
		CreateData create = new CreateData();
		create.CSV2arff(path,"PredictionDataWithDemoUnsorted");
		//		File a = new File(path + "PredictionDataWithDemo.csv" );
		//		a.delete();
		ChangeClassPreditions("PredictionDataWithDemoUnsorted");
	}

	private  void ChangeClassPreditions(String name) {
		System.out.println("changing class");
		try {
			BufferedReader data = new BufferedReader(new FileReader(path +name+".arff"));
			BufferedWriter out = new BufferedWriter(new FileWriter(path + name+steps+".arff"));
			String line;
			while((line = data.readLine()) != null){
				if(line.contains("@attribute Fibrosis string")){
					out.write("@attribute Fibrosis {F2,F1,F4,F3,F0}\n");//"@attribute Fibrosis {B2,C1,B1,C4,C3,B4,B3,C0,C2}\n");
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

	private  void predictExams(Classifier classifier) throws IOException {
		
		//String[] exams = {/*"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL",*/"ALB"/*,"CHE","T-CHO","TP"*/,"Type"};//,"Activity"};
		HashMap<String,HashMap<Double,String>> indexes = getIndexes(exams);
		try{
			for(int i= 0; i< exams.length;i++){
				Instances data = new Instances(new BufferedReader(new FileReader(path+exams[i]/*+"_Multi"*/+".arff")));

				Random rand = new Random(1);   // create seeded number generator
				Instances randData = new Instances(data);   // create copy of original data
				randData.randomize(rand); 

				for (int n = 0; n < folds; n++) {
					Instances train = randData.trainCV(folds, n);
					Instances test = randData.testCV(folds, n);

					// Set class index
					train.setClassIndex(train.numAttributes() - 1);
					test.setClassIndex(test.numAttributes() - 1);

					// Create Classifier
					Classifier cModel = classifier;   
					cModel.buildClassifier(train);

					// Test the model
					Evaluation eTest = new Evaluation(test);
					eTest.evaluateModel(cModel, test);

					// Print the result à la Weka explorer:
					//					String strSummary = eTest.toSummaryString();
					//					System.out.println(cModel.toString());
					//					System.out.println(strSummary);
					//					System.out.println("--------------------------------");
					DefaultHashMap<String,String> exam = predictions.get(exams[i]);
					if(exam == null){
						exam = new DefaultHashMap<String, String>("");
					}
					for(Instance ins:test){
						//						System.out.print(ins.toString());
						Double d = cModel.classifyInstance(ins);
						//						System.out.println("\t-> "+d);
						String in = (ins.toString().split(","))[0];
						exam.put(in , indexes.get(exams[i]).get(d));
					}
					System.out.println("saving "+ exams[i] + " exams \t" + exam);
					predictions.put(exams[i], exam);
					//					System.out.println("Sleeping - ZzZZZZZZzzZZZZ");
					//					Thread.sleep(1000);
				}
			}

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
}