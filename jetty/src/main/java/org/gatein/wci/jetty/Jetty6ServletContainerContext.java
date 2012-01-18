package org.gatein.wci.jetty;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.gatein.wci.RequestDispatchCallback;
import org.gatein.wci.ServletContainerVisitor;
import org.gatein.wci.WebApp;
import org.gatein.wci.api.GateInServlet;
import org.gatein.wci.api.GateInServletRegistrations;
import org.gatein.wci.authentication.GenericAuthentication;
import org.gatein.wci.command.CommandDispatcher;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.gatein.wci.security.Credentials;
import org.gatein.wci.spi.ServletContainerContext;
import org.mortbay.component.Container;
import org.mortbay.component.LifeCycle;
import org.mortbay.component.Container.Relationship;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.webapp.WebAppContext;

public class Jetty6ServletContainerContext  implements ServletContainerContext, org.mortbay.component.Container.Listener, LifeCycle.Listener
{

	private Registration registration;
	
	private Container container;
	private Server server;
	private ContextHandlerCollection chc;

   /** . */
   private GenericAuthentication authentication = new GenericAuthentication();
	
	   /** The monitored contexts. */
	   private final Set<String> monitoredContexts = new HashSet<String>();
	   
	   /** The monitored contexts which were manually added. */
	   private static Map<String, String> manualMonitoredContexts = new HashMap<String, String>();
	   
	   private final Set<String> monitoredContextHandlerCollection = new HashSet<String>();

   /** Perform cross-context session invalidation on logout, or not */
   private boolean crossContextLogout = true;

	public Jetty6ServletContainerContext(Server server) {
		this.server = server;
		this.container = server.getContainer();
	}
	
	   /** . */
	private final CommandDispatcher dispatcher = new CommandDispatcher("/jettygateinservlet");
	
	public Object include(ServletContext targetServletContext,
			HttpServletRequest request, HttpServletResponse response,
			RequestDispatchCallback callback, Object handback)
			throws ServletException, IOException 
    {
	   if (manualMonitoredContexts.containsKey(targetServletContext.getServletContextName()))
	   {
	      String dispatherPath = manualMonitoredContexts.get(targetServletContext.getServletContextName());
	      CommandDispatcher dispatcher = new CommandDispatcher(dispatherPath);
	      return dispatcher.include(targetServletContext, request, response, callback, handback);
	   }
	   else
	   {
	      return dispatcher.include(targetServletContext, request, response, callback, handback);
	   }
    }

	public void setCallback(Registration registration) {
		this.registration = registration;
		GateInServletRegistrations.setServletContainerContext(this);
	}

	public void unsetCallback(Registration registration) {
		this.registration = null;
	}

   public void setCrossContextLogout(boolean val)
   {
      crossContextLogout = val;
   }

   public void login(HttpServletRequest request, HttpServletResponse response, Credentials credentials, long validityMillis) throws IOException
   {
      authentication.login(credentials, request, response, validityMillis, null);
   }

   public void login(HttpServletRequest request, HttpServletResponse response, Credentials credentials, long validityMillis, String initialURI) throws IOException
   {
      authentication.login(credentials, request, response, validityMillis, initialURI);
   }

   public void logout(HttpServletRequest request, HttpServletResponse response) throws ServletException
   {
      HttpSession sess = request.getSession(false);

      if (sess == null)
         return;

      sess.invalidate();

      if (!crossContextLogout)
         return;

      final String sessId = sess.getId();
      DefaultServletContainerFactory.getInstance().getServletContainer().visit(new ServletContainerVisitor()
      {
         public void accept(WebApp webApp)
         {
            webApp.invalidateSession(sessId);
         }
      });

   }

   public String getContainerInfo()
   {
      return "Jetty/6.x";
   }

   public void start()
	{
		DefaultServletContainerFactory.registerContext(this);
		
        Handler[] children = server.getChildHandlersByClass(ContextHandlerCollection.class);
		for (int i = 0; i < children.length; i++)
		{
				ContextHandlerCollection chc = (ContextHandlerCollection)children[i];
				registerContextHandlerCollection(chc);
		}
		container.addEventListener(this);
	}
	
	public void stop()
	{
		container.removeEventListener(this);
		
		Handler[] children = server.getChildHandlersByClass(ContextHandlerCollection.class);
		for (int i=0; i< children.length; i++)
		{
			ContextHandlerCollection chc = (ContextHandlerCollection)children[i];
			unregisterContextHandlerCollection(chc);
		}
		
		registration.cancel();
		registration = null;
	}

	public void addBean(Object bean) 
	{
		if (bean instanceof ContextHandlerCollection)
		{
			ContextHandlerCollection chc = (ContextHandlerCollection)bean;
			registerContextHandlerCollection(chc);
		}
		else if (bean instanceof WebAppContext)
		{
			WebAppContext wac = (WebAppContext)bean;
			registerWebAppContext(wac);
		}
	}

	public void removeBean(Object bean) 
	{
		if (bean instanceof ContextHandlerCollection)
		{
			ContextHandlerCollection chc = (ContextHandlerCollection)bean;
			unregisterContextHandlerCollection(chc);
		}
		else if (bean instanceof WebAppContext)
		{
			WebAppContext wac = (WebAppContext)bean;
			unregisterWebAppContext(wac);
		}
	}

	public void add(Relationship relationship) 
	{
		//ignore event for now
	}

	public void remove(Relationship relationship) 
	{
      removeBean(relationship.getChild());
	}

	private void startWebAppContext(WebAppContext webappContext) 
	{
	   try
	   {
	      // skip if the webapp has explicitly stated it doesn't want native registration
	      // usefull when portlets are dependent on servlet ordering
	      if (!isDisabledNativeRegistration(webappContext.getServletContext()))
	      {
	         Jetty6WebAppContext context = new Jetty6WebAppContext(webappContext);

	         //
	         if (registration != null)
	         {
	            registration.registerWebApp(context);
	         }
	      }
	   }
	   catch (Exception e)
	   {
	      e.printStackTrace();
	   }

	}

	private void stopWebAppContext(WebAppContext webappContext) 
	{
	   try
	   {
	      // skip if the webapp has explicitly stated it doesn't want native registration
	      // usefull when portlets are dependent on servlet ordering
	      if (!isDisabledNativeRegistration(webappContext.getServletContext()))
	      {
	         if (registration != null)
	         {
	            registration.unregisterWebApp(webappContext.getContextPath());
	         }
	      }
	   }
	   catch (Exception e)
	   {
	      e.printStackTrace();
	   }
	}

	private void registerWebAppContext(WebAppContext wac) 
	{
		// using servletContext since its the standard object and not jetty specific
		// (need standard object when using ServletContextListener).
		if (!monitoredContexts.contains(wac.getServletContext().getServletContextName()))
				{
					wac.addLifeCycleListener(this);
					if (wac.isStarted())
					{
						startWebAppContext(wac);
					}
					
					monitoredContexts.add(wac.getContextPath());
				}
	}

	private void unregisterWebAppContext(WebAppContext wac) 
	{
		if (monitoredContexts.contains(wac.getServletContext().getServletContextName()))
	      {
	         monitoredContexts.remove(wac.getServletContext().getServletContextName());

	         //
	         if (wac.isStarted())
	         {
	            stopWebAppContext(wac);
	         }

	         //TODO: remove event listener from the webappcontext
	         wac.removeLifeCycleListener(this);
	      }
	}

	private void registerContextHandlerCollection(ContextHandlerCollection chc)
	{
		if (!monitoredContextHandlerCollection.contains(chc.toString())) 
		{
			Handler[] children = chc.getChildHandlersByClass(WebAppContext.class);
			for (int i = 0; i < children.length; i++) 
			{
				WebAppContext webAppContext = (WebAppContext)children[i];
				registerWebAppContext(webAppContext);
			}
		}

		monitoredContextHandlerCollection.add(chc.toString());
	}

	private void unregisterContextHandlerCollection(ContextHandlerCollection chc)
	{	
		if (monitoredContextHandlerCollection.contains(chc.toString()))
		{
			monitoredContextHandlerCollection.remove(chc.toString());
			
			Handler[] children = chc.getChildHandlersByClass(WebAppContext.class);
			for (int i = 0; i < children.length; i++)
			{
				WebAppContext webAppContext = (WebAppContext)children[i];
				unregisterWebAppContext(webAppContext);
			}
		}	
	}

	public void lifeCycleFailure(LifeCycle lifeCycle, Throwable t)
	{
		//ignore event
	}

	public void lifeCycleStarted(LifeCycle lifeCycle)
	{
		startWebAppContext((WebAppContext)lifeCycle);
	}

	public void lifeCycleStarting(LifeCycle lifeCycle)
	{
		//ignore event
	}

	public void lifeCycleStopped(LifeCycle lifeCycle)
	{
		stopWebAppContext((WebAppContext)lifeCycle);
	}

	public void lifeCycleStopping(LifeCycle lifeCycle)
	{
		//Ignore event
	}
	
	private boolean isDisabledNativeRegistration(ServletContext servletContext)
	{
	   if (servletContext != null)
	   {
	      String disableWCINativeRegistration = servletContext.getInitParameter(GateInServlet.WCIDISABLENATIVEREGISTRATION);
	      if (disableWCINativeRegistration != null && disableWCINativeRegistration.equalsIgnoreCase("true"))
	      {
	         return true;
	      }
	      else
	      {
	         return false;
	      }
	   }
	   else
	   {
	      return false;
	   }
	}

   @Override
   public void registerWebApp(org.gatein.wci.spi.WebAppContext webappContext, String dispatchPath)
   {
      if (isDisabledNativeRegistration(webappContext.getServletContext()))
      {
         this.manualMonitoredContexts.put(webappContext.getServletContext().getServletContextName(), dispatchPath);
         registration.registerWebApp(webappContext);
      }
   }

   @Override
   public void unregisterWebApp(ServletContext servletContext)
   {
      this.manualMonitoredContexts.remove(servletContext.getServletContextName());
      registration.unregisterWebApp(servletContext.getContextPath());
   }
}

