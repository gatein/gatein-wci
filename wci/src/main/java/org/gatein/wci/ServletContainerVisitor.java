package org.gatein.wci;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public interface ServletContainerVisitor
{
   /**
    * During visitation get reference to current WebApp
    *
    * @param webApp Currently visited WebApp
    */
   public void accept(WebApp webApp);
}
