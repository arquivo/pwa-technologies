function encodeHtmlEntities(str) {
    str = str.replaceAll('ç','%26ccedil%3B')
             .replaceAll('Á','%26Aacute%3B')
             .replaceAll('á','%26aacute%3B')
             .replaceAll('À','%26Agrave%3B')
             .replaceAll('Â','%26Acirc%3B')
             .replaceAll('à','%26agrave%3B')
             .replaceAll('â','%26acirc%3B')
             .replaceAll('Ä','%26Auml%3B')
             .replaceAll('ä','%26auml%3B')
             .replaceAll('Ã','%26Atilde%3B')
             .replaceAll('ã','%26atilde%3B')
             .replaceAll('Å','%26Aring%3B')
             .replaceAll('å','%26aring%3B')
             .replaceAll('Æ','%26Aelig%3B')
             .replaceAll('æ','%26aelig%3B')
             .replaceAll('Ç','%26Ccedil%3B')
             .replaceAll('Ð','%26Eth%3B')
             .replaceAll('ð','%26eth%3B')
             .replaceAll('É','%26Eacute%3B')
             .replaceAll('é','%26eacute%3B')
             .replaceAll('È','%26Egrave%3B')
             .replaceAll('è','%26egrave%3B')
             .replaceAll('Ê','%26Ecirc%3B')
             .replaceAll('ê','%26ecirc%3B')
             .replaceAll('Ë','%26Euml%3B')
             .replaceAll('ë','%26euml%3B')
             .replaceAll('Í','%26Iacute%3B')
             .replaceAll('í','%26iacute%3B')
             .replaceAll('Ì','%26Igrave%3B')
             .replaceAll('ì','%26igrave%3B')
             .replaceAll('Î','%26Icirc%3B')
             .replaceAll('î','%26icirc%3B')
             .replaceAll('Ï','%26Iuml%3B')
             .replaceAll('ï','%26iuml%3B')
             .replaceAll('Ñ','%26Ntilde%3B')
             .replaceAll('ñ','%26ntilde%3B')
             .replaceAll('Ó','%26Oacute%3B')
             .replaceAll('ó','%26oacute%3B')
             .replaceAll('Ò','%26Ograve%3B')
             .replaceAll('ò','%26ograve%3B')
             .replaceAll('Ô','%26Ocirc%3B')
             .replaceAll('ô','%26ocirc%3B')
             .replaceAll('Ö','%26Ouml%3B')
             .replaceAll('ö','%26ouml%3B')
             .replaceAll('Õ','%26Otilde%3B')
             .replaceAll('õ','%26otilde%3B')
             .replaceAll('Ø','%26Oslash%3B')
             .replaceAll('ø','%26oslash%3B')
             .replaceAll('ß','%26szlig%3B')
             .replaceAll('Þ','%26Thorn%3B')
             .replaceAll('þ','%26thorn%3B')
             .replaceAll('Ú','%26Uacute%3B')
             .replaceAll('ú','%26uacute%3B')
             .replaceAll('Ù','%26Ugrave%3B')
             .replaceAll('ù','%26ugrave%3B')
             .replaceAll('Û','%26Ucirc%3B')
             .replaceAll('û','%26ucirc%3B')
             .replaceAll('Ü','%26Uuml%3B')
             .replaceAll('ü','%26uuml%3B')
             .replaceAll('Ý','%26Yacute%3B')
             .replaceAll('ý','%26yacute%3B')
             .replaceAll('ÿ','%26yuml%3B')
             .replaceAll('©','%26copy%3B')
             .replaceAll('®','%26reg%3B')
             .replaceAll('™','%26trade%3B')
             .replaceAll('&','%26amp%3B')
             .replaceAll('<','%26lt%3B')
             .replaceAll('>','%26gt%3B')
             .replaceAll('€','%26euro%3B')
             .replaceAll('¢','%26cent%3B')
             .replaceAll('£','%26pound%3B')
             .replaceAll('\"','%26quot%3B')
             .replaceAll('‘','%26lsquo%3B')
             .replaceAll('’','%26rsquo%3B')
             .replaceAll('“','%26ldquo%3B')
             .replaceAll('”','%26rdquo%3B')
             .replaceAll('«','%26laquo%3B')
             .replaceAll('»','%26raquo%3B')
             .replaceAll('—','%26mdash%3B')
             .replaceAll('–','%26ndash%3B')
             .replaceAll('°','%26deg%3B')
             .replaceAll('±','%26plusmn%3B')
             .replaceAll('¼','%26frac14%3B')
             .replaceAll('½','%26frac12%3B')
             .replaceAll('¾','%26frac34%3B')
             .replaceAll('×','%26times%3B')
             .replaceAll('÷','%26divide%3B')
             .replaceAll('α','%26alpha%3B')
             .replaceAll('β','%26beta%3B')
             .replaceAll('∞','%26infin%3B')
             .replaceAll(' ','+');
    return str;
}