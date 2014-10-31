require_relative 'ParseTabelaPrincipal'
PWA = ParseTabelaPrincipal.new
nUrls=0
PWA.clearFiles
File.open($FileName).each do |url|
#print "What is the URL? "
#url = gets.chomp
#puts "We're going to erase #{filename}"
churl = "http://arquivo.pt/search.jsp?l=pt&query="+url
#waybackmachine.Initialize(churl)

#Do the waybackQueryUrl

#link_arr=waybackmachine.ParsePage('a',"title=\"")
#puts "Number of URL's: #{link_arr.length}"
#waybackmachine.createLineRules(link_arr,'">')
#waybackmachine.printWaybackMachineonFile(url)
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
#waybackmachine.setWaybackArray()
end
#aux = PWA.getPWAIndex_array
#puts aux
puts "Total Number of URLs #{nUrls}"
