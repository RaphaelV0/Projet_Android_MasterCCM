package fr.upjv.geoevent.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.services.IAuthService;
import fr.upjv.geoevent.domain.data.firebase.FirebaseAuthServiceImpl;
import fr.upjv.geoevent.domain.models.User;

/**
 * Écran unique d'authentification.
 * Gère deux états : CONNEXION et INSCRIPTION, via un tab switcher animé.
 *
 * Dépend uniquement de IAuthService : si le collègue change l'implémentation
 * (Firebase → Supabase), seule la ligne d'instanciation change ici.
 */
public class ConnexionActivity extends AppCompatActivity {

    private enum AuthMode { LOGIN, REGISTER }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);

        authService = new FirebaseAuthServiceImpl();

        if (authService.isLoggedIn()) {
            navigateToMain();
            return;
        }

        initViews();
        setupTabSwitcher();
        setupLoginForm();
        setupRegisterForm();
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
            String email = "";
            if (loginEmailInput.getText() != null) {
                email = loginEmailInput.getText().toString().trim();
            }
            if (email.isEmpty()) {
                showLoginError("Entrez votre email pour réinitialiser le mot de passe.");
                return;
            }
            setLoading(true);
            authService.sendPasswordResetEmail(email, new IAuthService.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    runOnUiThread(() -> {
                        setLoading(false);
                        showLoginError("Email de réinitialisation envoyé !");
                        loginErrorText.setTextColor(getColor(R.color.geo_success));
                    });
                }
                @Override
                public void onFailure(String errorMessage) {
                    runOnUiThread(() -> {
                        setLoading(false);
                        showLoginError(errorMessage);
                    });
                }
            });
        });

        loginGoogleButton.setOnClickListener(v -> {
            showLoginError("Connexion Google — à implémenter avec Firebase.");
        });
    }

    private void attemptLogin() {
        clearAllErrors();

        String email    = loginEmailInput.getText() != null ? loginEmailInput.getText().toString().trim() : "";
        String password = loginPasswordInput.getText() != null ? loginPasswordInput.getText().toString() : "";

        boolean valid = true;

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmailLayout.setError("Email invalide");
            valid = false;
        }
        if (password.isEmpty()) {
            loginPasswordLayout.setError("Mot de passe requis");
            valid = false;
        }

        if (!valid) return;

        setLoading(true);
        authService.login(email, password, new IAuthService.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    setLoading(false);
                    navigateToMain();
                });
            }
            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showLoginError(translateFirebaseError(errorMessage));
                });
            }
        });
    }

    private void setupRegisterForm() {
        registerButton.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        clearAllErrors();

        String firstName       = registerFirstNameInput.getText() != null ? registerFirstNameInput.getText().toString().trim() : "";
        String lastName        = registerLastNameInput.getText() != null ? registerLastNameInput.getText().toString().trim() : "";
        String email           = registerEmailInput.getText() != null ? registerEmailInput.getText().toString().trim() : "";
        String password        = registerPasswordInput.getText() != null ? registerPasswordInput.getText().toString() : "";
        String confirmPassword = registerConfirmPasswordInput.getText() != null ? registerConfirmPasswordInput.getText().toString() : "";

        boolean valid = true;

        if (firstName.isEmpty()) {
            findViewById(R.id.registerFirstNameLayout);
            valid = false;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            registerEmailLayout.setError("Email invalide");
            valid = false;
        }
        if (password.length() < 8) {
            registerPasswordLayout.setError("8 caractères minimum");
            valid = false;
        }
        if (!password.equals(confirmPassword)) {
            registerConfirmPasswordLayout.setError("Les mots de passe ne correspondent pas");
            valid = false;
        }

        if (!valid) return;

        setLoading(true);
        authService.register(email, password, firstName, lastName, new IAuthService.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    setLoading(false);
                    navigateToMain();
                });
            }
            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showRegisterError(translateFirebaseError(errorMessage));
                });
            }
        });
    }

    private void setLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!isLoading);
        registerButton.setEnabled(!isLoading);
    }

    private void showLoginError(String message) {
        loginErrorText.setText(message);
        loginErrorText.setTextColor(getColor(R.color.geo_error));
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
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Traduit les messages d'erreur Firebase (en anglais) en messages
     * compréhensibles pour l'utilisateur francophone.
     */
    private String translateFirebaseError(String firebaseMessage) {
        if (firebaseMessage == null) return "Une erreur est survenue.";
        if (firebaseMessage.contains("password is invalid") || firebaseMessage.contains("INVALID_PASSWORD")) {
            return "Mot de passe incorrect.";
        }
        if (firebaseMessage.contains("no user record") || firebaseMessage.contains("USER_NOT_FOUND")) {
            return "Aucun compte trouvé pour cet email.";
        }
        if (firebaseMessage.contains("email address is already in use") || firebaseMessage.contains("EMAIL_EXISTS")) {
            return "Cette adresse email est déjà utilisée.";
        }
        if (firebaseMessage.contains("network")) {
            return "Erreur réseau. Vérifiez votre connexion.";
        }
        return firebaseMessage;
    }
}