/**
 * Copyright 2018 mocomsys Inc. All Rights Reserved.
 */
package pep.per.mint.agent.util.parser;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pep.per.mint.agent.util.parser.annotation.Check;
import pep.per.mint.agent.util.parser.annotation.GetKey;
import pep.per.mint.agent.util.parser.annotation.GetValue;
import pep.per.mint.agent.util.parser.annotation.Parse;
import pep.per.mint.agent.util.parser.annotation.ParserDefinition;
 

/**
 * <pre>
 * test
 * KeyValueParser.java
 * </pre>
 * @author whoana
 * @date Nov 26, 2019
 */
public class KeyValueParser {

	 
	List<Map<String,String>> list = new ArrayList<Map<String,String>>();
	Object definition;
	Method getKeyMethod;
	Method getValueMethod;
	Method checkMethod;
	Method parseMethod;
	
	/**
	 * @throws Exception 
	 * 
	 */
	public KeyValueParser(Object definition) throws Exception {
		this.definition = definition;
		Class clazz = definition.getClass();
		Annotation parserDefinitionAnnotation = clazz.getAnnotation(ParserDefinition.class);
		if(parserDefinitionAnnotation == null) throw new Exception("The definition object " + clazz.toString() + " does not have any ParserDefinition annotation.");
		Method[] methods = clazz.getDeclaredMethods();
		 
		
		for (Method method : methods) {
			Annotation getKeyAnnotation = method.getAnnotation(GetKey.class);
			if(getKeyAnnotation != null) {
				getKeyMethod = method;		
				//System.out.println(" method:" + method.toGenericString());
			}
			Annotation getValueAnnotation = method.getAnnotation(GetValue.class);
			if(getValueAnnotation != null) {
				getValueMethod = method;	
				//System.out.println(" method:" + method.toGenericString());
			}
			Annotation checkAnnotation = method.getAnnotation(Check.class);
			if(checkAnnotation != null) {
				checkMethod = method;	 
				//System.out.println(" method:" + method.toGenericString());
			}
			Annotation parseAnnotation = method.getAnnotation(Parse.class);
			if(parseAnnotation != null) {
				parseMethod = method;	 
				//System.out.println(" method:" + method.toGenericString());
			}
			if(getValueMethod != null && getKeyMethod != null && checkMethod != null) break;
		}
		
		if(getKeyMethod == null) throw new Exception("The definition object " + clazz.toString() + " does not have any getKeyMethod.");
		if(getValueMethod == null) throw new Exception("The definition object " + clazz.toString() + " does not have any getValueMethod.");
		if(checkMethod == null) throw new Exception("The definition object " + clazz.toString() + " does not have any checkMethod.");
	}
	
	/**
	 * 
	 * @param data
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public String getKey(String data) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object[] args = {data}; 
		return (String)getKeyMethod.invoke(definition, args);
	}
	
	/**
	 * 
	 * @param data
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public Map<String, String> getValue(String data) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object[] args = {data}; 
		return (Map<String, String>)getValueMethod.invoke(definition, args);
	}
	
	/**
	 * 
	 * @param data
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public void parse(String data) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String key = (String)getKey(data);
		Map<String, String> value = getValue(data);
		value.put("key", key);
		list.add(value);
	}
	
	/**
	 * 
	 * @return
	 */
	public List<Map<String,String>> getLogs() {
		return list;
	}

	/**
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public boolean check(String data) throws Exception {
		try {
			boolean ok = false;
			Object[] args = {data}; 
			Object res = checkMethod.invoke(definition, args);
			if(res == null) {
				ok =false;
			} else {
				ok = ((Boolean) res).booleanValue();
			}
			return ok;
		}catch(Throwable t) { 
			throw new Exception(t);
		}
	}
	 
	
}
