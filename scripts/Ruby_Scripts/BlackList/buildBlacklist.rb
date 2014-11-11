#
#Developed by PWA
#This is the mais class to execute the Blacklist
#
#
require_relative 'ParseTabelaPrincipal'
PWA = ParseTabelaPrincipal.new
nUrls=0
PWA.clearFiles
File.open($FileName).each do |url|
churl = "http://arquivo.pt/search.jsp?l=pt&query="+url
PWA.Initialize(churl)
link_arr_1=PWA.ParsePage('a',"<a href=\"http://arquivo.pt/")
puts url
puts "Number of URL's: #{link_arr_1.length}"
nUrls+=link_arr_1.length
if link_arr_1.length >0
    PWA.printUrlWithID(link_arr_1,url)
    PWA.printRulesOnfile(link_arr_1,url)
   #Delete characters before ?
else
    PWA.printBrokenLinks
end
end
puts "Total Number of URLs #{nUrls}"
