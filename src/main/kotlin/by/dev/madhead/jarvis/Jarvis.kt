package by.dev.madhead.jarvis

import by.dev.madhead.jarvis.model.Email
import freemarker.template.Configuration
import java.io.StringWriter
import java.time.LocalDateTime
import java.util.Properties
import javax.activation.DataHandler
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

object Jarvis {
	val session = Session.getDefaultInstance(
			Properties().apply {
				this["mail.smtp.host"] = System.getenv("JARVIS_SMTP_HOST")
				this["mail.smtp.port"] = System.getenv("JARVIS_SMTP_PORT")
				this["mail.smtp.auth"] = (!System.getenv("JARVIS_SMTP_USER").isNullOrBlank()).toString()
				this["mail.smtp.starttls.enable"] = System.getenv("JARVIS_SMTP_TLS")
			},
			if (!System.getenv("JARVIS_SMTP_USER").isNullOrBlank()) {
				object : Authenticator() {
					override fun getPasswordAuthentication(): PasswordAuthentication? {
						return PasswordAuthentication(
								System.getenv("JARVIS_SMTP_USER"),
								System.getenv("JARVIS_SMTP_PASSWORD")
						)
					}
				}
			} else {
				null
			}
	)

	fun notify(email: Email) {
		val configuration = Configuration(Configuration.VERSION_2_3_23)

		configuration.urlEscapingCharset = "UTF-8"
		configuration.setClassForTemplateLoading(Jarvis::class.java, "/")

		val template = configuration.getTemplate("by/dev/madhead/jarvis/jarvis.ftl")
		val result = StringWriter()

		template.process(email, result)

		val content = MimeMultipart().apply {
			addBodyPart(MimeBodyPart().apply {
				setContent(result.toString(), "text/html; charset=utf-8")
			})
			addBodyPart(MimeBodyPart().apply {
				contentID = "<status.png>"
				description = "Build status"
				disposition = """inline; filename="${contentID.replace(Regex("[<>]"), "")}""""
				dataHandler = DataHandler(ByteArrayDataSource(Jarvis::class.java.getResourceAsStream("/by/dev/madhead/jarvis/success.png"), "image/png"))
			})
			addBodyPart(MimeBodyPart().apply {
				contentID = "<stopwatch.png>"
				description = "Stopwatch"
				disposition = """inline; filename="${contentID.replace(Regex("[<>]"), "")}""""
				dataHandler = DataHandler(ByteArrayDataSource(Jarvis::class.java.getResourceAsStream("/by/dev/madhead/jarvis/stopwatch-success.png"), "image/png"))
			})
		}

		Transport.send(
				MimeMessage(session).apply {
					setFrom(InternetAddress(System.getenv("JARVIS_FROM")))
					System.getenv("JARVIS_TO").split(",", " ", ";").forEach {
						addRecipient(Message.RecipientType.TO, InternetAddress(it))
					}
					subject = LocalDateTime.now().toString()
					setContent(content)
				}
		)
	}
}