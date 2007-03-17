package org.jbpm.instantiation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class FieldInstantiator implements Instantiator {

  public Object instantiate(Class clazz, String configuration) {

    // create a new instance with the default constructor
    Object newInstance = newInstance(clazz);

    if ( (configuration!=null)
         && (! "".equals(configuration))
       ) {
      // parse the bean configuration
      Element configurationElement = parseConfiguration(configuration);
      
      // loop over the configured properties
      Iterator iter = configurationElement.elements().iterator();
      while( iter.hasNext() ) {
        Element propertyElement = (Element) iter.next();
        String propertyName = propertyElement.getName();
        setPropertyValue(clazz, newInstance, propertyName, propertyElement);
      }
    }
    return newInstance;
  }
  
  protected void setPropertyValue(Class clazz, Object newInstance, String propertyName, Element propertyElement) {
    try {
      Field f = findField(clazz, propertyName);
      f.setAccessible(true);
      f.set(newInstance, getValue(f.getType(), propertyElement));
    } catch (Exception e) {
      log.error( "couldn't parse set field '"+propertyName+"' to value '"+propertyElement.asXML()+"'", e );
    }
  }

  private Field findField(Class clazz, String propertyName) throws NoSuchFieldException {
    Field f = null;
    if (clazz!=null) {
      try {
        f = clazz.getDeclaredField(propertyName);
      } catch (NoSuchFieldException e) {
        f = findField(clazz.getSuperclass(), propertyName);
      }
    }
    return f;
  }

  protected Element parseConfiguration(String configuration) {
    Element element = null;
    try {
      element = DocumentHelper.parseText( "<action>"+configuration+"</action>" ).getRootElement();
    } catch (DocumentException e) {
      log.error( "couldn't parse bean configuration : " + configuration, e );
      throw new RuntimeException(e);
    }
    return element;
  }

  protected Object newInstance(Class clazz) {
    Object newInstance = null;
    try {
      newInstance = clazz.newInstance();
    } catch (Exception e) {
      log.error( "couldn't instantiate type '" + clazz.getName() + "' with the default constructor" );
      throw new RuntimeException(e);
    }
    return newInstance;
  }

  public static Object getValue(Class type, Element propertyElement) {
    // parse the value
    Object value = null;
    try {
      
      if ( type == String.class ) {
        value = propertyElement.getText();
      } else if ( (type==Integer.class) || (type==int.class) ) {
        value = new Integer( propertyElement.getTextTrim() );
      } else if ( (type==Long.class) || (type==long.class) ) {
        value = new Long( propertyElement.getTextTrim() );
      } else if ( (type==Float.class ) || (type==float.class) ) {
        value = new Float( propertyElement.getTextTrim() );
      } else if ( (type==Double.class ) || (type==double.class) ) {
        value = new Double( propertyElement.getTextTrim() );
      } else if ( (type==Boolean.class ) || (type==boolean.class) ) {
        value = Boolean.valueOf( propertyElement.getTextTrim() );
      } else if ( (type==Character.class ) || (type==char.class) ) {
        value = new Character( propertyElement.getTextTrim().charAt(0) );
      } else if ( (type==Short.class ) || (type==short.class) ) {
        value = new Short( propertyElement.getTextTrim() );
      } else if ( (type==Byte.class ) || (type==byte.class) ) {
        value = new Byte( propertyElement.getTextTrim() );
      } else if (type.isAssignableFrom(List.class)) {
        value = getCollection(propertyElement, new ArrayList());
      } else if (type.isAssignableFrom(Set.class)) {
        value = getCollection(propertyElement, new HashSet());
      } else if (type.isAssignableFrom(Collection.class)) {
        value = getCollection(propertyElement, new ArrayList());
      } else if (type.isAssignableFrom(Map.class)) {
        value = getMap(propertyElement, new HashMap());
      } else if ( type==Element.class ) {
        value = propertyElement;
      } else {
        Constructor constructor = type.getConstructor(new Class[]{String.class});
        if ( (propertyElement.isTextOnly())
             && (constructor!=null) ) {
          value = constructor.newInstance(new Object[]{propertyElement.getTextTrim()});
        }
      }
    } catch (Exception e) {
      log.error("couldn't parse the bean property value '" + propertyElement.asXML() + "' to a '" + type.getName() + "'" );
      throw new RuntimeException( e );
    }
    return value;
  }

  private static Object getMap(Element mapElement, Map map) {
    Class keyClass = String.class;
    String keyType = mapElement.attributeValue("key-type");
    if (keyType!=null) {
      keyClass = ClassLoaderUtil.loadClass(keyType);
    }

    Class valueClass = String.class;
    String valueType = mapElement.attributeValue("value-type");
    if (valueType!=null) {
      valueClass = ClassLoaderUtil.loadClass(valueType);
    }

    Iterator iter = mapElement.elementIterator();
    while (iter.hasNext()) {
      Element element = (Element) iter.next();
      Element keyElement = element.element("key");
      Element valueElement = element.element("value");
      
      map.put(getValue(keyClass, keyElement), getValue(valueClass, valueElement));
    }
    return map;
  }

  private static Object getCollection(Element collectionElement, Collection collection) {
    Class elementClass = String.class;
    String elementType = collectionElement.attributeValue("element-type");
    if (elementType!=null) {
      elementClass = ClassLoaderUtil.loadClass(elementType);
    }
    Iterator iter = collectionElement.elementIterator();
    while (iter.hasNext()) {
      Element element = (Element) iter.next();
      collection.add(getValue(elementClass, element));
    }
    return collection;
  }

  private static final Log log = LogFactory.getLog(FieldInstantiator.class);
}
