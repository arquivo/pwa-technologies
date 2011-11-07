package pt.arquivo.spellchecker.rules.portuguese;

import pt.arquivo.spellchecker.rules.NormalizingRule;

import org.apache.log4j.Logger;

/**
 * Add prefix rule
 * @author Miguel Costa
 */
public class AddPrefixRule implements NormalizingRule {
	
	private static Logger logger = null;

	public AddPrefixRule() {
		logger = Logger.getLogger(AddPrefixRule.class.getName());
	}

	public String normalizeByRule(String word) {
		String normalized = null;

		if (word.charAt(0)=='h') {
			normalized = word;
		} else {
			normalized = 'h' + word;
		}

		logger.debug("Word being evaluated:\t"+ word);
		logger.debug("Word normalized:\t"+ normalized);

		return normalized;
	}
}

