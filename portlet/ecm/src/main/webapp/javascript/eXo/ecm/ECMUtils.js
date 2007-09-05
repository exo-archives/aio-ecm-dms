eXo.require('eXo.webui.UIRightClickPopupMenu');

function ECMUtils() {
	this.popupArray =  new Array() ;
};

ECMUtils.prototype.init = function(portletId) {
	var portlet = document.getElementById(portletId) ;
	// TODO: Fix temporary for the problem Minimize window in Page Mode
	if(!portlet) return ;

	eXo.webui.UIRightClickPopupMenu.disableContextMenu(portletId) ;
	portlet.onmousedown = function(event) {
		eXo.ecm.ECMUtils.closeAllPopup() ;
	}
	if(document.getElementById("UIPageDesktop")) this.fixHeight(portletId) ;
};

ECMUtils.prototype.fixHeight = function(portletId) {
	var portlet =document.getElementById(portletId) ;
	var delta = portlet.parentNode.offsetHeight - portlet.offsetHeight ;
	var resizeObj = eXo.core.DOMUtil.findDescendantsByClass(portlet, 'div', 'UIResizableBlock') ;
	for(var i = 0; i < resizeObj.length; i++) {
		var nHeight = parseInt(resizeObj[i].offsetHeight) + delta;
		if (nHeight < 0 ) nHeight = "0px" ;
		resizeObj[i].style.height = nHeight + 'px' ;
	}
};

ECMUtils.prototype.clickLeftMouse = function(evnt, clickedElemt, pos, option) {
	evnt.cancelBubble = true;
	popupSelector = eXo.core.DOMUtil.findAncestorByClass(clickedElemt, "UIPopupSelector");
	showBlock = eXo.core.DOMUtil.findFirstDescendantByClass(popupSelector,"div", "UISelectContent");
	if(option == 1) {
		showBlock.style.width = (popupSelector.offsetWidth - 2) + "px";
	}
	if(showBlock.style.display == "block") {
		eXo.webui.UIPopup.hide(showBlock) ;
		return ;
	}
	eXo.webui.UIPopup.show(showBlock) ;
	showBlock.onmousedown = function(e) {
		if(!e) e = window.event ;
		e.cancelBubble = true ;
	}
	this.popupArray.push(showBlock);
	showBlock.style.top = popupSelector.offsetHeight + "px";
};

ECMUtils.prototype.closeAllPopup = function() {
	for(var i = 0; i < this.popupArray.length; i++) {
		this.popupArray[i].style.display = "none" ;
	}
	this.popupArray.clear() ;
};

ECMUtils.prototype.initVote = function(voteId, rate) {
	var vote = document.getElementById(voteId) ;
	vote.rate = rate = parseInt(rate) ;
	var optsContainer = eXo.core.DOMUtil.findFirstDescendantByClass(vote, "div", "OptionsContainer") ;
	var options = eXo.core.DOMUtil.getChildrenByTagName(optsContainer, "div") ;
	for(var i = 0; i < options.length; i++) {
		options[i].onmouseover = this.overVote ;
		if(i < rate) options[i].className = "RatedVote" ;
	}

	vote.onmouseover = function() {
		var optsCon= eXo.core.DOMUtil.findFirstDescendantByClass(this, "div", "OptionsContainer") ;
		var opts = eXo.core.DOMUtil.getChildrenByTagName(optsCon, "div") ;
		for(var j = 0; j < opts.length; j++) {
			if(j < this.rate) opts[j].className = "RatedVote" ;
			else opts[j].className = "NormalVote" ;
		}
	}
	optsContainer.onmouseover = function(e) {
		if(!e) e = window.event ;
		e.cancelBubble = true ;
	}
};

ECMUtils.prototype.overVote = function(event) {
	var optsCont = eXo.core.DOMUtil.findAncestorByClass(this, "OptionsContainer") ;
	var opts = eXo.core.DOMUtil.getChildrenByTagName(optsCont, "div") ;
	var i = opts.length;
	for(--i; i >= 0; i--) {
		if(opts[i] == this) break ;
		opts[i].className = "NormalVote" ;
	}
	if(opts[i].className == "OverVote") return ;
	for(; i >= 0; i--) {
		opts[i].className = "OverVote" ;
	}
};

/*
 * minh.js.exo
 */
 
ECMUtils.prototype.showHideComponent = function(elemtClicked) {
	var DOMUtil = eXo.core.DOMUtil;
	var nodeReference = DOMUtil.findAncestorByClass(elemtClicked,  "ShowHideContainer");
	var elemt = DOMUtil.findFirstDescendantByClass(nodeReference, "div", "ShowHideComponent") ;

	if(elemt.style.display == 'none') {
		elemtClicked.childNodes[0].style.display = 'none' ;
		elemtClicked.childNodes[1].style.display = 'block' ;
		elemt.style.display = 'block' ;
	} else {
		elemtClicked.childNodes[0].style.display = 'block' ;
		elemtClicked.childNodes[1].style.display = 'none' ;
		elemt.style.display = 'none' ;
	}
};

ECMUtils.prototype.collapseExpand = function(elemt) {
	var node = elemt.parentNode ;
	var subGroup = eXo.core.DOMUtil.findFirstChildByClass(node, "div", "NodeGroup") ;
	if(!subGroup) return false;
	if(subGroup.style.display == "none") {
		elemt.className = "CollapseIcon" ;
		subGroup.style.display = "block" ;
	} else {
		elemt.className = "ExpandIcon" ;
		subGroup.style.display = "none" ;
	}
	return true
};

ECMUtils.prototype.filterValue = function(frmId) {
	var form = document.getElementById(frmId) ;
	form['result'].innerHTML = form['tempSel'].innerHTML ;
	var	filterValue = form['filter'].value ;
	filterValue = "^" + filterValue.replace("*", ".*") ;
	var re = new RegExp(filterValue, "gi") ;
	var elSel = form['result'];
  var i;
  for (i = elSel.length - 1; i>=0; i--) {
    if (!re.test(elSel.options[i].value)) {
      elSel.remove(i);
    }
  }
};

ECMUtils.prototype.convertElemtToHTML = function(id) {
	var elemt = document.getElementById(id) ;
	var text = elemt.innerHTML ;
	text = text.toString() ;

	text = text.replace(/&/g, "&amp;").replace(/"/g, "&quot;")
						 .replace(/</g, "&lt;").replace(/>/g, "&gt;") ;

	elemt.innerHTML = text ;
};

ECMUtils.prototype.onKeyPress = function() {
	var uiAddressBarControl = document.getElementById("UIAddressBarControl");
	if(uiAddressBarControl) {
		uiAddressBarControl.onkeypress = eXo.ecm.ECMUtils.onEnterPress ;
	}
};

ECMUtils.prototype.onEnterPress = function(e) {
	var uiAdressBarAction = document.getElementById("UIAddressBarAction");
	if(uiAdressBarAction) {
		var code;
		if(!e) e = window.event;
		if(e.keyCode) code = e.keyCode;
		else if (e.which) code = e.which;
		
		if(code == 13) {
			window.location.href = uiAdressBarAction.href ;
		}
	}
};

ECMUtils.prototype.replaceToIframe = function(txtAreaId) {
	if (!document.getElementById(txtAreaId)) {
		/*
		 * minh.js.exo
		 * fix bug ECM-1419
		 * this is Java bug.
		 * double call this method.
		 */
		return ;
	}
	var txtArea = document.getElementById(txtAreaId) ;
	var ifrm = document.createElement("IFRAME") ;
	with(ifrm) {
		className = 'ECMIframe' ;
		src = 'javascript:void(0)' ;
		frameBorder = 0 ;
		scrolling = "auto" ;
	}
	var strValue = txtArea.value ;
	txtArea.parentNode.replaceChild(ifrm, txtArea) ;
	try {
		var doc = ifrm.contentWindow.document ;
		doc.open() ;
		doc.write(strValue) ;
		doc.close() ;
	} catch (ex) {}
} ;


ECMUtils.prototype.generateWebDAVLink = function(serverInfo,portalName,repository,workspace,nodePath,mimetype) {		
 if(eXo.core.Browser.getBrowserType() == "ie") {
 	if(mimetype == "application/xls" || mimetype == "application/msword" || mimetype =="application/ppt") { 		
 		window.location = serverInfo + "/" + portalName +"/lnkgenerator/openit.lnk?path=" +"/"+repository +"/" +workspace + nodePath;  
 	} else {
 		window.location = serverInfo+ "/" + portalName + "/"+repository +"/" +workspace + nodePath; 		 		
 	} 	  
 } else {
  window.location = serverInfo+ "/" + portalName + "/"+repository +"/" +workspace + nodePath;
 } 
} ;

eXo.ecm.ECMUtils = new ECMUtils(); 