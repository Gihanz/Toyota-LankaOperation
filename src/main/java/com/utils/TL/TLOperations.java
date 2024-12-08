package com.utils.TL;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class TLOperations {
	
	private static Properties prop;
	public static Logger log = Logger.getLogger(TLOperations.class);
	
	public TLOperations(){
		try{
			PropertyReader pr = new PropertyReader();
	    	prop = pr.loadPropertyFile();
	    	
			String pathSep = System.getProperty("file.separator");
	        String logpath = prop.getProperty("LOG4J_FILE_PATH");
	        String activityRoot = prop.getProperty("LOG_PATH");
			String logPropertyFile =logpath+pathSep+"log4j.properties"; 
	
			PropertyConfigurator.configure(logPropertyFile);
			PropertyReader.loadLogConfiguration(logPropertyFile, activityRoot+"/TLOperations/", "TLOperations.log");
		}catch(Exception e){
			System.out.println("Error : " +e.fillInStackTrace());
			log.info("Error : " +e.fillInStackTrace());
		}	
	}
	
	public void sendEmailNotification(String to, String referenceNumber, String wobNum, String status, String requisitionNumber, String purchaseType, String purchaseCategory, String goodsCategory, String deliveryCriticality){
			
		log.info(">>>>>>> Entered into sendMailNotification "+wobNum+" >>>>>>>");
		try{		
			String subject = prop.getProperty("SUBJECT").replace("<<ReferenceNumber>>", referenceNumber);
			String msgString = prop.getProperty((status.replaceAll("\\s","").toUpperCase()));
            msgString = msgString.replace("<<RequisitionNumber>>", requisitionNumber);
            msgString = msgString.replace("<<PurchaseType>>", purchaseType);
            msgString = msgString.replace("<<PurchaseCategory>>", purchaseCategory);
            msgString = msgString.replace("<<GoodsCategory>>", goodsCategory);
            msgString = msgString.replace("<<DeliveryCriticality>>", deliveryCriticality);
            String fontColor = "black";
            if(deliveryCriticality.equalsIgnoreCase("Very High")){
            	fontColor = "red";
            }else if(deliveryCriticality.equalsIgnoreCase("High")){
            	fontColor = "orange";
            }else if(deliveryCriticality.equalsIgnoreCase("Medium")){
            	fontColor = "blue";
            }else if(deliveryCriticality.equalsIgnoreCase("Low")){
            	fontColor = "green";
            }
            msgString = msgString.replace("<<FontColor>>", fontColor);
            String messageText = msgString.replace("<<ReferenceNumber>>", referenceNumber);

            SendEmail mail=new SendEmail();
            mail.send(to, null, subject, messageText, null);
			
		}catch(Exception e){
			System.out.println("Error : " +e.fillInStackTrace());
			log.info("Error : " +e.fillInStackTrace());
		}
	}

	public void sendEmailToVendor(String to, String oPNumber, String wobNum){
		
		log.info(">>>>>>> Entered into sendEmailToVendor "+wobNum+" >>>>>>>");
		try{
			String subject = prop.getProperty("VENDORSEMAIL_SUBJECT").replace("<<PONumber>>", oPNumber);
			String messageText = prop.getProperty("VENDORSEMAIL_CONTENT").replace("<<PONumber>>", oPNumber);
			
			// Getting CPU-HOD Email address
			URL url = new URL(prop.getProperty("CPUHOD_EMAIL_SERVICE"));
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			StringBuilder stringBuilder = new StringBuilder();
			String output;
			while ((output = br.readLine()) != null) {
				System.out.println(output);
				stringBuilder.append(output);
			}
			conn.disconnect();
			String cc = stringBuilder!=null ? stringBuilder.substring(1, stringBuilder.length()-1) : null;
			cc = cc.replace("\"", "");
			log.info("Email addresses from database : "+cc);
			
			//Getting PO pdf by crystal report
			POGenerator pog = new POGenerator();
			String resultKey = pog.getPOResultKey(oPNumber).get("result_key").toString();
			log.info("PO Result Key = "+resultKey);
			System.out.println("PO Result Key = "+resultKey);
			
			File files[]=new File[1];
			files[0]=pog.getPOPdf(resultKey);
			
			SendEmail mail=new SendEmail();
            mail.send(to, cc, subject, messageText, files);
			
		}catch(Exception e){
			System.out.println("Error : " +e.fillInStackTrace());
			log.info("Error : " +e.fillInStackTrace());
		}
	}
	
	public void removeMergedCases(String[] requisitionNumbers){
		
		log.info(">>>>>>> Entered into removeMergedCases >>>>>>>");
		try{
			PEConnector peCon = new PEConnector();
			peCon.deleteWorkObject(requisitionNumbers);
		}catch(Exception e){
			System.out.println("Error : " +e.fillInStackTrace());
			log.info("Error : " +e.fillInStackTrace());
		}
	}
	
	public static void main(String[] args) {
		
		TLOperations tl = new TLOperations();
		//tl.sendEmailNotification("frankdecroos@gmail.com", "ANU200287874", "274872893748927489", "Initiated", "1102", "Asset", "Fixed Asset", "Iron Safe & Steel Cabinet", "Low");
		tl.sendEmailToVendor("frankdecroos@gmail.com", "102", "2837498273947789");
		
		/*String[] req = new String[3];
		req[0]="1000000012";
		req[1]="1000000013";
		tl.removeMergedCases(req);*/
	}

}
