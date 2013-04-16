package org.gatein.wci.test.servletlistener;

import junit.framework.Assert;
import org.gatein.wci.test.AbstractWCITestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public abstract class AbstractServletListenerTestCase extends AbstractWCITestCase
{

   protected static String webXml;

   @Deployment(name = "servletlistenersapp")
   public static WebArchive deployment()
   {
      WebArchive war = ShrinkWrap.create(WebArchive.class, "servletlistenerapp.war");
      war.addClass(ServletEventCountListener.class);
      war.setWebXML(webXml);
      war.addAsWebInfResource(getJBossDeploymentStructure("servletlistenerwci"), "jboss-deployment-structure.xml");
      return war;
   }

   @Test
   @InSequence(0)
   @RunAsClient
   @OperateOnDeployment("servletlistenerwci")
   public void testListener(@ArquillianResource URL requestDispatchURL) throws Exception
   {
      HttpURLConnection conn = (HttpURLConnection) requestDispatchURL.openConnection();
      conn.connect();
      Assert.assertEquals(200, conn.getResponseCode());
   }

   @Test
   @InSequence(1)
   @OperateOnDeployment("servletlistenerwci")
   public void testListenerCount() throws Exception
   {
      Assert.assertEquals(1, ServletEventCountListener.initializedRequests);
      Assert.assertEquals(1, ServletEventCountListener.destroyedRequests);
   }
}
