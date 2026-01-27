package util;

// Utility class to validate user input
public class Validator {
    
    // Check if a string is not empty or only whitespace
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    // Check if email has valid format (contains @ and . after @)
    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) {
            return false;
        }
        
        // Must contain @
        if (!email.contains("@")) {
            return false;
        }
        
        // Must have something before @
        int atIndex = email.indexOf("@");
        if (atIndex == 0) {
            return false;
        }
        
        // Must have a dot after @
        String afterAt = email.substring(atIndex + 1);
        if (!afterAt.contains(".")) {
            return false;
        }
        
        // Must have something after the last dot
        int lastDotIndex = email.lastIndexOf(".");
        if (lastDotIndex == email.length() - 1) {
            return false;
        }
        
        return true;
    }
    
    // Check if phone contains only numbers and optional spaces, dashes, or plus sign
    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) {
            return false;
        }
        
        // Remove spaces, dashes, and plus sign for validation
        String cleaned = phone.replaceAll("[\\s\\-\\+]", "");
        
        // Must have at least 9 digits
        if (cleaned.length() < 9) {
            return false;
        }
        
        // Check if all remaining characters are digits
        return cleaned.matches("\\d+");
    }
    
    // Check if username is valid
    public static boolean isValidUsername(String username) {
        if (!isNotEmpty(username)) {
            return false;
        }
        
        // Length between 3 and 20
        if (username.length() < 3 || username.length() > 20) {
            return false;
        }
        
        // Only letters and numbers
        return username.matches("[a-zA-Z0-9]+");
    }
    
    // Check if password is strong enough
    public static boolean isValidPassword(String password) {
        if (!isNotEmpty(password)) {
            return false;
        }
        
        // At least 6 characters
        return password.length() >= 6;
    }
    
    // Check if a string contains only letters and spaces (for names)
    public static boolean isValidName(String name) {
        if (!isNotEmpty(name)) {
            return false;
        }
        
        // Only letters and spaces
        return name.matches("[\\p{L}\\s]+");
    }
    
    // Check if a string is a valid positive integer
    public static boolean isPositiveInteger(String value) {
        if (!isNotEmpty(value)) {
            return false;
        }
        
        try {
            int num = Integer.parseInt(value);
            return num > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // Check if a string is a valid non-negative integer
    public static boolean isNonNegativeInteger(String value) {
        if (!isNotEmpty(value)) {
            return false;
        }
        
        try {
            int num = Integer.parseInt(value);
            return num >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // Check if a string is a valid decimal number
    public static boolean isValidDecimal(String value) {
        if (!isNotEmpty(value)) {
            return false;
        }
        
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static String getEmailError() {
        return "Email invalido. Deve conter @ e um dominio valido (ex: user@example.com)";
    }
    
    public static String getPhoneError() {
        return "Telefone invalido. Deve conter apenas numeros (min. 9 digitos)";
    }
    
    public static String getUsernameError() {
        return "Username invalido. Deve ter 3-20 caracteres (letras e numeros)";
    }
    
    public static String getPasswordError() {
        return "Password invalida. Deve ter pelo menos 6 caracteres";
    }
    
    public static String getNameError() {
        return "Nome invalido. Deve conter apenas letras e espacos";
    }
}
