package com.gue.plugin.webui.factory;

import org.adempiere.webui.factory.IFormFactory;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.IFormController;

public class GueFormFactory implements IFormFactory {

	@Override
	public ADForm newFormInstance(String formName) {
		if (formName.startsWith("com.gue.plugin.gueform")) {
			Object form =null;
			Class<?> clazz = null;
			ClassLoader loader  = getClass().getClassLoader();
			try {
				clazz = loader.loadClass(formName);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			
			if (clazz != null)
			{
				try
				{
					form = clazz.newInstance();
				}catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
			
			if (form != null)
			{
				if (form instanceof ADForm) {
					return (ADForm) form;
				}
				else if (form instanceof IFormController)
				{
					IFormController controller = (IFormController) form;
					ADForm adform = controller.getForm();
					adform.setICustomForm(controller);
					return adform;
				}
			}
			
		}
		 
		return null;
	}

}
