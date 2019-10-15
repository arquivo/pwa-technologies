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
    private String regexPatternString;

    public SpamBlacklistGenerator() throws IOException {
        InputStream inputStream = SpamBlacklistGenerator.class.getClassLoader().getResourceAsStream("config.properties");

        Properties prop = new Properties();
        prop.load(inputStream);
        this.regexPatternString = prop.getProperty("spam.regex.pattern");
    }

    public static void main(String[] args){
        if (args.length < 2){
            System.err.println("Usage: SpamBlacklistGenerator -in <path1> -in <path2>;");
        }

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

    public void generateBlacklist(ArrayList<String> indexPaths) {

        Pattern pattern = Pattern.compile(this.regexPatternString);

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
                        Matcher matcher = pattern.matcher(domain);

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
}