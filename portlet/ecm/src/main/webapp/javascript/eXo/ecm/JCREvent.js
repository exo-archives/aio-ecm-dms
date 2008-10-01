var JCR = function() {

	// eXo.ecm.JCREvent
	var Self = this;

	var DOM = eXo.core.DOMUtil;
	
	JCR.prototype.temporaryItem = null;
	JCR.prototype.itemsSelected = [];
	JCR.prototype.allItems = [];
	JCR.prototype.contextMenuId = null;
	JCR.prototype.actionAreaId = null;
	JCR.prototype.enableDragDrop = null;

	//attach all event
	JCR.prototype.initAllEvent = function(actionAreaId) {
		Self.contextMenuId = "JCRContextMenu";
		Self.actionAreaId = actionAreaId;
		var actionArea = document.getElementById(actionAreaId);

		Self.allItems = DOM.findDescendantsByClass(actionArea, "tr", "RowView");
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
	JCR.prototype.mouseOverItem = function(event) {
		var event = event || window.event;
		var element = this;
		if (!element.selected) element.style.background = "#ecffe2";
	};
	
	JCR.prototype.mouseOutItem = function(event) {
		var event = event || window.event;
		var element = this;
		if (!element.selected) element.style.background = "none";
	};
	
	JCR.prototype.mouseDownItem = function(event) {
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
			return;	
			document.onmousemove = Self.dragItemsSelected;
			document.onmouseup = Self.dropItemsSelected;
			//create mobile element
			var mobileElement = document.createElement("div");
			mobileElement.setAttribute("id", 'Id-' + Math.random().toString().substring(2));
			Self.mobileId = mobileElement.id;
			mobileElement.style.position = "absolute";
			mobileElement.style.display = "none";
			mobileElement.style.border = "1px solid red";
			for(var i in Self.itemsSelected) {
				if (Array.prototype[i]) continue;
				mobileElement.appendChild(Self.itemsSelected[i].cloneNode(true));
			}
			document.body.appendChild(mobileElement);
		}
	};
	
	JCR.prototype.dragItemsSelected = function(event) {
			var event = event || window.event;
			var mobileElement = document.getElementById(Self.mobileId);
			if (Self.enableDragDrop && mobileElement) {
				mobileElement.style.display = "block";
				var X = eXo.core.Browser.findMouseXInPage(event);
				var Y = eXo.core.Browser.findMouseYInPage(event);
				mobileElement.style.top = Y + 2 + "px";
				mobileElement.style.left = X + 2 + "px";
			}
	};
	
	JCR.prototype.dropItemsSelected = function(event) {
		if (document.getElementById(Self.mobileId)) {
				mobileElement = document.getElementById(Self.mobileId);
				mobileElement.parentNode.removeChild(mobileElement);
		}
		document.onmousemove = null;
	};
	
	JCR.prototype.clickItem = function(event, element, callback) {
		var event = event || window.event;
		unselect();
		element.selected = true;
		Self.itemsSelected = new Array(element);
		element.style.background = "#ebf5ff";
	};
	
	JCR.prototype.mouseUpItem = function(event) {
		var event = event || window.event;
		var element = this;
		Self.enableDragDrop = null;
		document.onmousemove = null;
		var rightClick = (event.which && event.which > 1) || (event.button && event.button == 2);
		var leftClick = !rightClick;
		
		if (leftClick) {
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
	JCR.prototype.mouseDownGround = function(event) {
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
			mask.style.backgroundColor = "violet";
			eXo.core.Browser.setOpacity(mask, 17);
			//store position for all item
			for( var i = 0 ; i < Self.allItems.length; ++i) {
				Self.allItems[i].posX = Math.abs(eXo.core.Browser.findPosXInContainer(Self.allItems[i], element));
				Self.allItems[i].posY = Math.abs(eXo.core.Browser.findPosYInContainer(Self.allItems[i], element));
			}
		}
	};
	
	JCR.prototype.mutipleSelect = function(event) {
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
				// IV of +
				if (mask.deltaX < 0 && mask.deltaY > 0) {
					mask.style.top = mask.storeY + "px";
					mask.style.left = mask.X + "px";
					mask.style.width = Math.abs(mask.deltaX) + "px";
					mask.style.height = mask.deltaY + "px";
				// III of +
				}	else if (mask.deltaX < 0 && mask.deltaY < 0) {
					mask.style.top = mask.Y + "px";
					mask.style.left = mask.X + "px";
					mask.style.width = Math.abs(mask.deltaX) + "px";
					mask.style.height = Math.abs(mask.deltaY) + "px";
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
					mask.style.top = mask.Y + "px";
					mask.style.left = mask.storeX + "px";
					mask.style.width = mask.deltaX + "px";
					mask.style.height = Math.abs(mask.deltaY) + "px";
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
					mask.style.top = mask.storeY + "px";
					mask.style.left = mask.storeX + "px";
					mask.style.width = mask.deltaX + "px";
					mask.style.height = mask.deltaY + "px";
				}
		}
		
	};
	
	JCR.prototype.mouseUpGround = function(event) {
		var event = event || window.event;
		var element = this;
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
	} ;
	
	// working with item context menu
	JCR.prototype.showItemContextMenu = function(event, element) {
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
	JCR.prototype.showGroundContextMenu = function(event, element) {
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
	JCR.prototype.hideContextMenu = function() {
		var contextMenu = document.getElementById(Self.contextMenuId);
		if (contextMenu) contextMenu.style.display = "none";
	};
	
	JCR.prototype.postGroupAction = function(url) {
		var objectId = [];
		var workspaceName = [];
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
			url = url.replace("MultiSelection", objectId.join(";") + "&workspaceName=" + workspaceName.join(";"));
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

eXo.ecm.JCREvent = new JCR();