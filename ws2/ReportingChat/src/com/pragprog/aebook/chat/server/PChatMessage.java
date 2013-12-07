/***
 * Excerpted from "Code in the Cloud",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/mcappe for more book information.
***/
// Copyright 2010, Mark C. Chu-Carroll (markcc@gmail.com).
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.pragprog.aebook.chat.server;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.pragprog.aebook.chat.client.ChatMessage;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class PChatMessage {
	
	public PChatMessage() {
	}
	
	public PChatMessage(String sender, String msg, String chatname) {
		this.senderName = sender;
		this.message = msg;
		this.chat = chatname;
	}
	
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
		
	@Persistent
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
	
	public ChatMessage asChatMessage() {
	    ChatMessage result = new ChatMessage(senderName, message, chat);
	    result.setDate(date);
	    return result;
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
