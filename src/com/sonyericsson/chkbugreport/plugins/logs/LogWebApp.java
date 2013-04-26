/*
 * Copyright (C) 2013 Sony Mobile Communications AB
 *
 * This file is part of ChkBugReport.
 *
 * ChkBugReport is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * ChkBugReport is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ChkBugReport.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sonyericsson.chkbugreport.plugins.logs;

import com.sonyericsson.chkbugreport.Module;
import com.sonyericsson.chkbugreport.doc.Block;
import com.sonyericsson.chkbugreport.doc.Button;
import com.sonyericsson.chkbugreport.doc.Chapter;
import com.sonyericsson.chkbugreport.doc.DocNode;
import com.sonyericsson.chkbugreport.doc.HtmlNode;
import com.sonyericsson.chkbugreport.doc.Renderer;
import com.sonyericsson.chkbugreport.doc.Script;
import com.sonyericsson.chkbugreport.doc.Span;
import com.sonyericsson.chkbugreport.webserver.Web;
import com.sonyericsson.chkbugreport.webserver.engine.HTTPRenderer;
import com.sonyericsson.chkbugreport.webserver.engine.HTTPRequest;
import com.sonyericsson.chkbugreport.webserver.engine.HTTPResponse;

import java.io.IOException;

public class LogWebApp {

    private LogPlugin mLog;
    private String mId;

    public LogWebApp(LogPlugin logPlugin) {
        mLog = logPlugin;
        mId = logPlugin.getInfoId();
    }

    @Web
    public void log(Module mod, HTTPRequest req, HTTPResponse resp) {
        Chapter ch = new Chapter(mod, "Log");
        Span filterSelect = new Span();
        filterSelect.add("Filter:");
        new HtmlNode("select", filterSelect).setName("filter").setId("filter");
        filterSelect.add(new Button("New filter", "javascript:log_new_filter()"));
        ch.addCustomHeaderView(filterSelect);
        new Block(ch).setId("log-placeholder");
        new Script(ch, "lib_log.js");
        new Script(ch).println("var logid=\"" + mId + "\";");

        try {
            Renderer r = new HTTPRenderer(resp, mId + "$log", mod, ch);
            ch.prepare(r);
            ch.render(r);
        } catch (IOException e) {
            e.printStackTrace();
            resp.setResponseCode(500);
        }
    }

    @Web
    public void logOnly(Module mod, HTTPRequest req, HTTPResponse resp) {
        String filterName = req.getArg("filter");
        DocNode log = new Block().addStyle("log");
        LogLines logs = mLog.getLogs();
        int cnt = logs.size();
        for (int i = 0; i < cnt; i++) {
            LogLine sl = logs.get(i);
            // FIXME: hardcoded filter, just for testing
            if ("Mine".equals(filterName)) {
                if ("PowerManagerService".equals(sl.tag)) {
                    continue;
                }
            }
            log.add(sl.copy());
        }
        try {
            Renderer r = new HTTPRenderer(resp, mId + "$log", mod, null);
            log.prepare(r);
            log.render(r);
        } catch (IOException e) {
            e.printStackTrace();
            resp.setResponseCode(500);
        }
    }

    @Web
    public void listFilters(Module mod, HTTPRequest req, HTTPResponse resp) {
        // FIXME: hardcoded filter list
        resp.println("{");
        resp.println("  \"filters\": [");
        resp.println("    \"Audio\",");
        resp.println("    \"ActivityManager\",");
        resp.println("    \"Mine\"");
        resp.println("  ]");
        resp.println("}");
    }

    @Web
    public void listFilter(Module mod, HTTPRequest req, HTTPResponse resp) {
        // FIXME: hardcoded filter list
        resp.println("{");
        resp.println("  \"name\": \"Audio\",");
        resp.println("  \"type\": \"add\",");
        resp.println("  \"match_tag\": \"\",");
        resp.println("  \"match_msg\": \"audio\",");
        resp.println("  \"match_line\": \"\",");
        resp.println("  \"color\": \"#ff0000\",");
        resp.println("}");
    }

}
