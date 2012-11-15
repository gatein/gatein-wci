/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2012, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.wci.test.jetty8.deployment;

import junit.framework.Assert;

import org.gatein.wci.ServletContainer;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.test.AbstractWCITestCase;
import org.gatein.wci.test.WebAppRegistry;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
@RunWith(Arquillian.class)
public class RedeployServiceTestCase extends AbstractWCITestCase
{  
   @ArquillianResource
   Deployer deployer;
   
   @Test
   @RunAsClient
   @InSequence(0)
   public void deployWCIServiceArchive()
   {
      deployer.deploy("deploymentwci");
   }
   
   @Test
   @InSequence(1)
   @OperateOnDeployment("deploymentwci")
   public void testFirstDeploy()
   {
      ServletContainer _container = ServletContainerFactory.getServletContainer();
      Assert.assertNotNull(_container);

      //
      WebAppRegistry _registry = new WebAppRegistry();
      _container.addWebAppListener(_registry);
      
      //check that the keys contain the /deploymentwci
      Assert.assertTrue(_registry.getKeys().contains("/deploymentwci"));
   }
   
   @Test
   @RunAsClient
   @InSequence(2)
   public void deployRedeployWCIServiceArchive()
   {
      deployer.undeploy("deploymentwci");
      deployer.deploy("deploymentwci");
   }
   
   @Test
   @InSequence(3)
   @OperateOnDeployment("deploymentwci")
   public void testReDeploy()
   {
      ServletContainer _container = ServletContainerFactory.getServletContainer();
      Assert.assertNotNull(_container);

      //
      WebAppRegistry _registry = new WebAppRegistry();
      _container.addWebAppListener(_registry);
      
      //check that the keys contain the /deploymentwci
      Assert.assertTrue(_registry.getKeys().contains("/deploymentwci"));
   }
   
   @Test
   @RunAsClient
   @InSequence(4)
   public void undeployArchive()
   {
      deployer.undeploy("deploymentwci");
   }
   
   @Deployment (name = "deploymentwci", managed = false, testable=true)
   public static WebArchive wciDeployment()
   {
      WebArchive war = ShrinkWrap.create(WebArchive.class, "deploymentwci.war");
      war.setWebXML("org/gatein/wci/test/jetty8/deployment/web.xml");
      war.addAsWebResource("org/gatein/wci/test/jetty8/deployment/jetty-web.xml", "WEB-INF/jetty-web.xml");

      return war;
   }
}

