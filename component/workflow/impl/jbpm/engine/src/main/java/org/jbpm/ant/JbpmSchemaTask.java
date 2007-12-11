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
package org.jbpm.ant;

import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.jbpm.db.JbpmSchema;

public class JbpmSchemaTask extends Task {

  private String cfg = null;
  private String properties = null;

  private boolean quiet = false;
  private boolean text = false;
  private String output = null;
  private String delimiter = null;

  private String actions = null;

  public void execute() throws BuildException {
    if (actions==null) throw new RuntimeException("actions is null in jbpmschema task");
    
    Configuration configuration = AntTaskJbpmSessionFactory.getConfiguration(cfg, properties);
    JbpmSchema jbpmSchema = new JbpmSchema(configuration);

    SchemaExport schemaExport = new SchemaExport(configuration);
    if (output!=null) schemaExport.setOutputFile(output);
    if (delimiter!=null) schemaExport.setDelimiter(delimiter);

    StringTokenizer tokenizer = new StringTokenizer(actions, ",");
    while (tokenizer.hasMoreTokens()) {
      String action = tokenizer.nextToken();

      if ("drop".equalsIgnoreCase(action)) {
        schemaExport.drop(!quiet, !text);

      } else if ("create".equalsIgnoreCase(action)) {
        schemaExport.create(!quiet, !text);
        
      } else if ("clean".equalsIgnoreCase(action)) {
        jbpmSchema.cleanSchema();
      }
    }
  }
  
  public void setActions(String actions) {
    this.actions = actions;
  }
  public void setCfg(String cfg) {
    this.cfg = cfg;
  }
  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }
  public void setOutput(String output) {
    this.output = output;
  }
  public void setProperties(String properties) {
    this.properties = properties;
  }
  public void setQuiet(boolean quiet) {
    this.quiet = quiet;
  }
  public void setText(boolean text) {
    this.text = text;
  }
}
