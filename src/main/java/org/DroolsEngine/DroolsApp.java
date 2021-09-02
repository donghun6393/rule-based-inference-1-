package org.DroolsEngine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

//import org.DroolsEngine.DroolsController;
//import org.DroolsEngine.DroolsEngineInput;
//import org.DroolsEngine.DroolsEngineOutput;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.emitter.Emitter.Listener;
import javax.swing.*;
import java.awt.*;


public class DroolsApp {
	
	static Socket socket;
	
	//, String serverSendURL
	public static void executeInferenceEngine(String serverURL, String posture) throws Exception {

		String SERVER_URL = serverURL;
		//String SERVER_SEND_URL = serverSendURL;

		// get json data from Nodejs Server
		JSONObject obj = getData(SERVER_URL);
		
		JSONObject inputDeviceInfo = (JSONObject) obj.get("deviceInfo");
		
		String inputTime = (String) inputDeviceInfo.get("time");
		String inputModelId = (String) inputDeviceInfo.get("modelId");
		String inputWorkerId = (String) inputDeviceInfo.get("workerId");
		String inputWorkerStencilId = (String) inputDeviceInfo.get("workerStencilId");
		
		JSONObject inputPositionData = (JSONObject) inputDeviceInfo.get("positionData");
		
		Number positionXnum = (Number) inputPositionData.get("x");
		Double positionX = positionXnum.doubleValue();
		Number positionYnum = (Number) inputPositionData.get("y");
		Double positionY = positionYnum.doubleValue();

		// output of posture detection module
		String poseName = posture;

		//positionX != null && positionY != null && 
		if (positionX != null && positionY != null && poseName != null) {

			// rule engine
			DroolsEngineInput droolsEngineInput = new DroolsEngineInput();

			droolsEngineInput.positionX = positionX;
			droolsEngineInput.positionY = positionY;
			droolsEngineInput.poseName = poseName;
			
			droolsEngineInput.timeStamp = inputTime;
			droolsEngineInput.modelId = inputModelId;
			droolsEngineInput.workerId = inputWorkerId;
			droolsEngineInput.workerStencilId = inputWorkerStencilId;

			DroolsEngineOutput droolsEngineOutput = new DroolsEngineOutput();

			droolsEngineOutput = DroolsController.inputForDrools(droolsEngineInput);

			// rule engine output
			System.out.println("---------------------------------------------------------------------");
			System.out.println();
			System.out.println("INPUT FOR INFERENCE ENGINE: ");
			System.out.println("The coordinate (X,Y,Z) of worker is [(" + droolsEngineInput.getPositionX() + ", "
					+ droolsEngineInput.getPositionY() + ", 0.0" + ")].");
			System.out.println("The name of worker posture is [" + droolsEngineInput.getPoseName() + "].");

			System.out.println();
			System.out.println("THE OUTPUT OF INFERENCE ENGINE: ");
			System.out.println("The name of location is [" + droolsEngineOutput.getLocationName() + "].");
			System.out.println("The name of workspace is [" + droolsEngineOutput.getTaskName() + "].");
			System.out.println("The name of posture is [" + droolsEngineOutput.getPoseName() + "].");
			System.out.println("The weight for the location and workspace is ["
					+ droolsEngineOutput.getLocationNtaskWeight() + "].");
			System.out.println("The weight for the posture is [" + droolsEngineOutput.getPoseWeight() + "].");
			System.out.println("The risk level is [" + droolsEngineOutput.getRiskLevel() + "].");
			System.out.println();
			System.out.println("---------------------------------------------------------------------");

			// set string value of output
			// already exist positionX, positionY
			String timeStampValue = droolsEngineOutput.getTimeStamp();
			String modelIdValue = droolsEngineOutput.getModelId();
			String workerIdValue = droolsEngineOutput.getWorkerId();
			String workerStencilIdValue = droolsEngineOutput.getWorkerStencilId();
		
			String poseNameValue = droolsEngineOutput.getPoseName();
			String accValue = null;
			String riskLevelValue = droolsEngineOutput.getRiskLevel();
			String locationNameValue = droolsEngineOutput.getLocationName();
			String taskNameValue = droolsEngineOutput.getTaskName();
			
			HashMap<String, Object> positionData = new HashMap<String, Object>();
			positionData.put("x", positionX);
			positionData.put("y", positionY);
			positionData.put("z", 0);
			
			// String workerMovingActionType, workerOperationType;
			String workerMovingActionType = poseNameValue + "|" + accValue;
			String workerOperationType = riskLevelValue + "|" + locationNameValue + "|" + taskNameValue;
					
			HashMap<String, Object> deviceInfo = new HashMap<String, Object>();
			deviceInfo.put("time", timeStampValue);
			deviceInfo.put("modelId", modelIdValue);
			deviceInfo.put("workerid", workerIdValue);
			deviceInfo.put("workerStencilId", workerStencilIdValue);			
			deviceInfo.put("positionData", positionData);
			deviceInfo.put("workerMovingActionType", workerMovingActionType);
			deviceInfo.put("workerOperationType", workerOperationType);

			HashMap<String, Object> outputJson = new HashMap<String, Object>();
			outputJson.put("deviceInfo", deviceInfo);
			
			
			JSONObject data = makeJsonObject(outputJson);
			
			try {
	            socket = IO.socket("http://rnd.dexta.kr:34801");
	            socket.emit("add motiondata", data);
	            socket.connect();
	         } catch (URISyntaxException e) {
	            e.printStackTrace();
	         }
	         
	         //String name = data.getClass().getSimpleName();
	         //System.out.println(name);
	         
	         // send output json data to Nodejs Server
	         //sendData(serverURL, outputJson);
	      }
	   }
			/*
			try {
				socket = IO.socket("http://rnd.dexta.kr:34801/");
				socket.emit("add motiondata",data);
				socket.connect();
				System.out.println(data);
			} catch (URISyntaxException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			*/
			
			/*
			socket = IO.socket("http://rnd.dexta.kr:34801/");
			System.out.println("connected to server!");
			
			JSONObject data = makeJsonObject(outputJson);
			System.out.println(data);
			socket.send("add motiondata",data);
			socket.connect();
			System.out.println("test");
			*/
			// send output json data to Nodejs Server
			
			//sendData(outputJson);
			//SERVER_SEND_URL,
		
	

	private static JSONObject makeJsonObject(HashMap<String, Object> jsonData) {
		JSONObject json = new JSONObject();
		jsonData.forEach((key, value) -> {
			json.put(key, value);
		});
		return json;
	}

	public static JSONObject getData(String targetUrl) throws Exception {
		URL url = new URL(targetUrl);
		final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		JSONParser parser = new JSONParser();
		JSONObject responseJson = null;
		try (AutoCloseable a = () -> conn.disconnect()) {
			conn.setRequestMethod("GET");
			conn.setDoOutput(true); // false

			int responseCode = conn.getResponseCode();
			if (responseCode != 200) {
				System.out.println("Error");
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			responseJson = (JSONObject) parser.parse(sb.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return responseJson;
	}
	//Map<String, Object>
		
	/*
	public static void sendData(HashMap<String, Object> jsonData) throws URISyntaxException{
		//String targetUrl,
		Map<String, Object> response = new HashMap<>();
		Socket socket;
		//socket = IO.socket("http://rnd.dexta.kr:34800/");
		
		try {
			socket = IO.socket("http://rnd.dexta.kr:34801/");
			JSONObject data = makeJsonObject(jsonData);
			socket.emit("add motiondata", data);
			System.out.println(data);
			socket.connect();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		//return response;
	}
	*/
	}
	
//		try {
//			socket = IO.socket("http://rnd.dexta.kr:34800/");
//			socket.on(Socket.EVENT_CONNECT,(Object... objects) -> {
//			JSONObject data = makeJsonObject(jsonData);
//			socket.emit("add motiondata",data);
//			socket.connect();
//		)} catch (URISyntaxException e) {
//			// TODO: handle exception
//			e.printStackTrace();
//		}};
		
			
		/*	
				
			})
			//socket.connect();
			System.out.println("connected to server!");
		} catch (URISyntaxException e) {
			System.out.println("Failed....");
		}
			
		

		//System.out.println("Connected");
		socket.emit("add motiondata",data); 
		
		//System.out.println(data);
		//System.out.println("test");
		return response;
	}*/	
//		socket.on("add motiondata",new Emitter.Listener() {
//			@Override
//			public void call(Object... args) {
//				System.out.println("Connected2");
//				JSONObject data = makeJsonObject(jsonData);
//				socket.emit("data");
//				
//				try{
//					JSONObject json = makeJsonObject(jsonData);
//					socket.emit(json.toString());
//				} catch(Exception e) {
//					e.printStackTrace();	
//				}
//			}
//		});
		
	
	
//	public static Map<String, Object> sendData(String targetUrl, HashMap<String, Object> jsonData) throws Exception {
//		Map<String, Object> response = new HashMap<>();
//
//		URL url = new URL(targetUrl);
//		final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//		try (AutoCloseable a = () -> conn.disconnect()) {
//			conn.setRequestMethod("POST");
//			conn.setRequestProperty("Content-Type", "application/json");
//			conn.setDoOutput(true);
//
//			try (final OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(), "UTF-8")) {
//				JSONObject json = makeJsonObject(jsonData);
//				osw.write(json.toString());
//				osw.flush();
//			}
//
//			// response.put("status", conn.getResponseCode());
//			if (conn.getResponseCode() != 200) {
//				System.out.println("Fail to send data");
//				System.out.println(conn.getResponseCode());
//				return response;
//			}
//			return response;
//		}
//	}

