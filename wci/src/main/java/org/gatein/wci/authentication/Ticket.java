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

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class Ticket
{
   /** . */
   private final long expirationTimeMillis;

   /** . */
   private final Credentials payload;

   public Ticket(long expirationTimeMillis, Credentials payload)
   {
      this.expirationTimeMillis = expirationTimeMillis;
      this.payload = payload;
   }

   public long getExpirationTimeMillis()
   {
      return expirationTimeMillis;
   }

   public Credentials getPayload()
   {
      return payload;
   }

   public boolean isExpired()
   {
      return System.currentTimeMillis() > expirationTimeMillis;
   }
}
