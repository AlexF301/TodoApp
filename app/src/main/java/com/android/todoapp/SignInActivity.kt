package com.android.todoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.amplifyframework.auth.AuthProvider
import com.amplifyframework.core.Amplify
import com.android.todoapp.databinding.ActivitySignInBinding
import kotlin.math.sign


class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        installSplashScreen()
        setContentView(binding.root)

        // check the current auth session.
        Amplify.Auth.fetchAuthSession(
            {
                Log.i("AmplifyQuickstart", "Auth session = $it")
                //start activity if user is already signed in
                if (it.isSignedIn)
                    launchMainActivity()
                else
                    launchGoogleSignIn()
            },
            { error ->
                Log.e("AmplifyQuickstart", "Failed to fetch auth session", error)
            }
        )
    }

    /**
     * launches the HostedWebUI cognito uses by default to use social sign in
     */
    private fun launchGoogleSignIn() {
        Amplify.Auth.signInWithSocialWebUI(
            // Authenticate with Google social sign in
            AuthProvider.google(),
            this,
            {
                Log.i("AuthQuickstart", "Sign in OK: $it")
                // launch MainActivity when user successfully signs in
                launchMainActivity()
            },
            {
                Log.e("AuthQuickstart", "Sign in failed", it)
                // if it fails, try to launch again. Exception provides the message
                // "recoverySuggestion=To recover: catch this error, and show the sign-in screen again."
                launchGoogleSignIn()
            }
        )
    }

    /**
     * Intent to start Main Activity
     */
    private fun launchMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}