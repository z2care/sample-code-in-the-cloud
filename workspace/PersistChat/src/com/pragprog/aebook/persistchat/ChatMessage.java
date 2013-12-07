/***
 * Excerpted from "Code in the Cloud",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/mcappe for more book information.
***/
package com.pragprog.aebook.persistchat;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(identityType = IdentityType.APPLICATION)//(1)
public class ChatMessage {
	
    public ChatMessage() {
    }

    public ChatMessage(String sender, String msg, String chatname) {
	this.senderName = sender;
	this.message = msg;
	this.chat = chatname;
    }
    @PrimaryKey //(2)
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
    
    @Persistent //(3)
	protected String senderName;
    
    @Persistent
	protected String message;
    
    @Persistent
	protected String chat;
    
    @Persistent
	protected long date;
    
    public Key getKey() {
	return key;
    }
    
    public String getSenderName() {
	return senderName;
    }
    
    public void setSenderName(String senderName) {
	this.senderName = senderName;
    }
    
    public String getMessage() {
	return message;
    }
    
    public void setMessage(String message) {
	this.message = message;
    }
    
    public String getChat() {
	return chat;
    }
    
    public void setChat(String chat) {
	this.chat = chat;
    }
    
    public long getDate() {
	return date;
    }
    
    public void setDate(long date) {
	this.date = date;
    }
}
