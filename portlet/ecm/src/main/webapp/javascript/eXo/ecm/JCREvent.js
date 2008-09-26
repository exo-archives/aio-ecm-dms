var JCR = function() {

	// eXo.ecm.JCR
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
		if (Self.initAllEvent && (/function/).test(typeof Self.initAllEvent)) {
			
			Self.contextMenuId = 'ID-' + Math.random().toString().substring(2);
			Self.actionAreaId = actionAreaId;
			Self.initAllEvent = null;
			var actionArea = document.getElementById(actionAreaId);
			actionArea.onmouseover = null;
			actionArea.removeAttribute("onmouseover");
			
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
		}
	};
	
	//event in item
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
			mobileElement.setAttribute("id", 'ID-' + Math.random().toString().substring(2));
			Self.mobileId = mobileElement.id;
			mobileElement.style.position = "absolute";
			mobileElement.style.border = "1px solid red";
			mobileElement.style.display = "none";
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
	}
	
	JCR.prototype.mouseUpItem = function(event) {
		var event = event || window.event;
		event.cancelBubble = true;
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
			if (inArray(Self.itemsSelected, element) && Self.itemsSelected.length > 1){
				Self.showItemContextMenu(event, element);
			} else {
				Self.clickItem(event, element);
				eval(element.getAttribute("mousedown"));
			}
		}
	};
	
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
	
	JCR.prototype.clickItem = function(event, element, callback) {
		var event = event || window.event;
		unselect();
		element.selected = true;
		Self.itemsSelected = new Array(element);
		element.style.background = "#ebf5ff";
	};
	

	
	//event in ground
	JCR.prototype.mouseDownGround = function(event) {
		var event = event || window.event;
		var element = this;
		element.holdMouse = true;
		var rightClick = (event.which && event.which > 1) || (event.button && event.button == 2);
		var leftClick = !rightClick;
		Self.hideContextMenu();
		if (rightClick) {
			Self.showGroundContextMenu(event, element);
		} else {
			unselect();
			element.onmousemove = Self.mutipleSelect;
			var mark = DOM.findFirstDescendantByClass(element, "div", "Mark");
			var eDot = mark.parentNode;
			mark.storeX = eXo.core.Browser.findMouseRelativeX(eDot, event);
			mark.storeY = eXo.core.Browser.findMouseRelativeY(eDot, event);
			mark.style.left = mark.storeX + "px";
			mark.style.top = mark.storeY + "px";
			mark.style.width = "0px";
			mark.style.height = "0px";
			mark.style.border = "1px dotted red";
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
		var mark = DOM.findFirstDescendantByClass(element, "div", "Mark");
		if (element.holdMouse) {
				//select mutiple item by mouse
				unselect();
				var eDot = mark.parentNode;
				mark.X = eXo.core.Browser.findMouseRelativeX(eDot, event);
				mark.Y = eXo.core.Browser.findMouseRelativeY(eDot, event);
				mark.deltaX = mark.X - mark.storeX;
				mark.deltaY = mark.Y - mark.storeY;
				//goc phan tu thu 3
				if (mark.deltaX < 0 && mark.deltaY < 0) {
					mark.style.top = mark.Y + "px";
					mark.style.left = mark.X + "px";
					mark.style.width = Math.abs(mark.deltaX) + "px";
					mark.style.height = Math.abs(mark.deltaY) + "px";
					//detect element 
					for (var i in Self.allItems) {
						if (Array.prototype[i]) continue;
						var itemBox = Self.allItems[i];
						var posX = itemBox.posX + itemBox.offsetWidth/2;
						var posY = itemBox.posY + itemBox.offsetHeight/2;
						if (mark.Y < posY && posY < mark.storeY) {
							itemBox.selected = true;
							itemBox.style.background = "#ebf5ff";
						} else {
							itemBox.selected = null;
							itemBox.style.background = "none";
						}
					}
				//goc phan tu thu 4	
				} else if (mark.deltaX < 0 && mark.deltaY > 0) {
					mark.style.top = mark.storeY + "px";
					mark.style.left = mark.X + "px";
					mark.style.width = Math.abs(mark.deltaX) + "px";
					mark.style.height = mark.deltaY + "px";
				//goc phan tu thu 2
				} else if (mark.deltaX > 0 && mark.deltaY < 0) {
					mark.style.top = mark.Y + "px";
					mark.style.left = mark.storeX + "px";
					mark.style.width = mark.deltaX + "px";
					mark.style.height = Math.abs(mark.deltaY) + "px";
					//detect element;
					for (var i in Self.allItems) {
						if (Array.prototype[i]) continue;
						var itemBox = Self.allItems[i];
						var posX = itemBox.posX + itemBox.offsetWidth/2;
						var posY = itemBox.posY + itemBox.offsetHeight/2;
						if (mark.Y < posY && posY < mark.storeY ) {
							itemBox.selected = true;
							itemBox.style.background = "#ebf5ff";
						} else {
							itemBox.selected = null;
							itemBox.style.background = "none";
						}
					}
				//goc phan thu thu 1
				} else {
					mark.style.top = mark.storeY + "px";
					mark.style.left = mark.storeX + "px";
					mark.style.width = mark.deltaX + "px";
					mark.style.height = mark.deltaY + "px";
				}
		}
		
	};
	
	JCR.prototype.mouseUpGround = function(event) {
		var event = event || window.event;
		var element = this;
		element.holdMouse = null;
		var mark = DOM.findFirstDescendantByClass(element, "div", "Mark");
		mark.style.width = "0px";
		mark.style.height = "0px";
		mark.style.top = "0px";
		mark.style.left = "0px";
		mark.style.border = "none";
		//select item
		var item = null;
		for(var i in Self.allItems) {
			if (Array.prototype[i]) continue;
			item = Self.allItems[i];
			if (item.selected && !inArray(Self.itemsSelected, item)) Self.itemsSelected.push(item);
		}
		element.onmousemove = null;
	} ;
	
	// working with item context menu
	JCR.prototype.showItemContextMenu = function(event, element) {
			if (document.getElementById(Self.contextMenuId)) {
				var contextMenu = document.getElementById(Self.contextMenuId);
				contextMenu.style.position = "absolute";
			} else {
				var contextMenu = document.createElement("div");
				contextMenu.setAttribute("id", Self.contextMenuId);
				contextMenu.style.position = "absolute";
				document.body.appendChild(contextMenu);
			}
			var actionArea = document.getElementById(Self.actionAreaId);
			var context = DOM.findFirstDescendantByClass(actionArea, "div", "ItemContextMenu");
			contextMenu.innerHTML = context.innerHTML;
			contextMenu.style.display = "block";
			var X = eXo.core.Browser.findMouseXInPage(event);
			var Y = eXo.core.Browser.findMouseYInPage(event);
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
			if (document.getElementById(Self.contextMenuId)) {
				var contextMenu = document.getElementById(Self.contextMenuId);
				contextMenu.style.position = "absolute";
			} else {
				var contextMenu = document.createElement("div");
				contextMenu.setAttribute("id", Self.contextMenuId);
				contextMenu.style.position = "absolute";
				document.body.appendChild(contextMenu);
			}
			var actionArea = document.getElementById(Self.actionAreaId);
			var context = DOM.findFirstDescendantByClass(actionArea, "div", "GroundContextMenu");
			contextMenu.innerHTML = context.innerHTML;
			contextMenu.style.display = "block";
			var X = eXo.core.Browser.findMouseXInPage(event);
			var Y = eXo.core.Browser.findMouseYInPage(event);
			contextMenu.style.top = Y + 5 + "px";
			contextMenu.style.left = X + 5 + "px";
	};
	
	// hide contex menu
	JCR.prototype.hideContextMenu = function() {
		var contextMenu = document.getElementById(Self.contextMenuId);
		if (contextMenu) {
			contextMenu.style.display = "none";
			contextMenu.innerHTML = "";
			contextMenu.onmouseup = null;
			document.body.onmousedown = null;
		}
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

eXo.ecm.JCR = new JCR();