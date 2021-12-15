package dk.kvalitetsit.hjemmebehandling.context;

import com.auth0.jwt.interfaces.DecodedJWT;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;

public class DIASUserContextHandler implements IUserContextHandler {

	private static String FULL_NAME = "FullName";
	private static String FIRST_NAME = "FirstName";
	private static String SUR_NAME = "SurName";
	private static String BSK_DIAS_ENTITLEMENTS = "bSKDIASEntitlements";
	private static String SOR_ID = "SORID";
	private static String AUTORISATIONS_IDS = "autorisationsids";
	private static String REGIONS_ID = "RegionsID";
	private static String EMAIL = "email";	
	private static String BSK_AUTORISATIONS_INFORMATION = "bSKAutorisationsInformation";

	
	public UserContext mapTokenToUser(FhirClient client, DecodedJWT jwt) {
        throw new UnsupportedOperationException();

//		var context = new UserContext();
//		if(jwt==null) {
//			return context;
//		}
//
//        context.setCpr(jwt.getClaim(DIASUserContextHandler.SOR_ID) !=null ? jwt.getClaim(DIASUserContextHandler.SOR_ID).asString() : null );
//        context.setFullName(jwt.getClaim(DIASUserContextHandler.FULL_NAME) !=null ? jwt.getClaim(DIASUserContextHandler.FULL_NAME).asString() : null );
//        context.setFirstName(jwt.getClaim(DIASUserContextHandler.FIRST_NAME) !=null ? jwt.getClaim(DIASUserContextHandler.FIRST_NAME).asString() : null );
//        context.setLastName(jwt.getClaim(DIASUserContextHandler.SUR_NAME) !=null ? jwt.getClaim(DIASUserContextHandler.SUR_NAME).asString() : null );
//        context.setUserId(jwt.getClaim(DIASUserContextHandler.REGIONS_ID) !=null ? jwt.getClaim(DIASUserContextHandler.REGIONS_ID).asString() : null );
//        context.setEntitlements(jwt.getClaim(DIASUserContextHandler.BSK_DIAS_ENTITLEMENTS) !=null ? jwt.getClaim(DIASUserContextHandler.BSK_DIAS_ENTITLEMENTS).asArray(String.class) : null );
//
//        return context;
	}
}
