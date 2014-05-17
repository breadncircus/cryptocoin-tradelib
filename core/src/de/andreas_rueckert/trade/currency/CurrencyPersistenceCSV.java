/**
 * Java implementation for cryptocoin trading.
 *
 * Copyright (c) 2014 the authors:
 * 
 * @author Andreas Rueckert <mail@andreas-rueckert.de>
 *
 * Permission is hereby granted, free of charge, to any person obtaining 
 * a copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all 
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION 
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.andreas_rueckert.trade.currency;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.util.LogUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;


/**
 * This class uses a CSV file to store the known currencies.
 */
public class CurrencyPersistenceCSV implements CurrencyPersistence {

    // Inner classes


    // Static variables


    // Instance variables

    /**
     * The filename of the file with the currencies.
     */
    private String _filename = null;

    /**
     * The data directory of the users app.
     */
    private File _datadir = null;


    // Contructors

    /**
     * Create a new persistence for currencies.
     *
     * @param datadir The data directory for the users app.
     */
    public CurrencyPersistenceCSV( File datadir) {

	_datadir = datadir;
    }


    // Methods

    /**
     * Get the name for the file holding the currency information.
     *
     * @return The name of the file holding the currency information.
     */
    private String getFilename() {

	if( _filename == null) {

	    // Just use a default file name for now.
	    _filename = "currency.lst";
	}

	return _filename;  // Return the name of the file.
    }

    /**
     * Load the known currencies.
     */
    public void load() {

	throw new NotYetImplementedException( "Loading currencies is not yet implemented");
    }

    /**
     * Save the known currencies.
     *
     * @return true, if the currencies were successfully saved. False otherwise.
     */
    public boolean save() {

	// Create the file for the currency info.
	File persistenceFile = new File( _datadir, getFilename());

	try {

	    // Create a writer for the persistence file.
	    BufferedWriter writer = new BufferedWriter( new FileWriter( persistenceFile));

	    for( Currency currentCurrency : CurrencyProvider.getInstance().getRegisteredCurrencies()) {

		writer.write( currentCurrency.getCode() == null ? "" : URLEncoder.encode( currentCurrency.getCode()));
		writer.write( "|");
		writer.write( currentCurrency.getName() == null ? "" : URLEncoder.encode( currentCurrency.getName()));
		writer.write( "|");
		writer.write( currentCurrency.getDescription() == null ? "" : URLEncoder.encode( currentCurrency.getDescription()));
		writer.write( "|");
		writer.write( URLEncoder.encode( currentCurrency.getCurrencyType().name()));
		writer.newLine();
            }

            writer.close();

	    return true;  // Saving worked.

        } catch( IOException ioe) {  // If the saving did not work.

	    // Log the exception.
	    LogUtils.getInstance().getLogger().error( "Cannot open file to store currencies: " + ioe);

	    return false;  // Saving failed.
	}
    }
}