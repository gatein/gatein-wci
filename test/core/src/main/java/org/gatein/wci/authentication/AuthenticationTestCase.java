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

import org.jboss.unit.api.pojo.annotations.Test;

import static org.jboss.unit.api.Assert.*;


/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@Test
public class AuthenticationTestCase {
   @Test
   void testTicket() {
      TicketService tService = GenericAuthentication.TICKET_SERVICE;
      WCICredentials credentials = new WCICredentials("foo", "bar");
      String strTicket = tService.createTicket(credentials);
      WCICredentials credentialsFromTs = tService.validateToken(strTicket, false);
      assertEquals(credentials.getUsername(), credentialsFromTs.getUsername());
      assertEquals(credentials.getPassword(), credentialsFromTs.getPassword());
      assertNotNull(tService.validateToken(strTicket, true));
      assertNull(tService.validateToken(strTicket, true));
   }


}
