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
package org.gatein.wci.test.crosscontext;

import junit.framework.Assert;
import org.gatein.wci.test.AbstractWCITestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractCrossContextTestCase extends AbstractWCITestCase
{

   @Deployment(name = "crosscontextsapp")
   public static WebArchive deployment()
   {
      WebArchive war = ShrinkWrap.create(WebArchive.class, "crosscontextapp.war");
      war.addAsWebInfResource(getJBossDeploymentStructure("crosscontextwci"), "jboss-deployment-structure.xml");
      return war;
   }

   @Test
   @RunAsClient
   public void testFoo(@ArquillianResource @OperateOnDeployment("crosscontextwci") URL requestDispatchURL) throws Exception
   {
      HttpURLConnection conn = (HttpURLConnection)requestDispatchURL.openConnection();
      conn.connect();
      Assert.assertEquals(200, conn.getResponseCode());
   }
}
