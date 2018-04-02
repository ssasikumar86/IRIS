package com.temenos.interaction.springdsl;

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

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

/**
 * This class  creates and loads the Bean definition from the Inputstream.
 *
 * @author mohamednazir
 *
 */
public class SpringStreamXmlApplicationContextProvider extends AbstractXmlApplicationContext {

    private Resource[] configResources = null;

    public SpringStreamXmlApplicationContextProvider(InputStream configFileStream) {
        super();
        this.configResources = new Resource[] { new InputStreamResource(configFileStream) };
        this.refresh();
    }

    @Override
    protected Resource[] getConfigResources() {
        return this.configResources;
    }

    @Override
    public void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
        reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
        super.loadBeanDefinitions(reader);
    }
}
