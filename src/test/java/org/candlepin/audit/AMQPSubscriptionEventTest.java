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

import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.candlepin.auth.Principal;
import org.candlepin.model.Content;
import org.candlepin.model.Product;
import org.candlepin.model.Subscription;
import org.candlepin.pki.PKIReader;
import org.candlepin.pki.PKIUtility;

import com.google.common.collect.Sets;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AMQPSubscriptionEventTest {

    @Mock
    private ObjectMapper mapper;
    @Mock
    private Principal principal;
    @Mock
    private PKIReader reader;
    @Mock
    private PKIUtility pkiutil;

    @Before
    public void init() {
    }

    @Test
    public void subscriptionCreated() throws Exception {
        verifySubscriptionEvent(Event.Type.CREATED);
    }

    @Test
    public void subscriptionModified() throws Exception {
        verifySubscriptionEvent(Event.Type.MODIFIED);
    }

    // This is pretty crazy - should these be broken up into smaller tests
    // for each entry?
    private void verifySubscriptionEvent(Event.Type type) throws Exception {
        // given
        Event event = new Event(type, Event.Target.SUBSCRIPTION, "name",
            principal, "1", "1", "33", "Old Subscription", "New Subscription",
            null, null);

        Subscription sub = mock(Subscription.class, Mockito.RETURNS_DEEP_STUBS);

        when(mapper.readValue("New Subscription", Subscription.class))
            .thenReturn(sub);
        when(sub.getId()).thenReturn("8a8b64a32c568ec4012c568ef30a001c");
        when(sub.getOwner().getKey()).thenReturn("test-owner");
        when(sub.getProduct().getId()).thenReturn("test-product-id");
        when(sub.getCertificate().getCert()).thenReturn("test-cert");
        when(sub.getCertificate().getKey()).thenReturn("test-key");
        when(pkiutil.getPemEncoded((X509Certificate) null)).thenReturn(
            "ca-cert".getBytes());

        when(sub.getProvidedProducts()).thenReturn(
            Sets.newHashSet(createProductWithContent("content1",
                "http://dummy.com/content", "/path/to/RPM-GPG-KEY")));

        // when
        this.mapper.writeValueAsString(event);

        // then
        Map<String, Object> expectedMap = new HashMap<String, Object>();
        expectedMap.put("id", "33");
        expectedMap.put("owner", "test-owner");
        expectedMap.put("name", "test-product-id");
        expectedMap.put("entitlement_cert", "test-cert");
        expectedMap.put("cert_public_key", "test-key");
        expectedMap.put("ca_cert", "ca-cert");

        Map<String, String> content = new HashMap<String, String>();
        content.put("content_set_label", "content1");
        content.put("content_rel_url", "http://dummy.com/content");
        content.put("gpg_key_url", "/path/to/RPM-GPG-KEY");

        expectedMap.put("content_sets", Arrays.asList(new Map[]{ content }));

        verify(mapper).writeValueAsString(
            argThat(hasEntry("event", expectedMap)));
    }

    private Product createProductWithContent(String label, String url,
        String gpgurl) {
        Product product = new Product(label, label);
        Content content = new Content();
        content.setLabel(label);
        content.setContentUrl(url);
        content.setGpgUrl(gpgurl);

        product.setContent(Sets.newHashSet(content));

        return product;
    }

    @Test
    public void subscriptionDeleted() throws IOException {
        // given
        Event event = new Event(Event.Type.DELETED, Event.Target.SUBSCRIPTION,
            "name", principal, "1", "1", "33", "Old Subscription",
            "New Subscription", null, null);

        Subscription sub = mock(Subscription.class, Mockito.RETURNS_DEEP_STUBS);

        when(mapper.readValue("New Subscription", Subscription.class))
            .thenReturn(sub);
        when(sub.getOwner().getKey()).thenReturn("test-owner");

        // when
        this.mapper.writeValueAsString(event);

        // then
        Map<String, Object> expectedMap = new HashMap<String, Object>();
        expectedMap.put("id", "33");
        expectedMap.put("owner", "test-owner");

        verify(mapper).writeValueAsString(
            argThat(hasEntry("event", expectedMap)));
    }
}
