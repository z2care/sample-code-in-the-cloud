/***
 * Excerpted from "Code in the Cloud",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/mcappe for more book information.
***/
function SetUpAndSendRequest(time, chat) {
  var request = new XMLHttpRequest(); //(1)
  var transcript = document.getElementById("chat-transcript");//(2)  

  request.onreadystatechange = function() {//(3)
    // Wait until the request is done. Done == ready state 4.
    if (request.readyState != 4) {
      return;
    }
    var xmlData = request.responseXML.documentElement;//(4)
    if (xmlData != null) {
	  transcript.innerHTML = "";
      messages = xmlData.getElementsByTagName("p"); 
      for (var x = 0; x < messages.length; x++) { //(5)
        transcript.innerHTML += "<p>" +  
            messages[x].childNodes[0].nodeValue + "</p>";
      }
      newtime = xmlData.getAttribute("time");//(6)
      SetUpAndSendRequest(newtime, chat);//(7)
    } else {
      transcript.innerHTML +=
          "<p>Sorry, but there was an error updating this chat</p>";
    }
  }
  request.open("GET", "latest?time=" + time + "&chat=" + chat, true);//(8)
  request.send();
}

function init() {
  SetUpAndSendRequest(0, "book")
}

window.onload = init;




