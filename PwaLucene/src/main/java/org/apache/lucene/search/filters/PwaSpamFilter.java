package org.apache.lucene.search.filters;

import org.apache.lucene.search.PwaSearchableCommon;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PwaSpamFilter extends PwaFilter {

    private IndexReader indexReader;
    private static String spamRegexPattern = "tp\\.ue\\.s3pm-e\\..*|tp\\.xlo\\..*|ue\\.sknil-ni\\..*|tp\\.moc\\.dekoob\\..*|tp\\.niwer\\..*|tp\\.sserpxeasac\\..*|tp\\.moc\\.nopoissalc\\..*|tp\\.moc\\.sdacilc\\..*|tp\\.tsilsgiarc\\..*|tp\\.moc\\.sodacifissalce\\..*|tp\\.moc\\.sosive\\..*|tp\\.sodacifissalci\\..*|ue\\.sknil-ni\\..*|tp\\.tnaigteni\\..*|tp\\.moc\\.ofnisiofni\\..*|tp\\.azadak\\..*|moc\\.adivalam\\..*|ue\\.redartxam\\..*|tp\\.sorracten\\..*|tp\\.moc\\.lacoloxen\\..*|tp\\.moc\\.sdaeerfgp\\..*|ue\\.sboj-ecarp\\..*|tp\\.moc\\.licafracilbup\\..*|ue\\.htiw-erahs\\..*|tp\\.odnals\\..*|tp\\.eert\\..*|tp\\.moc\\.ulkut\\..*|tp\\.moc\\.kubeez\\..*";

    public boolean next() throws IOException {
        while (searchable.next()) {
            IndexReader indexReader;
            Document document = this.indexReader.document(searchable.doc());
            String domain = document.getField("domain").stringValue();

            Pattern pattern = Pattern.compile(this.spamRegexPattern);
            Matcher matcher = pattern.matcher(domain);

            if (!matcher.matches()) {
                return true;
            }
        }


        return false;
    }

    public int doc() {
        return searchable.doc();
    }



    public PwaSpamFilter(IndexReader indexReader){
        this(null, indexReader);
    }

    public PwaSpamFilter(PwaSearchableCommon searchable, IndexReader indexReader) {
        super(searchable);
        this.indexReader = indexReader;
    }


    public boolean hasDoc() {
        return searchable.hasDoc();
    }
}
