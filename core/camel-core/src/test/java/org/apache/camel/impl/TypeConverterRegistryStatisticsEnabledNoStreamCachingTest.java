/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.TypeConverterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeConverterRegistryStatisticsEnabledNoStreamCachingTest extends ContextTestSupport {

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = new DefaultCamelContext(false);
        context.setLoadTypeConverters(isLoadTypeConverters());
        context.setStreamCaching(false);
        context.setTypeConverterStatisticsEnabled(true);
        context.build();

        return context;
    }

    @Test
    public void testTypeConverterRegistry() throws Exception {
        getMockEndpoint("mock:a").expectedMessageCount(2);

        template.sendBody("direct:start", "3");
        template.sendBody("direct:start", "7");

        assertMockEndpointsSatisfied();

        TypeConverterRegistry reg = context.getTypeConverterRegistry();
        assertTrue(reg.getStatistics().isStatisticsEnabled(), "Should be enabled");

        Long failed = reg.getStatistics().getFailedCounter();
        assertEquals(0, failed.intValue());
        Long miss = reg.getStatistics().getMissCounter();
        assertEquals(0, miss.intValue());

        assertThrows(Exception.class, () -> template.sendBody("direct:start", "foo"),
                "Should have thrown exception");

        // should now have a failed
        failed = reg.getStatistics().getFailedCounter();
        assertEquals(1, failed.intValue());
        miss = reg.getStatistics().getMissCounter();
        assertEquals(0, miss.intValue());

        // reset
        reg.getStatistics().reset();

        failed = reg.getStatistics().getFailedCounter();
        assertEquals(0, failed.intValue());
        miss = reg.getStatistics().getMissCounter();
        assertEquals(0, miss.intValue());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").routeId("foo").convertBodyTo(int.class).to("mock:a");
            }
        };
    }

}
