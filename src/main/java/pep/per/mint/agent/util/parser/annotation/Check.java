/**
 * Copyright 2018 mocomsys Inc. All Rights Reserved.
 */
package pep.per.mint.agent.util.parser.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
/**
 * <pre>
 * annotation
 * Check.java
 * </pre>
 * @author whoana
 * @date Nov 26, 2019
 */
public @interface Check {

}
