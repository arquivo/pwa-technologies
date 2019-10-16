    <div class="container-fluid topcontainer">
        <div class="row">
            <div class="col-sm-10 col-sm-offset-1 col-md-8 col-md-offset-2 col-lg-6 col-lg-offset-3 text-right">                            
                
                <!-- start search box -->
                <form id="searchForm" action="/search.jsp">                
                <div id="form_container"> 
                    <div id="searchBarBlock" class="input-group stylish-input-group">
                        
                            <input name="query" id="txtSearch" type="search" class="form-control no-radius search-input" placeholder="<fmt:message key='home.search.placeholder'/>" autocapitalize="off" autocomplete="off" autocorrect="off" autofocus="on"> 
                            <input type="hidden" id="l" name="l" value="<%=language%>">
                    </div>
                    <!-- starts search lupe and "x" close button -->
                    <div>
                      <span class="clear-text"><i class="fa fa-close"></i></span>                         
                              <span id="buttonSearch" class="input-group-addon no-radius search-button-span">
                                   <button class="search-button" type="submit">
                                      <span class="glyphicon glyphicon-search white"></span>
                                  </button>  
                     </span> 
                     </div>                 
                     <!-- ends Paginas and images links option -->                       
                    </div>                                
                 </form>    

                <script src="/include/clearForm.js"></script>      
            </div>
        </div>
</div>