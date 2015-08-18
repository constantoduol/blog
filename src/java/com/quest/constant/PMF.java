package com.quest.constant;



import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

/**
 * Singleton for the App Engine Data Store persistence manager.
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public final class PMF {
  /**
   * Constructor.
   */
  private PMF() {}

  private static final PersistenceManagerFactory pmfInstance =
      JDOHelper.getPersistenceManagerFactory("transactions-optional");

  /**
   * Returns an instance of the Persistence Manager.
   * @return persistence manager instance.
   */
  public static PersistenceManagerFactory getInstance() {
    return pmfInstance;
  }
}