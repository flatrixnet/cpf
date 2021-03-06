/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
* 
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cpk.elements.impl;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletRequest;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IParameterProvider;
import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.utils.PluginUtils;
import pt.webdetails.cpk.elements.AbstractElementType;
import pt.webdetails.cpk.elements.ElementInfo;
import pt.webdetails.cpk.elements.IElement;


import java.io.*;
import java.util.HashMap;

/**
 *
 * @author Pedro Alves<pedro.alves@webdetails.pt>
 */
public class DashboardElementType extends AbstractElementType {

    public DashboardElementType() {
    }

    @Override
    public String getType() {
        return "Dashboard";
    }

    @Override
    public void processRequest(Map<String, IParameterProvider> parameterProviders, IElement element) {
        try {
            // element = (DashboardElement) element;
            callCDE(parameterProviders, element);
        } catch (Exception ex) {
            logger.error("Error whie calling CDE: "+ Util.getExceptionDescription(ex));
        }    
    }

    protected void callCDE(Map<String, IParameterProvider> parameterProviders, IElement element) throws UnsupportedEncodingException, IOException {

        PluginUtils pluginUtils = PluginUtils.getInstance();

        
        String path = pluginUtils.getPluginRelativeDirectory(element.getLocation(), true);
        
        ServletRequest wrapper = pluginUtils.getRequest(parameterProviders);
        OutputStream out = pluginUtils.getResponseOutputStream(parameterProviders);

        String root = wrapper.getScheme() + "://" + wrapper.getServerName() + ":" + wrapper.getServerPort();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("solution", "system");
        params.put("path", path);
        params.put("file", element.getId() + ".wcdf");
        params.put("absolute", "true");
        params.put("inferScheme", "false");
        params.put("root", root);
        IParameterProvider requestParams = pluginUtils.getRequestParameters(parameterProviders);
        pluginUtils.copyParametersFromProvider(params, requestParams);

        if (requestParams.hasParameter("mode") && requestParams.getStringParameter("mode", "Render").equals("edit")) {
            redirectToCdeEditor(parameterProviders, params);
            return;
        }

        InterPluginCall pluginCall = new InterPluginCall(InterPluginCall.CDE, "Render", params);
        pluginCall.setResponse(pluginUtils.getResponse(parameterProviders));
        pluginCall.setOutputStream(out);
        pluginCall.run();
    }

    private void redirectToCdeEditor(Map<String, IParameterProvider> parameterProviders, Map<String, Object> params) throws IOException {

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("../pentaho-cdf-dd/edit");
        if (params.size() > 0) {
            urlBuilder.append("?");
        }

        List<String> paramArray = new ArrayList<String>();
        for (String key : params.keySet()) {
            Object value = params.get(key);
            if (value instanceof String) {
                paramArray.add(key + "=" + URLEncoder.encode((String) value, "utf-8"));
            }
        }

        urlBuilder.append(StringUtils.join(paramArray, "&"));
        PluginUtils.getInstance().redirect(parameterProviders, urlBuilder.toString());
    }

    
    @Override
    protected ElementInfo createElementInfo() {
        return new ElementInfo("text/html");
    }

    @Override
    public boolean isShowInSitemap() {
        return true;
    }
    
    
}
