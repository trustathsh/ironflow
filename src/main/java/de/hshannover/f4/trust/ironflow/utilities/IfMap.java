/*
 * #%L
 * =====================================================
 *   _____                _     ____  _   _       _   _
 *  |_   _|_ __ _   _ ___| |_  / __ \| | | | ___ | | | |
 *    | | | '__| | | / __| __|/ / _` | |_| |/ __|| |_| |
 *    | | | |  | |_| \__ \ |_| | (_| |  _  |\__ \|  _  |
 *    |_| |_|   \__,_|___/\__|\ \__,_|_| |_||___/|_| |_|
 *                             \____/
 * 
 * =====================================================
 * 
 * Hochschule Hannover
 * (University of Applied Sciences and Arts, Hannover)
 * Faculty IV, Dept. of Computer Science
 * Ricklinger Stadtweg 118, 30459 Hannover, Germany
 * 
 * Email: trust@f4-i.fh-hannover.de
 * Website: http://trust.f4.hs-hannover.de
 * 
 * This file is part of ironflow, version 0.0.1, implemented by the Trust@HsH
 * research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2013 - 2014 Trust@HsH
 * %%
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
 * #L%
 */
package de.hshannover.f4.trust.ironflow.utilities;

import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import de.fhhannover.inform.trust.ifmapj.IfmapJHelper;
import de.fhhannover.inform.trust.ifmapj.channel.ARC;
import de.fhhannover.inform.trust.ifmapj.channel.SSRC;
import de.fhhannover.inform.trust.ifmapj.exception.InitializationException;
import de.hshannover.f4.trust.ironflow.Ironflow;

/**
 * A ifmap class to initiate the ifmap server connection and to get the ssrc and arc channel
 */
public final class IfMap {

	/**
	 * A ifmap SSRC instance.
	 */
	private static SSRC ifmapSsrc = null;

	private static final Logger LOGGER = Logger.getLogger(IfMap.class.getName());

	/**
	 * Death constructor for code convention -> final class because utility
	 * class
	 */
	private IfMap() {
	}

	/**
	 * Creates a {@link SSRC} instance with the given configuration parameters.
	 * 
	 * @param authMethod
	 * @param basicUrl
	 * @param certUrl
	 * @param user
	 * @param pass
	 * @param keypath
	 * @param keypass
	 * @return SSRC
	 */
	public static SSRC initSsrc(String authMethod, String basicUrl, String certUrl, String user, String pass,
			String keypath, String keypass) {

		TrustManager[] tm = null;
		KeyManager[] km = null;

		try {
			tm = IfmapJHelper.getTrustManagers(Ironflow.class.getResourceAsStream(keypath), keypass);
			km = IfmapJHelper.getKeyManagers(Ironflow.class.getResourceAsStream(keypath), keypass);
		} catch (InitializationException e) {

			LOGGER.severe("could not read the security informations for the trust- and key- managers: " + e);
			System.exit(1);
		}

		try {
			if (authMethod.equals("basic")) {
				ifmapSsrc = new ThreadSafeSsrc(basicUrl, user, pass, tm);
			} else if (authMethod.equals("cert")) {
				ifmapSsrc = new ThreadSafeSsrc(certUrl, km, tm);
			} else {
				throw new IllegalArgumentException("unknown authentication method '" + authMethod + "'");
			}
		} catch (InitializationException e) {
			LOGGER.severe("could not read the security informations for basic or cert authentication: " + e);
			System.exit(1);
		}

		return ifmapSsrc;
	}

	public static SSRC getSsrc() {
		return ifmapSsrc;
	}

	/**
	 * get A ARC
	 * 
	 * @return a ARC or System exit
	 */
	public static ARC getArc() {
		try {
			return ifmapSsrc.getArc();
		} catch (InitializationException e) {
			LOGGER.severe("could not establish the arc channel: " + e);
			System.exit(1);
		}
		return null;
	}

}
