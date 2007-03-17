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
