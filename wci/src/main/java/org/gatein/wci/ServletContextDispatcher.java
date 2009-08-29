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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Encapsulate dispatch functionnality into a single class so it is easy to
 * pass it as an argment to a framework that needs a dispatcher to just a
 * servlet context and does not care about the underlying spi or request/response.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class ServletContextDispatcher
{

   /** . */
   private final HttpServletRequest request;

   /** . */
   private final HttpServletResponse response;

   /** . */
   private final ServletContainer servletContainer;

   public ServletContextDispatcher(HttpServletRequest request, HttpServletResponse response, ServletContainer servletContainer)
   {
      if (request == null)
      {
         throw new IllegalArgumentException("No null request allowed");
      }
      if (response == null)
      {
         throw new IllegalArgumentException("No null response allowed");
      }
      if (servletContainer == null)
      {
         throw new IllegalArgumentException("No null servlet container allowed");
      }

      //
      this.request = request;
      this.response = response;
      this.servletContainer = servletContainer;
   }

   public HttpServletRequest getRequest()
   {
      return request;
   }

   public HttpServletResponse getResponse()
   {
      return response;
   }

   public Object include(
      ServletContext targetServletContext,
      RequestDispatchCallback callback,
      Object handback) throws ServletException, IOException
   {
      return servletContainer.include(targetServletContext, request, response, callback, handback);
   }
}
