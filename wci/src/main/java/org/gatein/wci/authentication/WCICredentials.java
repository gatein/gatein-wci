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

import java.io.Serializable;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class WCICredentials implements Serializable
{
   /** . */
   private final String username;

   /** . */
   private final String password;

   /**
    * Construct a new instance.
    *
    * @param username the username value
    * @param password the password value
    * @throws NullPointerException if any argument is null
    */
   public WCICredentials(String username, String password) throws NullPointerException
   {
      if (username == null)
      {
         throw new IllegalArgumentException("Username is null");
      }
      if (password == null)
      {
         throw new IllegalArgumentException("Password is null");
      }
      this.username = username;
      this.password = password;
   }

   /**
    * Returns the username.
    *
    * @return the username
    */
   public String getUsername()
   {
      return username;
   }

   /**
    * Returns the password.
    *
    * @return the password
    */
   public String getPassword()
   {
      return password;
   }
}
