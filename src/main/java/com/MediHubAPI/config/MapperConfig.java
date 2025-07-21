package com.MediHubAPI.config;

import com.MediHubAPI.dto.*;
import com.MediHubAPI.model.Appointment;
import com.MediHubAPI.model.Patient;
import com.MediHubAPI.model.User;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {


    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Enable private field mapping
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setAmbiguityIgnored(true);


        // Prevent mapping to 'id' field explicitly
        // Mapping: PatientCreateDto -> Patient (skip ID)

        TypeMap<PatientCreateDto, Patient> typeMap = modelMapper.createTypeMap(PatientCreateDto.class, Patient.class);
        typeMap.addMappings(mapper -> mapper.skip(Patient::setId));

        // Mapping: User -> DoctorProfileDto (skip ID)
        TypeMap<User, DoctorProfileDto> doctorMap = modelMapper.createTypeMap(User.class, DoctorProfileDto.class);
        doctorMap.addMappings(mapper -> mapper.skip(DoctorProfileDto::setId));

        // Mapping: Appointment -> WalkInAppointmentDto (skip time)
        modelMapper.typeMap(Appointment.class, WalkInAppointmentDto.class)
                .addMappings(mapper -> mapper.skip(WalkInAppointmentDto::setTime));

        return modelMapper;
    }
}
