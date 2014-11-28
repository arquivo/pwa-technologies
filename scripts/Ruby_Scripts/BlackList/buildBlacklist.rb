#!/usr/bin/env ruby
#
#Developed by PWA
#This is the mais class to execute the Blacklist
#
#
def checkUrls
if CheckURLs.eql? "V"
    #Target.write("URL Number\n")
    targetWrite = open(FileName, 'r')
   
    targetWrite.each_line do |line|
        num = line.split(" ").last.to_i
        url_aux = line.split(" ").first
        InfoUrl.each do|key,value|
            if key.eql? url_aux
                unless value.to_i == num
                    puts "URL Changes on blacklist #{key} #{value}"
                end
            end
        end
    end
end
end
FileName= '/shareT2/backups/configs/httpd/Brookers/URLsInfo' # This is the name of the file where is stored information about number of url that was gathered
Target = open(FileName, 'w')

CheckURLs = ARGV[0]
require_relative 'ParseTabelaPrincipal'
PWA = ParseTabelaPrincipal.new
nUrls=0
PWA.clearFiles
InfoUrl = Hash.new
File.open($FileName).each do |url|
churl = "http://arquivo.pt/search.jsp?l=pt&query="+url
PWA.Initialize(churl)
link_arr_1=PWA.ParsePage('a',"<a href=\"http://arquivo.pt/")
InfoUrl[url.gsub("\n","")] = link_arr_1.length
puts url
puts "Number of URL's: #{link_arr_1.length}"
if CheckURLs.eql? "V"
    Target.write("#{url.gsub("\n","")} #{link_arr_1.length}\n")
end
nUrls+=link_arr_1.length
if link_arr_1.length >0
    PWA.printUrlWithID(link_arr_1,url)
    PWA.printRulesOnfile(link_arr_1,url)
   #Delete characters before ?
else
    PWA.printBrokenLinks
end
end
Target.close
puts "Total Number of URLs #{nUrls}"
checkUrls
#puts InfoUrl
