$(function(){
    $('.carousel').jcarousel({
        buttonPrevHTML: '<div class="seta-left-scroller"><img src="/temas/fccn/images/setascroller-esq.png" alt="Esquerda" width="21" height="120" border="0" /></div>',
        buttonNextHTML: '<div class="seta-right-scroller"><img src="/temas/fccn/images/setascroller-dir.png" alt="Direita" width="21" height="120" border="0" /></div>',
        scroll:         1
    });
	
	$('#destaque_grande ul.ciclo').cycle(
	{
		fx:                 'fade',
		timeout:            0,
		pager:              '#destaque_grande .paginacao',
		pagerAnchorBuilder: function(idx, slide)
		                    {
							  return '<span>'+(idx+1)+'</span>';
		                    }
	});
});