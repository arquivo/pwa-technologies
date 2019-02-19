$("#txtSearch").on("change paste keyup", function() {
  $(this).val().trim().length > 0 ? $('.clear-text').show() : $('.clear-text').hide();
   
}); 
$(".clear-text").on("click", function() {
   $("#txtSearch").val('');
   $(this).hide();
   $("#txtSearch").focus();
   
});     
$('#searchForm').submit(function() 
{
    if ($.trim($(".form-control").val()) === "") {
        /*TODO:: Do something when user enters empty input?*/
    return false;
    }
});    
