package com.fitmate.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.fitmate.R
import com.fitmate.ui.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

@Suppress("DEPRECATION")
@Composable
fun rememberGoogleSignInAction(viewModel: AuthViewModel): () -> Unit {
    val context = LocalContext.current
    val googleSignInClient = remember(context) {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, options)
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val account = GoogleSignIn
                .getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                viewModel.showError(
                    "Google Sign-In did not return an ID token. Check Firebase SHA and OAuth client configuration."
                )
                return@rememberLauncherForActivityResult
            }
            viewModel.signInWithCredential(
                GoogleAuthProvider.getCredential(idToken, null),
                "Google Sign-In failed"
            )
        } catch (exception: ApiException) {
            viewModel.showError(
                "Google Sign-In failed: ${exception.statusCode}. Check SHA fingerprints and google-services.json."
            )
        } catch (exception: Exception) {
            viewModel.showError(exception.message ?: "Google Sign-In failed.")
        }
    }

    return remember(googleSignInClient, launcher) {
        { launcher.launch(googleSignInClient.signInIntent) }
    }
}
