var validated_form=false;
var loadFormValidation=function(){
	$("form").submit(function(){
		var erro=false;
		var this_form=$(this);
		var submit_errors=[];
		var ct=0;
		var tested=[];
		$(this).find('.required').each(function(){
			if((($(this).attr('type')=='checkbox' || $(this).attr('type')=='radio') && !$(this).is(':checked')) || $(this).val()==''){
				$(this).parent().addClass('error');
				if(!tested[$(this).siblings('label').text().replace("*","")]){
					submit_errors[ct]='<p><b>- '+$(this).siblings('label').text().replace("*","")+'</b><span>preenchimento obrigatório</span></p>';
					tested[$(this).siblings('label').text().replace("*","")]=true;
				}
				erro=true;
				ct++;
			}else{
				$(this).parent().removeClass('error');
			}
		});

		var reg = [];
		//---- formulário candidatura emprego
		reg['email_candidato']=/^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
		reg['telefone_candidato']=/^[0-9]{9}$/;
		reg['postal_candidato']=/^[0-9]{4}-[0-9]{3}$/;
		reg['aniv_candidato']=/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/;

		//---- formulário subscrição comunicados
		reg['s_email']=/^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;

		//---- formulário pedido webcast
		reg['data_ini_testes']=reg['data_fim_testes']=reg['data_ini_evento']=reg['data_fim_evento']=/^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}$/
		reg['ident_tecnico']=/^[a-zA-Z0-9_]+$/;
		reg['end_web']=/^(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?$/;

		//---- formulário faq
		reg['faq_email']=/^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;

		reg['dns_dominio']=/^[a-zA-Z0-9_]+$/;

		$(this).find('.sintaxe').each(function(){
			var bloco=$(this).parent();
			if(!$(this).hasClass('required') && $(this).val()==''){
				bloco.removeClass('error');
			}else if(reg[$(this).attr('id')]){
				if(!reg[$(this).attr('id')].test($(this).val())){
					bloco.addClass('error');
					//submit_errors[$(this).siblings('label').text()]='formato incorrecto';

					if(!tested[$(this).siblings('label').text().replace("*","")]){
						submit_errors[ct]='<p><b>- '+$(this).siblings('label').text().replace("*","")+'</b><span>formato incorrecto</span></p>';
						tested[$(this).siblings('label').text().replace("*","")]=true;
					}
					erro=true;
					ct++;
				}
				else{
					bloco.removeClass('error');
				}
			}
		});
		if(erro){
			var t_div=$("#error_"+this_form.attr('id'));
			if(t_div.length>0){
				t_div.find('.form_error_desc').html('');
				for(var i=0; i<submit_errors.length; i++){
					t_div.find('.form_error_desc').append(submit_errors[i]);
				}
				update_facebox(t_div.html());
			}
			return false;
		}
		validated_form=true;
	 });
}

$(function(){
	$(".datepicker").datepicker(
	{
		  showOn:           'both',
		  buttonImage:      '/temas/fccn/images/calendar_icon.gif',
		  buttonImageOnly:  true,
		  dateFormat:       'yy-mm-dd',
		  changeYear: 		true,
		  changeMonth: 		true,
		  maxDate: 			'-20y',
		  yearRange: 		'c-80:c+00'
	});

	$('.datetimepicker').datetimepicker({
		showOn:				'both',
		buttonImage:		'/temas/fccn/images/calendar_icon.gif',
		buttonImageOnly:  true,
		dateFormat:			"yy-mm-dd",
		changeYear: 		true,
		changeMonth: 		true,
		timeText:			"",
		hourText:			"Horas",
		minuteText:			"Minutos",
		currentText:		"Agora",
		stepMinute:			5
	});

	/***************** VALIDAÇÃO FORMS (START) **********************/
	 $('.form_error .close').live('click',function(){
		jQuery(document).trigger('close.facebox');
	 });

	 $(".help_trigger").live('click',function(e){
		var dist=5;
		var flag=true;
		var padding=20;
		if(!flag){
			return false;
		}

		if($("#error_help").length<=0){
			$("#wrapper").prepend('<div id="error_help"><div class="content"></div></div>');
		}


		$('#error_help .content').html('<div class="loading_ajax"><img src="/img/ajax-loader.gif" alt="Loading..."/></div>');
		$("#error_help").css('top',e.pageY-$("#error_help").height()-dist-padding);
		$("#error_help").css('left',e.pageX+dist);
		$("#error_help").fadeIn();
		flag=false;
		$.get($(this).attr('href'), function(data){
			$('#error_help .content').html(data);
			$("#error_help").css('top',e.pageY-$("#error_help").height()-dist-padding);
			$("#error_help").css('left',e.pageX+dist);
			flag=true;
		});
		return false;
   });

   $(".help_trigger").live('mouseout',function(e){
		$("#error_help").fadeOut();
   });
   /***************** VALIDAÇÃO FORMS (END) **********************/
});
