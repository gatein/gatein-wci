/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.gatein.wci.test.authentication;

import junit.framework.Assert;
import org.gatein.wci.authentication.AuthenticationEvent;
import org.gatein.wci.authentication.AuthenticationEventType;
import org.gatein.wci.test.AbstractWCITestCase;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractAuthenticationTestCase extends AbstractWCITestCase
{

   private static URL readURL(InputStream in) throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[256];
      for (int i = in.read(buffer);i != -1;i = in.read(buffer))
      {
         baos.write(buffer, 0, i);
      }
      return new URL(baos.toString());
   }

   /** URL shared between unit test on client. */
   private static URL url;

   @Test
   @RunAsClient
   @InSequence(0)
   public void testFoo(@ArquillianResource URL deploymentURL) throws Exception
   {
      AuthenticationServlet.status = 0;
      HttpURLConnection conn = (HttpURLConnection)deploymentURL.openConnection();
      conn.connect();
      Assert.assertEquals(200, conn.getResponseCode());
   }

   @Test
   @InSequence(1)
   public void testUserIsNotAuthenticated()
   {
      Assert.assertNull(AuthenticationServlet.remoteUser);
      Assert.assertEquals(Collections.emptyList(), AuthenticationServlet.authEvents);
      AuthenticationServlet.status = 1;
   }

   @Test
   @RunAsClient
   @InSequence(2)
   public void testFoo2(@ArquillianResource URL deploymentURL) throws Exception
   {
      HttpURLConnection conn = (HttpURLConnection)deploymentURL.openConnection();
      conn.connect();
      Assert.assertEquals(200, conn.getResponseCode());
      url = readURL(conn.getInputStream());
   }

   @Test
   @InSequence(3)
   public void testUserIsAuthenticated()
   {
      Assert.assertEquals("foo", AuthenticationServlet.remoteUser);
      Assert.assertEquals(2, AuthenticationServlet.authEvents.size());
      AuthenticationEvent event = AuthenticationServlet.authEvents.removeFirst();
      Assert.assertEquals(AuthenticationEventType.FAILED, event.getType());
      Assert.assertEquals("foo", event.getUserName());
      Assert.assertEquals("foo", event.getCredentials().getPassword());
      event = AuthenticationServlet.authEvents.removeFirst();
      Assert.assertEquals(AuthenticationEventType.LOGIN, event.getType());
      Assert.assertEquals("foo", event.getCredentials().getUsername());
      Assert.assertEquals("bar", event.getCredentials().getPassword());
      AuthenticationServlet.status = 2;
   }

   @Test
   @RunAsClient
   @InSequence(4)
   public void testFoo3() throws Exception
   {
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.connect();
      Assert.assertEquals(200, conn.getResponseCode());
      url = readURL(conn.getInputStream());
   }

   @Test
   @InSequence(5)
   public void testUserRemainsAuthenticated()
   {
      Assert.assertEquals("foo", AuthenticationServlet.remoteUser);
      Assert.assertEquals(Collections.emptyList(), AuthenticationServlet.authEvents);
      AuthenticationServlet.status = 3;
   }

   @Test
   @RunAsClient
   @InSequence(6)
   public void testFoo4() throws Exception
   {
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.connect();
      Assert.assertEquals(200, conn.getResponseCode());
   }

   @Test
   @InSequence(7)
   public void testUserIsLoggeOut()
   {
      Assert.assertNull(AuthenticationServlet.remoteUser);
      Assert.assertEquals(0, AuthenticationServlet.authEvents.size());
      AuthenticationServlet.status = 4;
   }
}
