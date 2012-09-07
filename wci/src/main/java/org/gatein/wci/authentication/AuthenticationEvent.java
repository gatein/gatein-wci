/*
* Copyright (C) 2003-2009 eXo Platform SAS.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

package org.gatein.wci.authentication;

import org.gatein.wci.security.Credentials;
import org.gatein.wci.spi.ServletContainerContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class AuthenticationEvent
{

   /** . */
   private final AuthenticationEventType type;

   /** . */
   private final HttpServletRequest request;

   /** . */
   private final HttpServletResponse response;

   /** . */
   private final String userName;

   /** . */
   private final ServletContainerContext containerContext;

   public AuthenticationEvent(AuthenticationEventType type, HttpServletRequest request, HttpServletResponse response, String userName, ServletContainerContext containerContext)
   {
      if (type == null)
      {
         throw new IllegalArgumentException("type is null");
      }
      if (request == null)
      {
         throw new IllegalArgumentException("request is null");
      }
      if (response == null)
      {
         throw new IllegalArgumentException("response is null");
      }
      if (containerContext == null)
      {
         throw new IllegalArgumentException("container is null");
      }

      //
      this.type = type;
      this.request = request;
      this.response = response;
      this.userName = userName;
      this.containerContext = containerContext;
   }

   public AuthenticationEventType getType()
   {
      return type;
   }

   public HttpServletRequest getRequest()
   {
      return request;
   }

   public HttpServletResponse getResponse()
   {
      return response;
   }

   public String getUserName()
   {
      return userName;
   }

   public ServletContainerContext getContainerContext()
   {
      return containerContext;
   }

   @Override
   public String toString()
   {
      return "AuthenticationEvent[type=" + type.name() + ",userName=" + userName + ",uri=" + request.getRequestURI() + "]";
   }
}
