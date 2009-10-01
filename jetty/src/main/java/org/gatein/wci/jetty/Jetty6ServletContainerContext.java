//package org.jboss.portal.web.impl.jetty;
//
//import java.io.IOException;
//import java.util.HashSet;
//import java.util.Set;
//
//import javax.servlet.ServletContext;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.jboss.portal.web.RequestDispatchCallback;
//import org.jboss.portal.web.command.CommandDispatcher;
//import org.jboss.portal.web.impl.DefaultServletContainerFactory;
//import org.jboss.portal.web.spi.ServletContainerContext;
//import org.mortbay.component.Container;
//import org.mortbay.component.LifeCycle;
//import org.mortbay.component.LifeCycle.Listener;
//import org.mortbay.component.Container.Relationship;
//import org.mortbay.jetty.Handler;
//import org.mortbay.jetty.Server;
//import org.mortbay.jetty.handler.ContextHandlerCollection;
//import org.mortbay.jetty.webapp.WebAppContext;
//
//public class Jetty6ServletContainerContext  implements ServletContainerContext, org.mortbay.component.Container.Listener, LifeCycle.Listener
//{
//
//	private Registration registration;
//	
//	private Container container;
//	private Server server;
//	private ContextHandlerCollection chc;
//	
//	   /** The monitored contexts. */
//	   private final Set<String> monitoredContexts = new HashSet<String>();
//	   
//	   private final Set<String> monitoredContextHandlerCollection = new HashSet<String>();
//	
//	public Jetty6ServletContainerContext(Server server) {
//		this.server = server;
//		this.container = server.getContainer();
//	}
//	
//	   /** . */
//	private final CommandDispatcher dispatcher = new CommandDispatcher();
//	
//	public Object include(ServletContext targetServletContext,
//			HttpServletRequest request, HttpServletResponse response,
//			RequestDispatchCallback callback, Object handback)
//			throws ServletException, IOException 
//    {
//		return dispatcher.include(targetServletContext, request, response,
//				callback, handback);
//	}
//
//	public void setCallback(Registration registration) {
//		this.registration = registration;
//	}
//
//	public void unsetCallback(Registration registration) {
//		this.registration = null;
//	}
//
//
//	public void start()
//	{
//		DefaultServletContainerFactory.registerContext(this);
//		
//        Handler[] children = server.getChildHandlersByClass(ContextHandlerCollection.class);
//		for (int i = 0; i < children.length; i++)
//		{
//				ContextHandlerCollection chc = (ContextHandlerCollection)children[i];
//				registerContextHandlerCollection(chc);
//		}
//		container.addEventListener(this);
//	}
//	
//	public void stop()
//	{
//		container.removeEventListener(this);
//		
//		Handler[] children = server.getChildHandlersByClass(ContextHandlerCollection.class);
//		for (int i=0; i< children.length; i++)
//		{
//			ContextHandlerCollection chc = (ContextHandlerCollection)children[i];
//			unregisterContextHandlerCollection(chc);
//		}
//		
//		registration.cancel();
//		registration = null;
//	}
//
//	public void addBean(Object bean) 
//	{
//		if (bean instanceof ContextHandlerCollection)
//		{
//			ContextHandlerCollection chc = (ContextHandlerCollection)bean;
//			registerContextHandlerCollection(chc);
//		}
//		else if (bean instanceof WebAppContext)
//		{
//			WebAppContext wac = (WebAppContext)bean;
//			registerWebAppContext(wac);
//		}
//	}
//
//	public void removeBean(Object bean) 
//	{
//		if (bean instanceof ContextHandlerCollection)
//		{
//			ContextHandlerCollection chc = (ContextHandlerCollection)bean;
//			unregisterContextHandlerCollection(chc);
//		}
//		else if (bean instanceof WebAppContext)
//		{
//			WebAppContext wac = (WebAppContext)bean;
//			unregisterWebAppContext(wac);
//		}
//	}
//
//	public void add(Relationship relationship) 
//	{
//		//ignore event for now
//	}
//
//	public void remove(Relationship relationship) 
//	{
//		//ignore event for now
//	}
//
//	private void startWebAppContext(WebAppContext webappContext) 
//	{
//	      try
//	      {
//	         Jetty6WebAppContext context = new Jetty6WebAppContext(webappContext);
//	         
//	         //
//	         if (registration != null)
//	         {
//	            registration.registerWebApp(context);
//	         }
//	      }
//	      catch (Exception e)
//	      {
//	         e.printStackTrace();
//	      }
//		
//	}
//
//	private void stopWebAppContext(WebAppContext webappContext) 
//	{
//	      try
//	      {
//	         if (registration != null)
//	         {
//	            registration.unregisterWebApp(webappContext.getContextPath());
//	         }
//	      }
//	      catch (Exception e)
//	      {
//	         e.printStackTrace();
//	      }
//	}
//
//	private void registerWebAppContext(WebAppContext wac) 
//	{
//		// using servletContext since its the standard object and not jetty specific
//		// (need standard object when using ServletContextListener).
//		if (!monitoredContexts.contains(wac.getServletContext().getServletContextName()))
//				{
//					wac.addLifeCycleListener(this);
//					if (wac.isStarted())
//					{
//						startWebAppContext(wac);
//					}
//					
//					monitoredContexts.add(wac.getContextPath());
//				}
//	}
//
//	private void unregisterWebAppContext(WebAppContext wac) 
//	{
//		System.out.println("UNREGISTERWEBAPPCONTEXT : " + wac);
//		if (monitoredContexts.contains(wac.getServletContext().getServletContextName()))
//	      {
//	         monitoredContexts.remove(wac.getServletContext().getServletContextName());
//
//	         //
//	         if (wac.isStarted())
//	         {
//	            stopWebAppContext(wac);
//	         }
//
//	         //TODO: remove event listener from the webappcontext
//	         wac.removeLifeCycleListener(this);
//	      }
//	}
//
//	private void registerContextHandlerCollection(ContextHandlerCollection chc)
//	{
//		if (!monitoredContextHandlerCollection.contains(chc.toString())) 
//		{
//			Handler[] children = chc.getChildHandlersByClass(WebAppContext.class);
//			for (int i = 0; i < children.length; i++) 
//			{
//				WebAppContext webAppContext = (WebAppContext)children[i];
//				registerWebAppContext(webAppContext);
//			}
//		}
//
//		monitoredContextHandlerCollection.add(chc.toString());
//	}
//
//	private void unregisterContextHandlerCollection(ContextHandlerCollection chc)
//	{	
//		if (monitoredContextHandlerCollection.contains(chc.toString()))
//		{
//			monitoredContextHandlerCollection.remove(chc.toString());
//			
//			Handler[] children = chc.getChildHandlersByClass(WebAppContext.class);
//			for (int i = 0; i < children.length; i++)
//			{
//				WebAppContext webAppContext = (WebAppContext)children[i];
//				unregisterWebAppContext(webAppContext);
//			}
//		}	
//	}
//
//	public void lifeCycleFailure(LifeCycle lifeCycle, Throwable t)
//	{
//		//ignore event
//	}
//
//	public void lifeCycleStarted(LifeCycle lifeCycle)
//	{
//		startWebAppContext((WebAppContext)lifeCycle);
//	}
//
//	public void lifeCycleStarting(LifeCycle lifeCycle)
//	{
//		//ignore event
//	}
//
//	public void lifeCycleStopped(LifeCycle lifeCycle)
//	{
//		stopWebAppContext((WebAppContext)lifeCycle);
//	}
//
//	public void lifeCycleStopping(LifeCycle lifeCycle)
//	{
//		//Ignore event
//	}
//	
//}
//
