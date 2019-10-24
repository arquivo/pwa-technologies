package org.apache.lucene.util;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SpamBlacklistGenerator {
    private Pattern regexPatternString;

    public SpamBlacklistGenerator() throws IOException {
        this.regexPatternString = this.generateDomainsRegexPattern();
    }

    private Pattern generateDomainsRegexPattern() throws IOException{
        StringBuilder stringBuilderPattern = new StringBuilder();
        InputStream inputStream = SpamBlacklistGenerator.class.getClassLoader().getResourceAsStream("config.properties");

        Properties prop = new Properties();
        prop.load(inputStream);

        String domainStrList = prop.getProperty("spam.domains");
        String[] domains = domainStrList.split(",");

        stringBuilderPattern.append(SpamBlacklistGenerator.domainToRegexPattern(domains[0]));
        for (int i = 1; i <= domains.length - 1; i++) {
            stringBuilderPattern.append("|");
            stringBuilderPattern.append(SpamBlacklistGenerator.domainToRegexPattern(domains[i]));
        }
        System.out.println(stringBuilderPattern.toString());
        return Pattern.compile(stringBuilderPattern.toString());
    }

    public void generateBlacklist(ArrayList<String> indexPaths) {
        for (int i = 0; i < indexPaths.size(); i++){
            Directory idx;
            try {
                idx = FSDirectory.getDirectory(indexPaths.get(i), false);
                IndexReader reader = IndexReader.open(idx);

                PrintStream printStream = new PrintStream(new FileOutputStream(indexPaths.get(i) + "/spam_blacklist.txt"));

                for (int j = 0; j < reader.maxDoc(); j++) {
                    System.out.printf("Inspecting Spam in DocID: %d%n", j);
                    Document doc = reader.document(j);

                    try {
                        String domain = doc.getField("domain").stringValue();
                        Matcher matcher = this.regexPatternString.matcher(domain);

                        if (matcher.matches()) {
                            printStream.println(j);
                        }
                    }
                    catch (NullPointerException e){
                        System.out.printf("Error in DocId %d. Document does not have domain field!?!? %n",j);
                    }
                }
                printStream.close();
            } catch (IOException e) {
                System.out.printf("Something went wrong on %s. Error: %n%s", indexPaths.get(i), e);
            }
        }
    }

    public static String domainToRegexPattern(String domain){
        StringBuilder stringBuilder = new StringBuilder(domain);
        stringBuilder.reverse();

        ArrayList<Integer> dotIndexes = new ArrayList<Integer>();

        int index = stringBuilder.indexOf(".");
        while(index != -1){
            dotIndexes.add(index);
            stringBuilder.replace(index, index + 1, "\\.");

            index = stringBuilder.indexOf(".", index + 2 );
        }

        stringBuilder.append("(\\..*)?");
        return stringBuilder.toString();
    }

    public static void main(String[] args){
        if (args.length < 2){
            System.err.println("Usage: SpamBlacklistGenerator -in <path1> -in <path2>;");
        }
        else {
            SpamBlacklistGenerator blacklistGenerator = null;
            try {
                blacklistGenerator = new SpamBlacklistGenerator();
                ArrayList<String> indexPaths = new ArrayList<String>();

                for (int i = 0; i < args.length; i++){
                    if (args[i].equals("-in")){
                        indexPaths.add(args[++i]);
                    }
                }
                blacklistGenerator.generateBlacklist(indexPaths);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}