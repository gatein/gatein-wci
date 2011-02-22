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

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class TicketService
{
   public static final long DEFAULT_VALIDITY = 60 * 1000;

   protected final ConcurrentHashMap<String, Ticket> tickets = new ConcurrentHashMap<String, Ticket>();

   protected final Random random = new Random();

   public String createTicket(Credentials credentials, long validityMillis)
   {
      if (validityMillis < 0)
      {
         throw new IllegalArgumentException("validityMillis must be positive");
      }
      if (credentials == null)
      {
         throw new IllegalArgumentException("credentials is null");
      }
      String tokenId = nextTicketId();
      long expirationTimeMillis = System.currentTimeMillis() + validityMillis;
      tickets.put(tokenId, new Ticket(expirationTimeMillis, credentials));
      return tokenId;
   }

   public Credentials validateTicket(String stringKey, boolean remove)
   {
      if (stringKey == null)
      {
         throw new IllegalArgumentException("stringKey is null");
      }

      Ticket ticket;
      if (remove)
      {
         ticket = tickets.remove(stringKey);
      }
      else
      {
         ticket = tickets.get(stringKey);
      }

      if (ticket != null)
      {
         boolean valid = ticket.getExpirationTimeMillis() > System.currentTimeMillis();

         if (valid)
         {
            return ticket.getPayload();
         }
         else if (!remove)
         {
            tickets.remove(stringKey);
         }
         if (!valid)
         {
            throw new AuthenticationException("Ticket " +  stringKey + " has expired");
         }

      }

      return null;
   }

   private String nextTicketId()
   {
      return "wci-ticket-" + random.nextInt();
   }
}
