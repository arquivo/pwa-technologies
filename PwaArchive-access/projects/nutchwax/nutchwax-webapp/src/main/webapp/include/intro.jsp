   <!-- starts NEW block of code: home landing page -->  
   <div id="flex-container">
	  <div id="welcomeMessage">
      <div id="closeMessage">x</div>
           <h2><fmt:message key='home.intro.welcome'/></h2>
            <p><fmt:message key='home.intro.welcome.description'/></p>
       </div>  
	   <div>
	   	   <h3><fmt:message key='home.intro.award'/></h3>
		   <div id="premios-<%=language%>" class="call-to-actions">
		   		<a href="<fmt:message key='home.intro.award.href'/>" alt="<fmt:message key='home.intro.award'/>" title="<fmt:message key='home.intro.award'/>">
		   		<fmt:message key='home.intro.award'/></a>
		   </div>
	   </div>


	   <div>
		   <h3><fmt:message key='home.intro.video.presentation'/></h3>
		   <div id="arquivoPresentation" class="call-to-actions">
		   		<iframe title="<fmt:message key='home.intro.video'/>" src="https://www.youtube.com/embed/<fmt:message key='home.intro.presentation.youtube'/>?controls=0" frameborder="0" allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
		   </div>
	   </div>
	   <div>
	   	   <h3><fmt:message key='home.intro.preserved'/></h3>
		   <div id="exemplos-Paginas-<%=language%>" class="call-to-actions">
		   		<a href="<fmt:message key='home.intro.preserved.href'/>" alt="<fmt:message key='home.intro.preserved'/>" title="<fmt:message key='home.intro.preserved'/>">
		   		<fmt:message key='home.intro.preserved'/></a>
		   </div>
	   </div> 
	   <div>
		   <h3><fmt:message key='home.intro.exhibitions'/></h3>
		   <div id="exposicoes" class="call-to-actions">
		   		<a href="<fmt:message key='home.exhibitions.href'/>" alt="<fmt:message key='home.intro.exhibitions'/>" title="<fmt:message key='home.intro.exhibitions'/>"><fmt:message key='home.intro.exhibitions'/></a>
		   </div>
	   </div>
	   <div>
		   <h3><fmt:message key='home.intro.testimonials'/></h3>
		   <div id="testemunhos" class="call-to-actions">
		   		<iframe title="<fmt:message key='home.intro.testimonials'/>" src="https://www.youtube.com/embed/<fmt:message key='home.intro.testimonials.youtube'/>?controls=0" frameborder="0" allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
	   	   </div>
	   <div>   	   
   </div>  
   <!-- ends NEW block of code: home landing page -->