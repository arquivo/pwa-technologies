#!/usr/bin/env ruby
#
#Developed by PWA
#This is the mais class to execute the Blacklist
#
#
require 'open-uri'
def checkUrls
if CheckURLs.eql? "V"
    targetRead = open(FileName, 'r')
   
    targetRead.each_line do |line|
        num = line.split(" ").last.to_i
        url_aux = line.split(" ").first
        InfoUrl.each do|key,value|
            if key.eql? url_aux
                unless value.to_i == num
                    puts "URL Changes on blacklist #{key} #{value}"
                    $Haschanges=true
                end
            end
        end
    end
end
end
def saveChanges
    if $Haschanges
        targetWrite = open(FileNameBackup,'w')
    else
        targetWrite = open(FileName,'w')
    end
    InfoUrl.each do|key,value|
        targetWrite.write("#{key} #{value}\n")
    end
end
$Haschanges=false
FileName= '/shareT2/backups/configs/httpd/Brookers/URLsInfo' # This is the name of the file where is stored information about number of url that was gathered
FileNameBackup = '/shareT2/backups/configs/httpd/Brookers/URLsInfo_Backup'
CheckURLs = ARGV[0]
require_relative 'ParseTabelaPrincipal'
PWA = ParseTabelaPrincipal.new
nUrls=0
PWA.clearFiles
InfoUrl = Hash.new
File.open($FileName).each do |url|
churl = "http://arquivo.pt/search.jsp?l=pt&query="+url
PWA.Initialize(churl)
link_arr=PWA.ParsePage('a',"<a href=\"http://arquivo.pt/")
InfoUrl[url.gsub("\n","")] = link_arr.length
puts url
puts "Number of URL's: #{link_arr.length}"
nUrls+=link_arr.length
if link_arr.length >0
    PWA.printUrlWithID(link_arr,url)
    PWA.printRulesOnfile(link_arr,url)
   #Delete characters before ?
else
    PWA.printBrokenLinks
end
end
puts "Total Number of URLs #{nUrls}"
