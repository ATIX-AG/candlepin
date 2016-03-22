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
package org.candlepin.service.impl;

import org.candlepin.model.ContentCurator;
import org.candlepin.model.Owner;
import org.candlepin.model.Product;
import org.candlepin.model.ProductCertificate;
import org.candlepin.model.ProductCertificateCurator;
import org.candlepin.model.ProductCurator;
import org.candlepin.service.ProductServiceAdapter;
import org.candlepin.service.UniqueIdGenerator;

import com.google.inject.Inject;

import java.util.Collection;
import java.util.List;

/**
 * Default implementation of the ProductserviceAdapter.
 */
public class DefaultProductServiceAdapter implements ProductServiceAdapter {

    private ProductCurator prodCurator;

    // for product cert storage/generation - not sure if this should go in
    // a separate service?
    private ProductCertificateCurator prodCertCurator;

    @Inject
    public DefaultProductServiceAdapter(ProductCurator prodCurator,
        ProductCertificateCurator prodCertCurator, ContentCurator contentCurator,
        UniqueIdGenerator idGenerator) {

        this.prodCurator = prodCurator;
        this.prodCertCurator = prodCertCurator;
    }

    @Override
    public ProductCertificate getProductCertificate(Product product) {
        return this.prodCertCurator.getCertForProduct(product);
    }

    public boolean productHasSubscriptions(Product prod) {
        return prodCurator.productHasSubscriptions(prod);
    }

    @Override
    public List<Product> getProductsByIds(Owner owner, Collection<String> ids) {
        return prodCurator.listAllByIds(owner, ids);
    }

}
