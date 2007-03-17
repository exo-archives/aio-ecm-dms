package org.jbpm.db.hibernate;

import org.hibernate.cfg.*;

public class JbpmNamingStrategy implements NamingStrategy {

  public String classToTableName(String className) {
    className = className.substring(className.lastIndexOf('.')+1);
    return "JBPM_"+className.toUpperCase();
  }

  public String propertyToColumnName(String propertyName) {
    return propertyName.toUpperCase()+"_";
  }

  public String tableName(String tableName) {
    return "JBPM_"+tableName;
  }

  public String columnName(String columnName) {
    return columnName+"_";
  }

  public String propertyToTableName(String className, String propertyName) {
    return classToTableName(className)+"_"+propertyName.toUpperCase();
  }
}
