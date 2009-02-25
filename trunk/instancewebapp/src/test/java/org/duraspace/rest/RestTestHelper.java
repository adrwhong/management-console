package org.duraspace.rest;

import org.duraspace.common.web.RestHttpHelper;
import org.duraspace.common.web.RestHttpHelper.HttpResponse;


/**
 *
 * @author Bill Branan
 */
public class RestTestHelper {

    private static RestHttpHelper restHelper = new RestHttpHelper();

    public static HttpResponse addSpace(String baseUrl, String customerID, String spaceID)
    throws Exception {
        String url = baseUrl + "/space/"+customerID+"/"+spaceID;
        String formParams = "spaceName=Testing+Space&spaceAccess=OPEN";
        return restHelper.put(url, formParams, true);
    }

    public static HttpResponse deleteSpace(String baseUrl, String customerID, String spaceID)
    throws Exception {
        String url = baseUrl + "/space/"+customerID+"/"+spaceID;
        return restHelper.delete(url);
    }

}
