function DMSBrowser(){
}


DMSBrowser.prototype.findPosX = function(obj, isRTL) {
  var curleft = 0;
  var tmpObj = obj ;
  while (tmpObj) {
    curleft += tmpObj.offsetLeft ;
    tmpObj = tmpObj.offsetParent ;
  }
  // if RTL return right position of obj
  if(isRTL) return curleft + obj.offsetWidth ;
  return curleft ;
} ;

DMSBrowser.prototype.findMouseRelativeX = function(object, e) {
  var posx = -1 ;
  var posXObject = eXo.ecm.DMSBrowser.findPosX(object) ;
  if (!e) e = window.event ;
  if (e.pageX || e.pageY) {
    posx = e.pageX - posXObject ;
  } else if (e.clientX || e.clientY) {
    posx = e.clientX + document.body.scrollLeft - posXObject ;
  }
  return posx ;
} ;

eXo.ecm.DMSBrowser = new DMSBrowser();