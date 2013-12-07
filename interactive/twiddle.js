/***
 * Excerpted from "Code in the Cloud",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/mcappe for more book information.
***/

<script type="text/javascript"> //(1)
function highlightMessages() {
  var chatBlock = document.getElementById("chat-transcript"); //(2)
  var chatMessages = doc.getElementsByName("other"); //(3)
  for (c in chatMessages) {
    // Change the class attribute to be "other-highlight"
    for (a in c.attributes) {
      if (a.name == "class") {
        a.value = "other-highlight";//(4)
        break;
      }
    }
  }
}
</script>

<input type="button" value="Highlight Others"  //(5)
       id="HighlightButton" onclick="highlightMessages()"/>
