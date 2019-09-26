$( document ).ready(function() {
    console.log('ready')
	$('#expandedImageViewers').append(viewDetails(imageObj));
	$('.image-mobile-expanded-div').show();
});

function viewDetails(imageObj){
    var detailsCard = ''+
    '<ion-card id="detailsCard'+1+'" class="card-height">'+
      '<a href="'+imageObj.backURL+'"><ion-icon id="closeCard'+1+'" name="close" class="closeItAbsolute" size="large"></ion-icon></a>'+
      '<ion-row>'+
        '<h3 class="text-left">'+details.details+'</h4>'+                
      '</ion-row>'+            
      '<ion-row>'+
        '<h4 class="text-left">'+details.page+'</h4>'+                
      '</ion-row>'+      
      '<ion-card-content>'+
        '<ion-list>'+
         '<ion-item class="item-borderless" lines="none" ><h5><em>url:</em>&nbsp;<a href="/wayback/'+imageObj.pageTstamp+'/'+imageObj.pageURL+'">'+imageObj.pageURL+'</a></h5></ion-item>'+
          '<ion-item lines="none" ><h5><em>timestamp:</em> '+imageObj.pageTstamp+'</h5></ion-item>'+
          '<ion-item lines="none" ><h5><em>'+details.title+'</em> '+imageObj.pageTitleFull+'</h5></ion-item>'+
        '</ion-list>'+
      '</ion-card-content>'+
      '<ion-row>'+      
        '<h4 class="text-left">'+details.image+'</h4>'+                
      '</ion-row>'+      
      '<ion-card-content>'+
        '<ion-list>'+
          '<ion-item class="item-borderless" lines="none" ><h5><em>src:</em>&nbsp;<a href="/wayback/'+imageObj.timestamp+'/'+imageObj.imgSrc+'">'+imageObj.imgSrc+'</a></h5></ion-item>'+
          '<ion-item lines="none" ><h5><em>timestamp:</em> '+imageObj.timestamp+'</h5></ion-item>'+
          (imageObj.titleFull != "" ? '<ion-item lines="none" ><h5><em>'+details.title+'</em> '+imageObj.titleFull+'</h5></ion-item>': '') +
          (imageObj.imgAltFull != "" ? '<ion-item lines="none" ><h5><em>alt:</em> '+imageObj.imgAltFull+'</h5></ion-item>': '') +
          '<ion-item lines="none" ><h5><em>'+details.resolution+'</em> '+parseInt(imageObj.expandedWidth)+' x '+parseInt(imageObj.expandedHeight)+' pixels</h5></ion-item>'+
          '<ion-item lines="none" ><h5><em>mimetype:</em> '+imageObj.imgMimeType+'</h5></ion-item>'+
          '<ion-item lines="none" ><h5><em>'+details.safesearch+'</em> '+imageObj.safe+'</h5></ion-item>'+
        '</ion-list>'+
      '</ion-card-content>'+      
      '<ion-row>'+      
        '<h4 class="text-left">'+details.collection+'</h4>'+                
      '</ion-row>'+      
      '<ion-card-content>'+
        '<ion-list>'+
          '<ion-item class="item-borderless" lines="none" ><h5><em>'+details.name+'</em> '+imageObj.collection+'</h5></ion-item>'+
        '</ion-list>'+
      '</ion-card-content>'+      
    '</ion-card>';

    $('#insert-card-'+1).append(detailsCard);
}

function truncateUrlMiddleRemoveProtocol(url, maxSize)
{    
    url = url.replace(/(^\w+:|^)\/\//, ''); //remove all possible protocols
    if (url.length > maxSize){
            url = url.substring(0, parseInt(maxSize*0.75) -3) + "..." + url.substring(url.length - (parseInt(maxSize*0.25)) , url.length) ;
            return url;
    }
    else
        return url
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
function truncateUrlRemoveProtocol(url, maxSize)
{    
    url = url.replace(/(^\w+:|^)\/\//, ''); //remove all possible protocols
    if (url.length > maxSize){
            url = url.substring(0, maxSize-3) + "...";
            return url;
    }
    else
        return url
}