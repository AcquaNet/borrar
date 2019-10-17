package org.mule.modules.dragonfish.automation.runner;

import org.mule.modules.atila.jde.DragonfishConnector;
import org.mule.modules.dragonfish.automation.functional.EjecutarOperacionObtencionDePrecioTestCases;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.mule.tools.devkit.ctf.mockup.ConnectorTestContext;

@RunWith(Suite.class)
@SuiteClasses({
        EjecutarOperacionObtencionDePrecioTestCases.class
})
public class FunctionalObtencionDePrecioTestSuite {

    @BeforeClass
    public static void initialiseSuite() {

        ConnectorTestContext.initialize(DragonfishConnector.class);
    }

    @AfterClass
    public static void shutdownSuite() throws Exception {
        ConnectorTestContext.shutDown();
    }
}
