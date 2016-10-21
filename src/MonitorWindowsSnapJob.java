import java.io.*;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.concurrent.*;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;


//------------------------------------------------------------------------------
// 	Class that manages watching the heavy lifting part of snap+ uploads. The windows
//		server does the heavy lifting by running Snap+....
//------------------------------------------------------------------------------
public class MonitorWindowsSnapJob implements Callable<MonitorWindowsSnapJob> {

	//--------------------------------------------------------------------------
	// Static data and methods (Instance stuffs belowwwwww)
	//--------------------------------------------------------------------------
	private static final String mSnapExe = "\"C:\\Program Files (x86)\\UWSoils\\SnapPlusV2Beta\\SnapPlusV2.exe\"";
	private static final Integer MAX_TRIES = 4;
	private static final Long WAIT_DURATION = 15L; // in minutes
	public static Integer startYear;
	
	// Active snap+ jobs on the windows server will have an entry in the snap job map
	private static Map<String, FutureTask<MonitorWindowsSnapJob>> mSnapJobMap = 
			new HashMap<String, FutureTask<MonitorWindowsSnapJob>>();
	private static Map<String, File> mSnapFiles = new HashMap<String, File>();
	
	// Don't use newCachedThreadPool - there are no limits on it, 
	//	it allows adding threads until the server is crippled...
	private static ExecutorService mJobExecutor = Executors.newFixedThreadPool(4);

	//--------------------------------------------------------------------------
	public static String newJob(File snapDbFile, String rotationName, Integer startYear) {
	
		String jobHandle = UUID.randomUUID().toString();
		
		MonitorWindowsSnapJob job = new MonitorWindowsSnapJob(snapDbFile, rotationName, startYear);
		
		FutureTask<MonitorWindowsSnapJob> snapFuture = new FutureTask<MonitorWindowsSnapJob>(job);
		mSnapJobMap.put(jobHandle, snapFuture);
		mSnapFiles.put(jobHandle, snapDbFile);
		mJobExecutor.execute(snapFuture);
		
		return jobHandle;
	}
	
	//--------------------------------------------------------------------------
	public static Map<String, Boolean> getJobStatuses(){
		Map<String, Boolean> jobStatuses = new HashMap<String, Boolean>();
		for(String key:mSnapJobMap.keySet()){
			jobStatuses.put(key, mSnapJobMap.get(key).isDone());
		}
		
		return jobStatuses;
	}
	
	
	//--------------------------------------------------------------------------
	public static Boolean isJobRunning(String job_uuid) {
		if (mSnapJobMap.containsKey(job_uuid)) {
			return true;
		}
		return false;
	}

	// returns false if job doesn't exist or isn't running. Might just exception if
	//	job doesn't exist...
	//--------------------------------------------------------------------------
	public static Boolean isJobDone(String job_uuid) {
		if (isJobRunning(job_uuid)) {
			FutureTask<MonitorWindowsSnapJob> job = mSnapJobMap.get(job_uuid);
			if (job != null) {
				return job.isDone();
			}
		}
		return false;
	}
	
	//--------------------------------------------------------------------------
	public static String getCompletedJobNode(String job_uuid) {
		if (!isJobDone(job_uuid)) {
			Alert jobNotDoneAlert = new Alert(AlertType.CONFIRMATION, "MonitorWindowsSnapJob: getCompletedJobResult: job not actually done");
			jobNotDoneAlert.showAndWait()
		      .filter(response -> response == ButtonType.OK);
		      // .ifPresent(response -> formatSystem());
			// Logger.error("MonitorWindowsSnapJob: getCompletedJobResult: job not actually done");  
			return "{\"success\": false, \"msg\": \"Job is not actually done\", " +
					"\"done\": false}";
		}
		FutureTask<MonitorWindowsSnapJob> future = mSnapJobMap.remove(job_uuid);
		if (future != null) {
			MonitorWindowsSnapJob job = null;
			try {
				job = future.get();
			}
			catch(Exception e) {
				Alert futureErrorAlert = new Alert(AlertType.CONFIRMATION, "EMonitorWindowsSnapJob: getCompletedJobResult: job future get exception");
				futureErrorAlert.showAndWait()
			      .filter(response -> response == ButtonType.OK);
				// Logger.error("MonitorWindowsSnapJob: getCompletedJobResult: job future get exception");
				e.printStackTrace();
				return "{\"success\": false, \"msg\": \"Job done but something went wrong\", " +
					"\"done\": false}";
			}
			if (job != null) {
				return "{\"success\": " + job.mOk + ", \"msg\": \"" + job.mMessage + "\", " +
					"\"done\": true}" ;
			}
		}
		Alert futureErrorAlert = new Alert(AlertType.CONFIRMATION, "MonitorWindowsSnapJob: getCompletedJobResult: job done but couldn't remove it");
		futureErrorAlert.showAndWait()
	      .filter(response -> response == ButtonType.OK);
		
		// Logger.error("MonitorWindowsSnapJob: getCompletedJobResult: job done but couldn't remove it");
		return "{\"success\": false, \"msg\": \"Job done but something went wrong\", " +
					"\"done\": false}";
	}
	
		
	//--------------------------------------------------------------------------
	// Instance data and methods
	//--------------------------------------------------------------------------
	private File mSnapDbFile;
	private String mRotationName;
	
	private Boolean mOk; 
	private String mMessage;

	//--------------------------------------------------------------------------
	MonitorWindowsSnapJob(File snapDbFile, String rotationName, Integer startYear) {
		mSnapDbFile = snapDbFile;
		mRotationName = rotationName;
		mOk = false;
		mMessage = "";
		MonitorWindowsSnapJob.startYear = startYear;
	}
	
	//--------------------------------------------------------------------------
	@Override
	public MonitorWindowsSnapJob call() throws Exception {
	
		// Update the csv
		// System.out.println("MonitorWindowsSnapJob: about to update db");
		// System.out.println("MonitorWindowsSnapJob: updated db - moving on...");
		
		// Make a bat file
		//---------------------
		String exe = mSnapExe + " " + mSnapDbFile.getAbsolutePath() + " " + mRotationName + "\"run 2010\"";
		System.out.println(mSnapExe);
		Main.appendTextArea("executing command: " + exe);
    	Process p = null;
    	Integer tryCount = 0;
		Boolean result = false;
    	
    	while(result == false && tryCount < MAX_TRIES) {
			try {
				String uuidTempFileName = UUID.randomUUID().toString() + ".temp";
				File tempOutputFile = new File(uuidTempFileName);
				java.util.Date date = new java.util.Date();
				Main.appendTextArea("Starting job for rotation " + mRotationName + " at " + date.toString());
				p = Runtime.getRuntime().exec(exe);
				
				FileOutputStream fos = new FileOutputStream(tempOutputFile);
				PrintStream ps = new PrintStream(fos);
				System.setErr(ps);
				System.setOut(ps);
				
				// StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");            
	            // StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT", fos);
	            //     
	            // errorGobbler.start();
	            // outputGobbler.start();
	            
				result = p.waitFor(WAIT_DURATION, TimeUnit.MINUTES);
				
				// tempOutputFile.delete();
				
				try {
				    Files.delete(tempOutputFile.toPath());
				} catch (NoSuchFileException x) {
				    System.err.format("%s: no such" + " file or directory%n", tempOutputFile.toPath());
				} catch (DirectoryNotEmptyException x) {
				    System.err.format("%s not empty%n", tempOutputFile.toPath());
				} catch (IOException x) {
				    // File permission problems are caught here.
				    System.err.println(x);
				}
				
				// Utils.outputCsv(mSnapDbFile, mRotationName);
				
			} catch (Exception e) {
				Utils.raiseExceptionAlert(e);
				// System.out.println("Error in proccess!");
				// e.printStackTrace();
			}
			if (result == false) {
				Main.appendTextArea("Snap processing for " + mSnapDbFile.getName() + " did not complete for try(" + tryCount + ")");
			}
			tryCount++;
		}
    	
		if (result == false) {
			// EEEPs problem
			p.destroyForcibly();
			this.mMessage = "Forcibly destroyed snap process!";
		}
		else {
			java.util.Date date = new java.util.Date();
			Main.appendTextArea("Completed job for rotation " + mRotationName + " at " + date.toString());
		    // Logger.debug("snap job complete!");
			this.mMessage = "Something good";
			this.mOk = true;
		}
		return this;
	}
	
}
	
	
	
	
