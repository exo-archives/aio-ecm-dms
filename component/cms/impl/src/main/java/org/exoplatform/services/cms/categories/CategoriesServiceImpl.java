package org.exoplatform.services.cms.categories;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.Workspace;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.picocontainer.Startable;

public class CategoriesServiceImpl implements CategoriesService, Startable {		
	private static final String CATEGORY_MIXIN = "exo:categorized";
	private static final String CATEGORY_PROP = "exo:category";
	private static final String COPY = "copy";
	private static final String CUT = "cut"; 
	
	private RepositoryService repositoryService_;
	private CmsConfigurationService cmsConfig_;	
	List<TaxonomyPlugin> plugins_ = new ArrayList<TaxonomyPlugin>() ;
	
	public CategoriesServiceImpl(RepositoryService repositoryService,
			CmsConfigurationService cmsConfig) throws Exception{  
		repositoryService_ = repositoryService;
		cmsConfig_ = cmsConfig;
		        
	}
	
  public void start() {   
    try {
      for(TaxonomyPlugin plugin : plugins_) {
        plugin.init() ;
      }
    } catch (Exception e) {
      e.printStackTrace() ;
    }
  } 
  
  public void stop() { }
  
  public void init(String repository) throws Exception {
    for(TaxonomyPlugin plugin : plugins_) {
      plugin.init(repository) ;
    }
  }
  
	public void addTaxonomyPlugin(ComponentPlugin plugin) {
		if(plugin instanceof TaxonomyPlugin) {			
			plugins_.add((TaxonomyPlugin)plugin) ;
		}
	}
	
	public Node getTaxonomyHomeNode (String repository) throws Exception {    
    Session session = getSession(repository) ;    
		Node homeTaxonomy = (Node)session.getItem(cmsConfig_.getJcrPath(BasePath.EXO_TAXONOMIES_PATH)) ;
		return homeTaxonomy ;
	}	
	
	public Node getTaxonomyNode(String realPath, String repository) throws Exception {
    Session adminSession = getSession(repository) ;
		return (Node)adminSession.getItem(realPath) ;
	}	
	
	public void addTaxonomy(String parentPath,String childName, String repository) throws Exception  {
    Session adminSession = getSession(repository) ;
		Node parent = (Node)adminSession.getItem(parentPath) ;
		if(parent.hasNode(childName)) {
			throw (new ItemExistsException()) ;
		}		
		Node taxonomyNode = parent.addNode(childName,"exo:taxonomy") ;
		if(taxonomyNode.canAddMixin("mix:referenceable")) {
			taxonomyNode.addMixin("mix:referenceable") ;
		}					
		adminSession.save() ;		
	}
	
	public void removeTaxonomyNode(String realPath, String repository) throws Exception {
    Session adminSession = getSession(repository) ;    
		Node selectedNode = (Node)adminSession.getItem(realPath) ;
		selectedNode.remove() ;
    adminSession.save();	
    adminSession.refresh(false) ;
	}						
	
	public void moveTaxonomyNode(String srcPath, String destPath, String type, String repository) throws Exception { 
    Session session = getSession(repository) ;    		   
		if(CUT.equals(type)) {			
      session.move(srcPath,destPath) ;
      session.save() ;
      session.refresh(true) ;            						
		}
		else if(COPY.equals(type)) {		
      Workspace workspace = session.getWorkspace() ;       
			workspace.copy(srcPath,destPath) ;
      session.save() ;
      session.refresh(true) ;
		}
		else {
			throw( new UnsupportedRepositoryOperationException()) ;		
		}									
	}	
	
	public boolean hasCategories(Node node) throws Exception {
		if (node.isNodeType(CATEGORY_MIXIN))
			return true;
		return false;
	}
	
	public List<Node> getCategories(Node node, String repository) throws Exception {
		List<Node> cats = new ArrayList<Node>();
    Session systemSession = getSession(repository) ;
		try {			
      javax.jcr.Property categories = node.getProperty("exo:category");
			Value[] values = categories.getValues();
			for (int i = 0; i < values.length; i++) {				
				cats.add(systemSession.getNodeByUUID(values[i].getString()));
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return cats;
	}
	
	public void removeCategory(Node node, String categoryPath, String repository) throws Exception {
    Session systemSession = getSession(repository) ;
		List<Value> vals = new ArrayList<Value>();
		if (!"*".equals(categoryPath)) {						
      javax.jcr.Property categories = node.getProperty("exo:category");
			Value[] values = categories.getValues();
			String uuid2Remove = null;
			for (int i = 0; i < values.length; i++) {
				String uuid = values[i].getString();
				Node refNode = systemSession.getNodeByUUID(uuid);
				if(refNode.getPath().equals(categoryPath)) {
					uuid2Remove = uuid;              
				} else {
					vals.add(values[i]);
				}
			}
			if(uuid2Remove == null)
				return;
		}
		node.setProperty(CATEGORY_PROP, vals.toArray(new Value[vals.size()]));
    node.getSession().save() ;
	}
	
	public void addCategory(Node node, String categoryPath, String repository) throws Exception {    
    Session systemSession = getSession(repository) ;
		Node catNode = (Node) systemSession.getItem(categoryPath);
		Session currentSession = node.getSession() ;		
		Value value2add = currentSession.getValueFactory().createValue(catNode);   		
		if (!node.isNodeType(CATEGORY_MIXIN)) {			
			node.addMixin(CATEGORY_MIXIN);    
			node.setProperty(CATEGORY_PROP, new Value[] {value2add});
		} else {
			List<Value> vals = new ArrayList<Value>();
			Value[] values = node.getProperty(CATEGORY_PROP).getValues();
			for (int i = 0; i < values.length; i++) {
				Value value = values[i];
				String uuid = value.getString();
				Node refNode = systemSession.getNodeByUUID(uuid);				
				if(refNode.getPath().equals(categoryPath))
					return;
				vals.add(value);
			}
			vals.add(value2add);
			node.setProperty(CATEGORY_PROP, vals.toArray(new Value[vals.size()]));
		}			
	}
	
	public void addCategory(Node node, String categoryPath, boolean replaceAll, String repository) throws Exception {
		if (replaceAll) {
			removeCategory(node, "*", repository) ;
		}
		addCategory(node, categoryPath, repository) ;
	}    
	
	protected Session getSession(String repository) throws Exception {
    return repositoryService_.getRepository(repository).getSystemSession(cmsConfig_.getWorkspace(repository)) ;
//    try {
//      //return repositoryService_.getRepository(repository).getSystemSession(cmsConfig_.getWorkspace(repository)) ;
//      return repositoryService_.getRepository(repository).login(cmsConfig_.getWorkspace(repository)) ;
//    }catch(Exception e) {
//      return repositoryService_.getRepository(repository).getSystemSession(cmsConfig_.getWorkspace(repository)) ;
//    }
  }
}
