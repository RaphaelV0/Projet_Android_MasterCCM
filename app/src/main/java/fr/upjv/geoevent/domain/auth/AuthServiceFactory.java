package fr.upjv.geoevent.domain.auth;

import fr.upjv.geoevent.domain.data.firebase.FirebaseAuthServiceImpl;
import fr.upjv.geoevent.services.IAuthService;

public class AuthServiceFactory {
    public static IAuthService create() {
        return new FirebaseAuthServiceImpl();
    }
}
