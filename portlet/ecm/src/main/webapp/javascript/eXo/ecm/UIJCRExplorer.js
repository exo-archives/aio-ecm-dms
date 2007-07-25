
function UIJCRExplorer() {
	this.scrollMgr = null;
	this.ntScrollMgr = null;
};

UIJCRExplorer.prototype.addCallBackToLink = function(elementId) {
	if (elementId) {
		var rootElement = document.getElementById(elementId);
		var links = rootElement.getElementsByTagName("a");
		for (var i=0; i<links.length; i++) {
			var link = links[i];
			var url = link.href;
			if (url && url.indexOf('op=View') != -1 && url.indexOf('loadScroll') == -1) {
				url = url.substr(0, url.length-1).concat(", eXo.ecm.UIJCRExplorer.loadScroll)");
	  		link.href = url;
			}
		}
	}
};

UIJCRExplorer.prototype.addCallBackToNodeTypeLink = function() {
	var rootElement = document.getElementById("UIActionBar");
	if (rootElement) {
		var links = rootElement.getElementsByTagName("a");
		for (var i=0; i<links.length; i++) {
			var link = links[i];
			var url = link.href;
			if (url && url.indexOf('ViewNodeType') != -1 && url.indexOf('loadScroll') == -1) {
				url = url.substr(0, url.length-1).concat(", eXo.ecm.UIJCRExplorer.loadScroll)");
	  		link.href = url;
			}
		}
	}
};

UIJCRExplorer.prototype.loadScroll = function(e) {
//	console.log("load scroll jcr");
	var jcr = eXo.ecm.UIJCRExplorer;
	//var uiDocumentWorkspace = document.getElementById("UIDocumentWorkspace");
	var uiFilePlanView = document.getElementById("UIFilePlanView");
	//var rootNode = (uiDocumentWorkspace != null) ? uiDocumentWorkspace : ((uiFilePlanView != null) ? uiFilePlanView : null);
	if (uiFilePlanView) {
		jcr.scrollMgr = eXo.portal.UIPortalControl.newScrollManager();
		jcr.scrollMgr.initFunction = jcr.initScroll;
		var mainCont = eXo.core.DOMUtil.findFirstDescendantByClass(uiFilePlanView, "div", "UIHorizontalTabs");
		var tabs = eXo.core.DOMUtil.findFirstDescendantByClass(mainCont, "div", "TabsContainer");
		var arrows = eXo.core.DOMUtil.findFirstDescendantByClass(mainCont, "div", "NavigationButtonContainer");
		jcr.scrollMgr.mainContainer = mainCont;
		jcr.scrollMgr.arrowsContainer = arrows;
		jcr.scrollMgr.loadElements("UITab");
		var arrowButtons = eXo.core.DOMUtil.findDescendantsByTagName(arrows, "div");
		if (arrowButtons.length == 2) {
			jcr.scrollMgr.initArrowButton(arrowButtons[0], "left", "NavigationIcon ScrollBackArrow16x16Icon", "NavigationIcon DisableBackArrow16x16Icon", "NavigationIcon DisableBackArrow16x16Icon");
			jcr.scrollMgr.initArrowButton(arrowButtons[1], "right", "NavigationIcon ScrollNextArrow16x16Icon", "NavigationIcon DisableNextArrow16x16Icon", "NavigationIcon DisableNextArrow16x16Icon");
		}
		jcr.scrollMgr.callback = jcr.scrollCallback;
		jcr.initScroll();
	}
};

UIJCRExplorer.prototype.initScroll = function(e) {
	var scrollMgr = eXo.ecm.UIJCRExplorer.scrollMgr;
	scrollMgr.init();
	// Gets the maximum width available for the tabs
	scrollMgr.checkAvailableSpace();
	scrollMgr.renderElements();
};

UIJCRExplorer.prototype.scrollCallback = function() {
	var scrollMgr = eXo.ecm.UIJCRExplorer.scrollMgr;
	var selTab = eXo.core.DOMUtil.findFirstDescendantByClass(scrollMgr.mainContainer, "div", "SelectedTab");
	if (selTab) {
		scrollMgr.cleanElements();
		scrollMgr.getElementsSpace();
	}
};

UIJCRExplorer.prototype.loadNodeTypeScroll = function() {
	var jcr = eXo.ecm.UIJCRExplorer;
	var uiPopup = document.getElementById("UINodeTypeInfoPopup");
	if (uiPopup) {
		console.log("load nt scroll");
		jcr.ntScrollMgr = eXo.portal.UIPortalControl.newScrollManager();
		jcr.ntScrollMgr.initFunction = jcr.initNodeTypeScroll;
		var mainCont = eXo.core.DOMUtil.findFirstDescendantByClass(uiPopup, "div", "UIHorizontalTabs");
		var tabs = eXo.core.DOMUtil.findFirstDescendantByClass(mainCont, "div", "TabsContainer");
		var arrows = eXo.core.DOMUtil.findFirstDescendantByClass(mainCont, "div", "NavigationButtonContainer");
		jcr.ntScrollMgr.mainContainer = mainCont;
		jcr.ntScrollMgr.arrowsContainer = arrows;
		jcr.ntScrollMgr.loadElements("UITab");
		var arrowButtons = eXo.core.DOMUtil.findDescendantsByTagName(arrows, "div");
		if (arrowButtons.length == 2) {
			jcr.ntScrollMgr.initArrowButton(arrowButtons[0], "left", "NavigationIcon ScrollBackArrow16x16Icon", "NavigationIcon DisableBackArrow16x16Icon", "NavigationIcon DisableBackArrow16x16Icon");
			jcr.ntScrollMgr.initArrowButton(arrowButtons[1], "right", "NavigationIcon ScrollNextArrow16x16Icon", "NavigationIcon DisableNextArrow16x16Icon", "NavigationIcon DisableNextArrow16x16Icon");
		}
		//jcr.ntScrollMgr.callback = jcr.scrollCallback;
		jcr.initNodeTypeScroll();
	}
};

UIJCRExplorer.prototype.initNodeTypeScroll = function() {
	var ntScrollMgr = eXo.ecm.UIJCRExplorer.ntScrollMgr;
	ntScrollMgr.init();
	// Gets the maximum width available for the tabs
	ntScrollMgr.checkAvailableSpace();
	ntScrollMgr.renderElements();
};

eXo.ecm.UIJCRExplorer = new UIJCRExplorer();