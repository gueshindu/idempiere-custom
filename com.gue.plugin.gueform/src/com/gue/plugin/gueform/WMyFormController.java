package com.gue.plugin.gueform;

import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.CustomForm;
import org.adempiere.webui.panel.IFormController;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.North;
import org.zkoss.zul.Textbox;

public class WMyFormController implements IFormController, EventListener<Event>{
	
	private CustomForm m_form = new CustomForm();
	private Textbox txtURL ;
	private Iframe iframe = new Iframe();
	
	public WMyFormController () throws Exception  
	{
		txtURL =  new Textbox();
		//txtURL.setValue("abx");
		init();
	}
	
	private void init()
	{
		Borderlayout mainLayout = new Borderlayout();
		m_form.setStyle("width:101%; height:100%, position:absolote");
		mainLayout.setStyle("width:100%; height:100%, position:absolote");
		North north = new North();
		Center center = new Center();
		iframe.setHflex("true");
		iframe.setVflex("true");
		iframe.setStyle("width:100%; height:100%");
		center.appendChild(iframe);
		txtURL.setStyle("width:90%, height:19px");
		north.appendChild(txtURL);
		mainLayout.appendChild(north);
		mainLayout.appendChild(center);
		m_form.appendChild(mainLayout);
		
		txtURL.addEventListener(Events.ON_OK, this);
		 
		
	}

	@Override
	public void onEvent(Event event) throws Exception {
		/*
		try
		{
			if (event.getTarget().equals(txtURL) && txtURL.getValue().trim().length()>0 ) {
				iframe.setSrc(txtURL.getValue());
			}
		}catch(WrongValueException ex)
		{
			
		}
		
		*/
		
		
	}

	@Override
	public ADForm getForm() {
		// TODO Auto-generated method stub
		return m_form;
	}

}
