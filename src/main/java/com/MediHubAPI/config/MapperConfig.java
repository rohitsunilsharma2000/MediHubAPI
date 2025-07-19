package com.MediHubAPI.config;

import com.MediHubAPI.dto.PatientCreateDto;
import com.MediHubAPI.model.Patient;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {
//    @Bean
//    public ModelMapper modelMapper() {
//        return new ModelMapper();
//    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Enable private field mapping
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setAmbiguityIgnored(true);


        // Prevent mapping to 'id' field explicitly
        TypeMap<PatientCreateDto, Patient> typeMap = modelMapper.createTypeMap(PatientCreateDto.class, Patient.class);
        typeMap.addMappings(mapper -> mapper.skip(Patient::setId));

        return modelMapper;
    }
}
