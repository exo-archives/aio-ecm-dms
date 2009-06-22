function UIFormGeneratorPortlet() {
	this.inputText  = "<div class='InputComponent'><div class='Property Name'>Property Name : <Input type='text' value='Title' name='PropertyName'></div>";
	this.inputText += "<div class='Property CheckBoxMandatory'><input type='checkbox' name='mandatory' value='mandatory'>Select this checkbox is you want this property to be mandatory</input></div>";
	this.inputText += "<div class='Property OptionContainer'>Size : <input type='number' name='size' value='20'/>Max Length <input type='number' name='MaxLength' value-'20' />Type :<select name='InputType' id='InputType'><option value='None' name='None'>None</option><option value='Text' name='Text'>Text</option><option value='Number' name='Number'>Number</option><option value='File' name='File'>File</option></select></div>";
	this.content = "<div class='ContentComponent'><div class='CheckBoxEditor'><input type='checkbox' id='CheckBoxEditor' onclick='eXo.ecm.UIFormGenerator.showHideFCKEditor()' />Rick Text Editor</div><div class='txtContent'><textarea id='TextArea' name='TextArea'>Type content here...</textarea></div></div>"
	this.label = "<div class='LabelComponent'><div class='LabelName'>Label Name : <input type='text' value=''></div></div>";
}

UIFormGeneratorPortlet.prototype.initForm = function() {
	var leftContainer = document.getElementById('LeftContainer');
	var components = eXo.core.DOMUtil.findDescendantsByClass(leftContainer, "div", "UIComponentItem");
	var len = components.length ;
	for(var i =0; i < len; i++) {
		alert(components[i].className);	
		components[i].onclick = function() {
			eXo.ecm.UIFormGeneratorPortlet.insertComponent(this.getAttribute('type'));
		}
	}
}

UIFormGeneratorPortlet.prototype.insertComponent = function(type) {
	switch(type){
		case "InputText"	: this.addComponent(this.inputText) ; break;
		case "Select"		: this.addComponent(this.selectBox) ; break;
		case "Content"		: this.addComponent(this.content)	 ; break;
	}
};

UIFormGeneratorPortlet.prototype.addComponent = function(html) {
	var node = document.createElement("div");
	node.className = "RightComponentItem";
	node.style.width = "100%";
	node.style.border = "solid 1px #000";
	node.style.marginTop = "10px";
	node.innerHTML = html ;
	document.getElementById("DropContainer").appendChild(node);
};

UIFormGeneratorPortlet.prototype.showHideFCKEditor = function(obj) {
	var chkEditor = document.getElementById('CheckBoxEditor');
	var txtArea = document.getElementById('TextAreaFormGenerator');
	if(chkEditor.checked) {
		var oFCKEditor = new FCKeditor('TextAreaFormGenerator');; 
		oFCKEditor.BasePath	= 'fckeditor/';
		oFCKEditor.ReplaceTextarea();
	} else {
		var editorInstance = FCKeditorAPI.GetInstance('TextAreaFormGenerator');
		editorInstance.UpdateLinkedField();
		var editorContainer = document.getElementById('TextAreaFormGenerator___Frame');
		if(editorContainer) 	editorContainer.style.display = 'none';
		var textareaContainer = document.getElementById('TextAreaFormGenerator');
		textareaContainer.style.display = '';
	} 
};

UIFormGeneratorPortlet.prototype.createJSONObject = function(data) {
	
}

eXo.ecm.UIFormGeneratorPortlet = new UIFormGeneratorPortlet();