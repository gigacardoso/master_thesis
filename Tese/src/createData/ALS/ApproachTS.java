package createData.ALS;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.timeseries.WekaForecaster;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class ApproachTS {

	public static void main(String[] args) {
		try {
			// path to the Australian wine data included with the time series forecasting
			// package
			String pathToData = "patient329.arff";

			BufferedReader in = new BufferedReader(new FileReader(pathToData));
			String line;
			int last = 0;
			ArrayList<String> p = null;
			while ((line = in.readLine()) != null) {
				if(line.length() < 3 || line.charAt(0) == '@'){
					continue;
				}

				String[] splited = line.split(",");
				if(last == Integer.parseInt(splited[0])){
					p.add(line);
				}else{
					if(p != null){
						Calculate(p);
					}
					p = new ArrayList<String>();
					p.add(line);
					last = Integer.parseInt(splited[0]);
				}

			}
			in.close();


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void Calculate(ArrayList<String> p) {
		try{
			Attribute att1 = new Attribute("SubjectID");
			Attribute att2 = new Attribute("FormID");
			Attribute att3 = new Attribute("Speech");
			Attribute att4 = new Attribute("Respiratory");
			Attribute att5 = new Attribute("Salivation");
			Attribute att6 = new Attribute("Swallowing");
			Attribute att7 = new Attribute("Handwriting");
			Attribute att8 = new Attribute("Cutting_without_Gastrostomy");
			Attribute att9 = new Attribute("Cutting_with_Gastrostomy");
			Attribute att10 = new Attribute("Dressing_and_Hygiene");
			Attribute att11 = new Attribute("Turning_in_Bed");
			Attribute att12 = new Attribute("Walking");
			Attribute att13 = new Attribute("Climbing_Stairs");
			Attribute att14 = new Attribute("ALSFRS_Delta");
			Attribute att15 = new Attribute("ALSFRS_Total");
			Attribute att16 = new Attribute("ALSFRS-R_Total");
			Attribute att17 = new Attribute("Dyspnea");
			Attribute att18 = new Attribute("Respiratory_Insufficiency");
			Attribute att19 = new Attribute("Date", "dd-MM-yyyy");


			ArrayList<Attribute> WekaAttributes = new ArrayList<Attribute>();
			WekaAttributes.add(att1);
			WekaAttributes.add(att2);
			WekaAttributes.add(att3);
			WekaAttributes.add(att4);
			WekaAttributes.add(att5);
			WekaAttributes.add(att6);
			WekaAttributes.add(att7);
			WekaAttributes.add(att8);
			WekaAttributes.add(att9);
			WekaAttributes.add(att10);
			WekaAttributes.add(att11);
			WekaAttributes.add(att12);
			WekaAttributes.add(att13);
			WekaAttributes.add(att14);
			WekaAttributes.add(att15);
			WekaAttributes.add(att16);
			WekaAttributes.add(att17);
			WekaAttributes.add(att18);
			WekaAttributes.add(att19);

			Instances patient = new Instances("Patient", WekaAttributes, 30);

			for(String line: p){
				String[] splited = line.split(",");
				Instance hospitalVisit = new DenseInstance(19);
				for(int i=0;i<splited.length; i++){
					hospitalVisit.setValue((Attribute) WekaAttributes.get(i),splited[i]);
				}
				patient.add(hospitalVisit);
			}


			// new forecaster
			WekaForecaster forecaster = new WekaForecaster();

			// set the targets we want to forecast. This method calls
			// setFieldsToLag() on the lag maker object for us
			forecaster.setFieldsToForecast("ALSFRS_Total");

			// default underlying classifier is SMOreg (SVM) - we'll use
			// gaussian processes for regression instead
			forecaster.setBaseForecaster(new GaussianProcesses());

			forecaster.getTSLagMaker().setTimeStampField("Date"); // date time stamp
			//forecaster.getTSLagMaker().setMinLag(1);
			//forecaster.getTSLagMaker().setMaxLag(12); // monthly data

			// add a month of the year indicator field
			//forecaster.getTSLagMaker().setAddMonthOfYear(true);

			// add a quarter of the year indicator field
			//forecaster.getTSLagMaker().setAddQuarterOfYear(true);

			// build the model

			forecaster.buildForecaster(patient, System.out);


			// prime the forecaster with enough recent historical data
			// to cover up to the maximum lag. In our case, we could just supply
			// the 12 most recent historical instances, as this covers our maximum
			// lag period
			forecaster.primeForecaster(patient);

			// forecast for 12 units (months) beyond the end of the
			// training data
			List<List<NumericPrediction>> forecast = forecaster.forecast(2, System.out);

			// output the predictions. Outer list is over the steps; inner list is over
			// the targets
			for (int i = 0; i < 2; i++) {
				List<NumericPrediction> predsAtStep = forecast.get(i);
				for (int j = 0; j < 1; j++) {
					NumericPrediction predForTarget = predsAtStep.get(j);
					System.out.print("" + predForTarget.predicted() + " ");
				}
				System.out.println();
			}

			// we can continue to use the trained forecaster for further forecasting
			// by priming with the most recent historical data (as it becomes available).
			// At some stage it becomes prudent to re-build the model using current
			// historical data.
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
}