package configurations;

import org.intocps.maestrov2.scala.ConfigurationLoader.ConfigurationLoader;
import org.intocps.maestrov2.scala.configuration.datatypes.MultiModelConfigurationJava;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class ConfigurationLoaderTests {

    @Test
    public void testLoadMMCFile() throws IOException {
        File f = new File("src/test/resources/single-watertank.json");
        System.out.println(f.getAbsolutePath());
        MultiModelConfigurationJava mmcActual = ConfigurationLoader.loadMMCFromFile(f);
        Assert.assertTrue(mmcActual.fmus.containsKey("{control}"));
    }

}
