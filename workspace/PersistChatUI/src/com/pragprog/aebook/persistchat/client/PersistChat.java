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
package com.pragprog.aebook.persistchat.client;

import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.pragprog.aebook.persistchat.ChatMessage;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class PersistChat implements EntryPoint {
    /**
     * The message displayed to the user when the server cannot be reached or
     * returns an error.
     */
    private static final String SERVER_ERROR = "An error occurred while "
            + "attempting to contact the server. Please check your network "
            + "connection and try again.";

    /**
     * Create a remote service proxy to talk to the server-side Greeting
     * service.
     */
    private final ChatSubmissionServiceAsync chatService = GWT
            .create(ChatSubmissionService.class);

    private String chat;

    /**
     * Sets up a call and callback to retrieve the list of available chats. When
     * the server responds, this will create the link widgets, and add them to
     * the chatListPanel.
     * 
     * @param chatListPanel
     */
    public void populateChats(final VerticalPanel chatListPanel) {
        chatService.getChats(new AsyncCallback<List<String>>() {// (1)
                    public void onFailure(Throwable caught) {// (2)
                        chatListPanel.add(new Label("Couldn't retrieve chats"));
                    }

                    public void onSuccess(List<String> chats) {// (3)
                        for (String chat : chats) {
                            Button chatButton = new Button(chat); // (4)
                            chatListPanel.add(chatButton);
                            PersistChat.this.setupChatClickHandler(chatButton,
                                    chat);// (5)
                        }
                    }
                });
    }


    protected void setupChatClickHandler(Button chatButton, String chat) {
        // TODO Auto-generated method stub

    }

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        final VerticalPanel mainVert = new VerticalPanel(); // (6)

        final VerticalPanel topPanel = new VerticalPanel(); // (7)
        final HorizontalPanel midPanel = new HorizontalPanel();
        final VerticalPanel bottomPanel = new VerticalPanel();
        mainVert.add(topPanel); // (8)
        mainVert.add(midPanel);
        mainVert.add(bottomPanel);

        final Label title = new Label("AppEngine Chat"); // (9)
        final Label subtitle = new Label(new Date().toString());
        title.addStyleName("title"); // (10)
        mainVert.add(title);// (11)
        mainVert.add(subtitle);

        final VerticalPanel chatList = new VerticalPanel(); // (12)
        final Label chatLabel = new Label("CHATS");
        chatList.add(chatLabel);
        chatList.setWidth("20em");
        populateChats(chatList);
        final TextArea text = new TextArea();
        text.setWidth("60em");
        midPanel.add(chatList);
        midPanel.add(text);

        final TextBox messageBox = new TextBox(); // (13)
        final Button sendButton = new Button("send");
        bottomPanel.add(messageBox);
        bottomPanel.add(sendButton);

        setupSendMessageHandlers(sendButton, messageBox); // (14)

        RootPanel.get().add(mainVert); // (15)

        // focus the cursor on the message box.
        messageBox.setFocus(true); // (16)
        messageBox.selectAll();

        // Create a new timer
        Timer elapsedTimer = new Timer() {
            long timestamp = System.currentTimeMillis();

            public void run() {
                long new_timestamp = System.currentTimeMillis();
                chatService.getMessagesSince(timestamp,
                        new AsyncCallback<List<ChatMessage>>() {
                            public void onFailure(Throwable caught) {
                                // If a periodic update fails, don't worry about
                                // it.
                            }

                            public void onSuccess(List<ChatMessage> newMessages) {
                                addNewMessages(newMessages);
                            }
                        });
                timestamp = new_timestamp;
            }
        };
        // Schedule the timer for every 1/2 second (500 milliseconds)
        elapsedTimer.scheduleRepeating(500);
    }

    private void setupSendMessageHandlers(final Button sendButton,
            final TextBox messageBox) {
        // Create a handler for the sendButton and nameField
        class SendMessageHandler implements ClickHandler,// (17)
                KeyUpHandler {
            /** Fired when the user clicks on the sendButton. */
            public void onClick(ClickEvent event) {// (18)
                sendMessageToServer();
            }

            /** Fired when the user types in the nameField. */
            public void onKeyUp(KeyUpEvent event) {// (19)
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    sendMessageToServer();
                }
            }

            /** Send a chat message to the server. */
            private void sendMessageToServer() { // (20)
                ChatMessage chatmsg = new ChatMessage(getUser(), messageBox
                        .getText(), getCurrentChat());
                messageBox.setText("");
                chatService.postMessage(chatmsg,
                        new AsyncCallback<List<ChatMessage>>() {
                            public void onFailure(Throwable caught) {
                                PersistChat.this.addNewMessage(new ChatMessage(
                                        "System", "Error sending message: "
                                                + caught.getMessage(),
                                        getCurrentChat()));
                            }

                            public void onSuccess(List<ChatMessage> newMessages) {
                                PersistChat.this.addNewMessages(newMessages);
                            }
                        });
            }
        }
        SendMessageHandler handler = new SendMessageHandler();// (21)
        sendButton.addClickHandler(handler);
        messageBox.addKeyUpHandler(handler);
    }


    public String getUser() {
        // TODO Auto-generated method stub
        return null;
    }

    protected String getCurrentChat() {
        return chat;
    }

    protected void addNewMessages(List<ChatMessage> newMessages) {
        // TODO Auto-generated method stub

    }

    protected void addNewMessage(ChatMessage newMessage) {
    }

}
