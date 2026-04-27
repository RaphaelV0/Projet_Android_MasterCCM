package fr.upjv.geoevent.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.auth.AuthServiceFactory;
import fr.upjv.geoevent.domain.models.User;
import fr.upjv.geoevent.services.IAuthService;

/**
 * Écran unique d'authentification.
 */
public class ConnexionActivity extends AppCompatActivity {

    private enum AuthMode {LOGIN, REGISTER}
    private AuthMode currentMode = AuthMode.LOGIN;
    private IAuthService authService;

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

        authService = AuthServiceFactory.create();

        if (authService.isLoggedIn()) {
            navigateToMain();
            return;
        }

        initViews();
        setupTabSwitcher();
        setupLoginForm();
        setupRegisterForm();
        setupGoogleSignIn();
    }

    private void initViews() {
        tabLogin = findViewById(R.id.tabLogin);
        tabRegister = findViewById(R.id.tabRegister);
        formLogin = findViewById(R.id.formLogin);
        formRegister = findViewById(R.id.formRegister);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        loginEmailInput = findViewById(R.id.loginEmailInput);
        loginPasswordInput = findViewById(R.id.loginPasswordInput);
        loginEmailLayout = findViewById(R.id.loginEmailLayout);
        loginPasswordLayout = findViewById(R.id.loginPasswordLayout);
        loginErrorText = findViewById(R.id.loginErrorText);
        loginButton = findViewById(R.id.loginButton);
        loginGoogleButton = findViewById(R.id.loginGoogleButton);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        registerFirstNameInput = findViewById(R.id.registerFirstNameInput);
        registerLastNameInput = findViewById(R.id.registerLastNameInput);
        registerEmailInput = findViewById(R.id.registerEmailInput);
        registerPasswordInput = findViewById(R.id.registerPasswordInput);
        registerConfirmPasswordInput = findViewById(R.id.registerConfirmPasswordInput);
        registerEmailLayout = findViewById(R.id.registerEmailLayout);
        registerPasswordLayout = findViewById(R.id.registerPasswordLayout);
        registerConfirmPasswordLayout = findViewById(R.id.registerConfirmPasswordLayout);
        registerErrorText = findViewById(R.id.registerErrorText);
        registerButton = findViewById(R.id.registerButton);
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
            if (email.isEmpty()) {
                showLoginError("Entrez votre email pour réinitialiser.");
                return;
            }
            setLoading(true);
            authService.sendPasswordResetEmail(email, new IAuthService.AuthCallback() {
                @Override public void onSuccess(User user) {
                    runOnUiThread(() -> { setLoading(false); showLoginError("Email envoyé !"); });
                }
                @Override public void onFailure(String err) {
                    runOnUiThread(() -> { setLoading(false); showLoginError(err); });
                }
            });
        });
    }

    private void setupGoogleSignIn() {
        // Configuration Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Généré par Firebase
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
                                firebaseAuthWithGoogle(account.getIdToken());
                            }
                        } catch (ApiException e) {
                            showLoginError("Échec Google : " + e.getStatusCode());
                        }
                    }
                }
        );

        loginGoogleButton.setOnClickListener(v -> {
            setLoading(true);
            googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        authService.loginWithGoogle(idToken, new IAuthService.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> { setLoading(false); navigateToMain(); });
            }
            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> { setLoading(false); showLoginError(errorMessage); });
            }
        });
    }

    private void attemptLogin() {
        clearAllErrors();
        String email = loginEmailInput.getText() != null ? loginEmailInput.getText().toString().trim() : "";
        String password = loginPasswordInput.getText() != null ? loginPasswordInput.getText().toString() : "";
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmailLayout.setError("Email invalide");
            return;
        }
        if (password.isEmpty()) {
            loginPasswordLayout.setError("Mot de passe requis");
            return;
        }
        setLoading(true);
        authService.login(email, password, new IAuthService.AuthCallback() {
            @Override public void onSuccess(User user) { runOnUiThread(() -> { setLoading(false); navigateToMain(); }); }
            @Override public void onFailure(String err) { runOnUiThread(() -> { setLoading(false); showLoginError(translateFirebaseError(err)); }); }
        });
    }

    private void setupRegisterForm() {
        registerButton.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        clearAllErrors();
        String firstName = registerFirstNameInput.getText() != null ? registerFirstNameInput.getText().toString().trim() : "";
        String lastName = registerLastNameInput.getText() != null ? registerLastNameInput.getText().toString().trim() : "";
        String email = registerEmailInput.getText() != null ? registerEmailInput.getText().toString().trim() : "";
        String password = registerPasswordInput.getText() != null ? registerPasswordInput.getText().toString() : "";
        String confirmPassword = registerConfirmPasswordInput.getText() != null ? registerConfirmPasswordInput.getText().toString() : "";

        if (firstName.isEmpty()) { ((TextInputLayout)findViewById(R.id.registerFirstNameLayout)).setError("Requis"); return; }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) { registerEmailLayout.setError("Email invalide"); return; }
        if (password.length() < 8) { registerPasswordLayout.setError("8 caractères min."); return; }
        if (!password.equals(confirmPassword)) { registerConfirmPasswordLayout.setError("Différents"); return; }

        setLoading(true);
        authService.register(email, password, firstName, lastName, new IAuthService.AuthCallback() {
            @Override public void onSuccess(User user) { runOnUiThread(() -> { setLoading(false); navigateToMain(); }); }
            @Override public void onFailure(String err) { runOnUiThread(() -> { setLoading(false); showRegisterError(translateFirebaseError(err)); }); }
        });
    }

    private void setLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!isLoading);
        loginGoogleButton.setEnabled(!isLoading);
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

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private String translateFirebaseError(String firebaseMessage) {
        if (firebaseMessage == null) return "Erreur inconnue";
        String msg = firebaseMessage.toLowerCase();
        if (msg.contains("password")) return "Mot de passe incorrect.";
        if (msg.contains("user-not-found")) return "Compte inconnu.";
        if (msg.contains("email-already-in-use")) return "Email déjà utilisé.";
        return firebaseMessage;
    }
}
