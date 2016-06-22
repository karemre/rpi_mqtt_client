/*
 * Copyright 2014 IBM Corp. All Rights Reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.bluemixmqtt;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedReader;
import java.io.InputStreamReader;


import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class DeviceTest {

	private int count = 0;
	private int totalcount = 0;
	private MqttHandler handler = null;

	private  boolean pub = false;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new DeviceTest().doDevice();
	}

	/**
	 * Run the device
	 */
	public void doDevice() {
		//Read properties from the conf file
		Properties props = MqttUtil.readProperties("device.conf");

		String org = props.getProperty("org");
		String id = props.getProperty("deviceid");
		String authmethod = "use-token-auth";
		String authtoken = props.getProperty("token");
		//isSSL property
		String sslStr = props.getProperty("isSSL");
		boolean isSSL = false;
		if (sslStr.equals("T")) {
			isSSL = true;
		}

		System.out.println("org: " + org);
		System.out.println("id: " + id);
		System.out.println("authmethod: " + authmethod);
		System.out.println("authtoken: " + authtoken);
		System.out.println("isSSL: " + isSSL);

		String serverHost = org + MqttUtil.SERVER_SUFFIX;

		//Format: d:<orgid>:<type-id>:<divice-id>
		String clientId = "d:" + org + ":" + MqttUtil.DEFAULT_DEVICE_TYPE + ":"
				+ id;
		handler = new DeviceMqttHandler();
		handler.connect(serverHost, clientId, authmethod, authtoken, isSSL);

		//Subscribe the Command events
		//iot-2/cmd/<cmd-type>/fmt/<format-id>
		handler.subscribe("iot-2/cmd/" + MqttUtil.DEFAULT_CMD_ID + "/fmt/json",
				0);


		JSONObject jsonObj = new JSONObject();


                try {
     	        	jsonObj.put("message", "register");
                } catch (JSONException e1) {
                        e1.printStackTrace();
               	}

                //Publish device events to the app
                //iot-2/evt/<event-id>/fmt/<format> 
                handler.publish("iot-2/evt/" + MqttUtil.DEFAULT_EVENT_ID
                                        + "/fmt/json", jsonObj.toString(), false, 0);


		while (true) {
			
			System.out.println("asdfjds" + totalcount);


			if(pub){
				System.out.println("sdkfhsadjhfasdkfaskjd");

				jsonObj = new JSONObject();
                                        
                                 //Getting the runtime reference from system
                                 Runtime runtime = Runtime.getRuntime();
			  	 OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                                 
				try {
                                 	jsonObj.put("message", "SystemInfo");
                                        jsonObj.put("memory", operatingSystemMXBean.getFreePhysicalMemorySize());
                                 } catch (JSONException e1) {
                                        e1.printStackTrace();
                                 }


                                 //Publish device events to the app
                                 //iot-2/evt/<event-id>/fmt/<format> 
                                 handler.publish("iot-2/evt/" + MqttUtil.DEFAULT_EVENT_ID
                                                + "/fmt/json", jsonObj.toString(), false, 0);
				
				pub = false;
			}

			/*
			//Format the Json String
			JSONObject contObj = new JSONObject();
			JSONObject jsonObj = new JSONObject();


			try {
				contObj.put("count", count);
				contObj.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new Date()));
				jsonObj.put("d", contObj);
				jsonObj.put("memory", runtime.totalMemory());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}

			System.out.println("Send count as " + count);

			//Publish device events to the app
			//iot-2/evt/<event-id>/fmt/<format> 
			handler.publish("iot-2/evt/" + MqttUtil.DEFAULT_EVENT_ID
					+ "/fmt/json", jsonObj.toString(), false, 0);

			*/
			

			try {
				Thread.sleep(5 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			
		}

//		handler.disconnect();
	}

	/**
	 * This class implements as the device MqttHandler
	 *
	 */
	private class DeviceMqttHandler extends MqttHandler {

		@Override
		public void messageArrived(String topic, MqttMessage mqttMessage)
				throws Exception {
			
			System.out.println(topic);
			super.messageArrived(topic, mqttMessage);
			
			//Check whether the event is a command event from app
			if (topic.equals("iot-2/cmd/" + MqttUtil.DEFAULT_CMD_ID
					+ "/fmt/json")) {
				String payload = new String(mqttMessage.getPayload());
				JSONObject jsonObject = new JSONObject(payload);
				String cmd = jsonObject.getString("cmd");
				//Reset the count
				if (cmd != null && cmd.equals("reset")) {
					int resetcount = jsonObject.getInt("count");
					count = resetcount;
					System.out.println("Count is reset to " + resetcount);
				}else
			
				if (cmd != null && cmd.equals("SystemInfo")){
					
					pub = true;
					/*
					JSONObject jsonObj = new JSONObject();
					
					//Getting the runtime reference from system
               				Runtime runtime = Runtime.getRuntime();

                       			try {
						jsonObj.put("message", "SystemInfo");
                               			jsonObj.put("memory", runtime.totalMemory());
                       			} catch (JSONException e1) {
                               			 e1.printStackTrace();
                        		}


		                        //Publish device events to the app
                		        //iot-2/evt/<event-id>/fmt/<format> 
                       			 handler.publish("iot-2/evt/" + MqttUtil.DEFAULT_EVENT_ID
                                       			 + "/fmt/json", jsonObj.toString(), false, 0);
				       	*/
				
				}else if(cmd != null && cmd.equals("killFirst")){
					Process p; 
               				 try{

                       				 p = Runtime.getRuntime().exec("sh docker.sh");
                       				 int value =  p.waitFor();
               				 }catch(Exception e){
                       				 e.printStackTrace();
                			 }
				}
			}
		}
	}

}
