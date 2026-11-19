package com.campusdual.cd2026mnd1g3;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Carga datos B2C iniciales exclusivamente en la base clinicadb. */
@Component
public class ClinicaDataInitializer implements CommandLineRunner {
    private final DataSource clinicaDataSource;

    public ClinicaDataInitializer(@Qualifier("clinicaDataSource") DataSource clinicaDataSource) {
        this.clinicaDataSource = clinicaDataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = clinicaDataSource.getConnection()) {
            if (count(connection, "SELECT COUNT(*) FROM propietarios") > 0) {
                return;
            }
            connection.setAutoCommit(false);
            Long jose = owner(connection, "José", "García López", "+34600111222", "jose.garcia@example.com", "12345678A");
            Long maria = owner(connection, "María", "Fernández Soto", "+34600222333", "maria.fernandez@example.com", "23456789B");
            Long carlos = owner(connection, "Carlos", "Rodríguez", "+34600333444", "carlos.rodriguez@example.com", "34567890C");
            Long ana = owner(connection, "Ana", "Pérez", "+34600444555", "ana.perez@example.com", "45678901D");

            Long toby = pet(connection, jose, "Toby", "PERRO", "Labrador", "941000001234567", "2020-04-12", "AL_DIA", "2026-12-10");
            pet(connection, maria, "Luna", "PERRO", "Mestizo / Sin raza definida", "941000001234568", "2022-08-20", "PROXIMA_A_VENCER", "2026-11-30");
            pet(connection, carlos, "Rocky", "PERRO", "Bulldog Francés", null, "2025-05-10", "AL_DIA", "2026-12-15");
            pet(connection, ana, "Misifu", "GATO", "Europeo Común", "941000001234569", "2021-02-14", "AL_DIA", "2027-01-08");
            pet(connection, jose, "Nube", "GATO", "Persa", null, "2023-09-03", "VENCIDA", "2026-10-01");
            pet(connection, maria, "Kiko", "PAJARO", null, null, "2024-01-18", "PROXIMA_A_VENCER", "2026-12-05");
            pet(connection, carlos, "Tambor", "OTRO", "Conejo Belier", null, "2024-06-22", "AL_DIA", "2027-03-01");

            Long c1 = consultation(connection, toby, "2026-11-18", "Vacunación anual", "Paciente estable");
            Long c2 = consultation(connection, petId(connection, "Luna"), "2026-11-17", "Revisión preventiva", "Próxima vacuna pendiente");
            Long c3 = consultation(connection, petId(connection, "Misifu"), "2026-11-16", "Control dermatológico", "Evolución favorable");
            Long c4 = consultation(connection, petId(connection, "Nube"), "2026-11-15", "Vacuna vencida", "Programar refuerzo");
            recipe(connection, c1, "Vacuna polivalente canina", "1 dosis", "Administrar una dosis anual");
            recipe(connection, c2, "Antiparasitario externo", "1 aplicación", "Repetir según indicación");
            recipe(connection, c3, "Solución dérmica", "Aplicar 1 vez/día", "Durante 7 días");
            recipe(connection, c4, "Vacuna trivalente felina", "1 dosis", "Aplicar refuerzo");
            connection.commit();
        }
    }

    private int count(Connection connection, String sql) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getInt(1);
        }
    }

    private Long owner(Connection connection, String nombre, String apellidos, String phone,
                       String email, String dni) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO propietarios (clinica_id, nombre, apellidos, telefono_e164, email, dni) VALUES (1, ?, ?, ?, ?, ?) ",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, nombre);
            statement.setString(2, apellidos);
            statement.setString(3, phone);
            statement.setString(4, email);
            statement.setString(5, dni);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    private Long pet(Connection connection, Long ownerId, String name, String species, String breed,
                     String microchip, String birthDate, String vaccinationStatus, String nextVaccination) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO paciente_mascotas (clinica_id, propietario_id, nombre, especie, raza, microchip, fecha_nacimiento, estado_vacunal, proxima_vacuna) VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?) ",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, ownerId);
            statement.setString(2, name);
            statement.setString(3, species);
            statement.setString(4, breed);
            statement.setString(5, microchip);
            statement.setObject(6, LocalDate.parse(birthDate));
            statement.setString(7, vaccinationStatus);
            statement.setObject(8, LocalDate.parse(nextVaccination));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    private Long petId(Connection connection, String name) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
            "SELECT id FROM paciente_mascotas WHERE nombre = ?");
             ) {
            statement.setString(1, name);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getLong(1);
            }
        }
    }

    private Long consultation(Connection connection, Long petId, String date, String reason, String diagnosis) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO consultas_clinica (clinica_id, mascota_id, fecha, motivo, diagnostico) VALUES (1, ?, ?, ?, ?) ",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, petId);
            statement.setTimestamp(2, Timestamp.valueOf(LocalDate.parse(date).atStartOfDay()));
            statement.setString(3, reason);
            statement.setString(4, diagnosis);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    private void recipe(Connection connection, Long consultationId, String medicine, String dose, String indication) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO recetas_clinica (consulta_id, medicamento, dosis, indicaciones) VALUES (?, ?, ?, ?)")) {
            statement.setLong(1, consultationId);
            statement.setString(2, medicine);
            statement.setString(3, dose);
            statement.setString(4, indication);
            statement.executeUpdate();
        }
    }
}
