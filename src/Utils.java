import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class Utils {
	//--------------------------------------------------------------------------
	public static void outputCsv(File snapDb, String rotationName){
		Connection c = null;
		Statement s = null;
		ResultSet rs = null;
		
		FileWriter fileWriter = null;
		final String FILE_HEADER = "fieldName,cropYear,slope,slopeLength,adjPlowBrayPI,total,soilLoss";
	    final String COMMA_DELIMITER = ",";
	    final String NEW_LINE_SEPARATOR = "\n";
		
		
		String fieldName = null;
		Integer cropYear;
		Double slope = null;
		Double slopeLength = null;
		Double adjPlowBrayP1 = null;
		Double total = null;
		Double soilLoss = null;
			
		String resultQuery = "Select Fields.Name as name, ModelResultsPI.cropYear as cropYear, "
				+ "Fields.Slope as slope, Fields.SlopeLength as slopeLength, "
				+ "ModelResultsPI.AdjPlowBrayP1 as AdjPlowBrayP1, ModelResultsPI.Total as total, "
				+ "ModelResultsR2.SoilLoss as soilLoss "
				+ "from modelresultspi "
				+ "inner join modelresultsr2 on modelresultsr2.cropyear = modelresultspi.cropyear AND modelresultsr2.fieldid = modelresultspi.fieldid "
				+ "join fields on fields.id = modelresultsr2.fieldid;";
		
		String regressionQuery = "select NutrientApps.fieldID, NutrientApps.CropYear, NutrientApps.TotalApplied, NutrientApps.RateUnits, NutrientApps.sourceID NutrientSources.P2O5,"
				+ "Fields.Name as name, ModelResultsPI.cropYear as cropYear, Fields.Slope as slope, Fields.SlopeLength as slopeLength," 
				+ "ModelResultsPI.AdjPlowBrayP1 as AdjPlowBrayP1, ModelResultsPI.Total as total," 
				+ "ModelResultsR2.SoilLoss as soilLoss, cropYear "  
				+ "FROM NutrientApps "
				+ "JOIN NutrientSources ON NutrientApps.sourceID = NutrientSources.ID AND NutrientSources.CropYear=NutrientApps.CropYear "
				+ "join modelresultspi on modelresultspi.fieldid = NutrientApps.fieldID AND modelresultspi.cropyear = NutrientApps.CropYear "
				+ "join modelresultsr2 on modelresultsr2.cropyear = modelresultspi.cropyear AND modelresultsr2.fieldid = modelresultspi.fieldid " 
				+ "join fields on fields.id = modelresultsr2.fieldid;";
	    try {
	    	Main.appendTextArea("CSV query: " + resultQuery);
	    	
	    	Main.appendTextArea("Writing csv for rotation " + rotationName + " from file " + snapDb.getAbsolutePath());
            int lastPeriodPos = snapDb.getName().lastIndexOf('.');
            String csvFileName = "";
            if (lastPeriodPos > 0){
            	csvFileName = snapDb.getName().substring(0, lastPeriodPos);
            }
	    	
            String jarLocation = Utils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            String outputFilePath = jarLocation +  "/" + csvFileName + ".csv";
            // String outputFilePath = snapDb.getParentFile().getParentFile() + "/" + csvFileName + ".csv";
	    	
	    	Main.appendTextArea("csv output path: " + outputFilePath);
	    	fileWriter = new FileWriter(outputFilePath);
	    	
	    	//Write the CSV file header
	    	fileWriter.append(FILE_HEADER.toString());
    		
	    	//Add a new line separator after the header
	    	fileWriter.append(NEW_LINE_SEPARATOR);

	    	
	    	// create connection and execute query
	    	Class.forName("org.sqlite.JDBC");
	    	c = DriverManager.getConnection("jdbc:sqlite:" + snapDb.getAbsolutePath());
	    	s = c.createStatement();
	    	rs = s.executeQuery(resultQuery);
	    	
	    	// parse result of query into a csv
	    	while(rs.next()){
	    		fileWriter.append(rs.getString("Name"));
	    		fileWriter.append(COMMA_DELIMITER);
	    		cropYear = rs.getInt("cropYear");
	    		fileWriter.append(cropYear.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		slope = rs.getDouble("slope");
	    		fileWriter.append(slope.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		slopeLength = rs.getDouble("slopeLength");
	    		fileWriter.append(slopeLength.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		adjPlowBrayP1 = rs.getDouble("AdjPlowBrayP1");
	    		fileWriter.append(adjPlowBrayP1.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		total = rs.getDouble("total");
	    		fileWriter.append(total.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		soilLoss = rs.getDouble("SoilLoss");
	    		fileWriter.append(soilLoss.toString());
	    		fileWriter.append(NEW_LINE_SEPARATOR);
	    		
	    		
	    		// additional details for regression data
	    		
	    		
	    	}
	    } catch ( Exception e ) {
	    	raiseExceptionAlert(e);
	    	// System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	    	System.exit(0);
	    } finally {
			try {
                fileWriter.flush();
                fileWriter.close();
				
                if (s != null) {
					s.close();
				}
				if (c != null) {
					c.close();
				}
			} catch (SQLException ex) {
				raiseExceptionAlert(ex);
				// System.out.println(ex.getMessage());
			} catch (Exception e) {
				raiseExceptionAlert(e);
                // System.out.println("Error while flushing/closing fileWriter !!!");
                // e.printStackTrace();
            }
	    }
	}
	
	
	//--------------------------------------------------------------------------
	public static void outputRegressionCsv(File snapDb, String rotationName, File outputDirectory){
		Connection c = null;
		Statement s = null;
		ResultSet rs = null;
		
		FileWriter fileWriter = null;
		
		
		final String FILE_HEADER = "total,cropYear,cropName,soilTexture,soilLoss,SoilParticulatePI,SolublePI,nutrientType,nutrientRate,nutrientUnits,totalNutrientApplied,nutrientN,nutrientK2O,nutrientP2O5,fertRate,fertUnits,fertName,fertIsSolid,fertDensity,fertP2O5,fieldName,slope,slopeLength,adjPlowBrayP1,acres";
	    final String COMMA_DELIMITER = ",";
	    final String NEW_LINE_SEPARATOR = "\n";
		
		
		Double pLoss = null;
		Integer cropYear;
		String cropName = null;
		String soilTexture = null;
		Double soilLoss = null;
		Double soilParticulatePI = null;
		Double solublePI = null;
		String nutrientType = null;
		Double nutrientRate = null;
		String nutrientUnits = null;
		Double totalNutrientApplied = null;
		Double nutrientN = null;
		Double nutrientK2O = null;
		Double nutrientP2O5 = null;
		Double fertRate = null;
		String fertUnits = null;
		String fertName = null;
		Integer fertIsSolid = null;
		Double fertDensity = null;
		Double fertP2O5;
		String fieldName = null;
		Double slope = null;
		Double slopeLength = null;
		Double adjPlowBrayP1 = null;
		Double acres = null;
		
		String regressionQuery = "select ModelResultsPI.AdjPlowBrayP1 as AdjPlowBrayP1, ModelResultsPI.Total as total, ModelResultsPI.cropYear as cropYear, ModelResultsPI.SoilParticulate as SoilParticulatePI, ModelResultsPI.Soluble as SolublePI," 
				+ "FieldPlans.SoilTexture, "
				+ "ModelResultsR2.SoilLoss as soilLoss,"
				+ "NutrientApps.CropYear, NutrientApps.TotalApplied as totalNutrientApplied, NutrientApps.RateUnits as nutrientUnits, NutrientApps.Rate as nutrientRate, " 
				+ "FertilizerApps.Rate as fertRate, FertilizerApps.RateUnits as fertUnits, FertilizerApps.CropYear, "
				+ "FertilizerSources.FertilizerName as fertName, FertilizerSources.IsSolid as FertIsSolid, FertilizerSources.LiquidDensity as fertDensity, FertilizerSources.P2O5 as fertP2O5, "
				+ "Fields.Name as fieldName, Fields.Slope as slope, Fields.SlopeLength as slopeLength, Fields.Acres as acres, "
				+ "NutrientSources.nutrientType as nutrientType, NutrientSources.N as nutrient_N, NutrientSources.P2O5 as nutrient_P2O5, NutrientSources.K2O as nutrient_K2O, "
				+ "CropYears.CropName as cropName "
				+ "FROM ModelResultsPI "
				+ "LEFT OUTER JOIN ModelResultsR2 on ModelResultsR2.cropyear = ModelResultsPI.cropyear AND ModelResultsR2.fieldid = ModelResultsPI.fieldid " 
				+ "LEFT OUTER JOIN NutrientApps on NutrientApps.fieldID = ModelResultsPI.fieldID AND NutrientApps.CropYear = ModelResultsPI.CropYear "
				+ "LEFT OUTER JOIN NutrientSources ON NutrientApps.sourceID = NutrientSources.ID AND NutrientSources.CropYear = NutrientApps.CropYear "
				+ "LEFT OUTER JOIN FertilizerApps ON ModelResultsPI.fieldid = FertilizerApps.fieldID AND ModelResultsPI.cropyear = FertilizerApps.CropYear "
				+ "LEFT OUTER JOIN FertilizerSources ON FertilizerApps.sourceID = FertilizerSources.ID "
				+ "LEFT OUTER JOIN fields on fields.id = ModelResultsR2.fieldid "
				+ "LEFT OUTER JOIN CropYears on CropYears.fieldID = fields.id AND ModelResultsPI.cropyear = CropYears.cropYear "
				+ "JOIN fieldPlans on fieldPlans.fieldID = fields.id "
				+ "ORDER BY fields.Name, ModelResultsPI.cropYear;";
	    try {
	    	Main.appendTextArea("CSV query: " + regressionQuery);
	    	
	    	Main.appendTextArea("Writing csv for rotation " + rotationName + " from file " + snapDb.getAbsolutePath());
            int lastPeriodPos = snapDb.getName().lastIndexOf('.');
            String csvFileName = "";
            if (lastPeriodPos > 0){
            	csvFileName = snapDb.getName().substring(0, lastPeriodPos);
            }
	    	
            // String jarLocation = Utils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            // String outputFilePath = jarLocation +  "/" + csvFileName + ".csv";
            String outputFilePath = outputDirectory.getPath() + "/" + csvFileName + ".csv";
	    	
	    	Main.appendTextArea("csv output path: " + outputFilePath);
	    	fileWriter = new FileWriter(outputFilePath);
	    	
	    	//Write the CSV file header
	    	fileWriter.append(FILE_HEADER.toString());
    		
	    	//Add a new line separator after the header
	    	fileWriter.append(NEW_LINE_SEPARATOR);

	    	Main.appendTextArea("----------");
	    	Main.appendTextArea("DB query: " + regressionQuery);
	    	Main.appendTextArea("----------");
	    	// create connection and execute query
	    	Class.forName("org.sqlite.JDBC");
	    	c = DriverManager.getConnection("jdbc:sqlite:" + snapDb.getAbsolutePath());
	    	s = c.createStatement();
	    	rs = s.executeQuery(regressionQuery);
	    	
	    	// parse result of query into a csv
	    	while(rs.next()){
	    		
	    		pLoss = rs.getDouble("total");
	    		cropYear = rs.getInt("cropYear");
	    		cropName = rs.getString("cropName");
	    		soilTexture = rs.getString("soilTexture");
	    		soilLoss = rs.getDouble("soilLoss");
	    		soilParticulatePI = rs.getDouble("SoilParticulatePI");
	    		solublePI = rs.getDouble("SolublePI");
	    		nutrientType = rs.getString("nutrientType");
	    		nutrientRate = rs.getDouble("nutrientRate");
	    		nutrientUnits = rs.getString("nutrientUnits");
	    		totalNutrientApplied = rs.getDouble("totalNutrientApplied");
	    		nutrientK2O = rs.getDouble("nutrient_K2O");
	    		nutrientN = rs.getDouble("nutrient_N");
	    		nutrientP2O5 = rs.getDouble("nutrient_P2O5");
	    		fertRate = rs.getDouble("fertRate");
	    		fertUnits = rs.getString("fertUnits");
	    		fertName = rs.getString("fertName");
	    		fertIsSolid = rs.getInt("fertIsSolid");
	    		fertDensity = rs.getDouble("fertDensity");
	    		fertP2O5 = rs.getDouble("fertP2O5");
	    		fieldName = rs.getString("fieldName");
	    		slope = rs.getDouble("slope");
	    		slopeLength = rs.getDouble("slopeLength");
	    		adjPlowBrayP1 = rs.getDouble("adjPlowBrayP1");
	    		acres = rs.getDouble("acres");
	    		
	    		
	    		fileWriter.append(pLoss.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(cropYear.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(cropName);
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(soilTexture);
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(soilLoss.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(soilParticulatePI.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(solublePI.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append("\"" + nutrientType + "\"");
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(nutrientRate.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(nutrientUnits);
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(totalNutrientApplied.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(nutrientN.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(nutrientP2O5.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(nutrientK2O.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(fertRate.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(fertUnits);
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(fertName);
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(fertIsSolid.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(fertDensity.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(fertP2O5.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(fieldName);
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(slope.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(slopeLength.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(adjPlowBrayP1.toString());
	    		fileWriter.append(COMMA_DELIMITER);
	    		fileWriter.append(acres.toString());
	    		fileWriter.append(NEW_LINE_SEPARATOR);
	    	}
	    	
	    } catch ( Exception e ) {
	    	raiseExceptionAlert(e);
	    	// System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	    	System.exit(0);
	    } finally {
			try {
                fileWriter.flush();
                fileWriter.close();
				
                if (s != null) {
					s.close();
				}
				if (c != null) {
					c.close();
				}
			} catch (SQLException ex) {
				raiseExceptionAlert(ex);
				// System.out.println(ex.getMessage());
			} catch (Exception e) {
				raiseExceptionAlert(e);
                // System.out.println("Error while flushing/closing fileWriter !!!");
                // e.printStackTrace();
            }
	    }
	}

	
	
	public static void raiseExceptionAlert(Exception e){
		Alert alert = new Alert(AlertType.ERROR);

		alert.setTitle("Exception Dialog");
		alert.setHeaderText("An exception occured");
		// alert.setContentText("Could not find file blabla.txt!");
	
		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String exceptionText = sw.toString();
	
		Label label = new Label("The exception stacktrace was:");
	
		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);
	
		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);
	
		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);
	
		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);
	
		alert.showAndWait();
	}
	
	
	public static void stepCompletedAlert(String shortTitle, String message){
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Information Dialog");
		alert.setHeaderText(shortTitle);
		alert.setContentText(message);
		alert.showAndWait();
	}
	
	
	
	//--------------------------------------------------------------------------
	public static void updateSnapdb(File snapDb, String action, Integer startYear){
		
		String soilSeriesUpdateSql = "update Fields set SoilSeries='PLAINFIELD', SoilSymbol='PlBT', SoilSymbolAg='PlBT', SoilSeriesAg='PLAINFIELD' where Name = 'GW-7';";
		String soilTextureUpdate = "update FieldPlans set SoilTexture = 'LOAMY_SAND' where SoilTexture = '';";
		String omUpdatesSql = "UPDATE SoilTestSamples SET OM = (SELECT AvgOM FROM SoilTests where fieldID = SoilTestSamples.fieldID) where OM = 0;";
		String updatePBalStartYear = "UPDATE FieldPlans set PBalStartYear = " + startYear + ";";
		String updatePbalRotationLength = "UPDATE FieldPlans set PBalRotationLength = 8;";
		String updateStartYear = "UPDATE FieldPlans set StartYear = " + startYear;
		Main.appendTextArea("Requested action = " + action);

	    String fakeUpdateSql = "update fields set Slope = (slope * 1);";
	    // String slopeUpdateSql = "update fields set Slope = (slope * 1.5);";
	    String slopeUpdateSql = "update fields set Slope = 15;";
		String slopeLengthUpdateSql = "update Fields set SlopeLength = (SlopeLength * 1.25);";
		String soilTextureUpdateSql = "update FieldPlans set SoilTexture = CASE " +
				"WHEN SoilTexture = 'LOAM' THEN 'CLAY_LOAM' " + 
				"WHEN SoilTexture = 'SILT_LOAM' THEN 'SILT_CLAY' " + 
				"WHEN SoilTexture = 'SANDY_LOAM' THEN 'LOAM' " +
				"WHEN SoilTexture = 'LOAMY_SAND' THEN 'SANDY_CLAY_LOAM' " + 
				"ELSE SoilTexture " +
				"END;";

		// Main.appendTextArea("sql update statement: " + actionSql);
		
		Connection c = null;
		Statement s = null;
	    try {
	    	Main.appendTextArea("Updating snapdb for " + snapDb.getName());
	    	Class.forName("org.sqlite.JDBC");
	    	c = DriverManager.getConnection("jdbc:sqlite:" + snapDb.getAbsolutePath());
	    	s = c.createStatement();
	    	
	    	s.execute(soilTextureUpdate);
	    	s.executeUpdate(updatePBalStartYear);
	    	s.executeUpdate(updatePbalRotationLength);
	    	s.executeUpdate(updateStartYear);
	    	s.executeUpdate(soilSeriesUpdateSql);
	    	s.executeUpdate(omUpdatesSql);
	    	
	    	switch (action) {
			    case "fake": s.executeUpdate(fakeUpdateSql);
			    	break;
				case "slope": s.executeUpdate(slopeUpdateSql);
					break;
				case "slopeLength": s.executeUpdate(slopeLengthUpdateSql);
					break;
				case "soilTexture": s.executeUpdate(soilTextureUpdateSql);
					break;
				case "all":
					s.executeUpdate(fakeUpdateSql);
					s.executeUpdate(slopeUpdateSql);
					// s.executeUpdate(slopeLengthUpdateSql);
					s.executeUpdate(soilTextureUpdateSql);
	    	}
	    	
	    	
	    } catch ( Exception e ) {
	    	raiseExceptionAlert(e);
	    	// System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	    	System.exit(0);
	    } finally {
			try {
				if (s != null) {
					s.close();
				}
				if (c != null) {
					c.close();
				}
			} catch (SQLException ex) {
				raiseExceptionAlert(ex);
				// System.out.println(ex.getMessage());
			}
	    }
	    // System.out.println("should get completed popup now...");
	    Main.appendTextArea("The snapDb " + snapDb.getPath() + " has been successfully updated with new " + action + " information.");
	}
	
	
	
	public static List<File> updateSnapDbList(List<File> snapDbList){
		List<File> newSnapDbList = new ArrayList<File>();
		
		// create a temp folder
		UUID uuid = UUID.randomUUID();
		String tempDirPath = snapDbList.get(0).getParentFile() + "/temp_" + uuid.toString();
		new File(tempDirPath).mkdir();
		
		// add a copy of each of the snapDb's into the temp folder
		for(File f:snapDbList){
			File newFile = new File(tempDirPath + "/" + f.getName());
			try {
				copyFileUsingFileChannels(f, newFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			newSnapDbList.add(newFile);
		}
		
		// return the list of snapDb copies stored in the temp folder
		return newSnapDbList;
	}
	
	
	private static void copyFileUsingFileChannels(File source, File dest)
			throws IOException {
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} finally {
			inputChannel.close();
			outputChannel.close();
		}
	}
	
	
	public static String extractRotationName(File file){
		String delims = "[-.]+";
		String filename = file.getName();
    	ArrayList<String> rotationNameTokens = new ArrayList<String>(Arrays.asList(filename.split(delims)));
    	// rotationNameTokens = (ArrayList<String>) Arrays.asList(filename.split(delims));
    	// System.out.println("     " + file.getName());
    	rotationNameTokens.remove(0);
    	rotationNameTokens.remove(rotationNameTokens.size()-1);
    	String rotationName = "";
    	for(int i=0;i<rotationNameTokens.size(); i++){
    		String s = rotationNameTokens.get(i);
    		rotationName += s;
    		if(i < rotationNameTokens.size() - 1){
    			rotationName += "-";
    		}
    	}
    	return rotationName;
	}

}
