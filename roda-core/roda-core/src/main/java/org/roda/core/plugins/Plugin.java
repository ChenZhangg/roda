/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants.PreservationAgentType;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.storage.StorageService;

/**
 * This interface should be implemented by any class that want to be a RODA
 * plugin.
 * 
 * @author Rui Castro
 * @author Luis Faria<lfaria@keep.p>
 */
public interface Plugin<T extends Serializable> {

  /**
   * Initializes this {@link Plugin}. This method is called by the
   * {@link PluginManager} before any other methods in the plugin.
   * 
   * @throws PluginException
   */
  public void init() throws PluginException;

  /**
   * Stops all {@link Plugin} activity. This is the last method to be called by
   * {@link PluginManager} on the {@link Plugin}.
   */
  public void shutdown();

  /**
   * Returns the name of this {@link Plugin}.
   * 
   * @return a {@link String} with the name of this {@link Plugin}.
   */
  public String getName();

  /**
   * Returns the version of this {@link Plugin}.
   * 
   * @return a <code>String</code> with the version number for this
   *         {@link Plugin}.
   */
  public String getVersion();

  /**
   * Returns description of this {@link Plugin}.
   * 
   * @return a {@link String} with the description of this {@link Plugin}.
   */
  public String getDescription();

  /**
   * Returns the type of the agent linked to this {@link Plugin}.
   * 
   * @return a {@link PreservationAgentType} with the type of the agent of this
   *         {@link Plugin}.
   */
  public PreservationAgentType getAgentType();

  /**
   * Returns the type of the execution preservation event linked to this
   * {@link Plugin}.
   * 
   * @return a {@link PreservationEventType} with the type of the execution
   *         event of this {@link Plugin}.
   */
  public PreservationEventType getPreservationEventType();

  /**
   * Returns the description of the execution preservation event linked to this
   * {@link Plugin}.
   * 
   * @return a {@link String} with the description of the execution event of
   *         this {@link Plugin}.
   */
  public String getPreservationEventDescription();

  /**
   * Returns the success message of the execution preservation event linked to
   * this {@link Plugin}.
   * 
   * @return a {@link String} with the success message of the execution event of
   *         this {@link Plugin}.
   */
  public String getPreservationEventSuccessMessage();

  /**
   * Returns the failure message of the execution preservation event linked to
   * this {@link Plugin}.
   * 
   * @return a {@link String} with the failure message of the execution event of
   *         this {@link Plugin}.
   */
  public String getPreservationEventFailureMessage();

  /**
   * Returns the {@link List} of {@link PluginParameter}s necessary to run this
   * {@link Plugin}.
   * 
   * @return a {@link List} of {@link PluginParameter} with the parameters.
   */
  public List<PluginParameter> getParameters();

  /**
   * Gets the parameter values inside a {@link Map} with attribute names and
   * values.
   * 
   * @return a {@link Map} with the parameters name and value.
   */
  public Map<String, String> getParameterValues();

  /**
   * Sets the parameters returned by a previous call to
   * {@link Plugin#getParameters()}.
   * 
   * @param parameters
   *          a {@link List} of parameters.
   * 
   * @throws InvalidParameterException
   */
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException;

  public Report beforeBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException;

  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException;

  /**
   * Executes the {@link Plugin}.
   * 
   * @return a {@link Report} of the actions performed.
   * 
   * @throws PluginException
   */
  public Report execute(IndexService index, ModelService model, StorageService storage, List<T> list)
    throws PluginException;

  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException;

  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException;

  /**
   * Method to return Plugin type (so it can be grouped for different purposes)
   */
  public PluginType getType();

  /**
   * Method used by PluginManager to obtain a new instance of a plugin, from the
   * current loaded Plugin, to provide to PluginOrchestrator
   */
  public Plugin<T> cloneMe();

  /**
   * Method that validates the parameters provided to the Plugin
   * 
   * FIXME this should be changed to return a report
   */
  public boolean areParameterValuesValid();

  // TODO 20160222 hsilva: do we need this???
  // public Report getReport();
}
