package com.self.code.framework.webmvc;

import java.io.File;
import java.util.Locale;

/**
 * @Auther: wangxiaoshuai
 * @Date: 2019/5/4 20:49
 * @Description:
 */
public class ViewResolver {
    private static final String DEFAULT_TEMPLATE_SUFFX = ".html";

    private File templateRootDir;

    public ViewResolver(String templateRoot) {
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        this.templateRootDir = new File(templateRootPath);
    }

    public View resolveViewName(String viewName, Locale locale) throws Exception{
        if(null == viewName || "".equals(viewName.trim())){return null;}
        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFX) ? viewName : (viewName + DEFAULT_TEMPLATE_SUFFX);
        File templateFile = new File((templateRootDir.getPath() + "/" + viewName).replaceAll("/+","/"));
        return new View(templateFile);
    }
}
