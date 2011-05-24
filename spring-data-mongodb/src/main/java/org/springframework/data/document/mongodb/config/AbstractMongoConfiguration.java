/*
 * Copyright 2010-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.document.mongodb.config;

import java.util.HashSet;
import java.util.Set;

import com.mongodb.Mongo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.document.mongodb.MongoDbFactory;
import org.springframework.data.document.mongodb.MongoTemplate;
import org.springframework.data.document.mongodb.SimpleMongoDbFactory;
import org.springframework.data.document.mongodb.convert.MappingMongoConverter;
import org.springframework.data.document.mongodb.mapping.Document;
import org.springframework.data.document.mongodb.mapping.MongoMappingContext;
import org.springframework.data.mapping.context.MappingContextAwareBeanPostProcessor;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

@Configuration
public abstract class AbstractMongoConfiguration {

	public abstract String defaultDatabaseName();

	@Bean
	public abstract Mongo mongo() throws Exception;

	@Bean
	public abstract MongoTemplate mongoTemplate() throws Exception;

	@Bean
	public MongoDbFactory mongoDbFactory() throws Exception {
		return new SimpleMongoDbFactory(mongo(), defaultDatabaseName());
	}

	public String mappingBasePackage() {
		return "";
	}

	@Bean
	public MongoMappingContext mongoMappingContext() throws ClassNotFoundException, LinkageError {
		MongoMappingContext mappingContext = new MongoMappingContext();
		String basePackage = mappingBasePackage();
		if (StringUtils.hasText(basePackage)) {
			ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false);
			componentProvider.addIncludeFilter(new AnnotationTypeFilter(Document.class));
			componentProvider.addIncludeFilter(new AnnotationTypeFilter(Persistent.class));

			Set<Class<?>> initialEntitySet = new HashSet<Class<?>>();
			for (BeanDefinition candidate : componentProvider.findCandidateComponents(basePackage)) {
				initialEntitySet.add(ClassUtils.forName(candidate.getBeanClassName(), mappingContext.getClass().getClassLoader()));
			}
			mappingContext.setInitialEntitySet(initialEntitySet);
		}
		return mappingContext;
	}

	@Bean
	public MappingMongoConverter mappingMongoConverter() throws Exception {
		MappingMongoConverter converter = new MappingMongoConverter(mongoDbFactory(), mongoMappingContext());
		afterMappingMongoConverterCreation(converter);
		return converter;
	}

	/**
	 * Hook that allows post-processing after the MappingMongoConverter has been successfully created.
	 *
	 * @param converter
	 */
	protected void afterMappingMongoConverterCreation(MappingMongoConverter converter) {
	}

	@Bean
	public MappingContextAwareBeanPostProcessor mappingContextAwareBeanPostProcessor() {
		MappingContextAwareBeanPostProcessor bpp = new MappingContextAwareBeanPostProcessor();
		bpp.setMappingContextBeanName("mongoMappingContext");
		return bpp;
	}

}
