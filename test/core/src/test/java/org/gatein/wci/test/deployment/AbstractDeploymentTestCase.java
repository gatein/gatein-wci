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
package org.gatein.wci.test.deployment;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.gatein.wci.ServletContainer;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.WebApp;
import org.gatein.wci.test.AbstractWCITestCase;
import org.gatein.wci.test.WebAppRegistry;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractDeploymentTestCase extends AbstractWCITestCase
{

   /** . */
   private static WebAppRegistry registry;

   /** . */
   private static Set<String> keys;

   /** . */
   private static ServletContainer container;

   @ArquillianResource
   Deployer deployer;

   @Deployment(name = "deploymentnative", managed = false)
   public static WebArchive nativeDeployment()
   {
      WebArchive war = ShrinkWrap.create(WebArchive.class, "deploymentnative.war");
      war.addAsWebInfResource(getJBossDeploymentStructure("deploymentwci"), "jboss-deployment-structure.xml");
      return war;
   }

   @Deployment(name = "deploymentnativeskip", managed = false)
   public static WebArchive nativeSkipDeployment()
   {
      WebArchive war = ShrinkWrap.create(WebArchive.class, "deploymentnativeskip.war");
      war.addAsWebInfResource(getJBossDeploymentStructure("deploymentwci"), "jboss-deployment-structure.xml");
      war.setWebXML(getAsset("" +
         "<!DOCTYPE web-app PUBLIC\n" +
         "\"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\"\n" +
         "\"http://java.sun.com/dtd/web-app_2_3.dtd\">\n" +
         "<web-app>\n" +
         "<context-param>\n" +
         "<param-name>gatein.wci.native.DisableRegistration</param-name>\n" +
         "<param-value>true</param-value>\n" +
         "</context-param>\n" +
         "<listener>\n" +
         "<listener-class>org.gatein.wci.test.deployment.NativeSkipFilter</listener-class>\n" +
         "</listener>\n</web-app>\n"));
      return war;
   }

   @Deployment(name = "deploymentnativeskipgatein", managed = false)
   public static WebArchive nativeSkipGateInDeployment()
   {
      WebArchive war = ShrinkWrap.create(WebArchive.class, "deploymentnativeskipgatein.war");
      war.addAsWebInfResource(getJBossDeploymentStructure("deploymentwci"), "jboss-deployment-structure.xml");
      war.setWebXML(getAsset("" +
         "<!DOCTYPE web-app PUBLIC\n" +
         "\"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\"\n" +
         "\"http://java.sun.com/dtd/web-app_2_3.dtd\">\n" +
         "<web-app>\n" +
         "<context-param>\n" +
         "<param-name>gatein.wci.native.DisableRegistration</param-name>\n" +
         "<param-value>true</param-value>\n" +
         "</context-param>\n" +
         "<servlet>\n" +
         "<servlet-name>GateInServlet</servlet-name>\n" +
         "<servlet-class>org.gatein.wci.api.GateInServlet</servlet-class>\n" +
         "<load-on-startup>1</load-on-startup>\n" +
         "</servlet>\n" +
         "<servlet-mapping>\n" +
         "<servlet-name>GateInServlet</servlet-name>\n" +
         "<url-pattern>/gateinservlet</url-pattern>\n" +
         "</servlet-mapping>" +
         "</web-app>\n"));
      return war;
   }

   @Deployment(name = "deploymentgatein", managed = false)
   public static WebArchive gateInDeployment()
   {
      WebArchive war = ShrinkWrap.create(WebArchive.class, "deploymentgatein.war");
      war.addAsWebInfResource(getJBossDeploymentStructure("deploymentwci"), "jboss-deployment-structure.xml");
      war.setWebXML(getAsset("" +
         "<!DOCTYPE web-app PUBLIC\n" +
         "\"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\"\n" +
         "\"http://java.sun.com/dtd/web-app_2_3.dtd\">\n" +
         "<web-app>\n" +
         "<servlet>\n" +
         "<servlet-name>GateInServlet</servlet-name>\n" +
         "<servlet-class>org.gatein.wci.api.GateInServlet</servlet-class>\n" +
         "<load-on-startup>1</load-on-startup>\n" +
         "</servlet>\n" +
         "<servlet-mapping>\n" +
         "<servlet-name>GateInServlet</servlet-name>\n" +
         "<url-pattern>/gateinservlet</url-pattern>\n" +
         "</servlet-mapping>" +
         "</web-app>\n"));
      return war;
   }

   @Test
   @InSequence(0)
   public void testBefore()
   {
      ServletContainer _container = ServletContainerFactory.getServletContainer();
      Assert.assertNotNull(_container);

      //
      WebAppRegistry _registry = new WebAppRegistry();
      _container.addWebAppListener(_registry);
      HashSet<String> _keys = new HashSet<String>(_registry.getKeys());

      //
      registry = _registry;
      keys = _keys;
      container = _container;
   }

   @Test
   @RunAsClient
   @InSequence(1)
   public void testDeployApp()
   {
      deployer.deploy("deploymentnative");
   }

   @Test
   @InSequence(2)
   public void testAfterDeployApp()
   {
      // Compute the difference with the previous deployed web apps
      Set diff = new HashSet<String>(registry.getKeys());
      diff.removeAll(keys);

      // It should be 1
      if (diff.size() != 1)
      {
         throw new AssertionFailedError("The size of the new web application deployed should be 1, it is " + diff.size() + " instead." +
            "The previous set was " + keys + " and the new set is " + registry.getKeys());
      }
      String key = (String)diff.iterator().next();
      if (!"/deploymentnative".equals(key))
      {
         throw new AssertionFailedError("The newly deployed web application should be /deployment-war and it is " + key);
      }

      //
      WebApp webApp = registry.getWebApp("/deploymentnative");
      if (webApp == null)
      {
         throw new AssertionFailedError("The web app /deploymentnative was not found");
      }
      if (!"/deploymentnative".equals(webApp.getContextPath()))
      {
         throw new AssertionFailedError("The web app context is not equals to the expected value but has the value " + webApp.getContextPath());
      }
   }

   @Test
   @RunAsClient
   @InSequence(3)
   public void testUndeployApp()
   {
      deployer.undeploy("deploymentnative");
   }

   @Test
   @InSequence(4)
   public void testAfterUndeployApp()
   {
      // It should be equals
      if (!registry.getKeys().equals(keys))
      {
         throw new AssertionFailedError("The size of the new web application deployed should be equals: " +
            "" + keys + " != " + registry.getKeys());
      }
   }

   @Test
   @RunAsClient
   @InSequence(5)
   public void testDeployNativeSkip()
   {
      deployer.deploy("deploymentnativeskip");
   }

   @Test
   @InSequence(6)
   public void testAfterDeployNativeSkip()
   {
      Assert.assertEquals(keys, registry.getKeys());
      Assert.assertTrue(
         "Was expecting " + NativeSkipFilter.contextPaths + " to contain /deploymentnativeskip",
         NativeSkipFilter.contextPaths.contains("/deploymentnativeskip"));
   }

   @Test
   @RunAsClient
   @InSequence(7)
   public void testUndeployNativeSkip()
   {
      deployer.undeploy("deploymentnativeskip");
   }

   @Test
   @InSequence(8)
   public void testAfterUndeployNativeSkip()
   {
      Assert.assertEquals(keys, registry.getKeys());
      Assert.assertFalse(
         "Was expecting " + NativeSkipFilter.contextPaths + " to not contain /deploymentnativeskip",
         NativeSkipFilter.contextPaths.contains("/deploymentnativeskip"));
   }

   @Test
   @RunAsClient
   @InSequence(9)
   public void testDeployNativeSkipGateIn()
   {
      deployer.deploy("deploymentnativeskipgatein");
   }

   @Test
   @InSequence(10)
   public void testAfterDeployNativeSkipGateIn()
   {
      HashSet<String> tmp = new HashSet<String>(keys);
      tmp.add("/deploymentnativeskipgatein");
      Assert.assertEquals(tmp, registry.getKeys());
   }

   @Test
   @RunAsClient
   @InSequence(11)
   public void testUndeployNativeSkipGateIn()
   {
      deployer.undeploy("deploymentnativeskipgatein");
   }

   @Test
   @InSequence(12)
   public void testAfterUndeployNativeSkipGateIn()
   {
      Assert.assertEquals(keys, registry.getKeys());
   }

   @Test
   @RunAsClient
   @InSequence(13)
   public void testDeployGateIn()
   {
      deployer.deploy("deploymentgatein");
   }

   @Test
   @InSequence(14)
   public void testAfterDeployGateIn()
   {
      HashSet<String> tmp = new HashSet<String>(keys);
      tmp.add("/deploymentgatein");
      Assert.assertEquals(tmp, registry.getKeys());
   }

   @Test
   @RunAsClient
   @InSequence(15)
   public void testUndeployGateIn()
   {
      deployer.undeploy("deploymentgatein");
   }

   @Test
   @InSequence(16)
   public void testAfterUndeployGateIn()
   {
      Assert.assertEquals(keys, registry.getKeys());
   }

   @Test
   @InSequence(400)
   public void testAfter()
   {
      // It should be equals
      if (!registry.getKeys().equals(keys))
      {
         throw new AssertionFailedError("The size of the new web application deployed should be equals: " +
            "" + keys + " != " + registry.getKeys());
      }

      // Remove registration
      container.removeWebAppListener(registry);

      //
      if (registry.getKeys().size() > 0)
      {
         throw new AssertionFailedError("The set of deployed web application should be empty instead of " + registry.getKeys());
      }
   }
}
