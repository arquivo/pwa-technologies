 imageObjs = []; /*Global array containing images*/

function shortenURL( longURL) {
    gapi.client.setApiKey('AIzaSyB7R8gTEu34CTfTBL8rolvjZOchKg2RyAA');
    gapi.client.load('urlshortener', 'v1', function() { 
        var request = gapi.client.urlshortener.url.insert({
            'resource': {
            'longUrl': longURL
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
            return false;
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
} );

startPosition = 0; //the default start position to get the images global variable

/*When user presses enter submits the input text*/
$(function() {
    $("#txtSearch").keypress(function (e) {
        if ((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) {
            $('#btnSubmit').click();
            return false;
        } else {
            return true;
        }
    });
}); 

function getMoreImages(){
    startPosition += 100;
    $('#moreResults').remove();
    $('#resultsUl').append('<li id="loadingMoreImages" style="text-align:center; margin-right: 1%; margin-bottom:1%; margin-top:4%; width:100%"> <div style="width: 100%; text-align:center"><div style="text-align:center;" class="cp-spinner cp-round"></div></div> </li>');
    
    searchImages(startPosition);
}

function loadingFinished(){
    $("#resultsUl").append('<li id="moreResults" style="width:100%; padding-top: 20px;"><button class="search-submit" style="float:none" onclick="getMoreImages()">Mais Resultados</button></li>');
    $('#loadingDiv').hide();
    $("#resultsUl").show();
    if($('#loadingMoreImages').length){
        $('#loadingMoreImages').remove();
    }
}

totalPosition = 0; //global variable

function doInitialSearch(){
    startPosition = 0;
    searchImages(startPosition);
}

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

/*Truncates large URL in the replay bar*/
function truncateUrl(url, maxSize)
{   /*remove https and http*/
    if(url.substring(0, "https://".length) === "https://"){
      url = url.substring(8,url.length);
    }else if (url.substring(0, "http://".length) === "http://"){
      url = url.substring(7,url.length);
    }       
    if (url.length > maxSize){
            url = url.substring(0, maxSize-3) + "...";
            return url;
    }
    else
        return url
}

lastPosition = -1; /*Global var refers to the lastImage the user*/

function expandImage(position){
    var arrowWidth = 16; //width of the arrow
    if(lastPosition != -1){
        $('#testViewer'+lastPosition).hide();
        $('#arrowWrapper'+lastPosition).hide();
        //$('#arrow'+lastPosition).fadeOut();

    }
    if(lastPosition === position){
        //you clicked twice in the same image, image closed no image selected
        lastPosition = -1;
        return false;
    }
    var arrowMarginLeft = $('#imageResults'+position).width()/2 - arrowWidth/2;
    $('#arrow'+position).css('margin-left', arrowMarginLeft+'px');
    $('#testViewer'+position).fadeIn();
    $('#arrowWrapper'+position).fadeIn();
    lastPosition = position;
//top.alert('top ' +  $('#date'+position).offset().top  );
    window.scrollTo( 0 , $('#date'+position).offset().top - 250);
return false;
}

function previousImage(position){
    var previousImageLi = $('#imageResults'+position).prev();
    if( previousImageLi.attr('position') != undefined){
        expandImage(parseInt(position)); /*Close current image*/
        expandImage(parseInt(previousImageLi.attr('position')));
    }
    return;
}
function nextImage(position){
    var nextImageLi = $('#imageResults'+position).next();
    if( nextImageLi.attr('position') != undefined){
        expandImage(parseInt(position)); /*Close current image*/
        expandImage(parseInt(nextImageLi.attr('position')));            
    }
    return;
}

function insertInPosition(position, imageObj, imageHeight, maxImageHeight, expandedImageHeight, expandedImageWidth){

var maxImageExpandHeight = 400; //for now fix later

var maxImageDivWidth =  ( ($(window).width() * 0.6) -70 ) * 0.95 ;

if(expandedImageHeight > maxImageExpandHeight){
    expandedImageHeight = maxImageExpandHeight;
} 
else if ( expandedImageWidth > maxImageDivWidth ){
    //resize height in porportion to resized width
    var ratio = maxImageDivWidth/expandedImageWidth;
    expandedImageHeight = expandedImageHeight * ratio;
}

var centerImage;

var centerImage = maxImageExpandHeight/2 - expandedImageHeight/2 ;

var liMarginTop = maxImageHeight - imageHeight;

var contentToInsert = '<li position='+position+' id="imageResults'+position+'" style="background:white; margin-right: 1%; margin-bottom:1%; margin-top:'+liMarginTop.toString()+'px;"><h2><button style="cursor:pointer;" onclick = "expandImage('+position+')"> <img style="padding:0px 0px 4px 0px;" height="'+imageHeight.toString()+'" src="'+imageObj.src+'"/> </button></h2> <p class="green" style="font-size:1em!important; text-align: left; padding-left:5px">'+truncateUrl(imageObj.originalURL, 20)+'</p> <p class="date" id="date'+position+'" style="font-weight:normal; font-size:1em!important; text-align: left; padding-left:5px;padding-bottom:2px;">'+getDateSpaceFormated(imageObj.timestamp)+'</p>'+
    '<div id="arrowWrapper'+position+'" class="arrowWrapper" ><div id="arrow'+position+'" class="arrow"/></div>' +
    '<div id="testViewer'+position+'" class="imageExpandedDiv"><button onclick = "expandImage('+position+')" class="expand__close" title="'+close+'"></button> <button onclick="previousImage('+position+')"  class="left__arrow" title="'+leftArrow+'"></button> <button onclick="nextImage('+position+')" class="right__arrow" title="'+rightArrow+'"></button> <div style="width: 60%; display: inline-block;float: left;"> <a target="_blank" href="//arquivo.pt/wayback/'+imageObj.timestamp+'/'+imageObj.originalURL+'"> <img style="max-width:'+maxImageDivWidth+'px; margin-left: 70px; margin-top:'+ centerImage+'px; margin-bottom:'+ centerImage+'px"class="imageExpanded" id="ExpandedImg'+position+'" src="'+imageObj.currentImageURL+'"> </a> </div> <div style="min-height: 500px;margin-top: -50px;width: 39%;display: inline-block; border-left: solid 1px #454545;"> <div style="padding-top:120px; margin-left: 25px; margin-right:70px; text-align: left;"> <h2 style="color:white; word-wrap: break-word;">'+truncateUrl(imageObj.originalURL, 80)+'</h2> <br/> <h2 style="color:white; font-weight:bold;word-wrap: break-word;" > '+getDateSpaceFormated(imageObj.timestamp)+' </h2> <div style="padding-top:20px; padding-bottom:50px;"> <h2 style="color:white;word-wrap: break-word;" > '+resolution+' '+parseInt(expandedImageWidth)+'x'+parseInt(expandedImageHeight)+'</h2></div><div style="display: inline; white-space: nowrap; overflow: hidden;"><a class="imageViewerAnchor" target="_blank" href="/wayback/'+imageObj.timestamp+'/'+imageObj.originalURL+'"><span class="imageViewerButton">'+visitPage+'</span></a><a target="_blank" class="imageViewerAnchor" style="margin-left: 20px"  href="'+imageObj.currentImageURL+'"><span class="imageViewerButton">'+showImage+'</span></a><button id="dButton" position='+position+'  class="imageViewerAnchor" style="margin-left: 20px"><span class="imageViewerButton" style="line-height:25px;">'+share+'</span></button></div>  </div> </div> </div>'+ '</li>';     

  /*href="/shareImage.jsp?imgurl='+encodeURIComponent(imageObj.currentImageURL)+'&imgrefurl='+encodeURIComponent(imageObj.originalURL)+'&imgrefts='+imageObj.timestamp+'&imgres='+parseInt(expandedImageWidth)+'x'+parseInt(expandedImageHeight)+'&query='+$('#txtSearch').val()+'"*/
  imageObj.expandedImageWidth = expandedImageWidth;
  imageObj.expandedImageHeight = expandedImageHeight;
  imageObjs[position] = imageObj;


var lengthofUL = $('#resultsUl li').length;
if(lengthofUL === 0){ /*list is empty only the hidden li*/
  $('#resultsUl').prepend(contentToInsert)
}
else{
  var inserted = false;
  for (var i = 0 ; i< lengthofUL; i ++){
    var insertedPos = $('#resultsUl li').eq(i).attr('position');
    if(position < insertedPos ){
      $('#resultsUl li:eq('+i+')').before(contentToInsert);
      inserted = true;
      break;
    }
  }
  if(inserted === false){
    $('#resultsUl').append(contentToInsert);
  }
}
}    

function encodeHtmlEntity(str) {

    str = str.replaceAll('ç','%26ccedil%3B')
             .replaceAll('Á','%26Aacute%3B')
             .replaceAll('á','%26aacute%3B')
             .replaceAll('À','%26Agrave%3B')
             .replaceAll('Â','%26Acirc%3B')
             .replaceAll('à','%26agrave%3B')
             .replaceAll('â','%26acirc%3B')
             .replaceAll('Ä','%26Auml%3B')
             .replaceAll('ä','%26auml%3B')
             .replaceAll('Ã','%26Atilde%3B')
             .replaceAll('ã','%26atilde%3B')
             .replaceAll('Å','%26Aring%3B')
             .replaceAll('å','%26aring%3B')
             .replaceAll('Æ','%26Aelig%3B')
             .replaceAll('æ','%26aelig%3B')
             .replaceAll('Ç','%26Ccedil%3B')
             .replaceAll('Ð','%26Eth%3B')
             .replaceAll('ð','%26eth%3B')
             .replaceAll('É','%26Eacute%3B')
             .replaceAll('é','%26eacute%3B')
             .replaceAll('È','%26Egrave%3B')
             .replaceAll('è','%26egrave%3B')
             .replaceAll('Ê','%26Ecirc%3B')
             .replaceAll('ê','%26ecirc%3B')
             .replaceAll('Ë','%26Euml%3B')
             .replaceAll('ë','%26euml%3B')
             .replaceAll('Í','%26Iacute%3B')
             .replaceAll('í','%26iacute%3B')
             .replaceAll('Ì','%26Igrave%3B')
             .replaceAll('ì','%26igrave%3B')
             .replaceAll('Î','%26Icirc%3B')
             .replaceAll('î','%26icirc%3B')
             .replaceAll('Ï','%26Iuml%3B')
             .replaceAll('ï','%26iuml%3B')
             .replaceAll('Ñ','%26Ntilde%3B')
             .replaceAll('ñ','%26ntilde%3B')
             .replaceAll('Ó','%26Oacute%3B')
             .replaceAll('ó','%26oacute%3B')
             .replaceAll('Ò','%26Ograve%3B')
             .replaceAll('ò','%26ograve%3B')
             .replaceAll('Ô','%26Ocirc%3B')
             .replaceAll('ô','%26ocirc%3B')
             .replaceAll('Ö','%26Ouml%3B')
             .replaceAll('ö','%26ouml%3B')
             .replaceAll('Õ','%26Otilde%3B')
             .replaceAll('õ','%26otilde%3B')
             .replaceAll('Ø','%26Oslash%3B')
             .replaceAll('ø','%26oslash%3B')
             .replaceAll('ß','%26szlig%3B')
             .replaceAll('Þ','%26Thorn%3B')
             .replaceAll('þ','%26thorn%3B')
             .replaceAll('Ú','%26Uacute%3B')
             .replaceAll('ú','%26uacute%3B')
             .replaceAll('Ù','%26Ugrave%3B')
             .replaceAll('ù','%26ugrave%3B')
             .replaceAll('Û','%26Ucirc%3B')
             .replaceAll('û','%26ucirc%3B')
             .replaceAll('Ü','%26Uuml%3B')
             .replaceAll('ü','%26uuml%3B')
             .replaceAll('Ý','%26Yacute%3B')
             .replaceAll('ý','%26yacute%3B')
             .replaceAll('ÿ','%26yuml%3B')
             .replaceAll('©','%26copy%3B')
             .replaceAll('®','%26reg%3B')
             .replaceAll('™','%26trade%3B')
             .replaceAll('&','%26amp%3B')
             .replaceAll('<','%26lt%3B')
             .replaceAll('>','%26gt%3B')
             .replaceAll('€','%26euro%3B')
             .replaceAll('¢','%26cent%3B')
             .replaceAll('£','%26pound%3B')
             .replaceAll('\"','%26quot%3B')
             .replaceAll('‘','%26lsquo%3B')
             .replaceAll('’','%26rsquo%3B')
             .replaceAll('“','%26ldquo%3B')
             .replaceAll('”','%26rdquo%3B')
             .replaceAll('«','%26laquo%3B')
             .replaceAll('»','%26raquo%3B')
             .replaceAll('—','%26mdash%3B')
             .replaceAll('–','%26ndash%3B')
             .replaceAll('°','%26deg%3B')
             .replaceAll('±','%26plusmn%3B')
             .replaceAll('¼','%26frac14%3B')
             .replaceAll('½','%26frac12%3B')
             .replaceAll('¾','%26frac34%3B')
             .replaceAll('×','%26times%3B')
             .replaceAll('÷','%26divide%3B')
             .replaceAll('α','%26alpha%3B')
             .replaceAll('β','%26beta%3B')
             .replaceAll('∞','%26infin%3B')
             .replaceAll(' ','+');
    return str;
}

$(document).ajaxStart(function(){
  if(startPosition == 0){
    $('#resultsUl').empty();        
    $('#loadingDiv').show();
  }
});

$(document).ajaxStop(function(){
  if(startPosition == 0){       
    $('#loadingDiv').hide();
  }
});

function initClipboard(linkCopied){
    var clipboard = new Clipboard('#btnCopy');
    clipboard.on('success', function(e) {
      $('#h2Copy').html(linkCopied);
    });
    return;    
}

function searchImagesJS(dateStartWithSlashes, dateEndWithSlashes, safeSearchOption,startIndex){
    if( safeSearchOption == "null"){
        safeSearchOption = "yes";
    }
    
    var input = $('#txtSearch').val();
    var tokens = input.split(' ');
    var size = '';
    for (var i= 0; i< tokens.length; i++){
      if(tokens[i].substring(0, 'size:'.length) === 'size:'){
        size += tokens[i].substring('size:'.length) + ' ';
      }
    }
    if( size === ''){
      size = 'all';
    }

    var dateStartTokenes = dateStartWithSlashes.split("/");
    var dateStartTs = dateStartTokenes[2]+ dateStartTokenes[1] + dateStartTokenes[0]+ "000000";

    var dateEndTokenes = dateEndWithSlashes.split("/");
    var dateEndTs = dateEndTokenes[2]+ dateEndTokenes[1] + dateEndTokenes[0]+ "000000"     ;

    var dateFinal = dateStartTs+"-"+dateEndTs; 
    var showAll = false;

    $.ajax({
    // example request to the cdx-server api - 'http://arquivo.pt/pywb/replay-cdx?url=http://www.sapo.pt/index.html&output=json&fl=url,timestamp'
       url: "//p27.arquivo.pt/getimagesWS",
       data: {
          query: input,
          stamp: dateFinal,
          start: startIndex,
          safeImage: safeSearchOption 
       },
       timeout: 300000,
       error: function() {
         console.log("Error In Ajax request to getimages");            
       },
       dataType: 'text',
       success: function(data) {

        var responseJson = $.parseJSON(data);

        if(startIndex == 0 ){
            $('#resultsUl').empty();
        }
        var totalResults = responseJson.totalResults;
        
        if ( totalResults === 0){
            loadingFinished();
        }
        else{
            for (var i=0; i< responseJson.totalResults; i++){
                var currentImageURL = responseJson.content[i].url;
                var originalURL = responseJson.content[i].urlOriginal;
                var thumbnail = responseJson.content[i].thumbnail;

                imageObj = new Image();
                imageObj.timestamp = responseJson.content[i].timestamp;
                imageObj.originalURL = originalURL;
                imageObj.currentImageURL = currentImageURL;
                imageObj.position = totalPosition;
                imageObj.expandedHeight = responseJson.content[i].height;
                imageObj.expandedWidth = responseJson.content[i].width;

                totalPosition = totalPosition + 1;

                imageObj.src = 'data:image/gif;base64,' + thumbnail;

                var resizeImageHeight = 200;

                imageObj.onload = function() {
                            
                    if( startIndex != 0 &&  totalResults == responseJson.totalResults){
                        $('#loadingMoreImages').remove();
                    }
                            totalResults --;
                               
                            if(this.height <= resizeImageHeight){
                            insertInPosition(this.position, this, this.height, resizeImageHeight, this.expandedHeight, this.expandedWidth);
                            }
                            else{
                            insertInPosition(this.position, this, resizeImageHeight, resizeImageHeight, this.expandedHeight, this.expandedWidth);
                            }
                       
                            if(totalResults <= 0){
                                loadingFinished();
                            }
                }
                
               
                imageObj.onerror = function() {
                    // image did not load
                    if( startIndex != 0 &&  totalResults == responseJson.totalResults){
                        $('#loadingMoreImages').remove();
                    }                
                    totalResults --;
                    console.log("Error loading: " + this.currentImageURL);
                    if(totalResults <= 0){
                        loadingFinished();
                    }
                }

            }
        }


       },
       type: 'GET'
    });

}

$(document).ready(function() {     
    $(document).on('click', '#dButton', function() {
      var position =  $(this).attr('position');
      var imageObj = imageObjs[position]; /*get Current Image Object*/
      var shareURL = '//' + window.location.hostname + '/shareImage.jsp?l='+language+'&imgurl='+encodeURIComponent(imageObj.currentImageURL)+'&imgrefurl='+encodeURIComponent(imageObj.originalURL)+'&imgrefts='+imageObj.timestamp+'&imgres='+parseInt(imageObj.expandedImageWidth)+'x'+parseInt(imageObj.expandedImageHeight)+'&query='+$('#txtSearch').val();
      shortenURL( shareURL);
      
      $('#h2Copy').html(clickToCopy);
      $("#dialog").dialog('open'); 
      return false;
    });
    $(document).on('click', '#dialogClose', function() {
      $("#dialog").dialog('close'); 
      return false;
    });            
});