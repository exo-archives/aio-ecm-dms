/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.jbpm.bytes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * is used by {@link org.jbpm.bytes.ByteArray} to chop a 
 * byte arrays into a list of chunks and glue them back together. 
 */
public abstract class ByteBlockChopper {

  private static final int BLOCKSIZE = 1024;
  
  public static List chopItUp(byte[] byteArray) {
    List bytes = null;
    if ( (byteArray!=null)
         && (byteArray.length>0) ){
      bytes = new ArrayList();
      int index = 0;
      while ( (byteArray.length-index) > BLOCKSIZE ) {
        byte[] byteBlock = new byte[BLOCKSIZE];
        System.arraycopy(byteArray, index, byteBlock, 0, BLOCKSIZE);
        bytes.add(byteBlock);
        index+=BLOCKSIZE;
      }
      byte[] byteBlock = new byte[byteArray.length-index];
      System.arraycopy(byteArray, index, byteBlock, 0, byteArray.length-index);
      bytes.add(byteBlock);
    }
    return bytes;
  }

  public static byte[] glueChopsBackTogether(List bytes) {
    byte[] value = null;
    
    if (bytes!=null) {
      Iterator iter = bytes.iterator();
      while (iter.hasNext()) {
        byte[] byteBlock = (byte[]) iter.next();
        if (value==null) {
          value = byteBlock;
        } else {
          byte[] oldValue = value;
          value = new byte[value.length+byteBlock.length];
          System.arraycopy(oldValue, 0, value, 0, oldValue.length);
          System.arraycopy(byteBlock, 0, value, oldValue.length, byteBlock.length);
        }
      }
    }
    
    return value;
  }
}
