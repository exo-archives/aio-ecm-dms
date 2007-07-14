
function UIJCRExplorer() {
	this.scrollMgr = null;
};

UIJCRExplorer.prototype.loadScroll = function(e) {
//	console.log("load scroll jcr");
	var jcr = eXo.ecm.UIJCRExplorer;
	var uiDocumentWorkspace = document.getElementById("UIDocumentWorkspace");
	if (uiDocumentWorkspace) {
		jcr.scrollMgr = eXo.portal.UIPortalControl.newScrollManager();
		jcr.scrollMgr.initFunction = jcr.initScroll;
		var mainCont = eXo.core.DOMUtil.findFirstDescendantByClass(uiDocumentWorkspace, "div", "UIHorizontalTabs");
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

eXo.ecm.UIJCRExplorer = new UIJCRExplorer();