import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class MobileLockWebApp {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                System.out.println("Invalid port supplied. Using default port 8080.");
            }
        }

        MobileLockSystem lockSystem = new MobileLockSystem();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new LockPageHandler(lockSystem));
        server.setExecutor(null);
        server.start();

        System.out.println("Mobile Lock System is running.");
        System.out.println("Open http://localhost:" + port + " in your browser.");
    }
}

class MobileLockSystem {
    private String pin;
    private boolean configured;
    private boolean locked;
    private String lastAction;

    public MobileLockSystem() {
        this.pin = "";
        this.configured = false;
        this.locked = true;
        this.lastAction = "Waiting for setup";
    }

    public synchronized AccessMessage setPin(String newPin) {
        ValidationResult validation = PinValidator.validate(newPin);
        if (!validation.isValid()) {
            return AccessMessage.error(validation.getMessage());
        }
        if (configured) {
            return AccessMessage.error("A PIN is already set. Use Change PIN instead.");
        }

        this.pin = newPin;
        this.configured = true;
        this.locked = true;
        this.lastAction = "PIN created";
        return AccessMessage.success("PIN set successfully. Your mobile is locked.");
    }

    public synchronized AccessMessage unlock(String enteredPin) {
        if (!configured) {
            return AccessMessage.error("No PIN is set yet. Create a PIN first.");
        }
        ValidationResult validation = PinValidator.validate(enteredPin);
        if (!validation.isValid()) {
            return AccessMessage.error(validation.getMessage());
        }
        if (!pin.equals(enteredPin)) {
            locked = true;
            lastAction = "Failed unlock attempt";
            return AccessMessage.error("Incorrect PIN. Access denied.");
        }

        locked = false;
        lastAction = "Device unlocked";
        return AccessMessage.success("Correct PIN. Access granted.");
    }

    public synchronized AccessMessage lock() {
        if (!configured) {
            return AccessMessage.error("Create a PIN before locking the mobile.");
        }
        locked = true;
        lastAction = "Device locked";
        return AccessMessage.success("Mobile locked successfully.");
    }

    public synchronized AccessMessage changePin(String oldPin, String newPin) {
        if (!configured) {
            return AccessMessage.error("No PIN is set yet. Create a PIN first.");
        }

        ValidationResult oldValidation = PinValidator.validate(oldPin);
        if (!oldValidation.isValid()) {
            return AccessMessage.error("Current PIN: " + oldValidation.getMessage());
        }
        if (!pin.equals(oldPin)) {
            lastAction = "Failed PIN change";
            return AccessMessage.error("Current PIN is incorrect. PIN was not changed.");
        }

        ValidationResult newValidation = PinValidator.validate(newPin);
        if (!newValidation.isValid()) {
            return AccessMessage.error("New PIN: " + newValidation.getMessage());
        }
        if (oldPin.equals(newPin)) {
            return AccessMessage.error("New PIN must be different from the current PIN.");
        }

        pin = newPin;
        locked = true;
        lastAction = "PIN changed";
        return AccessMessage.success("PIN changed successfully. Please unlock with the new PIN.");
    }

    public synchronized boolean isConfigured() {
        return configured;
    }

    public synchronized boolean isLocked() {
        return locked;
    }

    public synchronized String getLastAction() {
        return lastAction;
    }
}

class PinValidator {
    private static final int MIN_LENGTH = 4;
    private static final int MAX_LENGTH = 8;

    private PinValidator() {
    }

    public static ValidationResult validate(String pin) {
        if (pin == null || pin.isBlank()) {
            return new ValidationResult(false, "PIN cannot be empty.");
        }
        if (!pin.matches("\\d+")) {
            return new ValidationResult(false, "PIN must contain digits only.");
        }
        if (pin.length() < MIN_LENGTH || pin.length() > MAX_LENGTH) {
            return new ValidationResult(false, "PIN must be 4 to 8 digits long.");
        }
        if (pin.matches("(\\d)\\1+")) {
            return new ValidationResult(false, "PIN cannot use the same digit repeatedly.");
        }
        return new ValidationResult(true, "PIN is valid.");
    }
}

class ValidationResult {
    private final boolean valid;
    private final String message;

    public ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }
}

class AccessMessage {
    private final boolean success;
    private final String text;

    public AccessMessage(boolean success, String text) {
        this.success = success;
        this.text = text;
    }

    public static AccessMessage success(String text) {
        return new AccessMessage(true, text);
    }

    public static AccessMessage error(String text) {
        return new AccessMessage(false, text);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getText() {
        return text;
    }
}

class LockPageHandler implements HttpHandler {
    private final MobileLockSystem lockSystem;

    public LockPageHandler(MobileLockSystem lockSystem) {
        this.lockSystem = lockSystem;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"/".equals(exchange.getRequestURI().getPath())) {
            send(exchange, 404, "text/plain; charset=utf-8", "Page not found");
            return;
        }

        AccessMessage message = null;
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> form = readForm(exchange);
            message = processAction(form);
        }

        send(exchange, 200, "text/html; charset=utf-8", renderPage(message));
    }

    private AccessMessage processAction(Map<String, String> form) {
        String action = form.getOrDefault("action", "");
        switch (action) {
            case "set":
                return lockSystem.setPin(form.get("newPin"));
            case "unlock":
                return lockSystem.unlock(form.get("pin"));
            case "lock":
                return lockSystem.lock();
            case "change":
                return lockSystem.changePin(form.get("oldPin"), form.get("newPin"));
            default:
                return AccessMessage.error("Unknown action requested.");
        }
    }

    private Map<String, String> readForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> values = new LinkedHashMap<>();
        if (body.isBlank()) {
            return values;
        }

        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            values.put(key, value);
        }
        return values;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String renderPage(AccessMessage message) {
        String statusText = lockSystem.isConfigured()
                ? (lockSystem.isLocked() ? "LOCKED" : "UNLOCKED")
                : "SETUP REQUIRED";
        String statusClass = lockSystem.isConfigured()
                ? (lockSystem.isLocked() ? "locked" : "unlocked")
                : "setup";
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

        return """
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>Java Mobile Lock System</title>
                    <style>
                        * { box-sizing: border-box; }
                        body {
                            margin: 0;
                            min-height: 100vh;
                            font-family: Arial, Helvetica, sans-serif;
                            color: #17202a;
                            background: linear-gradient(135deg, #eef2f3 0%%, #dfe9f3 48%%, #f7f2e8 100%%);
                            display: grid;
                            place-items: center;
                            padding: 28px;
                        }
                        main {
                            width: min(1080px, 100%%);
                            display: grid;
                            grid-template-columns: 340px 1fr;
                            gap: 28px;
                            align-items: stretch;
                        }
                        .phone {
                            background: #111820;
                            border: 10px solid #0a0d10;
                            border-radius: 34px;
                            box-shadow: 0 24px 55px rgba(19, 32, 48, 0.28);
                            min-height: 620px;
                            padding: 22px;
                            color: white;
                            display: flex;
                            flex-direction: column;
                            justify-content: space-between;
                        }
                        .speaker {
                            width: 86px;
                            height: 7px;
                            background: #2c3844;
                            border-radius: 20px;
                            margin: 0 auto 24px;
                        }
                        .screen {
                            flex: 1;
                            border-radius: 22px;
                            padding: 28px 22px;
                            background: linear-gradient(160deg, #1b2733 0%%, #273f55 54%%, #31535a 100%%);
                            display: flex;
                            flex-direction: column;
                            justify-content: center;
                            text-align: center;
                            gap: 22px;
                        }
                        .status {
                            display: inline-block;
                            align-self: center;
                            border-radius: 999px;
                            padding: 9px 15px;
                            font-weight: 700;
                            letter-spacing: 1px;
                            font-size: 13px;
                        }
                        .locked { background: #f45b69; color: #fff; }
                        .unlocked { background: #35ce8d; color: #08251a; }
                        .setup { background: #ffd166; color: #3d2b00; }
                        h1 {
                            margin: 0;
                            font-size: 31px;
                            line-height: 1.15;
                        }
                        .time {
                            color: #d8e4ec;
                            font-size: 14px;
                        }
                        .dot-row {
                            display: flex;
                            justify-content: center;
                            gap: 10px;
                        }
                        .dot {
                            width: 14px;
                            height: 14px;
                            border: 2px solid rgba(255, 255, 255, .75);
                            border-radius: 50%%;
                        }
                        .home {
                            width: 58px;
                            height: 58px;
                            border: 2px solid #2d3742;
                            border-radius: 50%%;
                            margin: 20px auto 0;
                        }
                        .panel {
                            background: rgba(255, 255, 255, .88);
                            border: 1px solid rgba(23, 32, 42, .1);
                            border-radius: 8px;
                            box-shadow: 0 18px 45px rgba(19, 32, 48, 0.16);
                            padding: 24px;
                        }
                        .panel h2 {
                            margin: 0 0 6px;
                            font-size: 25px;
                        }
                        .panel p {
                            margin: 0 0 18px;
                            color: #4d5b68;
                        }
                        .message {
                            padding: 13px 15px;
                            border-radius: 7px;
                            margin-bottom: 18px;
                            font-weight: 700;
                        }
                        .message.ok {
                            background: #dff7ec;
                            color: #11623c;
                            border: 1px solid #9ee6c3;
                        }
                        .message.error {
                            background: #ffe5e9;
                            color: #9b1f31;
                            border: 1px solid #ffb9c3;
                        }
                        .grid {
                            display: grid;
                            grid-template-columns: repeat(2, minmax(0, 1fr));
                            gap: 16px;
                        }
                        form {
                            border: 1px solid #d7dee5;
                            border-radius: 8px;
                            padding: 16px;
                            background: #ffffff;
                        }
                        form h3 {
                            margin: 0 0 12px;
                            font-size: 18px;
                        }
                        label {
                            display: block;
                            font-size: 13px;
                            font-weight: 700;
                            color: #344250;
                            margin: 12px 0 6px;
                        }
                        input {
                            width: 100%%;
                            height: 42px;
                            border: 1px solid #bcc8d4;
                            border-radius: 6px;
                            padding: 0 12px;
                            font-size: 17px;
                        }
                        button {
                            width: 100%%;
                            height: 42px;
                            margin-top: 14px;
                            border: 0;
                            border-radius: 6px;
                            background: #1f7a8c;
                            color: #ffffff;
                            font-weight: 700;
                            font-size: 15px;
                            cursor: pointer;
                        }
                        button.secondary { background: #2f3e46; }
                        .rules {
                            margin-top: 18px;
                            padding: 14px 16px;
                            border-radius: 8px;
                            background: #edf6f9;
                            color: #334b54;
                            font-size: 14px;
                            line-height: 1.55;
                        }
                        @media (max-width: 850px) {
                            main { grid-template-columns: 1fr; }
                            .phone { min-height: 470px; }
                            .grid { grid-template-columns: 1fr; }
                        }
                    </style>
                </head>
                <body>
                    <main>
                        <section class="phone" aria-label="Mobile phone preview">
                            <div>
                                <div class="speaker"></div>
                                <div class="screen">
                                    <span class="status %s">%s</span>
                                    <h1>Mobile Lock System</h1>
                                    <div class="dot-row" aria-hidden="true">
                                        <span class="dot"></span><span class="dot"></span><span class="dot"></span><span class="dot"></span>
                                    </div>
                                    <div class="time">%s</div>
                                    <div>Last action: %s</div>
                                </div>
                            </div>
                            <div class="home"></div>
                        </section>
                        <section class="panel">
                            <h2>Java Password / PIN Control</h2>
                            <p>Set, unlock, lock again, or change the PIN with validation and access messages.</p>
                            %s
                            <div class="grid">
                                <form method="post">
                                    <h3>Set PIN</h3>
                                    <input type="hidden" name="action" value="set">
                                    <label for="set-pin">New PIN</label>
                                    <input id="set-pin" name="newPin" type="password" inputmode="numeric" maxlength="8" placeholder="4 to 8 digits">
                                    <button type="submit">Set PIN</button>
                                </form>
                                <form method="post">
                                    <h3>Unlock Mobile</h3>
                                    <input type="hidden" name="action" value="unlock">
                                    <label for="unlock-pin">PIN</label>
                                    <input id="unlock-pin" name="pin" type="password" inputmode="numeric" maxlength="8" placeholder="Enter PIN">
                                    <button type="submit">Unlock</button>
                                </form>
                                <form method="post">
                                    <h3>Change PIN</h3>
                                    <input type="hidden" name="action" value="change">
                                    <label for="old-pin">Current PIN</label>
                                    <input id="old-pin" name="oldPin" type="password" inputmode="numeric" maxlength="8" placeholder="Current PIN">
                                    <label for="new-pin">New PIN</label>
                                    <input id="new-pin" name="newPin" type="password" inputmode="numeric" maxlength="8" placeholder="New PIN">
                                    <button type="submit">Change PIN</button>
                                </form>
                                <form method="post">
                                    <h3>Lock Mobile</h3>
                                    <input type="hidden" name="action" value="lock">
                                    <p>Use this after unlocking to return the mobile to locked mode.</p>
                                    <button class="secondary" type="submit">Lock Now</button>
                                </form>
                            </div>
                            <div class="rules">PIN rules: digits only, 4 to 8 digits, and not the same digit repeated.</div>
                        </section>
                    </main>
                </body>
                </html>
                """.formatted(
                escapeHtml(statusClass),
                escapeHtml(statusText),
                escapeHtml(now),
                escapeHtml(lockSystem.getLastAction()),
                renderMessage(message)
        );
    }

    private String renderMessage(AccessMessage message) {
        if (message == null) {
            return "";
        }
        String cssClass = message.isSuccess() ? "ok" : "error";
        return "<div class=\"message " + cssClass + "\">" + escapeHtml(message.getText()) + "</div>";
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void send(HttpExchange exchange, int statusCode, String contentType, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }
}
