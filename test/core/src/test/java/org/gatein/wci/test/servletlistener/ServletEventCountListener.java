package org.gatein.wci.test.servletlistener;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class ServletEventCountListener implements ServletRequestListener
{

   public static int initializedRequests = 0;
   public static int destroyedRequests = 0;

   @Override
   public void requestDestroyed(ServletRequestEvent sre)
   {
      destroyedRequests++;
   }

   @Override
   public void requestInitialized(ServletRequestEvent sre)
   {
      initializedRequests++;
   }
}
