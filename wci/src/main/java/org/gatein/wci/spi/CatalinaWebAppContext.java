package org.gatein.wci.spi;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.command.CommandServlet;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public abstract class CatalinaWebAppContext implements WebAppContext {
    protected final static Logger log = LoggerFactory.getLogger(CatalinaWebAppContext.class);

    protected static final String GATEIN_SERVLET_NAME = "TomcatGateInServlet";
    protected static final String GATEIN_SERVLET_PATH = "/tomcatgateinservlet";
    protected static final int GATEIN_SERVLET_LOAD_ON_STARTUP = 0;

    private static final String BEAN_MGR_ATTR = "javax.enterprise.inject.spi.BeanManager";

    /**
     * .
     */
    protected ServletContext servletContext;

    /**
     * .
     */
    protected ClassLoader classLoader;

    /**
     * .
     */
    protected String contextPath;

    public CatalinaWebAppContext(ServletContext servletContext, ClassLoader classLoader, String contextPath) {
        this.servletContext = servletContext;
        this.classLoader = classLoader;
        this.contextPath = contextPath;
    }

    @Override
    public void start() throws Exception {
        performStartup();

        // Add BeanManager to ServletContext
        try {
            Object beanMgr = new InitialContext().lookup("java:comp/BeanManager");
            if (null != beanMgr) {
                servletContext.setAttribute(BEAN_MGR_ATTR, beanMgr);
            }
        } catch (NamingException e) {
            // Ignored
        }
    }

    @Override
    public void stop() {
        cleanup();
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public boolean importFile(String parentDirRelativePath, String name, InputStream source, boolean overwrite) throws IOException {
        return false;
    }

    protected String getCommandServletClassName() {
        String className = null;
        try {
            className = CommandServlet.class.getName();
            classLoader.loadClass(className);
        } catch(Exception ex) {
            className = null;
            log.debug("WCI integration skipped for context: /" + contextPath);
        }
        return className;
    }

    @Override
    public void fireRequestDestroyed(ServletRequest servletRequest)
    {
       //Do Nothing
    }

    @Override
    public void fireRequestInitialized(ServletRequest servletRequest)
    {
       //Do Nothing
    }

    protected abstract void performStartup() throws Exception;

    protected abstract void cleanup();
}
