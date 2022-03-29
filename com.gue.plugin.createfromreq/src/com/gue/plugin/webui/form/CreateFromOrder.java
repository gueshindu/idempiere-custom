package com.gue.plugin.webui.form;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Vector;
import java.util.logging.Level;

import org.compiere.apps.IStatusBar;
import org.compiere.grid.CreateFrom;
import org.compiere.minigrid.IMiniTable;
import org.compiere.model.GridTab;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MRequisitionLine;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;

/**
 * 
 * 
 * @author GueShindu
 * @baseon: Fabian Aguilar faaguilar@gmail.com
 */

public abstract class CreateFromOrder extends CreateFrom
{
	/**
	 *  Protected Constructor
	 *  @param mTab MTab
	 */
	public CreateFromOrder(GridTab mTab)
	{
		super(mTab);
		if (log.isLoggable(Level.INFO)) log.info(mTab.toString());
	}   //  VCreateFromInvoice

	/**
	 *  Dynamic Init
	 *  @return true if initialized
	 */
	public boolean dynInit() throws Exception
	{
		log.config("");
		setTitle(Msg.getElement(Env.getCtx(), "C_Order_ID", false) + " .. " + Msg.translate(Env.getCtx(), "CreateFrom"));

		return true;
	}   //  dynInit


	/**
	 *  Load Data - Shipment not invoiced
	 *  @param M_InOut_ID InOut
	 */
	protected Vector<Vector<Object>> getRequisitionData(Object Requisition, Object Org,  Object User, Object product)
	{
		int M_PriceListID = Env.getContextAsInt(Env.getCtx(), getGridTab().getWindowNo(), "M_PriceList_ID");
		log.log(Level.INFO, "Price list id: " + M_PriceListID);


		//
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		StringBuilder sql = new StringBuilder(); 
		
		//Modified query by shindu\
		//30 aug 2021
		//filter data by price list
		sql.append("SELECT r.M_Requisition_ID,r.DocumentNo,r.DateRequired,r.PriorityRule,rl.M_Product_ID, " +  //1-5 
				"      p.Name as ProductName,rl.Description,rl.Qty,rl.C_BPartner_ID, bp.Name as BpName, rl.M_RequisitionLine_ID, u.Name as Username, o.Name as OrgName," + // 6-13 
				"		c.C_Charge_ID, c.name as ChargeName " + //14-15 			 
				"FROM  M_Requisition r " + 
				"		INNER JOIN M_RequisitionLine rl on (r.m_requisition_id=rl.m_requisition_id) " + 
				"       INNER JOIN AD_User u on (r.AD_User_ID=u.AD_User_ID)" + 
				" 		INNER JOIN AD_Org o on (r.AD_Org_ID=o.AD_Org_ID)" + 
				" 		INNER JOIN m_productprice prc ON (rl.m_product_id = prc.m_product_id)" + 
				" 		INNER JOIN m_pricelist_version plv ON (prc.m_pricelist_version_id = plv.m_pricelist_version_id)" + 
				"		LEFT OUTER JOIN M_Product p on (rl.M_Product_ID=p.M_Product_ID)" + 
				"		LEFT OUTER JOIN C_Charge c on (rl.C_Charge_ID=c.C_Charge_ID)" + 
				"		LEFT OUTER JOIN C_BPartner bp on (rl.C_BPartner_ID=bp.C_BPartner_ID)" + 
				"WHERE r.docstatus='CO' AND rl.C_OrderLine_ID is null AND plv.m_pricelist_id = " + M_PriceListID) ;
		//end by shindu
		
		if(Requisition!=null)
			sql.append(" AND rl.M_Requisition_ID=?");
		if(Org!=null)
			sql.append(" AND r.AD_Org_ID=?");
		if(User!=null)
			sql.append(" AND r.AD_User_ID=?");
		
		//Add by shindu 1 sep 2021
		if(product!=null)
			sql.append(" AND rl.M_Product_ID=?");
		//end by shindu
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			int i=1;
			pstmt = DB.prepareStatement(sql.toString(), null);
			if(Requisition!=null)
				pstmt.setInt(i++, (Integer)Requisition);
			if(Org!=null)
				pstmt.setInt(i++, (Integer)Org);
			if(User!=null)
				pstmt.setInt(i++, (Integer)User);
			//
			//add by shindu 1 sep 2021
			if(product!=null)
				pstmt.setInt(i++, (Integer)product);
			//end by shindu
			
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				Vector<Object> line = new Vector<Object>(7);
				line.add(new Boolean(false));           //  0-Selection
				line.add(rs.getString(13)); //1-OrgName
				
				KeyNamePair pp =  new KeyNamePair(rs.getInt(11), rs.getString(2).trim());
				line.add(pp);  //  2-DocumentNo Line iD
				line.add(rs.getTimestamp(3));//  3-DateRequired
				
				if(rs.getString(10)!=null)
				{
					pp = new KeyNamePair(rs.getInt(9), rs.getString(10).trim());
					line.add(pp);	//  4-BPartner
				}
				else
					line.add(null); //  4-BPartner
			
				if( rs.getString(6)!=null)
				{
				pp = new KeyNamePair(rs.getInt(5), rs.getString(6).trim());
				line.add(pp);				// 5-Product
				}
				else
				line.add(null);      // 5-Product
				
				if(rs.getString(15)!=null)
					line.add(rs.getString(15)); //6-charge
				else
					line.add(null); //6-charge
				
				line.add(rs.getBigDecimal(8));//7- Qty
		
				line.add(rs.getString(7));                           //  8-description
				line.add(rs.getString(12).trim());                     	//  9-user
				data.add(line);
			}
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}

		return data;
	}   //  loadShipment


	/**
	 *  List number of rows selected
	 */
	public void info(IMiniTable miniTable, IStatusBar statusBar)
	{

	}   //  infoInvoice

	protected void configureMiniTable (IMiniTable miniTable)
	{
		miniTable.setColumnClass(0, Boolean.class, false);      //  0-Selection
		miniTable.setColumnClass(1, String.class, true);        //  1-OrgName
		miniTable.setColumnClass(2, String.class, true);        //  2-DocumentNo
		miniTable.setColumnClass(3, Timestamp.class, true);        //  3-DateRequired
		miniTable.setColumnClass(4, String.class, true);        //  4-BPartner
		miniTable.setColumnClass(5, String.class, true);        //  5-Product
		miniTable.setColumnClass(6, String.class, true);        //  6-Charge
		miniTable.setColumnClass(7, BigDecimal.class, true);        //  7-Qty
		miniTable.setColumnClass(8, String.class, true);        //  8-Description
		miniTable.setColumnClass(9, String.class, true);        //  9-User
		
		//  Table UI
		miniTable.autoSize();
	}

	/**
	 *  Save - Create Invoice Lines
	 *  @return true if saved
	 */
	public boolean save(IMiniTable miniTable, String trxName)
	{
		//  Order
		int C_Order_ID = ((Integer)getGridTab().getValue("C_Order_ID")).intValue();
		MOrder order = new MOrder (Env.getCtx(), C_Order_ID, trxName);
		if (log.isLoggable(Level.CONFIG)) log.config(order.toString());

		//  Lines
		for (int i = 0; i < miniTable.getRowCount(); i++)
		{
			if (((Boolean)miniTable.getValueAt(i, 0)).booleanValue())
			{
				

				KeyNamePair pp = (KeyNamePair)miniTable.getValueAt(i, 2);   //  1-documentno  line id
				int M_RequisitionLine_ID = pp.getKey();
				MRequisitionLine rLine = new MRequisitionLine (Env.getCtx(), M_RequisitionLine_ID, trxName);

				//	Create new Order Line
				MOrderLine m_orderLine = new MOrderLine (order);
				m_orderLine.setDatePromised(rLine.getDateRequired());
				if (rLine.getM_Product_ID() >0)
				{
					m_orderLine.setProduct(MProduct.get(Env.getCtx(), rLine.getM_Product_ID()));
					m_orderLine.setM_AttributeSetInstance_ID(rLine.getM_AttributeSetInstance_ID());
				}
				else
				{
					m_orderLine.setC_Charge_ID(rLine.getC_Charge_ID());
					
				}
				//Add shindu 29 mar 2022
				m_orderLine.setDescription(rLine.getDescription());
				//end
				m_orderLine.setPriceActual(rLine.getPriceActual());
				m_orderLine.setAD_Org_ID(rLine.getAD_Org_ID());
				m_orderLine.setQty(rLine.getQty());
				m_orderLine.saveEx();
				
				//	Update Requisition Line
				rLine.setC_OrderLine_ID(m_orderLine.getC_OrderLine_ID());
				rLine.saveEx();
				
			}   //   if selected
		}   //  for all rows

		

		return true;
	}   //  saveInvoice

	protected Vector<String> getOISColumnNames()
	{
		//  Header Info
	    Vector<String> columnNames = new Vector<String>(7);
	    columnNames.add(Msg.getMsg(Env.getCtx(), "Select"));
	    columnNames.add(Msg.getElement(Env.getCtx(), "AD_Org_ID"));
	    columnNames.add(Msg.translate(Env.getCtx(), "Documentno"));
	    columnNames.add(Msg.translate(Env.getCtx(), "DateRequired"));
	    columnNames.add(Msg.translate(Env.getCtx(), "C_BPartner_ID"));
	    columnNames.add(Msg.getElement(Env.getCtx(), "M_Product_ID", false));
	    columnNames.add(Msg.getElement(Env.getCtx(), "C_Charge_ID", false));
	    columnNames.add(Msg.getElement(Env.getCtx(), "Qty"));
	    columnNames.add(Msg.getElement(Env.getCtx(), "Description", false));
	    columnNames.add(Msg.getElement(Env.getCtx(), "AD_User_ID", false));
	    

	    return columnNames;
	}

}
