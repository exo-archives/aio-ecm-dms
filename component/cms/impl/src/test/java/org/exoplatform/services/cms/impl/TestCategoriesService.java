package org.exoplatform.services.cms.impl;

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.test.BasicTestCase;

public class TestCategoriesService extends BasicTestCase{
	private CategoriesService service_ ;
	
	public TestCategoriesService(String name) {
		super(name) ;
	}	
	
	public void setUp() throws Exception {
		PortalContainer manager = PortalContainer.getInstance() ;
		if(service_ == null) {
			service_ = (CategoriesService)manager.getComponentInstanceOfType(CategoriesService.class) ;			
		}		
	}
	
	public void tearDown() {} 
	
	public void testCategoriesService() throws Exception{		
	}
}
