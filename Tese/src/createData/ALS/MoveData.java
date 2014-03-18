package createData.ALS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MoveData {

	private  String alternative;
	private  String approach1;
	private  String diagnosis;
	private  String datasets = "C:" + File.separator + "PROACT_2013_08_27_ALL_FORMS" + File.separator + "FormatedData" + File.separator;
	private  String alternativeOut = datasets + "AlternativeApproach";
	private  String approach1Out = datasets + "Approach1";
	private  String approach2Out = datasets + "Approach2";
	private  String baseline = datasets + "Baseline";
	private  String baselineNoClass = datasets + "Baseline" + File.separator + "NoClass";
	
	private int steps;
	
	public MoveData(String alternative, String approach1, String diagnosis,
			int steps) {
		super();
		this.alternative = alternative;
		this.approach1 = approach1;
		this.diagnosis = diagnosis;
		this.steps = steps;
	}


	private void copyFile(File source, File dest) throws IOException {
	    Files.copy(source.toPath(), dest.toPath());
	}

	
	public void MoveAllData(int i) {
		System.out.println("Moving Data");
		steps = i;
		MoveAlternative();
		MoveApproach1();
		MoveApproach2();
		MoveBaseline();
	}

	private void MoveApproach2() {
		System.out.println("Moving Approach 2");
		try {
			CreateData create = new CreateData();
			create.DeleteFiles(approach2Out);
			copyFile(new File(approach1 + "approach2_SVC2_"+steps+".csv"), new File(approach2Out + File.separator + "approach2_SVC2_"+steps+".csv"));
			copyFile(new File(approach1 + "approach2_SVC5_"+steps+".csv"), new File(approach2Out + File.separator + "approach2_SVC5_"+steps+".csv"));
			copyFile(new File(approach1 + "approach2_SVC6_"+steps+".csv"), new File(approach2Out + File.separator + "approach2_SVC6_"+steps+".csv"));
			copyFile(new File(approach1 + "approach2_SVC7_"+steps+".csv"), new File(approach2Out + File.separator + "approach2_SVC7_"+steps+".csv"));
			copyFile(new File(approach1 + "approach2_Vitals2_"+steps+".csv"), new File(approach2Out  + File.separator+ "approach2_Vitals2_"+steps+".csv"));
			copyFile(new File(approach1 + "approach2_Vitals3_"+steps+".csv"), new File(approach2Out + File.separator + "approach2_Vitals3_"+steps+".csv"));
			copyFile(new File(approach1 + "approach2_Vitals6_"+steps+".csv"), new File(approach2Out + File.separator + "approach2_Vitals6_"+steps+".csv"));
			copyFile(new File(approach1 + "approach2_Vitals7_"+steps+".csv"), new File(approach2Out + File.separator + "approach2_Vitals7_"+steps+".csv"));
			copyFile(new File(approach1 + "approach2_Vitals8_"+steps+".csv"), new File(approach2Out + File.separator + "approach2_Vitals8_"+steps+".csv"));
			copyFile(new File(approach1 + "approach2_Vitals9_"+steps+".csv"), new File(approach2Out + File.separator + "approach2_Vitals9_"+steps+".csv"));
			
			copyFile(new File(diagnosis + "DiagnoseData.arff"), new File(approach2Out + File.separator + "DiagnoseData.arff"));
			copyFile(new File(diagnosis + "DiagnoseData.csv"), new File(approach2Out + File.separator + "DiagnoseData.csv"));
			copyFile(new File(diagnosis + "DiagnoseDataReal.arff"), new File(approach2Out + File.separator + "DiagnoseDataReal.arff"));
			
//			copyFile(new File(approach1 + "PredictionDataWithDemo.csv"), new File(approach1Out + "PredictionDataWithDemo.csv"));
//			copyFile(new File(approach1 + "PredictionDataWithoutDemo.csv"), new File(approach1Out + "PredictionDataWithoutDemo.csv"));
			copyFile(new File(alternative + steps+"VITALS_DATA.csv"), new File(approach2Out + File.separator + steps+"_VITALS_DATA.csv"));
			copyFile(new File(alternative + "Demo_Data.csv"), new File(approach2Out + File.separator + "Demo_Data.csv"));
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}


	private void MoveBaseline() {
		System.out.println("Moving Baseline");
		try {
			CreateData create = new CreateData();
			create.DeleteFiles(baseline);
			create.DeleteFiles(baselineNoClass);
			for(int i=0; i< steps-1;i++){
				copyFile(new File(diagnosis + "baseline"+i+".arff"), new File(baseline + File.separator + "baseline"+i+".arff"));
//				copyFile(new File(diagnosis + "baselineNoClass"+i+".arff"), new File(baselineNoClass + File.separator + "baselineNoClass"+i+".arff"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}			
	}


	private void MoveApproach1() {
		System.out.println("Moving Approach 1");
		try {
			CreateData create = new CreateData();
			create.DeleteFiles(approach1Out);
			copyFile(new File(approach1 + "approach1_SVC2_"+steps+".csv"), new File(approach1Out + File.separator + "approach1_SVC2_"+steps+".csv"));
			copyFile(new File(approach1 + "approach1_SVC5_"+steps+".csv"), new File(approach1Out + File.separator + "approach1_SVC5_"+steps+".csv"));
			copyFile(new File(approach1 + "approach1_SVC6_"+steps+".csv"), new File(approach1Out + File.separator + "approach1_SVC6_"+steps+".csv"));
			copyFile(new File(approach1 + "approach1_SVC7_"+steps+".csv"), new File(approach1Out + File.separator + "approach1_SVC7_"+steps+".csv"));
			copyFile(new File(approach1 + "approach1_Vitals2_"+steps+".csv"), new File(approach1Out  + File.separator+ "approach1_Vitals2_"+steps+".csv"));
			copyFile(new File(approach1 + "approach1_Vitals3_"+steps+".csv"), new File(approach1Out + File.separator + "approach1_Vitals3_"+steps+".csv"));
			copyFile(new File(approach1 + "approach1_Vitals6_"+steps+".csv"), new File(approach1Out + File.separator + "approach1_Vitals6_"+steps+".csv"));
			copyFile(new File(approach1 + "approach1_Vitals7_"+steps+".csv"), new File(approach1Out + File.separator + "approach1_Vitals7_"+steps+".csv"));
			copyFile(new File(approach1 + "approach1_Vitals8_"+steps+".csv"), new File(approach1Out + File.separator + "approach1_Vitals8_"+steps+".csv"));
			copyFile(new File(approach1 + "approach1_Vitals9_"+steps+".csv"), new File(approach1Out + File.separator + "approach1_Vitals9_"+steps+".csv"));
			
			copyFile(new File(diagnosis + "DiagnoseData.arff"), new File(approach1Out + File.separator + "DiagnoseData.arff"));
			copyFile(new File(diagnosis + "DiagnoseData.csv"), new File(approach1Out + File.separator + "DiagnoseData.csv"));
			copyFile(new File(diagnosis + "DiagnoseDataReal.arff"), new File(approach1Out + File.separator + "DiagnoseDataReal.arff"));
			
//			copyFile(new File(approach1 + "PredictionDataWithDemo.csv"), new File(approach1Out + "PredictionDataWithDemo.csv"));
//			copyFile(new File(approach1 + "PredictionDataWithoutDemo.csv"), new File(approach1Out + "PredictionDataWithoutDemo.csv"));
			copyFile(new File(alternative + steps+"VITALS_DATA.csv"), new File(approach1Out + File.separator + steps+"_VITALS_DATA.csv"));
			copyFile(new File(alternative + "Demo_Data.csv"), new File(approach1Out + File.separator + "Demo_Data.csv"));
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}


	private void MoveAlternative() {
		System.out.println("Moving Alternative Approach");
		 try {
			 CreateData create = new CreateData();
			create.DeleteFiles(alternativeOut);
			copyFile(new File(alternative + "approach1_"+steps+".arff"), new File(alternativeOut + File.separator + "approach1_"+steps+".arff"));
			copyFile(new File(alternative + "approach1_NoClass_"+steps+".arff"), new File(alternativeOut + File.separator + "approach1_NoClass_"+steps+".arff"));
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public static void main(String[] args){
//		MoveAllData(5);
	}
	
	

}
