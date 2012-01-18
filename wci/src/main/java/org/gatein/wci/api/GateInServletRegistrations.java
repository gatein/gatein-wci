/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
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
package org.gatein.wci.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.gatein.wci.ServletContainer;
import org.gatein.wci.impl.DefaultServletContainer;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.gatein.wci.impl.generic.GenericWebAppContext;
import org.gatein.wci.spi.ServletContainerContext;
import org.gatein.wci.spi.ServletContainerContext.Registration;
import org.gatein.wci.spi.WebAppContext;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class GateInServletRegistrations
{
   /** . */
   private static final Map<String, WebAppContext> pendingContexts = Collections.synchronizedMap(new LinkedHashMap<String, WebAppContext>(16, 0.5f, false));
   private static HashMap<ServletContext, String> requestDispatchMap = new HashMap<ServletContext, String>();
   
   private static ServletContainerContext servletContainerContext;
   
   public static void register(WebAppContext webAppContext, String dispatcherPath)
   {
      requestDispatchMap.put(webAppContext.getServletContext(), dispatcherPath);
      
      if (servletContainerContext != null)
      {
         servletContainerContext.registerWebApp(webAppContext, dispatcherPath);
      }
      else
      {
         pendingContexts.put(webAppContext.getContextPath(), webAppContext);
      }
   }
   
   public static void unregister(ServletContext servletContext)
   {
      requestDispatchMap.remove(servletContext);
      
      String contextPath = servletContext.getContextPath();
      
      if (servletContainerContext != null)
      {
         servletContainerContext.unregisterWebApp(servletContext);
      }
      
      //
      if (pendingContexts.containsKey(contextPath))
      {
         pendingContexts.remove(contextPath);
      }
   }
   
   public static void setServletContainerContext(ServletContainerContext context)
   {
      servletContainerContext = context;
      
      for (String contextPath : pendingContexts.keySet())
      {
         WebAppContext webAppContext = pendingContexts.get(contextPath);
         String dispatcherPath = requestDispatchMap.get(webAppContext.getServletContext());
         servletContainerContext.registerWebApp(webAppContext, dispatcherPath);
      }
   }
}

