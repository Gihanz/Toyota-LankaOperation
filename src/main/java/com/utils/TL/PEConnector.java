package com.utils.TL;

import java.util.Properties;

import org.apache.log4j.Logger;

import filenet.vw.api.VWException;
import filenet.vw.api.VWFetchType;
import filenet.vw.api.VWRoster;
import filenet.vw.api.VWRosterQuery;
import filenet.vw.api.VWSession;
import filenet.vw.api.VWWorkObject;

public class PEConnector {

	private VWSession peSession;
	private String uname;
	private String password;
	private String cp;
	private String ceuri;
	private String jaas;
	private String rost;
	
	public static Logger log = Logger.getLogger(PEConnector.class);

	public PEConnector() {
		try {
			PropertyReader pr = new PropertyReader();
			Properties prop = pr.loadPropertyFile();
			uname =prop.getProperty("USERNAME");
			password =prop.getProperty("PASSWORD");
			ceuri =prop.getProperty("CEURI");
			cp =prop.getProperty("CONNECTION_POINT");
			jaas=prop.getProperty("JAAS_PATH");
			rost=prop.getProperty("ROSTER");
			log.info("Filenet Connection Data - Username:"+uname+", Password:#######, CE Uri:"+ceuri+", Connection point:"+cp );
			System.out.println("Filenet Connection Data - Username:"+uname+", Password:#######, CE Uri:"+ceuri+", Connection point:"+cp );
		
		} catch (Exception e) {
			log.error("Error Occured while initiating connector class");
		}
	}

	public VWSession getPESession(String username,String password,String cpName,String ceuri) {
		
		log.info("Inside Get PE connection Method");
		System.out.println("Inside Get PE Connection Method");

		System.setProperty("java.security.auth.login.config", jaas);
		
		try{
			peSession = new VWSession();
			peSession.setBootstrapCEURI(ceuri);
			peSession.logon(username,password,cpName);
			log.info("Session in : "+peSession.isLoggedOn());
			log.info("Sn : "+peSession.getPEServerName());
		}catch (VWException e) {
	          log.info("Exception occured while establishing PE session.");
	          e.printStackTrace();
	    }
		return peSession;
	}

	public boolean deleteWorkObject(String[] requisitionNumbers) {
		
		VWSession pesession=null;
		boolean returnVal=false;
		try
		{	
			pesession= getPESession(uname, password, cp, ceuri);
			pesession.isLoggedOn();
			final VWRoster roster = pesession.getRoster(rost);
			roster.setBufferSize(1);

			int queryFlags=VWRoster.QUERY_NO_OPTIONS;
			String filter="";
			if(requisitionNumbers.length>0){
				for(int i=0; i<requisitionNumbers.length; i++){
					if(i == requisitionNumbers.length-1){
						filter= filter+"TPP_RequisitionNumber ='"+requisitionNumbers[i]+"'";
					}else{
						filter= filter+"TPP_RequisitionNumber ='"+requisitionNumbers[i]+"' OR ";
					}	
				}
			}else{
				filter="TPP_RequisitionNumber ='xxxxxxxxx'";
			}	
			log.info("filter : "+filter);
			VWRosterQuery query=roster.createQuery(null, null, null, queryFlags, filter, null, VWFetchType.FETCH_TYPE_WORKOBJECT);
			
			log.info("Number of cases found : "+query.fetchCount());
			System.out.println("Number of cases found : "+query.fetchCount());

			while (query.hasNext()) {
				VWWorkObject xc = (VWWorkObject)query.next();
				log.info("Merging WobNum : "+xc.getWorkflowNumber());
				xc.doDelete(true, true);
				returnVal=true;
			}
			
		} catch (Exception e) {
			log.error("Error Occured While Getting WorkObject");
		}finally {
			disconnect(pesession);
		}
		return returnVal;
	}
	
	public void disconnect(VWSession session) {
		
		if (log.isDebugEnabled()){
			log.info("> disconnect PE");
		}
		if (session != null && session.isLoggedOn()){
			log.info("Inside PE Logging OFF");
			session.logoff();
			session = null;
		}
		if (log.isDebugEnabled()){
			log.info("< disconnected PE Successfully");
		}
	}

}

