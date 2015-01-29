package webom.request;

import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webom.annotations.validation.Length;
import webom.annotations.validation.NotNull;
import webom.annotations.validation.Param;
import webom.annotations.validation.Range;
import webom.annotations.validation.Type;
import webom.session.Session;

import com.google.gson.Gson;

public abstract class POJOBuilder {
	private Logger logger = LoggerFactory.getLogger(POJOBuilder.class);

	private HashMap<String, ArrayList<String>> validation_messages = new HashMap<String, ArrayList<String>>();

	private void addMessage(String fieldName, String message, Object actualValue) {
		if (!validation_messages.containsKey(fieldName)) {
			validation_messages.put(fieldName, new ArrayList<String>());
		}
		ArrayList<String> fieldMessages = validation_messages.get(fieldName);
		fieldMessages.add(message + " , value: " + actualValue);
	}

	public void buildHTTP(Map<String, String> urlParams, Map<String, String[]> queryMap, Session session,
			InputStream body) {
		Class<?> cls = this.getClass();

		// Map to existing fields, therefore don't fail when there extra
		// parameters arrives

		Field[] fields = cls.getFields();

		for (Field field : fields) {
			Param param = field.getAnnotation(Param.class);
			if (param == null) {
				continue;
			} else if (param.type() == Type.JSON) {
				StringWriter str = new StringWriter();
				try {
					IOUtils.copy(body, str);
					Class<?> fieldCls = field.getType();
					Gson gson = new Gson();
					// logger.info(str.toString());
					Object obj = gson.fromJson(str.toString(), fieldCls);
					field.set(this, obj);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		for (Field field : fields) {
			Param param = field.getAnnotation(Param.class);
			if (param == null) {
				continue;
			}

			String name;
			if (param.name().equals("")) {
				name = field.getName();
			} else {
				name = param.name();
			}

			if (param.type() == Type.URL) {
				putURLParam(urlParams, name, field);
			} else if (param.type() == Type.SESSION) {
				putSession(session, name, field);
			} else if (param.type() == Type.GETPOST) {
				putGETPOSTParam(queryMap, name, field);
			} else if (param.type() == Type.ANY) {
				// The order should be preserved
				// Order:
				// 1- URL parameters
				// 2- Session parameters
				// 3- GET/POST parameters
				boolean couldPut = putURLParam(urlParams, name, field) || putSession(session, name, field)
						|| putGETPOSTParam(queryMap, name, field);
			}
		}
	}

	private void putJSONToField(JSONObject json, String name, Field field) {
		Class<?> cls = field.getClass();
		if (Collection.class.isAssignableFrom(cls)) {
			throw new IllegalArgumentException("Not implemented yet");
		} else {
			try {
				if (cls.equals(String.class)) {
					field.set(this, json.getString(name));
				} else if (cls.equals(Integer.class)) {
					field.set(this, json.getInt(name));
				} else if (cls.equals(Double.class)) {
					field.set(this, json.getDouble(name));
				} else if ( cls.equals(Boolean.class)){
					field.set(this, json.getBoolean(name));
				} else {
					throw new IllegalArgumentException("I just don't know how to put this class:" + cls.getName());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			// Singular field
		}
	}

	public void buildWebsocket(Map<String, String> urlParams, Map<String, List<String>> queryMap, Session session) {
		Class<?> cls = this.getClass();
		// Map to existing fields, therefore don't fail when there extra
		// parameters arrives
		Field[] fields = cls.getFields();

		for (Field field : fields) {
			Param param = field.getAnnotation(Param.class);
			if (param == null) {
				continue;
			}

			String name;
			if (param.name().equals("")) {
				name = field.getName();
			} else {
				name = param.name();
			}

			if (param.type() == Type.URL) {
				putURLParam(urlParams, name, field);
			} else if (param.type() == Type.SESSION) {
				putSession(session, name, field);
			} else if (param.type() == Type.GETPOST) {
				putGETPOSTParamList(queryMap, name, field);
			} else if (param.type() == Type.JSON) {
				logger.error("JSONBody putting not implemented yet");
			} else if (param.type() == Type.ANY) {
				// The order should be preserved
				// Order:
				// 1- URL parameters
				// 2- Session parameters
				// 3- GET/POST parameters
				// 4- JSONBody parameters
				boolean couldPut = putURLParam(urlParams, name, field) || putSession(session, name, field)
						|| putGETPOSTParamList(queryMap, name, field);
			}
		}
	}

	public HashMap<String, ArrayList<String>> getValidation_messages() {
		return validation_messages;
	}

	public boolean isValid() {
		validation_messages.clear();

		Class<?> cls = this.getClass();
		Field[] fields = cls.getFields();

		for (Field field : fields) {
			String fieldName = field.getName();
			Object obj = null;
			if (fieldName.equals("validation_messages")) {
				continue;
			}

			try {
				obj = field.get(this);
			} catch (IllegalAccessException ex) {
				ex.printStackTrace();
			}

			NotNull nn = field.getAnnotation(NotNull.class);
			if (nn != null) {
				if (obj == null) {
					addMessage(fieldName, nn.message(), obj);
				}
			}

			Range range = field.getAnnotation(Range.class);
			if (range != null) {
				// TODO: Check if it is a int, double, etc.. (number)
				try {
					double value = Double.parseDouble(obj.toString());
					if (range.max() < value) {
						addMessage(fieldName, "The value is larger than max value: " + range.max(), obj);
					} else if (range.min() > value) {
						addMessage(fieldName, "The value is smaller than min value: " + range.min(), obj);
					}
				} catch (Exception ex) {
					// Do not mind yet..
				}
			}

			Length length = field.getAnnotation(Length.class);
			if (length != null) {
				String str = obj.toString();
				if (str != null) {
					int value = str.length();
					if (length.max() < value) {
						addMessage(fieldName, "The value is larger than max value: " + range.max(), obj);
					} else if (length.min() > value) {
						addMessage(fieldName, "The value is smaller than min value: " + length.min(), obj);
					}
				} else {

				}
			}
		}

		return validation_messages.size() == 0;
	}

	private boolean putGETPOSTParam(Map<String, String[]> queryMap, String name, Field field) {
		String[] values = queryMap.get(name);
		if (values != null) {
			if (values.length == 1) {
				return putStringToField(field, values[0]);
			} else {
				// TODO: Handle later, get class check if list etc
				return false;
			}
		}
		return false;
	}

	private boolean putGETPOSTParamList(Map<String, List<String>> queryMap, String name, Field field) {
		List<String> values = queryMap.get(name);
		if (values != null) {
			if (values.size() == 1) {
				return putStringToField(field, values.get(0));
			} else {
				logger.warn("The array values in putGETPOST are not implemented!");
				// TODO: Handle later, get class check if list etc
				return false;
			}
		}
		return false;
	}

	private boolean putSession(Session session, String name, Field field) {
		// TODO: just to be safe, is it right choice
		if (session == null)
			return false;
		Object storedObj = session.get(name);
		if (storedObj == null) {
			return false;
		}
		try {
			field.set(this, storedObj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean putStringToField(Field field, String value) {
		try {
			Class<?> fieldCls = field.getType();
			if (fieldCls.equals(String.class)) {
				field.set(this, value);
			} else if (fieldCls.equals(Integer.class)) {
				field.set(this, Integer.parseInt(value));
			} else if (fieldCls.equals(Double.class)) {
				field.set(this, Double.parseDouble(value));
			} else if (fieldCls.equals(Long.class)) {
				field.set(this, Long.parseLong(value));
			} else if ( fieldCls.equals(Boolean.class)){
				field.set(this, Boolean.parseBoolean(value));
			} else {
				// TODO: all the types, or non parsable things Handle
				// later
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	private boolean putURLParam(Map<String, String> urlParams, String name, Field field) {
		String value = urlParams.get(name);
		if (value != null) {
			return putStringToField(field, value);
		}
		return false;
	}
}
