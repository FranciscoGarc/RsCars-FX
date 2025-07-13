package com.rscars.taller.rscarsfx;

import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class ValidationUtil {

    // Expresiones regulares
    private static final Pattern TEXT_ONLY_PATTERN = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$");
    private static final Pattern NUMBERS_ONLY_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^[0-9]+(\\.[0-9]{1,2})?$");
    // Cambiado: solo acepta exactamente 7 caracteres alfanuméricos
    private static final Pattern LICENSE_PLATE_PATTERN = Pattern.compile("^[a-zA-Z0-9]{7}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // --- Métodos de Validación Específicos ---

    public static boolean isTextOnly(String text) {
        return text != null && !text.trim().isEmpty() && TEXT_ONLY_PATTERN.matcher(text).matches();
    }

    public static boolean isNumbersOnly(String text) {
        return text != null && !text.trim().isEmpty() && NUMBERS_ONLY_PATTERN.matcher(text).matches();
    }

    public static boolean isDecimal(String text) {
        return text != null && !text.trim().isEmpty() && DECIMAL_PATTERN.matcher(text).matches();
    }

    public static boolean isValidLicensePlate(String plate) {
        return plate != null && !plate.trim().isEmpty() && LICENSE_PLATE_PATTERN.matcher(plate).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isPhoneValid(String phone) {
        return phone != null && phone.replaceAll("-", "").matches("^[0-9]{8}$");
    }

    public static boolean isDuiValid(String dui) {
        return dui != null && dui.replaceAll("-", "").matches("^[0-9]{9}$");
    }

    // --- Métodos de Formato Automático ---

    // Limita la entrada del TextField a 7 caracteres
    public static void limitLicensePlateLength(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 7) {
                textField.setText(newValue.substring(0, 7));
                textField.positionCaret(7);
            }
        });
    }

    public static void limitYearLength(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 4) {
                textField.setText(newValue.substring(0, 4));
                textField.positionCaret(4);
            }
        });
    }

    public static void autoFormatPhone(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
        String numbers = newValue.replaceAll("[^\\d]", "");
        if (numbers.length() > 8) {
                numbers = numbers.substring(0, 8);
            }

            StringBuilder formatted = new StringBuilder();
            if (numbers.length() > 4) {
                formatted.append(numbers.substring(0, 4));
                formatted.append("-");
                formatted.append(numbers.substring(4));
            } else {
                formatted.append(numbers);
            }

            if (!formatted.toString().equals(newValue)) {
                textField.setText(formatted.toString());
                textField.positionCaret(formatted.length());
            }
        });
    }

    public static void autoFormatHour(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            String numbers = newValue.replaceAll("[^\\d]", "");
            if (numbers.length() > 4) {
                numbers = numbers.substring(0, 4);
            }

            StringBuilder formatted = new StringBuilder();
            if (numbers.length() > 2) {
                formatted.append(numbers.substring(0, 2));
                formatted.append(":");
                formatted.append(numbers.substring(2));
            } else {
                formatted.append(numbers);
            }

            if (!formatted.toString().equals(newValue)) {
                textField.setText(formatted.toString());
                textField.positionCaret(formatted.length());
            }
        });
    }

    public static void autoFormatDui(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            String numbers = newValue.replaceAll("[^\\d]", "");
            if (numbers.length() > 9) {
                numbers = numbers.substring(0, 9);
            }

            StringBuilder formatted = new StringBuilder();
            if (numbers.length() > 8) {
                formatted.append(numbers.substring(0, 8));
                formatted.append("-");
                formatted.append(numbers.substring(8));
            } else {
                formatted.append(numbers);
            }

            if (!formatted.toString().equals(newValue)) {
                textField.setText(formatted.toString());
                textField.positionCaret(formatted.length());
            }
        });
    }

    // --- Métodos de Validación con Base de Datos ---

    public static boolean usuarioYaExiste(String usuario) {
        String sql = "SELECT COUNT(*) FROM tbUsuarios WHERE usuario = ?";
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, usuario);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // En caso de error, asumimos que no existe para no bloquear
    }
}
