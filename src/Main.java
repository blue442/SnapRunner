import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
 

public class Main extends Application {
	
	static TextArea textArea;
	
	private Desktop desktop = Desktop.getDesktop();
	Map<String, File> snapInputs = new HashMap<String, File>();
	List<File> snapDbList = new ArrayList<File>();
	public Integer startYear = 2010;
	
	
    public static void main(String[] args){
        launch(args);
    }
    
    @Override
    public void start(Stage stage) {
    	
    	stage.setTitle("SnapRunner");
        
    	// trying to add table view... 
    	// stage.setWidth(300);
        // stage.setHeight(500);
    	
        // set up text area for output
        textArea = new TextArea();
        textArea.setPrefRowCount(10);
        textArea.setPrefColumnCount(20);
        textArea.setWrapText(true);
        textArea.setEditable(false);

    	
    	FileChooser fileChooser = new FileChooser();
    	
    	
        Button loadFilesButton = new Button("Choose snapDb's for processing...");
        Button fakeAnUpdate = new Button("Fake a db update");
        Button updateSlopeButton = new Button("Update slope data");
        Button updateSlopeLengthButton = new Button("Update slope length");
        Button updateSoilTextureButton = new Button("Update soil texture");
        Button updateAllButton = new Button("Update ALL the things");
        Button processSnapDbsButton = new Button("Process snapDb's");
        Button outputCsvs = new Button("Output csv files");
        
        updateSlopeButton.setDisable(true);
        fakeAnUpdate.setDisable(true);
        updateSlopeLengthButton.setDisable(true);
        updateSoilTextureButton.setDisable(true);
        updateAllButton.setDisable(true);
        processSnapDbsButton.setDisable(true);
        outputCsvs.setDisable(true);
        
        
        loadFilesButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                    	textArea.appendText("choosing files...\n");
                    	configureDbChooser(fileChooser);
                        snapDbList = fileChooser.showOpenMultipleDialog(stage);
                        snapDbList = Utils.updateSnapDbList(snapDbList);
                        fakeAnUpdate.setDisable(false);
                        updateSlopeButton.setDisable(false);
                        updateSlopeLengthButton.setDisable(false);
                        updateSoilTextureButton.setDisable(false);
                        updateAllButton.setDisable(false);
                        outputCsvs.setDisable(false);
                    }
                }
            );
        
        
        fakeAnUpdate.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                    	fakeAnUpdate.setDisable(true);
                        updateSlopeButton.setDisable(true);
                        updateSlopeLengthButton.setDisable(true);
                        updateSoilTextureButton.setDisable(true);
                        updateAllButton.setDisable(true);
                        for(File snapDb:snapDbList){
                        	textArea.appendText("updating slope of snapDb " + snapDb.getPath() + "\n");
                        	Utils.updateSnapdb(snapDb, "fake", startYear);
                        }
                        outputCsvs.setDisable(false);
                        processSnapDbsButton.setDisable(false);
                    }
                }
            );
        
        
        updateSlopeButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                    	fakeAnUpdate.setDisable(true);
                        updateSlopeButton.setDisable(true);
                        updateSlopeLengthButton.setDisable(true);
                        updateSoilTextureButton.setDisable(true);
                        updateAllButton.setDisable(true);
                        for(File snapDb:snapDbList){
                        	textArea.appendText("updating slope of snapDb " + snapDb.getPath() + "\n");
                        	Utils.updateSnapdb(snapDb, "slope", startYear);
                        }
                        outputCsvs.setDisable(false);
                        processSnapDbsButton.setDisable(false);
                    }
                }
            );
        
        updateSlopeLengthButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                    	fakeAnUpdate.setDisable(true);
                        updateSlopeButton.setDisable(true);
                        updateSlopeLengthButton.setDisable(true);
                        updateSoilTextureButton.setDisable(true);
                        updateAllButton.setDisable(true);
                        for(File snapDb:snapDbList){
                        	Utils.updateSnapdb(snapDb, "slopeLength", startYear);
                        	textArea.appendText("updating slopeLength of snapDb " + snapDb.getPath() + "\n");
                        }
                        outputCsvs.setDisable(false);
                        processSnapDbsButton.setDisable(false);
                    }
                }
            );
        
        updateSoilTextureButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                    	fakeAnUpdate.setDisable(true);
                        updateSlopeButton.setDisable(true);
                        updateSlopeLengthButton.setDisable(true);
                        updateSoilTextureButton.setDisable(true);
                        updateAllButton.setDisable(true);
                        for(File snapDb:snapDbList){
                        	Utils.updateSnapdb(snapDb, "soilTexture", startYear);
                        	textArea.appendText("updating soilTexture of snapDb " + snapDb.getPath() + "/n");
                        }
                        outputCsvs.setDisable(false);
                        processSnapDbsButton.setDisable(false);
                    }
                }
            );
        
        
        updateAllButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                    	fakeAnUpdate.setDisable(true);
                        updateSlopeButton.setDisable(true);
                        updateSlopeLengthButton.setDisable(true);
                        updateSoilTextureButton.setDisable(true);
                        updateAllButton.setDisable(true);
                        for(File snapDb:snapDbList){
                        	Utils.updateSnapdb(snapDb, "all", startYear);
                        	textArea.appendText("updating ALL the things of snapDb " + snapDb.getPath() + "/n");
                        }
                        outputCsvs.setDisable(false);
                        processSnapDbsButton.setDisable(false);
                    }
                }
            );
        
        processSnapDbsButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                    	processSnapDbsButton.setDisable(true);
                    	// ArrayList<String> rotationNameTokens = new ArrayList<String>();
                    	textArea.appendText("-----/n");
                    	
                        if (snapDbList != null) {
                        	System.out.println("Proccessing files: ");
                            for (File file : snapDbList) {
                            	textArea.appendText("running snap for snapDb " + file.getPath() + "\n");
                                // processSnapDb(file);
                            	String rotationName = Utils.extractRotationName(file);
                            	snapInputs.put(rotationName, file);
                            	System.out.println("File: " + snapInputs.get(rotationName) + " Rotation name: " + rotationName);
                            	String job_id = MonitorWindowsSnapJob.newJob(file, rotationName, startYear);
                            }
                        }
                        // packageCsv();
                        outputCsvs.setDisable(false);
                    }
                }
            );
        
        outputCsvs.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                    	textArea.appendText("choosing csv location...\n");
                    	configureDbChooser(fileChooser);
              
                        DirectoryChooser directoryChooser = new DirectoryChooser();
                        File selectedDirectory = directoryChooser.showDialog(stage);
                        
                    	outputCsvs.setDisable(true);
                    	packageCsv(selectedDirectory);
                    	textArea.appendText("All csv files have been created.\n");
                    }
                }
            );
        
        

        
        
        // put it all together in a gui
        
        final GridPane inputGridPane = new GridPane();
        
        GridPane.setConstraints(loadFilesButton, 0, 0);
        GridPane.setHalignment(loadFilesButton, HPos.CENTER);
        GridPane.setConstraints(fakeAnUpdate, 0, 1);
        GridPane.setHalignment(fakeAnUpdate, HPos.CENTER);
        GridPane.setConstraints(updateSlopeButton, 0, 2);
        GridPane.setHalignment(updateSlopeButton, HPos.CENTER);
        GridPane.setConstraints(updateSlopeLengthButton, 0, 3);
        GridPane.setHalignment(updateSlopeLengthButton, HPos.CENTER);
        GridPane.setConstraints(updateSoilTextureButton, 0, 4);
        GridPane.setHalignment(updateSoilTextureButton, HPos.CENTER);
        GridPane.setConstraints(updateAllButton, 0, 5);
        GridPane.setHalignment(updateAllButton, HPos.CENTER);
        GridPane.setConstraints(processSnapDbsButton, 0, 6);
        GridPane.setHalignment(processSnapDbsButton, HPos.CENTER);
        GridPane.setConstraints(outputCsvs, 0, 7);
        GridPane.setHalignment(outputCsvs, HPos.CENTER);
        GridPane.setConstraints(textArea, 0, 9);
        inputGridPane.setHgap(6);
        inputGridPane.setVgap(6);
        inputGridPane.getChildren().addAll(loadFilesButton, fakeAnUpdate, updateSlopeButton, updateSlopeLengthButton, updateSoilTextureButton, processSnapDbsButton, updateAllButton, outputCsvs, textArea);
 
        final Pane rootGroup = new VBox(12);
        rootGroup.getChildren().addAll(inputGridPane);
        rootGroup.setPadding(new Insets(12, 12, 12, 12));
        stage.setScene(new Scene(rootGroup));
        
//        final Pane rootGroup = new BorderPane();
//        rootGroup.setMinSize(300, 600);
//        rootGroup.getChildren().addAll(inputGridPane);
//        rootGroup.setPadding(new Insets(12, 12, 12, 12));
//        stage.setScene(new Scene(rootGroup));
//        stage.sizeToScene();

        stage.show();
        
        
        

    }
    
    
 
    private static void configureDbChooser(
            final FileChooser fileChooser) {      
                fileChooser.setTitle("Select snap databases");
                // fileChooser.setInitialDirectory(
                //     new File(System.getProperty("user.home"))
                // );                 
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter(".snapdb", "*.snapdb"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
                );
        }
    
    
	public String trackSnapJob(String jobUuid) {
		
		String res = null;
		if (jobUuid == null) {
			res = "trackSnapJob: bad request, expected <jobUuid> but got null - bad parms passed";
		}
		
		if (MonitorWindowsSnapJob.isJobRunning(jobUuid)) {
			if (MonitorWindowsSnapJob.isJobDone(jobUuid)) {
				res = MonitorWindowsSnapJob.getCompletedJobNode(jobUuid);
			}
			else {
				res = "{\"success\": true, \"msg\": \"still working\", " +
					"\"done\": false}";
			}
		} else {
			res = "job not null, but also not found.";
		}
		
		return res;
	}
	
	
	private void packageCsv(File outputDirectory){
		int i = 1;
    	if(snapInputs != null && snapInputs.size() != 0){
    		textArea.appendText("snapInputs contains " + snapInputs.size() + " things...\n");
        	for(String key:snapInputs.keySet()){
        		textArea.appendText("creating csv #  " + i + " for " + snapInputs.get(key).getPath() + "...\n");
        		Utils.outputRegressionCsv(snapInputs.get(key), key, outputDirectory);
        		i++;
        	}
    	} else {
    		textArea.appendText("snapDbList contains " + snapDbList.size() + " things...\n");
        	for(File snapDb:snapDbList){
    			textArea.appendText("creating csv #  " + i + " for " + snapDb.getPath() + "...\n");
    			String rotationName = Utils.extractRotationName(snapDb);
    			Utils.outputRegressionCsv(snapDb, rotationName, outputDirectory);
    			i++;
    		}
    	}
	}
	
	
	public static void appendTextArea(String text){
		textArea.appendText(text + "\n");
	}
		
}

