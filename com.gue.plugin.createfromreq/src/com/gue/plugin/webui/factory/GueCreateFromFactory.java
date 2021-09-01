package com.gue.plugin.webui.factory;

import org.compiere.grid.ICreateFrom;
import org.compiere.grid.ICreateFromFactory;
import org.compiere.model.GridTab;
import org.compiere.model.I_C_Order;
import com.gue.plugin.webui.form.WCreateFromOrderUI;


/**
 * 
 * 
 * @author GueShindu
 * @baseon: Fabian Aguilar faaguilar@gmail.com
 *  *
 */
public class GueCreateFromFactory implements ICreateFromFactory {
	@Override
	public ICreateFrom create(GridTab mTab) 
	{
		String tableName = mTab.getTableName();
		if (tableName.equals(I_C_Order.Table_Name))
			return new WCreateFromOrderUI(mTab);
		
		return null;
	}
}
