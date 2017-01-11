<%
/** Query String ***/
String queryString = request.getParameter("query");
final String QUERY_REGEX =  "(-?&quot;.*?&quot;|\\S+)+";
/*"(\".+?\"|\\S+)*";*/

StringBuilder and = new StringBuilder();
StringBuilder phrase = new StringBuilder();
StringBuilder not = new StringBuilder();
int hitsPerPage = request.getParameter("hitsPerPage") != null ? Integer.parseInt(request.getParameter("hitsPerPage")) : 10;
String format = null;
String imagesSize = null;
String site = "";
String sortType = request.getParameter("sort") != null ? request.getParameter("sort") : null;
boolean sortReverse = "true".equals(request.getParameter("reverse")) ? true : false;

if (queryString != null) {		
	Pattern regex = Pattern.compile(QUERY_REGEX);
	Matcher match = regex.matcher(queryString);
	
	while( match.find() ) {
		if (match.group(1) != null) {
			String parcel = match.group(1);

			if (parcel.charAt(0) == '-') {					//check for negation
				if (parcel.length() > 1 				//check if just "-" is present
					&& ( parcel.startsWith("-&quot;")		//check for phrase negation
					|| parcel.contains(":") ) )			//check for operator negation
				{
					continue;		
				} else {
					//It's a term negation
					if(not.length() != 0) {
						not.append(" ");
					}
					not.append( parcel.substring(1));
				}
			} else if ( parcel.contains(" ")) {					//check for phrase
				if (phrase.length() != 0) {
					phrase.append(" ");
				}
				parcel = parcel.replaceAll("&quot;", "");
				phrase.append(parcel);
			} else if (parcel.contains(":")) {				//check for option
				if (parcel.startsWith("site:")) {
					site = parcel.substring(parcel.indexOf(':')+1);
				} else if (parcel.startsWith("type:")) {
					format += parcel.substring(parcel.indexOf(':')+1) + " ";
				} else if (parcel.startsWith("size:")) {
					imagesSize += parcel.substring(parcel.indexOf(':')+1) + " ";
				}
				// TODO - handle
			} else {								//words
				if (and.length() != 0) {
					and.append(" ");
				}
				and.append(URLDecoder.decode(parcel));
			}
		}
	}
}	

/*** Start date ***/
String oldest_date = "01/01/1996";

String dateStartString = request.getParameter("dateStart") != null ? request.getParameter("dateStart") : oldest_date;

/*** End date ***/
String dateEndString;

if ( (dateEndString = request.getParameter("dateEnd")) == null) {
	Calendar dateEnd = new GregorianCalendar();
	dateEnd.set( Calendar.YEAR, dateEnd.get(Calendar.YEAR)-1 );
	dateEnd.set( Calendar.MONTH, 12-1 );
	dateEnd.set( Calendar.DAY_OF_MONTH, 1 );

	SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
	dateEndString = fmt.format(dateEnd.getTime());
}
%>
