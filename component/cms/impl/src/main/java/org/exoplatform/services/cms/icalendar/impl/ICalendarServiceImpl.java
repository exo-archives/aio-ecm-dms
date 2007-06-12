package org.exoplatform.services.cms.icalendar.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Categories;

import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.icalendar.ICalendarService;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * @author Hung Nguyen Quang
 * @mail   nguyenkequanghung@yahoo.com
 */

public class ICalendarServiceImpl implements ICalendarService{
  
  static private String JCR_CONTENT = "jcr:content".intern() ;
  static private String JCR_DATA = "jcr:data".intern() ;
  static private String JCR_MIMETYPE = "jcr:mimeType".intern() ;
  static private String JCR_LASTMODIFIED = "jcr:lastModified".intern() ;
  static private String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  static private String NT_FILE = "nt:file".intern() ;
  static private String NT_RESOURCE = "nt:resource".intern() ;  
  static private String MIX_VERSIONABLE = "mix:versionable".intern() ;
  
  private RepositoryService repositoryService_;
  private CmsConfigurationService cmsConfigService_ ;
  public ICalendarServiceImpl(CmsConfigurationService cmsConfigService, RepositoryService repositoryService) {
    cmsConfigService_ = cmsConfigService ;
    repositoryService_ = repositoryService ;
  }
  
  public void generateICalendar(Map context) throws Exception {
    String fileName = (String)context.get("exo:calendarPath") ;
    String queryPath = (String)context.get("exo:query") ;
    String srcWorkspace = (String) context.get("srcWorkspace") ;
    String repository = (String)context.get("repository") ;
    Session session = repositoryService_.getRepository(repository).getSystemSession(srcWorkspace);
    
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query query = queryManager.createQuery(queryPath, Query.XPATH);
    QueryResult queryResult = query.execute(); 
    NodeIterator iter = queryResult.getNodes() ;      
    Calendar calendar = new Calendar();
    calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
    calendar.getProperties().add(Version.VERSION_2_0);
    calendar.getProperties().add(CalScale.GREGORIAN);
    while (iter.hasNext()) {
      Node refEvent = iter.nextNode() ;
      long start = refEvent.getProperty("exo:dtstart").getDate().getTimeInMillis() ;
      long end = 0 ;
      if(refEvent.hasProperty("exo:dtend")){
        end = refEvent.getProperty("exo:dtend").getDate().getTimeInMillis() ;
      }
      String summary = refEvent.getProperty("exo:summary").getString() ;
      VEvent event ;
      if(end > 0) {
        event = new VEvent(new DateTime(start), new DateTime(end), summary);
        event.getProperties().getProperty(Property.DTEND).getParameters().add(Value.DATE_TIME);
      }else {
        event = new VEvent(new DateTime(start), summary);            
      }
      event.getProperties().getProperty(Property.DTSTART).getParameters().add(Value.DATE_TIME);      
      if(refEvent.hasProperty("exo:description")) {
        String desc = refEvent.getProperty("exo:description").getString() ;
        event.getProperties().add(new Description(desc));
      }
      
      String location = refEvent.getProperty("exo:location").getString() ;
      event.getProperties().add(new Location(location));
      
      String uuid = refEvent.getProperty("jcr:uuid").getString() ;
      Uid id = new Uid(uuid) ; 
      event.getProperties().add(id) ; 
      calendar.getComponents().add(event);
    }
    storeCalendar(calendar, fileName, repository) ;    
  }
  
  private void storeCalendar(Calendar calendar, String fileName, String repository){   
    try {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      CalendarOutputter output = new CalendarOutputter();
      output.output(calendar, bout) ;
      Session session = repositoryService_.getRepository(repository)
                        .getSystemSession(cmsConfigService_.getWorkspace());
      Node rootNode = session.getRootNode();
      String[] array = fileName.split("/") ;
      for(int i = 0; i < array.length - 1; i ++) {
        if(array[i] != null && array[i].trim().length() > 0) {
          if(rootNode.hasNode(array[i].trim())) {
            rootNode = rootNode.getNode(array[i].trim()) ;
          }else {
            rootNode.addNode(array[i].trim(),NT_UNSTRUCTURED) ;
            rootNode.save() ;
            rootNode = rootNode.getNode(array[i].trim()) ;
          }
        }
      }
      String name = array[array.length - 1] ;
      session.save() ;
      String mimeType = "text/calendar" ;
      Node rss = null;
      if(!rootNode.hasNode(name)){
        rss = rootNode.addNode(name, NT_FILE);
        Node contentNode = rss.addNode(JCR_CONTENT, NT_RESOURCE);
        contentNode.setProperty(JCR_DATA, new ByteArrayInputStream(bout.toByteArray()));
        contentNode.setProperty(JCR_MIMETYPE, mimeType);
        contentNode.setProperty(JCR_LASTMODIFIED, new GregorianCalendar());
        session.save(); 
      } else {
        rss = rootNode.getNode(name);
        boolean isEnabledVersion = false ;
        NodeType[] mixinTypes = rss.getMixinNodeTypes() ;
        for(int i = 0; i < mixinTypes.length; i ++) {
          if(mixinTypes[i].getName().equals(MIX_VERSIONABLE)) {
            isEnabledVersion = true ;
            break ;
          }
        }
        if(isEnabledVersion)  rss.checkout();           
        else  rss.addMixin(MIX_VERSIONABLE) ;                
        Node contentNode = rss.getNode(JCR_CONTENT);
        contentNode.setProperty(JCR_DATA, new ByteArrayInputStream(bout.toByteArray()));
        contentNode.setProperty(JCR_MIMETYPE, mimeType);
        contentNode.setProperty(JCR_LASTMODIFIED, new GregorianCalendar());
        rss.save() ;
        rss.checkin() ;        
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Object generateICalendar(String categoryName) throws Exception {
    Calendar calendar = new Calendar();
    try{
      Session session = repositoryService_.getRepository().getSystemSession(cmsConfigService_.getWorkspace());
      calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
      calendar.getProperties().add(Version.VERSION_2_0);
      calendar.getProperties().add(CalScale.GREGORIAN);
      
      Node calendarTaxonomy = (Node)session.getItem(cmsConfigService_.getJcrPath(BasePath.CALENDAR_CATEGORIES_PATH)) ;
      Node category = calendarTaxonomy.getNode(categoryName) ;
      boolean isReference = false ;
      PropertyIterator references = null;
      try{
        references = category.getReferences() ;
        isReference = true ;
      }catch(Exception e){
        isReference = false ;
      }
      if(isReference && references != null && references.getSize() > 0){
        while(references.hasNext()){
          javax.jcr.Property pro = references.nextProperty() ;
          Node refEvent = pro.getParent() ;
          long start = refEvent.getProperty("exo:dtstart").getDate().getTimeInMillis() ;
          long end = 0 ;
          if(refEvent.hasProperty("exo:dtend")){
            end = refEvent.getProperty("exo:dtend").getDate().getTimeInMillis() ;
          }
          String summary = refEvent.getProperty("exo:summary").getString() ;
          VEvent event ;
          if(end > 0) {
            event = new VEvent(new DateTime(start), new DateTime(end), summary);
            event.getProperties().getProperty(Property.DTEND).getParameters().add(Value.DATE_TIME);
          }else {
            event = new VEvent(new DateTime(start), summary);            
          }
          event.getProperties().getProperty(Property.DTSTART).getParameters().add(Value.DATE_TIME);      
          if(refEvent.hasProperty("exo:description")) {
            String desc = refEvent.getProperty("exo:description").getString() ;
            event.getProperties().add(new Description(desc));
          }
          
          String location = refEvent.getProperty("exo:location").getString() ;
          event.getProperties().add(new Location(location));
          
          String uuid = refEvent.getProperty("jcr:uuid").getString() ;
          Uid id = new Uid(uuid) ; 
          event.getProperties().add(id) ; 
          event.getProperties().add(new Categories(categoryName)) ;
          calendar.getComponents().add(event);          
        }
      }      
    }catch(Exception e) {
      e.printStackTrace() ;
    }    
    return calendar ;
  }
}
