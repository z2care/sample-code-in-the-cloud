/***
 * Excerpted from "Code in the Cloud",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/mcappe for more book information.
***/
package com.pragprog.aebook.persistchat.client;

import java.util.Date;
import java.util.List;

import com.pragprog.aebook.persistchat.ChatMessage;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ChatSubmissionServiceAsync {
    void  postMessage(ChatMessage messages,
		      AsyncCallback<List<ChatMessage>> callback);
    
    void getMessages(String chatroom,
		     AsyncCallback<List<ChatMessage>> callback);
    
    void getMessagesSince(String chat, Date timestamp,
			  AsyncCallback<List<ChatMessage>> callback);

    
}
