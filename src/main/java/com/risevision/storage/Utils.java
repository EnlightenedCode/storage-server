package com.risevision.storage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.lang.RandomStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class Utils {
	
	static public String wrapNull(String s) {
		
   		return s != null ? s : "";
	}
	
	static public Integer wrapNull(Integer i) {
		
   		return i != null ? i : 0;
	}
	
	static public Long wrapNull(Long l) {
		
   		return l != null ? l : 0;
	}
	
	static public Boolean wrapNull(Boolean b) {
		
   		return b != null ? b : false;
	}
	
	static public Date wrapNull(Date d) {
		
   		return d != null ? d : new Date(0);
	}
	
	static public GeoPoint wrapNull(GeoPoint gp) {
		
   		return gp != null ? gp : new GeoPoint(0, 0);
	}
	
	static public String dateToHMTime(Date date) {
		
    	if (date != null) {
    		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm"); 
    		return formatter.format(date);
    	} else {
    		return "";
    	}
	}
	
	static public Date timeStampToDate(String timeStamp) {
		
    	if (timeStamp != null) {
    		
    		try {

    			return new SimpleDateFormat("ddMMyyyyHHmmssSSS").parse(timeStamp);

    		} catch (Exception e) {

    			return null;
    		}
    	} else {
    		
    		return null;
    	}
	}
	
	static public Date csvDateToDate(String csvDate) {
		
    	if (csvDate != null) {
    		
    		try {

    			return new SimpleDateFormat("MM/dd/yyyy").parse(csvDate);

    		} catch (Exception e) {

    			return null;
    		}
    	} else {
    		
    		return null;
    	}
	}
	
	static public String dateToTimeStamp(Date date) {
		
    	if (date != null) {
    		SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS"); 
    		return formatter.format(date);
    	} else {
    		return "";
    	}
	}
	
	static public String dateToDateTag(Date date) {
		
    	if (date != null) {
    		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
    		return formatter.format(date);
    	} else {
    		return "";
    	}
	}
	
	static public String getDateTimeQuery(String fieldName, int days) {
		
		Calendar today = Calendar.getInstance(TimeZone.getTimeZone(Globals.HQ_TIMEZONE));
		today.add(Calendar.DAY_OF_MONTH, -days);
		
		Date dt = today.getTime();
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
    	
    	return "%20where%20" + fieldName + ">datetime%20'" + formatter.format(dt) + "%2000:00:00'";
	}
	
	public static String unquote(String s) {  

		if (s != null && ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))) {  

			s = s.substring(1, s.length() - 1);  
		}  
		return s;  
	}
	
	static public String bytes2String(byte[] bytes) {

		StringBuilder string = new StringBuilder();
		for (byte b: bytes) {
			String hexString = Integer.toHexString(0x00FF & b);
			string.append(hexString.length() == 1 ? "0" + hexString : hexString);
		}
		return string.toString();
	}
	
	static public String getSHA1Hash(String s) {
		
		String result = "";
		
		try {
			
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			byte[] digest = sha1.digest(s.getBytes());
			result = bytes2String(digest);
		
		} catch (Exception e) {

			result = "";
		}
		
		return result;
	} 
	
	static public Integer safeParseInt(String s, Integer defaultValue) {
		
		if (s == null || s.isEmpty())
			return defaultValue;
		
		try {
			int i = Integer.parseInt(s);
			return i;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	static public Integer safeParseInt(String s) {
		
		return safeParseInt(s, null);
	}
	
//	static public String listToCSV(List<String> list) {
//        
//		if (list == null) {
//			return "";
//		}
//		
//    	StringBuilder sb = new StringBuilder();
//    	for (String item : list)
//    	{
//    	    if (sb.length() > 0)
//    	    	sb.append(", ");
//    		sb.append(item);
//     	}
//    	
//    	return sb.toString();
//    }
//	
//	static public List<String> csvToList(String csv) {
//		
//		List<String> result = null; 
//		
//		if (csv != null && !csv.isEmpty()) {
//			
//			result = new ArrayList<String>();
//
//			String[] ss = csv.split(",");
//			for (int i = 0; i < ss.length; i++) {
//				if(!ss[i].isEmpty()) {
//					result.add(ss[i].trim());
//				}
//			}
//		} 
//		
//		return result;
//	}
		
	static public boolean listIntersect(List<String> list1, List<String> list2) {
        
		if (list1 == null || list2 == null || list1.isEmpty() || list2.isEmpty()) {
			return false;
		}
		
		for (String item : list1)
    	{
    	    if (list2.contains(item)) {
    	    	return true;
    	    }
     	}
    	
    	return false;
    }

//	static public Document loadXml(InputStream is) {
//		
//		Document ret = null;
//		DocumentBuilderFactory domFactory;
//		DocumentBuilder builder;
//
//		try {
//			domFactory = DocumentBuilderFactory.newInstance();
//			domFactory.setValidating(false);
//			domFactory.setNamespaceAware(false);
//			builder = domFactory.newDocumentBuilder();
//
//			ret = builder.parse(is);
//		}
//		catch (Exception ex) {
//			ret = null;
//		}
//		return ret;
//	}
//	
//	static public String xmlToString(Document d) throws TransformerFactoryConfigurationError, TransformerException {
//		StringWriter stw = new StringWriter(); 
//		Transformer serializer = TransformerFactory.newInstance().newTransformer(); 
//		serializer.transform(new DOMSource(d), new StreamResult(stw)); 
//		return stw.toString(); 
//	} 
	
	static public void logStackTrace(Exception e) {

		Logger log = Logger.getAnonymousLogger();
		Utils.logStackTrace(e, log);

	}
	
	static public void logStackTrace(Exception e, Logger log) {

		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		log.warning(sw.toString());

	}
		
	static public void logException(Exception e) {
		
		Logger log = Logger.getAnonymousLogger();
		log.warning("Error: " + e.toString() + ", " +  e.getMessage());
		Utils.logStackTrace(e, log);
		
	}

	static public Key getEntityKey(Key companyKey, String entityKind, String entityId) {

		return KeyFactory.createKey(companyKey, entityKind, entityId);
	}
	
	static public Key getEntityKey(String entityKind, String entityId) {

		return KeyFactory.createKey(entityKind, entityId);
	}
	
	static public String getGoogleEmail() {
		
		UserService userService = UserServiceFactory.getUserService();
		com.google.appengine.api.users.User googleUser = userService.getCurrentUser();
		
		return googleUser != null ? googleUser.getEmail() : Globals.UNKNOWN_USER;
		
	}
	
	static public void listToXML(List<String> items, Document d, String listNodeName, String itemNodeName, Element e) {

		if (items != null) {

			Element eltList = d.createElement(listNodeName); 

			for (int i = 0; i < items.size(); i++) {
				Element eltListItem = d.createElement(itemNodeName);  
				eltListItem.appendChild(d.createTextNode(items.get(i)));  
				eltList.appendChild(eltListItem);
			}

			e.appendChild(eltList);
		}
	}
	
//	static public void fieldToXML(String value, Document d, String itemNodeName, Element e) {
//		
//		Element eltField = d.createElement(itemNodeName);  
//		eltField.appendChild(d.createTextNode(wrapNull(value)));  
//		e.appendChild(eltField);
//	}
//	
//	static public void fieldToXML(Boolean value, Document d, String itemNodeName, Element e) {
//		
//		Element eltField = d.createElement(itemNodeName);  
//		eltField.appendChild(d.createTextNode(wrapNull(value) ? Global.TRUE: Global.FALSE));  
//		e.appendChild(eltField);
//	}
//	
//	static public void fieldToXML(Integer value, Document d, String itemNodeName, Element e) {
//		
//		Element eltField = d.createElement(itemNodeName);  
//		eltField.appendChild(d.createTextNode(Integer.toString(wrapNull(value))));  
//		e.appendChild(eltField);
//	}
//	
//	static public void fieldToXML(Date value, Document d, String itemNodeName, Element e) {
//		
//		Element eltField = d.createElement(itemNodeName);  
//		eltField.appendChild(d.createTextNode(Utils.dateToRfc822(value)));  
//		e.appendChild(eltField);
//	}
	
	static public String excludeDisplayId(String data, String displayId) {

		if (data == null || data.isEmpty() || !data.contains(displayId)) 
			return data;

		return data
				.replace("\n\r", "")
				.replace("\n", "")
				.replace("\"" + displayId + "\"", "")
				.replace(",,", ",")
				.replace(",]", "]")
				.replace("[,", "[");

	}
	
	static public String generateID12() {
    	
		return RandomStringUtils.random(12, "23456789ABCDEFGHJKMNPQRSTUVWXYZ");
    }
	
	static public String keyToString(Key key) {
		
		String result = "";
		
		if (key != null) {
		
			StringBuilder sb = new StringBuilder();
			
			sb.append(key.getKind());
			sb.append(":");
			sb.append(key.getName());
			
			Key parentKey = key.getParent();
			
			while (parentKey != null){
				
				sb.insert(0, ":");
				sb.insert(0, parentKey.getName());
				sb.insert(0, ":");
				sb.insert(0, parentKey.getKind());
				parentKey = parentKey.getParent();
			}				
		
			
			result = sb.toString();
		}
		
		return result;
		
	}
	
	static public Key stringToKey(String keyStr) {
		
		Key result = null;
		
		if (keyStr != null && !keyStr.isEmpty()) {
			
			
			String[] tmp = keyStr.split(":");
			
			int i = 0;
			
			while (i < tmp.length) {
				
				String kind = tmp[i];
				i++;
				String name = tmp[i];
				i++;
				
				result = result != null ? KeyFactory.createKey(result, kind, name) : KeyFactory.createKey(kind, name);
				
			}
		}
		
		return result;
		
	}

	public static String generateUUID(){
    long ts = System.currentTimeMillis();

    return ts + "-" + UUID.randomUUID().toString();
  }

  public static void changeValuesToLowerCase(List<String> values){
    if (values == null) {return;}
    for (int i = 0; i < values.size(); i++) {
      String lowerCased = values.get(i).toLowerCase();
      values.set(i,lowerCased);
    }
  }
  
  public static boolean isEmpty(String value) {
    return value == null || value.trim().equals("");
  }
  
  public static <T> boolean allItemsExist(List<T> items, List<T> master) {
    for(T item : items) {
      if(!master.contains(item)) {
        return false;
      }
    }
    
    return true;
  }
  
  public static Collection<String> addPrefix(Collection<String> values, String prefix) {
    Collection<String> newValues = new ArrayList<String>();
    
    for(String value : values) {
      newValues.add(prefix + value);
    }
    
    return newValues;
  }
  
  public static Collection<String> replaceString(Collection<String> values, String regex, String replacement) {
    Collection<String> newValues = new ArrayList<String>();
    
    for(String value : values) {
      newValues.add(value.replaceAll(regex, replacement));
    }
    
    return newValues;
  }
  
}
