var CoverFlow = function() {
	var Self = this;
	var DOM = eXo.core.DOMUtil;
	var Browser = eXo.core.Browser;
	CoverFlow.prototype.portletId = null;
	
	CoverFlow.prototype.initEvent = function(portletId) {
		Self.portletId = portletId;
		var portlet = document.getElementById(portletId);
		var album = DOM.getChildrenByTagName(portlet, "textarea")[0];
		var iframe = document.createElement("iframe");
		iframe.setAttribute("frameborder", "0");
		iframe.setAttribute("border", "0");
		iframe.style.border = "none";
		var workingArea = DOM.findAncestorByClass(portlet, "UIWorkingArea");
		iframe.style.width = "98%";
		iframe.style.height = workingArea.offsetHeight - 5 + "px";
		iframe.style.margin = "auto";
		portlet.insertBefore(iframe, album);
		var idoc = iframe.contentWindow.document;  
		  idoc.open();
		  idoc.write(album.value);
		  setTimeout(function() {idoc.close()}, 1000);
	};
};

eXo.ecm.UICoverFlow = new CoverFlow();

window.onerror = function() {return false;}