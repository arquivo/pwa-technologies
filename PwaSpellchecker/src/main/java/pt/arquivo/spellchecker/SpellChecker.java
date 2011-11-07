package pt.arquivo.spellchecker;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;

import pt.arquivo.spellchecker.rules.*;
import pt.arquivo.spellchecker.rules.portuguese.AddPrefixRule;
import pt.arquivo.spellchecker.rules.portuguese.DiacriticsRule;
import pt.arquivo.spellchecker.rules.portuguese.MultipleSubstituteRule;
import pt.arquivo.spellchecker.rules.portuguese.SubstituteRule;


/**
 * SpellChecker based on Hunspell
 * @author Miguel Costa
 * @note Hunspell must be installed (http://hunspell.sourceforge.net/) and its dictionaries (http://wiki.services.openoffice.org/wiki/Dictionaries)
 */
public class SpellChecker  {
  
	
  private final static int MAX_LENGTH_DIFF=2;
	
  private final static NormalizingRule rulesPT[]={new DiacriticsRule(), new AddPrefixRule(), new MultipleSubstituteRule(), new SubstituteRule()}; // do not change this order
  private final static NormalizingRule rulesDefault[]={};
  
  private static Logger logger=Logger.getLogger(SpellChecker.class.getName());
	
	
  /**
   * Execute command in the operating system
   * @param comm command list
   * @param vars environment variables list or null if the subprocess should inherit the environment of the current process
   * @param dir working directory of the subprocess, or null if the subprocess should inherit the working directory of the current process
   * @param wait true if response should wait for the thread to end
   * @param debug true if response should be printed (wait should be true)
   * @throws IOException 
   * @throws InterruptedException 
   */
  private static Process execCommand(String comm[], String vars[], String dir, boolean wait, boolean debug) throws IOException, InterruptedException {
	  String initialComm[] = {"/bin/bash", "-c"};
	  String finalComm[]=new String[initialComm.length + comm.length];
	  System.arraycopy(initialComm, 0, finalComm, 0, initialComm.length);
	  System.arraycopy(comm, 0, finalComm, initialComm.length, comm.length);
	  
	  ProcessBuilder pb = new ProcessBuilder(finalComm);
	  Map<String, String> env = pb.environment();
	  String sarray[]=null;
	  if (vars!=null) {
		  for (int i=0;i<vars.length;i++) {
			  sarray=vars[i].split("=");
			  env.put(sarray[0], sarray[1]);
		  }
	  }
	  if (dir!=null) {
		  pb.directory(new File(dir));
	  }
	  Process p = pb.start();

	  if (debug) { // necessary for long processes to still running		
		  // stdout
		  BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));		 
		  String line=null;
		  while((line=input.readLine()) != null) {
			  logger.debug(line);			  
		  }
		  input.close();

		  // stderr
		  input = new BufferedReader(new InputStreamReader(p.getErrorStream()));		 			
		  while((line=input.readLine()) != null) {
			  logger.debug(line);
		  }
		  input.close();		
	  }
	  if (wait) {
		  p.waitFor();
	  }

	  return p;
  }

  public static String[] suggestSimilarAspell(String word, String lang, int numSug, IndexReader ir, String field, int minFreq, int timesFreq, String dictPath) throws IOException, InterruptedException {
	  String scomm = "echo "+word+" | /usr/bin/aspell -a -l "+lang+" --sug-mode=normal -W 2 --ignore-case=true"; // --sug-mode=normal is not so fast but evaluates until 2 chars distance; -W 2 ignores words with 2 or less characters; --sug-split-char ndo not split words
	  return suggestSimilarSpell(word, lang, numSug, ir, field, minFreq, timesFreq, dictPath, scomm);
  }
  
  public static String[] suggestSimilarHunspell(String word, String lang, int numSug, IndexReader ir, String field, int minFreq, int timesFreq, String dictPath) throws IOException, InterruptedException {
	  String scomm="echo "+word+" | /usr/local/bin/hunspell -a -d "+dictPath+"/"+lang;
	  return suggestSimilarSpell(word, lang, numSug, ir, field, minFreq, timesFreq, dictPath, scomm);
  }
  
  /**
   * Suggests words using Aspell or Hunspell  
   * @param word query term
   * @param lang dictionary language, "pt_PT" or "en_US"
   * @param numSug number of suggestions to return
   * @param ir index reader
   * @param field field in index to search
   * @param minFreq minimum frequency to not suggested a word
   * @param timesFreq number of times the frequency of the suggestion must be higher than the frequency of the query term
   * @param dictPath dictionary path 
   * @param scomm command to call spellchecker
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  private static String[] suggestSimilarSpell(String word, String lang, int numSug, IndexReader ir, String field, int minFreq, int timesFreq, String dictPath, String scomm) throws IOException, InterruptedException {
	  if (word.indexOf("-")!=-1) { // ignore words with hifens
		  return new String[] { };  
	  }	  
	  word=word.toLowerCase();
	  
	  int freqWord=-1; // frequency of the word	  
	  //String scomm = {"echo "+word+" | /usr/bin/aspell -a -l "+lang+" --sug-mode=normal -W 2 --ignore-case=true"}; // --sug-mode=normal is not so fast but evaluates until 2 chars distance; -W 2 ignores words with 2 or less characters; --sug-split-char ndo not split words
	  //String scomm="echo "+word+" | /usr/local/bin/hunspell -a -d "+dictPath+"/"+lang;
	  logger.debug("command: "+scomm);
	  String comm[] = {scomm};
	  
	  String vars[]={};
	  Process proc=execCommand(comm, vars, null, true, false);
	  String terms[]={ };
	  NormalizingRule rules[]=null;
	  if (lang.equals("pt_PT")) {
		  rules=rulesPT;
	  }
	  else {
		  rules=rulesDefault;
	  }	 
	  								
	  BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	  input.readLine(); // ignore first line	  
	  String line=input.readLine();
	  input.close();
	  logger.debug("suggestions: "+line);
	  // check	  
	  if (line==null) {
		  return new String[] { };
	  }
	  
	  String parts[]=line.split(":");
	  if (parts.length==2) { // check if it has suggestions
		  terms=parts[1].split(", ");
	  }	  
	  // check	  
	  if (terms==null) {
		  return new String[] { };
	  }	  

	  int counter=0;		  
	  Vector<String> suggestions=new Vector<String>();		
	  for (int r=0;r<rules.length+1 && counter<numSug;r++) { // all rules plus the heuristic	  	
		 for (int i=0;i<terms.length && counter<numSug;i++) {		
			  if (terms[i]==null) { // removed previously
				  continue;
			  }
			  							 
			  if (r==0) {
				  if (i==0) {
					  terms[i]=terms[i].substring(1);		
				  }
				  terms[i]=terms[i].toLowerCase();				  
			  }
		  
			  // if suggestion is equal to word
			  if (word.equals(terms[i])) {
				  suggestions.add(terms[i]);
				  counter++;
				  terms[i]=null;
				  continue;
			  }
			  
			  // the difference in length can be only MAX_LENGTH_DIFF			  
			  if (Math.abs(word.length()-terms[i].length())>MAX_LENGTH_DIFF) { 
				  terms[i]=null;
				  continue;
			  }
					  
			  // ignore terms splitted with sub-words. It gives good results in some cases and bad in others
			  String subTerms[]=terms[i].split(" |-");	
			  if (subTerms.length>1) {
				  terms[i]=null;
				  continue;
			  }
			  			  			  			 			  				 
			  if (r<rules.length) { 			  // test all normalizing rules				  				 
				  if (rules[r].normalizeByRule(word).equals(rules[r].normalizeByRule(terms[i]))) {					  					 					 
					  suggestions.add(terms[i]);
					  counter++;
					  terms[i]=null;					  
					  continue;
				  }
			  }				  				  			  
			  else { // if the suggestions are not one of the rules then use the other heuristics					  				  				  			
				  if (freqWord==-1) {
					  freqWord=ir.docFreq(new Term(field, word)); 
					  logger.debug("freqWord: "+freqWord+" minFreq: "+minFreq);
				  }
				  
				  if (freqWord>minFreq) { // ignore if the term is very used
					  return new String[] { };		
				  }
				  int freq=ir.docFreq(new Term(field, terms[i]));
				  if (freq<freqWord*timesFreq) { // ignore the suggestion if the frequency is not higher x times
					  logger.debug("freq: "+freq+" suggestion: "+terms[i]);
					  terms[i]=null;						  
					  continue;
				  }				  

				  suggestions.add(terms[i]);
				  terms[i]=null;
				  counter++;					  				  			 
			  }			  		  
		  }	  		  
	 }	
	  	  	  	 
	 String suggestionsArr[]=new String[suggestions.size()];
	 suggestions.toArray(suggestionsArr);
	 return suggestionsArr;	
  }  
}
