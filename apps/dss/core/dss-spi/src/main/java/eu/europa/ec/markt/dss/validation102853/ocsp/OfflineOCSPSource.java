/*
 * DSS - Digital Signature Services
 *
 * Copyright (C) 2013 European Commission, Directorate-General Internal Market and Services (DG MARKT), B-1049 Bruxelles/Brussel
 *
 * Developed by: 2013 ARHS Developments S.A. (rue Nicolas Bové 2B, L-1253 Luxembourg) http://www.arhs-developments.com
 *
 * This file is part of the "DSS - Digital Signature Services" project.
 *
 * "DSS - Digital Signature Services" is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * DSS is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * "DSS - Digital Signature Services".  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.markt.dss.validation102853.ocsp;

import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.markt.dss.DSSRevocationUtils;
import eu.europa.ec.markt.dss.DSSUtils;

/**
 * Abstract class that helps to implement an OCSPSource with an already loaded list of BasicOCSPResp
 *
 * @version $Revision: 3573 $ - $Date: 2014-03-07 06:56:00 +0100 (Fri, 07 Mar 2014) $
 */

public abstract class OfflineOCSPSource implements OCSPSource {

    private static final Logger LOG = LoggerFactory.getLogger(OfflineOCSPSource.class);

    @Override
    final public BasicOCSPResp getOCSPResponse(final X509Certificate cert, final X509Certificate issuerCert) {

        /**
         * TODO: (Bob 2013.05.08) Does the OCSP responses always use SHA1?<br>
         * RFC 2560:<br>
         * CertID ::= SEQUENCE {<br>
         * hashAlgorithm AlgorithmIdentifier,<br>
         * issuerNameHash OCTET STRING, -- Hash of Issuer’s DN<br>
         * issuerKeyHash OCTET STRING, -- Hash of Issuers public key<br>
         * serialNumber CertificateSerialNumber }<br>
         *
         * ... The hash algorithm used for both these hashes, is identified in hashAlgorithm. serialNumber is the
         * serial number of the cert for which status is being requested.
         */
        Date bestUpdate = null;
        BasicOCSPResp bestBasicOCSPResp = null;
        final CertificateID certId = DSSUtils.getOCSPCertificateID(cert, issuerCert);
        for (final BasicOCSPResp basicOCSPResp : getContainedOCSPResponses()) {

            for (final SingleResp singleResp : basicOCSPResp.getResponses()) {

                if (DSSRevocationUtils.matches(certId, singleResp)) {

                    final Date thisUpdate = singleResp.getThisUpdate();
                    if (bestUpdate == null || thisUpdate.after(bestUpdate)) {

                        bestBasicOCSPResp = basicOCSPResp;
                        bestUpdate = thisUpdate;
                    }
                }
            }
        }
        return bestBasicOCSPResp;
    }

    /**
     * Retrieves the list of BasicOCSPResp contained in the source.
     *
     * @return
     */
    public abstract List<BasicOCSPResp> getContainedOCSPResponses();
}
