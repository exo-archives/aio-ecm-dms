
function UIJCRExplorer() {
	this.vnScrollMgr = null;
	this.ntScrollMgr = null;
};

//UIJCRExplorer.prototype.addCallBackToViewNodeLink = function() {
//	var rootElement = document.getElementById("UIJCRExplorer");
//	var searchResult = eXo.core.DOMUtil.findFirstDescendantByClass(rootElement, "div", "UISearchResult");
//	if (searchResult) {
//		var links = searchResult.getElementsByTagName("a");
//		for (var i=0; i<links.length; i++) {
//			var link = links[i];
//			var url = link.href;
//			if (url && url.indexOf('op=View') != -1 && url.indexOf('loadViewNodeScroll') == -1) {
//				url = url.substr(0, url.length-1).concat(", eXo.ecm.UIJCRExplorer.loadViewNodeScroll)");
//	  		link.href = url;
//			}
//		}
//	}
//};

//UIJCRExplorer.prototype.addCallBackToNodeTypeLink = function() {
//	var rootElement = document.getElementById("UIJCRExplorer");
//	var actionBar = eXo.core.DOMUtil.findFirstDescendantByClass(rootElement, "div", "UIActionBar");
//	if (actionBar) {
//		var links = actionBar.getElementsByTagName("a");
//		for (var i=0; i<links.length; i++) {
//			var link = links[i];
//			var url = link.href;
//			if (url && url.indexOf('ViewNodeType') != -1 && url.indexOf('loadNodeTypeScroll') == -1) {
//				url = url.substr(0, url.length-1).concat(", eXo.ecm.UIJCRExplorer.loadNodeTypeScroll)");
//	  		link.href = url;
//			}
//		}
//	}
//};

UIJCRExplorer.prototype.loadViewNodeScroll = function(e) {
	var jcr = eXo.ecm.UIJCRExplorer;
	var uiFilePlanView = document.getElementById("UIFilePlanView");
	if (uiFilePlanView) {
		jcr.vnScrollMgr = eXo.portal.UIPortalControl.newScrollManager();
		jcr.vnScrollMgr.initFunction = jcr.initViewNodeScroll;
		var mainCont = eXo.core.DOMUtil.findFirstDescendantByClass(uiFilePlanView, "div", "UIHorizontalTabs");
		var tabs = eXo.core.DOMUtil.findFirstDescendantByClass(mainCont, "div", "TabsContainer");
		var arrows = eXo.core.DOMUtil.findFirstDescendantByClass(mainCont, "div", "NavigationButtonContainer");
		jcr.vnScrollMgr.mainContainer = mainCont;
		jcr.vnScrollMgr.arrowsContainer = arrows;
		jcr.vnScrollMgr.loadElements("UITab");
		var arrowButtons = eXo.core.DOMUtil.findDescendantsByTagName(arrows, "div");
		if (arrowButtons.length == 2) {
			jcr.vnScrollMgr.initArrowButton(arrowButtons[0], "left", "NavigationIcon ScrollBackArrow16x16Icon", "NavigationIcon DisableBackArrow16x16Icon", "NavigationIcon DisableBackArrow16x16Icon");
			jcr.vnScrollMgr.initArrowButton(arrowButtons[1], "right", "NavigationIcon ScrollNextArrow16x16Icon", "NavigationIcon DisableNextArrow16x16Icon", "NavigationIcon DisableNextArrow16x16Icon");
		}
		jcr.vnScrollMgr.callback = jcr.viewNodeScrollCallback;
		jcr.initViewNodeScroll();
	}
};

UIJCRExplorer.prototype.initViewNodeScroll = function(e) {
	var scrollMgr = eXo.ecm.UIJCRExplorer.vnScrollMgr;
	scrollMgr.init();
	// Gets the maximum width available for the tabs
	scrollMgr.checkAvailableSpace();
	scrollMgr.renderElements();
};

UIJCRExplorer.prototype.viewNodeScrollCallback = function() {
	var scrollMgr = eXo.ecm.UIJCRExplorer.vnScrollMgr;
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
	var scrollMgr = eXo.ecm.UIJCRExplorer.ntScrollMgr;
	scrollMgr.init();
	// Gets the maximum width available for the tabs
	scrollMgr.checkAvailableSpace();
	scrollMgr.renderElements();
};

eXo.ecm.UIJCRExplorer = new UIJCRExplorer();