/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.wci.spi;

import org.gatein.wci.WebAppRegistry;
import org.gatein.wci.TestServlet;
import org.gatein.wci.ServletTestCase;
import org.gatein.wci.spi.callbacks.NormalCallback;
import org.gatein.wci.spi.callbacks.ExceptionCallback;
import org.gatein.wci.ServletContainer;
import org.gatein.wci.WebApp;
import org.gatein.wci.ServletContextDispatcher;
import org.gatein.wci.WebRequest;
import org.gatein.wci.WebResponse;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.jboss.unit.driver.DriverResponse;
import org.jboss.unit.driver.DriverCommand;
import org.jboss.unit.driver.response.FailureResponse;
import org.jboss.unit.driver.response.EndTestResponse;
import org.jboss.unit.remote.driver.handler.deployer.response.UndeployResponse;
import org.jboss.unit.remote.driver.handler.deployer.response.DeployResponse;
import org.jboss.unit.remote.driver.handler.http.response.InvokeGetResponse;
import org.jboss.unit.Failure;
import static org.jboss.unit.api.Assert.*;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class SPITestCase extends ServletTestCase
{

   /** . */
   private WebAppRegistry registry;

   /** . */
   private Set<String> keys;

   /** . */
   private ServletContainer container;

   public DriverResponse service(TestServlet testServlet, WebRequest req, WebResponse resp) throws ServletException, IOException
   {
      if (getRequestCount() == 1)
      {
         // Check that this web app is here
         String key = req.getContextPath();
         if (!keys.contains(key))
         {
            fail("The current test web app with key " + key + " is not seen as deployed among " + keys);
         }

         // Should try
         ServletContext appContext = testServlet.getServletContext().getContext("/test-spi-app");

         //
         if (appContext == null)
         {
            fail("Cannot get access to the /test-spi-app servlet context");
         }

         //
         WebApp webApp = registry.getWebApp("/test-spi-app");
         NormalCallback cb1 = new NormalCallback(appContext, webApp.getClassLoader());
         Exception ex = new Exception();
         ExceptionCallback cb2 = new ExceptionCallback(appContext, ex, ex);
         Error err = new Error();
         ExceptionCallback cb3 = new ExceptionCallback(appContext, err, err);
         RuntimeException rex = new RuntimeException();
         ExceptionCallback cb4 = new ExceptionCallback(appContext, rex, rex);
         IOException ioe = new IOException();
         ExceptionCallback cb5 = new ExceptionCallback(appContext, ioe, ioe);

         //
         ServletContextDispatcher dispatcher = new ServletContextDispatcher(req, resp, container);
         DriverResponse response = cb1.test(null, dispatcher);
         response = cb2.test(response, dispatcher);
         response = cb3.test(response, dispatcher);
         response = cb4.test(response, dispatcher);
         response = cb5.test(response, dispatcher);

         //
         if (response != null)
         {
            return response;
         }

         // Now we undeploy
         return new UndeployResponse("test-spi-app.war");
      }
      else if (getRequestCount() == 2)
      {
         if (!keys.equals(registry.getKeys()))
         {
            fail("The set of deployed web applications " + registry.getKeys() + " is not equals to the expected set " + keys);
         }

         // Remove registration
         container.removeWebAppListener(registry);

         //
         if (registry.getKeys().size() > 0)
         {
            fail("The set of deployed web application should be empty instead of " + registry.getKeys());
         }
         else
         {
            return new EndTestResponse();
         }
      }

      //
      return new FailureResponse(Failure.createAssertionFailure(""));
   }


   public DriverResponse invoke(TestServlet testServlet, DriverCommand driverCommand)
   {
      if (getRequestCount() == -1)
      {
         container = DefaultServletContainerFactory.getInstance().getServletContainer();
         if (container == null)
         {
            return new FailureResponse(Failure.createAssertionFailure("No servlet container present"));
         }

         // Register and save the deployed web apps
         registry = new WebAppRegistry();
         container.addWebAppListener(registry);
         keys = new HashSet<String>(registry.getKeys());

         // Deploy the application web app
         return new DeployResponse("test-spi-app.war");
      }
      else if (getRequestCount() == 0)
      {
         // Compute the difference with the previous deployed web apps
         Set diff = new HashSet<String>(registry.getKeys());
         diff.removeAll(keys);

         // It should be 1
         if (diff.size() != 1)
         {
            return new FailureResponse(Failure.createAssertionFailure("The size of the new web application deployed should be 1, it is " + diff.size() + " instead." +
            "The previous set was " + keys + " and the new set is " + registry.getKeys()));
         }
         String key = (String)diff.iterator().next();
         if (!"/test-spi-app".equals(key))
         {
            return new FailureResponse(Failure.createAssertionFailure("The newly deployed web application should be /test-spi-war and it is " + key));
         }

         //
         WebApp webApp = registry.getWebApp("/test-spi-app");
         if (webApp == null)
         {
            return new FailureResponse(Failure.createAssertionFailure("The web app /test-spi-app was not found"));
         }
         if (!"/test-spi-app".equals(webApp.getContextPath()))
         {
            return new FailureResponse(Failure.createAssertionFailure("The web app context is not equals to the expected value but has the value " + webApp.getContextPath()));
         }

         //
         return new InvokeGetResponse("/test-spi-server");
      }
      else
      {
         return new FailureResponse(Failure.createAssertionFailure(""));
      }
   }
}
