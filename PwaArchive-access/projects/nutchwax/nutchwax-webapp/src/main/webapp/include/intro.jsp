   <!-- starts NEW block of code: home landing page -->  
   <div id="flex-container">
	 
	  <!--<div id="welcomeMessage" class="hidden">
      <div id="closeMessage">&#10005;</div>-->
           <!--<h2 id="home-tagline"><fmt:message key='home.intro.welcome'/></h2>-->
          <!-- <img src="<fmt:message key='home.intro.welcome'/>" alt="<fmt:message key='home.intro.welcome.alt'/>">
          <p><fmt:message key='home.intro.welcome.description'/></p>-->
      <!--</div>-->
	  
	   <div>
	   	   <h3><fmt:message key='home.intro.video.presentation'/></h3>
		   <div id="archivePresentation-<%=language%>" class="call-to-actions">	
               <iframe title="<fmt:message key='home.intro.video.presentation'/>" src="https://www.youtube.com/embed/<fmt:message key='home.intro.presentation.youtube'/>?controls=0" frameborder="0" allow="accelerometer; autoplay; encrypted-media; gyroscope; " allowfullscreen></iframe>
		   </div>
	   </div> 
        <div id="intro-award">
	   	   <h3><fmt:message key='home.intro.award'/></h3>
		   <div id="award-<%=language%>" class="call-to-actions">
		   		<a href="<fmt:message key='home.intro.award.href'/>" alt="<fmt:message key='home.intro.award'/>" title="<fmt:message key='home.intro.award'/>">
		   		<fmt:message key='home.intro.award'/></a>
		   </div>
	   </div>
        <div>
	   	   <h3><fmt:message key='home.intro.preserved'/></h3>
		   <div id="pagesPreserved-<%=language%>" class="call-to-actions">
		   		<a href="<fmt:message key='home.video'/>" alt="<fmt:message key='home.intro.video.presentation'/>" title="<fmt:message key='home.intro.video.presentation'/>"><fmt:message key='home.intro.video.presentation'/></a>
		   </div>
	   </div>       
       
	   <div>
		   <h3><fmt:message key='home.intro.exhibitions'/></h3>
		   <div id="exhibitions-<%=language%>" class="call-to-actions">
		   		<a href="<fmt:message key='home.exhibitions.href'/>" alt="<fmt:message key='home.intro.exhibitions'/>" title="<fmt:message key='home.intro.exhibitions'/>"><fmt:message key='home.intro.exhibitions'/></a>
		   </div>
	   </div>
	   <div>
		   <h3><fmt:message key='home.intro.testimonials'/></h3>
		   <div id="testimonials-<%=language%>" class="call-to-actions">
		   		<iframe title="<fmt:message key='home.intro.testimonials'/>" src="https://www.youtube.com/embed/<fmt:message key='home.intro.testimonials.youtube'/>?controls=0" frameborder="0" allow="accelerometer; autoplay; encrypted-media; gyroscope; " allowfullscreen></iframe>
	   	   </div>
	   <div>   	   
   </div>     
<!-- starts The closing of the welcome blue box -->
<script type="text/javascript">
	if (typeof(Storage) !== "undefined") {
	  // Code for localStorage/sessionStorage.
	  if(localStorage.getItem('welcomeMessage') == null){
	  	/*User is new show welcomeMessage*/
	  	$('#welcomeMessage').removeClass('hidden');
	  }	  
	} else {
	  // Sorry! No Web Storage support..
	  $('#welcomeMessage').removeClass('hidden');
	} 
</script>  
<!-- starts The closing of the welcome blue box -->
<!-- ends NEW block of code: home landing page -->