/**
 * Copyright 2018 mocomsys Inc. All Rights Reserved.
 */
package pep.per.mint.agent.util.parser.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
/**
 * <pre>
 * annotation
 * ParserDefineParser.java
 * </pre>
 * @author whoana
 * @date Nov 26, 2019
 */
public @interface ParserDefinition {
	
	int TYPE_K_V_PARSER  = 0;
	 
	public String name();
	
	public int type();
	
}
