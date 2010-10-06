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
  
  protected long validityMillis = 1000 * 60; // TODO : Init from confguration

  protected final ConcurrentHashMap<String, Ticket> tickets = new ConcurrentHashMap<String, Ticket>();

  protected final Random random = new Random();

  public String createTicket(Credentials credentials)
  {
    if (validityMillis < 0)
    {
      throw new IllegalArgumentException();
    }
    if (credentials == null)
    {
      throw new NullPointerException();
    }
    String tokenId = nextTicketId();
    long expirationTimeMillis = System.currentTimeMillis() + validityMillis;
    tickets.put(tokenId, new Ticket(expirationTimeMillis, credentials));
    return tokenId;
  }

  public Credentials validateToken(String stringKey, boolean remove)
  {
    if (stringKey == null)
    {
      throw new IllegalArgumentException("stringKey is null");
    }

    Ticket token;
    try
    {
      if (remove)
      {
        token = tickets.remove(stringKey);
      }
      else
      {
        token = tickets.get(stringKey);
      }

      if (token != null)
      {
        boolean valid = token.getExpirationTimeMillis() > System.currentTimeMillis();
        
        if (valid)
        {
          return token.getPayload();
        }
        else if (!remove)
        {
          tickets.remove(stringKey);
        }
        
      }
    }
    catch (Exception ignore)
    {
    }

   return null;
  }

  private String nextTicketId() {
    return "wci-ticket-" + random.nextInt();
  }
}
