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
package org.gatein.wci.impl.generic;

import org.gatein.wci.command.CommandServlet;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class GenericBootstrapServlet extends CommandServlet
{

   /** . */
   private String contextPath;

   public void init() throws ServletException
   {
      try
      {
         Method m = ServletContext.class.getMethod("getContextPath", new Class[0]);
         ServletContext servletContext = getServletContext();

         //
         String contextPath = (String)m.invoke(servletContext, new Object[0]);
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         GenericWebAppContext webAppContext = new GenericWebAppContext(servletContext, contextPath, classLoader);

         //
         GenericServletContainerContext.instance.register(webAppContext);
         this.contextPath = contextPath;
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public void destroy()
   {
      if (contextPath != null)
      {
         GenericServletContainerContext.instance.unregister(contextPath);
      }
   }
}
