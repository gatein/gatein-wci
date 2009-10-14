package org.gatein.wci.spi;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;

import org.gatein.wci.ServletContainer;
import org.gatein.wci.ServletTestCase;
import org.gatein.wci.TestServlet;
import org.gatein.wci.WebApp;
import org.gatein.wci.WebAppRegistry;
import org.gatein.wci.WebRequest;
import org.gatein.wci.WebResponse;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.jboss.unit.Failure;
import org.jboss.unit.driver.DriverCommand;
import org.jboss.unit.driver.DriverResponse;
import org.jboss.unit.driver.response.EndTestResponse;
import org.jboss.unit.driver.response.FailureResponse;
import org.jboss.unit.remote.driver.handler.deployer.response.DeployResponse;

public class SPICompatibilityTestCase extends ServletTestCase
{

	   /** . */
	   private WebAppRegistry registry;

	   /** . */
	   private Set<String> keys;

	   /** . */
	   private ServletContainer container;

	   public DriverResponse service(TestServlet testServlet, WebRequest req, WebResponse resp) throws ServletException, IOException
	   {
		  // we shouldn't be calling service for these tests
	      return new FailureResponse(Failure.createAssertionFailure("Service call should not be called"));
	   }


	   public DriverResponse invoke(TestServlet testServlet, DriverCommand driverCommand)
	   {
	      if (getRequestCount() == -1)
	      {
	         container = DefaultServletContainerFactory.getInstance().getServletContainer();
	         if (container == null)
	         {
	            return new FailureResponse(Failure.createAssertionFailure("No servlet container present"));
	         }

	         // Register and save the deployed web apps
	         registry = new WebAppRegistry();
	         container.addWebAppListener(registry);
	         keys = new HashSet<String>(registry.getKeys());

	         // Deploy the application web app
	         return new DeployResponse("test-spi-app.war");
	      }
	      else if (getRequestCount() == 0)
	      { 
	    	  FailureResponse failureResponse = checkDeployments("/test-spi-app", 1);
	    	  if (failureResponse != null)
	    	  {
	    		  return failureResponse;
	    	  }
	    	  else
	    	  {
	    		  // deploy test-generic-app.war
	    		  return new DeployResponse("test-generic-app.war");
	    	  }
	      }
	      else if (getRequestCount() == 1)
	      {
	    	  FailureResponse failureResponse = checkDeployments("/test-generic-app", 2);
	    	  if (failureResponse != null)
	    	  {
	    		  return failureResponse;
	    	  }
	    	  else
	    	  {
	    		  return new DeployResponse("test-exo-app.war");
	    	  }
	      }
	      else if (getRequestCount() == 2)
	      {	
	    	  FailureResponse failureResponse = checkDeployments("/test-exo-app", 3);
	    	  if (failureResponse != null)
	    	  {
	    		  return failureResponse;
	    	  }
	    	  else
	    	  {
	    		  return new EndTestResponse();
	    	  }
	      }
	      else
	      {
	         return new FailureResponse(Failure.createAssertionFailure(""));
	      }
	   }

	   protected FailureResponse checkDeployments(String appContext, int count)
	   {
	         // Compute the difference with the previous deployed web apps
	         Set diff = new HashSet<String>(registry.getKeys());
	         diff.removeAll(keys);

	         // It should be 1
	         if (diff.size() != count)
	         {
	            return new FailureResponse(Failure.createAssertionFailure("The size of the new web application deployed should be " + count + ", it is " + diff.size() + " instead." +
	            "The previous set was " + keys + " and the new set is " + registry.getKeys()));
	         }
	         if (!diff.contains(appContext))
	         {
	        	 return new FailureResponse(Failure.createErrorFailure("Could not find the requested webapp [" + appContext + "] in the list of depoyed webapps."));
	         }

	         //
	         WebApp webApp = registry.getWebApp(appContext);
	         if (webApp == null)
	         {
	            return new FailureResponse(Failure.createAssertionFailure("The web app " + appContext + " was not found"));
	         }
	         if (!appContext.equals(webApp.getContextPath()))
	         {
	            return new FailureResponse(Failure.createAssertionFailure("The web app context is not equals to the expected value [" + appContext + "] but has the value " + webApp.getContextPath()));
	         }
	         
	         return null;
	   }
	   
}
