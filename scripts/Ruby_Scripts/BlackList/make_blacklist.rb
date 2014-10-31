#
#Input: 
#   This script needs a file called URL wich provides the URL's list.
#Output: 
#   Returns tree files: Result, BrokenLinks and ResultRules
#   Result: Contains the URL's that was success
#   BrokenLinks: Contains the URL's with same probelm,for instance no indexation
#   ResultReules: Rules for Apache
#
require 'rubygems'
require 'nokogiri'
require 'open-uri'

File.open('URL').each do |url|
    # Filenames
    # Result: is where the Rules are written
    # BrokenLinks: The pages that have problems
    Result_filename = "Result"
    BrokenLinks_filename="BrokenLinks"
    ResultRules_filename="ResultRules"
    #print "What is the URL? "
    #url = gets.chomp
    #puts "We're going to erase #{filename}"
    url = "http://arquivo.pt/search.jsp?l=pt&query="+url
    puts "URL:  #{url}"
    doc = Nokogiri::HTML(open(url))
    puts doc.at_css("title").text
    link_arr = Array.new
    link_aux= String.new
    doc.css(".tabela-principal").each do |item|
        links = item.css("a").to_s
        link=links.split("<a href=\"http://arquivo.pt/")
        link_arr =link 
        link_arr.delete_at 0
    end
    if link_arr.length >0
    #Delete characters before ?
    target = open(ResultRules_filename, 'a')
    target_result = open(Result_filename,'a')
    target_result.write(url)
    target.write("# URL: #{url}")
    link_arr.each do |aux| # if contains indexed pages
#        puts "RewriteRule ^#{aux[0..aux.index('?').to_i-1]} - [R=404,NC,L]"
        target.write( "RewriteRule ^/#{aux[0..aux.index('?').to_i-1]} - [R=404,NC,L]")
        target.write("\n")
    end
   else
    target = open(BrokenLinks_filename, 'a')
    target.write("# URL: #{url}")
   end
end
