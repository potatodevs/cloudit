/**
 * EmailValidator
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package com.tomasvitek.android.cloudapp.tools;

public class EmailValidator {

    /**
     * Validate hex with regular expression
     * 
     * @param hex
     *            hex for validation
     * @return true valid hex, false invalid hex
     */
    public final static boolean isValidEmail(final String hex) {
	if (hex == null) {
	    return false;
	} else {
	    return android.util.Patterns.EMAIL_ADDRESS.matcher(hex).matches();
	}
    }
}