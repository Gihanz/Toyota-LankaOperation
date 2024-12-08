package com.utils.TL;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

public class SendEmail {
	
	private static String SMTP_HOST_NAME;
	private static String SMTP_PORT;
	private static String SMTP_AUTH_USER;
	private static String SMTP_AUTH_PWD;
	private static String FROM_USER;
	private static String TIME_OUT ;
	private static String LOGO_PATH ;
	
	public static Logger log = Logger.getLogger(SendEmail.class);
	
	public SendEmail() {
	    try {
	    	PropertyReader pr = new PropertyReader();
	    	Properties prop = pr.loadPropertyFile();
	    	SMTP_HOST_NAME = prop.getProperty("SMTP_HOST_NAME");
	    	SMTP_PORT = prop.getProperty("SMTP_PORT");
	    	SMTP_AUTH_USER = prop.getProperty("SMTP_AUTH_USER");
	    	SMTP_AUTH_PWD = prop.getProperty("SMTP_AUTH_PWD");
	        FROM_USER = prop.getProperty("FROM_USER");
	        TIME_OUT = prop.getProperty("TIME_OUT");
	        LOGO_PATH = prop.getProperty("LOGO_PATH");	
	        
		} catch(Exception e) {
			System.out.println("Error : " +e.fillInStackTrace());
			log.info("Error : " +e.fillInStackTrace());
		}
	}
	
	public void send(String to, String cc, String subject, String emailContent, File[] attachments) throws Exception {
		
		boolean sessionDebug = false;

        Properties props = System.getProperties();
        props.put("mail.smtp.host", SMTP_HOST_NAME);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.smtp.starttls.required", "false");
        props.put("mail.smtp.timeout", TIME_OUT);
        
        Authenticator auth = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(SMTP_AUTH_USER, SMTP_AUTH_PWD);
			}
		};

        log.info("E-Mail Sending...");
      //java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        Session mailSession = Session.getInstance(props, auth);
        mailSession.setDebug(sessionDebug);
        Message msg = new MimeMessage(mailSession);
        msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
	    msg.addHeader("format", "flowed");
	    msg.addHeader("Content-Transfer-Encoding", "8bit");
        msg.setFrom(new InternetAddress(FROM_USER));
        
      //multiple address in Adress TO		
		List<String> toRecipientsArray = Arrays.asList(to.split("\\s*,\\s*"));

		InternetAddress[] addressTo = new InternetAddress[toRecipientsArray.size()];
		for (int i=0; i<toRecipientsArray.size(); i++)
		{
			addressTo[i] = new InternetAddress(toRecipientsArray.get(i).toString()) ;
		}
		msg.setRecipients(Message.RecipientType.TO, addressTo);
		
	  //multiple address in Adress CC	
		if(cc != null){
			List<String> ccRecipientsArray = Arrays.asList(cc.split("\\s*,\\s*"));

			InternetAddress[] addressCC = new InternetAddress[ccRecipientsArray.size()];
			for (int i=0; i<ccRecipientsArray.size(); i++)
			{
				addressCC[i] = new InternetAddress(ccRecipientsArray.get(i).toString()) ;
			}
			msg.addRecipients(Message.RecipientType.CC, addressCC);
		}
		   
        msg.setSubject(subject); 
        msg.setSentDate(new Date());
        
     // creates multi-part
        Multipart multipart = new MimeMultipart();
        
     // creates message part
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(emailContent, "text/html");
        multipart.addBodyPart(messageBodyPart);
        
     // creates attachments part
        if(attachments != null){
        	for(int x=0; x<attachments.length; x++){
            	BodyPart attachmentBodyPart = new MimeBodyPart();
                DataSource dSource = new FileDataSource(attachments[x]);
                attachmentBodyPart.setDataHandler(new DataHandler(dSource));
                attachmentBodyPart.setFileName(attachments[x].getName());
                attachmentBodyPart.setHeader("Content-ID", "attachment");
                multipart.addBodyPart(attachmentBodyPart);       	
            }	
        }
        
     // setting toyota logo
        BodyPart logoImagePart = new MimeBodyPart();
        DataSource img = new FileDataSource(LOGO_PATH);
        logoImagePart.setDataHandler(new DataHandler(img));
        logoImagePart.setHeader("Content-ID", "<logo>");
        logoImagePart.setFileName("logo.png");
        logoImagePart.setDisposition(MimeBodyPart.INLINE);
        multipart.addBodyPart(logoImagePart);
        
        msg.setContent(multipart);
        Transport.send(msg);
        
        System.out.println("E-Mail sent successfully");
        log.info("E-Mail sent Successfully. To:"+to+", Cc:"+cc+", Subject:"+subject+", Content:"+emailContent);	
	}
	
}
