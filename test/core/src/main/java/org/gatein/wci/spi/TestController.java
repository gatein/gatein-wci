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

package org.gatein.wci.spi;

import org.gatein.wci.security.Credentials;
import org.gatein.wci.security.WCIController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class TestController extends WCIController
{
   @Override
   public void showLoginForm(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
   {
   }

   @Override
   public void showErrorLoginForm(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
   {
   }

   @Override
   public Credentials getCredentials(final HttpServletRequest req, final HttpServletResponse resp)
   {
      return new Credentials("foo", "bar");
   }

   @Override
   public String getHomeURI(final HttpServletRequest req)
   {
      return "/home";
   }
}
