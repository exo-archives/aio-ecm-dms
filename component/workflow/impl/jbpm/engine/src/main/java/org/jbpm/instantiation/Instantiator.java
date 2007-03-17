package org.jbpm.instantiation;

public interface Instantiator {

  Object instantiate(Class clazz, String configuration);
}
