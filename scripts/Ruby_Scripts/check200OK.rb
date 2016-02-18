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
    $FileName= '/data/nfs_share/awp-operations-configs/BlackList/URLsConfigurationList'
    $Reportfilename= "/var/log/Blacklist_report"
    #
    # Return true when the url is not blocked
    # Problems: Due to the closestdate algoritm, arquivo.pt might gather a web content that is not susposed to be blocked
    # As consequence, it will return a 200OK because the page is not the same.
    # URLisTheSame solved this problem.

    def response404 (url,pattern)
    result=false
        begin
            response = open(url).status
            url_retrieved = open(url).base_uri.to_s
            tobePrinted='URL NOT BLOCKED: '+ url
            result=true
            url_aux = URI(url.sub(pattern, ""))
            url_aux_2= URI(url_retrieved.to_s.sub(pattern,""))
	       result =  url_aux.eql? url_aux_2
           if result # if not returns 404 and the url is the same, the url is not blocked
                puts tobePrinted 
           end            
            target.write(tobePrinted)
            target.write("\n")
            SystemExit.new(2,"#{url} is not on proper way")
        rescue => e
            if e.message== "404 Not Found"
                SystemExit.new(0,"#{url} is putting in the dark")
            end
        end
    result
    end

    time = Time.new
    target=open($Reportfilename,'a')
    target.write(time)
    target.write("\n")
    isnotDoneBoth=true # var to control when it was already checked both brokers
    firstCycle=true # var to control which broker to check availability
    isnotBlocked=false
    urlAretheSame_p58=false
    urlAretheSame_p62=false
    File.open($FileName).each do |url|
        while isnotDoneBoth do
            urlPerformed=""
                if firstCycle 
                    urlPerformed="http://p58.arquivo.pt/#{url}"
                    firstCycle=false
                    aux = response404(urlPerformed,"p58.")
                else
                    urlPerformed="http://p62.arquivo.pt/#{url}"
                    firstCycle=true
                    isnotDoneBoth=false
                    aux=response404(urlPerformed,"p62.")
                end
                unless isnotBlocked # there are at least one not blocked URL	
                    isnotBlocked=aux
                end
        end
            isnotDoneBoth=true
    end
    unless isnotBlocked 
       puts ("ALL BLOCKED")
    end
