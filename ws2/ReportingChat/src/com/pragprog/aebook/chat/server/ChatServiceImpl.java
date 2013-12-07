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


import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.*;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.pragprog.aebook.chat.client.ChatMessage;
import com.pragprog.aebook.chat.client.ChatMessageList;
import com.pragprog.aebook.chat.client.ChatRoom;
import com.pragprog.aebook.chat.client.ChatService;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class ChatServiceImpl extends RemoteServiceServlet implements
		ChatService {
	
	@SuppressWarnings("unchecked")
	public ChatMessageList getMessages(String chat) {
        PersistenceManager persister = Persister.getPersistenceManager();
        try {
            Query query = persister.newQuery(PChatMessage.class);
            query.setFilter("chat == desiredRoom");
            query.declareParameters("String desiredRoom");
            query.setOrdering("date");
            List<PChatMessage> messages =  (List<PChatMessage>)query.execute(chat);
            // Get the most recent message.
            ChatMessageList result = null;
            if (messages.size() > 1) {
                PChatMessage lastMessage = messages.get(messages.size() - 1);//(1)
                result = new ChatMessageList(chat, lastMessage.getDate());
                for (PChatMessage pchatmsg : messages) {
                    result.addMessage(pchatmsg.asChatMessage());
                }
            } else {
                result = new ChatMessageList(chat, System.currentTimeMillis());
            }
            return result;
        } finally {
            persister.close();
        }
	}
	
	@SuppressWarnings("unchecked")
    public void postMessage(ChatMessage message) {
	    UserService userService = UserServiceFactory.getUserService();
	    User user = userService.getCurrentUser();
        PersistenceManager persister = Persister.getPersistenceManager();
	    try {
	        PChatMessage pmessage = new PChatMessage(user.getNickname(), message.getMessage(), message.getChat());
	        long timestamp = System.currentTimeMillis();//(2)
	        pmessage.setDate(timestamp);
	        persister.makePersistent(pmessage);

	        Query query = persister.newQuery(PChatRoom.class);//(3)
	        query.setFilter("name == " + message.getChat());
	        List<PChatRoom> chats = (List<PChatRoom>) query.execute();
	        PChatRoom chat = chats.get(0);//(4)
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
            Query query = persister.newQuery(PChatRoom.class);
            query.setOrdering("date");
            List<PChatRoom> rooms = (List<PChatRoom>)query.execute();
            if (rooms.isEmpty()) {
                return initializeChats(persister);
            } else {
                List<ChatRoom> result = new ArrayList<ChatRoom>();
                for (PChatRoom pchatroom : rooms) {
                    result.add(pchatroom.asChatRoom());
                }
                return result;
            }
        } 
        finally {
            persister.close();
        }
    }
    
    public void addChat(String chat) {
        PersistenceManager persister = Persister.getPersistenceManager();
        try {
            PChatRoom newchat = new PChatRoom(chat, System.currentTimeMillis());
            persister.makePersistent(newchat);
        } finally {
            persister.close();
        }
    }
    
    static final String[] DEFAULT_ROOMS = new String[] { "chat", "book", "java", "python"  }; 
    
    public List<ChatRoom> initializeChats(PersistenceManager persister) {
        List<ChatRoom> rooms = new ArrayList<ChatRoom>();
        List<PChatRoom> prooms = new ArrayList<PChatRoom>();
        long now = System.currentTimeMillis();
        for (String name : DEFAULT_ROOMS) {
            PChatRoom r = new PChatRoom(name, now);
            prooms.add(r);
            rooms.add(r.asChatRoom());
            persister.makePersistent(r);
        }
        return rooms;
    }



    @SuppressWarnings("unchecked")
    public ChatMessageList getMessagesSince(String chat, long timestamp) {
        PersistenceManager persister = Persister.getPersistenceManager();
        try {
            Query query = persister.newQuery(PChatMessage.class);
            query.declareParameters("String desiredRoom, int earliest");//(6)
            query.setFilter("chat == desiredRoom && date > earliest"); //(7)
            query.setOrdering("date");
            List<PChatMessage> messages =  (List<PChatMessage>)query.execute(chat, timestamp);
            ChatMessageList msgList = null;
            // Get the most recent message.
            if (messages.size() >= 1) {
                PChatMessage lastMessage = messages.get(messages.size() - 1);
                msgList = new ChatMessageList(chat, lastMessage.getDate());
            } else {
                msgList = new ChatMessageList(chat, System.currentTimeMillis());
            }
            for (PChatMessage msg : messages) {
                msgList.addMessage(msg.asChatMessage());
            }
            return msgList;
        } finally {
            persister.close();
        }
    }

	@Override
	public void generateReport(String address) {
		TaskOptions task = method(Method.GET).param("to", address);//(8)
		QueueFactory.getDefaultQueue().add(task); //(9)
	}
}
