package com.utils.TL;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.crystaldecisions.sdk.occa.report.application.ReportClientDocument;
import com.crystaldecisions.sdk.occa.report.data.ConnectionInfoKind;
import com.crystaldecisions.sdk.occa.report.data.IConnectionInfo;
import com.crystaldecisions.sdk.occa.report.data.ITable;
import com.crystaldecisions.sdk.occa.report.exportoptions.ReportExportFormat;
import com.crystaldecisions.sdk.occa.report.lib.PropertyBag;
import com.crystaldecisions.sdk.occa.report.lib.ReportSDKException;

public class POGenerator {
	
	private static String DBUSERNAME;
	private static String DBPASSWORD;
	private static String SERVERNAME;
	private static String PORT;
	private static String DATABASE_NAME;
	private static String PO_RPT_FILE_LOCATION ;
	private static String PO_RESULTKEY_SERVICE ;	
	
	public static Logger log = Logger.getLogger(POGenerator.class);
	
	public POGenerator() {
	    try{
	    	PropertyReader pr = new PropertyReader();
	    	Properties prop = pr.loadPropertyFile();
	    	DBUSERNAME = prop.getProperty("DBUSERNAME");
	    	DBPASSWORD = prop.getProperty("DBPASSWORD");
	    	SERVERNAME = prop.getProperty("SERVERNAME");
	    	PORT = prop.getProperty("PORT");
	    	DATABASE_NAME = prop.getProperty("DATABASE_NAME");
	    	PO_RPT_FILE_LOCATION = prop.getProperty("PO_RPT_FILE_LOCATION");
	        PO_RESULTKEY_SERVICE = prop.getProperty("PO_RESULTKEY_SERVICE");	
	        	        
		} catch(Exception e) {
			System.out.println("Error : " +e.fillInStackTrace());
			log.info("Error : " +e.fillInStackTrace());
		}
	}
	
	public File getPOPdf(String resultKey) {
		
		File tempFile = null;
		
        try {
            //information Oracle database
            String URI = "!oracle.jdbc.driver.OracleDriver!jdbc:oracle:thin:{userid}/{password}@" + SERVERNAME + ":" + PORT + "/" + DATABASE_NAME;  //1521/ or :1521
            String DATABASE_DLL = "crdb_jdbc.dll";
            
            String report = PO_RPT_FILE_LOCATION;
            ReportClientDocument clientDoc = new ReportClientDocument();     
            clientDoc.open(report, ReportExportFormat._PDF);

            // Obtain collection of tables from this database controller.
            ITable table = clientDoc.getDatabaseController().getDatabase().getTables().getTable(0);

            IConnectionInfo connectionInfo = table.getConnectionInfo();
            PropertyBag propertyBag = connectionInfo.getAttributes();
            propertyBag.clear();

            //Overwrite any existing properties with updated values.
            propertyBag.put("Trusted_Connection", "false");
            propertyBag.put("Server Name", SERVERNAME); //Optional property.
            propertyBag.put("Database Name", DATABASE_NAME);
            propertyBag.put("Server Type", "JDBC (JNDI)");
            propertyBag.put("URI", URI);
            propertyBag.put("Use JDBC", "true");
            propertyBag.put("Database DLL", DATABASE_DLL);
            connectionInfo.setAttributes(propertyBag);

            //Set database username and password.
            connectionInfo.setUserName(DBUSERNAME);
            connectionInfo.setPassword(DBPASSWORD);
            connectionInfo.setKind(ConnectionInfoKind.SQL);

            table.setConnectionInfo(connectionInfo);
            //Update old table in the report with the new table.
            clientDoc.getDatabaseController().setTableLocation(table, table);

            clientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "IFS_RESULT_KEY", resultKey);
            InputStream in = clientDoc.getPrintOutputController().export(ReportExportFormat.PDF);
            
            final String PREFIX = "Purchase Order";
            final String SUFFIX = ".pdf";
              
            tempFile = File.createTempFile(PREFIX, SUFFIX);
            tempFile.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                IOUtils.copy(in, out);
            }

        } catch (ReportSDKException ex) {
            System.out.println("ReportSDKException : " + ex.getSDKError());
            log.info("ReportSDKException : " + ex);
        } catch (Exception ex) {
            System.out.println("Exception : " + ex);
            log.info("Exception : " + ex);
        }
        
        return tempFile;
    }
	
	public JSONObject getPOResultKey(String pONo){
		
		JSONObject rtnData = null;
		try{
			// Getting IFS PO Result Key
			URL url = new URL(PO_RESULTKEY_SERVICE + pONo);
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
			
			JSONParser parse = new JSONParser();
			rtnData = (JSONObject)parse.parse(stringBuilder.toString());
			log.info("Response from IFS : "+rtnData);
			
		}catch(Exception e){
			System.out.println("Error : " +e.fillInStackTrace());
			log.info("Error : " +e.fillInStackTrace());
		}

		return rtnData;
	}

}
