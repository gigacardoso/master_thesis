package tdm.classification.run.hepatitis;

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
import java.util.Set;

import tdm.classification.createData.ALS.CreateData;
import tdm.classification.utils.DefaultHashMap;
import tdm.classification.utils.Utils;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

public class Approach2HMM {
	private String data = "C:\\hepat_data030704\\";
	private String path = data +"data\\";
	private String andreia = data + "andreia\\";
	private String hmm = path + "predictionsHMM_Multi\\";
	private static String[] classes_simb = {"F0","F1","F2","F3","F4"};
	private DefaultHashMap<String, String> patients = new DefaultHashMap<String, String>("");
	private String[] exams = {"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL","ALB","CHE","T-CHO","TP","Type","Activity"};
	private String[] indices = {"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL","ALB","CHE","T-CHO","TP","Type","Activity"};
	private String[] examsHMM = {"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL","ALB","CHE","T-CHO","TP","Type","Activity"};
	private  HashMap<String,DefaultHashMap<String,String>> predictionsHMM = new HashMap<String,DefaultHashMap<String,String>>();
	private static int steps = 12;

	long[] examsTime = new long[exams.length];
	static long start,predictionsTime,startClassification, endNaive, endRF,endDT, endAdaboost, endLogistic;
	static ArrayList<String> accuracies = new ArrayList<String>();
	public static void main(String[] args){
		try {
			hmm();

			System.out.println("------------------\tDiagnostic\t------------------");
						Approach2 a = new Approach2();
						a .ClassifyDiagnostic(new NaiveBayes());
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

	private static void hmm() throws IOException, Exception,FileNotFoundException {
		Approach2HMM a = new Approach2HMM();
		a.evaluatePredictionsHMM();
		a.buildDataWithHMMPredictionsSorted();
		//			a.buildDataWithPredictionsUnsorted();
		Classifier j = null;
		Utils u = new Utils();
		accuracies  = new ArrayList<String>();
		
		a.ClassifyData(new NaiveBayes(), "");
		int[][] matrix = a.buildConfussionMatrix("Naive Bayes", "");
		u.metrics(matrix,classes_simb,"Hepatitis",j,"Approach2(4,5)", steps, "NaiveBayes");
		a.ClassifyData(new J48(), "");
		matrix = a.buildConfussionMatrix("J48","");
		u.metrics(matrix,classes_simb,"Hepatitis",j,"Approach2(4,5)", steps, "J48");
		a.ClassifyData(new AdaBoostM1(), "");
		matrix = a.buildConfussionMatrix("AdaBoost","");
		u.metrics(matrix,classes_simb,"Hepatitis",j,"Approach2(4,5)", steps, "AdaBoost");
		a.ClassifyData(new Logistic(), "");
		matrix = a.buildConfussionMatrix("Logistic", "");
		u.metrics(matrix,classes_simb,"Hepatitis",j,"Approach2(4,5)", steps, "Logistic");
		a.ClassifyData(new RandomForest(), "");
		matrix = a.buildConfussionMatrix("RandomForest", "");
		u.metrics(matrix,classes_simb,"Hepatitis",j,"Approach2(4,5)", steps, "RandomForest");
		
		for (String s : accuracies) {
			System.out.println(s);
		}
	}

	private void evaluatePredictionsHMM() throws IOException {
		ArrayList<String> examsIndexes = new ArrayList<String>( Arrays.asList(indices));
		//String[] exams = {/*"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL",*/"ALB","CHE"/*,"T-CHO","TP"*/,"Type"};//,"Activity"};
		for(int i= 0; i<examsHMM.length;i++){
			BufferedReader pred = new BufferedReader(new FileReader(hmm+examsHMM[i]+"_Predictions.csv"));
			String line = null;
			DefaultHashMap<String, String> e = new DefaultHashMap<String, String>("");
			while((line = pred.readLine()) != null){
				String[] split = line.split(",",-1);
				String id = split[0];
				String p = split[1].split("_",-1)[0];// "N";
				e.put(id, p);
			}
			predictionsHMM.put(examsHMM[i], e);
			pred.close();
		}


		BufferedReader real = new BufferedReader(new FileReader(path+"DiagnosisReal.csv"));
		real.readLine();
		String line;
		HashMap<String,Integer> correct = new HashMap<String, Integer>();
		for(int i= 0; i<examsHMM.length;i++){
			correct.put(examsHMM[i], 0);
		}
		int total = 0;
		while((line = real.readLine()) != null){
			String[] split = line.split(",",-1);
			String id = split[0];
			for(int i= 0; i<examsHMM.length;i++){
				DefaultHashMap<String, String> e = predictionsHMM.get(examsHMM[i]);

				if(split[5+examsIndexes.indexOf(examsHMM[i])].equals(e.get(id))){
					Integer count = correct.get(examsHMM[i]);
					count++;
					correct.put(examsHMM[i],count);
				}
			}
			total++;
		}
		real.close();
		System.out.println("\nTotal predictions ->\t" + total);
		double average = 0;
		DecimalFormat df = new DecimalFormat("#.##");
		for(String exam:examsHMM){
			//System.out.println(exam);
			Integer count = correct.get(exam);
			double perc = ((double)count)/total *100;
			average += perc;
			System.out.println(exam + "\t->\t"+ /*count+" / "+ total+ "\t"+*/ df.format(perc) + " %" );
		}
		System.out.println("\nAverage - "+ df.format(average/examsHMM.length) + " %");
	}

	private  int[][] buildConfussionMatrix(String method, String string) throws FileNotFoundException, IOException {
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
		accuracies.add(df.format(accuracy*100) + " %");
		System.out.println("Incorrectly Classified Instances\t"+ bad +"\t\t" + df.format(errorRate*100) + " %");
		return matrix;
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

		// Print the result � la Weka explorer:
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

	private  void buildDataWithHMMPredictionsSorted() throws IOException {
		System.out.println("build Data With Predictions");
		String[] exams = {"GPT","GOT","ZTT","TTT","T-BIL","D-BIL","I-BIL","ALB","CHE","T-CHO","TP","Type","Activity"};
		BufferedReader diag = new BufferedReader(new FileReader(path +"Diagnosis.csv"));
		String header = diag.readLine();
		diag.close();
		BufferedWriter outPredictions = new BufferedWriter(new FileWriter(path+"PredictionDataWithDemo.csv"));
		outPredictions.write(header+ '\n');
		String patientWith = "";

		readPatients();

		Set<String> keys = predictionsHMM.get(exams[0]).keySet();
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
				patientWith += predictionsHMM.get(exam).get(key) + ",";
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
}