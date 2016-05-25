package pt.arquivo.spellchecker.rules.portuguese;

import pt.arquivo.spellchecker.rules.NormalizingRule;

import org.apache.log4j.Logger;

/**
 * Substitute probable mistakes of one char difference
 * @author Miguel Costa
 */
public class SubstituteRule implements NormalizingRule {

	private static Logger logger = null;

        public SubstituteRule() {
                logger = Logger.getLogger(SubstituteRule.class.getName());
        }

	public String normalizeByRule(String word) {
		String normalized = null;

		char cWordWithAccents[]=word.toCharArray();
		for (int i=0;i<cWordWithAccents.length;i++) {

			switch(cWordWithAccents[i]) {
				case 0x87:					// á
				case 0x88:					// à
				case 0x89:					// â
				case 0x8B: cWordWithAccents[i]='a'; break;	// ã
				
				case 0x8E: 					// é
				case 0x8F:					// è
				case 0x90: cWordWithAccents[i]='e'; break;	// ê

				case 0x69: cWordWithAccents[i]='e'; break;	// i to e
				case 0x92: cWordWithAccents[i]='i'; break;	// í

				case 0x97:					// ó
				case 0x99:					// ô
				case 0x9B: cWordWithAccents[i]='o'; break;	// õ

				case 0x75: cWordWithAccents[i]='o'; break;	// u to o
				case 0x9C: cWordWithAccents[i]='u'; break;	// ú		

				case 0x7A: cWordWithAccents[i]='s'; break;	// z to s								

				case 0x6E: cWordWithAccents[i]='m'; break;	// n to m
			//case 'x': cWordWithAccents[i]='s'; break;
			//case 'ç': cWordWithAccents[i]='s'; break;
			//case 'j': cWordWithAccents[i]='g'; break;		
			}
		}
		normalized = new String(cWordWithAccents);

		logger.debug("Word being evaluated:\t"+ word);
		logger.debug("Word normalized:\t"+ normalized);

		return new String(cWordWithAccents);
	}
}
