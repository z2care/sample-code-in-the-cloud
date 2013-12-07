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
package com.pragprog.aebook.chat.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ChatServiceAsync {
    
    void  postMessage(ChatMessage message, 
			AsyncCallback<Void> callback);
	  
	void getMessages(String chatroom, 
			AsyncCallback<ChatMessageList> callback);	

	void getMessagesSince(String chat, long timestamp,
			AsyncCallback<ChatMessageList> callback);

	void getChats(AsyncCallback<List<ChatRoom>> chats);

    void addChat(String chatname,
            AsyncCallback<Void> callback);

	void generateReport(String address, AsyncCallback<Void> callback);
}
