var CoverFlow = function() {
	var Self = this;
	var DOM = eXo.core.DOMUtil;
	CoverFlow.prototype.portletId = null;
	
	CoverFlow.prototype.initEvent = function(portletId) {
		Self.portletId = portletId;
		var portlet = document.getElementById(portletId);
		var album = DOM.getChildrenByTagName(portlet, "textarea")[0];
		var iframe = document.createElement("iframe");
		iframe.setAttribute("frameborder", "0");
		portlet.insertBefore(iframe, album);
		if (iframe.contentDocument) var idoc = iframe.contentDocument;  
 		else if (iframe.contentWindow) var idoc = iframe.contentWindow.document;  
   	else if (iframe.document) var idoc = iframe.document; 
		idoc.open();
		idoc.write(album.value);
		idoc.close();
		var workingArea = DOM.findAncestorByClass(portlet, "UIWorkingArea");
		iframe.style.width = "100%";
		iframe.style.height = workingArea.offsetHeight - 5 + "px";
	};
};

eXo.ecm.UICoverFlow = new CoverFlow();