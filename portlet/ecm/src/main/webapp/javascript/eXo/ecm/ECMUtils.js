eXo.require('eXo.webui.UIRightClickPopupMenu');

function ECMUtils() {
	this.popupArray =  new Array() ;
}

ECMUtils.prototype.init = function(portletId) {
	eXo.webui.UIRightClickPopupMenu.disableContextMenu(portletId) ;
	var portlet = document.getElementById(portletId) ;
	portlet.onmousedown = function(event) {
		eXo.ecm.ECMUtils.closeAllPopup() ;
	}
	if(document.getElementById("UIPageDesktop")) this.fixHeight(portletId) ;
}

ECMUtils.prototype.fixHeight = function(portletId) {
	var portlet =document.getElementById(portletId) ;
	var child = eXo.core.DOMUtil.getChildrenByTagName(portlet, 'div') ;
	
	var delta = portlet.offsetHeight - child[0].offsetHeight ;
	var resizeObj = eXo.core.DOMUtil.findDescendantsByClass(portlet, 'div', 'UIResizableBlock') ;
	for(var i = 0, ln = resizeObj.length; i < ln; i++) {
		resizeObj[i].style.height = (parseInt(resizeObj[i].offsetHeight) + delta) + 'px' ;
	}
}

ECMUtils.prototype.clickLeftMouse = function(event, clickedElemt, pos, option) {
	event.cancelBubble = true;
	popupSelector = eXo.core.DOMUtil.findAncestorByClass(clickedElemt, "UIPopupSelector");
	showBlock = eXo.core.DOMUtil.findFirstDescendantByClass(popupSelector,"div", "UISelectContent");
	if(option == 1) {
		showBlock.style.width = (popupSelector.offsetWidth - 2) + "px";
	}
	this.showPopup(showBlock, true)
	var intTop = 0;
	var intLeft = 0;
	switch (pos) {
		case 1:							// Top
		  intTop  = eXo.core.Browser.findPosY(popupSelector) - showBlock.offsetHeight;
		  intLeft = eXo.core.Browser.findPosX(popupSelector) + 1;	
			break;
		case 2:							// Bottom
  		intTop  = eXo.core.Browser.findPosY(popupSelector) + popupSelector.offsetHeight;
		  intLeft = eXo.core.Browser.findPosX(popupSelector) + 1;
			break;
		case 3:							// Left
  		intTop  = eXo.core.Browser.findPosY(popupSelector);
		  intLeft = eXo.core.Browser.findPosX(popupSelector) - showBlock.offsetWidth;						
			break;
		default:						// Right
  		intTop  = eXo.core.Browser.findPosY(popupSelector);
		  intLeft = eXo.core.Browser.findPosX(popupSelector) + popupSelector.offsetWidth;						
			break;
	}
	
  if(document.getElementById("UIPageDesktop")) {
  	popupWindow = eXo.core.DOMUtil.findAncestorByClass(popupSelector, "UIDragObject");
    intTop = intTop - eXo.core.Browser.findPosY(popupWindow) ;
    intLeft = intLeft - eXo.core.Browser.findPosX(popupWindow) ;
  }
	showBlock.style.top = intTop + "px";
	showBlock.style.left = intLeft + "px";
	showBlock.firstCall = true;
}

ECMUtils.prototype.showPopup = function(showBlock, clearAll) {
		if(clearAll) this.closeAllPopup();
		eXo.webui.UIPopup.show(showBlock) ;
		showBlock.onmousedown = function(e) {
			if(!e) e = window.event ;
			e.cancelBubble = true ;
		}
		this.popupArray.push(showBlock);
};

ECMUtils.prototype.closeAllPopup = function() {
	for(var i = 0; i < this.popupArray.length; i++) {
		this.popupArray[i].style.display = "none" ;
	}
	this.popupArray.clear() ;
}

ECMUtils.prototype.selectBoxOnChange = function(elemt) {
	var selectBox = eXo.core.DOMUtil.findAncestorByClass(elemt, "UISelectBoxOnChange");
	var contentContainer = eXo.core.DOMUtil.findFirstDescendantByClass(selectBox, "div", "SelectBoxContentContainer") ;
	var tabs = eXo.core.DOMUtil.findChildrenByClass(contentContainer, "div", "SelectBoxContent");
	for(var i=0; i < tabs.length; i++) {
		tabs[i].style.display = "none";
	}
	tabs[elemt.selectedIndex].style.display = "block";
}

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
}

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
}

ECMUtils.prototype.showHideComponent = function(elemtId) {
	var elemt = document.getElementById(elemtId) ;
	if(elemt.style.display == 'none') elemt.style.display = 'block' ;
	else elemt.style.display = 'none' ;
}

eXo.ecm.ECMUtils = new ECMUtils(); 