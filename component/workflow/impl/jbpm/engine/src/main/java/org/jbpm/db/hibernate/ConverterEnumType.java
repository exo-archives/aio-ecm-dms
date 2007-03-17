package org.jbpm.db.hibernate;

import java.io.*;
import java.sql.*;

import org.hibernate.*;
import org.hibernate.usertype.*;
import org.jbpm.context.exe.*;

/**
 * is the hibernate UserType for storing converters as a char in the database.
 * The conversion can be found (and customized) in the file jbpm.converter.properties.
 */
public class ConverterEnumType implements UserType {

  static final int[] SQLTYPES = new int[]{Types.CHAR};

  public boolean equals(Object o1, Object o2) { return (o1==o2); }
  public int hashCode(Object o) throws HibernateException { return o.hashCode(); }
  public Object deepCopy(Object o) throws HibernateException { return o; }
  public boolean isMutable() { return false; }
  public Serializable disassemble(Object o) throws HibernateException { return (Serializable) o; }
  public Object assemble(Serializable s, Object o) throws HibernateException { return s; }
  public Object replace(Object original, Object target, Object owner) { return target; }
  public int[] sqlTypes() { return SQLTYPES; }
  public Class returnedClass() { return Converter.class; }

  public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws HibernateException, SQLException {
    String converterDatabaseId = resultSet.getString(names[0]);
    return Converters.getConverterByDatabaseId(converterDatabaseId);
  }

  public void nullSafeSet(PreparedStatement preparedStatement, Object value, int index) throws HibernateException, SQLException {
    String converterDatabaseId = Converters.getConverterId((Converter) value);
    preparedStatement.setString(index, converterDatabaseId);
  }
}
