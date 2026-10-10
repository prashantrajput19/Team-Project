package com.examly.springapp.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SmtpMailer {
    private static final int TIMEOUT_MS = 10000;

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String from;

    public SmtpMailer(@Value("${spring.mail.host:}") String host,
                      @Value("${spring.mail.port:587}") int port,
                      @Value("${spring.mail.username:}") String username,
                      @Value("${spring.mail.password:}") String password,
                      @Value("${app.mail.from:no-reply@architectpoint.com}") String from) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.from = from;
    }

    public boolean isConfigured() {
        return !host.isBlank();
    }

    public void send(String to, String subject, String text) throws IOException {
        SSLSocketFactory tls = (SSLSocketFactory) SSLSocketFactory.getDefault();
        Socket socket = port == 465 ? tls.createSocket() : new Socket();
        socket.connect(new InetSocketAddress(host, port), TIMEOUT_MS);
        socket.setSoTimeout(TIMEOUT_MS);

        try {
            Session session = new Session(socket);
            session.expect(220);
            session.command("EHLO architectpoint", 250);

            if (port != 465) {
                session.command("STARTTLS", 220);
                SSLSocket secure = (SSLSocket) tls.createSocket(socket, host, port, true);
                secure.startHandshake();
                socket = secure;
                session = new Session(socket);
                session.command("EHLO architectpoint", 250);
            }

            if (!username.isBlank()) {
                session.command("AUTH LOGIN", 334);
                session.command(base64(username), 334);
                session.command(base64(password), 235);
            }

            session.command("MAIL FROM:<" + from + ">", 250);
            session.command("RCPT TO:<" + to + ">", 250);
            session.command("DATA", 354);
            session.command("From: ArchitectPoint <" + from + ">\r\n"
                    + "To: <" + to + ">\r\n"
                    + "Subject: " + subject + "\r\n"
                    + "Content-Type: text/plain; charset=UTF-8\r\n\r\n"
                    + text + "\r\n.", 250);
            session.command("QUIT", 221);
        } finally {
            socket.close();
        }
    }

    private static String base64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static final class Session {
        private final BufferedReader in;
        private final OutputStream out;

        Session(Socket socket) throws IOException {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.out = socket.getOutputStream();
        }

        void command(String line, int expectedCode) throws IOException {
            out.write((line + "\r\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
            expect(expectedCode);
        }

        void expect(int expectedCode) throws IOException {
            String line;
            do {
                line = in.readLine();
                if (line == null) {
                    throw new IOException("Mail server closed the connection");
                }
            } while (line.length() > 3 && line.charAt(3) == '-');

            if (!line.startsWith(String.valueOf(expectedCode))) {
                throw new IOException("Mail server replied: " + line);
            }
        }
    }
}