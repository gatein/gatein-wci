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

import org.gatein.wci.RequestDispatchCallback;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.gatein.wci.spi.ServletContainerContext;
import org.gatein.wci.command.CommandDispatcher;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class GenericServletContainerContext implements ServletContainerContext
{

   /** . */
   static final GenericServletContainerContext instance = new GenericServletContainerContext();

   /** . */
   private Registration registration;

   static
   {
      DefaultServletContainerFactory.registerContext(instance);
   }

   void register(GenericWebAppContext webAppContext)
   {
      if (registration != null)
      {
         registration.registerWebApp(webAppContext);
      }
   }

   void unregister(String webAppId)
   {
      if (registration != null)
      {
         registration.unregisterWebApp(webAppId);
      }
   }

   /** . */
   private final CommandDispatcher dispatcher = new CommandDispatcher();

   public Object include(
      ServletContext targetServletContext,
      HttpServletRequest request,
      HttpServletResponse response,
      RequestDispatchCallback callback,
      Object handback) throws ServletException, IOException
   {
      return dispatcher.include(targetServletContext, request, response, callback, handback);
   }

   public void setCallback(Registration registration)
   {
      this.registration = registration;
   }

   public void unsetCallback(Registration registration)
   {
      this.registration = null;
   }
}
