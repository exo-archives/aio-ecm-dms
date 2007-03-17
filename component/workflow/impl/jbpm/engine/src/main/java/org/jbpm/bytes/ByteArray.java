package org.jbpm.bytes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * is a persistable array of bytes.  While there is no generic way of storing blobs
 * that is supported by many databases, all databases are able to handle small chunks 
 * of bytes properly.  It is the responsibility of this class to chop the large byte 
 * array into small chunks of 1K (and combine the chunks again in the reverse way).  
 * Hibernate will persist the list of byte-chunks in the database.
 * 
 * ByteArray is used in process variableInstances and in the file module (that stores the 
 * non-parsed process archive files). 
 */
public class ByteArray implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  private long id = 0;
  protected String name = null;
  protected List byteBlocks = null;
  
  public ByteArray() {
  }

  public ByteArray(byte[] bytes) {
    this.byteBlocks = ByteBlockChopper.chopItUp(bytes);
  }

  public ByteArray(String name, byte[] bytes) {
    this(bytes);
    this.name = name;
  }

  public ByteArray(ByteArray other) {
    this.byteBlocks = new ArrayList(other.getByteBlocks());
    this.name = other.name;
  }

  public byte[] getBytes() {
    return ByteBlockChopper.glueChopsBackTogether(byteBlocks);
  }

  public long getId() {
    return id;
  }

  public boolean equals(Object o) {
    if (o==null) return false;
    if (! (o instanceof ByteArray)) return false;
    ByteArray other = (ByteArray) o;
    return Arrays.equals(ByteBlockChopper.glueChopsBackTogether(byteBlocks), ByteBlockChopper.glueChopsBackTogether(other.byteBlocks));
  }

  public int hashCode() {
    if (byteBlocks==null) return 0;
    return byteBlocks.hashCode();
  }

  public List getByteBlocks() {
    return byteBlocks;
  }
}
