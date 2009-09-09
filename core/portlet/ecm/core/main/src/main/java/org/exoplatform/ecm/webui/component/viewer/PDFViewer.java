/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ecm.webui.component.viewer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.PInfo;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Aug 18, 2009  
 * 3:49:41 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/viewer/PDFViewer.gtmpl",
    events = {
        @EventConfig(listeners = PDFViewer.NextPageActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = PDFViewer.PreviousPageActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = PDFViewer.GotoPageActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = PDFViewer.RotateRightPageActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = PDFViewer.RotateLeftPageActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = PDFViewer.ScalePageActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = PDFViewer.DownloadFileActionListener.class, phase = Phase.DECODE)
    }
)
/**
 * PDF Viewer component which will be used to display PDF file on web browser
 */
public class PDFViewer extends UIForm {

  final static private String PAGE_NUMBER = "pageNumber";
  final static private String SCALE_PAGE = "scalePage";
  
  private int currentPageNumber_ = 1;
  private int maximumOfPage_ = 0;
  private float currentRotation_ = 0.0f;
  private float currentScale_ = 1.0f;
  private Map<String, String> metadatas = new HashMap<String, String>();
  
  public PDFViewer() throws Exception {
    addUIFormInput(new UIFormStringInput(PAGE_NUMBER, PAGE_NUMBER, "1"));
    UIFormSelectBox uiScaleBox = new UIFormSelectBox(SCALE_PAGE, SCALE_PAGE, initScaleOptions());
    uiScaleBox.setOnChange("ScalePage");
    addUIFormInput(uiScaleBox);
    uiScaleBox.setValue("1.0f");
  }
  
  public Method getMethod(UIComponent uiComponent, String name) {
    Method[] arrMethod = uiComponent.getClass().getMethods();
    for(Method method : arrMethod) {
      if(method.getName().equals(name)) {
        return method;
      }
    }
    return null;
  }
  
  public void initDatas() throws Exception {
    UIComponent uiParent = getParent();
    Method method = getMethod(uiParent, "getOriginalNode");
    Node originalNode = null;
    if(method != null) originalNode = (Node) method.invoke(uiParent, null);
    
    if(originalNode != null) {
      Document document = getDocument(originalNode);
      if(document != null) maximumOfPage_ = document.getNumberOfPages();
      metadatas.clear();
      putDocumentInfo(document.getInfo());
      document.dispose();
    }
    
  }
  
  public Map getMetadataExtraction() { return metadatas; }
  
  public int getMaximumOfPage() throws Exception { 
    if(maximumOfPage_ == 0) initDatas();
    return maximumOfPage_; 
  }
  
  public float getCurrentRotation() { return currentRotation_; }
  
  public void setRotation(float rotation) { currentRotation_ = rotation; }
  
  public float getCurrentScale() { return currentScale_; }
  
  public void setScale(float scale) { currentScale_ = scale; }
  
  public int getPageNumber() { return currentPageNumber_; }
  
  public void setPageNumber(int pageNum) { currentPageNumber_ = pageNum; };

  private Document getDocument(Node node) throws RepositoryException {
    if(!node.hasNode(Utils.JCR_CONTENT)) return null;
    Document document = new Document();
    Node contentNode = node.getNode(Utils.JCR_CONTENT);
    try {
      InputStream input = contentNode.getProperty(Utils.JCR_DATA).getStream() ;      
      document.setInputStream(input, node.getPath());
    } catch (PDFException ex) {
      System.out.println("Error parsing PDF document " + ex);
    } catch (PDFSecurityException ex) {
      System.out.println("Error encryption not supported " + ex);
    } catch (FileNotFoundException ex) {
      System.out.println("Error file not found " + ex);
    } catch (IOException ex) {
      System.out.println("Error handling PDF document " + ex);
    }
    return document;
  }
  
  private void putDocumentInfo(PInfo documentInfo) {
    if (documentInfo != null) {
      if(documentInfo.getTitle() != null && documentInfo.getTitle().length() > 0) {
        metadatas.put("title", documentInfo.getTitle());
      }
      if(documentInfo.getAuthor() != null && documentInfo.getAuthor().length() > 0) {
        metadatas.put("author", documentInfo.getAuthor());
      }
      if(documentInfo.getSubject() != null && documentInfo.getSubject().length() > 0) {
        metadatas.put("subject", documentInfo.getSubject());
      }
      if(documentInfo.getKeywords() != null && documentInfo.getKeywords().length() > 0) {
        metadatas.put("keyWords", documentInfo.getKeywords());
      }
      if(documentInfo.getCreator() != null && documentInfo.getCreator().length() > 0) {
        metadatas.put("creator", documentInfo.getCreator());
      }
      if(documentInfo.getProducer() != null && documentInfo.getProducer().length() > 0) {
        metadatas.put("producer", documentInfo.getProducer());
      }
      if(documentInfo.getCreationDate() != null) {
        metadatas.put("creationDate", documentInfo.getCreationDate().toString());
      }
      if(documentInfo.getModDate() != null) {
        metadatas.put("modDate", documentInfo.getModDate().toString());
      }      
    }
  }
  
  private List<SelectItemOption<String>> initScaleOptions() {
    List<SelectItemOption<String>> scaleOptions = new ArrayList<SelectItemOption<String>>();
    scaleOptions.add(new SelectItemOption<String>("5%",  "0.05f"));
    scaleOptions.add(new SelectItemOption<String>("10%",  "0.1f"));
    scaleOptions.add(new SelectItemOption<String>("25%",  "0.25f"));
    scaleOptions.add(new SelectItemOption<String>("50%",  "0.5f"));
    scaleOptions.add(new SelectItemOption<String>("75%",  "0.75f"));
    scaleOptions.add(new SelectItemOption<String>("100%",  "1.0f"));
    scaleOptions.add(new SelectItemOption<String>("125%",  "1.25f"));
    scaleOptions.add(new SelectItemOption<String>("150%",  "1.5f"));
    scaleOptions.add(new SelectItemOption<String>("200%",  "2.0f"));
    scaleOptions.add(new SelectItemOption<String>("300%",  "3.0f"));
    return scaleOptions;
  }
  
  static public class PreviousPageActionListener extends EventListener<PDFViewer> {
    public void execute(Event<PDFViewer> event) throws Exception {     
      PDFViewer pdfViewer = event.getSource();
      if(pdfViewer.currentPageNumber_ == 1) {
        pdfViewer.getUIStringInput(PAGE_NUMBER).setValue(
            Integer.toString((pdfViewer.currentPageNumber_)));
      } else {
        pdfViewer.getUIStringInput(PAGE_NUMBER).setValue(
            Integer.toString((pdfViewer.currentPageNumber_ -1)));
      }
      pdfViewer.setPageNumber(pdfViewer.currentPageNumber_ - 1);
      event.getRequestContext().addUIComponentToUpdateByAjax(pdfViewer);
    }
  }
  
  static public class NextPageActionListener extends EventListener<PDFViewer> {
    public void execute(Event<PDFViewer> event) throws Exception {     
      PDFViewer pdfViewer = event.getSource();
      if(pdfViewer.currentPageNumber_ == pdfViewer.maximumOfPage_) {
        pdfViewer.getUIStringInput(PAGE_NUMBER).setValue(
            Integer.toString((pdfViewer.currentPageNumber_)));
      } else {
        pdfViewer.getUIStringInput(PAGE_NUMBER).setValue(
            Integer.toString((pdfViewer.currentPageNumber_ + 1)));
      }
      pdfViewer.setPageNumber(pdfViewer.currentPageNumber_ + 1);
      event.getRequestContext().addUIComponentToUpdateByAjax(pdfViewer);
    }
  }

  static public class GotoPageActionListener extends EventListener<PDFViewer> {
    public void execute(Event<PDFViewer> event) throws Exception {     
      PDFViewer pdfViewer = event.getSource();
      String pageStr = pdfViewer.getUIStringInput(PAGE_NUMBER).getValue();
      int pageNumber = 1;
      try {
        pageNumber = Integer.parseInt(pageStr);
      } catch(NumberFormatException e) {
        pageNumber = pdfViewer.currentPageNumber_;
      }
      if(pageNumber >= pdfViewer.maximumOfPage_) pageNumber = pdfViewer.maximumOfPage_;
      else if(pageNumber < 1) pageNumber = 1;
      pdfViewer.getUIStringInput(PAGE_NUMBER).setValue(Integer.toString((pageNumber)));
      pdfViewer.setPageNumber(pageNumber);
      event.getRequestContext().addUIComponentToUpdateByAjax(pdfViewer);
    }
  }

  static public class RotateRightPageActionListener extends EventListener<PDFViewer> {
    public void execute(Event<PDFViewer> event) throws Exception {     
      PDFViewer pdfViewer = event.getSource();
      pdfViewer.setRotation(pdfViewer.currentRotation_ + 270.0f);
      event.getRequestContext().addUIComponentToUpdateByAjax(pdfViewer);
    }
  }
 
  static public class RotateLeftPageActionListener extends EventListener<PDFViewer> {
    public void execute(Event<PDFViewer> event) throws Exception {     
      PDFViewer pdfViewer = event.getSource();
      pdfViewer.setRotation(pdfViewer.currentRotation_ + 90.0f);
      event.getRequestContext().addUIComponentToUpdateByAjax(pdfViewer);
    }
  }
  
  static public class ScalePageActionListener extends EventListener<PDFViewer> {
    public void execute(Event<PDFViewer> event) throws Exception {     
      PDFViewer pdfViewer = event.getSource();
      String scale = pdfViewer.getUIFormSelectBox(SCALE_PAGE).getValue();
      pdfViewer.setScale(Float.parseFloat(scale));
      event.getRequestContext().addUIComponentToUpdateByAjax(pdfViewer);
    }
  }

  static public class DownloadFileActionListener extends EventListener<PDFViewer> {
    public void execute(Event<PDFViewer> event) throws Exception {     
      PDFViewer pdfViewer = event.getSource();
      UIComponent uiParent = pdfViewer.getParent();
      Method methodGetNode = pdfViewer.getMethod(uiParent, "getNode");
      if(methodGetNode != null) {
        Method methodDownloadLink = pdfViewer.getMethod(uiParent, "getDownloadLink");
        String downloadLink = 
          (String)methodDownloadLink.invoke(uiParent, (Node)methodGetNode.invoke(uiParent, null));
        event.getRequestContext().getJavascriptManager().addCustomizedOnLoadScript(
            "ajaxRedirect('" + downloadLink + "');");
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(pdfViewer);
    }
  }
  
}
