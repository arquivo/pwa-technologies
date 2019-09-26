$( document ).ready(function() {
	$('#expandedImageViewers').append(insertImageViewer(imageObj));
	$('.image-mobile-expanded-div').show();
});

function  insertImageViewer(imageObj){
return ''+
//image-expanded-full-width
/*If landscaped image show it full width on small screens*/
'<div id="testViewer'+1+'" class="height-vh image-mobile-expanded-div no-outline" tabindex="1">'+
    '<div onclick="expandImage('+1+',false)" class="image-mobile-expanded-viewer-mask no-outline"></div>'+
    '<div class="row full-height no-outline">'+
        '<div id="insert-card-'+1+'" class="full-height col-sm-8 col-sm-offset-2 col-md-4 col-md-offset-4 text-right">'+
            '<ion-card id="card'+1+'" class="card-height">'+
               (parseInt(imageObj.expandedWidth) > parseInt(imageObj.expandedHeight) ? ''+
               '<a href="'+imageObj.backURL+'"><ion-icon id="close'+1+'" name="close" class="closeCard" size="large"></ion-icon></a>' : '' +
               '<a href="'+imageObj.backURL+'"><ion-icon id="close'+1+'" name="close" class="closeIt" size="large" ></ion-icon></a>') +
               '<a href="'+window.location.protocol+'//'+window.location.hostname+'/wayback/'+imageObj.pageTstamp+'/'+imageObj.pageURL+'">'+
                (parseInt(imageObj.expandedWidth) > parseInt(imageObj.expandedHeight) ? '<img class="image-expanded-viewer image-expanded-full-width" src="/wayback/'+imageObj.timestamp+'/'+imageObj.currentImageURL+'">' : '<img class="image-expanded-viewer" src="/wayback/'+imageObj.timestamp+'/'+imageObj.currentImageURL+'">')+
               '</a>'+
               '<ion-row class="image-viewer-expanded-main-actions">'+
                    '<ion-col size="6" class="text-left"><a href="'+window.location.protocol+'//'+window.location.hostname+'/wayback/'+imageObj.pageTstamp+'/'+imageObj.pageURL+'"><ion-button size="small" class="visit-page border-mobile" fill="clear"><ion-icon name="globe" class="middle"></ion-icon><span class="middle"><h5>&nbsp;'+details.visit+'</h5></span></ion-button></a></ion-col>'+                    
                '</ion-row>'+
                '<ion-row>'+
                    '<h4 class="text-left">'+details.image+'</h4>'+                
                '</ion-row>'+
                '<ion-card-content>'+                
                    '<ion-list class="imageList selected">'+
    ( imageObj.title !== ""  ? ' <ion-item class="item-borderless" lines="none" ><a target="_blank" href="/wayback/'+imageObj.timestamp+'/'+imageObj.currentImageURL+'"><h5>' +imageObj.title+'</a></h5></ion-item>':'') +
    ( imageObj.imgAlt !== "" &&  imageObj.title == ""  ? ' <ion-item id="imgTitleLabel'+1+'" lines="none"><h5><a target="_blank" href="'+imageObj.currentImageURL+'">' +imageObj.imgAlt+'</a></h5></ion-item>':'') +  
                        '<ion-item lines="none"><h5>' +truncateUrlMiddleRemoveProtocol(imageObj.currentImageURL, 40)+'</h5></ion-item>'+
                        '<ion-item lines="none"><h5>'+imageObj.imgMimeType+' '+parseInt(imageObj.expandedWidth)+' x '+parseInt(imageObj.expandedHeight)+'</h5></ion-item>'+
                        '<ion-item lines="none"><h5>'+getDateSpaceFormated(imageObj.timestamp)+'</h5></ion-item>'+             
                    '</ion-list>'+
                '</ion-card-content>'+  
                '<ion-row>'+
                    '<h4 class="text-left">'+details.page+'</h4>'+                
                '</ion-row>'+
                '<ion-card-content>'+                
                    '<ion-list>'+
    '                       <ion-item class="item-borderless" lines="none" ><a target="_blank" href="'+window.location.protocol+'//'+window.location.hostname+'/wayback/'+imageObj.pageTstamp+'/'+imageObj.pageURL+'"><h5>'+imageObj.pageTitle+'</h5></a></ion-item>'+
    '                       <ion-item lines="none" "><h5>'+truncateUrlRemoveProtocol(imageObj.pageURL, 60)+'</h5></ion-item>'+
    '                       <ion-item lines="none" "><h5>'+getDateSpaceFormated(imageObj.pageTstamp)+'</h5></ion-item>'+          
                    '</ion-list>'+
                '</ion-card-content>'+                                
            '</ion-card> '+
        '</div>'+
    '</div>'+    
'</div>';        
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