/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the Nxt software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.http;

import nxt.Constants;
import nxt.util.Convert;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class APITestServlet extends HttpServlet {

    private static final String header1 =
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset='UTF-8'/>\n" +
            "    <meta http-equiv='X-UA-Compatible' content='IE=edge'>\n" +
            "    <meta name='viewport' content='width=device-width, initial-scale=1'>\n" +
            "    <title>Nxt http API</title>\n" +
            "    <link href='css/bootstrap.min.css' rel='stylesheet' type='text/css' />\n" +
            "    <link href='css/font-awesome.min.css' rel='stylesheet' type='text/css' />\n" +
            "    <link href='css/highlight.style.css' rel='stylesheet' type='text/css' />\n" +
            "    <style type='text/css'>\n" +
            "        table {border-collapse: collapse;}\n" +
            "        td {padding: 10px;}\n" +
            "        .result {white-space: pre; font-family: monospace; overflow: auto;}\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "<div class='navbar navbar-default' role='navigation'>\n" +
            "   <div class='container' style='min-width: 90%;'>\n" +
            "       <div class='navbar-header'>\n" +
            "           <a class='navbar-brand' href='/test'>Nxt http API</a>\n" +
            "       </div>\n" +
            "       <div class='navbar-collapse collapse'>\n" +
            "           <ul class='nav navbar-nav navbar-right'>\n" +
            "               <li><input type='text' class='form-control' id='nodeType' " +
            "                    readonly style='margin-top:8px;'></li>\n" +
            "               <li><input type='text' class='form-control' id='servletPath' " +
            "                    readonly style='margin-top:8px;'></li>\n" +
            "               <li><input type='text' class='form-control' id='search' " +
            "                    placeholder='Search' style='margin-top:8px;'></li>\n" +
            "               <li><a href='https://nxtwiki.org/wiki/The_Nxt_API' target='_blank' style='margin-left:20px;'>Wiki Docs</a></li>\n" +
            "           </ul>\n" +
            "       </div>\n" +
            "   </div>\n" +
            "</div>\n" +
            "<div class='container' style='min-width: 90%;'>\n" +
            "<div class='row'>\n" +
            "  <div class='col-xs-12' style='margin-bottom:10px;'>\n" +
            "    <div class='pull-right'>\n" +
            "      <div class='btn-group'>\n" +
            "        <button type='button' class='btn btn-default btn-sm dropdown-toggle' data-toggle='dropdown'>\n" +
            "          <i class='fa fa-check-circle-o'></i> <i class='fa fa-circle-o'></i>\n" +
            "        </button>\n" +
            "        <ul class='dropdown-menu' role='menu' style='font-size:12px;'>\n" +
            "          <li><a href='#' id='navi-select-all-d-add-btn'>Select All Displayed (Add)</a></li>\n" +
            "          <li><a href='#' id='navi-select-all-d-replace-btn'>Select All Displayed (Replace)</a></li>\n" +
            "          <li><a href='#' id='navi-deselect-all-d-btn'>Deselect All Displayed</a></li>\n" +
            "          <li><a href='#' id='navi-deselect-all-btn'>Deselect All</a></li>\n" +
            "        </ul>\n" +
            "      </div>\n" +
            "      <button type='button' id='navi-show-fields' data-navi-val='ALL' class='btn btn-default btn-sm' style='width:165px;'>Show Non-Empty Fields</button>\n" +
            "      <button type='button' id='navi-show-tabs' data-navi-val='ALL' class='btn btn-default btn-sm' style='width:130px;'>Show Open Tabs</button>\n" +
            "    </div>\n" +
            "  </div>\n" +
            "</div>\n" +
            "<div class='row' style='margin-bottom:15px;'>\n" +
            "<div class='col-xs-4 col-sm-3 col-md-2'>\n" +
            "<ul class='nav nav-pills nav-stacked'>\n";
    private static final String header2 =
            "</ul>\n" +
            "</div> <!-- col -->" +
            "<div  class='col-xs-8 col-sm-9 col-md-10'>\n" +
            "<div class='panel-group' id='accordion'>\n";

    private static final String footer1 =
            "</div> <!-- panel-group -->\n" +
            "</div> <!-- col -->\n" +
            "</div> <!-- row -->\n" +
            "</div> <!-- container -->\n" +
            "<script src='js/3rdparty/jquery.js'></script>\n" +
            "<script src='js/3rdparty/bootstrap.js' type='text/javascript'></script>\n" +
            "<script src='js/3rdparty/highlight.pack.js' type='text/javascript'></script>\n" +
            "<script src='js/ats.js' type='text/javascript'></script>\n" +
            "<script src='js/ats.util.js' type='text/javascript'></script>\n" +
            "<script>\n" +
            "$(document).ready(function() {";

    private static final String footer2 =
            "});\n" +
            "</script>\n" +
            "</body>\n" +
            "</html>\n";

    private static final List<String> allRequestTypes = new ArrayList<>(APIServlet.apiRequestHandlers.keySet());
    static {
        Collections.sort(allRequestTypes);
    }

    private static final SortedMap<String, SortedSet<String>> requestTags = new TreeMap<>();
    static {
        for (Map.Entry<String, APIServlet.APIRequestHandler> entry : APIServlet.apiRequestHandlers.entrySet()) {
            String requestType = entry.getKey();
            Set<APITag> apiTags = entry.getValue().getAPITags();
            for (APITag apiTag : apiTags) {
                SortedSet<String> set = requestTags.computeIfAbsent(apiTag.name(), k -> new TreeSet<>());
                set.add(requestType);
            }
        }
    }

    private static String buildLinks(HttpServletRequest req) {
        StringBuilder buf = new StringBuilder();
        String requestTag = Convert.nullToEmpty(req.getParameter("requestTag"));
        buf.append("<li");
        if (requestTag.equals("") 
                & !req.getParameterMap().containsKey("requestType")
                & !req.getParameterMap().containsKey("requestTypes")) {
            buf.append(" class='active'");
        }
        buf.append("><a href='/test'>ALL</a></li>\n");
        buf.append("<li");
        if (req.getParameterMap().containsKey("requestTypes")) {
            buf.append(" class='active'");
        }
        buf.append("><a href='/test?requestTypes=' id='navi-selected'>SELECTED</a></li>\n");
        for (APITag apiTag : APITag.values()) {
            if (requestTags.get(apiTag.name()) != null) {
                buf.append("<li");
                if (requestTag.equals(apiTag.name())) {
                    buf.append(" class='active'");
                }
                buf.append("><a href='/test?requestTag=").append(apiTag.name()).append("'>");
                buf.append(apiTag.getDisplayName()).append("</a></li>\n");
            }
        }
        return buf.toString();
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);
        resp.setContentType("text/html; charset=UTF-8");

        if (! API.isAllowed(req.getRemoteHost())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try (PrintWriter writer = resp.getWriter()) {
            writer.print(header1);
            writer.print(buildLinks(req));
            writer.print(header2);
            String requestType = Convert.nullToEmpty(req.getParameter("requestType"));
            APIServlet.APIRequestHandler requestHandler = APIServlet.apiRequestHandlers.get(requestType);
            StringBuilder bufJSCalls = new StringBuilder();
            String nodeType = "Full Node";
            if (Constants.isLightClient) {
                nodeType = "Light Client";
            } else if (APIProxy.enableAPIProxy) {
                nodeType = "Roaming Client";
            }
            bufJSCalls.append("    $('#nodeType').val('").append(nodeType).append("');");
            bufJSCalls.append("    $('#servletPath').val('").append(req.getServletPath()).append("');");
            if (requestHandler != null) {
                writer.print(form(req, requestType, true, requestHandler));
                bufJSCalls.append("    ATS.apiCalls.push('").append(requestType).append("');\n");
            } else if (!req.getParameterMap().containsKey("requestTypes")) {
                String requestTag = Convert.nullToEmpty(req.getParameter("requestTag"));
                Set<String> taggedTypes = requestTags.get(requestTag);
                for (String type : (taggedTypes != null ? taggedTypes : allRequestTypes)) {
                    requestHandler = APIServlet.apiRequestHandlers.get(type);
                    writer.print(form(req, type, false, requestHandler));
                    bufJSCalls.append("    ATS.apiCalls.push('").append(type).append("');\n");
                }
            } else {
                String requestTypes = Convert.nullToEmpty(req.getParameter("requestTypes"));
                if (!requestTypes.equals("")) {
                    Set<String> selectedRequestTypes = new TreeSet<>(Arrays.asList(requestTypes.split("_")));
                    for (String type: selectedRequestTypes) {
                        requestHandler = APIServlet.apiRequestHandlers.get(type);
                        writer.print(form(req, type, false, requestHandler));
                        bufJSCalls.append("    ATS.apiCalls.push('").append(type).append("');\n");
                    }
                } else {
                    writer.print(fullTextMessage("No API calls selected.", "info"));
                }
            }
            writer.print(footer1);
            writer.print(bufJSCalls.toString());
            writer.print(footer2);
        }

    }

    private static String fullTextMessage(String msg, String msgType) {
        return "<div class='alert alert-" + msgType + "' role='alert'>" + msg + "</div>\n";
    }

    private static String form(HttpServletRequest req, String requestType, boolean singleView, APIServlet.APIRequestHandler requestHandler) {
        String className = requestHandler.getClass().getName();
        List<String> parameters = requestHandler.getParameters();
        boolean requirePost = requestHandler.requirePost();
        String fileParameter = requestHandler.getFileParameter();
        StringBuilder buf = new StringBuilder();
        buf.append("<div class='panel panel-default api-call-All' ");
        buf.append("id='api-call-").append(requestType).append("'>\n");
        buf.append("<div class='panel-heading'>\n");
        buf.append("<h4 class='panel-title'>\n");
        buf.append("<a data-toggle='collapse' class='collapse-link' data-target='#collapse").append(requestType).append("' href='#'>");
        buf.append(requestType);
        buf.append("</a>\n");
        buf.append("<span style='float:right;font-weight:normal;font-size:14px;'>\n");
        if (!singleView) {
            buf.append("<a href='/test?requestType=").append(requestType);
            buf.append("' target='_blank' style='font-weight:normal;font-size:14px;color:#777;'>\n<span class='glyphicon glyphicon-new-window'></span>\n</a>");
            buf.append(" &nbsp;&nbsp;\n");
        }
        buf.append("<a style='font-weight:normal;font-size:14px;color:#777;' href='/doc/");
        buf.append(className.replace('.', '/').replace('$', '.')).append(".html' target='_blank'>javadoc</a>&nbsp;&nbsp;\n");
        buf.append("<a style='font-weight:normal;font-size:14px;color:#777;' href='https://nxtwiki.org/wiki/The_Nxt_API#");
        appendWikiLink(className.substring(className.lastIndexOf('.') + 1), buf);
        buf.append("' target='_blank'>wiki</a>&nbsp;&nbsp;\n");
        buf.append("&nbsp;&nbsp;&nbsp;\n<input type='checkbox' class='api-call-sel-ALL' ");
        buf.append("id='api-call-sel-").append(requestType).append("'>\n");
        buf.append("</span>\n");
        buf.append("</h4>\n");
        buf.append("</div> <!-- panel-heading -->\n");
        buf.append("<div id='collapse").append(requestType).append("' class='panel-collapse collapse");
        if (singleView) {
            buf.append(" in");
        }
        buf.append("'>\n");
        buf.append("<div class='panel-body'>\n");
        String path = req.getServletPath();
        String formAction = "/test-proxy".equals(path) ? "/nxt-proxy" : "/nxt";
        buf.append("<form action='").append(formAction).append("' method='POST' ");
        if (fileParameter != null) {
            buf.append("enctype='multipart/form-data' ");
        }
        buf.append("onsubmit='return ATS.submitForm(this");
        if (fileParameter != null) {
            buf.append(", \"").append(fileParameter).append("\"");
        }
        buf.append(")'>\n");
        buf.append("<input type='hidden' id='formAction' value='").append(formAction).append("'/>\n");
        buf.append("<input type='hidden' name='requestType' value='").append(requestType).append("'/>\n");
        buf.append("<div class='col-xs-12 col-lg-6' style='min-width: 40%;'>\n");
        buf.append("<table class='table'>\n");
        if (fileParameter != null) {
            buf.append("<tr class='api-call-input-tr'>\n");
            buf.append("<td>").append(fileParameter).append(":</td>\n");
            buf.append("<td><input type='file' name='").append(fileParameter).append("' id='").append(fileParameter).append(requestType).append("' ");
            buf.append("style='width:100%;min-width:200px;'/></td>\n");
            buf.append("</tr>\n");
        }
        for (String parameter : parameters) {
            buf.append("<tr class='api-call-input-tr'>\n");
            buf.append("<td>").append(parameter).append(":</td>\n");
            if (isTextArea(parameter, requestHandler)) {
                buf.append("<td><textarea ");
            } else {
                buf.append("<td><input type='").append(isPassword(parameter, requestHandler) ? "password" : "text").append("' ");
            }
            buf.append("name='").append(parameter).append("' ");
            String value = Convert.emptyToNull(req.getParameter(parameter));
            if (value != null) {
                buf.append("value='").append(value.replace("'", "&quot;")).append("' ");
            }
            buf.append("style='width:100%;min-width:200px;'");
            if (isTextArea(parameter, requestHandler)) {
                buf.append("></textarea></td>\n");
            } else {
                buf.append("/></td>\n");
            }
            buf.append("</tr>\n");
        }
        buf.append("<tr>\n");
        buf.append("<td colspan='2'><input type='submit' class='btn btn-default' value='submit'/></td>\n");
        buf.append("</tr>\n");
        buf.append("</table>\n");
        buf.append("</div>\n");
        buf.append("<div class='col-xs-12 col-lg-6' style='min-width: 50%;'>\n");
        buf.append("<h5 style='margin-top:0px;'>\n");
        if (!requirePost) {
            buf.append("<span style='float:right;' class='uri-link'>");
            buf.append("</span>\n");
        } else {
            buf.append("<span style='float:right;font-size:12px;font-weight:normal;'>POST only</span>\n");
        }
        buf.append("Response</h5>\n");
        buf.append("<pre class='hljs json'><code class='result'>JSON response</code></pre>\n");
        buf.append("</div>\n");
        buf.append("</form>\n");
        buf.append("</div> <!-- panel-body -->\n");
        buf.append("</div> <!-- panel-collapse -->\n");
        buf.append("</div> <!-- panel -->\n");
        return buf.toString();
    }

    private static boolean isPassword(String parameter, APIServlet.APIRequestHandler requestHandler) {
        return "secretPhrase".equals(parameter) || "adminPassword".equals(parameter)
                || "recipientSecretPhrase".equals(parameter) || requestHandler.isPassword(parameter);
    }

    private static boolean isTextArea(String parameter, APIServlet.APIRequestHandler requestHandler) {
        return "website".equals(parameter) || requestHandler.isTextArea(parameter);
    }

    private static void appendWikiLink(String className, StringBuilder buf) {
        for (int i = 0; i < className.length(); i++) {
            char c = className.charAt(i);
            if (i == 0) {
                c = Character.toUpperCase(c);
            }
            buf.append(c);
            if (i < className.length() - 2 && Character.isUpperCase(className.charAt(i + 1))
                    && (Character.isLowerCase(c) || Character.isLowerCase(className.charAt(i + 2)))) {
                buf.append('_');
            }
        }
    }

    static void initClass() {}

}
