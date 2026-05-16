package com.example.smartfinance.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.smartfinance.R
import com.example.smartfinance.ui.theme.CriticalRed
import com.example.smartfinance.ui.theme.DarkBackground
import com.example.smartfinance.ui.theme.DarkOnSurface
import com.example.smartfinance.ui.theme.DarkOnSurfaceVariant
import com.example.smartfinance.ui.theme.DarkSurface
import com.example.smartfinance.ui.theme.DarkSurfaceVariant
import com.example.smartfinance.ui.theme.HealthyGreen
import com.example.smartfinance.ui.theme.Purple
import com.example.smartfinance.ui.theme.Teal
import com.example.smartfinance.viewmodel.MainViewModel
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val accountState by viewModel.accountState.collectAsState()
    val verificationSent by viewModel.verificationSent.collectAsState()
    val verificationMessage by viewModel.verificationMessage.collectAsState()
    val reportUri by viewModel.reportUri.collectAsState()

    var name by remember { mutableStateOf(accountState.name) }
    var email by remember { mutableStateOf(accountState.email) }
    var linkedToGmail by remember { mutableStateOf(accountState.linkedToGmail) }
    var monthlyReport by remember { mutableStateOf(accountState.monthlyReport) }
    var inputCode by remember { mutableStateOf("") }

    val isLoggedIn = accountState.name.isNotBlank() || accountState.email.isNotBlank()
    val canModifyEmail = !isLoggedIn || email.isBlank()

    LaunchedEffect(reportUri) {
        reportUri?.let { uriStr ->
            val uri = android.net.Uri.parse(uriStr)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Report"))
            viewModel.clearReportUri()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = DarkOnSurface
                )
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = stringResource(R.string.profile),
                    modifier = Modifier.size(60.dp),
                    tint = if (isLoggedIn) Teal else DarkOnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (accountState.name.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.logged_in_as),
                            style = MaterialTheme.typography.titleSmall,
                            color = Teal
                        )
                        Text(
                            text = accountState.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = DarkOnSurface
                        )
                        if (accountState.email.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = accountState.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DarkOnSurfaceVariant
                                )
                            if (accountState.emailVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    modifier = Modifier.size(16.dp),
                                    tint = HealthyGreen
                                )
                            }
                        }
                    }
                }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.profile_information),
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkOnSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DarkOnSurface,
                            unfocusedTextColor = DarkOnSurface,
                            focusedBorderColor = Teal,
                            unfocusedBorderColor = DarkSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            if (canModifyEmail) {
                                email = it
                            }
                        },
                        label = { Text(stringResource(R.string.email)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = canModifyEmail,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DarkOnSurface,
                            unfocusedTextColor = if (canModifyEmail) DarkOnSurface else DarkOnSurfaceVariant,
                            focusedBorderColor = Teal,
                            unfocusedBorderColor = DarkSurfaceVariant,
                            disabledTextColor = DarkOnSurfaceVariant,
                            disabledBorderColor = DarkSurfaceVariant,
                            disabledLabelColor = DarkOnSurfaceVariant
                        )
                    )

                    if (email.isNotBlank() && !accountState.emailVerified) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.sendVerificationCode() },
                            colors = ButtonDefaults.buttonColors(containerColor = Teal)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.send_verification_code))
                        }
                        if (verificationSent) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = verificationMessage,
                                color = if (verificationMessage.contains("failed", true) || verificationMessage.contains("incorrect", true))
                                    CriticalRed else HealthyGreen,
                                style = MaterialTheme.typography.bodySmall
                            )
                            OutlinedTextField(
                                value = inputCode,
                                onValueChange = { inputCode = it },
                                label = { Text(stringResource(R.string.verification_code)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = DarkOnSurface,
                                    unfocusedTextColor = DarkOnSurface,
                                    focusedBorderColor = Teal,
                                    unfocusedBorderColor = DarkSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    viewModel.verifyCode(inputCode)
                                    inputCode = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = HealthyGreen)
                            ) {
                                Text(stringResource(R.string.verify))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoggedIn) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = DarkOnSurface
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.link_with_gmail),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = DarkOnSurface
                                )
                                Text(
                                    text = if (accountState.emailVerified) stringResource(R.string.connected_verified) else stringResource(R.string.not_verified),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (accountState.emailVerified) HealthyGreen else DarkOnSurfaceVariant
                                )
                            }
                            Switch(
                                checked = linkedToGmail,
                                onCheckedChange = { linkedToGmail = it },
                                enabled = accountState.emailVerified,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = Teal,
                                    checkedThumbColor = DarkSurface
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                tint = DarkOnSurface
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.monthly_report),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = DarkOnSurface
                                )
                                Text(
                                    text = stringResource(R.string.monthly_report_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkOnSurfaceVariant
                                )
                            }
                            Switch(
                                checked = monthlyReport,
                                onCheckedChange = { monthlyReport = it },
                                enabled = accountState.emailVerified,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = Teal,
                                    checkedThumbColor = DarkSurface
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { viewModel.generateReport(context) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Purple)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.generate_report))
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    viewModel.saveAccount(name, email, linkedToGmail, monthlyReport)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) {
                Text(if (isLoggedIn) stringResource(R.string.update) else stringResource(R.string.save_login))
            }

            if (isLoggedIn) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.logout()
                        name = ""
                        email = ""
                        linkedToGmail = false
                        monthlyReport = false
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CriticalRed)
                ) {
                    Text(stringResource(R.string.logout))
                }
            }
        }
    }
}
