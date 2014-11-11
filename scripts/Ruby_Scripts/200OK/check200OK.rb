#
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
File.open($FileName).each do |url|
    begin
#    puts url
        response = open(url).status
        tobePrinted=url+ ' is online.'
        puts tobePrinted.gsub("\n","")
        SystemExit.new(2,"#{url} is not on proper way")
    rescue => e
        if e.message == "404 Not Found"
            SystemExit.new(0, "#{url} is put in the dark")
        end
    end
end
