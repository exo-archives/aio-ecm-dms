var SimpleView = function() {
	/*
	 * multiply select in JCR Explorer
	 * working with ThumbnailsView.gtmpl
	 */
	
	// eXo.ecm.UISimpleView
	var Self = this;
	var DOM = eXo.core.DOMUtil;
	
	SimpleView.prototype.temporaryItem = null;
	SimpleView.prototype.itemsSelected = [];
	SimpleView.prototype.allItems = [];
	SimpleView.prototype.contextMenuId = null;
	SimpleView.prototype.actionAreaId = null;
	SimpleView.prototype.enableDragDrop = null;
	
	//init event
	SimpleView.prototype.initAllEvent = function(actionAreaId) {
		Self.contextMenuId = "JCRContextMenu";
		Self.actionAreaId = actionAreaId;
		var actionArea = document.getElementById(actionAreaId);
		Self.allItems = DOM.findDescendantsByClass(actionArea, "div", "ActionIconBox");
		var mousedown = null;
		for (var i in Self.allItems) {
			if (Array.prototype[i]) continue;
			if (Self.allItems[i].getAttribute("onmousedown")) {
				mousedown = Self.allItems[i].getAttributeNode("onmousedown").value;
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
		//registry action drag drop in tree list
		var UIWorkingArea = DOM.findAncestorByClass(actionArea, "UIWorkingArea");
		var UITreeExplorer = DOM.findFirstDescendantByClass(UIWorkingArea, "div", "UITreeExplorer");
		DOM.getElementsBy(
				function(element) {return element.getAttribute("objectId");},
				"div",
				UITreeExplorer,
				function(element) {
					if (element.getAttribute("onmousedown")) {
						mousedown = element.getAttributeNode("onmousedown").value;
						element.setAttribute("mousedown", mousedown);
					}
					element.onmousedown = Self.mouseDownTree;
					element.onmouseup = Self.mouseUpTree;
				}
		);
	};
	
	//event in tree list
	SimpleView.prototype.mouseDownTree = function(event) {
		var event = event || window.event;
		var element = this;
		Self.enableDragDrop = true;
		resetArrayItemsSelected();
		
		var rightClick = (event.which && event.which > 1) || (event.button && event.button == 2);
		if (rightClick) {
			eval(element.getAttribute("mousedown"));
		} else {
			// init drag drop;
			document.onmousemove = Self.dragItemsSelected;
			document.onmouseup = Self.dropOutActionArea;
			
			var itemSelected = element.cloneNode(true);
			Self.itemsSelected = new Array(itemSelected);
			
			var uiResizableBlock = DOM.findAncestorByClass(element, "UIResizableBlock");
			uiResizableBlock.style.overflow = "hidden";
			
			//create mobile element
			var mobileElement = document.createElement("div");
			mobileElement.setAttribute("id", DOM.generateId('Id'));
			Self.mobileId = mobileElement.getAttribute('id');
			mobileElement.setAttribute("class", "UIJCRExplorerPortlet");
			mobileElement.style.position = "absolute";
			mobileElement.style.display = "none";
			mobileElement.style.background = "white";
			var coverElement = document.createElement("div");
			coverElement.setAttribute("class", "UITreeExplorer");
			coverElement.style.margin = "3px 3px 0px 3px";
			coverElement.appendChild(itemSelected);
			mobileElement.appendChild(coverElement);
			document.body.appendChild(mobileElement);
		}
	};
	
	SimpleView.prototype.mouseUpTree = function(event) {
		var event = event || window.event;
		var element = this;
		revertResizableBlock();
		Self.enableDragDrop = null;
		
		var mobileElement = document.getElementById(Self.mobileId);
		if (mobileElement && mobileElement.move) {
			//post action
			var actionArea = document.getElementById(Self.actionAreaId);
			var moveAction = DOM.findFirstDescendantByClass(actionArea, "div", "JCRMoveAction");
			var wsTarget = element.getAttribute('workspacename');
			var idTarget = element.getAttribute('objectId');
			Self.postGroupAction(moveAction.getAttribute('request'), "&destInfo="+idTarget+";"+wsTarget);
		}
	};
	
	//event in item
	SimpleView.prototype.mouseOverItem = function(event) {
		var event = event || window.event;
		var element = this;
		if (!element.selected) {
			element.style.background = "#ecffe2";
			element.temporary = true;
		}
	};
	
	SimpleView.prototype.mouseOutItem = function(event) {
		var event = event || window.event;
		var element = this;
		element.temporary = false;
		if (!element.selected) element.style.background = "none";
	};
	
	SimpleView.prototype.mouseDownItem = function(event) {
		var event = event || window.event;
		event.cancelBubble = true;
		var element = this;
		removeMobileElement();
		Self.hideContextMenu();
		Self.enableDragDrop = true;
		var rightClick = (event.which && event.which > 1) || (event.button && event.button == 2);
		if (!rightClick) {
			if (!inArray(Self.itemsSelected, element) && !event.ctrlKey) {
				Self.clickItem(event, element);
			};
			
			// init drag drop;
			document.onmousemove = Self.dragItemsSelected;
			document.onmouseup = Self.dropOutActionArea;
			
			//create mobile element
			var mobileElement = document.createElement("div");
			mobileElement.setAttribute("id", DOM.generateId('Id'));
			Self.mobileId = mobileElement.getAttribute('id');
			mobileElement.setAttribute("class", "UIJCRExplorerPortlet");
			mobileElement.style.position = "absolute";
			mobileElement.style.display = "none";
			mobileElement.style.background = "#fff6a4";
			mobileElement.style.border = "1px solid #f7f7f7";
			eXo.core.Browser.setOpacity(mobileElement, 72);
			var coverElement = document.createElement("div");
			coverElement.setAttribute("class", "UIThumbnailsView");
			coverElement.style.clear = "left";
			for(var i in Self.itemsSelected) {
				if (Array.prototype[i]) continue;
				coverElement.appendChild( Self.itemsSelected[i].cloneNode(true));
			}
			mobileElement.appendChild(coverElement);
			document.body.appendChild(mobileElement);
		}
	};

	SimpleView.prototype.dragItemsSelected = function(event) {
			var event = event || window.event;
			document.onselectstart = function(){return false;}
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
	
	SimpleView.prototype.dropOutActionArea = function(event) {
		var event = event || window.event;
		Self.enableDragDrop = null;
		revertResizableBlock();
		//use when drop out of action area
		if (document.getElementById(Self.mobileId)) {
				mobileElement = document.getElementById(Self.mobileId);
				mobileElement.parentNode.removeChild(mobileElement);
		}
		document.onmousemove = null;
		document.onmouseup = null;
		document.onselectstart = function(){return true;}
	};
	
	SimpleView.prototype.clickItem = function(event, element, callback) {
		var event = event || window.event;
		resetArrayItemsSelected();
		element.selected = true;
		Self.itemsSelected = new Array(element);
		element.style.background = "#ebf5ff";
	};
	
	SimpleView.prototype.mouseUpItem = function(event) {
		var event = event || window.event;
		var element = this;
		Self.enableDragDrop = null;
		document.onmousemove = null;
		revertResizableBlock();
		
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
				Self.postGroupAction(moveAction.getAttribute('request'), "&destInfo="+idTarget+";"+wsTarget);
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
	SimpleView.prototype.mouseDownGround = function(event) {
		var event = event || window.event;
		var element = this;
		element.holdMouse = true;
		Self.hideContextMenu();
		document.onselectstart = function(){return false};
		
		var rightClick = (event.which && event.which > 1) || (event.button && event.button == 2);
		if (rightClick) {
			event.cancelBubble = true;
			Self.showGroundContextMenu(event, element);
		} else {
			resetArrayItemsSelected();
			element.onmousemove = Self.mutipleSelect;
			var mask = DOM.findFirstDescendantByClass(element, "div", "Mask");
			mask.storeX = eXo.core.Browser.findMouseRelativeX(element, event);
			mask.storeY = eXo.core.Browser.findMouseRelativeY(element, event);
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
	
	SimpleView.prototype.mutipleSelect = function(event) {
		var event = event || window.event;
		var element = this;
		var mask = DOM.findFirstDescendantByClass(element, "div", "Mask");
		
		var top = mask.storeY - 2;
		var right = element.offsetWidth - mask.storeX - 2;
		var bottom = element.offsetHeight - mask.storeY - 2;
		var left = mask.storeX - 2;
		
		if (element.holdMouse) {
			resetArrayItemsSelected();
			//select mutiple item by mouse
			mask.X = eXo.core.Browser.findMouseRelativeX(element, event);
			mask.Y = eXo.core.Browser.findMouseRelativeY(element, event);
			mask.deltaX = mask.X - mask.storeX;
			mask.deltaY = mask.Y - mask.storeY;
			
			mask.style.width = Math.abs(mask.deltaX) + "px";
			mask.style.height = Math.abs(mask.deltaY) + "px";
			
			//IV of +
			if (mask.deltaX < 0 && mask.deltaY > 0) {
				if (mask.offsetHeight > bottom) {
					mask.style.height = bottom + "px";
				}
				mask.style.top = mask.storeY + "px";	
				if (mask.offsetWidth > left) {
					mask.style.width = left + "px";
					mask.style.left = 0 + "px";
				} else {
					mask.style.left = mask.X + "px";
				}
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
				if (mask.offsetHeight > top) {
					mask.style.height = top + "px";
					mask.style.top = 0 + "px";
				} else {
					mask.style.top = mask.Y + "px";
				}
				if (mask.offsetWidth > left) {
					mask.style.width = left + "px";
					mask.style.left = 0 + "px";
				} else {
					mask.style.left = mask.X + "px";
				}
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
				if (mask.offsetHeight > top) {
					mask.style.height = top + "px";
					mask.style.top = 0 + "px";
				} else {
					mask.style.top = mask.Y + "px";
				}	
				if (mask.offsetWidth > right) {
					mask.style.width = right + "px";
				} 
				mask.style.left = mask.storeX + "px";
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
				if (mask.offsetHeight > bottom) {
					mask.style.height = bottom + "px";
				}
				mask.style.top = mask.storeY + "px";	
				if (mask.offsetWidth > right) {
					mask.style.width = right + "px";
				}
				mask.style.left = mask.storeX + "px";
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
	
	SimpleView.prototype.mouseUpGround = function(event) {
		var event = event || window.event;
		var element = this;
		element.holdMouse = null;
		element.onmousemove = null;
		revertResizableBlock();
		removeMobileElement();
		Self.enableDragDrop = null;
		document.onselectstart = function(){return true};
		
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
	} ;
	
	// working with item context menu
	SimpleView.prototype.showItemContextMenu = function(event, element) {
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
	SimpleView.prototype.showGroundContextMenu = function(event, element) {
			var event = event || window.event;
			event.cancelBubble = true;
			resetArrayItemsSelected();
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
	SimpleView.prototype.hideContextMenu = function() {
		var contextMenu = document.getElementById(Self.contextMenuId);
		if (contextMenu) contextMenu.style.display = "none";
	};
	
	SimpleView.prototype.postGroupAction = function(url, ext) {
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
	function revertResizableBlock() {
		//revert status overflow for UIResizableBlock;
		var actionArea = document.getElementById(Self.actionAreaId);
		var uiWorkingArea = DOM.findAncestorByClass(actionArea, "UIWorkingArea");
		var uiResizableBlock = DOM.findFirstDescendantByClass(uiWorkingArea, "div", "UIResizableBlock");
		uiResizableBlock.style.overflow = "auto";
	}
	function removeMobileElement() {
			var mobileElement = document.getElementById(Self.mobileId);
			if (mobileElement) document.body.removeChild(mobileElement);
	}
	function resetArrayItemsSelected() {
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

eXo.ecm.UISimpleView = new SimpleView();