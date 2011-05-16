// Read a page's GET URL variables and return them as an associative array.
function getUrlVars()
{
    var vars = new Array(), hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        //vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}

$(document).ready( function() {
	/* Set focus in the search box */
	$('#query_top').focus();

	/* Calendar configuration */
	$(function() {
        var min = minDate;
        var max = maxDate; 

                        $('#dateStart_top, #dateStart_bottom').datepicker(
                                $.extend({
                                        changeMonth: true,
                                        changeYear: true,
					dateFormat: 'dd/mm/yy',
                                        minDate: min,
                                        maxDate: max,
                                        yearRange: '-15:+15',
                                	showOn: 'button',
                                        showButtonPanel: true,
                                        currentText: 'OK',
                                        fuzzySelection: false,
                                        buttonImageOnly: true,
                                        buttonImage: 'img/calendar.gif',
                                        buttonText: 'Calendário - data inicial'
				})
                        );
		
                        $('#dateEnd_top, #dateEnd_bottom').datepicker(
                                $.extend({
                                        changeMonth: true,
                                        changeYear: true,
					dateFormat: 'dd/mm/yy',
                                        minDate: min,
                                        maxDate: max,
                                        yearRange: '-15:+15',
                                	showOn: 'button',
                                        showButtonPanel: true,
                                        currentText: 'OK',
                                        fuzzySelection: true,
                                        buttonImageOnly: true,
                                        buttonImage: 'img/calendar.gif',
                                        buttonText: 'Calendário - data final'
				})
                        );

                        $(".ui-datepicker-trigger").mouseover(function() {
                                $(this).css('cursor','pointer');
                        });

                });

	/* Search feedback for slow searches/page views */
	var timeout = 2000;	

	$("h3 a").click( function(event){
		$('.feedback').remove();	//remove existing feedback
		var n = $(this);
		setTimeout(function(node) {return function() {
			node.parents('li').find('.history').after('<img src="img/loader.gif" class="feedback" />');
		}; } (n), timeout);
   	});
	$(".history").click( function(event){
		$('.feedback').remove();	//remove existing feedback
		var n = $(this);
		setTimeout(function(node) {return function() {
			node.after('<img src="img/loader.gif" class="feedback" />');
		}; } (n), timeout);
	});
	$(".submit").click( function(event){
        	var addLoader = function(node) {
                	var inputLoadFeedback = {'background':'#fff url("img/loader.gif") no-repeat right'};
	                $('#query_top').css(inputLoadFeedback);
        	        $('#query_bottom').css(inputLoadFeedback);
	         };
        	setTimeout( addLoader, timeout, $(this) );
	});

	var params = getUrlVars();

	if ( params['query'] !== undefined && params['query'] !== '' ) {
		var queryParam = decodeURIComponent(params['query'].replace(/\+/g, ' ') ).trim();
		var queryCleaned = $( "<div>"+ queryParam +"</div>").text().trim();/* 2nd trim needed to clean after tags are removed */
		var spellchecker = location.protocol +"//"+ location.host +"/spellchecker/checker";
		spellchecker += "?query="+ queryCleaned;
		spellchecker += ( (params['l'] === undefined) ? '' : '&l='+ params['l']);

		$.get( spellchecker, function (data) {
			var correctionText = $.trim( $(data).filter('#correction').html() );
			var correctionUrl = window.location.pathname +"?";

			if ( queryParam !== correctionText) {
				for (var p in params) {
					if (p == 'query') {
						correctionUrl += p +'='+ encodeURIComponent(correctionText) +'&';
					} else {
						correctionUrl += p +"="+ params[p] +"&";
					}
                                }
				correctionUrl += 'spellchecked=true';
				correctionUrl = correctionUrl.replace("%3Cem%3E","");
				correctionUrl = correctionUrl.replace("%3C%2Fem%3E","");

				$('.suggestion').html('<a href="'+ correctionUrl +'"></a>');
				$('.suggestion a').html( correctionText );

				$('.spell').removeClass('hidden');
			}
		} );
	}
});
