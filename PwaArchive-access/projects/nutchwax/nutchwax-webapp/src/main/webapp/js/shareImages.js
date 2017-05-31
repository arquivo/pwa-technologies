function shortenCurrentURL() {
    gapi.client.setApiKey('AIzaSyB7R8gTEu34CTfTBL8rolvjZOchKg2RyAA');
    gapi.client.load('urlshortener', 'v1', function() { 
        var Url = window.location.href;
        var request = gapi.client.urlshortener.url.insert({
            'resource': {
            'longUrl': Url
            }
        });
        request.execute(function(response) {

            if (response.id != null) {
              $('#shortURL').html(response.id);
              addthis.update('share', 'url', $('#shortURL').html());
            }
            else {
                alert("Error: creating short url \n" + response.error);
            }
        });
    });
}

$( function() {
    $( "#dialog" ).dialog({
    width: 350,
    autoOpen: false,
    dialogClass: "test",
    modal: true,
    responsive: true
    });

    $("#dButton").click(function () {
       $("#dialog").dialog('open');
    });
    $("#dialogClose").click(function () {
       $("#dialog").dialog('close');
    });    
} );

function getDateSpaceFormated(ts){
  var year = ts.substring(0, 4);
  var month = ts.substring(4, 6);
  month = Content.months[month];
  var day = ts.substring(6, 8);
  if(day.charAt(0) === '0' ){
    day = day.charAt(1);
  }
  return day + " "+ month + ", " +year;
}  

function initClipboard(linkCopied){
    var clipboard = new Clipboard('#btnCopy');
    clipboard.on('success', function(e) {
      $('#h2Copy').html(linkCopied);
      //e.clearSelection();
    });    
}