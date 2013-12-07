/***
 * Excerpted from "Code in the Cloud",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/mcappe for more book information.
***/
package com.pragprog.aebook.persistchat.server;

import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.pragprog.aebook.persistchat.ChatMessage;
import com.pragprog.aebook.persistchat.client.ChatService;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class ChatSubmissionServiceImpl extends RemoteServiceServlet implements
		ChatService {

	@SuppressWarnings("unchecked")
	public List<ChatMessage> getMessages(String chat) {
		PersistenceManager persister = Persister.getPersistenceManager();
		try {
			Query query = persister.newQuery(ChatMessage.class);
			query.setFilter("chat == desiredRoom");
			query.declareParameters("String desiredRoom");
			query.setOrdering("date");
			return (List<ChatMessage>)query.execute(chat);
		} finally {
			persister.close();
		}
	}

	public List<ChatMessage> postMessage(ChatMessage message) {
		PersistenceManager persister = Persister.getPersistenceManager();
		persister.makePersistent(message);
		persister.close();
		return getMessages(message.getChat());
	}

	@SuppressWarnings("unchecked")
    public List<ChatMessage> getMessagesSince(String room, Date timestamp) {
	    PersistenceManager persister = Persister.getPersistenceManager();
	    try {
	        Query query = persister.newQuery(ChatMessage.class);
	        query.declareParameters("Date timestamp, String chat");
	        query.setFilter("date > timestamp && chat == room");
	        query.setOrdering("date");
	        return (List<ChatMessage>)query.execute(timestamp, room);
	    } finally {
	        persister.close();
	    }
	}
	
}
