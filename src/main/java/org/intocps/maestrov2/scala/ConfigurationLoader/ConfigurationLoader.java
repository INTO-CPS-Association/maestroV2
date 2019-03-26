package org.intocps.maestrov2.scala.ConfigurationLoader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.intocps.maestrov2.scala.configuration.datatypes.MultiModelConfigurationJava;

import java.io.File;
import java.io.IOException;

public class ConfigurationLoader {
    public static MultiModelConfigurationJava loadMMCFromFile(File path) throws IOException {
        return new ObjectMapper().readValue(path, MultiModelConfigurationJava.class);
    }
}
