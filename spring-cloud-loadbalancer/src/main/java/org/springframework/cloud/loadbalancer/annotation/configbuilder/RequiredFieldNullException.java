package org.springframework.cloud.loadbalancer.annotation.configbuilder;

import org.springframework.cloud.loadbalancer.core.CachingServiceInstanceListSupplier;

/**
 * @author Olga Maciaszek-Sharma
 */
public class RequiredFieldNullException extends IllegalArgumentException {

	public RequiredFieldNullException(String fieldName) {
		super(buildNullCheckMessage(fieldName));
	}

	private static String buildNullCheckMessage(String fieldName) {
		return "The " + fieldName + " field/s of " + CachingServiceInstanceListSupplier.class
				.getSimpleName()
				+ " cannot be null";
	}

}
