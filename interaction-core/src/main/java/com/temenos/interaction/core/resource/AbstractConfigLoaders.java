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

import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.entity.vocabulary.TermFactory;

/**
 * 
 * This class provides an abstraction from the underlying mechanism used to load
 * config files
 *
 *
 * 
 * @author mohamednazir
 *
 */
public interface AbstractConfigLoaders {

    public InputStream load(String filename) throws Exception;

    public Metadata parseMetadataXML(String entityName, TermFactory termFactory);
    
    public Metadata parseMetadataXML(TermFactory termFactory);

    public void setIrisConfigDirPath(String location);

}
