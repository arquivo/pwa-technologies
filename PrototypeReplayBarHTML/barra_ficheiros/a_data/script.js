function trim(str, chars) {
    return ltrim(rtrim(str, chars), chars);
}

function ltrim(str, chars) {
    chars = chars || "\\s";
    return str.replace(new RegExp("^[" + chars + "]+", "g"), "");
}

function rtrim(str, chars) {
    chars = chars || "\\s";
    return str.replace(new RegExp("[" + chars + "]+$", "g"), "");
}

function update_facebox(data){
    if($('#facebox').is(':visible')){
        $('#facebox .content').html(data);
    }
    else{
        $.facebox(data);
    }
}
var addthis_config =
{   
    services_compact:	'facebook,twitter,digg,more',
    ui_click:			true
}

var loadForm=function(){
    $(".popup_form").submit(function(){
        var result=$(this).serialize();
        var action=$(this).attr('action');
        var method=$(this).attr('method');
        $.ajax({
            type:method,
            url:action,
            data:result,
            beforeSend:function(){
                update_facebox('<div class="loading_ajax"><img src="/img/ajax-loader.gif" alt="Loading..."/></div>');
            },
            success:function(res){
                if(res!=''){
                    update_facebox(res);
                    loadFormValidation();
                    loadForm();
                }
            }
        });
        return false;
    });
}

$(function(){		 
    $("#jumpMenu").change(function(){
        if($(this).val() != 0){
            window.location = "http://" + $(this).val();
        }
    });
    /*********** OFERTAS DE EMPREGO *************/
    $(".titulo_oferta a").live('click',function(){
        var obj=$(this);				
        $(this).parent().parent().siblings().find('.detalhe_oferta').slideUp();		
        obj.parent().next('.detalhe_oferta').slideToggle();
    });

    $("#a_termos").live('click',function(){		
        $("#termos_condicoes").slideToggle();
    });

    $(".termos_aplicacoes").live('click',function(){
        $(this).parent().next(".termos_condicoes").slideToggle();
    });

    //--------------------------------------------------------------
    $('.cycle').cycle(
    {
        fx:                 'fade',
        timeout:            10000
    });

    /********** TOOLTIP (start) *************/
    $("a.tooltip").live('click',function(e){
        //if($('#tooltip').is(':visible'))
        //	return false;
        var pos=$(this).offset();
        var obj=$(this);
        var dist=50;
        $('#tooltip .content').html('<div class="loading_ajax"><img src="/img/ajax-loader.gif" alt="Loading..."/></div>');
        $("#tooltip").css('top',e.pageY-$("#tooltip").height());
        $("#tooltip").css('left',e.pageX-$("#tooltip").width()+dist);
        $("#tooltip").fadeIn();

        $.get($(this).attr('href'), function(data){
            $('#tooltip .content').html(data);
            $("#tooltip").css('top',e.pageY-$("#tooltip").height());
            $("#tooltip").css('left',e.pageX-$("#tooltip").width()+dist);
        });
        return false;
    });

    $("#close_tooltip").click(function(){
        $("#tooltip").fadeOut(function(){
            $("#tooltip .content").html('');
        });
    });
    /********** TOOLTIP (end) *************/

    /********** AJAX LINK (start) *********/
    $('a.ajax_link').live('click',function(){
        var target=$(this).attr('rel');
        var update=true;
        var obj=$(this);
        var href=$(this).attr('href');
        $('#'+target).html('<div class="loading_ajax"><img src="/img/ajax-loader.gif" alt="Loading..."/></div>');
        $.get(href, function(data) {
            $('#'+target).html(data);
            loadFormValidation();
            loadForm();
			window.location.hash='!'+href;
        });
        return false;
    });

	var hsh=window.location.hash.replace("#!", "");
	if(hsh!=''){
		window.location=hsh;
	}
	
    $(".ajax_form").submit(function(){
        var result=$(this).serialize();
        var action=$(this).attr('action');
        var method=$(this).attr('method');
        var target='';
        if($(this).find('input[class="target"]').length>0){
            target=$(this).find('input[class="target"]').val();
        }
        if($('#'+target).length > 0){
            $.ajax({
                type:method,
                url:action,
                data:result,
                beforeSend:function(){						
                    $('#'+target).html('<div class="loading_ajax"><img src="/img/ajax-loader.gif" alt="Loading..."/></div>');						
                },
                success:function(res){						
                    $('#'+target).html(res);
                }
            });
        }
        return false;
    });
    /********** AJAX LINK (end) *********/

    /********** TOGGLE AJAX (start) *********/
    $('a.toogle_ajax').live('click',function(){
        $("#lista_ofertas_emprego p a.purple").removeClass("purple");
        $(this).addClass("purple");
        var target=$(this).attr('rel');
        if(trim($('#'+target).html())==''){
            $('#'+target).html('<div class="loading_ajax"><img src="/img/ajax-loader.gif" alt="Loading..."/></div>');
            $.get($(this).attr('href'), function(data) {
                $('#'+target).html(data);
                loadFormValidation();
            });
        }
        return false;
    });
    /********** TOGGLE AJAX (end) *********/

	
    /********** FACEBOX (start) *********/

    $('a[rel=facebox]').live('click',function(){
        update_facebox('<div class="loading_ajax"><img src="/img/ajax-loader.gif" alt="Loading..."/></div>');
        $.get($(this).attr('href'), function(data) {
            if(data!=''){
                update_facebox(data);
                loadFormValidation();
                loadForm();
            }
        });
        return false;
    });
    loadFormValidation();
    loadForm();
    /********** FACEBOX (END) *********/

    //---------- TABS
    $(".tabs a").click(function(){       
        $(this).parent().siblings().removeClass('tabselected');       
        $(this).parent().addClass('tabselected');
    });

    $(".select_auto_submit").change(function(){
        window.location=$(this).val();
    });

    /**************************/
    $(".radiobtn-eventos input").click(function(){		
        if($(this).attr('id')=="arquivo_eventos" || $(this).attr('id')=="arquivo_recortes" || $(this).attr('id')=="arquivo_videos"){
            $("#select_year").removeAttr('disabled');
        }else{
            $("#select_year").attr('disabled',true);
        }
    });

    $("#form_contactos_servicos").submit(function(){
        var target='show_service_contacts';
        var href=$(this).find('select[name=tema]').val();
        if(href==''){
            $('#'+target).hide();
        }else{
            $('#'+target).html('<div class="loading_ajax"><img src="/img/ajax-loader.gif" alt="Loading..."/></div>');
            $.get(href, function(data) {
                $('#'+target).html(data);
                $('#'+target).show();
                loadForm();
            });
        }
        return false;
    });
    var search_style=$("input[name=pesquisagoogle]").attr('style');
    $("input[name=pesquisagoogle]").focus(function(){
        $(this).removeAttr('style');
    });

    $("input[name=pesquisagoogle]").blur(function(){
        if($(this).val()==''){
            $(this).attr('style',search_style);
        }
    });

	var input=[];
	$('.toggle_input').each(function(){
		input[$(this).attr('id')]=$(this).val();
	});

	$('.toggle_input').focus(function(){
		if($(this).attr('readonly')==false && $(this).val()==input[$(this).attr('id')]){
			$(this).val('');
		}
	});

	$('.toggle_input').blur(function(){
		if($(this).attr('readonly')==false && $(this).val()==''){
			$(this).val(input[$(this).attr('id')]);
		}
	});
});