package tdm.classification.run.als;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
	private  String path = "C:" + File.separator + "PROACT_2013_08_27_ALL_FORMS" +
			File.separator + "FormatedData"+ File.separator + "Approach2" + File.separator;
	private String hmm = "C:\\PROACT_2013_08_27_ALL_FORMS\\hmm\\multidata\\run\\";
	private static String[] examsHMM = {"Demo1","Demo2","Demo3","SVC2","SVC5","SVC6","SVC7","Vitals2","Vitals3","Vitals6","Vitals7","Vitals8","Vitals9"};
	private  HashMap<String,DefaultHashMap<String,String>> predictionsHMM = new HashMap<String,DefaultHashMap<String,String>>();
	private static String[] classes_simb = {"{0-12}","{12-24}","{24-36}","{36-48}"};
	static ArrayList<String> accuracies = new ArrayList<String>();
	private static int steps = 3;

	public static void main(String[] args){
		try {
			hmm();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void hmm() throws IOException, FileNotFoundException,
	Exception {
		Approach2HMM a = new Approach2HMM();
		a.evaluatePredictionsHMM();
		a.buildDataWithHMMPredictionsSorted();
		Classifier j = null;
		Utils u = new Utils();
		accuracies = new ArrayList<String>();
		a.ClassifyDataNominal(new NaiveBayes());
		int[][] matrix = a.buildConfussionMatrix("Naive Bayes");
		u.metrics(matrix,classes_simb,"ALS",j,"Approach2(4,10)", steps, "NaiveBayes");
		a.ClassifyDataNominal(new J48());
		matrix = a.buildConfussionMatrix("J48");
		u.metrics(matrix,classes_simb,"ALS",j,"Approach2(4,10)", steps, "J48");
		a.ClassifyDataNominal(new AdaBoostM1());
		matrix = a.buildConfussionMatrix("AdaBoost");
		u.metrics(matrix,classes_simb,"ALS",j,"Approach2(4,10)", steps, "AdaBoost");
		a.ClassifyDataNominal(new Logistic());
		matrix = a.buildConfussionMatrix("Logistic");
		u.metrics(matrix,classes_simb,"ALS",j,"Approach2(4,10)", steps, "Logistic");
		a.ClassifyDataNominal(new RandomForest());
		matrix = a.buildConfussionMatrix("RandomForest");
		u.metrics(matrix,classes_simb,"ALS",j,"Approach2(4,10)", steps, "RandomForest");

		for (String s : accuracies) {
			System.out.println(s);
		}
	}
	
	private  void ClassifyDataNominal(Classifier classifier) throws Exception {
		System.out.println("Classify Data");
		changeClassesInAttributes("DiagnoseData.arff","DiagnoseDataNominal.arff");
		changeClassesInAttributes("PredictionDataWithDemo"+steps+".arff","PredictionDataWithDemoNominal"+steps+".arff");
		Instances train = new Instances(new BufferedReader(new FileReader(path+"DiagnoseDataNominal.arff")));
		//		CSVLoader loader = new CSVLoader();
		//		loader.setSource(new File(path+"PredictionDataWithDemo.csv"));
		//		Instances test = loader.getDataSet();
		Instances test = new Instances(new BufferedReader(new FileReader(path+"PredictionDataWithDemoNominal"+steps+".arff")));

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

		//		System.out.println("----------------------------------------");
		//		System.out.println(cModel.toString());

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
	
	private void changeClassesInAttributes(String file,String outFile) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(path + file));
		BufferedWriter out = new BufferedWriter(new FileWriter(path + outFile));
		String line;
		for(int i=0;i<4;i++){
			out.write(in.readLine()+ '\n');
		}
		while((line = in.readLine()) != null){
			if (line.contains("@attribute")){
				if(line.contains("@attribute ALSFRS-RTotal")){
					out.write("@attribute ALSFRS-RTotal {'{36-48}','{24-36}','{12-24}','{0-12}'}\n");
				}else{
//					String feat = line.split(" ")[1];
//					out.write("@attribute "+ feat + " " + classes.get(feat) + '\n');
					out.write(line+ '\n');
				}
			}else{
				out.write(line+ '\n');
			}
		}
		in.close();
		out.close();
	}
	
	private void buildDataWithHMMPredictionsSorted() throws IOException {
		System.out.println("build Data With Predictions");
		String[] exams = examsHMM;
		BufferedReader diag = new BufferedReader(new FileReader(path +"DiagnoseData.csv"));
		String header = diag.readLine();
		diag.close();
		BufferedWriter outPredictions = new BufferedWriter(new FileWriter(path+"PredictionDataWithDemo.csv"));
		outPredictions.write(header+ '\n');
		String patientWith = "";

		Set<String> keys = getIDs();
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for(String key: keys){
			ids.add(Integer.parseInt(key));
		}
		Collections.sort(ids);
		for(Integer id: ids){
			patientWith = id + ",";
			String key = id+"";
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

	private Set<String> getIDs() throws IOException {
		Set<String> s = new HashSet<String>();
		BufferedReader in = new BufferedReader(new FileReader(path+"DiagnoseDataReal.csv"));
		in.readLine();
		String line;
		while((line = in.readLine()) != null){
			String[] split = line.split(",",-1);
			s.add(split[0]);
		}
		in.close();
		return s;
	}

	private void evaluatePredictionsHMM() throws IOException {
		ArrayList<String> examsIndexes = new ArrayList<String>( Arrays.asList(examsHMM));
		for(int i= 0; i<examsHMM.length;i++){
			BufferedReader pred = new BufferedReader(new FileReader(hmm+examsHMM[i]+"_Predictions.csv"));
			String line = null;
			DefaultHashMap<String, String> e = new DefaultHashMap<String, String>("");
			while((line = pred.readLine()) != null){
				String[] split = line.split(",",-1);
				String id = split[0];
				String p = split[1];
				e.put(id, p);
			}
			predictionsHMM.put(examsHMM[i], e);
			pred.close();
		}


		BufferedReader real = new BufferedReader(new FileReader(path+"DiagnoseDataReal.csv"));
		real.readLine();
		String line;
		HashMap<String,Integer> correct = new HashMap<String, Integer>();
		int total = 0;
		while((line = real.readLine()) != null){
			String[] split = line.split(",",-1);
			String id = split[0];
			for(int i= 0; i<examsHMM.length;i++){
				DefaultHashMap<String, String> e = predictionsHMM.get(examsHMM[i]);

				if(split[1+examsIndexes.indexOf(examsHMM[i])].equals(e.get(id))){
					Integer count = correct.get(examsHMM[i]);
					if(count == null){
						count = 1;
					}else{
						count++;
					}
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

}