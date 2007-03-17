package org.jbpm.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;
import org.jbpm.db.JbpmSessionFactory;
import org.jbpm.jpdl.par.ProcessArchiveDeployer;

/**
 * ant task for deploying process archives.
 */
public class DeployParTask extends MatchingTask {

  private String cfg = null;
  private String properties = null;
  private String par = null;
  private List fileSets = new ArrayList();

  public void execute() throws BuildException {
    try {
      // get the JbpmSessionFactory
      JbpmSessionFactory jbpmSessionFactory = AntTaskJbpmSessionFactory.getJbpmSessionFactory(cfg,properties);
      
      // if attribute par is set, deploy that par file
      if (par!=null) {
        log( "deploying par "+par+" ..." );
        File file = new File(par);
        deploy(file, jbpmSessionFactory);
      }
      
      // loop over all files that are specified in the filesets
      Iterator iter = fileSets.iterator();
      while (iter.hasNext()) {
        FileSet fileSet = (FileSet) iter.next();
        DirectoryScanner dirScanner = fileSet.getDirectoryScanner(getProject());
        String[] fileSetFiles = dirScanner.getIncludedFiles();

        for (int i = 0; i < fileSetFiles.length; i++) {
          String fileName = fileSetFiles[i];
          File file = new File(fileName);
          if ( !file.isFile() ) {
            file = new File( dirScanner.getBasedir(), fileName );
          }

          // deploy the file, specified in a fileset element
          log( "deploying process archive "+file+" ..." );
          deploy(file, jbpmSessionFactory);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException( "couldn't deploy process archives : " + e.getMessage() );
    }
  }

  private void deploy(File file, JbpmSessionFactory jbpmSessionFactory) throws IOException, FileNotFoundException {
    ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
    ProcessArchiveDeployer.deployZipInputStream(zipInputStream,jbpmSessionFactory);
  }
  
  public void addFileset(FileSet fileSet) {
    this.fileSets.add(fileSet);
  }
  public void setCfg(String cfg) {
    this.cfg = cfg;
  }
  public void setProperties(String properties) {
    this.properties = properties;
  }
  public void setPar(String par) {
    this.par = par;
  }
}
