package com.example.smartfinance.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {

    private const val SMTP_HOST = "smtp.gmail.com"
    private const val SMTP_PORT = "587"
    private const val FROM_EMAIL = "your-email@gmail.com"
    private const val FROM_PASSWORD = "your-app-password"

    suspend fun sendVerificationCode(toEmail: String, code: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val props = Properties().apply {
                    put("mail.smtp.host", SMTP_HOST)
                    put("mail.smtp.port", SMTP_PORT)
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                }
                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD)
                    }
                })
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(FROM_EMAIL))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                    subject = "SmartFinance - Verification Code"
                    setText("Your verification code is: $code\n\nThis code will expire in 10 minutes.")
                }
                Transport.send(message)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
