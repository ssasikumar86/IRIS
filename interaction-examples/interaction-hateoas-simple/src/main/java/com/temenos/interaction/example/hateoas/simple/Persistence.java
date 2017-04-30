package com.temenos.interaction.example.hateoas.simple;

/*
 * #%L
 * interaction-example-hateoas-simple
 * %%
 * Copyright (C) 2012 - 2013 Temenos Holdings N.V.
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


import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.example.hateoas.simple.model.Note;
import com.temenos.interaction.example.hateoas.simple.model.Profile;

public class Persistence {
    private final static Logger logger = LoggerFactory.getLogger(Persistence.class);

    @PersistenceContext(unitName = "ResponderServiceHibernate", type = PersistenceContextType.EXTENDED)
    @Access(AccessType.FIELD) 
    private EntityManager entityManager;

    public Persistence(EntityManagerFactory entityManagerFactory) {
    	entityManager = entityManagerFactory.createEntityManager();
    }

	@SuppressWarnings("unchecked")
	public List<Note> getNotes() {
		List<Note> entities = null;
		try {
			Query jpaQuery = entityManager.createQuery("SELECT n FROM note n");
			entities = jpaQuery.getResultList();
		} catch(Exception e) {
			logger.error("Error while loading entities: ", e);
		}
		return entities;
    }

	public Note getNote(Long id) {
		Note note = null;
		try {
			note = entityManager.find(Note.class, id);
		} catch(Exception e) {
			logger.error("Error while loading entity [" + id + "]: ", e);
		}
		return note;
    }

	public Note removeNote(Long id) {
		Note note = null;
		try {
    		entityManager.getTransaction().begin();
			note = entityManager.find(Note.class, id);
			entityManager.remove(note);
    		entityManager.getTransaction().commit();    		
		} catch(Exception e) {
			logger.error("Error while removing entity [" + id + "]: ", e);
		}
		return note;
    }
	
	public Note insertNote(Note note) {
		try {
    		entityManager.getTransaction().begin();
			//note = entityManager.find(Note.class, id);
			entityManager.persist(note);
    		entityManager.getTransaction().commit();    		
		} catch(Exception e) {
			logger.error("Error while removing entity [" + note.getNoteID() + "]: ", e);
		}
		return note;
    }

    @SuppressWarnings("unchecked")
    public List<Profile> getProfiles() {
        List<Profile> entities = null;
        try {
            Query jpaQuery = entityManager.createQuery("SELECT p FROM Profile p");
            entities = jpaQuery.getResultList();
        } catch (Exception e) {
            logger.error("Error while loading entities: ", e);
        }
        return entities;
    }

    public Profile getProfile(String profileId) {
        Profile profile = null;
        try {
            profile = entityManager.find(Profile.class, profileId);
        } catch (Exception e) {
            logger.error("Error while loading entity [" + profileId + "]: ", e);
        }
        return profile;
    }

}
