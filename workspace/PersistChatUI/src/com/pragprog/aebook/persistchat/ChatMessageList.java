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
package com.pragprog.aebook.persistchat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ChatMessageList implements IsSerializable {

    private List<ChatMessage> messages;
    private Date time;
    private String chat;

    public ChatMessageList(String chat, Date time) {
      this.chat = chat;
      this.time = time;
      this.messages = new ArrayList<ChatMessage>();
    }
    
    /**
     * Default 0-argument constructor for GWT serialization.
     */
    public ChatMessageList() {
        messages = new ArrayList<ChatMessage>();
        time = new Date();
        chat = null;
    }
    
    public String getChat() {
        return chat;
    }
    
    public List<ChatMessage> getMessages() {
        return messages;
    }
    
    public Date getTimestamp() {
        return time;
    }
    
    public void addMessage(ChatMessage msg) {
        messages.add(msg);
    }
    
    public void addMessages(List<ChatMessage> messages) {
        messages.addAll(messages);
    }
}
