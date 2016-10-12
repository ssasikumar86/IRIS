package com.temenos.interaction.rimdsl.generator.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Injector;
import com.temenos.interaction.core.entity.EntityMetadata;
import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.entity.vocabulary.Vocabulary;
import com.temenos.interaction.core.entity.vocabulary.terms.TermComplexGroup;
import com.temenos.interaction.core.entity.vocabulary.terms.TermComplexType;
import com.temenos.interaction.core.entity.vocabulary.terms.TermListType;
import com.temenos.interaction.core.entity.vocabulary.terms.TermValueType;
import com.temenos.interaction.rimdsl.RIMDslStandaloneSetup;
import com.temenos.interaction.rimdsl.RIMDslStandaloneSetupSpringPRD;
import com.temenos.interaction.rimdsl.RIMDslStandaloneSetupSwagger;

public class GeneratorTest {
	private File validGenJavaDir = new File("target/valid-gen-java");
	private File invalidGenJavaDir = new File("target/invalid-gen-java");
	
	
	@Before
	public void setUp() throws Exception {
		createNewDir(validGenJavaDir);
		createNewDir(invalidGenJavaDir);		
	}

	private void createNewDir(File dir) {
		if(dir.exists()) {
			dir.delete();
		}
		
		dir.mkdirs();
	}
	
	@Test
	public void testStandaloneWithoutListenerValidRim() {
		Injector injector = new RIMDslStandaloneSetup().createInjectorAndDoEMFRegistration();
		Generator generator = injector.getInstance(Generator.class);
				
		boolean result = generator.runGenerator("src/test/resources/valid.rim", validGenJavaDir.getPath());
		
		assertTrue(result);
	}
	

	@Test
	public void testStandaloneSpringPRDWithoutListenerValidRim() {
		Injector injector = new RIMDslStandaloneSetupSpringPRD().createInjectorAndDoEMFRegistration();
		Generator generator = injector.getInstance(Generator.class);
				
		boolean result = generator.runGenerator("src/test/resources/valid.rim", validGenJavaDir.getPath());
		
		assertTrue(result);
	}
	

	@Test
	public void testStandaloneListenerWithValidRim() {
		Injector injector = new RIMDslStandaloneSetup().createInjectorAndDoEMFRegistration();
		Generator generator = injector.getInstance(Generator.class);
		
		ValidatorEventListener listener = mock(ValidatorEventListener.class);
		
		generator.setValidatorEventListener(listener);
		
		generator.runGenerator("src/test/resources/valid.rim", validGenJavaDir.getPath());
		
		verify(listener, times(0)).notify(anyString());
	}

	
	@Test
	public void testStandaloneSpringPRDListenerWithRestbucksRim() {
		Injector injector = new RIMDslStandaloneSetupSpringPRD().createInjectorAndDoEMFRegistration();
		Generator generator = injector.getInstance(Generator.class);
		
		ValidatorEventListener listener = mock(ValidatorEventListener.class);
		
		generator.setValidatorEventListener(listener);
		
		generator.runGenerator("src/test/resources/Restbucks.rim", validGenJavaDir.getPath());
		
		verify(listener, times(0)).notify(anyString());
	}

	
	@Test
	public void testStandaloneListenerWithInvalidRim() {
		Injector injector = new RIMDslStandaloneSetup().createInjectorAndDoEMFRegistration();
		Generator generator = injector.getInstance(Generator.class);
		
		ValidatorEventListener listener = mock(ValidatorEventListener.class);
		
		generator.setValidatorEventListener(listener);
		
		generator.runGenerator("src/test/resources/invalid.rim", invalidGenJavaDir.getPath());
		
		verify(listener, times(1)).notify(anyString());
	}
	
	@Test
	public void testStandaloneSpringPRDListenerWithInvalidRim() {
		Injector injector = new RIMDslStandaloneSetupSpringPRD().createInjectorAndDoEMFRegistration();
		Generator generator = injector.getInstance(Generator.class);
		
		ValidatorEventListener listener = mock(ValidatorEventListener.class);
		
		generator.setValidatorEventListener(listener);
		
		generator.runGenerator("src/test/resources/invalid.rim", invalidGenJavaDir.getPath());
		
		verify(listener, times(1)).notify(anyString());
	}

	@Test
	public void testStandaloneSpringPRDListenerWithValidRim() {
		Injector injector = new RIMDslStandaloneSetupSpringPRD().createInjectorAndDoEMFRegistration();
		Generator generator = injector.getInstance(Generator.class);
		
		ValidatorEventListener listener = mock(ValidatorEventListener.class);
		
		generator.setValidatorEventListener(listener);
		
		generator.runGenerator("src/test/resources/valid.rim", validGenJavaDir.getPath());
		
		verify(listener, times(0)).notify(anyString());
	}		
	
	@Test
	public void testStandaloneSpringPRDListenerWithNotesRim() {
		Injector injector = new RIMDslStandaloneSetupSpringPRD().createInjectorAndDoEMFRegistration();
		Generator generator = injector.getInstance(Generator.class);
		
		ValidatorEventListener listener = mock(ValidatorEventListener.class);
		
		generator.setValidatorEventListener(listener);
		
		generator.runGenerator("src/test/resources/Notes.rim", validGenJavaDir.getPath());
		
		verify(listener, times(0)).notify(anyString());
	}	
	
	@Test
	public void testStandaloneSpringPRDListenerWithSimpleRim() {
		Injector injector = new RIMDslStandaloneSetupSpringPRD().createInjectorAndDoEMFRegistration();
		Generator generator = injector.getInstance(Generator.class);
		
		ValidatorEventListener listener = mock(ValidatorEventListener.class);
		
		generator.setValidatorEventListener(listener);
		
		generator.runGenerator("src/test/resources/Simple.rim", validGenJavaDir.getPath());
		
		verify(listener, times(0)).notify(anyString());
	}
	
	@Test
	public void testStandaloneSpringPRDListenerWithRestbucksim() {
		Injector injector = new RIMDslStandaloneSetupSpringPRD().createInjectorAndDoEMFRegistration();
		Generator generator = injector.getInstance(Generator.class);
		
		ValidatorEventListener listener = mock(ValidatorEventListener.class);
		
		generator.setValidatorEventListener(listener);
		
		generator.runGenerator("src/test/resources/Restbucks.rim", validGenJavaDir.getPath());
		
		verify(listener, times(0)).notify(anyString());
	}
	
	@Test
    public void testStandaloneSpringPRDListenerWithSimpleRimAndMetadata() {
        Injector injector = new RIMDslStandaloneSetupSpringPRD().createInjectorAndDoEMFRegistration();
        Generator generator = injector.getInstance(Generator.class);        
        ValidatorEventListener listener = mock(ValidatorEventListener.class);        
        generator.setValidatorEventListener(listener);
        Metadata metadata = new Metadata("metadata");
        metadata.setEntityMetadata(new EntityMetadata("Note"));     
        generator.runGenerator("src/test/resources/Simple.rim", metadata, validGenJavaDir.getPath());        
        verify(listener, times(0)).notify(anyString());
    }
	
	@Test
    public void testStandaloneSetupSpringPRDListenerWithRimDirAndMetadata() {
        Injector injector = new RIMDslStandaloneSetupSpringPRD().createInjectorAndDoEMFRegistration();
        Generator generator = injector.getInstance(Generator.class);        
        ValidatorEventListener listener = mock(ValidatorEventListener.class);        
        generator.setValidatorEventListener(listener);        
        Metadata metadata = new Metadata("metadata");
        metadata.setEntityMetadata(new EntityMetadata("Note"));     
        generator.runGeneratorDir("src/test/resources/Simple.rim", metadata, validGenJavaDir.getPath());        
        verify(listener, times(0)).notify(anyString());
    }
	
    @Test
    public void testStandaloneSetupSpringPRDListenerWithRimDirAndNoMetadata() {
        Injector injector = new RIMDslStandaloneSetupSpringPRD().createInjectorAndDoEMFRegistration();
        Generator generator = injector.getInstance(Generator.class);        
        ValidatorEventListener listener = mock(ValidatorEventListener.class);        
        generator.setValidatorEventListener(listener);
        generator.runGeneratorDir("src/test/resources/Simple.rim", validGenJavaDir.getPath());        
        verify(listener, times(0)).notify(anyString());
    }
	
	@Test
    public void testStandaloneSetupSwaggerListenerWithSimpleRimAndMetadata() {
        Injector injector = new RIMDslStandaloneSetupSwagger().createInjectorAndDoEMFRegistration();
        Generator generator = injector.getInstance(Generator.class);
        ValidatorEventListener listener = mock(ValidatorEventListener.class);
        generator.setValidatorEventListener(listener);
        Metadata metadata = new Metadata("metadata");
        EntityMetadata em = new EntityMetadata("Note");
        
        Vocabulary vocId = new Vocabulary();
        vocId.setTerm(new TermValueType(TermValueType.TEXT));
        em.setPropertyVocabulary("name", vocId);
        
        Vocabulary vocNotes = new Vocabulary();
        vocNotes.setTerm(new TermListType(true));
        vocNotes.setTerm(new TermComplexType(true));
        em.setPropertyVocabulary("notes", vocNotes);        
        
        Vocabulary vocNoteText = new Vocabulary();
        vocNoteText.setTerm(new TermValueType(TermValueType.TEXT));
        vocNoteText.setTerm(new TermComplexGroup("notes"));
        em.setPropertyVocabulary("Text", vocNoteText, Collections.enumeration(Collections.singletonList("notes")));   
        
        Vocabulary vocNoteNumbers = new Vocabulary();
        vocNoteNumbers.setTerm(new TermListType(true));
        vocNotes.setTerm(new TermComplexType(true));
        em.setPropertyVocabulary("Numbers", vocNoteNumbers, Collections.enumeration(Collections.singletonList("notes")));
        
        Vocabulary vocNoteNumber = new Vocabulary();
        vocNoteNumber.setTerm(new TermValueType(TermValueType.TEXT));
        vocNoteNumber.setTerm(new TermComplexGroup("Numbers"));
        em.setPropertyVocabulary("Number", vocNoteNumber, Collections.enumeration(Collections.singletonList("Numbers")));
        
        
        metadata.setEntityMetadata(em);
        generator.runGenerator("src/test/resources/Simple.rim", metadata, validGenJavaDir.getPath());
        verify(listener, times(0)).notify(anyString());
    }
	
	@Test
    public void testStandaloneSetupSwaggerListenerWithDirAndMetadata() {
        Injector injector = new RIMDslStandaloneSetupSwagger().createInjectorAndDoEMFRegistration();
        Generator generator = injector.getInstance(Generator.class);
        ValidatorEventListener listener = mock(ValidatorEventListener.class);
        generator.setValidatorEventListener(listener);
        Metadata metadata = new Metadata("metadata");
        metadata.setEntityMetadata(new EntityMetadata("Note"));
        generator.runGeneratorDir("src/test/resources/Simple.rim", metadata, validGenJavaDir.getPath());
        verify(listener, times(0)).notify(anyString());
    }
	
	@Test
    public void testStandaloneSetupSwaggerListenerWithDirAndNoMetadata() {
        Injector injector = new RIMDslStandaloneSetupSwagger().createInjectorAndDoEMFRegistration();
        Generator generator = injector.getInstance(Generator.class);
        ValidatorEventListener listener = mock(ValidatorEventListener.class);
        generator.setValidatorEventListener(listener);
        generator.runGeneratorDir("src/test/resources/Simple.rim", validGenJavaDir.getPath());
        verify(listener, times(0)).notify(anyString());
    }
	
	@Test
    public void testStandaloneSetupSwaggerListenerWithInvalidRimOnDirAndNoMetadata() {
        Injector injector = new RIMDslStandaloneSetupSwagger().createInjectorAndDoEMFRegistration();
        Generator generator = injector.getInstance(Generator.class);
        ValidatorEventListener listener = mock(ValidatorEventListener.class);
        generator.setValidatorEventListener(listener);
        generator.runGeneratorDir("src/test/resources/invalid.rim", validGenJavaDir.getPath());
        verify(listener, times(1)).notify(anyString());
    }
	
	@Test
    public void testStandaloneSetupSwaggerListenerWithInvalidRimOnDirAndMetadata() {
        Injector injector = new RIMDslStandaloneSetupSwagger().createInjectorAndDoEMFRegistration();
        Generator generator = injector.getInstance(Generator.class);
        ValidatorEventListener listener = mock(ValidatorEventListener.class);
        Metadata metadata = new Metadata("metadata");
        metadata.setEntityMetadata(new EntityMetadata("ENTITY"));
        generator.setValidatorEventListener(listener);
        generator.runGeneratorDir("src/test/resources/invalid.rim", metadata, validGenJavaDir.getPath());
        verify(listener, times(1)).notify(anyString());
    }

	@Test
	public void testComplexTypeHandler() {
	    Injector injector = new RIMDslStandaloneSetupSpringPRD().createInjectorAndDoEMFRegistration();
	    Generator generator = injector.getInstance(Generator.class);	    
	    EntityMetadata em = new EntityMetadata("Riders");	    
	    Vocabulary vocId = new Vocabulary();
	    vocId.setTerm(new TermValueType(TermValueType.TEXT));
	    em.setPropertyVocabulary("name", vocId);	    
	    Vocabulary vocBody = new Vocabulary();
        vocBody.setTerm(new TermValueType(TermValueType.INTEGER_NUMBER));
        em.setPropertyVocabulary("age", vocBody);
        Vocabulary vocRides = new Vocabulary();
        vocRides.setTerm(new TermListType(true));
        vocRides.setTerm(new TermComplexType(true));
        em.setPropertyVocabulary("rides", vocRides);        
        Vocabulary vocHorseName = new Vocabulary();
        vocHorseName.setTerm(new TermValueType(TermValueType.TEXT));
        vocHorseName.setTerm(new TermComplexGroup("rides"));
        em.setPropertyVocabulary("HorseName", vocHorseName, Collections.enumeration(Collections.singletonList("rides")));        
        Vocabulary vocHorseSize = new Vocabulary();
        vocHorseSize.setTerm(new TermValueType(TermValueType.TEXT));
        vocHorseSize.setTerm(new TermComplexGroup("rides"));
        em.setPropertyVocabulary("HorseSize", vocHorseSize, Collections.enumeration(Collections.singletonList("rides")));
        
        Map<String, Object> getRides = generator.complexTypeHandler("rides", em);
	    
	    assertNotNull(getRides);
	    assertEquals(2,getRides.size());
	    assertTrue(getRides.containsKey("HorseName"));
	    assertTrue(getRides.containsKey("HorseSize"));
	    assertEquals("string",getRides.get("HorseName"));
	    assertEquals("string",getRides.get("HorseSize"));
	    
	    Map<String, Object> getAge = generator.complexTypeHandler("age", em);
	    assertNotNull(getAge);
	    assertEquals(0,getAge.size());
	    
	    Map<String, Object> getName = generator.complexTypeHandler("name", em);
        assertNotNull(getName);
        assertEquals(0,getName.size());
	}
}
