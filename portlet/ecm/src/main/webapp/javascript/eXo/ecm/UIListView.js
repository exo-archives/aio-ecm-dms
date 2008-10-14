var ListView = function() {

	// eXo.ecm.UIListView
	var Self = this;
	var DOM = eXo.core.DOMUtil;
	
	ListView.prototype.temporaryItem = null;
	ListView.prototype.itemsSelected = [];
	ListView.prototype.allItems = [];
	ListView.prototype.contextMenuId = null;
	ListView.prototype.actionAreaId = null;
	ListView.prototype.enableDragDrop = null;

	//attach all event
	
	ListView.prototype.initAllEvent = function(actionAreaId) {
		Self.contextMenuId = "JCRContextMenu";
		Self.actionAreaId = actionAreaId;
		var actionArea = document.getElementById(actionAreaId);
		Self.allItems = DOM.findDescendantsByClass(actionArea, "div", "RowView");
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
		//apply action drop in tree list
		var UIWorkingArea = DOM.findAncestorByClass(actionArea, "UIWorkingArea");
		var UITreeExplorer = DOM.findFirstDescendantByClass(UIWorkingArea, "div", "UITreeExplorer");
		DOM.getElementsBy(
				function(element) {return element.getAttribute("objectId");},
				"div",
				UITreeExplorer,
				function(element) {element.onmouseup = Self.dropTreeItem;}
		);
	};
	//working with tree list
	ListView.prototype.dropTreeItem = function(event) {
		var event = event || window.event;
		var element = this;
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
	ListView.prototype.mouseOverItem = function(event) {
		var event = event || window.event;
		var element = this;
		if (!element.selected) {
			element.style.background = "#ecffe2";
			element.temporary = true;
		}
	};
	
	ListView.prototype.mouseOutItem = function(event) {
		var event = event || window.event;
		var element = this;
		element.temporary = false;
		if (!element.selected) element.style.background = "none";
	};
	
	ListView.prototype.mouseDownItem = function(event) {
		var event = event || window.event;
		event.cancelBubble = true;
		var element = this;
		Self.enableDragDrop = true;
		var rightClick = (event.which && event.which > 1) || (event.button && event.button == 2);
		var leftClick = !rightClick;
		Self.hideContextMenu();
		
		if (document.getElementById(Self.mobileId)) {
			mobileElement = document.getElementById(Self.mobileId);
			mobileElement.innerHTML = "";
			mobileElement.parentNode.removeChild(mobileElement);
		}
		eXo.webui.UIRightClickPopupMenu.hideContextMenu();
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
			mobileElement.style.padding = "2px";
			mobileElement.style.background = "#fff6a4";
			mobileElement.style.border = "1px solid #ffae00";
			eXo.core.Browser.setOpacity(mobileElement, 72);
			for(var i in Self.itemsSelected) {
				if (Array.prototype[i]) continue;
				mobileElement.appendChild(Self.itemsSelected[i].cloneNode(true));
			}
			document.body.appendChild(mobileElement);
			var actionArea = document.getElementById(Self.actionAreaId);
			mobileElement.style.width = actionArea.offsetWidth + "px";
		}
	};
	
	ListView.prototype.dragItemsSelected = function(event) {
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
	
	ListView.prototype.dropItemsSelected = function(event) {
		var event = event || window.event;
		Self.enableDragDrop = null;
		//use when drop out of action area
		if (document.getElementById(Self.mobileId)) {
				mobileElement = document.getElementById(Self.mobileId);
				mobileElement.parentNode.removeChild(mobileElement);
		}
		document.onmousemove = null;
		document.onmouseup = null;
		document.onselectstart = function(){return true;}
	};
	
	ListView.prototype.clickItem = function(event, element, callback) {
		var event = event || window.event;
		unselect();
		element.selected = true;
		Self.itemsSelected = new Array(element);
		element.style.background = "#ebf5ff";
	};
	
	ListView.prototype.mouseUpItem = function(event) {
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
	ListView.prototype.mouseDownGround = function(event) {
		var event = event || window.event;
		var element = this;
		element.holdMouse = true;
		document.onselectstart = function(){return false};
		var rightClick = (event.which && event.which > 1) || (event.button && event.button == 2);
		var leftClick = !rightClick;
		Self.hideContextMenu();
		if (leftClick) {
			unselect();
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
	
	ListView.prototype.mutipleSelect = function(event) {
		var event = event || window.event;
		var element = this;
		var mask = DOM.findFirstDescendantByClass(element, "div", "Mask");
		
		var top = mask.storeY - 2;
		var right = element.offsetWidth - mask.storeX - 2;
		var bottom = element.offsetHeight - mask.storeY - 2;
		var left = mask.storeX - 2;
		
		if (element.holdMouse) {
				//select mutiple item by mouse
				unselect();
				mask.X = eXo.core.Browser.findMouseRelativeX(element, event);
				mask.Y = eXo.core.Browser.findMouseRelativeY(element, event);
				mask.deltaX = mask.X - mask.storeX;
				mask.deltaY = mask.Y - mask.storeY;
				
				mask.style.width = Math.abs(mask.deltaX) + "px";
				mask.style.height = Math.abs(mask.deltaY) + "px";
				// IV of +
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
				// III of +
				}	else if (mask.deltaX < 0 && mask.deltaY < 0) {
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
					//detect element 
					for (var i in Self.allItems) {
						if (Array.prototype[i]) continue;
						var itemBox = Self.allItems[i];
						var posX = itemBox.posX + itemBox.offsetWidth/2;
						var posY = itemBox.posY + itemBox.offsetHeight/2;
						if (mask.Y < posY && posY < mask.storeY) {
							itemBox.selected = true;
							itemBox.style.background = "#ebf5ff";
						} else {
							itemBox.selected = null;
							itemBox.style.background = "none";
						}
					}
				// II	of +
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
					for (var i in Self.allItems) {
						if (Array.prototype[i]) continue;
						var itemBox = Self.allItems[i];
						var posX = itemBox.posX + itemBox.offsetWidth/2;
						var posY = itemBox.posY + itemBox.offsetHeight/2;
						if (mask.Y < posY && posY < mask.storeY ) {
							itemBox.selected = true;
							itemBox.style.background = "#ebf5ff";
						} else {
							itemBox.selected = null;
							itemBox.style.background = "none";
						}
					}
				// I of +
				} else {
					if (mask.offsetHeight > bottom) {
						mask.style.height = bottom + "px";
					}
					mask.style.top = mask.storeY + "px";	
					if (mask.offsetWidth > right) {
						mask.style.width = right + "px";
					}
					mask.style.left = mask.storeX + "px";
				}
		}
		
	};
	
	ListView.prototype.mouseUpGround = function(event) {
		var event = event || window.event;
		var element = this;
		element.holdMouse = null;
		element.onmousemove = null;
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
	ListView.prototype.showItemContextMenu = function(event, element) {
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
	ListView.prototype.showGroundContextMenu = function(event, element) {
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
	ListView.prototype.hideContextMenu = function() {
		var contextMenu = document.getElementById(Self.contextMenuId);
		if (contextMenu) contextMenu.style.display = "none";
	};
	
	ListView.prototype.postGroupAction = function(url, ext) {
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
	};
	
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

eXo.ecm.UIListView = new ListView();