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
                var resizableBlock = DOM.findFirstDescendantByClass(workingArea, "div", "UIResizableBlock");                
		iframe.style.width = "75%";
		iframe.style.height = resizableBlock.offsetHeight - 20 + "px";
		iframe.style.margin = "auto";
		portlet.insertBefore(iframe, album);
		var idoc = iframe.contentWindow.document;  
		  idoc.open();
		  idoc.write(album.value);
		  setTimeout(function() {idoc.close()}, 1000);
	};
	
	CoverFlow.prototype.errorCallbackImage = function(obj){
	  var img = eXo.core.DOMUtil.findNextElementByTagName(obj.parentNode,"image");
	  img.style.display = "block";
	  obj.style.display = "none";
	};

        CoverFlow.prototype.setHeight = function() {
		
		 Self.portletId = portletId;
		 var portlet = document.getElementById(portletId);
			var workingArea = DOM.findAncestorByClass(portlet, "UIWorkingArea");			 
			var sizeBarContainer = DOM.findFirstDescendantByClass(workingArea, "div", "UISideBarContainer");		
                        var sizeBar =  DOM.findFirstDescendantByClass(workingArea, "div", "UISideBar");
                        var resizableBlock = DOM.findFirstDescendantByClass(workingArea, "div", "UIResizableBlock");
                        var uiSelectContent = DOM.findFirstDescendantByClass(workingArea, "div", "UISelectContent");
                        sizeBarContainer.style.height = workingArea.offsetHeight -15 + 'px';
                        uiSelectContent.style.height = workingArea.offsetHeight -15 + 'px';
                        resizableBlock.style.height = workingArea.offsetHeight -25 + 'px';
                        sizeBarContainer.style.margin = "auto";
			sizeBar.style.height = workingArea.offsetHeight + 'px';	
                        
                       
                        								
       };
};

eXo.ecm.UICoverFlow = new CoverFlow();

window.onerror = function() {return false;}
