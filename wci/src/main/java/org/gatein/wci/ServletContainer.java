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
package org.gatein.wci;

import org.gatein.wci.spi.ServletContainerContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A static registry for the servlet container context.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public interface ServletContainer
{

   /**
    * Add a web listener.
    *
    * @param listener the listener
    * @return true if the listener has been added
    */
   boolean addWebAppListener(WebAppListener listener);

   /**
    * Removes a web listener.
    *
    * @param listener the listener
    * @return true if the listener has been removed
    */
   boolean removeWebAppListener(WebAppListener listener);

   /**
    * Returns an executor that will use the provided request and response.
    *
    * @param request the request
    * @param response the response
    * @return an executor
    */
   WebExecutor getExecutor(HttpServletRequest request, HttpServletResponse response);

   /**
    * Generic detyped request dispatch to a servlet context using the include mechanism.
    *
    * @param targetServletContext the target servlet context to dispatch to
    * @param request              the request valid in the current servlet context
    * @param response             the response valid in the current servlet context
    * @param callback             the callback to perform after the dispatch operation
    * @param handback             the handback object that will be provided to the callback
    * @return the object returned by the callback
    * @throws ServletException any servlet exception
    * @throws IOException any io exception
    */
   public Object include(
      ServletContext targetServletContext,
      HttpServletRequest request,
      HttpServletResponse response,
      RequestDispatchCallback callback,
      Object handback) throws ServletException, IOException;

   /**
    * Register a servlet container context. The registration is considered as successful if no existing context is
    * already registered.
    *
    * @param context the servlet container context to register
    * @throws IllegalArgumentException if the context is null
    */
   void register(ServletContainerContext context);
}
