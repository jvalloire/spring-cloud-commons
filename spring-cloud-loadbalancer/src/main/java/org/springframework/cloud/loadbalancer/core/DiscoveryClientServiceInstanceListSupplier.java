/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.loadbalancer.core;

import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.loadbalancer.annotation.configbuilder.AbstractServiceInstanceListSupplierBuilder;
import org.springframework.cloud.loadbalancer.annotation.configbuilder.RequiredFieldNullException;
import org.springframework.core.env.Environment;

import static org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory.PROPERTY_NAME;

/**
 * A discovery-client-based {@link ServiceInstanceListSupplier} implementation.
 *
 * @author Spencer Gibb
 * @author Olga Maciaszek-Sharma
 * @author Tim Ysewyn
 * @since 2.2.0
 */
public class DiscoveryClientServiceInstanceListSupplier
		implements ServiceInstanceListSupplier {

	private final String serviceId;

	private final Flux<ServiceInstance> serviceInstances;

	public DiscoveryClientServiceInstanceListSupplier(DiscoveryClient delegate,
			Environment environment) {
		this.serviceId = environment.getProperty(PROPERTY_NAME);
		this.serviceInstances = Flux
				.defer(() -> Flux.fromIterable(delegate.getInstances(serviceId)))
				.subscribeOn(Schedulers.boundedElastic());
	}

	public DiscoveryClientServiceInstanceListSupplier(ReactiveDiscoveryClient delegate,
			Environment environment) {
		this.serviceId = environment.getProperty(PROPERTY_NAME);
		this.serviceInstances = delegate.getInstances(serviceId);
	}

	@Override
	public String getServiceId() {
		return serviceId;
	}

	@Override
	public Flux<List<ServiceInstance>> get() {
		return serviceInstances.collectList().flux();
	}

	public static Builder discoveryClientServiceInstanceListSupplierBuilder(
			DiscoveryClient discoveryClient,
			Environment environment) {
		return new Builder(discoveryClient, environment);
	}

	public static Builder discoveryClientServiceInstanceListSupplierBuilder(
			ReactiveDiscoveryClient reactiveDiscoveryClient,
			Environment environment) {
		return new Builder(reactiveDiscoveryClient, environment);
	}

	public static Builder discoveryClientServiceInstanceListSupplierBuilder() {
		return new Builder();
	}

	public static final class Builder extends AbstractServiceInstanceListSupplierBuilder {

		private Object discoveryClient;
		private Environment environment;

		private Builder(Object discoveryClient, Environment environment) {
			this.discoveryClient = discoveryClient;
			this.environment = environment;
		}

		private Builder() {

		}

		public Builder withDiscoveryClient(Object discoveryClient) {
			if (!(discoveryClient instanceof DiscoveryClient) && !(discoveryClient instanceof ReactiveDiscoveryClient)) {
				throw new IllegalArgumentException("Field discoveryClient must by of type " + DiscoveryClient.class
						.getSimpleName()
						+ " or " + ReactiveDiscoveryClient.class.getSimpleName());
			}
			this.discoveryClient = discoveryClient;
			return this;
		}

		public Builder withEnvironment(Environment environment) {
			this.environment = environment;
			return this;
		}

		// TODO: less conditions?
		public DiscoveryClientServiceInstanceListSupplier build() {
			if (discoveryClient == null) {
				throw new RequiredFieldNullException("discoveryClient");
			}
			if (environment == null) {
				throw new RequiredFieldNullException("environment");
			}
			if (discoveryClient instanceof DiscoveryClient) {
				return new DiscoveryClientServiceInstanceListSupplier((DiscoveryClient) discoveryClient,
						environment);
			}
			return new DiscoveryClientServiceInstanceListSupplier((ReactiveDiscoveryClient) discoveryClient,
					environment);
		}
	}

}
