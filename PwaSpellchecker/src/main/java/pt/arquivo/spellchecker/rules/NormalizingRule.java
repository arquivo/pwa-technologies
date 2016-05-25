package pt.arquivo.spellchecker.rules;

/**
 * Normalizing rules interface
 * @author Miguel Costa
 */
public interface NormalizingRule {

	public String normalizeByRule(String word);	
	
}
