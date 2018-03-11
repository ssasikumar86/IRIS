package com.temenos.interaction.springdsl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.temenos.interaction.core.hypermedia.MethodNotAllowedException;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.Transition;
import com.temenos.interaction.core.resource.AbstractConfigLoaders;
import com.temenos.interaction.core.resource.ConfigLoader;
import com.temenos.interaction.metadata.resource.MetadataResourceProvider;

/*
 * #%L
 * interaction-springdsl
 * %%
 * Copyright (C) 2012 - 2018 Temenos Holdings N.V.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

public class DatabaseSystemResourceStateLoader extends SpringDSLResourceStateProvider implements ResourceLoader {

    private static final String PROPERTY_FILE_EXT = ".properties";

    private static final String IRIS_T24_PREFIX = "IRIS-T24_";

    private static final String IRIS_COMMON_PREFIX = "IRIS-common_";

    private final Logger logger = LoggerFactory.getLogger(SpringDSLResourceStateProvider.class);

    private List<String> supportingFiles;

    private MetadataResourceProvider metadataResourceProvider;

    private ResourceState result;
    private String state;
    private List<String> attempts = new ArrayList<String>(2);
    private String foundFile;
    protected ConcurrentMap<String, ResourceState> resources;

    private AbstractConfigLoaders configLoader = new ConfigLoader();

    /**
     * @param beanXmlInputStream
     *            of the filename to load
     * @return a Spring ApplicationContext
     */
    public ApplicationContext createApplicationContext(InputStream beanXmlInputStream) {

        return new SpringStreamXmlApplicationContextProvider(beanXmlInputStream);
    }

    /**
     * @param beanXml
     *            the filename to load
     * @return a Spring ApplicationContext
     */
    public ApplicationContext createApplicationContext(String beanXml) {

        logger.debug("Looking the file for hash :[{}]  in Database", beanXml);
        try {
            InputStream inputStream = metadataResourceProvider.readFile(beanXml);
            if (inputStream == null) {
                return null;
            }
            return new SpringStreamXmlApplicationContextProvider(inputStream);

        } catch (Exception e) {
            logger.error("Error reading File [{}] from database", beanXml);
            return null;
        }
    }

    @Override
    public String getResourceStateId(String httpMethod, String url) throws MethodNotAllowedException {
        Map<String, String> methodToState = null;

        initialise();
        methodToState = pathTree.get(url);

        // load the method state from database;
        if (methodToState == null || methodToState.isEmpty()) {
            loadResourceStateFromDatabase(url);
            methodToState = pathTree.get(url);
        }

        String resourceStateId = null;

        if (methodToState != null) {
            resourceStateId = methodToState.get(httpMethod);
            if (resourceStateId == null) {
                if (pathTree.get(url) != null) {
                    Set<String> allowedMethods = pathTree.get(url).keySet();
                    throw new MethodNotAllowedException(allowedMethods);
                }
            }
        } else {
            return null;
        }

        return resourceStateId;
    }

    /**
     * @return the supportingFiles
     */
    public List<String> getSupportingFiles() {
        return supportingFiles;
    }

    @PostConstruct
    private void init() throws Exception {
        logger.debug("Loading Mandatory files from Database");
        List<InputStream> inputStreamList = new ArrayList<>();
        for (String filename : supportingFiles) {
            logger.debug("Loading file [{}] from Database", filename);

            inputStreamList = metadataResourceProvider.readListOfFiles(filename);
            if (!inputStreamList.isEmpty()) {
                for (InputStream inputStream : inputStreamList) {
                    loadandStoreResourceState(inputStream, filename);
                }
            }
        }
    }

    @Override
    public void initialise(Properties beanMap, ConcurrentMap<String, ResourceState> resources, ResourceState result) {
        this.beanMap = beanMap;
        this.resources = resources;
        this.result = result;

    }

    /**
     * Was the load operation successful?
     */
    public boolean isLoaded() {
        return (result != null);
    }

    /**
     * Load the configured resource state. Use this method only once. call
     * isLoaded() to discover success or failure
     */
    @Override
    public void load(String state) {

        boolean multipleFile = false;
        ApplicationContext context = null;

        List<InputStream> inStreamList = new ArrayList<InputStream>();

        String tmpResourceStateName = state;
        String tmpResourceName = tmpResourceStateName;

        if (tmpResourceName.contains("-")) {
            tmpResourceName = tmpResourceName.substring(0, tmpResourceName.indexOf("-"));
        }

        String beanXml = "IRIS-" + tmpResourceName + "-PRD.xml";

        inStreamList = metadataResourceProvider.readListOfFiles(beanXml);

        if (inStreamList != null) {

            List<Transition> tempTransitions = new ArrayList<Transition>();

            if (inStreamList.size() > 1) {
                multipleFile = true;
            }

            for (InputStream fileContentInputStream : inStreamList) {

                if (inStreamList.size() > 1) {
                    multipleFile = true;
                }

                logger.debug("Reading the file [{}] from database", beanXml);
                // Attempt to create Spring context based on current resource
                // filename pattern
                context = createApplicationContext(fileContentInputStream);

                if (context != null) {
                    result = loadAllResourceStatesFromFile(context, tmpResourceStateName);
                    tempTransitions.addAll(result.getTransitions());
                }
            }

            if (multipleFile) {
                result.getTransitions().clear();
                result.getTransitions().addAll(tempTransitions);

            }
        } else {

            if (context == null) {
                // Failed to create Spring context using current resource
                // filename pattern so use old pattern
                int pos = tmpResourceName.lastIndexOf("_");

                if (pos > 3) {
                    tmpResourceName = tmpResourceName.substring(0, pos);
                    beanXml = "IRIS-" + tmpResourceName + "-PRD.xml";

                    context = createApplicationContext(beanXml);

                    if (context != null) {
                        // Successfully created Spring context using old
                        // resource filename pattern

                        // Convert resource state name to old resource name
                        // format
                        pos = tmpResourceStateName.lastIndexOf("-");

                        if (pos < 0) {
                            pos = tmpResourceStateName.lastIndexOf("_");

                            if (pos > 0) {
                                tmpResourceStateName = tmpResourceStateName.substring(0, pos) + "-"
                                        + tmpResourceStateName.substring(pos + 1);
                            }
                        }
                    }
                }
                logger.warn("Error reading file: [{}] from database or file doesn't exist in Database.", beanXml);
            }
            if (context != null) {
                result = loadAllResourceStatesFromFile(context, tmpResourceStateName);
            }
        }
    }

    private ResourceState loadAllResourceStatesFromFile(ApplicationContext context, String resourceState) {
        Map<String, ResourceState> tmpResources = context.getBeansOfType(ResourceState.class);

        // Save all the loaded resources into the main resource state cache
        resources.putAll(tmpResources);

        ResourceState result = null;

        if (tmpResources.containsKey(resourceState)) {
            result = tmpResources.get(resourceState);
        }

        return result;
    }

    private void loadandStoreResourceState(InputStream inputStream, String fname) throws IOException {

        logger.debug("Loading resource State from databse file :", fname);
        Properties property = new Properties();
        property.load(inputStream);
        super.initialised = false;
        super.setResourceMap(property);
        super.initialise();
    }

    /**
     * Get the Resource State from a successful load
     */
    public ResourceState loaded() {
        return result;
    }

    /*
     * Method to load and store the resource files from database.
     * 
     * @param URL
     * 
     * @return void.
     */
    private void loadResourceStateFromDatabase(String url) {

        // EDP load the .properties file form Database and check
        InputStream inputStream;
        String applicationName = url;

        if (applicationName.contains("(")) {
            // EG: IRIS-T24_verSector('123') ==> verSector
            applicationName = applicationName.substring(applicationName.indexOf("/") + 1,
                    applicationName.indexOf("(") - 1);
        }
        if (applicationName.contains("/")) {
            // EG: /GB0001/IRIS-T24_verSector('123') ==> verSector
            applicationName = applicationName.substring(applicationName.lastIndexOf("/") + 1, applicationName.length());
        }
        String fname = IRIS_T24_PREFIX + applicationName + PROPERTY_FILE_EXT;
        try {
            inputStream = metadataResourceProvider.readFile(fname);
            if (inputStream == null && StringUtils.containsIgnoreCase(applicationName, "entry")) {
                // There are cases where the request contains some "Entry"
                // string appended to the request,
                // EG , verCustomerEntry, for which we don't have a property
                // file as "verCustomerEntry" so remove this entry and try to
                // process
                applicationName = applicationName.replaceAll("(?i)Entry", "");
                fname = IRIS_T24_PREFIX + applicationName + PROPERTY_FILE_EXT;
                inputStream = metadataResourceProvider.readFile(fname);
            }
            if (inputStream == null) {
                // only for those files that have "IRIS-common_*.properties"
                // in it's name
                applicationName = applicationName.substring(0, 1).toUpperCase() + applicationName.substring(1);
                fname = IRIS_COMMON_PREFIX + applicationName + PROPERTY_FILE_EXT;
                inputStream = metadataResourceProvider.readFile(fname);
            }
            if (inputStream != null) {
                loadandStoreResourceState(inputStream, fname);
            }

        } catch (Exception e) {
            logger.error("Error loading file [{}] form database", fname, e.getCause());
        }
    }

    /**
     * @param supportingFiles
     *            the supportingFiles to set
     */
    public void setSupportingFiles(List<String> supportingFiles) {
        this.supportingFiles = supportingFiles;
    }

    /**
     * Description of the state of this load, intended for logging
     */
    public String toString() {
        if (isLoaded()) {
            return "Loaded Resource State " + state + " from " + foundFile;
        } else if (attempts.size() == 0) {
            return "Not-loaded Resource State " + state;
        } else if (foundFile != null) {
            return "State " + state + " not found in " + foundFile;
        }

        StringBuilder msg = new StringBuilder("Failed to load resource state ");
        msg.append(state);
        msg.append(". Attempted to load from ");
        for (int i = 0; i < attempts.size(); ++i) {
            if (i > 0)
                msg.append(", ");
            msg.append("[");
            msg.append(attempts.get(i));
            msg.append("]");
        }
        return msg.toString();
    }

    /**
     * @param metadataResourceProvider
     *            the metadataResourceProvider to set
     */
    public void setMetadataResourceProvider(MetadataResourceProvider metadataResourceProvider) {
        this.metadataResourceProvider = metadataResourceProvider;
    }

    @Override
    public void setIrisConfigDirPath(String location) {
        configLoader.setIrisConfigDirPath(location);

    }

}