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

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.temenos.interaction.metadata.resource.MetadataResourceProvider;

@RunWith(MockitoJUnitRunner.class)
public class TestDatabaseSystemConfigLoader {
    
    DatabaseSystemConfigLoader confLoader = new DatabaseSystemConfigLoader();
    
    MetadataResourceProvider metadataResourceProvider = Mockito.mock(MetadataResourceProvider.class);
    
    InputStream inputStream = null;
  
    @Before
    public void setUp() throws Exception {
        inputStream = this.getClass().getClassLoader().getResourceAsStream("metadata-CountryList.xml");
       
    }
    
    @Test
    public void testLoad() throws Exception {
        
        confLoader.setMetadataResourceProvider(metadataResourceProvider);
        Mockito.when(metadataResourceProvider.readFile(Mockito.anyString())).thenReturn(inputStream);
        InputStream in = confLoader.load("metadata-CountryList.xml");
        assertNotNull(in);
    }
    
}
