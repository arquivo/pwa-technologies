package org.apache.lucene.search.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PwaSpamRegex {

    public static final Log LOG = LogFactory.getLog(PwaSpamRegex.class);

    private static PwaSpamRegex pwaSpamRegex = null;
    public String regexPatternString = "";

    private PwaSpamRegex(){
        LOG.info("Initializing PwaSpamRegex Singleton...");

        try {
            InputStream inputStream = PwaSpamRegex.class.getClassLoader().getResourceAsStream("config.properties");

            Properties prop = new Properties();
            prop.load(inputStream);
            this.regexPatternString = prop.getProperty("spam.regex.pattern");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PwaSpamRegex getInstance(){
        if (pwaSpamRegex == null){
            pwaSpamRegex = new PwaSpamRegex();
        }

        return pwaSpamRegex;
    }
}
