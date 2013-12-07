/***
 * Excerpted from "Code in the Cloud",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/mcappe for more book information.
***/
package com.pragprog.aebook.chat.server;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class Reporter extends HttpServlet {
	
	Logger logger = Logger.getLogger(Reporter.class.getName());

	@Override
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String toAddress = req.getParameter("to");
		PersistenceManager persister = Persister.getPersistenceManager();//(1)
		Query query = persister.newQuery(PChatMessage.class);
		query.setFilter("date >= yesterday");
		query.declareParameters("long yesterday");
		query.setOrdering("date");
		long yesterday = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
		List<PChatMessage> messages =
		    (List<PChatMessage>)query.execute(yesterday);
		resp.setContentType("text/html");
		
		PrintWriter out = new PrintWriter(new CharArrayWriter());//(2)
		out.println("<html>");
		out.println("  <head>");
		out.println("    <title>Chat Usage Report</title>");
		out.println("  </head>");
		out.println("  <body>");
		out.println("  <h1>Chat Usage Report</h1>");
		out.println("  <p> Messages in the last 24 hours: " +
	        messages.size());
		out.println("</body></html>");
		out.close();
		String report = out.toString();

		Session mailSession =
		    Session.getDefaultInstance(new Properties(), null);//(3)
	    try {
	      Message msg = new MimeMessage(mailSession);
	      msg.setFrom(new InternetAddress(toAddress));
	      msg.addRecipient(Message.RecipientType.TO,
	          new InternetAddress(req.getParameter("to")));
	      msg.setSubject("Chat Status Report");
	      msg.setText(report);
	      Transport.send(msg);
	   } catch (AddressException e) {
		   // Email address is constant and valid, so this
		   // can't happen.
	   } catch (MessagingException e) {
		   logger.log(Level.INFO, "Error sending report: " + e);
	   }		
	}
}
