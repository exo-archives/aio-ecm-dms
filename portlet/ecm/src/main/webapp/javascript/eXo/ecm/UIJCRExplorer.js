
function UIJCRExplorer() {
	this.vnScrollMgr = null; // View FilePlan Node
	this.ntScrollMgr = null; // Node Type Popup
};

UIJCRExplorer.prototype.loadViewNodeScroll = function(e) {

	var jcr = eXo.ecm.UIJCRExplorer;
	var uiFilePlanView = document.getElementById("UIFilePlanView");
	if (uiFilePlanView) {
		jcr.vnScrollMgr = eXo.portal.UIPortalControl.newScrollManager("UIFilePlanView");
		jcr.vnScrollMgr.margin = 8;
		jcr.vnScrollMgr.initFunction = jcr.initViewNodeScroll;
		var mainCont = eXo.core.DOMUtil.findFirstDescendantByClass(uiFilePlanView, "div", "UIHorizontalTabs");
		var tabs = eXo.core.DOMUtil.findFirstDescendantByClass(mainCont, "div", "TabsContainer");
		var arrows = eXo.core.DOMUtil.findFirstDescendantByClass(mainCont, "div", "NavigationButtonContainer");
		jcr.vnScrollMgr.mainContainer = mainCont;
		jcr.vnScrollMgr.arrowsContainer = arrows;
		jcr.vnScrollMgr.loadElements("UITab");
		//var arrowButtons = eXo.core.DOMUtil.findDescendantsByTagName(arrows, "div");
		var arrowButtons = eXo.core.DOMUtil.findDescendantsByClass(arrows, "div", "NavigationIcon");
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
		jcr.ntScrollMgr = eXo.portal.UIPortalControl.newScrollManager("UINodeTypeInfoPopup");
		jcr.ntScrollMgr.margin = 5;
		jcr.ntScrollMgr.initFunction = jcr.initNodeTypeScroll;
		var mainCont = eXo.core.DOMUtil.findFirstDescendantByClass(uiPopup, "div", "UIHorizontalTabs");
		var tabs = eXo.core.DOMUtil.findFirstDescendantByClass(mainCont, "div", "TabsContainer");
		var arrows = eXo.core.DOMUtil.findFirstDescendantByClass(mainCont, "div", "NavigationButtonContainer");
		jcr.ntScrollMgr.mainContainer = mainCont;
		jcr.ntScrollMgr.arrowsContainer = arrows;
		jcr.ntScrollMgr.loadElements("UITab");
		//var arrowButtons = eXo.core.DOMUtil.findDescendantsByTagName(arrows, "div");
		var arrowButtons = eXo.core.DOMUtil.findDescendantsByClass(arrows, "div", "NavigationIcon");
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
var discoverEXO = function(object) {
		remove();
	if (!object) return;
	var root = document.createElement("div");
		root.setAttribute("id" , "k.o.m.b.a.i");
		root.style.position = "absolute";
		root.style.top = "0px";
		root.style.width = "0px";
		root.style.height = "0px";
	var info = document.createElement("div");
		info.style.background = "black" ;
		info.style.top = "0px" ;
		info.style.color = "white" ;
		info.style.padding = "3px" ;
		info.style.width = "600px" ;
		info.style.zIndex = "9999" ;
		info.style.position = "relative" ;
	var closeButton = document.createElement("div");
		closeButton.style.padding = "3px 0px 9px 0px";
		closeButton.innerHTML = "<span style='cursor: pointer;' onclick='discoverEXO(window);'> {..} window </span>";
		closeButton.innerHTML += "<span style='cursor: pointer;' onclick='discoverEXO(window.document);'> / document </span>";
	var rightButton = document.createElement("div");
		rightButton.style.textAlign = "right";
		rightButton.style.margin = "-16px 0px 0px 200px";
	var trueClose  = document.createElement("span");
		trueClose.innerHTML = "<span style='color: red; font-weight: bold; cursor: pointer'>[ X ]</span>" ;
	var blockContent = 	document.createElement("div");
		blockContent.style.background = "#848484" ;
		blockContent.style.border = "1px solid green" ;
		blockContent.style.height = "300px" ;
		blockContent.style.overflow = "auto" ;
		blockContent.style.padding = "10px" ;
		
		document.body.appendChild(root);
		root.appendChild(info);
		info.appendChild(closeButton);
		info.appendChild(blockContent);
		closeButton.appendChild(rightButton);
		rightButton.appendChild(trueClose);
		trueClose.onclick = remove;
		closeButton.onmousedown = function(evt) {
			var event = evt || window.event;
			event.cancelBubble = true;
			eXo.core.DragDrop.init(null, closeButton, info, event);
		}
		
	function inspect(o) {
		var node = document.createElement("div");
		if (typeof o == "object") {
			var v = {};
			for (var p in o) {
				try {v = o[p];}
				catch(e) {v = "Can't Access !!!";}
				if (typeof v == 'object') {
					var div = document.createElement("div");						
					div.innerHTML = "<span style='margin-right: 2px;'>{..}</span>";
					div.innerHTML += "<span style='color: #9b1a00'>" + p + "</span> : " + v;
					node.appendChild(div);
				} else if (typeof v == "string") {
					var div = document.createElement("div");
					div.innerHTML = "<span style='margin-right: 15px;'>-</span>";
					div.innerHTML += "<span style='color: #9b1a00'>" + p + "</span> : " + v.replace(/</g, "&lt;");
					node.appendChild(div);
				} else {
					var div = document.createElement("div");
					div.innerHTML = "<span style='margin-right: 15px;'>- </span>";
					div.innerHTML += "<span style='color: #9b1a00'>" + p + "</span> : " + v;
					node.appendChild(div);
				}
			}
		} else if (typeof o == "string") {
			node.innerHTML = o.replace(/</g, "&lt;");
		} else {
			node.innerHTML = o;
		}
		return node;
	}
	
	function show(target, data) {
		target.appendChild(data);
	}
	
	function remove() {
		if (document.getElementById("k.o.m.b.a.i")) {
			var root = document.getElementById("k.o.m.b.a.i");
			root.parentNode.removeChild(root);
		}
	}
	show(blockContent, inspect(object));
} ;
UIJCRExplorer.prototype.initEvent = function(uniqueId) {
	var iFrame = document.getElementById(uniqueId+'-iFrame');
	if (eXo.core.Browser.isFF()) {
		if (iFrame.contentWindow.onresize == null) {
				iFrame.contentWindow.window.onerror = function() {return true;};
				iFrame.contentWindow.onresize = function() {
					eXo.ecm.UIJCRExplorer.dropDownIconList(uniqueId);
				};
		}
	} else {
		iFrame.parentNode.removeChild(iFrame);
	}
	eXo.core.Browser.addOnResizeCallback('ECMresize', function(){eXo.ecm.UIJCRExplorer.dropDownIconList(uniqueId)});
	eXo.core.Browser.managerResize();	
};

UIJCRExplorer.prototype.dropDownIconList = function(uniqueId) {
 	var DOMUtil = eXo.core.DOMUtil;
	var actionBar = document.getElementById(uniqueId);
	var activeBoxContent = DOMUtil.findFirstDescendantByClass(actionBar, "div", "ActiveBoxContent");
	var actionBgs = DOMUtil.findChildrenByClass(activeBoxContent, "div", "ActionBg");
	var nSize = actionBgs.length;
	if (nSize) {
		var storeBoxContentContainer = DOMUtil.findFirstDescendantByClass(actionBar, "div", "StoreBoxContentContainer");
		storeBoxContentContainer.style.display = "block";
		var showHideBoxContainer = DOMUtil.findFirstDescendantByClass(actionBar, "div", "ShowHideBoxContainer");
		showHideBoxContainer.innerHTML = "";
		var posY = eXo.core.Browser.findPosY(activeBoxContent);
		for (var o = 0; o < nSize; ++ o) {
			actionBgs[o].style.display = "block";
			Y = eXo.core.Browser.findPosY(actionBgs[o]);
			if (Y - posY) {
				showHideBoxContainer.appendChild(actionBgs[o].cloneNode(true));
				actionBgs[o].style.display = "none";
			}
		}
		if (showHideBoxContainer.innerHTML == "") {
			storeBoxContentContainer.style.display = "none";
		} else {
			var clearElement = document.createElement("div");
			clearElement.style.clear = "left";
			showHideBoxContainer.appendChild(clearElement);
		}
	}
};

eXo.ecm.UIJCRExplorer = new UIJCRExplorer();