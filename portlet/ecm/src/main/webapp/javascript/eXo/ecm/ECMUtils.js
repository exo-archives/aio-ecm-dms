function ECMUtils() {
	var Self = this;

	//set private property;
	var DOMUtils = eXo.core.DOMUtil;
	var Browser = eXo.core.Browser;
	var RightClick = eXo.webui.UIRightClickPopupMenu;
	
	ECMUtils.prototype.popupArray = [];
	ECMUtils.prototype.selectItemList = [];
	ECMUtils.prototype.temporaryItem = null;
	ECMUtils.prototype.allActionBox = [];
	
	ECMUtils.prototype.init = function(portletId) {
		
		//remove select item list;
		Self.temporaryItem = null;
		Self.selectItemList = [];
		
		var portlet = document.getElementById(portletId) ;
		if(!portlet) return ;
		RightClick.disableContextMenu(portletId) ;
		portlet.onmousedown = function(event) {
			eXo.ecm.ECMUtils.closeAllPopup() ;
		}
		if(document.getElementById("UIPageDesktop")) {
			Self.fixHeight(portletId) ;
			var uiPageDeskTop = document.getElementById("UIPageDesktop");
			var uiJCRExplorers = DOMUtils.findDescendantsByClass(uiPageDeskTop, 'div', 'UIJCRExplorer') ;
			if (uiJCRExplorers.length) {
				for (var i = 0; i < uiJCRExplorers.length; i++) {
					var uiResizeBlock = DOMUtils.findAncestorByClass(uiJCRExplorers[i], "UIResizableBlock");
					if (uiResizeBlock) uiResizeBlock.style.overflow = "hidden";
				}
			}
		}
	};
	
	ECMUtils.prototype.fixHeight = function(portletId) {
		var portlet = document.getElementById(portletId) ;
		var refElement = DOMUtils.findAncestorByClass(portlet, "UIApplication") ;
		if (refElement == null) return;
		var delta = (parseInt(refElement.style.height) - portlet.offsetHeight);
		var resizeObj = DOMUtils.findDescendantsByClass(portlet, 'div', 'UIResizableBlock') ;
		if(resizeObj.length) {
			for(var i = 0; i < resizeObj.length; i++) {
				resizeObj[i].style.height = (resizeObj[i].offsetHeight + delta) + "px" ;
			}
		}
	};
	
	ECMUtils.prototype.clickLeftMouse = function(event, clickedElement, position, option) {
		var event = event || window.event;
		event.cancelBubble = true;
		popupSelector = DOMUtils.findAncestorByClass(clickedElement, "UIPopupSelector");
		showBlock = DOMUtils.findFirstDescendantByClass(popupSelector,"div", "UISelectContent");
		if(option == 1) {
			showBlock.style.width = (popupSelector.offsetWidth - 2) + "px";
		}
		if(showBlock.style.display == "block") {
			eXo.webui.UIPopup.hide(showBlock) ;
			return ;
		}
		eXo.webui.UIPopup.show(showBlock) ;
		showBlock.onmousedown = function(event) {
			var event = event || window.event ;
			event.cancelBubble = true ;
		}
		Self.popupArray.push(showBlock);
		showBlock.style.top = popupSelector.offsetHeight + "px";
	};
	
	ECMUtils.prototype.closeAllPopup = function() {
		for(var i = 0; i < Self.popupArray.length; i++) {
			Self.popupArray[i].style.display = "none" ;
		}
		Self.popupArray.clear() ;
	};
	
	ECMUtils.prototype.initVote = function(voteId, rate) {
		var vote = document.getElementById(voteId) ;
		vote.rate = rate = parseInt(rate) ;
		var optsContainer = DOMUtils.findFirstDescendantByClass(vote, "div", "OptionsContainer") ;
		var options = DOMUtils.getChildrenByTagName(optsContainer, "div") ;
		for(var i = 0; i < options.length; i++) {
			options[i].onmouseover = Self.overVote ;
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
		optsContainer.onmouseover = function(event) {
			var event = event || window.event ;
			event.cancelBubble = true ;
		}
	};
	
	ECMUtils.prototype.overVote = function(event) {
		var optionsContainer = DOMUtils.findAncestorByClass(this, "OptionsContainer") ;
		var opts = DOMUtils.getChildrenByTagName(optionsContainer, "div") ;
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
	
	 
	ECMUtils.prototype.showHideComponent = function(elemtClicked) {
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
	
	ECMUtils.prototype.collapseExpand = function(element) {
		var node = element.parentNode ;
		var subGroup = DOMUtils.findFirstChildByClass(node, "div", "NodeGroup") ;
		if(!subGroup) return false;
		if(subGroup.style.display == "none") {
			if (element.className == "ExpandIcon") 	element.className = "CollapseIcon" ;
			subGroup.style.display = "block" ;
		} else {
			if (element.className == "CollapseIcon") element.className = "ExpandIcon" ;
			subGroup.style.display = "none" ;
		}
		return true;
	};
	
	ECMUtils.prototype.collapseExpandPart = function(element) {
		var node = element.parentNode ;
		var subGroup1 = DOMUtils.findFirstChildByClass(node, "div", "NodeGroup1") ;
		var subGroup2 = DOMUtils.findFirstChildByClass(node, "div", "NodeGroup2") ;
		if (subGroup1.style.display == "none") {
			if (element.className == "CollapseIcon") 	element.className = "ExpandIcon";
			subGroup1.style.display = "block";
			subGroup2.style.display = "none";
		} else {
			if (element.className == "ExpandIcon") element.className = "CollapseIcon";
			subGroup1.style.display = "none";
			subGroup2.style.display = "block";
		}
		return true;
	};
	
	ECMUtils.prototype.filterValue = function(frmId) {
		var form = document.getElementByIdR(frmId) ;
		if (eXo.core.Browser.browserType == "ie") {
			var text = document.createTextNode(form['tempSel'].innerHTML);
			form['result'].appendChild(text);
		}else {
		  form['result'].innerHTML = form['tempSel'].innerHTML ;
		}
		var	filterValue = form['filter'].value ;
		filterValue = filterValue.replace("*", ".*") ;		
		var re = new RegExp(filterValue, "i") ;	
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
			uiAddressBarControl.onkeypress = Self.onEnterPress ;
		}
	};
	
	ECMUtils.prototype.onEnterPress = function(event) {
		var uiAdressBarAction = document.getElementById("UIAddressBarAction");
		if(uiAdressBarAction) {
			var code;
			var event = event || window.event;
			if(event.keyCode) code = event.keyCode;
			else if (event.which) code = event.which;
			
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
	      // query parameter s must be encoded.
	      var path = "/";
	      nodePath = nodePath.substr(1).split("\/");
		    if (typeof(nodePath.length) == 'number') {
		      for (var i=0; i < nodePath.length; i++) {
		        path += encodeURIComponent(nodePath[i]) + "/";
		      }
		    }
		    window.location = serverInfo+ "/"+portalName + "/rest/lnkproducer/openit.lnk?path=/"+repository +"/" +workspace + path;
	   	} else {
	 	  	window.location = serverInfo + "/"+portalName + "/rest/jcr/"+repository +"/" +workspace + nodePath; 		 		
	 	  } 	  
	  } else {
	    window.location = serverInfo+ "/"+portalName + "/rest/jcr/"+repository +"/" +workspace + nodePath;
	  } 
	} ;
	
	/*
	 * multiply select in JCR Explorer
	 * working with ThumbnailsView.gtmpl
	 */
	
	ECMUtils.prototype.concatMethod =  function() {
		 	var oArg = arguments;
		 	var nSize = oArg.length;
		 	if (nSize < 2) return;
		 	var mSelf = oArg[0];
			return function() {
				var aArg = [];
				for (var i = 0; i < arguments.length; ++ i) {
					aArg.push(arguments[i]);
				}
				mSelf.apply(mSelf, aArg);
				for (i = 1; i < nSize; ++ i) {
					var oSet = {
						method: oArg[i].method || function() {},
						param: oArg[i].param || aArg
					}
					oSet.method.apply(oSet.method, oSet.param);
				}
			}
	};
	
	ECMUtils.prototype.prepareSelectItem = function(event, element, menuId, objId) {
		
		var event = event || window.event;
		event.cancelBubble = true;
		var uiDocumentWorkspace = DOMUtils.findAncestorByClass(element ,"UIDocumentWorkspace");
		if (!uiDocumentWorkspace) return;
		
		if (element.className == "ActionIconBox") var parent = element;
		else var parent = DOMUtils.findAncestorByClass(element, "ActionIconBox");
		if (Self.temporaryItem) Self.temporaryItem.style.background = "none";
		Self.temporaryItem = parent;
		try {
			var groupItem = document.getElementById("groupItem");
			groupItem.style.display = "none";
			var freeSpace = document.getElementById("freeSpace");
			freeSpace.style.display = "none";
		} catch(e) {}
		if((event.which && event.which > 1) || (event.button && event.button == 2))	{
			//right click in item
			var inItemList = false;
			for (var i = 0 ; i < Self.selectItemList.length; ++ i) {
				if (parent == Self.selectItemList[i]) {
					inItemList = true;
					break;
				}
			}
			if (Self.selectItemList.length > 1 && inItemList) {
				document.getElementById(menuId).style.display = 'none';
				groupItem.style.display = "block";
				var posX = eXo.core.Browser.findMouseRelativeX(groupItem.parentNode, event) ;
				groupItem.style.left = posX + "px";
				var posY = eXo.core.Browser.findMouseRelativeY(groupItem.parentNode, event) ;
				groupItem.style.top = posY + "px";
			} else {
					Self.clearSelectItem();
					Self.selectItemList[0] = parent;
			}
		} else {
				if (event.ctrlKey) {
					if (parent.isSelect) {
						for (var i = 0 ; i < Self.selectItemList.length; ++ i) {
							if (parent == Self.selectItemList[i]) {
								parent.style.background = "none";
								parent.isSelect = null;
								Self.selectItemList.splice(i, 1);
								break;
							}
						}
					} else {
						Self.selectItemList.push(parent);
						parent.isSelect = true;
					}
				} else {
					Self.clearSelectItem();
					Self.selectItemList[0] = parent;
				}
		}
		for (var i in Self.selectItemList) {
			if (Array.prototype[i]) continue;
			Self.selectItemList[i].style.background = "#F8F8F8";
		}
	};
	
	ECMUtils.prototype.selectItem = function(event, bgArea) {
		var groupItem = document.getElementById("groupItem");
		groupItem.style.display = "none";
		var freeSpace = document.getElementById("freeSpace");
		freeSpace.style.display = "none";
		bgArea.holdMouse = false;
		if(((event.which && event.which > 1) || (event.button && event.button == 2))) {
				var freeSpace = document.getElementById("freeSpace");
				freeSpace.style.display = "block";
				var posX = eXo.core.Browser.findMouseRelativeX(freeSpace.parentNode, event) ;
				freeSpace.style.left = posX + "px";
				var posY = eXo.core.Browser.findMouseRelativeY(freeSpace.parentNode, event) ;
				freeSpace.style.top = posY + "px";
		} else {
			bgArea.holdMouse = true;
			//clear all select item
			Self.clearSelectItem();
			var selectArea = document.getElementById("selectArea");
			selectArea.storeX = eXo.core.Browser.findMouseRelativeX(bgArea.parentNode, event);
			selectArea.storeY = eXo.core.Browser.findMouseRelativeY(bgArea.parentNode, event);
			selectArea.style.left = selectArea.storeX + "px";
			selectArea.style.top = selectArea.storeY + "px";
			selectArea.style.width = "0px";
			selectArea.style.height = "0px";
			selectArea.style.border = "1px dotted red";
			var rootElement = DOMUtils.findAncestorByClass(selectArea ,"UIDocumentWorkspace");
			var actionBoxs = DOMUtils.findDescendantsByClass(rootElement, 'div', 'ActionIconBox');
			Self.allActionBox = new Array();
			for(var i = 0 ; i < actionBoxs.length; ++ i) {
				actionBoxs[i].posX = Browser.findPosXInContainer(rootElement, actionBoxs[i]);
				actionBoxs[i].posY = Browser.findPosYInContainer(rootElement, actionBoxs[i]);
				actionBoxs[i].posX = Math.abs(actionBoxs[i].posX);
				actionBoxs[i].posY = Math.abs(actionBoxs[i].posY);  
				Self.allActionBox.push(actionBoxs[i]);
			}
			bgArea.onmouseup = EventHandler.mouseUp; 
			bgArea.onmousemove = EventHandler.mouseMove;
		}
	};
	
	var EventHandler = {
		mouseMove: function(event) {
			var bgArea = this;
			if (bgArea.holdMouse) {
				//select mutiple item by mouse
				var selectArea = document.getElementById("selectArea");
				selectArea.X = Browser.findMouseRelativeX(bgArea.parentNode, event);
				selectArea.Y = Browser.findMouseRelativeY(bgArea.parentNode, event);
				selectArea.deltaX = selectArea.X - selectArea.storeX;
				selectArea.deltaY = selectArea.Y - selectArea.storeY;
				//goc phan tu thu 3
				if (selectArea.deltaX < 0 && selectArea.deltaY < 0) {
					selectArea.style.top = selectArea.Y + "px";
					selectArea.style.left = selectArea.X + "px";
					selectArea.style.width = Math.abs(selectArea.deltaX) + "px";
					selectArea.style.height = Math.abs(selectArea.deltaY) + "px";
					//detect element;
					for (var i = 0; i < Self.allActionBox.length; ++ i) {
						var itemBox = Self.allActionBox[i];
						var posX = itemBox.posX + itemBox.offsetWidth/2;
						var posY = itemBox.posY + itemBox.offsetHeight/2;
						if (selectArea.X < posX && posX < selectArea.storeX &&
								selectArea.Y < posY && posY < selectArea.storeY) {
							itemBox.isSelect = true;
						} else {
							itemBox.isSelect = null;
						}
					}
				//goc phan tu thu 4	
				} else if (selectArea.deltaX < 0 && selectArea.deltaY > 0) {
					selectArea.style.top = selectArea.storeY + "px";
					selectArea.style.left = selectArea.X + "px";
					selectArea.style.width = Math.abs(selectArea.deltaX) + "px";
					selectArea.style.height = selectArea.deltaY + "px";
					//detect element;
					for (var i = 0; i < Self.allActionBox.length; ++ i) {
						var itemBox = Self.allActionBox[i];
						var posX = itemBox.posX + itemBox.offsetWidth/2;
						var posY = itemBox.posY + itemBox.offsetHeight/2;
						if (selectArea.X < posX && posX < selectArea.storeX &&
								posY < selectArea.Y && selectArea.storeY < posY) {
							itemBox.isSelect = true;
						} else {
							itemBox.isSelect = null;
						}
					}
				//goc phan tu thu 2
				} else if (selectArea.deltaX > 0 && selectArea.deltaY < 0) {
					selectArea.style.top = selectArea.Y + "px";
					selectArea.style.left = selectArea.storeX + "px";
					selectArea.style.width = selectArea.deltaX + "px";
					selectArea.style.height = Math.abs(selectArea.deltaY) + "px";
					//detect element;
					for (var i = 0; i < Self.allActionBox.length; ++ i) {
						var itemBox = Self.allActionBox[i];
						var posX = itemBox.posX + itemBox.offsetWidth/2;
						var posY = itemBox.posY + itemBox.offsetHeight/2;
						if ( posX < selectArea.X && selectArea.storeX < posX&&
								selectArea.Y < posY && posY < selectArea.storeY ) {
								itemBox.isSelect = true;
						} else {
							itemBox.isSelect = null;
						}
					}
				//goc phan thu thu 1
				} else {
					selectArea.style.top = selectArea.storeY + "px";
					selectArea.style.left = selectArea.storeX + "px";
					selectArea.style.width = selectArea.deltaX + "px";
					selectArea.style.height = selectArea.deltaY + "px";
					//detect element;
					for (var i = 0; i < Self.allActionBox.length; ++ i) {
						var itemBox = Self.allActionBox[i];
						var posX = itemBox.posX + itemBox.offsetWidth/2;
						var posY = itemBox.posY + itemBox.offsetHeight/2;
						if (selectArea.storeX < posX && posX < selectArea.X &&
								selectArea.storeY < posY && posY < selectArea.Y) {
							itemBox.isSelect = true;
						} else {
							itemBox.isSelect = null;
						}
					}
				}
			}
			for (var i = 0; i < Self.allActionBox.length; ++ i) {
				var itemBox = Self.allActionBox[i];
				if (itemBox.isSelect) {
					itemBox.style.background = "#F8F8F8";
				} else {
					itemBox.style.background = "none";
				}
			}
		},
		mouseUp: function(event) {
			Self.selectItemList = new Array();
			var selectArea = document.getElementById("selectArea");
			var bgArea = this;
			bgArea.holdMouse = false;
			selectArea.storeX = 0;
			selectArea.storeY = 0;
			selectArea.style.top = "0px";
			selectArea.style.left = "0px";
			selectArea.style.width = "0px";
			selectArea.style.height = "0px";
			selectArea.style.border = "none";
			for (var i = 0; i < Self.allActionBox.length; ++ i) {
				var itemBox = Self.allActionBox[i];
				if (itemBox.isSelect) {
					Self.selectItemList.push(itemBox);
				}
			}
		}
	};
	
	ECMUtils.prototype.clearSelectItem = function() {
		for(var i in Self.selectItemList) {
			if (Array.prototype[i]) continue;
			Self.selectItemList[i].isSelect = null;
			Self.selectItemList[i].style.background = "none";
			delete Self.selectItemList[i];
		}
		Self.selectItemList = new Array();
	};
	ECMUtils.prototype.postGroupAction = function(url) {
		var objectId = [];
		var workspaceName = [];
		if(Self.selectItemList && Self.selectItemList.length) {
			for(var i in Self.selectItemList) {
				if (Array.prototype[i]) continue;
				var currentNode = Self.selectItemList[i].childNodes[1];
				currentNode.isSelect = false;
				var wsname = currentNode.getAttribute("workspaceName");
				if (wsname) workspaceName.push(wsname);
				else workspaceName.push("");
				var oid = currentNode.getAttribute("objectId");
				if (oid) objectId.push(oid);
				else objectId.push("");
			}
			url = url.replace("MultiSelection", objectId.join(";") + "&workspaceName=" + workspaceName.join(";"));
			eval(url);
		}
	}	;
	ECMUtils.prototype.concatWithPortal = function() {
		//return;
		eXo.webui.UIRightClickPopupMenu.clickRightMouse =
		Self.concatMethod(eXo.webui.UIRightClickPopupMenu.clickRightMouse, {method: Self.prepareSelectItem});
	};
	
	Self.concatWithPortal();
}

eXo.ecm.ECMUtils = new ECMUtils();




