/******************************************************************************
 * Copyright (C) 2009 Low Heng Sin                                            *
 * Copyright (C) 2009 Idalica Corporation                                     *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/
package com.gue.plugin.webui.form;

import java.util.Vector;
import java.util.logging.Level;

import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.apps.form.WCreateFromWindow;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Column;
import org.adempiere.webui.component.Columns;
import org.adempiere.webui.component.ConfirmPanel;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.ListModelTable;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.editor.WEditor;
import org.adempiere.webui.editor.WSearchEditor;
import org.adempiere.webui.editor.WTableDirEditor;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.GridTab;
import org.compiere.model.MColumn;
import org.compiere.model.MLookup;
import org.compiere.model.MLookupFactory;
import org.compiere.model.MPriceList;
import org.compiere.model.MProduct;
import org.compiere.model.MRequisition;

import org.compiere.util.CLogger;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;

/**
 * 
 * @author GueShindu
 * @baseon: Fabian Aguilar faaguilar@gmail.com
 *
 */
public class WCreateFromOrderUI extends CreateFromOrder implements EventListener<Event>
{
	private WCreateFromWindow window;
	
	public WCreateFromOrderUI(GridTab tab) 
	{
		super(tab);
		log.info(getGridTab().toString());
		
		window = new WCreateFromWindow(this, getGridTab().getWindowNo());
		
		p_WindowNo = getGridTab().getWindowNo();

		try
		{
			if (!dynInit())
				return;
			zkInit();
			setInitOK(true);
		}
		catch(Exception e)
		{
			log.log(Level.SEVERE, "", e);
			setInitOK(false);
		}
		AEnv.showWindow(window);
	}
	
	/** Window No               */
	private int p_WindowNo;

	/**	Logger			*/
	private CLogger log = CLogger.getCLogger(getClass());
	
	//add by shindu
	//31 aug 2021
	protected Label PriceList_label = new Label(Msg.translate(Env.getCtx(), "PriceList"));
	protected Label PriceList_value = new Label("");
	protected Label product_label = new Label(Msg.translate(Env.getCtx(), "Product"));
	protected WEditor productLookup;
	//end shindu
	
	protected Label Requisition_Label = new Label(Msg.translate(Env.getCtx(), "Requisition"));
	protected WEditor requisitionLookup;
	
	protected Label User_Label = new Label(Msg.translate(Env.getCtx(), "User"));
	protected WEditor userLookup;
	
	protected Label Org_Label = new Label(Msg.translate(Env.getCtx(), "Org"));
	protected WTableDirEditor orgLookup;
	
	/**
	 *  Dynamic Init
	 *  @throws Exception if Lookups cannot be initialized
	 *  @return true if initialized
	 */
	public boolean dynInit() throws Exception
	{
		log.config("");
		
		super.dynInit();
		//Refresh button
				Button refreshButton = window.getConfirmPanel().createButton(ConfirmPanel.A_REFRESH);
				refreshButton.addEventListener(Events.ON_CLICK, this);
				window.getConfirmPanel().addButton(refreshButton);				
		if (getGridTab().getValue("C_Order_ID") == null)
		{
			FDialog.error(0, window, "SaveErrorRowNotFound");
			return false;
		}
		if (getGridTab().getValueAsBoolean("IsSOTrx"))
		{
			FDialog.error(p_WindowNo, window, "Only Purchase Order");
			return false;
		}
		
		window.setTitle("Create From Requisition");
		
		MLookup lookup = MLookupFactory.get (Env.getCtx(), p_WindowNo, 0, MColumn.getColumn_ID(MRequisition.Table_Name, MRequisition.COLUMNNAME_M_Requisition_ID), DisplayType.Search);
		requisitionLookup = new WSearchEditor ("M_Requisition_ID", false, false, true, lookup);
		
		lookup = MLookupFactory.get (Env.getCtx(), p_WindowNo, 0, MColumn.getColumn_ID(MRequisition.Table_Name, MRequisition.COLUMNNAME_AD_User_ID), DisplayType.Search);
		userLookup = new WSearchEditor ("AD_User_ID", false, false, true, lookup);
		
		lookup = MLookupFactory.get (Env.getCtx(), p_WindowNo, 0, MColumn.getColumn_ID(MRequisition.Table_Name, MRequisition.COLUMNNAME_AD_Org_ID), DisplayType.TableDir);
		orgLookup = new WTableDirEditor ("AD_Org_ID",false,false,true,lookup);
		orgLookup.setValue(getGridTab().getValue("AD_Org_ID")); // default
		orgLookup.getComponent().addEventListener(Events.ON_CHANGE, this);
		
		//add by shindu 1 sept 2021
		//product lookup
		lookup = MLookupFactory.get (Env.getCtx(), p_WindowNo, 0, MColumn.getColumn_ID(MProduct.Table_Name, MProduct.COLUMNNAME_M_Product_ID), DisplayType.Search);
		productLookup = new WSearchEditor ("M_Product_ID", false, false, true, lookup);
		//
		
		return true;
	}   //  dynInit
	
	protected void zkInit() throws Exception
	{
		Requisition_Label.setText(Msg.getElement(Env.getCtx(), "M_Requisition_ID"));
		User_Label.setText(Msg.getElement(Env.getCtx(), "AD_User_ID", false));
		Org_Label.setText(Msg.getElement(Env.getCtx(), "AD_Org_ID", false));
        
        
		Borderlayout parameterLayout = new Borderlayout();
		parameterLayout.setHeight("130px");
		parameterLayout.setWidth("100%");
    	Panel parameterPanel = window.getParameterPanel();
		parameterPanel.appendChild(parameterLayout);
		
		Grid parameterStdLayout = GridFactory.newGridLayout();
    	Panel parameterStdPanel = new Panel();
		parameterStdPanel.appendChild(parameterStdLayout);

		Center center = new Center();
		parameterLayout.appendChild(center);
		center.appendChild(parameterStdPanel);
		
		Columns columns = new Columns();
		parameterStdLayout.appendChild(columns);
		Column column = new Column();
		columns.appendChild(column);		
		column = new Column();
		column.setWidth("15%");
		columns.appendChild(column);
		column.setWidth("35%");
		column = new Column();
		column.setWidth("15%");
		columns.appendChild(column);
		column = new Column();
		column.setWidth("35%");
		columns.appendChild(column);
		
		
		Rows rows = (Rows) parameterStdLayout.newRows();
		Row row = rows.newRow();
		//add by shindu
		//
		MPriceList pl = new MPriceList(Env.getCtx(), Env.getContextAsInt(Env.getCtx(), p_WindowNo, "M_PriceList_ID"), null);
		if (pl != null) 
		{
			row.appendChild(PriceList_label.rightAlign());
			PriceList_value.setText(pl.getName());
			row.appendChild( PriceList_value);
		}
		row = rows.newRow();
		row.appendChild(product_label.rightAlign());
		row.appendChild(productLookup.getComponent());
		row = rows.newRow();
		//end by shindu
		//
		row.appendChild(Requisition_Label.rightAlign());
		row.appendChild(requisitionLookup.getComponent());
		row.appendChild(Org_Label.rightAlign());
		row.appendChild(orgLookup.getComponent());
		row = rows.newRow();
		row.appendChild(User_Label.rightAlign());
		row.appendChild(userLookup.getComponent());
		
	}

	/**
	 *  Action Listener
	 *  @param e event
	 * @throws Exception 
	 */
	public void onEvent(Event e) throws Exception
	{
		if (log.isLoggable(Level.CONFIG)) log.config("Action=" + e.getTarget().getId());
		if(e.getTarget().equals(window.getConfirmPanel().getButton(ConfirmPanel.A_REFRESH)))
		{
			loadRequisition();
			window.tableChanged(null);
		}
	}

	/**
	 *  Load Data - Order
	 *  @param C_Order_ID Order
	 *  @param forInvoice true if for invoice vs. delivery qty
	 */
	protected void loadRequisition ()
	{
		loadTableOIS(getRequisitionData(requisitionLookup.getValue(), orgLookup.getValue(), 
				userLookup.getValue(), productLookup.getValue()));
	}   //  LoadOrder
	
	/**
	 *  Load Order/Invoice/Shipment data into Table
	 *  @param data data
	 */
	protected void loadTableOIS (Vector<?> data)
	{
		window.getWListbox().clear();
		
		//  Remove previous listeners
		window.getWListbox().getModel().removeTableModelListener(window);
		//  Set Model
		ListModelTable model = new ListModelTable(data);
		model.addTableModelListener(window);
		window.getWListbox().setData(model, getOISColumnNames());
		//
		
		configureMiniTable(window.getWListbox());
	}   //  loadOrder
	
	public void showWindow()
	{
		window.setVisible(true);
	}
	
	public void closeWindow()
	{
		window.dispose();
	}

	@Override
	public Object getWindow() {
		return window;
	}
}
