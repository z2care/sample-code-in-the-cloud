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
public class Chat implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final ChatServiceAsync chatService = GWT
			.create(ChatService.class);
	
	private TextArea text;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
        final VerticalPanel mainVert = new VerticalPanel(); // (1)

        final VerticalPanel topPanel = new VerticalPanel(); // (2)
        final HorizontalPanel midPanel = new HorizontalPanel();
        final HorizontalPanel bottomPanel = new HorizontalPanel();
        mainVert.add(topPanel); // (3)
        mainVert.add(midPanel);
        mainVert.add(bottomPanel);
        
        final Label title = new Label("AppEngine Chat"); // (4)
        final Label subtitle = new Label(new Date().toString());
        title.addStyleName("title"); // (5)
        topPanel.add(title);// (6)
        topPanel.add(subtitle);

        final VerticalPanel chatList = new VerticalPanel(); // (7)
        chatList.setBorderWidth(2); 
        final Label chatLabel = new Label("Chats");
        chatLabel.addStyleName("emphasized");//(8)
        chatList.add(chatLabel);
        chatList.setWidth("10em");//(9)
        populateChats(chatList); //(10)
        // "TextArea text" is defined as a field of the class, so that
        // the textarea can be referenced by handler methods.
        text = new TextArea();//(11)
        text.addStyleName("messages");
        text.setWidth("60em");
        text.setHeight("20em");
        midPanel.add(chatList);
        midPanel.add(text);

        final Label label = new Label("Enter Message:");
        label.addStyleName("bold");
        final TextBox messageBox = new TextBox(); 
        messageBox.setWidth("60em");
        final Button sendButton = new Button("send");
        bottomPanel.add(label);
        bottomPanel.add(messageBox);
        bottomPanel.add(sendButton);
        setupSendMessageHandlers(sendButton, messageBox);

        RootPanel.get().add(mainVert); 

        // focus the cursor on the message box.
        messageBox.setFocus(true); 
        messageBox.selectAll();
        setupTimedUpdate();
	}

	private void setupTimedUpdate() {
        // Create a new timer
        Timer elapsedTimer = new Timer() {
            public void run() {                
                chatService.getMessagesSince(getCurrentChat(), lastMessageTime,
                        new MessageListCallback());
            }
        };
        // Schedule the timer for every 1/2 second (500 milliseconds)
        elapsedTimer.scheduleRepeating(500);
	}

    /**
     * Sets up a call and callback to retrieve the list of available chats. When
     * the server responds, this will create the link widgets and add them to
     * the chatListPanel.
     * 
     * @param chatListPanel
     */
    public void populateChats(final VerticalPanel chatListPanel) {
        chatService.getChats(new AsyncCallback<List<String>>() { //(12)
            public void onFailure(Throwable caught) { //(13)
                chatListPanel.add(new Label("Couldn't retrieve chats: " + caught));
            }

            public void onSuccess(List<String> chats) {//(14)
                for (String chat : chats) {
                    Button chatButton = new Button(chat);
                    chatListPanel.add(chatButton);
                    Chat.this.setupChatClickHandler(chatButton,//(15)
                            chat);
                }
                setCurrentChat(chats.get(0));

            }
        });
    }

    protected void setupChatClickHandler(final Button chatButton, final String chat) {
        chatButton.addClickHandler(new ClickHandler() { 
            public void onClick(ClickEvent event) {
                setCurrentChat(chat);
                text.setText("Current chat: " + chat + "\n");
                currentChat = chat;
                chatService.getMessages(currentChat, new MessageListCallback());
            }
        });
    }

    public class MessageListCallback implements AsyncCallback<List<ChatMessage>> {

        public void onFailure(Throwable caught) {
        }

        public void onSuccess(List<ChatMessage> result) {
            addNewMessages(result);
        }        
    }
    
    protected void addNewMessages(List<ChatMessage> newMessages) {
        StringBuilder content = new StringBuilder();
        content.append(text.getText());
        for (ChatMessage cm : newMessages) {
            content.append(renderChatMessage(cm));
        }
        text.setText(content.toString());
    }
    
    protected String renderChatMessage(ChatMessage msg) {
        Date d = new Date(msg.getDate());
        String dateStr = d.getMonth() + "/" + d.getDate() + " " +
            d.getHours() + ":" + d.getMinutes() + "." +
            d.getSeconds();
        return "[From: " + msg.getSenderName() + " at " +
            dateStr  + "]: " + msg.getMessage() + "\n";  
    }

    protected void addNewMessage(ChatMessage newMessage) {
        text.setText(text.getText() + renderChatMessage(newMessage));
    }



    private void setupSendMessageHandlers(final Button sendButton,
            final TextBox messageBox) {
        // Create a handler for the sendButton and nameField
        class SendMessageHandler implements ClickHandler,// (16)
                KeyUpHandler {
            /** Fired when the user clicks on the sendButton. */
            public void onClick(ClickEvent event) {// (17)
                sendMessageToServer();
            }

            /** Fired when the user types in the nameField. */
            public void onKeyUp(KeyUpEvent event) {// (18)
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    sendMessageToServer();
                }
            }

            /** Send a chat message to the server. */
            private void sendMessageToServer() { // (19)
                ChatMessage chatmsg = new ChatMessage(user,
                        messageBox.getText(), getCurrentChat());
                messageBox.setText("");
                chatService.postMessage(chatmsg,
                        new AsyncCallback<Void>() {
                    public void onFailure(Throwable caught) {
                        Chat.this.addNewMessage(new ChatMessage(
                                "System", "Error sending message: " +
                                caught.getMessage(),
                                getCurrentChat()));
                    }

                    public void onSuccess(Void v) {
                        chatService.getMessagesSince(getCurrentChat(), 
                                lastMessageTime,
                                new MessageListCallback());
                    }
                });
            }
        }
        SendMessageHandler handler = new SendMessageHandler();// (20)
        sendButton.addClickHandler(handler);
        messageBox.addKeyUpHandler(handler);
    }

	protected void AddPostedMessages(List<ChatMessage> newMessages) {
		// TODO Auto-generated method stub
		
	}
}
