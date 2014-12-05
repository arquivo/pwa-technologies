#!/usr/bin/env ruby
#Input: 
#   This script needs a file called URL wich provides the URL's list.
#Output: 
#   Returns tree files: Result, BrokenLinks and ResultRules
#   Result: Contains the URL's that was success
#   BrokenLinks: Contains the URL's with same probelm,for instance no indexation
#   ResultReules: Rules for Apache
#
require 'open-uri'
$FileName= '/shareT2/backups/configs/BlackList/URLsConfigurationList'
$Reportfilename= "/var/log/Blacklist_report"
def response (url)
    begin
        response = open(url).status
        tobePrinted=url+ ' is online.'
        puts tobePrinted.gsub("\n","")
        target.write(tobePrinted)
        target.write("\n")
        SystemExit.new(2,"#{url} is not on proper way")
    rescue => e
        if e.message== "404 Not Found"
            SystemExit.new(0,"#{url} is putting in the dark")
        end
    end
end



time = Time.new
target=open($Reportfilename,'a')
target.write(time)
target.write("\n")
isnotDoneBoth=true
firstCycle=true
File.open($FileName).each do |url|
    while isnotDoneBoth do
            urlPerformed=""
                if firstCycle 
                    urlPerformed="http://p58.arquivo.pt/#{url}"
                    firstCycle=false
                else
                    urlPerformed="http://p62.arquivo.pt/#{url}"
                    firstCycle=true
                    isnotDoneBoth=false
                end
                response (urlPerformed)
     end
        isnotDoneBoth=true
end
