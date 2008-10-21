var CoverFlow = function() {
	var Self = this;
	var DOM = eXo.core.DOMUtil;
	CoverFlow.prototype.portletId = null;
	
	CoverFlow.prototype.initEvent = function(portletId) {
		Self.portletId = portletId;
		var portlet = document.getElementById(portletId);
		var album = DOM.getChildrenByTagName(portlet, "xmp")[0];
		var iframe = DOM.getChildrenByTagName(portlet, "iframe")[0];
		var idoc = iframe.contentWindow.document;
		idoc.open()
		idoc.write(album.innerHTML);
		idoc.close();
		var workingArea = DOM.findAncestorByClass(portlet, "UIWorkingArea");
		iframe.style.width = iframe.offsetWidth - 5 + "px";
		iframe.style.height = workingArea.offsetHeight - 5 + "px";
	}
};

eXo.ecm.UICoverFlow = new CoverFlow();