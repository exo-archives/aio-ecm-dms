 function ECMUtils() {
	var Self = this;

	//set private property;
	var DOM = eXo.core.DOMUtil;
	var Browser = eXo.core.Browser;
	var RightClick = eXo.webui.UIRightClickPopupMenu;
	
	ECMUtils.prototype.popupArray = [];
	
	ECMUtils.prototype.init = function(portletId) {
		var portlet = document.getElementById(portletId) ;
		if(!portlet) return ;
		RightClick.disableContextMenu(portletId) ;
		portlet.onmousedown = function(event) {
			eXo.ecm.ECMUtils.closeAllPopup() ;
		}
		if(document.getElementById("UIPageDesktop")) {
			Self.fixHeight(portletId) ;
			var uiPageDeskTop = document.getElementById("UIPageDesktop");
			var uiJCRExplorers = DOM.findDescendantsByClass(uiPageDeskTop, 'div', 'UIJCRExplorer') ;
			if (uiJCRExplorers.length) {
				for (var i = 0; i < uiJCRExplorers.length; i++) {
					var uiResizeBlock = DOM.findAncestorByClass(uiJCRExplorers[i], "UIResizableBlock");
					if (uiResizeBlock) uiResizeBlock.style.overflow = "hidden";
				}
			}
		}
	};
	
	ECMUtils.prototype.fixHeight = function(portletId) {
		var portlet = document.getElementById(portletId) ;
		var refElement = DOM.findAncestorByClass(portlet, "UIApplication") ;
		if (refElement == null) return;
		var delta = (parseInt(refElement.style.height) - portlet.offsetHeight);
		var resizeObj = DOM.findDescendantsByClass(portlet, 'div', 'UIResizableBlock') ;
		if(resizeObj.length) {
			for(var i = 0; i < resizeObj.length; i++) {
				resizeObj[i].style.height = (resizeObj[i].offsetHeight + delta) + "px" ;
			}
		}
	};
	
	ECMUtils.prototype.clickLeftMouse = function(event, clickedElement, position, option) {
		var event = event || window.event;
		event.cancelBubble = true;
		popupSelector = DOM.findAncestorByClass(clickedElement, "UIPopupSelector");
		showBlock = DOM.findFirstDescendantByClass(popupSelector,"div", "UISelectContent");
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
		var optsContainer = DOM.findFirstDescendantByClass(vote, "div", "OptionsContainer") ;
		var options = DOM.getChildrenByTagName(optsContainer, "div") ;
		for(var i = 0; i < options.length; i++) {
			options[i].onmouseover = Self.overVote ;
			if(i < rate) options[i].className = "RatedVote" ;
		}
	
		vote.onmouseover = function() {
			var optsCon= DOM.findFirstDescendantByClass(this, "div", "OptionsContainer") ;
			var opts = DOM.getChildrenByTagName(optsCon, "div") ;
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
		var optionsContainer = DOM.findAncestorByClass(this, "OptionsContainer") ;
		var opts = DOM.getChildrenByTagName(optionsContainer, "div") ;
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
		var nodeReference = DOM.findAncestorByClass(elemtClicked,  "ShowHideContainer");
		var elemt = DOM.findFirstDescendantByClass(nodeReference, "div", "ShowHideComponent") ;
	
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
		var subGroup = DOM.findFirstChildByClass(node, "div", "NodeGroup") ;
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
		var subGroup1 = DOM.findFirstChildByClass(node, "div", "NodeGroup1") ;
		var subGroup2 = DOM.findFirstChildByClass(node, "div", "NodeGroup2") ;
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
		    window.location = serverInfo+ "/"+portalName + "/rest/private/lnkproducer/openit.lnk?path=/"+repository +"/" +workspace + path;
	   	} else {
	 	  	window.location = serverInfo + "/"+portalName + "/rest/private/jcr/"+repository +"/" +workspace + nodePath; 		 		
	 	  } 	  
	  } else {
	    window.location = serverInfo+ "/"+portalName + "/rest/private/jcr/"+repository +"/" +workspace + nodePath;
	  } 
	} ;
	
	/*
	 * multiply select in JCR Explorer
	 * working with ThumbnailsView.gtmpl
	 */
	
	ECMUtils.prototype.temporaryItem = null;
	ECMUtils.prototype.itemsSelected = [];
	ECMUtils.prototype.allItems = [];
	ECMUtils.prototype.contextMenuId = null;
	ECMUtils.prototype.actionAreaId = null;
	ECMUtils.prototype.enableDragDrop = null;
	
	ECMUtils.prototype.initAllEvent = function(actionAreaId) {
		Self.contextMenuId = "JCRContextMenu";
		Self.actionAreaId = actionAreaId;
		var actionArea = document.getElementById(actionAreaId);
		Self.allItems = DOM.findDescendantsByClass(actionArea, "div", "ActionIconBox");
		var mousedown = null;
		for (var i in Self.allItems) {
			if (Array.prototype[i]) continue;
			if (Self.allItems[i].hasAttribute("onmousedown")) {
				mousedown = Self.allItems[i].getAttribute("onmousedown");
				Self.allItems[i].setAttribute("mousedown", mousedown);
				Self.allItems[i].onmousedown = null;
				Self.allItems[i].removeAttribute("onmousedown");
			}
			Self.allItems[i].onmouseover = Self.mouseOverItem;
			Self.allItems[i].onmousedown = Self.mouseDownItem;
			Self.allItems[i].onmouseup = Self.mouseUpItem;
			Self.allItems[i].onmouseout = Self.mouseOutItem;
		}
		actionArea.onmousedown = Self.mouseDownGround;
		actionArea.onmouseup = Self.mouseUpGround;
		//remove context menu
		var contextMenu = document.getElementById(Self.contextMenuId);
		if (contextMenu) contextMenu.parentNode.removeChild(contextMenu);
	};
	
	//event in item
	ECMUtils.prototype.mouseOverItem = function(event) {
		var event = event || window.event;
		var element = this;
		if (!element.selected) {
			element.style.background = "#ecffe2";
			element.temporary = true;
		}
	};
	
	ECMUtils.prototype.mouseOutItem = function(event) {
		var event = event || window.event;
		var element = this;
		element.temporary = false;
		if (!element.selected) element.style.background = "none";
	};
	
	ECMUtils.prototype.mouseDownItem = function(event) {
		var event = event || window.event;
		event.cancelBubble = true;
		var element = this;
		Self.enableDragDrop = true;
		var rightClick = (event.which && event.which > 1) || (event.button && event.button == 2);
		var leftClick = !rightClick;
		Self.hideContextMenu();
		
		if (document.getElementById(Self.mobileId)) {
			mobileElement = document.getElementById(Self.mobileId);
			mobileElement.parentNode.removeChild(mobileElement);
		}
		if (leftClick) {
			if (!inArray(Self.itemsSelected, element) && !event.ctrlKey) {
				Self.clickItem(event, element);
			};
			
			// init drag drop;
			document.onmousemove = Self.dragItemsSelected;
			document.onmouseup = Self.dropItemsSelected;
			//create mobile element
			var mobileElement = document.createElement("div");
			mobileElement.setAttribute("id", 'Id-' + Math.random().toString().substring(2));
			Self.mobileId = mobileElement.getAttribute('id');
			mobileElement.style.position = "absolute";
			mobileElement.style.display = "none";
			mobileElement.style.padding = "5px";
			mobileElement.style.width = "92px";
			mobileElement.style.height = "36px";
			mobileElement.style.textAlign = "center";
			mobileElement.style.background = "#fff6a4";
			mobileElement.style.border = "1px solid #ffae00";
			mobileElement.innerHTML = "Items selected: " +  Self.itemsSelected.length;
			document.body.appendChild(mobileElement);
		}
	};
	
	ECMUtils.prototype.dragItemsSelected = function(event) {
			var event = event || window.event;
			var mobileElement = document.getElementById(Self.mobileId);
			if (Self.enableDragDrop && mobileElement && !event.ctrlKey) {
				mobileElement.style.display = "block";
				var X = eXo.core.Browser.findMouseXInPage(event);
				var Y = eXo.core.Browser.findMouseYInPage(event);
				mobileElement.style.top = Y + 5 + "px";
				mobileElement.style.left = X + 5 + "px";
				mobileElement.move = true;
			}
	};
	
	ECMUtils.prototype.dropItemsSelected = function(event) {
		var event = event || window.event;
		Self.enableDragDrop = null;
		//use when drop out of action area
		if (document.getElementById(Self.mobileId)) {
				mobileElement = document.getElementById(Self.mobileId);
				mobileElement.parentNode.removeChild(mobileElement);
		}
		document.onmousemove = null;
		document.onmouseup = null;
	};
	
	ECMUtils.prototype.clickItem = function(event, element, callback) {
		var event = event || window.event;
		unselect();
		element.selected = true;
		Self.itemsSelected = new Array(element);
		element.style.background = "#ebf5ff";
	};
	
	ECMUtils.prototype.mouseUpItem = function(event) {
		var event = event || window.event;
		var element = this;
		Self.enableDragDrop = null;
		document.onmousemove = null;
		var rightClick = (event.which && event.which > 1) || (event.button && event.button == 2);
		var leftClick = !rightClick;
		if (leftClick) {
			var mobileElement = document.getElementById(Self.mobileId);
			if (mobileElement && mobileElement.move && element.temporary) {
				//post action
				var actionArea = document.getElementById(Self.actionAreaId);
				var moveAction = DOM.findFirstDescendantByClass(actionArea, "div", "JCRMoveAction");
				var wsTarget = element.getAttribute('workspacename');
				var idTarget = element.getAttribute('objectId');
				Self.postGroupAction(moveAction.innerHTML, "&dest="+idTarget+";"+wsTarget);
			} else {
				if (event.ctrlKey && !element.selected) {
					element.selected = true;
					Self.itemsSelected.push(element);
				} else if(event.ctrlKey && element.selected) {
					element.selected = null;
					element.style.background = "none";
					removeItem(Self.itemsSelected, element);
				} else {
					Self.clickItem(event, element);
				}
				for(var i in Self.itemsSelected) {
					if (Array.prototype[i]) continue;
					Self.itemsSelected[i].style.background = "#ebf5ff";
				}
			}
		}else {
			event.cancelBubble = true;
			if (inArray(Self.itemsSelected, element) && Self.itemsSelected.length > 1){
				Self.showItemContextMenu(event, element);
			} else {
				Self.clickItem(event, element);
				eval(element.getAttribute("mousedown"));
			}
		}
	};
	
	//event in ground
	ECMUtils.prototype.mouseDownGround = function(event) {
		var event = event || window.event;
		var element = this;
		element.holdMouse = true;
		var rightClick = (event.which && event.which > 1) || (event.button && event.button == 2);
		var leftClick = !rightClick;
		Self.hideContextMenu();
		if (leftClick) {
			unselect();
			element.onmousemove = Self.mutipleSelect;
			var mask = DOM.findFirstDescendantByClass(element, "div", "Mask");
			var eDot = mask.parentNode;
			mask.storeX = eXo.core.Browser.findMouseRelativeX(eDot, event);
			mask.storeY = eXo.core.Browser.findMouseRelativeY(eDot, event);
			mask.style.left = mask.storeX + "px";
			mask.style.top = mask.storeY + "px";
			mask.style.zIndex = 1;
			mask.style.width = "0px";
			mask.style.height = "0px";
			mask.style.border = "1px dotted black";
			mask.style.backgroundColor = "gray";
			eXo.core.Browser.setOpacity(mask, 17);
			//store position for all item
			for( var i = 0 ; i < Self.allItems.length; ++i) {
				Self.allItems[i].posX = Math.abs(eXo.core.Browser.findPosXInContainer(Self.allItems[i], element));
				Self.allItems[i].posY = Math.abs(eXo.core.Browser.findPosYInContainer(Self.allItems[i], element));
			}
		}
	};
	
	ECMUtils.prototype.mutipleSelect = function(event) {
		var event = event || window.event;
		var element = this;
		var mask = DOM.findFirstDescendantByClass(element, "div", "Mask");
		if (element.holdMouse) {
			//select mutiple item by mouse
			unselect();
			var eDot = mask.parentNode;
			mask.X = eXo.core.Browser.findMouseRelativeX(eDot, event);
			mask.Y = eXo.core.Browser.findMouseRelativeY(eDot, event);
			mask.deltaX = mask.X - mask.storeX;
			mask.deltaY = mask.Y - mask.storeY;
			//IV of +
			if (mask.deltaX < 0 && mask.deltaY > 0) {
				mask.style.top = mask.storeY + "px";
				mask.style.left = mask.X + "px";
				mask.style.width = Math.abs(mask.deltaX) + "px";
				mask.style.height = mask.deltaY + "px";
				//detect element;
				for (var i = 0; i < Self.allItems.length; ++ i) {
					var itemBox = Self.allItems[i];
					var posX = itemBox.posX + itemBox.offsetWidth/2;
					var posY = itemBox.posY + itemBox.offsetHeight/2;
					if (mask.X < posX && posX < mask.storeX &&
							posY < mask.Y && mask.storeY < posY) {
						itemBox.selected = true;
						itemBox.style.background = "#ebf5ff";
					} else {
						itemBox.selected = null;
						itemBox.style.background = "none";
					}
				}
			//III of +
			} else if (mask.deltaX < 0 && mask.deltaY < 0) {
				mask.style.top = mask.Y + "px";
				mask.style.left = mask.X + "px";
				mask.style.width = Math.abs(mask.deltaX) + "px";
				mask.style.height = Math.abs(mask.deltaY) + "px";
				//detect element;
				for (var i = 0; i < Self.allItems.length; ++ i) {
					var itemBox = Self.allItems[i];
					var posX = itemBox.posX + itemBox.offsetWidth/2;
					var posY = itemBox.posY + itemBox.offsetHeight/2;
					if (mask.X < posX && posX < mask.storeX &&
							mask.Y < posY && posY < mask.storeY) {
						itemBox.selected = true;
						itemBox.style.background = "#ebf5ff";
					} else {
						itemBox.selected = null;
						itemBox.style.background = "none";
					}
				}
			//II of +
			} else if (mask.deltaX > 0 && mask.deltaY < 0) {
				mask.style.top = mask.Y + "px";
				mask.style.left = mask.storeX + "px";
				mask.style.width = mask.deltaX + "px";
				mask.style.height = Math.abs(mask.deltaY) + "px";
				//detect element;
				for (var i = 0; i < Self.allItems.length; ++ i) {
					var itemBox = Self.allItems[i];
					var posX = itemBox.posX + itemBox.offsetWidth/2;
					var posY = itemBox.posY + itemBox.offsetHeight/2;
					if ( posX < mask.X && mask.storeX < posX&&
							mask.Y < posY && posY < mask.storeY ) {
							itemBox.selected = true;
							itemBox.style.background = "#ebf5ff";
					} else {
						itemBox.selected = null;
						itemBox.style.background = "none";
					}
				}
			//I of +
			} else {
				mask.style.top = mask.storeY + "px";
				mask.style.left = mask.storeX + "px";
				mask.style.width = mask.deltaX + "px";
				mask.style.height = mask.deltaY + "px";
				//detect element;
				for (var i = 0; i < Self.allItems.length; ++ i) {
					var itemBox = Self.allItems[i];
					var posX = itemBox.posX + itemBox.offsetWidth/2;
					var posY = itemBox.posY + itemBox.offsetHeight/2;
					if (mask.storeX < posX && posX < mask.X &&
							mask.storeY < posY && posY < mask.Y) {
						itemBox.selected = true;
						itemBox.style.background = "#ebf5ff";
					} else {
						itemBox.selected = null;
						itemBox.style.background = "none";
					}
				}
			}
		}
	};
	
	ECMUtils.prototype.mouseUpGround = function(event) {
		var event = event || window.event;
		event.cancelBubble = true;
		var element = this;
		Self.enableDragDrop = null;
		element.holdMouse = null;
		element.onmousemove = null;
		
		var mask = DOM.findFirstDescendantByClass(element, "div", "Mask");
		mask.style.width = "0px";
		mask.style.height = "0px";
		mask.style.top = "0px";
		mask.style.left = "0px";
		mask.style.border = "none";
		//collect item
		var item = null;
		for(var i in Self.allItems) {
			if (Array.prototype[i]) continue;
			item = Self.allItems[i];
			if (item.selected && !inArray(Self.itemsSelected, item)) Self.itemsSelected.push(item);
		}
		var rightClick = (event.which && event.which > 1) || (event.button && event.button == 2);
		var leftClick = !rightClick;
		if (rightClick) {
			event.cancelBubble = true;
			Self.showGroundContextMenu(event, element);
		}
		//remove mobile element
		if (document.getElementById(Self.mobileId)) {
				mobileElement = document.getElementById(Self.mobileId);
				mobileElement.parentNode.removeChild(mobileElement);
		}
	} ;
	
	// working with item context menu
	ECMUtils.prototype.showItemContextMenu = function(event, element) {
			var event = event || window.event;
			event.cancelBubble = true;
			if (document.getElementById(Self.contextMenuId)) {
				var contextMenu = document.getElementById(Self.contextMenuId);
				contextMenu.parentNode.removeChild(contextMenu);
			}
			var contextMenu = document.createElement("div");
			contextMenu.setAttribute("id", Self.contextMenuId);
			contextMenu.style.position = "absolute";
			contextMenu.style.height = "0px";
			contextMenu.style.width = "0px";
			contextMenu.style.top = "-1000px";
			contextMenu.style.display = "block";
			document.body.appendChild(contextMenu);

			var actionArea = document.getElementById(Self.actionAreaId);
			var context = DOM.findFirstDescendantByClass(actionArea, "div", "ItemContextMenu");
			contextMenu.innerHTML = context.innerHTML;
			
			//check position popup
			var X = eXo.core.Browser.findMouseXInPage(event);
			var Y = eXo.core.Browser.findMouseYInPage(event);
			var portWidth = eXo.core.Browser.getBrowserWidth();
			var portHeight = eXo.core.Browser.getBrowserHeight();
			var contentMenu = DOM.findFirstChildByClass(contextMenu, "div", "UIRightClickPopupMenu");
			if (event.clientX + contentMenu.offsetWidth > portWidth) X -= contentMenu.offsetWidth;
			if (event.clientY + contentMenu.offsetHeight > portHeight) Y -= contentMenu.offsetHeight + 5;
			contextMenu.style.top = Y + 5 + "px";
			contextMenu.style.left = X + 5 + "px";
			
			//check lock, unlock action
			var checkUnlock = false;
			for (var i in Self.itemsSelected) {
				if (Array.prototype[i]) continue;
				if (Self.itemsSelected[i].getAttribute('locked') == "true") checkUnlock = true;
			}
			var lockAction = DOM.findFirstDescendantByClass(contextMenu, "div", "Lock16x16Icon");
			var unlockAction = DOM.findFirstDescendantByClass(contextMenu, "div", "Unlock16x16Icon");

			if (checkUnlock) {
				unlockAction.parentNode.style.display = "block";
				lockAction.parentNode.style.display = "none";
			} else {
				unlockAction.parentNode.style.display = "none";
				lockAction.parentNode.style.display = "block";
			}
				
			contextMenu.onmouseup = Self.hideContextMenu;
			document.body.onmousedown = Self.hideContextMenu;
	};
	// working with ground context menu
	ECMUtils.prototype.showGroundContextMenu = function(event, element) {
			var event = event || window.event;
			event.cancelBubble = true;
			unselect();
			if (document.getElementById(Self.contextMenuId)) {
				var contextMenu = document.getElementById(Self.contextMenuId);
				contextMenu.parentNode.removeChild(contextMenu);
			}
			var contextMenu = document.createElement("div");
			contextMenu.setAttribute("id", Self.contextMenuId);
			contextMenu.style.position = "absolute";
			contextMenu.style.height = "0px";
			contextMenu.style.width = "0px";
			contextMenu.style.top = "-1000px";
			contextMenu.style.display = "block";
			document.body.appendChild(contextMenu);
			
			var actionArea = document.getElementById(Self.actionAreaId);
			var context = DOM.findFirstDescendantByClass(actionArea, "div", "GroundContextMenu");
			contextMenu.innerHTML = context.innerHTML;
			
			//check position popup
			var X = eXo.core.Browser.findMouseXInPage(event);
			var Y = eXo.core.Browser.findMouseYInPage(event);
			var portWidth = eXo.core.Browser.getBrowserWidth();
			var portHeight = eXo.core.Browser.getBrowserHeight();
			var contentMenu = DOM.findFirstChildByClass(contextMenu, "div", "UIRightClickPopupMenu");
			if (event.clientX + contentMenu.offsetWidth > portWidth) X -= contentMenu.offsetWidth;
			if (event.clientY + contentMenu.offsetHeight > portHeight) Y -= contentMenu.offsetHeight + 5;
			contextMenu.style.top = Y + 5 + "px";
			contextMenu.style.left = X + 5 + "px";
			
			contextMenu.onmouseup = Self.hideContextMenu;
			document.body.onmousedown = Self.hideContextMenu;
	};
	
	// hide context menu
	ECMUtils.prototype.hideContextMenu = function() {
		var contextMenu = document.getElementById(Self.contextMenuId);
		if (contextMenu) contextMenu.style.display = "none";
	};
	
	ECMUtils.prototype.postGroupAction = function(url, ext) {
		var objectId = [];
		var workspaceName = [];
		var ext = ext? ext : "";
		if(Self.itemsSelected.length) {
			for(var i in Self.itemsSelected) {
				if (Array.prototype[i]) continue;
				var currentNode = Self.itemsSelected[i];
				currentNode.isSelect = false;
				var wsname = currentNode.getAttribute("workspaceName");
				if (wsname) workspaceName.push(wsname);
				else workspaceName.push("");
				var oid = currentNode.getAttribute("objectId");
				if (oid) objectId.push(oid);
				else objectId.push("");
			}
			url = url.replace("MultiSelection", objectId.join(";") + "&workspaceName=" + workspaceName.join(";") + ext);
			eval(url);
		}
	}	;
	//private method
	function unselect() {
		for(var i in Self.itemsSelected) {
			if (Array.prototype[i]) continue;
			Self.itemsSelected[i].selected = null;
			Self.itemsSelected[i].style.background = "none";
		}
		Self.itemsSelected = new Array();
	}
	function removeItem(arr, item) {
		for(var i = 0, nSize = arr.length; i < nSize; ++i) {
			if (arr[i] == item) {
				arr.splice(i, 1);
				break;
			}
		}
	}
	function inArray(arr, item) {
		for(var i = 0, nSize = arr.length; i < nSize; ++i) {
				if (arr[i] == item)	return true;
		}
		return false;
	}
};

eXo.ecm.ECMUtils = new ECMUtils();
