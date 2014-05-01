/**
 * Copyright (c) 2009 - 2012 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package org.candlepin.audit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.candlepin.guice.PrincipalProvider;
import org.candlepin.model.Consumer;
import org.candlepin.model.Entitlement;
import org.candlepin.model.IdentityCertificate;
import org.candlepin.pki.PKIReader;
import org.candlepin.pki.PKIUtility;
import org.candlepin.test.TestUtil;
import org.candlepin.util.Util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import org.candlepin.auth.UserPrincipal;
import org.candlepin.model.Owner;

@RunWith(MockitoJUnitRunner.class)
public class AMQPConsumerEventsAdapterTest {
    @Mock private PrincipalProvider mockPrincipalProvider;
    @Spy private ObjectMapper spiedMapper = new ObjectMapper();
    private ObjectMapper mapper = new ObjectMapper();
    private UserPrincipal principal;
    private Owner owner;
    private EventFactory factory;
    @Mock private PKIReader reader;
    @Mock private PKIUtility pkiutil;

    @Before
    public void init() {
        this.factory = new EventFactory(mockPrincipalProvider);
        this.principal = TestUtil.createOwnerPrincipal();
        this.owner = this.principal.getOwners().get(0);
        this.owner.setId(String.valueOf(new Random().nextLong()));
        when(mockPrincipalProvider.get()).thenReturn(this.principal);
    }

    @Test
    @Ignore("need to fix json mapping to ignore entitlemetnCount")
    public void consumerCreatedEventShouldSerializeSuccessfully() throws Exception {
        Consumer consumer = TestUtil.createConsumer(owner);
        storeFacts(consumer);
        IdentityCertificate idCert = TestUtil.createIdCert();
        consumer.setIdCert(idCert);

        Event event = factory.consumerCreated(consumer);
        verifyMap(consumer, idCert, event);
    }

    /**
     * @param consumer
     * @param idCert
     * @param event
     * @throws IOException
     * @throws JsonParseException
     * @throws JsonMappingException
     */
    private void verifyMap(Consumer consumer, IdentityCertificate idCert,
        Event event) throws IOException, JsonParseException,
        JsonMappingException {
        Map<String, Object> map = unmarshallEvent(event);

        verify(spiedMapper, times(1)).readValue(eq(event.getNewEntity()),
            eq(Consumer.class));

        assertThat(map, containsEntry("identity_cert", idCert.getCert()));
        assertThat(map, containsEntry("identity_cert_key", idCert.getKey()));
        assertThat(map,
            hasEntry("hardware_facts", (Object) consumer.getFacts()));
        assertThat(map, containsEntry("id", consumer.getUuid()));
        assertThat(map, containsEntry("owner", consumer.getOwner().getId()));
    }

    /**
     * @param result
     * @return
     * @throws IOException
     * @throws JsonParseException
     * @throws JsonMappingException
     */
    private Map<String, Object> unmarshall(String result) throws IOException,
        JsonParseException, JsonMappingException {
        return mapper.readValue(result,
            new TypeReference<Map<String, Object>>() {});
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> unmarshallEvent(Event event) throws IOException {
        Map<String, Object> map = unmarshall(this.mapper.writeValueAsString(event));
        return (Map<String, Object>) map.get("event");
    }

    @Test
    @Ignore("need to fix json mapping to ignore entitlemetnCount")
    public void consumerModifiedEventShouldSerializeSuccessfully() throws Exception {
        Consumer consumer = TestUtil.createConsumer(owner);
        storeFacts(consumer);
        IdentityCertificate idCert = TestUtil.createIdCert();
        consumer.setIdCert(idCert);

        Event event = factory.consumerModified(new Consumer(), consumer);
        verifyMap(consumer, idCert, event);
    }

    @Test
    @Ignore("need to fix json mapping to ignore entitlemetnCount")
    public void consumerDeletedEventShouldSerializeSuccessfully() throws Exception {
        Consumer consumer = TestUtil.createConsumer(owner);
        Event event = factory.consumerDeleted(consumer);
        Map<String, Object> map = unmarshallEvent(event);

        assertThat(map, containsEntry("id", consumer.getUuid()));
        assertThat(map, containsEntry("owner", consumer.getOwner().getId()));
        assertThat(map.size(), is(equalTo(2)));
    }


    @Test
    public void entitlementCreatedEventShouldSerializeSuccessfuly() throws Exception {
        Entitlement ent = TestUtil.createEntitlement();
        storeFacts(ent.getConsumer());
        //this.principal = new ConsumerPrincipal(ent.getConsumer());
        Event event = factory.entitlementCreated(ent);
        verifyMap(ent, event);
    }

    /**
     * @param ent
     * @param event
     * @throws IOException
     * @throws JsonParseException
     * @throws JsonMappingException
     */
    private void verifyMap(Entitlement ent, Event event) throws IOException,
        JsonParseException, JsonMappingException {
        Map<String, Object> map = unmarshallEvent(event);

        assertThat(map, containsEntry("id", ent.getConsumer().getUuid()));
        assertThat(map, containsEntry("owner", ent.getOwner().getId()));
        assertThat(map, containsEntry("product_id", ent.getProductId()));
        assertThat(map, containsEntry("consumer_os_arch", OS_ARCH));
        assertThat(map, containsEntry("consumer_os_version", OS_VERSION));
    }

    @Test
    public void entitlementDeletedEventShouldSerializeSuccessfully() throws Exception {
        Entitlement ent = TestUtil.createEntitlement();
        storeFacts(ent.getConsumer());
        //this.principal = new ConsumerPrincipal(ent.getConsumer());
        Event event = factory.entitlementDeleted(ent);
        verifyMap(ent, event);
    }

    private Matcher<Map<String, Object>> containsEntry(String key, final String value) {
        return hasEntry(equalTo(key), new BaseMatcher<Object>() {
            @Override
            public boolean matches(Object arg0) {
                if (arg0 == null) {
                    return arg0 == value;
                }
                return Util.equals(arg0.toString(), value);
            }

            @Override
            public void describeTo(Description arg0) {
                arg0.appendText("does not match: " + value);
            }
        });
    }

    private static final String OS_ARCH = "i686";
    private static final String OS_VERSION = "1234";

    private void storeFacts(Consumer consumer) {
        consumer.getFacts().put("uname.machine", OS_ARCH);
        consumer.getFacts().put("distribution.version", OS_VERSION);
    }
}
