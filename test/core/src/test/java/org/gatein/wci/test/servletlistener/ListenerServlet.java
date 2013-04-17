package org.gatein.wci.test.servletlistener;

import org.gatein.wci.RequestDispatchCallback;
import org.gatein.wci.ServletContainer;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.WebApp;
import org.gatein.wci.test.WebAppRegistry;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class ListenerServlet extends HttpServlet
{
   private ServletContainer container;

   private WebAppRegistry registry;

   @Override
   public void init() throws ServletException
   {
      container = ServletContainerFactory.getServletContainer();
      container.addWebAppListener(registry = new WebAppRegistry());
   }

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      WebApp app = registry.getWebApp("/servletlistenerapp");
      if (app == null)
      {
         throw new ServletException("Could not find the app to dispatch to");
      }

      //
      container.include(app.getServletContext(), req, resp, new RequestDispatchCallback()
      {
         @Override
         public Object doCallback(ServletContext dsc, HttpServletRequest req, HttpServletResponse resp, Object handback) throws ServletException, IOException
         {
            return null;
         }
      }, null);

      //
      resp.setStatus(200);
   }

   @Override
   public void destroy()
   {
      if (registry != null && container != null)
      {
         container.removeWebAppListener(registry);
      }
   }
}
