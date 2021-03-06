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

package de.andreas_rueckert.trade.site.poloniex.client;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.currency.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.currency.CurrencyPairImpl;
import de.andreas_rueckert.trade.currency.CurrencyProvider;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.DepositOrder;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.order.WithdrawOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteImpl;
import de.andreas_rueckert.trade.site.TradeSiteRequestType;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.trade.Ticker;
import de.andreas_rueckert.trade.Trade;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import de.andreas_rueckert.util.HttpUtils;
import de.andreas_rueckert.util.LogUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * Main class for the Poloniex API.
 *
 * @see <a href="https://poloniex.com/api">Poloniex API</a>
 */
public class PoloniexClient extends TradeSiteImpl implements TradeSite {

    // Inner classes


    // Static variables


    // Instance variables


    // Constructors

    /**
     * Create a new Poloniex client instance.
     */
    public PoloniexClient() {

	_name = "Poloniex";  // Set the name of this exchange.

	_url = "https://poloniex.com/public";  // Base URL for API calls.

	// Try to request the supported currency pairs.
	if( ! requestSupportedCurrencyPairs()) {

	    LogUtils.getInstance().getLogger().error( "Cannot fetch the supported currency pairs for " + _name);
	}
    }


    // Methods


    /**
     * Cancel an order on the trade site.
     *
     * @param order The order to cancel.
     *
     * @return true, if the order was canceled. False otherwise.
     */
    public boolean cancelOrder( SiteOrder order) {

	throw new NotYetImplementedException( "Cancelling an order is not yet implemented for " + _name);
    }

    /**
     * Execute an order on the trade site.
     * Synchronize this method, since several users might execute orders in parallel via an API implementation instance.
     *
     * @param order The order to execute.
     *
     * @return The new status of the order.
     */
    public synchronized OrderStatus executeOrder( SiteOrder order) {

	throw new NotYetImplementedException( "Executing an order is not yet implemented for " + _name);
    }

    /**
     * Get the current funds of the user via the API.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The accounts with the current balance as a collection of Account objects, or null if the request failed.
     */
    public Collection<TradeSiteAccount> getAccounts( TradeSiteUserAccount userAccount) {

	throw new NotYetImplementedException( "Getting the accounts is not yet implemented for " + _name);
    }

    /**
     * Get the market depth as a Depth object.
     *
     * @param currencyPair The queried currency pair.
     *
     * @throws TradeDataNotAvailableException if the depth is not available.
     */
    public Depth getDepth( CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on " + _name);
	}

	// Create the URL to fetch the depth.
	String url = _url + "?command=returnOrderBook&currencyPair=" + getPoloniexCurrencyPairName( currencyPair);

	// Do the actual request.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?

	    try {
		
		// Convert the result to JSON.
		JSONObject requestResultJSON = (JSONObject)JSONObject.fromObject( requestResult);

		// ToDo: error checking, but Poloniex just returns an empty page in case of an error?
		
		// Create a new depth instance from the data and return it.
		return new PoloniexDepth( requestResultJSON, currencyPair, this);

	    } catch( JSONException je) {

		System.err.println( "Cannot parse " + this._name + " depth return: " + je.toString());

		throw new TradeDataNotAvailableException( "cannot parse depth data from " + this._name);
	    }
	}
    
	throw new TradeDataNotAvailableException( this._name + " server did not respond to depth request");
    }

    /**
     * Get the fee for an order in the resulting currency.
     * Synchronize this method, since several users might use this method with different
     * accounts and therefore different fees via a single API implementation instance.
     *
     * @param order The order to use for the fee computation.
     *
     * @return The fee in the resulting currency (currency value for buy, payment currency value for sell).
     */
    public synchronized Price getFeeForOrder( SiteOrder order) {
	
	if( order instanceof WithdrawOrder) {

	    // Poloniex doesn't charge for withdraw orders.
	    return new Price( "0", order.getCurrencyPair().getCurrency());

	} else if( order instanceof DepositOrder) {

	    // Poloniex doesn't charge for deposit orders.
	    return new Price( "0", order.getCurrencyPair().getCurrency());

	} else if( order.getOrderType() == OrderType.BUY) {

	    // The fees for buys is 0.2% of the target currency.
	    return new Price( order.getAmount().multiply( new BigDecimal( "0.002")), order.getCurrencyPair().getCurrency());

	} else if( order.getOrderType() == OrderType.SELL) {

	    // The fee for sell orders is 0.2% of the target currency (= payment currency).
	    return new Price( order.getAmount().multiply( order.getPrice()).multiply( new BigDecimal( "0.002"))
			      , order.getCurrencyPair().getPaymentCurrency());

	} else {  // This is an unknown order type?

	    return null;  // Should never happen.
	}
    }

    /**
     * Get the open orders on this trade site.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The open orders as a collection, or null if the request failed.
     */
    public Collection<SiteOrder> getOpenOrders( TradeSiteUserAccount userAccount) {

	throw new NotYetImplementedException( "Getting the open orders is not yet implemented for " + _name);
    }

    /**
     * Get the Poloniex name for a currency pair. The ticker class needs access to this method,
     * so it can't be private.
     *
     * @param currencyPair The currency pair.
     *
     * @return The Poloniex name for the currency pair.
     */
    final String getPoloniexCurrencyPairName( CurrencyPair currencyPair) {

	// The Poloniex names look like 'BTC_NXT'
	return currencyPair.getCurrency().getCode().toUpperCase() 
	    + "_" 
	    + currencyPair.getPaymentCurrency().getCode();
    }

    /**
     * Get the section name in the global property file.
     *
     * @return The name of the property section as a String.
     */
    public String getPropertySectionName() {

	return _name;
    }

    /**
     * Get the current ticker from the API.
     *
     * @param currencyPair The currency pair to query.
     *
     * @return The current ticker.
     *
     * @throws TradeDataNotAvailableException if the ticker is not available.
     */
    public Ticker getTicker( CurrencyPair currencyPair) throws TradeDataNotAvailableException {
	
	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on " + _name);
	}

	// Create the URL to fetch the ticker.
	// Poloniex fetches the ticker for all currency pairs at once, so we have to filter the correct pair later.
	// That's why no currency pair is in the URL.
	String url = _url + "?command=returnTicker";

	// Do the actual request.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?

	    try {
		
		// Convert the result to JSON.
		JSONObject requestResultJSON = (JSONObject)JSONObject.fromObject( requestResult);

		// ToDo: error checking, but Poloniex just returns an empty page in case of an error?
		
		// Create a new ticker instance from the data and return it.
		return new PoloniexTicker( requestResultJSON, currencyPair, this);

	    } catch( JSONException je) {

		System.err.println( "Cannot parse " + this._name + " ticker return: " + je.toString());

		throw new TradeDataNotAvailableException( "cannot parse ticker data from " + this._name);
	    }
	}

	throw new TradeDataNotAvailableException( this._name + " server did not respond to depth request");
    }

    /**
     * Get a list of recent trades.
     *
     * @param since_micros The GMT-relative epoch in microseconds.
     * @param currencyPair The currency pair to query.
     *
     * @return The trades as a list of Trade objects.
     *
     * @throws TradeDataNotAvailableException if the ticker is not available.
     */
    public List<Trade> getTrades( long since_micros, CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	throw new NotYetImplementedException( "Getting the trades is not yet implemented for " + _name);
    }

    /**
     * Get the interval, in which the trade site updates it's depth, ticker etc. 
     * in microseconds.
     *
     * @return The update interval in microseconds.
     */
    public final long getUpdateInterval() {
	return 15L * 1000000L;  // 15s should work for most exchanges. Dont't know the actual frequency (a_rueckert).
    }

    /**
     * Check, if some request type is allowed at the moment. Most
     * trade site have limits on the number of request per time interval.
     *
     * @param requestType The type of request (trades, depth, ticker, order etc).
     *
     * @return true, if the given type of request is possible at the moment.
     */
    public boolean isRequestAllowed( TradeSiteRequestType requestType) {

	return true;  // Just a dummy for now.
    }

    /**
     * Request the supported currency pairs from the Bitfinex server.
     *
     * @return true, if the currencies were returned, false in case of an error.
     */
    private final boolean requestSupportedCurrencyPairs() {


	// Since Poloniex has no special method to request the traded markets,
	// I use the return24Volume method to fetch the names of the markets.
	String url = _url + "?command=return24hVolume";

	// Request info on the traded pairs from the server.
	String requestResult = HttpUtils.httpGet( url);
	
	if( requestResult != null) {  // If the server returned a response.

	    try {

		// Try to parse the response.
		JSONObject jsonResult = JSONObject.fromObject( requestResult);

		// Create a buffer for the parsed currency pairs.
		List< CurrencyPair> resultBuffer = new ArrayList< CurrencyPair>();

		// The keys of the result are the currency pairs.
		for( Iterator<?> keys = jsonResult.keys(); keys.hasNext(); ) {

		    // The name of the pair is the next key.
		    String currencyPairName = (String)keys.next();

		    // Ignore the total* entries for now...
		    if( currencyPairName.indexOf( "_") != -1) {

			// The Poloniex currency pair names have the form 'BTC_NXT'.
			String [] currencyNames = currencyPairName.split( "_");

			// Get the traded currency from the JSON object.
			Currency currency = CurrencyProvider.getInstance().getCurrencyForCode( currencyNames[0].toUpperCase());
		    
			// Get the payment currency from the JSON object.
			Currency paymentCurrency = CurrencyProvider.getInstance().getCurrencyForCode( currencyNames[1].toUpperCase());
			
			// Create a pair from the currencies.
			CurrencyPair currentPair = new CurrencyPairImpl( currency, paymentCurrency);
		
			// Add the current pair to the result buffer.
			resultBuffer.add( currentPair);
		    }
		}

		// Convert the buffer to an array and store the currency pairs into the default client array.
		_supportedCurrencyPairs = resultBuffer.toArray( new CurrencyPair[ resultBuffer.size()]);
		
		return true;  // Reading the currency pairs worked ok.

	    } catch( JSONException je) {

		// Write the exception to the log. Should help to identify the problem.
		LogUtils.getInstance().getLogger().error( "Cannot parse " + this._name + " market 24s volumes return to get supported currency pairs: " + je.toString());
		
		return false;  // Reading the currency pairs failed.
	    }

	} else {  // The server did not return any reply.
		    
	    // Write the error message to the log. Should help to identify the problem.
	    LogUtils.getInstance().getLogger().error( "Error while fetching the " 
						      + _name 
						      + " supported currency pairs. Server returned no reply.");
	}

	return false;   // Fetching the traded currency pairs failed.
    }
}

