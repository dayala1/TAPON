package main;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.classification.DecisionTreeClassificationModel;
import org.apache.spark.sql.SQLContext;

public class SparkDriver {

	public static void main(String[] args) {
		SparkConf sparkConf;
		JavaSparkContext context;
		SQLContext sqlContext;
		
		sparkConf = new SparkConf();
		sparkConf.setAppName("SparkDriver");
		sparkConf.setMaster(sparkConf.get("master", "local"));
		
		context = new JavaSparkContext(sparkConf);
		sqlContext = new SQLContext(context);
		
		DecisionTreeClassificationModel model = DecisionTreeClassificationModel.load("C:/Users/Boss/Documents/DTWithTime/classifiersAndTables/modelClassifiers/4-domains/fold-10/classifiersHint/attributes/title");
		System.out.println(model.toDebugString());
		
		
	}
}
