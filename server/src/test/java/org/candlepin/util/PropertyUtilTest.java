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
package org.candlepin.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * PropertyUtilTest
 */
public class PropertyUtilTest {

    public static final String FOO = "foobar";
    public static final int TRYME = 10;
    public static final String NULL = null;

    @Test
    public void testStaticPropertyClassName() {
        try {
            String v = PropertyUtil.<String>getStaticProperty(
                getClass().getName(), "FOO");
            assertNotNull(v);
            assertEquals("foobar", v);
        }
        catch (NoSuchFieldException e) {
            fail(e.getMessage());
        }
        catch (ClassNotFoundException cnfe) {
            fail(cnfe.getMessage());
        }

    }
    @Test
    public void testStaticProperty() {
        try {
            String v = PropertyUtil.<String>getStaticProperty(getClass(), "FOO");
            assertNotNull(v);
            assertEquals("foobar", v);
        }
        catch (NoSuchFieldException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testStaticPropertyNonExistent() {
        try {
            PropertyUtil.<String>getStaticProperty(getClass(), "BAR");
            fail("BAR should've thrown exception");
        }
        catch (NoSuchFieldException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testStaticPropertyNull() {
        try {
            PropertyUtil.<String>getStaticProperty((Class) null, "FOO");
            fail("Should've thrown NPE");
        }
        catch (NullPointerException npe) {
            assertTrue(true);
        }
        catch (NoSuchFieldException e) {
            fail("should've thrown NPE");
        }

        try {
            PropertyUtil.<String>getStaticProperty(getClass(), null);
            fail("Should've thrown NPE");
        }
        catch (NullPointerException npe) {
            assertTrue(true);
        }
        catch (NoSuchFieldException e) {
            fail("should've thrown NPE");
        }
    }

    @Test
    public void testStaticPropertyInteger() {
        try {
            Integer v = PropertyUtil.<Integer>getStaticProperty(getClass(), "TRYME");
            assertNotNull(v);
            assertEquals(new Integer(10), v);
        }
        catch (NoSuchFieldException e) {
            fail("TRYME should've been found");
        }
    }
}
