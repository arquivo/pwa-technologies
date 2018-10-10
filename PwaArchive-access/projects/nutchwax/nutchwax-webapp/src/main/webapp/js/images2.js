 jQuery.browser = {};
(function () {
    jQuery.browser.msie = false;
    jQuery.browser.version = 0;
    if (navigator.userAgent.match(/MSIE ([0-9]+)\./)) {
        jQuery.browser.msie = true;
        jQuery.browser.version = RegExp.$1;
    }
})();


 imageObjs = []; /*Global array containing images*/
 imageDigests = []; /*Global array unique image digests*/
 noMoreResults = false; /*Global variable control if there are no more results*/

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
    responsive: true,
    resizable: false
    });
    $( "#detailsDialog" ).dialog({
    width: 650,
    autoOpen: false,
    dialogClass: "test",
    modal: true,
    responsive: true,
    resizable: false
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
    $('#moreResults').remove();
    $('#resultsUl').append('<li id="loadingMoreImages" style="text-align:center; margin-right: 1%; margin-bottom:1%; margin-top:4%; width:100%"> <div style="width: 100%; text-align:center"><div class="sk-fading-circle"><div class="sk-circle1 sk-circle"></div><div class="sk-circle2 sk-circle"></div><div class="sk-circle3 sk-circle"></div><div class="sk-circle4 sk-circle"></div><div class="sk-circle5 sk-circle"></div><div class="sk-circle6 sk-circle"></div><div class="sk-circle7 sk-circle"></div><div class="sk-circle8 sk-circle"></div><div class="sk-circle9 sk-circle"></div><div class="sk-circle10 sk-circle"></div><div class="sk-circle11 sk-circle"></div><div class="sk-circle12 sk-circle"></div></div></div> </li>');
    startPosition += numrows;
    searchImages(startPosition);
}

function loadingFinished(){
    if(!noMoreResults){
        $("#resultsUl").append('<li id="moreResults" style="width:100%; padding-top: 20px;"><button class="search-submit" style="float:none" onclick="getMoreImages()">Mais Resultados</button></li>');
    }    
    $('#loadingDiv').hide();
    $("#resultsUl").show();
    if($('#loadingMoreImages').length){
        $('#loadingMoreImages').remove();
    }
}

totalPosition = 0; //global variable
currentOffset= 0;

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

/*Truncates large URL in the replay bar*/
function truncateUrlKeepProtocol(url, maxSize)
{    
    if (url.length > maxSize){
            url = url.substring(0, maxSize-3) + "...";
            return url;
    }
    else
        return url
}


lastPosition = -1; /*Global var refers to the lastImage the user*/
lastPress= -1; /*Global var refers to last time user pressed arrow in image viewer*/

function expandImage(position, animate){
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
    (position == 0) ? $('.left__arrow').hide() : $('.left__arrow').show();
    var arrowMarginLeft = $('#imageResults'+position).width()/2 - arrowWidth/2;
    $('#arrow'+position).css('margin-left', arrowMarginLeft+'px');
    $('#testViewer'+position).show();
    $('#arrowWrapper'+position).show();
    lastPosition = position;
    animate ? $('html, body').animate({scrollTop: $('#date'+position).offset().top - 150}, 400) : window.scrollTo( 0 , $('#date'+position).offset().top - 150);
    $('#testViewer'+position).focus();
 $('#testViewer'+position).bind('keydown', function(event) {
   var now = Date.now();
   var minimumTime= 100;

   console.log('last Press: ' + lastPress);
   console.log('now: ' + now);   
   console.log('position: ' + position);

   if(event.keyCode == 37 && position > 0 ) {
        if(lastPress < 0 || (now - lastPress) > minimumTime ){
		previousImage(''+position);
		lastPress = Date.now();
	}
    } 
   else if (event.keyCode == 39 && position >= 0){
	if(lastPress < 0 || (now - lastPress) > minimumTime ){
		nextImage(''+position);
		lastPress = Date.now();
	}
   }
 });


return false;
}

function previousImage(position){
    var previousImageLi = $('#imageResults'+position).prev();
    if( previousImageLi.attr('position') != undefined){
        expandImage(parseInt(position), false); /*Close current image*/
        expandImage(parseInt(previousImageLi.attr('position')), false);
    }
    return;
}
function nextImage(position){
    var nextImageLi = $('#imageResults'+position).next();
    if( nextImageLi.attr('position') != undefined){
        expandImage(parseInt(position), false); /*Close current image*/
        expandImage(parseInt(nextImageLi.attr('position')), false);            
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

var contentToInsert = ''+
'<li position='+position+' id="imageResults'+position+'" style="background:white; margin-right: 1%; margin-bottom:1%; margin-top:'+liMarginTop.toString()+'px;">'+
'   <h2>'+
'       <button style="cursor:pointer;" onclick = "expandImage('+position+',true)">'+
'           <img style="max-height:200px; padding:0px 0px 4px 0px;" height="'+imageHeight.toString()+'" src="'+imageObj.src+'"/>'+
'       </button>'+
'   </h2>'+
'   <p class="green" style="font-size:1.2em!important; text-align: left; padding-left:5px;padding-right:5px;">'+truncateUrl(imageObj.pageURL, 20)+'</p>'+
'   <p class="date" id="date'+position+'" style="font-weight:normal; font-size:1.4em!important; text-align: left; padding-left:5px;padding-right:5px;padding-bottom:2px;">'+getDateSpaceFormated(imageObj.timestamp)+'</p>'+
'   <div id="arrowWrapper'+position+'" class="arrowWrapper" >'+
'       <div id="arrow'+position+'" class="arrow"/></div>' +
'       <div id="testViewer'+position+'" class="imageExpandedDiv" tabindex="1">'+
'           <button onclick = "expandImage('+position+',false)" class="expand__close" title="'+close+'"></button>'+
'           <button onclick="previousImage('+position+')"  class="left__arrow" title="'+leftArrow+'"></button>'+
'           <button onclick="nextImage('+position+')" class="right__arrow" title="'+rightArrow+'"></button>'+
'           <div style="width: 60%; display: inline-block;float: left;">'+
'               <a target="_blank" href="//arquivo.pt/wayback/'+imageObj.pageTstamp+'/'+imageObj.pageURL+'">'+
'                   <img style="max-width:'+maxImageDivWidth+'px; margin-left: 70px; margin-top:'+ centerImage+'px; margin-bottom:'+ centerImage+'px"class="imageExpanded" id="ExpandedImg'+position+'" src="'+imageObj.currentImageURL+'">'+
'               </a>'+
'           </div>'+
'           <div style="min-height: 500px;margin-top: -50px;width: 39%;display: inline-block; border-left: solid 1px #454545;">'+
'               <div style="padding-top:120px; margin-left: 25px; margin-right:70px; text-align: left;">'+
'                   <h1 style="overflow: hidden;text-indent: initial;position: initial;word-wrap: break-word;color: #5e8400;font-size:2.1em;"> '+pageString+' </h1>'+
'                   <div style="padding-left:15px">'+
'                       <h2 style="color:white; word-wrap: break-word;"><a style="color:white" target="_blank" href="//arquivo.pt/wayback/'+imageObj.pageTstamp+'/'+imageObj.pageURL+'">'+imageObj.pageTitle+'</a></h2><br/>'+
'                       <h2 style="color:white; word-wrap: break-word;">'+truncateUrlKeepProtocol(imageObj.pageURL, 80)+'</h2>'+
'                       <br/>'+
'                       <h2 style="color:white; font-weight:bold;word-wrap: break-word;" > '+getDateSpaceFormated(imageObj.pageTstamp)+' </h2>'+
'                   </div>'+
'                   <div style="padding-top:20px; padding-bottom:50px;">'+
'                   <h1 style="overflow: hidden;text-indent: initial;position: initial;word-wrap: break-word;color: #5e8400;font-size:2.1em;"> '+imageString+' </h1>'+
                    '<div style="padding-left: 15px;">'+
( imageObj.title !== ""  ? ' <h2 style="color:white;word-wrap: break-word" id="imgTitleLabel'+position+'" ><a style="color:white" target="_blank" href="'+imageObj.currentImageURL+'">' +imageObj.title+'</a></h2><br/>':'') +
( imageObj.imgAlt !== "" &&  imageObj.title == ""  ? ' <h2 style="color:white;word-wrap: break-word" id="imgTitleLabel'+position+'" ><a style="color:white" target="_blank" href="'+imageObj.currentImageURL+'">' +imageObj.imgAlt+'</a></h2><br/>':'') +
'                           <h2 style="color:white;word-wrap: break-word" >'+imageObj.imgMimeType+' '+parseInt(expandedImageWidth)+' x '+parseInt(expandedImageHeight)+'</h2> <br/>'+
'                           <h2 style="color:white;word-wrap: break-word; font-weight:bold" > '+getDateSpaceFormated(imageObj.timestamp)+' </h2>'+
                    '</div>'+
'                </div>'+
'                   <div style="display: inline; white-space: nowrap; overflow: hidden;">'+
'                       <a class="imageViewerAnchor" target="_blank" href="/wayback/'+imageObj.pageTstamp+'/'+imageObj.pageURL+'">'+
'                           <span class="imageViewerButton">'+visitPage+'</span>'+
'                       </a>'+
'                       <a target="_blank" class="imageViewerAnchor" style="margin-left: 20px"  href="'+imageObj.currentImageURL+'">'+
'                           <span class="imageViewerButton">'+showImage+'</span>'+
'                       </a>'+
'                       <button  id="showDetails" class="imageViewerAnchor" position='+position+' style="margin-left: 20px">'+
'                           <span class="imageViewerButton" style="">'+showDetails+'</span>'+
'                       </button>'+
'                       <button id="dButton" position='+position+'  class="imageViewerAnchor" style="margin-left: 20px">'+
'                           <span class="imageViewerButton" style="line-height:25px;">'+share+'</span>'+
'                       </button>'+
'                   </div>'+
'               </div>'+
'           </div>'+
'   </div>'+ 
'</li>';     

  /*href="/shareImage.jsp?imgurl='+encodeURIComponent(imageObj.currentImageURL)+'&imgrefurl='+encodeURIComponent(imageObj.pageURL)+'&imgrefts='+imageObj.timestamp+'&imgres='+parseInt(expandedImageWidth)+'x'+parseInt(expandedImageHeight)+'&query='+$('#txtSearch').val()+'"*/
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
    var query;
    var input = $('#txtSearch').val();
    /*var tokens = input.split(' ');
    var size = '';
    var query ='';
    var finalQuery;
    for (var i= 0; i< tokens.length; i++){
      if(tokens[i].substring(0, 'size:'.length) === 'size:'){
        size += tokens[i].substring('size:'.length) + ' ';
      }
      var tokenNoAccents = removeDiacritics(tokens[i]);
      query+= "(imgSrc:*"+tokenNoAccents+"*OR imgSrc:*"+capitalizeFirstLetter(tokenNoAccents)+"* OR imgAlt:*"+capitalizeFirstLetter(tokenNoAccents)+"* imgAlt:*"+tokenNoAccents+"* OR imgSrc:*"+tokenNoAccents.toLowerCase()+"* OR imgAlt:*"+tokenNoAccents.toLowerCase()+"*) AND "; 
    }
    query = query.substring(0, query.length -5); 
    */
    var dateStart= $('#dateStart_top').attr("value");
    dateStart = dateStart.substring(6, 10)+dateStart.substring(3, 5) + dateStart.substring(0, 2)+'000000' ;
    var dateEnd= $('#dateEnd_top').attr("value");
    dateEnd = dateEnd.substring(6, 10)+dateEnd.substring(3, 5) + dateEnd.substring(0, 2)+'000000' ;

    //query += "AND (timestamp:["+dateStart+" TO "+dateEnd+"])";
    //console.log(query);

    /*if( size === ''){
      size = 'all';
    }*/

   /* var dateStartTokenes = dateStartWithSlashes.split("/");
    var dateStartTs = dateStartTokenes[2]+ dateStartTokenes[1] + dateStartTokenes[0]+ "000000";

    var dateEndTokenes = dateEndWithSlashes.split("/");
    var dateEndTs = dateEndTokenes[2]+ dateEndTokenes[1] + dateEndTokenes[0]+ "000000"     ;

    var dateFinal = dateStartTs+"-"+dateEndTs; */
    var showAll = false;
    numrows =50;
    currentStart = startIndex;
    
    safeSearch = true;
    if($('#safeSearch').find(":selected").attr("value") === 'off'){
        safeSearch = false;       
    }


    $.ajax({
    // example request to the cdx-server api - 'http://arquivo.pt/pywb/replay-cdx?url=http://www.sapo.pt/index.html&output=json&fl=url,timestamp'
       url: "/imagesearch",      

 /*+ " AND pageTstamp:["+dateStart+" TO "+dateEnd+"]"*/
       data: {
          q: input,
          defType: 'edismax',                  
          qf: 'imgTitle^4 imgAlt^3 imgSrcTokens^2 pageTitle pageURLTokens', //TODO: improve ranking
          pf: 'imgTitle^4000 imgAlt^3000 imgSrcTokens^2000 pageTitle^1000 pageURLTokens^1000', //TODO: improve ranking  
          ps: 1,
          pf2: 'imgTitle^400 imgAlt^300 imgSrcTokens^200 pageTitle^100 pageURLTokens^100', //TODO: improve ranking
          ps2: 2,
          pf3: 'imgTitle^40 imgAlt^30 imgSrcTokens^20 pageTitle^10 pageURLTokens^10', //TODO: improve ranking
          ps3: 3,
          from: dateStart,
          to: dateEnd,
          offset: startIndex,
          maxItems: numrows,
          safeSearch:safeSearch        
       },
           
       timeout: 300000,
       error: function() {
         console.log("Error In Ajax request to getimages");            
       },
       dataType: 'text',
       success: function(data) {

        var responseJson = $.parseJSON(data);

        if(currentStart == 0 ){
            $('#resultsUl').empty();
        }
        var totalResults = responseJson.response.numFound;
        
        if ( totalResults === 0){
            createErrorPage();
            console.log("no results found");
            /*TODO:: help suggestions function*/
            noMoreResults=true;
            loadingFinished();
        }
        else{
            console.log("Found "+totalResults+ " results");
            var currentResults
            if(totalResults > numrows){
            	currentResults = responseJson.response.numShowing;
            }else{
            	currentResults = totalResults;
            	noMoreResults=true;
            }
            var resultsToLoad = currentResults;
            console.log("Showing "+ currentResults + " results");

            for (var i=0; i< currentResults; i++){
                console.log("Result "+i);
                var currentDocument = responseJson.response.docs[i];
                if (typeof currentDocument === 'undefined' || !currentDocument){
                    console.log("undefined document");
                    continue;
                }
                if (typeof currentDocument.imgTstamp === 'undefined' || !currentDocument.imgTstamp){
                    console.log("No imgtstamp found for image");
                    continue;
                }

                var currentImageURL = '//arquivo.pt/wayback/'+ currentDocument.imgTstamp +'/'+currentDocument.imgSrc;
                var imageDigest = currentDocument.imgDigest;
                

                if(imageDigests.indexOf(imageDigest) > -1){
                    console.log('Duplicated: ' + imageDigest);
                    console.log('imgDigest: ' +currentDocument.imgDigest);
                    console.log('pageURL: ' + currentDocument.pageURL);
                    console.log('pageTstamp: ' + currentDocument.pageTstamp);
                    
                    resultsToLoad--;
                    console.log("current results: "+ resultsToLoad);
                    continue; 
                }
                
                else{
                    console.log('Digest: ' + imageDigest);
                    imageDigests.push(imageDigest);
                }

                var pageURL = currentDocument.pageURL;
                var thumbnail = currentImageURL;

                console.log("Creating Image");
                imageObj = new Image();
                imageObj.timestamp = currentDocument.imgTstamp.toString();
                console.log("Image timestamp: " + imageObj.timestamp );
                imageObj.pageURL = pageURL.toString();
                imageObj.currentImageURL = currentImageURL.toString();
                console.log("Image URL: " + imageObj.currentImageURL );
                imageObj.position = totalPosition;
                console.log("Position: " + totalPosition );
                imageObj.expandedHeight = currentDocument.imgHeight;
                imageObj.expandedWidth = currentDocument.imgWidth;
                imageObj.imgMimeType= currentDocument.imgMimeType.substring(6,currentDocument.imgMimeType.length);
                imageObj.imgAlt = currentDocument.imgAlt;
                imageObj.imgAltFull = currentDocument.imgAlt;
                if (typeof imageObj.imgAlt === 'undefined' || imageObj.imgAlt =='undefined' ){imageObj.imgAlt ='';}
                if (typeof imageObj.imgAltFull === 'undefined' || imageObj.imgAltFull =='undefined' ){imageObj.imgAltFull ='';}
                if(imageObj.imgAlt.length > 40) {imageObj.imgAlt = imageObj.imgAlt.substring(0,37) + "...";}
                imageObj.title = currentDocument.imgTitle;
                if (typeof imageObj.title === 'undefined' || imageObj.title == 'undefined' ){imageObj.title ='';}
                imageObj.titleFull = currentDocument.imgTitle;
                if (typeof imageObj.titleFull === 'undefined' || imageObj.titleFull == 'undefined' ){imageObj.titleFull ='';}
                
                if(imageObj.title.length > 40) {imageObj.title = imageObj.title.substring(0,37) + "...";}

                imageObj.safe = currentDocument.safe;
                imageObj.pageTstamp = currentDocument.pageTstamp.toString();
                imageObj.pageTitle = currentDocument.pageTitle;
                imageObj.pageTitleFull = currentDocument.pageTitle;
                if (typeof imageObj.pageTitle === 'undefined' || imageObj.pageTitle == 'undefined' ){imageObj.pageTitle ='';}
                if(imageObj.pageTitle.length > 40) {imageObj.pageTitle = imageObj.pageTitle.substring(0,37) + "...";}
                if (typeof imageObj.pageTitleFull === 'undefined' || imageObj.pageTitleFull == 'undefined' ){imageObj.pageTitleFull ='';}               
                imageObj.collection = currentDocument.collection;
                imageObj.imgSrc = currentDocument.imgSrc;
                //if (imageObj.title == "undefined") {imageObj.title = imageUndefined; $('#imgTitleLabel'+imageObj.position).hide();}


                totalPosition = totalPosition + 1;

                imageObj.src = "data:"+currentDocument.imgMimeType+";base64," + currentDocument.imgSrcBase64;

                var resizeImageHeight = 200;

                imageObj.onload = function() {
                            
                    if( startIndex != 0 &&  totalResults == responseJson.totalResults){
                        $('#loadingMoreImages').remove();
                    }
                            totalResults --;
                            resultsToLoad --;

                            console.log("current results: "+ resultsToLoad);

                               
                            if(this.height <= resizeImageHeight){
                            insertInPosition(this.position +currentStart, this, this.height, resizeImageHeight, this.expandedHeight, this.expandedWidth);
                            }
                            else{
                            insertInPosition(this.position +currentStart, this, resizeImageHeight, resizeImageHeight, this.expandedHeight, this.expandedWidth);
                            }
                       
                            if(resultsToLoad <= 0){
                                loadingFinished();
                            }
                    console.log("Created Image");                            
                }
                
               
                imageObj.onerror = function() {
                    // image did not load
                    if( startIndex != 0 &&  totalResults == responseJson.totalResults){
                        $('#loadingMoreImages').remove();
                    }                
                    totalResults --;
                    resultsToLoad --;
                    console.log("Error loading: " + this.currentImageURL);
                    console.log("Results: " + totalResults);
                    if(resultsToLoad <= 0){
                        loadingFinished();
                    }
                }
                
            }
        }

        console.log('Query time: ' + responseJson.responseHeader.QTime + 'ms');
        console.log('Number of Results: ' + responseJson.response.numFound );
       },
       type: 'GET'
    });

}

$(document).ready(function() {     
    $(document).on('click', '#dButton', function() {
      var position =  $(this).attr('position');
      var imageObj = imageObjs[position]; /*get Current Image Object*/
      var shareURL = '//' + window.location.hostname + '/shareImage.jsp?l='+language+'&imgurl='+encodeURIComponent(imageObj.currentImageURL)+'&imgrefurl='+encodeURIComponent(imageObj.pageURL)+'&imgrefts='+imageObj.timestamp+'&imgres='+parseInt(imageObj.expandedImageWidth)+'x'+parseInt(imageObj.expandedImageHeight)+'&query='+$('#txtSearch').val();
      shortenURL( shareURL);
      
      $('#h2Copy').html(clickToCopy);
      $("#dialog").dialog('open');

      /*If click anywhere outside modal lets close it*/
     $(document).on('click', function(e) {
            if (e.target.id !== 'dialog'  && !$(e.target).parents("#dialog").length) {
                $("#dialog").dialog('close');
                $(this).off(e);
            }
            return false;            
        });      
      return false;
    });
    $(document).on('click', '#dialogClose', function() {
      $("#dialog").dialog('close'); 
      return false;
    }); 
    
    $(document).on('click', '#showDetails', function() {
      var position =  $(this).attr('position');
      var imageObj = imageObjs[position]; /*get Current Image Object*/
      $("#detailsDialog").dialog('open');
      $('#imageDetailImageElements').empty();
      $('#imageDetailPageElements').empty();
      $('#imageDetailCollectionElements').empty();
      /*Insert Page Details*/
      $('#imageDetailPageElements').append('<h3 style="margin-left: 10px; margin-right: 10px;word-wrap: break-word;"> <span style="font-weight: bold;"> url: </span>'+imageObj.pageURL+'</h3>');
      $('#imageDetailPageElements').append('<h3 style="margin-left: 10px; margin-right: 10px;word-wrap: break-word;"> <span style="font-weight: bold;"> timestamp: </span>'+imageObj.pageTstamp+'</h3>');
      $('#imageDetailPageElements').append('<h3 style="margin-left: 10px; margin-right: 10px;word-wrap: break-word;"> <span style="font-weight: bold;"> '+titleString+': </span>'+imageObj.pageTitleFull+'</h3>');
      /*Insert Image Details*/
      $('#imageDetailImageElements').append('<h3 style="margin-left: 10px; margin-right: 10px;word-wrap: break-word;"> <span style="font-weight: bold;"> src: </span>'+imageObj.imgSrc+'</h3>');
      $('#imageDetailImageElements').append('<h3 style="margin-left: 10px; margin-right: 10px;word-wrap: break-word;"> <span style="font-weight: bold;"> timestamp: </span>'+imageObj.timestamp+'</h3>');
      if(imageObj.titleFull !== ''){
        $('#imageDetailImageElements').append('<h3 style="margin-left: 10px; margin-right: 10px;word-wrap: break-word;"> <span style="font-weight: bold;"> '+titleString+': </span>'+imageObj.titleFull+'</h3>');
      }
      if(imageObj.imgAltFull !== ''){
        $('#imageDetailImageElements').append('<h3 style="margin-left: 10px; margin-right: 10px;word-wrap: break-word;"> <span style="font-weight: bold;"> alt: </span>'+imageObj.imgAltFull+'</h3>');
      }      
      $('#imageDetailImageElements').append('<h3 style="margin-left: 10px; margin-right: 10px;word-wrap: break-word;"> <span style="font-weight: bold;"> '+resolutionString+': </span>'+parseInt(imageObj.expandedWidth)+' x '+parseInt(imageObj.expandedHeight)+' pixels</h3>');
      $('#imageDetailImageElements').append('<h3 style="margin-left: 10px; margin-right: 10px;word-wrap: break-word;"> <span style="font-weight: bold;"> mimetype: </span>image/'+imageObj.imgMimeType+'</h3>');
      $('#imageDetailImageElements').append('<h3 style="margin-left: 10px; margin-right: 10px;word-wrap: break-word;"> <span style="font-weight: bold;"> safesearch score: </span>'+imageObj.safe+'</h3>');
      $('#imageDetailCollectionElements').append('<h3 style="margin-left: 10px; margin-right: 10px;word-wrap: break-word;"> <span style="font-weight: bold;"> '+nameString+': </span>'+imageObj.collection+'</h3>');
      
      $(document).on('click', function(e) {
        if (e.target.id !== 'detailsDialog'  && !$(e.target).parents("#detailsDialog").length) {
            $("#detailsDialog").dialog('close');
            $(this).off(e);
        }
        return false;
      });          
      return false;
    });
    $(document).on('click', '#detailsDialogClose', function() {
      $("#detailsDialog").dialog('close'); 
      return false;
    });    

});

var defaultDiacriticsRemovalMap = [
    {'base':'A', 'letters':'\u0041\u24B6\uFF21\u00C0\u00C1\u00C2\u1EA6\u1EA4\u1EAA\u1EA8\u00C3\u0100\u0102\u1EB0\u1EAE\u1EB4\u1EB2\u0226\u01E0\u00C4\u01DE\u1EA2\u00C5\u01FA\u01CD\u0200\u0202\u1EA0\u1EAC\u1EB6\u1E00\u0104\u023A\u2C6F'},
    {'base':'AA','letters':'\uA732'},
    {'base':'AE','letters':'\u00C6\u01FC\u01E2'},
    {'base':'AO','letters':'\uA734'},
    {'base':'AU','letters':'\uA736'},
    {'base':'AV','letters':'\uA738\uA73A'},
    {'base':'AY','letters':'\uA73C'},
    {'base':'B', 'letters':'\u0042\u24B7\uFF22\u1E02\u1E04\u1E06\u0243\u0182\u0181'},
    {'base':'C', 'letters':'\u0043\u24B8\uFF23\u0106\u0108\u010A\u010C\u00C7\u1E08\u0187\u023B\uA73E'},
    {'base':'D', 'letters':'\u0044\u24B9\uFF24\u1E0A\u010E\u1E0C\u1E10\u1E12\u1E0E\u0110\u018B\u018A\u0189\uA779\u00D0'},
    {'base':'DZ','letters':'\u01F1\u01C4'},
    {'base':'Dz','letters':'\u01F2\u01C5'},
    {'base':'E', 'letters':'\u0045\u24BA\uFF25\u00C8\u00C9\u00CA\u1EC0\u1EBE\u1EC4\u1EC2\u1EBC\u0112\u1E14\u1E16\u0114\u0116\u00CB\u1EBA\u011A\u0204\u0206\u1EB8\u1EC6\u0228\u1E1C\u0118\u1E18\u1E1A\u0190\u018E'},
    {'base':'F', 'letters':'\u0046\u24BB\uFF26\u1E1E\u0191\uA77B'},
    {'base':'G', 'letters':'\u0047\u24BC\uFF27\u01F4\u011C\u1E20\u011E\u0120\u01E6\u0122\u01E4\u0193\uA7A0\uA77D\uA77E'},
    {'base':'H', 'letters':'\u0048\u24BD\uFF28\u0124\u1E22\u1E26\u021E\u1E24\u1E28\u1E2A\u0126\u2C67\u2C75\uA78D'},
    {'base':'I', 'letters':'\u0049\u24BE\uFF29\u00CC\u00CD\u00CE\u0128\u012A\u012C\u0130\u00CF\u1E2E\u1EC8\u01CF\u0208\u020A\u1ECA\u012E\u1E2C\u0197'},
    {'base':'J', 'letters':'\u004A\u24BF\uFF2A\u0134\u0248'},
    {'base':'K', 'letters':'\u004B\u24C0\uFF2B\u1E30\u01E8\u1E32\u0136\u1E34\u0198\u2C69\uA740\uA742\uA744\uA7A2'},
    {'base':'L', 'letters':'\u004C\u24C1\uFF2C\u013F\u0139\u013D\u1E36\u1E38\u013B\u1E3C\u1E3A\u0141\u023D\u2C62\u2C60\uA748\uA746\uA780'},
    {'base':'LJ','letters':'\u01C7'},
    {'base':'Lj','letters':'\u01C8'},
    {'base':'M', 'letters':'\u004D\u24C2\uFF2D\u1E3E\u1E40\u1E42\u2C6E\u019C'},
    {'base':'N', 'letters':'\u004E\u24C3\uFF2E\u01F8\u0143\u00D1\u1E44\u0147\u1E46\u0145\u1E4A\u1E48\u0220\u019D\uA790\uA7A4'},
    {'base':'NJ','letters':'\u01CA'},
    {'base':'Nj','letters':'\u01CB'},
    {'base':'O', 'letters':'\u004F\u24C4\uFF2F\u00D2\u00D3\u00D4\u1ED2\u1ED0\u1ED6\u1ED4\u00D5\u1E4C\u022C\u1E4E\u014C\u1E50\u1E52\u014E\u022E\u0230\u00D6\u022A\u1ECE\u0150\u01D1\u020C\u020E\u01A0\u1EDC\u1EDA\u1EE0\u1EDE\u1EE2\u1ECC\u1ED8\u01EA\u01EC\u00D8\u01FE\u0186\u019F\uA74A\uA74C'},
    {'base':'OI','letters':'\u01A2'},
    {'base':'OO','letters':'\uA74E'},
    {'base':'OU','letters':'\u0222'},
    {'base':'OE','letters':'\u008C\u0152'},
    {'base':'oe','letters':'\u009C\u0153'},
    {'base':'P', 'letters':'\u0050\u24C5\uFF30\u1E54\u1E56\u01A4\u2C63\uA750\uA752\uA754'},
    {'base':'Q', 'letters':'\u0051\u24C6\uFF31\uA756\uA758\u024A'},
    {'base':'R', 'letters':'\u0052\u24C7\uFF32\u0154\u1E58\u0158\u0210\u0212\u1E5A\u1E5C\u0156\u1E5E\u024C\u2C64\uA75A\uA7A6\uA782'},
    {'base':'S', 'letters':'\u0053\u24C8\uFF33\u1E9E\u015A\u1E64\u015C\u1E60\u0160\u1E66\u1E62\u1E68\u0218\u015E\u2C7E\uA7A8\uA784'},
    {'base':'T', 'letters':'\u0054\u24C9\uFF34\u1E6A\u0164\u1E6C\u021A\u0162\u1E70\u1E6E\u0166\u01AC\u01AE\u023E\uA786'},
    {'base':'TZ','letters':'\uA728'},
    {'base':'U', 'letters':'\u0055\u24CA\uFF35\u00D9\u00DA\u00DB\u0168\u1E78\u016A\u1E7A\u016C\u00DC\u01DB\u01D7\u01D5\u01D9\u1EE6\u016E\u0170\u01D3\u0214\u0216\u01AF\u1EEA\u1EE8\u1EEE\u1EEC\u1EF0\u1EE4\u1E72\u0172\u1E76\u1E74\u0244'},
    {'base':'V', 'letters':'\u0056\u24CB\uFF36\u1E7C\u1E7E\u01B2\uA75E\u0245'},
    {'base':'VY','letters':'\uA760'},
    {'base':'W', 'letters':'\u0057\u24CC\uFF37\u1E80\u1E82\u0174\u1E86\u1E84\u1E88\u2C72'},
    {'base':'X', 'letters':'\u0058\u24CD\uFF38\u1E8A\u1E8C'},
    {'base':'Y', 'letters':'\u0059\u24CE\uFF39\u1EF2\u00DD\u0176\u1EF8\u0232\u1E8E\u0178\u1EF6\u1EF4\u01B3\u024E\u1EFE'},
    {'base':'Z', 'letters':'\u005A\u24CF\uFF3A\u0179\u1E90\u017B\u017D\u1E92\u1E94\u01B5\u0224\u2C7F\u2C6B\uA762'},
    {'base':'a', 'letters':'\u0061\u24D0\uFF41\u1E9A\u00E0\u00E1\u00E2\u1EA7\u1EA5\u1EAB\u1EA9\u00E3\u0101\u0103\u1EB1\u1EAF\u1EB5\u1EB3\u0227\u01E1\u00E4\u01DF\u1EA3\u00E5\u01FB\u01CE\u0201\u0203\u1EA1\u1EAD\u1EB7\u1E01\u0105\u2C65\u0250'},
    {'base':'aa','letters':'\uA733'},
    {'base':'ae','letters':'\u00E6\u01FD\u01E3'},
    {'base':'ao','letters':'\uA735'},
    {'base':'au','letters':'\uA737'},
    {'base':'av','letters':'\uA739\uA73B'},
    {'base':'ay','letters':'\uA73D'},
    {'base':'b', 'letters':'\u0062\u24D1\uFF42\u1E03\u1E05\u1E07\u0180\u0183\u0253'},
    {'base':'c', 'letters':'\u0063\u24D2\uFF43\u0107\u0109\u010B\u010D\u00E7\u1E09\u0188\u023C\uA73F\u2184'},
    {'base':'d', 'letters':'\u0064\u24D3\uFF44\u1E0B\u010F\u1E0D\u1E11\u1E13\u1E0F\u0111\u018C\u0256\u0257\uA77A'},
    {'base':'dz','letters':'\u01F3\u01C6'},
    {'base':'e', 'letters':'\u0065\u24D4\uFF45\u00E8\u00E9\u00EA\u1EC1\u1EBF\u1EC5\u1EC3\u1EBD\u0113\u1E15\u1E17\u0115\u0117\u00EB\u1EBB\u011B\u0205\u0207\u1EB9\u1EC7\u0229\u1E1D\u0119\u1E19\u1E1B\u0247\u025B\u01DD'},
    {'base':'f', 'letters':'\u0066\u24D5\uFF46\u1E1F\u0192\uA77C'},
    {'base':'g', 'letters':'\u0067\u24D6\uFF47\u01F5\u011D\u1E21\u011F\u0121\u01E7\u0123\u01E5\u0260\uA7A1\u1D79\uA77F'},
    {'base':'h', 'letters':'\u0068\u24D7\uFF48\u0125\u1E23\u1E27\u021F\u1E25\u1E29\u1E2B\u1E96\u0127\u2C68\u2C76\u0265'},
    {'base':'hv','letters':'\u0195'},
    {'base':'i', 'letters':'\u0069\u24D8\uFF49\u00EC\u00ED\u00EE\u0129\u012B\u012D\u00EF\u1E2F\u1EC9\u01D0\u0209\u020B\u1ECB\u012F\u1E2D\u0268\u0131'},
    {'base':'j', 'letters':'\u006A\u24D9\uFF4A\u0135\u01F0\u0249'},
    {'base':'k', 'letters':'\u006B\u24DA\uFF4B\u1E31\u01E9\u1E33\u0137\u1E35\u0199\u2C6A\uA741\uA743\uA745\uA7A3'},
    {'base':'l', 'letters':'\u006C\u24DB\uFF4C\u0140\u013A\u013E\u1E37\u1E39\u013C\u1E3D\u1E3B\u017F\u0142\u019A\u026B\u2C61\uA749\uA781\uA747'},
    {'base':'lj','letters':'\u01C9'},
    {'base':'m', 'letters':'\u006D\u24DC\uFF4D\u1E3F\u1E41\u1E43\u0271\u026F'},
    {'base':'n', 'letters':'\u006E\u24DD\uFF4E\u01F9\u0144\u00F1\u1E45\u0148\u1E47\u0146\u1E4B\u1E49\u019E\u0272\u0149\uA791\uA7A5'},
    {'base':'nj','letters':'\u01CC'},
    {'base':'o', 'letters':'\u006F\u24DE\uFF4F\u00F2\u00F3\u00F4\u1ED3\u1ED1\u1ED7\u1ED5\u00F5\u1E4D\u022D\u1E4F\u014D\u1E51\u1E53\u014F\u022F\u0231\u00F6\u022B\u1ECF\u0151\u01D2\u020D\u020F\u01A1\u1EDD\u1EDB\u1EE1\u1EDF\u1EE3\u1ECD\u1ED9\u01EB\u01ED\u00F8\u01FF\u0254\uA74B\uA74D\u0275'},
    {'base':'oi','letters':'\u01A3'},
    {'base':'ou','letters':'\u0223'},
    {'base':'oo','letters':'\uA74F'},
    {'base':'p','letters':'\u0070\u24DF\uFF50\u1E55\u1E57\u01A5\u1D7D\uA751\uA753\uA755'},
    {'base':'q','letters':'\u0071\u24E0\uFF51\u024B\uA757\uA759'},
    {'base':'r','letters':'\u0072\u24E1\uFF52\u0155\u1E59\u0159\u0211\u0213\u1E5B\u1E5D\u0157\u1E5F\u024D\u027D\uA75B\uA7A7\uA783'},
    {'base':'s','letters':'\u0073\u24E2\uFF53\u00DF\u015B\u1E65\u015D\u1E61\u0161\u1E67\u1E63\u1E69\u0219\u015F\u023F\uA7A9\uA785\u1E9B'},
    {'base':'t','letters':'\u0074\u24E3\uFF54\u1E6B\u1E97\u0165\u1E6D\u021B\u0163\u1E71\u1E6F\u0167\u01AD\u0288\u2C66\uA787'},
    {'base':'tz','letters':'\uA729'},
    {'base':'u','letters': '\u0075\u24E4\uFF55\u00F9\u00FA\u00FB\u0169\u1E79\u016B\u1E7B\u016D\u00FC\u01DC\u01D8\u01D6\u01DA\u1EE7\u016F\u0171\u01D4\u0215\u0217\u01B0\u1EEB\u1EE9\u1EEF\u1EED\u1EF1\u1EE5\u1E73\u0173\u1E77\u1E75\u0289'},
    {'base':'v','letters':'\u0076\u24E5\uFF56\u1E7D\u1E7F\u028B\uA75F\u028C'},
    {'base':'vy','letters':'\uA761'},
    {'base':'w','letters':'\u0077\u24E6\uFF57\u1E81\u1E83\u0175\u1E87\u1E85\u1E98\u1E89\u2C73'},
    {'base':'x','letters':'\u0078\u24E7\uFF58\u1E8B\u1E8D'},
    {'base':'y','letters':'\u0079\u24E8\uFF59\u1EF3\u00FD\u0177\u1EF9\u0233\u1E8F\u00FF\u1EF7\u1E99\u1EF5\u01B4\u024F\u1EFF'},
    {'base':'z','letters':'\u007A\u24E9\uFF5A\u017A\u1E91\u017C\u017E\u1E93\u1E95\u01B6\u0225\u0240\u2C6C\uA763'}
];

var diacriticsMap = {};
for (var i=0; i < defaultDiacriticsRemovalMap .length; i++){
    var letters = defaultDiacriticsRemovalMap [i].letters;
    for (var j=0; j < letters.length ; j++){
        diacriticsMap[letters[j]] = defaultDiacriticsRemovalMap [i].base;
    }
}

// "what?" version ... http://jsperf.com/diacritics/12
function removeDiacritics (str) {
    return str.replace(/[^\u0000-\u007E]/g, function(a){ 
       return diacriticsMap[a] || a; 
    });
}    

function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

function createErrorPage(){
  $(''+
        '<div id="conteudo-pesquisa-erro">'+
            '<h2>'+ notFoundTitle+'</h2> <h3>'+$('#txtSearch').val()+'</h3>'+
            '<div id="sugerimos-que">'+
                '<p>'+noResultsSuggestions+'</p>'+
              '<ul>'+
                '<li>'+noResultsWellWritten+'</li>'+
                '<li>'+noResultsInterval+'</li>'+                    
                '<li>'+noResultsKeywords+'</li>'+                    
                '<li>'+noResultsGenericWords+'</li>'+                    
              '</ul>'+
            '</div>'+
        '</div>'+          
    '').insertAfter("#resultados-lista");
    $('#conteudo-pesquisa-erro').css('margin-left', $('#search-dateStart_top').offset().left);
    $( window ).resize(function() {$('#conteudo-pesquisa-erro').css('margin-left', $('#search-dateStart_top').offset().left)}); /*dirty hack to keep message aligned with not responsive searchbox*/$( window ).resize(function() {$('.spell').css('margin-left', $('#search-dateStart_top').offset().left)}); /*dirty hack to keep message aligned with not responsive searchbox*/ 
}




