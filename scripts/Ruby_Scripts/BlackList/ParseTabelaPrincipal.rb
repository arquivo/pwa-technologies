#
#Developed by PWA
#Input: 
#   This script needs a file called URL wich provides the URL's list.
#Output: 
#   Returns tree files: Result, BrokenLinks and ResultRules
#   Result: Contains the URL's that was success
#   BrokenLinks: Contains the URL's with same probelm,for instance no indexation
#   ResultReules: Rules for Apache
#
class ParseTabelaPrincipal
require 'rubygems'
require 'nokogiri'
require 'open-uri'
#Begin declaration of files used
$FileName= '/shareT2/backups/configs/httpd/Brookers/URLsBlackList' # This is the name of the file tobe imported for processing URL
$Result_filename = "Result" # Files processed succesfuly 
$BrokenLinks_filename="BrokenLinks" # This is where are located the URLs that had problems
$ResultRules_filename="ResultRules" # File with rules to add on apache
$Wiki_filename="wikiFile" # File with wiki format
#End declaration of files used
$ArquivoPT='http://arquivo.pt/wayback/wayback/'
$waybackMachine_array=Array.new
$PWAIndex=String.new
$waybackMachine=Array.new
def Initialize(url)
    @url = url
end
def clearFiles()
    if File.file?($BrokenLinks_filename)
        File.delete($BrokenLinks_filename)
    end
    if File.file?($ResultRules_filename)
        File.delete($ResultRules_filename)
    end
    if File.file?($Result_filename)
        File.delete($Result_filename)
    end
    if File.file?($Wiki_filename)
        File.delete($Wiki_filename)
    end
end

def ParsePage(symbolTobeFind,splitSymbol)
   doc = Nokogiri::HTML(open(@url))
   link_arr = Array.new
   doc.css(".tabela-principal").each do |item|
      links = item.css(symbolTobeFind).to_s
      link=links.split(splitSymbol)
      link_arr =link 
      link_arr.delete_at 0
   end
   link_arr
end
def getWaybackArray()
$waybackMachine_array
end
def setWaybackArray()
$waybackMachine_array.clear
end
def createLineRules(array,delimiter)
    array.each do |aux| # if contains indexed pages
        processArray((aux[0..aux.index(delimiter).to_i-1]))
    end
end

def printRulesOnfile(linesTobePrint,initURL)
    i=0
    isHTTP=true
    target = open($ResultRules_filename, 'a')
    target.write("\n\n\n#Wayback URL query: #{@url}")
    target.write("   RewriteRule ^#{$waybackMachineALL}#{initURL} - [R=404,NC,L]".gsub("\n",""))
    if initURL.include? "http://"
        target.write("\n")
        target.write("   RewriteRule ^#{$waybackMachineALL}#{initURL.split("http://").last} - [R=404,NC,L]".gsub("\n",""))
    else
        target.write("\n")
        target.write("   RewriteRule ^#{$waybackMachineALL}http:\/\/#{initURL} - [R=404,NC,L]".gsub("\n",""))
        isHTTP=false
    end
    target.write("\n#PWA URL query")
    linesTobePrint.each do |aux| # if contains indexed pages
        target_result = open($Result_filename,'a')
        target_aux = open($Wiki_filename,'a')
        target_result.write(@url)
        target.write("\n") 
        target.write("   RewriteRule ^/#{aux[0..aux.index('?').to_i-1]} - [R=404,NC,L]".gsub("\n",""))
        unless $waybackMachine.nil?
            target_aux.write("|- \n")
            if isHTTP
                 target_aux.write("|http://arquivo.pt/#{aux[0..aux.index('?').to_i-1]} ||  #{$waybackMachine[i]}#{initURL}\n #{$waybackMachine[i]}#{initURL.split("http://").last}\n}")
            else
                 target_aux.write("|http://arquivo.pt/#{aux[0..aux.index('?').to_i-1]} || #{$waybackMachine[i]}http://#{initURL}\n #{$waybackMachine[i]}#{initURL}\n")
            end
            i+=1
        end
    end
end
def printUrlWithID(linesTobePrint,initURL)
    linesTobePrint.each do |aux| 
        $PWAIndex="http://arquivo.pt/#{aux[0..aux.index('?').to_i-1]}"
        $PWAIndex="http://p58.arquivo.pt:8080/#{aux[0..aux.index('?').to_i-1]}"
        makeWayBack
    end
end

def makeWayBack 
    begin
        doc = Nokogiri::HTML(open($PWAIndex))
        aux = doc.to_s
        i=aux.index('sWayBackCGI').to_i
        $waybackMachine.push(aux[i..i+72].split("\"").last)
        $waybackMachineALL=aux[i+41..i+57].split("\"").last+"(\.*)/"
        rescue Exception => e
            puts e.message 
            puts  "An error occured in  the url: #{$PWAIndex}, this have to be done manualy"
    end
end

def printWaybackMachineonFile(initURL)
    target=open($WaybackMachineFileResult,'a')
    $waybackMachine_array.each do |lineTobePrint|
        target.write("#{lineTobePrint}/#{initURL}")
    end
end
def printBrokenLinks()
    target = open($BrokenLinks_filename, 'a')
    target.write("# URL: #{@url}")
end

end
