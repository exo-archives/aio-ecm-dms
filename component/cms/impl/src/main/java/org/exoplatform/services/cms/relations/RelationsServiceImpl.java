package org.exoplatform.services.cms.relations;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.picocontainer.Startable;

/**
 * @author monica franceschini
 */

public class RelationsServiceImpl implements RelationsService, Startable {
	private static final String RELATION_MIXIN = "exo:relationable";
	private static final String RELATION_PROP = "exo:relation";

	private RepositoryService repositoryService_;

	private CmsConfigurationService cmsConfig_;

	public RelationsServiceImpl(RepositoryService repositoryService,
			CmsConfigurationService cmsConfig) {
		repositoryService_ = repositoryService;
		cmsConfig_ = cmsConfig;
	}

	public boolean hasRelations(Node node) throws Exception {
		if (node.isNodeType(RELATION_MIXIN))
			return true;
		return false;

	}

	public List<Node> getRelations(Node node) {
		List<Node> rels = new ArrayList<Node>();
		try {
			Session session = getSystemSession();
			Property relations = null;
			try {
				relations = node.getProperty(RELATION_PROP);
			} catch (Exception ex) {
				// ex.printStackTrace();
			}
			if (relations != null) {
				Value[] values = relations.getValues();
				for (int i = 0; i < values.length; i++) {
					rels.add(session.getNodeByUUID(values[i].getString()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rels;
	}

	public void removeRelation(Node node, String relationPath) throws Exception {
		List<Value> vals = new ArrayList<Value>();
		if (!"*".equals(relationPath)) {
			Session session = getSystemSession();
			Property relations = node.getProperty(RELATION_PROP);
			if (relations != null) {
				Value[] values = relations.getValues();
				String uuid2Remove = null;
				for (int i = 0; i < values.length; i++) {
					String uuid = values[i].getString();
					Node refNode = session.getNodeByUUID(uuid);
					if (refNode.getPath().equals(relationPath)) {
						uuid2Remove = uuid;
					} else {
						vals.add(values[i]);
					}
				}
				if (uuid2Remove == null)
					return;
			}

		}
		node.setProperty(RELATION_PROP, vals.toArray(new Value[vals.size()]));
	}

	public void addRelation(Node node, String relationPath) throws Exception {
		Session session = getSystemSession();
    Session userSession = node.getSession() ;
		Node catNode = (Node) session.getItem(relationPath);    
    if(!catNode.isNodeType("mix:referenceable")) {
      catNode.addMixin("mix:referenceable") ;
      catNode.save() ;
    }      
		Value value2add = userSession.getValueFactory().createValue(catNode); 
		if (!node.isNodeType(RELATION_MIXIN)) {
      node.addMixin(RELATION_MIXIN);    
			node.setProperty(RELATION_PROP, new Value[] {value2add});
		} else {
			List<Value> vals = new ArrayList<Value>();
			Value[] values = node.getProperty(RELATION_PROP).getValues();
			for (int i = 0; i < values.length; i++) {
				Value value = values[i];
				String uuid = value.getString();
				Node refNode = session.getNodeByUUID(uuid);
				if(refNode.getPath().equals(relationPath))
					return;
				vals.add(value);
			}
			vals.add(value2add);
			node.setProperty(RELATION_PROP, vals.toArray(new Value[vals.size()]));
      userSession.save() ;
      userSession.refresh(true) ;
		}
	}

	public void addRelation(Node node, String relationPath, boolean replaceAll)
			throws Exception {
		if (replaceAll) {
			removeRelation(node, "*");
		}
		addRelation(node, relationPath);
	}

	public void start() {
		Session session = null;
		Node relationsHome = null;
		String relationPath = "";
		try {
			relationPath = cmsConfig_.getJcrPath(BasePath.CMS_PUBLICATIONS_PATH);
			session = getSystemSession();
			relationsHome = (Node) session.getItem(relationPath);
			for (NodeIterator iterator = relationsHome.getNodes(); iterator.hasNext();) {
				Node rel = iterator.nextNode();
				rel.addMixin("mix:referenceable");
			}
			relationsHome.save();
		} catch (Exception e) {
			// e.printStackTrace() ;
		}
	}

	public void stop() {
	}

	protected Session getSystemSession() throws Exception {
		ManageableRepository jcrRepository = repositoryService_.getRepository();
		return jcrRepository.getSystemSession(cmsConfig_.getWorkspace());
	}
}
