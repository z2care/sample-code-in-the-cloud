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
package com.pragprog.aebook.persistchat.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.pragprog.aebook.persistchat.ChatRoom;
import com.pragprog.aebook.persistchat.ChatMessage;
import com.pragprog.aebook.persistchat.ChatMessageList;
import com.pragprog.aebook.persistchat.client.ChatSubmissionService;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class ChatSubmissionServiceImpl extends RemoteServiceServlet implements
		ChatSubmissionService {
    

	@SuppressWarnings("unchecked")
	public ChatMessageList getMessages(String chat) {
        PersistenceManager persister = Persister.getPersistenceManager();
        try {
            Query query = persister.newQuery(ChatMessage.class);
            query.setFilter("chat == desiredRoom");
            query.declareParameters("String desiredRoom");
            query.setOrdering("date");
            List<ChatMessage> messages =  (List<ChatMessage>)query.execute(chat);
            // Get the most recent message.
            ChatMessage lastMessage = messages.get(messages.size() - 1);//(1)
            ChatMessageList result = new ChatMessageList(chat, lastMessage.getDate());
            result.addMessages(messages);
            return result;
        } finally {
            persister.close();
        }
	}
	
	public void postMessage(ChatMessage message) {
        PersistenceManager persister = Persister.getPersistenceManager();
	    try {
	        persister.makePersistent(message);
	        Date timestamp = new Date();//(2)
	        message.setDate(timestamp);
	        Query query = persister.newQuery();//(3)
	        query.setFilter("chat == " + message.getChat());
	        List<ChatRoom> chats = (List<ChatRoom>) query.execute();
	        ChatRoom chat = chats.get(0);//(4)
	        chat.updateLastMessageDate(timestamp);//(5) 
	    } finally {
	        persister.close();
	    }
	}
/*	
    @SuppressWarnings("unchecked")
    public List<ChatRoom> getChats() {
        PersistenceManager persister = Persister.getPersistenceManager();
        try {
            Query query = persister.newQuery(ChatRoom.class);
            query.setOrdering("date");
            return (List<ChatRoom>)query.execute();
        } finally {
            persister.close();
        }
    }
*/
	
    @SuppressWarnings("unchecked")
    public List<ChatRoom> getChats() {
        PersistenceManager persister = Persister.getPersistenceManager();
        try {
            Query query = persister.newQuery(ChatRoom.class);
            query.setOrdering("date");
            List<ChatRoom> result = (List<ChatRoom>)query.execute();
            if (result.isEmpty()) {
                return initializeChats();
            } else {
                return result;
            }
        } finally {
            persister.close();
        }
    }
    
    public void addChat(String chat) {
        PersistenceManager persister = Persister.getPersistenceManager();
        try {
            ChatRoom newchat = new ChatRoom(chat, new Date());
            persister.makePersistent(newchat);
        } finally {
            persister.close();
        }
    }
    
    static final String[] DEFAULT_ROOMS = new String[] { "chat", "book", "java", "python"  }; 
    
    public List<ChatRoom> initializeChats() {
        List<ChatRoom> rooms = new ArrayList<ChatRoom>();
        Date now = new Date();
        PersistenceManager persister = Persister.getPersistenceManager();
        for (String name : DEFAULT_ROOMS) {
            ChatRoom r = new ChatRoom(name, now);
            persister.makePersistent(r);
            rooms.add(r);
        }
        persister.close();
        return rooms;
    }



    @SuppressWarnings("unchecked")
    public ChatMessageList getMessagesSince(String chat, Date timestamp) {
        PersistenceManager persister = Persister.getPersistenceManager();
        try {
            Query query = persister.newQuery(ChatMessage.class);
            query.declareParameters("String desiredRoom, Date earliest");//(6)
            query.setFilter("chat == desiredRoom && date > earliest"); //(7)
            query.setOrdering("date");
            List<ChatMessage> messages =  (List<ChatMessage>)query.execute(chat);
            // Get the most recent message.
            ChatMessage lastMessage = messages.get(messages.size() - 1);
            ChatMessageList result = new ChatMessageList(chat, lastMessage.getDate());
            result.addMessages(messages);
            return result;
        } finally {
            persister.close();
        }
    }
}
