package com.temenos.interaction.core.resource;

/*
 * #%L
 * interaction-core
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

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.entity.MetadataParser;
import com.temenos.interaction.core.entity.vocabulary.TermFactory;
import com.temenos.interaction.metadata.resource.MetadataResourceProvider;

/**
 * Config Loader to load file from Database.
 *
 * @author mohamednazir
 *
 */
public class DatabaseSystemConfigLoader implements AbstractConfigLoaders {
    
    private final static Logger logger = LoggerFactory.getLogger(DatabaseSystemConfigLoader.class);   
    
    private MetadataResourceProvider metadataResourceProvider;
    
    
    @Override
    public InputStream load(String filename) throws Exception {
        
        return metadataResourceProvider.readFile(filename);
    }
    
    @Override
    public Metadata parseMetadataXML(String entityName, TermFactory termFactory) {
        String metadataFilename;

        metadataFilename = "metadata-" + entityName + ".xml";
        try (InputStream is = metadataResourceProvider.readFile(metadataFilename);) {
            if(is == null){
                logger.warn("Unable to load metadata file : [{}], check whether the metadata file is avavilable in Database.",metadataFilename);
            }
            return new MetadataParser(termFactory).parse(is);
        } catch (Exception e) {
            logger.debug("Failed to parse " + metadataFilename + ": ", e);
            throw new RuntimeException("Failed to parse " + metadataFilename + ": ", e);
        }
    }

    /**
     * @param metadataResourceProvider the metadataResourceProvider to set
     */
    public void setMetadataResourceProvider(MetadataResourceProvider metadataResourceProvider) {
        this.metadataResourceProvider = metadataResourceProvider;
    }

    @Override
    public Metadata parseMetadataXML(TermFactory termFactory) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setIrisConfigDirPath(String location) {
        // TODO Auto-generated method stub
        
    }


  
}
