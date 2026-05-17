package fr.upjv.geoevent.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.auth.AuthServiceFactory;
import fr.upjv.geoevent.domain.data.DataCallback;
import fr.upjv.geoevent.domain.models.User;
import fr.upjv.geoevent.domain.repository.UserRepository;
import fr.upjv.geoevent.services.IAuthService;

/**
 * Écran unique d'authentification.
 *
 * Après connexion réussie, récupère le profil Firestore de l'utilisateur
 * pour connaître son rôle et le rediriger vers :
 *   - AdminActivity  si role == "admin"
 *   - MainActivity   sinon
 */
public class ConnexionActivity extends AppCompatActivity {

    private enum AuthMode { LOGIN, REGISTER }
    private AuthMode currentMode = AuthMode.LOGIN;
    private IAuthService authService;
    private UserRepository userRepository;

    private TextView tabLogin, tabRegister;
    private LinearLayout formLogin, formRegister;
    private ProgressBar loadingIndicator;

    private TextInputEditText loginEmailInput, loginPasswordInput;
    private TextInputLayout loginEmailLayout, loginPasswordLayout;
    private TextView loginErrorText;
    private MaterialButton loginButton, loginGoogleButton;
    private TextView forgotPasswordText;

    private TextInputEditText registerFirstNameInput, registerLastNameInput;
    private TextInputEditText registerEmailInput, registerPasswordInput, registerConfirmPasswordInput;
    private TextInputLayout registerEmailLayout, registerPasswordLayout, registerConfirmPasswordLayout;
    private TextView registerErrorText;
    private MaterialButton registerButton;

    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);

        authService    = AuthServiceFactory.create();
        userRepository = new UserRepository();

        initViews();

        if (authService.isLoggedIn()) {
            navigateBasedOnRole(authService.getCurrentUser());
            return;
        }

        setupTabSwitcher();
        setupLoginForm();
        setupRegisterForm();
        setupGoogleSignIn();
    }

    private void initViews() {
        tabLogin    = findViewById(R.id.tabLogin);
        tabRegister = findViewById(R.id.tabRegister);
        formLogin   = findViewById(R.id.formLogin);
        formRegister = findViewById(R.id.formRegister);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        loginEmailInput    = findViewById(R.id.loginEmailInput);
        loginPasswordInput = findViewById(R.id.loginPasswordInput);
        loginEmailLayout   = findViewById(R.id.loginEmailLayout);
        loginPasswordLayout = findViewById(R.id.loginPasswordLayout);
        loginErrorText     = findViewById(R.id.loginErrorText);
        loginButton        = findViewById(R.id.loginButton);
        loginGoogleButton  = findViewById(R.id.loginGoogleButton);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        registerFirstNameInput        = findViewById(R.id.registerFirstNameInput);
        registerLastNameInput         = findViewById(R.id.registerLastNameInput);
        registerEmailInput            = findViewById(R.id.registerEmailInput);
        registerPasswordInput         = findViewById(R.id.registerPasswordInput);
        registerConfirmPasswordInput  = findViewById(R.id.registerConfirmPasswordInput);
        registerEmailLayout           = findViewById(R.id.registerEmailLayout);
        registerPasswordLayout        = findViewById(R.id.registerPasswordLayout);
        registerConfirmPasswordLayout = findViewById(R.id.registerConfirmPasswordLayout);
        registerErrorText             = findViewById(R.id.registerErrorText);
        registerButton                = findViewById(R.id.registerButton);
    }

    private void setupTabSwitcher() {
        tabLogin.setOnClickListener(v -> switchToMode(AuthMode.LOGIN));
        tabRegister.setOnClickListener(v -> switchToMode(AuthMode.REGISTER));
    }

    private void switchToMode(AuthMode mode) {
        if (currentMode == mode) return;
        currentMode = mode;
        if (mode == AuthMode.LOGIN) {
            tabLogin.setBackgroundResource(R.drawable.bg_tab_selected);
            tabLogin.setTextColor(getColor(R.color.white));
            tabRegister.setBackgroundResource(android.R.color.transparent);
            tabRegister.setTextColor(getColor(R.color.geo_text_secondary));
            formLogin.setVisibility(View.VISIBLE);
            formLogin.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            formRegister.setVisibility(View.GONE);
        } else {
            tabRegister.setBackgroundResource(R.drawable.bg_tab_selected);
            tabRegister.setTextColor(getColor(R.color.white));
            tabLogin.setBackgroundResource(android.R.color.transparent);
            tabLogin.setTextColor(getColor(R.color.geo_text_secondary));
            formRegister.setVisibility(View.VISIBLE);
            formRegister.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            formLogin.setVisibility(View.GONE);
        }
        clearAllErrors();
    }

    private void setupLoginForm() {
        loginButton.setOnClickListener(v -> attemptLogin());
        forgotPasswordText.setOnClickListener(v -> {
            String email = loginEmailInput.getText() != null ? loginEmailInput.getText().toString().trim() : "";
            if (email.isEmpty()) { showLoginError("Entrez votre email pour réinitialiser."); return; }
            setLoading(true);
            authService.sendPasswordResetEmail(email, new IAuthService.AuthCallback() {
                @Override public void onSuccess(User user) { runOnUiThread(() -> { setLoading(false); showLoginError("Email envoyé !"); }); }
                @Override public void onFailure(String err) { runOnUiThread(() -> { setLoading(false); showLoginError(err); }); }
            });
        });
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                setLoading(true);
                                authService.loginWithGoogle(account.getIdToken(), new IAuthService.AuthCallback() {
                                    @Override public void onSuccess(User user) { runOnUiThread(() -> { setLoading(false); subscribeAndNavigate(user); }); }
                                    @Override public void onFailure(String err)  { runOnUiThread(() -> { setLoading(false); showLoginError(err); }); }
                                });
                            }
                        } catch (ApiException e) {
                            setLoading(false);
                            showLoginError("Échec Google : " + e.getStatusCode());
                        }
                    } else {
                        setLoading(false);
                    }
                }
        );

        loginGoogleButton.setOnClickListener(v -> {
            setLoading(true);
            googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
        });
    }

    private void attemptLogin() {
        clearAllErrors();
        String email    = loginEmailInput.getText() != null ? loginEmailInput.getText().toString().trim() : "";
        String password = loginPasswordInput.getText() != null ? loginPasswordInput.getText().toString() : "";
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) { loginEmailLayout.setError("Email invalide"); return; }
        if (password.isEmpty()) { loginPasswordLayout.setError("Mot de passe requis"); return; }

        setLoading(true);
        authService.login(email, password, new IAuthService.AuthCallback() {
            @Override public void onSuccess(User user) { runOnUiThread(() -> { setLoading(false); subscribeAndNavigate(user); }); }
            @Override public void onFailure(String err) { runOnUiThread(() -> { setLoading(false); showLoginError(translateFirebaseError(err)); }); }
        });
    }

    private void setupRegisterForm() {
        registerButton.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        clearAllErrors();
        String firstName       = registerFirstNameInput.getText() != null ? registerFirstNameInput.getText().toString().trim() : "";
        String email           = registerEmailInput.getText() != null ? registerEmailInput.getText().toString().trim() : "";
        String password        = registerPasswordInput.getText() != null ? registerPasswordInput.getText().toString() : "";
        String confirmPassword = registerConfirmPasswordInput.getText() != null ? registerConfirmPasswordInput.getText().toString() : "";
        String lastName        = registerLastNameInput.getText() != null ? registerLastNameInput.getText().toString().trim() : "";

        if (firstName.isEmpty()) { ((TextInputLayout) findViewById(R.id.registerFirstNameLayout)).setError("Requis"); return; }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) { registerEmailLayout.setError("Email invalide"); return; }
        if (password.length() < 8) { registerPasswordLayout.setError("8 caractères min."); return; }
        if (!password.equals(confirmPassword)) { registerConfirmPasswordLayout.setError("Différents"); return; }

        setLoading(true);
        authService.register(email, password, firstName, lastName, new IAuthService.AuthCallback() {
            @Override public void onSuccess(User user) { runOnUiThread(() -> { setLoading(false); subscribeAndNavigate(user); }); }
            @Override public void onFailure(String err) { runOnUiThread(() -> { setLoading(false); showRegisterError(translateFirebaseError(err)); }); }
        });
    }

    /**
     * Abonne l'utilisateur au topic FCM "all_users" puis route vers l'activité adaptée.
     */
    private void subscribeAndNavigate(User user) {
        FirebaseMessaging.getInstance().subscribeToTopic("all_users");
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult();

                // On utilise le repository pour mettre à jour l'utilisateur
                java.util.Map<String, Object> updates = new java.util.HashMap<>();
                updates.put("fcmToken", token);

                // Mise à jour directe dans Firestore via votre DataService ou Repository
                fr.upjv.geoevent.domain.data.DataServiceFactory.create()
                        .update("users", user.getUid(), updates);

                android.util.Log.d("ConnexionActivity", "Jeton FCM mis à jour : " + token);
            }
        });
        navigateBasedOnRole(user);
    }

    /**
     * Récupère le rôle Firestore de l'utilisateur et redirige vers
     * AdminActivity (admin) ou MainActivity (user).
     */
    private void navigateBasedOnRole(User user) {
        if (user == null) {
            goToActivity(MainActivity.class);
            return;
        }
        setLoading(true);
        userRepository.getUserById(user.getUid(), new DataCallback() {
            @Override
            public void onSuccess(Object data) {
                runOnUiThread(() -> {
                    setLoading(false);
                    String role = User.ROLE_USER;
                    if (data instanceof DocumentSnapshot) {
                        DocumentSnapshot doc = (DocumentSnapshot) data;
                        if (doc.exists() && doc.getString("role") != null) {
                            role = doc.getString("role");
                        }
                    }
                    goToActivity(User.ROLE_ADMIN.equals(role) ? AdminActivity.class : MainActivity.class);
                });
            }
            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> { setLoading(false); goToActivity(MainActivity.class); });
            }
        });
    }

    private void goToActivity(Class<?> target) {
        startActivity(new Intent(this, target)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void setLoading(boolean isLoading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (loginButton != null) loginButton.setEnabled(!isLoading);
        if (loginGoogleButton != null) loginGoogleButton.setEnabled(!isLoading);
        if (registerButton != null) registerButton.setEnabled(!isLoading);
    }

    private void showLoginError(String message) {
        loginErrorText.setText(message);
        loginErrorText.setVisibility(View.VISIBLE);
    }

    private void showRegisterError(String message) {
        registerErrorText.setText(message);
        registerErrorText.setVisibility(View.VISIBLE);
    }

    private void clearAllErrors() {
        loginEmailLayout.setError(null);
        loginPasswordLayout.setError(null);
        loginErrorText.setVisibility(View.GONE);
        registerEmailLayout.setError(null);
        registerPasswordLayout.setError(null);
        registerConfirmPasswordLayout.setError(null);
        registerErrorText.setVisibility(View.GONE);
    }


    private String translateFirebaseError(String firebaseMessage) {

        if (firebaseMessage == null) return "Une erreur est survenue.";
        
        String msg = firebaseMessage.toLowerCase();
        
        // Gestion du message générique pour identifiants invalides
        if (msg.contains("credential") || msg.contains("invalid") || msg.contains("expired")) {
            return "Email ou mot de passe incorrect.";
        }
        
        if (msg.contains("password")) {
            return "Mot de passe incorrect.";
        }
        
        if (msg.contains("user-not-found") || msg.contains("no user")) {
            return "Aucun compte trouvé avec cet email.";
        }
        
        if (msg.contains("email-already-in-use") || msg.contains("email_exists")) {
            return "Cet email est déjà utilisé par un autre compte.";
        }

        if (msg.contains("network") || msg.contains("connection")) {
            return "Problème de connexion réseau. Vérifiez votre internet.";
        }
        
        return "Erreur d'authentification : " + firebaseMessage;
    }
}
